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
package org.dataconservancy.dcs.ingest.file.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;

import org.dataconservancy.dcs.ingest.FileContentStager;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.StagedFile;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;
import org.springframework.beans.factory.annotation.Required;

/**
 * Simple in-memory file content stager.
 * <p>
 * Maintains file content non-durably in memory. Intended primarily for testing
 * and integration. Does not give meaningful access URLs.
 * </p>
 */
public class MemoryFileContentStager
        implements FileContentStager {

    private static String URI_PREFIX = "memory:/";

    private SipStager sipStager;

    private static final AtomicInteger counter = new AtomicInteger();

    private static final Map<String, byte[]> content =
            new HashMap<String, byte[]>();

    @Required
    public void setSipStager(SipStager stager) {
        sipStager = stager;
    }

    public StagedFile add(InputStream stream, Map<String, String> metadata) {
        try {
            String id = Integer.toHexString(counter.incrementAndGet());
            content.put(id, IOUtils.toByteArray(stream));
            return new MemoryStagedFile(id, createSip(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean contains(String id) {

        if (id == null) return false;
        if (id.startsWith(URI_PREFIX)) {
            id = id.replace(URI_PREFIX, "");
        }

        return content.containsKey(getPathKeyFromSip(id));
    }

    public StagedFile get(String id) {

        if (id.startsWith(URI_PREFIX)) {
            id = id.replace(URI_PREFIX, "");
        }

        String key = getPathKeyFromSip(id);

        if (content.containsKey(key)) {
            return new MemoryStagedFile(key, id);
        } else {
            return null;
        }
    }

    public void remove(String id) {
        if (id.startsWith(URI_PREFIX)) {
            id = id.replace(URI_PREFIX, "");
        }
        content.remove(getPathKeyFromSip(id));
        sipStager.removeSIP(id);
    }

    public void retire(String id) {
        if (id.startsWith(URI_PREFIX)) {
            id = id.replace(URI_PREFIX, "");
        }
        sipStager.removeSIP(id);
    }

    private String getPathKeyFromSip(String sipid) {
        Dcp sip = sipStager.getSIP(sipid);

        if (sip != null) {
            for (DcsFile file : sip.getFiles()) {
                return file.getId().replace(URI_PREFIX, "");
            }
        }
        return null;
    }

    private String createSip(String key) {
        Dcp sip = new Dcp();
        String id = sipStager.addSIP(new Dcp());

        DcsFile file = new DcsFile();
        file.setId(URI_PREFIX + key);
        file.setSource(URI_PREFIX + id);
        sip.addFile(file);

        sipStager.updateSIP(sip, id);

        return id;
    }

    private class MemoryStagedFile
            implements StagedFile {

        private final String fileId;

        private final String sipRef;

        public MemoryStagedFile(String id, String sipRef) {
            fileId = id;
            this.sipRef = sipRef;
        }

        public String getAccessURI() {
            return URI_PREFIX + fileId;
        }

        public InputStream getContent() {
            return new ByteArrayInputStream(content.get(fileId));
        }

        public String getReferenceURI() {
            return URI_PREFIX + sipRef;
        }

        public String getSipRef() {
            return sipRef;
        }

    }

}
