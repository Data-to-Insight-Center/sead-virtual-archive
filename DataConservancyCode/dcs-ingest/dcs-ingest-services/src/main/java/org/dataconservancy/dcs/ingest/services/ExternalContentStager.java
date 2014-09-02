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

import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.codec.binary.Hex;

import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.FileContentStager;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.StagedFile;
import org.dataconservancy.dcs.util.DigestListener;
import org.dataconservancy.dcs.util.DigestNotificationStream;
import org.dataconservancy.dcs.util.HttpHeaderUtil;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;
import org.seadva.model.pack.ResearchObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.seadva.model.SeadDeliverableUnit;

/**
 * Retrieves and stages extant content included by reference.
 * <p>
 * File src values pointing to file or http uris are dereferenced, and the
 * content put into a {@link FileContentStager}. Optionally, fixity is
 * calculalted on the way in.
 * </p>
 * <p>
 * TODO: take advantage of any checksum sent along with http sources.
 * </p>
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setIngestFramework(IngestFramework)}</dt>
 * <dd><b>Required</b>.</dd>
 * <dt>{@link #setAlwaysCalculateFixityFor(String...)}</dt>
 * <dd>Optional. All algorithms specified here will be computed upon deposit,
 * and compared with declared values. This may be exploited in order to pass
 * along computed checksums to the file stager</dd>
 * </dl>
 */
public class ExternalContentStager
        extends IngestServiceBase
        implements IngestService {

    private static final Logger log =
            LoggerFactory.getLogger(ExternalContentStager.class);

    private String[] calculateFixity;

    public void setAlwaysCalculateFixityFor(String... algorithms) {
        calculateFixity = algorithms;
    }



    public void execute(String sipRef) throws IngestServiceException {
        if (isDisabled()) return;
        FileContentStager fileStager = ingest.getFileContentStager();
        Dcp sip = ingest.getSipStager().getSIP(sipRef);
        Set<DcsFile> files = new HashSet<DcsFile>();
        //Set files = new HashSet();

        boolean modified = false;
        for (DcsFile file : sip.getFiles()) {
            if (file.getSource().startsWith("/")) {
                file.setSource("file://" + file.getSource());
            }

            if ((file.isExtant()) && (!fileStager.contains(file.getSource()))) {
                stageExternalFile(file);
                modified = true;
            }
            files.add(file);
        }

        if (modified) {
            sip.setFiles(files);
            ingest.getSipStager().updateSIP((ResearchObject)sip, sipRef);
        }
    }

    private void stageExternalFile(DcsFile file) {

        //List<DcsEvent> eventsToAdd = new ArrayList<DcsEvent>();
        List eventsToAdd = new ArrayList();
        URL fileUrl;
        try {
            fileUrl = new URL(file.getSource());
        } catch (MalformedURLException e) {
            throw new RuntimeException(String
                    .format("Invalid file content url in file %s: %s", file
                            .getId(), file.getSource()));
        }

        HashMap<String, String> metadata = new HashMap<String, String>();

        if (file.getName() == null) {
            file.setName(getFileName(fileUrl));
        }

        /* Stage the content */
        InputStream stream = null;
        StagedFile staged = null;
        try {
            InputStream src = null;
            if(file.getSource().contains("file:"))
                src = fileUrl.openStream();
            else if (file.getSource().contains("http:"))
            {
                String loginPassword = "kavchand@indiana.edu"+ ":" + "maximus";
                String encoded = new sun.misc.BASE64Encoder().encode (loginPassword.getBytes());
                URLConnection conn = fileUrl.openConnection();
                conn.setConnectTimeout(15*1000);
                conn.setReadTimeout(15*1000);
                conn.setRequestProperty ("Authorization", "Basic " + encoded);
                src = conn.getInputStream();
            }
            stream = fixityFilter(src, metadata, eventsToAdd);
            staged = ingest.getFileContentStager().add(stream, metadata);
            file.setSource(staged.getReferenceURI());
        } catch (IOException e) {
            throw new RuntimeException("Error getting content from "
                    + fileUrl.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {

                }
            }
        }

        /*
         * Add digest and download events to the staged content's sip, as well
         * as fixity values themselves
         */
        updateStagedSip(staged.getSipRef(), fileUrl, metadata, eventsToAdd);

    }

    private void updateStagedSip(String sipRef,
                                 URL url,
                                 Map<String, String> metadata,
                                 List<DcsEvent> events) {

        Dcp stagedSip = ingest.getSipStager().getSIP(sipRef);
        DcsFile fileEntity = stagedSip.getFiles().iterator().next();

        /* First, create the download event, but don't assign it target yet */
        DcsEvent downloadEvent =
                ingest.getEventManager().newEvent(Events.FILE_DOWNLOAD);
        downloadEvent.setOutcome(url.toString() + " to "
                + fileEntity.getSource());
        downloadEvent.setDetail("Retrieved file content from " + url
                + ", changed src to " + fileEntity.getSource());
        events.add(downloadEvent);

        /* add fixity values to staged file */
        for (DcsEvent fixityEvent : events) {
            if (!fixityEvent.getEventType().equals(Events.FIXITY_DIGEST))
                continue;
            String[] outcome = fixityEvent.getOutcome().split(" ");

            DcsFixity fixity = new DcsFixity();
            fixity.setAlgorithm(outcome[0]);
            fixity.setValue(outcome[1]);
            fileEntity.addFixity(fixity);
        }
        ingest.getSipStager().updateSIP((ResearchObject)stagedSip, sipRef);

        /*
         * Set the staged content file as the target for all events, and add
         * them
         */
        for (DcsEvent e : events) {
            e.addTargets(new DcsEntityReference(fileEntity.getId()));
        }
        ingest.getEventManager().addEvents(sipRef, events);
    }

    private String getFileName(URL uriPath) {
        String[] parts = uriPath.getPath().split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        } else {
            return uriPath.getPath();
        }
    }

    /*
     * For each specified algorithm, calculate the digest and create a fixity
     * event
     */
    private InputStream fixityFilter(InputStream src,
                                     final Map<String, String> metadata,
                                     final List<DcsEvent> events) {
        /* If there are no specific fixity checks, don't do anything */

        InputStream filtered = src;
        if (calculateFixity != null) {
            for (final String algorithm : calculateFixity) {
                filtered =
                        getFilterStream(algorithm, events, filtered, metadata);
            }
        }
        return filtered;
    }

    private InputStream getFilterStream(final String algorithm,
                                        final List<DcsEvent> events,
                                        InputStream filtered,
                                        final Map<String, String> metadata) {
        try {
            return new DigestNotificationStream(filtered, MessageDigest
                    .getInstance(algorithm), new DigestListener() {

                public void notify(byte[] digestValue) throws IOException {
                    events.add(newFixityEvent(algorithm, digestValue));
                    HttpHeaderUtil.addDigest(algorithm, digestValue, metadata);
                }
            });
        } catch (NoSuchAlgorithmException e) {
            log.warn("Provider does not support algorithm %s, skipping check",
                     algorithm);
            return filtered;
        }
    }

    private DcsEvent newFixityEvent(String algorithm, byte[] value) {
        DcsEvent fixity =
                ingest.getEventManager().newEvent(Events.FIXITY_DIGEST);
        fixity.setOutcome(algorithm + " " + new String(Hex.encodeHex(value)));
        fixity.setDetail("Calculated " + algorithm + " upon content retrieval");
        return fixity;
    }
}
