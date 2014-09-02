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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFormat;
import org.seadva.model.pack.ResearchObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

/**
 * Detects and adds mime characterization to files.
 * <p>
 * Simply adds characterizations that are not present. Does not verify existing
 * characterizations.
 * </p>
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setIngestFramework(IngestFramework)}</dt>
 * <dd><b>Required</b></dd>
 * </dl>
 */
public class SimpleMimeCharacterizer
        extends IngestServiceBase
        implements IngestService {

    private static final Logger log =
            LoggerFactory.getLogger(SimpleMimeCharacterizer.class);

    public static final String MIME_SCHEME =
            "http://www.iana.org/assignments/media-types/";

    static {
        MimeUtil.registerMimeDetector(MagicMimeMimeDetector.class.getName());
        MimeUtil.registerMimeDetector(ExtensionMimeDetector.class.getName());
    }

    public void execute(String sipRef) throws IngestServiceException {
        if (isDisabled()) return;

        Dcp sip = ingest.getSipStager().getSIP(sipRef);
        Set<DcsFile> files = new HashSet<DcsFile>();
        List<DcsEvent> events = new ArrayList<DcsEvent>();
        boolean modified = false;

        for (DcsFile file : sip.getFiles()) {
            Set<String> mimes = new HashSet<String>();

            /* Get mime types by name */
            for (Object mime : MimeUtil.getMimeTypes(file.getName())) {
                mimes.add(mime.toString());
            }

            /* Get mime types by fragment, if possible */
            for (Object mime : MimeUtil.getMimeTypes(getFragment(file
                    .getSource(), mimes))) {
                mimes.add(mime.toString());
            }

            if (updateFileInfo(file, mimes, events)) {
                modified = true;
            }

            File filePath = new File(file.getSource().replace("file://",""));
            long length = filePath.length();
            file.setSizeBytes(length);

            modified = true;
            files.add(file);
        }

        /* Set files and save sip if modified */
        if (modified) {
            sip.setFiles(files);
            ingest.getSipStager().updateSIP(sip, sipRef);
        }

        if (events.size() > 0) {
            ingest.getEventManager().addEvents(sipRef, events);
        }
    }

    public boolean updateFileInfo(DcsFile file,
                                  Set<String> mimeTypes,
                                  List<DcsEvent> events) {
        Set<String> discovered = cleanMimes(mimeTypes);
        Set<String> declared = getDeclaredMimes(file);
        boolean modified = false;

        for (String mime : discovered) {
            if (!declared.contains(mime)) {
                modified = true;
                addCharacterization(file, mime, events);
            }
        }

        if (discovered.isEmpty() && declared.isEmpty()) {
            /* Last ditch to have something */
            addCharacterization(file, "application/octet-stream", events);
        }

        return modified;
    }

    private void addCharacterization(DcsFile file,
                                     String mime,
                                     List<DcsEvent> events) {
        DcsFormat format = new DcsFormat();
        format.setSchemeUri(MIME_SCHEME);
        format.setFormat(mime);
        file.addFormat(format);

        DcsEvent formatEvent =
                ingest.getEventManager()
                        .newEvent(Events.CHARACTERIZATION_FORMAT);
        formatEvent.setOutcome(String.format("%s %s", MIME_SCHEME, mime));
        formatEvent
                .setDetail("Characterized " + file.getName() + " as " + mime);
        formatEvent.addTargets(new DcsEntityReference(file.getId()));
        events.add(formatEvent);
    }

    private Set<String> cleanMimes(Set<String> mimes) {
        Set<String> ret = new HashSet<String>();
        for (String mime : mimes) {
            if (!mime.contains("unknown")
                    && !mime.contains("application/octet-stream")) {
                ret.add(mime);
            }
        }
        return ret;
    }

    private Set<String> getDeclaredMimes(DcsFile file) {
        Set<String> mimes = new HashSet<String>();

        for (DcsFormat fmt : file.getFormats()) {
            if (fmt.getSchemeUri().equals(MIME_SCHEME)) {
                mimes.add(fmt.getFormat());
            }
        }

        return mimes;
    }

    public byte[] getFragment(String src, Set<String> mimes) {
        byte[] frag = new byte[512];

        InputStream content = null;
        try {
            URL url = new URL(src);
            URLConnection conn = url.openConnection();

            /*
             * Get the content type header if it's an http url. We could do
             * this, but it isn't reallt "official", so it probably belongs
             * somewhere else
             */
            //String contentType = conn.getContentType();
            //if (contentType != null) {
            //    mimes.add(contentType);
            //}

            content = conn.getInputStream();
            content.read(frag);
        } catch (Exception e) {
            log
                    .warn(String
                                  .format("Could not get content fragment from %s for characterization",
                                          src),
                          e);
        } finally {
            if (content != null) {
                try {
                    content.close();
                } catch (IOException e) {
                }
            }
        }

        return frag;
    }
}
