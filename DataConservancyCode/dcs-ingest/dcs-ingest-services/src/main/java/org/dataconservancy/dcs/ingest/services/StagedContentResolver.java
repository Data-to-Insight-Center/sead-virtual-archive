/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.ingest.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.FileContentStager;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.StagedFile;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;
import org.seadva.model.pack.ResearchObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes file src links for uploaded files from opaque IDs to internal
 * resolvable URIs, and copies relevant info from uploaded file.
 * <p>
 * Files introduced to the DCS through a {@link FileContentStager} (i.e. through
 * file upload deposit) are referenced in a SIP through their "reference URI",
 * which is ann opaque, non-resolvable URI handle to the file. This service
 * replaces this value in the SIP with a valid, resolvable internal URI (for
 * example, a file:// uri pointing to an actual file)
 * </p>
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setIngestFramework(IngestFramework)}</dt>
 * <dd><b>Required</b>. Contains the sipStager from which the sip will be
 * pulled, and an event manager into which to create the archive event.</dd>
 * </dl>
 */
public class StagedContentResolver
        extends IngestServiceBase
        implements IngestService {

    private static final Logger log =
            LoggerFactory.getLogger(StagedContentResolver.class);

    @Override
    public void execute(String sipRef) {
        if (isDisabled()) return;

        FileContentStager stager = ingest.getFileContentStager();

        Dcp sip = ingest.getSipStager().getSIP(sipRef);

        Collection<DcsFile> files = sip.getFiles();

        ArrayList<DcsEvent> events = new ArrayList<DcsEvent>();

        boolean resolvedFile = false;

        for (DcsFile file : files) {
            if (stager.contains(file.getSource())) {
                resolvedFile = true;

                StagedFile staged = stager.get(file.getSource());
                String accessURI = staged.getAccessURI();
                String referenceURI = staged.getReferenceURI();
                DcsFile stagedFile =
                        ingest.getSipStager().getSIP(staged.getSipRef())
                                .getFiles().iterator().next();

                /* Copy relevant info from staged file to sip file */
                events.addAll(copyFileInfo(staged, sipRef, file, stagedFile));

                /*
                 * Replace the referenceURI in the file source with the
                 * accessURI
                 */
                file.setSource(accessURI);

                /* Create a staged file content resolution event */
                DcsEvent resolution =
                        ingest.getEventManager()
                                .newEvent(Events.FILE_RESOLUTION_STAGED);
                resolution.setOutcome(referenceURI + " to " + accessURI);
                resolution
                        .setDetail("Resolved staged content, and changed src from "
                                + referenceURI + " to " + accessURI);
                resolution.addTargets(new DcsEntityReference(file.getId()));
                events.add(resolution);

                /* Copy the file upload/download event from the staged file */
                DcsEvent stagedFileEvent =
                        ingest.getEventManager()
                                .getEventByType(staged.getSipRef(),
                                                Events.FILE_UPLOAD);
                if (stagedFileEvent == null) {
                    stagedFileEvent =
                            ingest.getEventManager()
                                    .getEventByType(staged.getSipRef(),
                                                    Events.FILE_DOWNLOAD);
                }

                if (stagedFileEvent != null) {
                    DcsEvent copy = new DcsEvent(stagedFileEvent);
                    copy.setTargets(new HashSet<DcsEntityReference>());
                    copy.addTargets(new DcsEntityReference(file.getId()));
                    events.add(copy);
                } else {
                    log
                            .warn("Could not find file transfer event to associate with "
                                    + file.getId());
                }
            }
        }

        if (resolvedFile) {
            sip = ingest.getSipStager().getSIP(sipRef);
            sip.setFiles(files);
            ingest.getSipStager().updateSIP(sip, sipRef);

            ingest.getEventManager().addEvents(sipRef, events);
        }
    }

    private Collection<DcsEvent> copyFileInfo(StagedFile staged,
                                              String sipRef,
                                              DcsFile file,
                                              DcsFile stagedFile) {

        ArrayList<DcsEvent> events = new ArrayList<DcsEvent>();

        /*
         * If no filename was provided in the sip, see if one was provided
         * during the upload
         */
        if (file.getName() == null) {
            file.setName(stagedFile.getName());
        }

        events.addAll(copyFixity(staged, stagedFile, sipRef, file));

        return events;

    }

    private Collection<DcsEvent> copyFixity(StagedFile staged,
                                            DcsFile stagedFile,
                                            String sipRef,
                                            DcsFile file) {

        ArrayList<DcsEvent> events = new ArrayList<DcsEvent>();

        /* Get fixity values specified in the sip */
        Map<String, DcsFixity> fileFixity = new HashMap<String, DcsFixity>();
        for (DcsFixity f : file.getFixity()) {
            fileFixity.put(f.getAlgorithm(), f);
        }

        /*
         * For each uploaded fixity value, compare with value specified in the
         * sip. Add if not present. Also, add the staged file fixity calculation
         * events if there are none in the sip.
         */
        for (DcsFixity stagedFixity : stagedFile.getFixity()) {

            if (!fileFixity.containsKey(stagedFixity.getAlgorithm())) {
                file.addFixity(stagedFixity);
            } else {
                String origValue =
                        fileFixity.get(stagedFixity.getAlgorithm()).getValue();
                String calculatedValue = stagedFixity.getValue();

                if (!origValue.equals(calculatedValue)) {
                    throw new RuntimeException(String
                            .format("Provided %s %s does not match calculated value %s",
                                    stagedFixity.getAlgorithm(),
                                    origValue,
                                    calculatedValue));
                }
            }

            /*
             * If the sip file does not already have an associated fixity event,
             * but the staged file does, then add a copy of the fixity event to
             * the sip file's native sip.
             */
            if (getFixityEvent(sipRef, file.getId(), stagedFixity
                    .getAlgorithm()) == null) {
                DcsEvent stagedFileFixity =
                        getFixityEvent(staged.getSipRef(),
                                       stagedFile.getId(),
                                       stagedFixity.getAlgorithm());

                if (stagedFileFixity != null) {
                    DcsEvent copy = new DcsEvent(stagedFileFixity);
                    copy.setTargets(new HashSet<DcsEntityReference>());
                    copy.addTargets(new DcsEntityReference(file.getId()));
                    events.add(copy);
                }
            }
        }

        return events;
    }

    private DcsEvent getFixityEvent(String sipid,
                                    String fileid,
                                    String algorithm) {

        DcsEntityReference fileRef = new DcsEntityReference(fileid);

        /* For any fixity event in a sip.. */
        for (DcsEvent event : ingest.getEventManager()
                .getEvents(sipid, Events.FIXITY_DIGEST)) {

            /* If it is attached to our file and uses the given algorithm.. */
            if (event.getOutcome().contains(algorithm)
                    && event.getTargets().contains(fileRef)) {
                return event;
            }
        }

        return null;
    }
}
