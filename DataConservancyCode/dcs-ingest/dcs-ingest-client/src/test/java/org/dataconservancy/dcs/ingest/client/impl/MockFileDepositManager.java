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
package org.dataconservancy.dcs.ingest.client.impl;

import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.deposit.PackageException;

public class MockFileDepositManager
        implements DepositManager {

    public static final String FILE_SRC = "example/src";

    private DepositInfo last;

    public DepositInfo deposit(InputStream content,
                               String contentType,
                               String packaging,
                               Map<String, String> metadata)
            throws PackageException {

        last = new MockDepositInfo(metadata);
        return last;
    }

    public DepositInfo getDepositInfo(String id) {
        return new MockDepositInfo(new HashMap<String, String>());
    }

    @Override
    public String getManagerID() {
        return "manager_id";
    }

    public DepositInfo getLastDepositInfo() {
        return last;
    }

    private class MockDepositInfo
            implements DepositInfo {

        private final Map<String, String> metadata;

        public MockDepositInfo(Map<String, String> md) {
            metadata = md;
        }

        public DepositDocument getDepositContent() {
            return null;
        }

        @Override
        public String getDepositID() {
            return "id";
        }

        public DepositDocument getDepositStatus() {
            return null;
        }

        public String getManagerID() {
            return "1";
        }

        public Map<String, String> getMetadata() {

            metadata.put("X-dcs-src", FILE_SRC);

            return metadata;
        }

        public String getSummary() {
            return "";
        }

        public boolean hasCompleted() {
            return true;
        }

        public boolean isSuccessful() {
            return true;
        }
    }
}
