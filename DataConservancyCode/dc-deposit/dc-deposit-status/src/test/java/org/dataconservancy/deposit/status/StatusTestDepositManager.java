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

import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.deposit.PackageException;

/** Mock deposit manager for testing deposit status functionality */
public class StatusTestDepositManager
        implements DepositManager {

    private final String id;

    private final Map<String, DepositInfo> status =
            new HashMap<String, DepositInfo>();

    public StatusTestDepositManager(String id) {
        this.id = id;
    }

    public DepositInfo deposit(InputStream pkg,
                               String contentType,
                               String packaging,
                               Map<String, String> metadata)
            throws PackageException {
        /* This class is not used for testing deposit */
        return null;
    }

    public String getManagerID() {
        return id;
    }

    public void setDepositStatus(String id, DepositInfo toReturn) {
        status.put(id, toReturn);
    }

    public DepositInfo getDepositInfo(String id) {
        return status.get(id);
    }
}
