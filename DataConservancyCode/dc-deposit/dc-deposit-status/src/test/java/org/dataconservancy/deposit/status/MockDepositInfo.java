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

import java.util.Map;

import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.deposit.DepositInfo;

public class MockDepositInfo
        implements DepositInfo {

    private String deposit;

    private String manager;

    private String summary;

    private boolean successful = false;

    private boolean completed = false;

    private DepositDocument depositStatus;

    private DepositDocument depositContent;

    private Map<String, String> metadata;

    public MockDepositInfo(String depositId, String managerId) {
        deposit = depositId;
        manager = managerId;
    }

    public String getDepositID() {
        return deposit;
    }

    public String getManagerID() {
        return manager;
    }

    public void setCompleted(boolean val) {
        completed = val;
    }

    public boolean hasCompleted() {
        return completed;
    }

    public void setSuccessful(boolean val) {
        this.successful = val;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSummary(String sum) {
        summary = sum;
    }

    public String getSummary() {
        return summary;
    }

    public void setDepositContent(DepositDocument d) {
        depositContent = d;
    }

    public DepositDocument getDepositContent() {
        return depositContent;
    }

    public void setDepositStatus(DepositDocument d) {
        depositStatus = d;
    }

    public DepositDocument getDepositStatus() {
        return depositStatus;
    }

    public void setMetadata(Map<String, String> md) {
        metadata = md;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
