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
package org.dataconservancy.dcs.ingest.deposit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.deposit.DepositDocument;

public abstract class AbstractDepositDocument
        implements DepositDocument {

    protected abstract long getDocument(OutputStream out);

    private byte[] outputDoc;

    private boolean initialized;

    private long lastModDate;

    public InputStream getInputStream() {
        initIfNecessary();
        return new ByteArrayInputStream(outputDoc);
    }

    public long getLastModified() {
        initIfNecessary();
        return lastModDate;
    }

    public Map<String, String> getMetadata() {
        initIfNecessary();
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Length", Long.toString(outputDoc.length));

        return metadata;
    }

    private void initIfNecessary() {
        if (initialized) return;

        initialized = true;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        lastModDate = getDocument(out);
        outputDoc = out.toByteArray();
    }

}
