/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.mhf.instances;

/**
 * Capture the system activities related metadata.
 */
public class SystemMetadata {
    /**
     * The date on which an entity is deposited into the archive.
     */
   private String depositDate;

    /**
     * Unique identifier to the person who deposited the entity.
     */
    private String depositorId;

    public String getDepositDate() {
        return depositDate;
    }

    public void setDepositDate(String depositDate) {
        this.depositDate = depositDate;
    }

    public String getDepositorId() {
        return depositorId;
    }

    public void setDepositorId(String depositorId) {
        this.depositorId = depositorId;
    }

    public boolean isEmpty() {
        if (depositDate != null && !depositDate.isEmpty()) {
            return false;
        } else if (depositorId != null && !depositorId.isEmpty()) {
            return false;
        } else {
            return true;
        }

    }

    @Override
    public String toString() {
        return "SystemMetadata{" +
                "depositDate='" + depositDate + '\'' +
                ", depositorId='" + depositorId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemMetadata)) return false;

        SystemMetadata that = (SystemMetadata) o;

        if (depositDate != null ? !depositDate.equals(that.depositDate) : that.depositDate != null) return false;
        if (depositorId != null ? !depositorId.equals(that.depositorId) : that.depositorId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = depositDate != null ? depositDate.hashCode() : 0;
        result = 31 * result + (depositorId != null ? depositorId.hashCode() : 0);
        return result;
    }
}
