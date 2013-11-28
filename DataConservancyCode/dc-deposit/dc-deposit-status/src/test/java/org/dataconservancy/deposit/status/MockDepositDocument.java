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
package org.dataconservancy.deposit.status;

import java.io.InputStream;

import java.util.Map;

import org.dataconservancy.deposit.DepositDocument;

public class MockDepositDocument
        implements DepositDocument {

    private InputStream istream;

    private long lastModified;

    private Map<String, String> fileMetadata;

    private String mimeType;

    public void setInputStream(InputStream i) {
        this.istream = i;
    }

    public InputStream getInputStream() {
        return istream;
    }

    public void setLastModified(long modified) {
        lastModified = modified;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setMetadata(Map<String, String> metadata) {
        fileMetadata = metadata;
    }

    public Map<String, String> getMetadata() {
        return fileMetadata;
    }

    public void setMimeType(String mime) {
        mimeType = mime;
    }

    public String getMimeType() {
        return mimeType;
    }
}
