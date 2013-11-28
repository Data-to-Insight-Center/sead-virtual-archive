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
package org.dataconservancy.ui.model;

import org.joda.time.DateTime;

/**
 * Information about the deposit (or transaction) of an object into the archive.
 * <p/>
 * Upon a successful call (no exceptions are thrown) to any {@code deposit(...)} method on {@link ArchiveService}, an
 * instance of {@code ArchiveDepositInfo} is created and added to the {@link ArchiveDepositInfoDAO}.  The initial
 * state of the {@code ArchiveDepositInfo} added to the DAO will have:
 * <ul>
 *     <li>An object {@link #getObjectId() id}: the id of the business object being deposited</li>
 *     <li>An object {@link #getObjectType() type}: the type of the business object being deposited</li>
 *     <li>A deposit {@link #getDepositDateTime() date}: the date/time that the deposit method was invoked on the
 *         {@code ArchiveService}</li>
 *     <li>A deposit {@link #getDepositId() id}: the identifier of the deposit transaction with the archive</li>
 *     <li>A deposit {@link #getDepositStatus() status}: set to {@link ArchiveDepositInfo.Status#PENDING PENDING}</li>
 * </ul>
 * The remaining fields such as the object's {@link #getArchiveId() archive id} and {@link #getStateId() state id} are
 * set when {@link ArchiveService#pollArchive() pollArchive()} is called.  The {@link #getDepositStatus() deposit
 * status} will also be updated and set accordingly.
 */
public class ArchiveDepositInfo {

    /**
     * Identifier of the business object being deposited.
     */
    private String object_id;

    /**
     * Identifier of the business object in the archive
     */
    private String archive_id;

    /**
     * Identifier of the business object's state in the archive
     */
    private String state_id;

    /**
     * Identifier of the deposit (archive transaction) attempt
     */
    private String deposit_id;

    /**
     * Identifier of a parent deposit attempt (e.g. a DataFile deposit attempt will have this field set
     * to the deposit id of the DataItem it belongs to)
     */
    private String parent_deposit_id;

    private Status deposit_status;
    private Type object_type;
    private DateTime deposit_datetime;
    
    public enum Status {
        PENDING, FAILED, DEPOSITED;
    }

    public enum Type {
        COLLECTION, DATASET, METADATA_FILE, REGISTRY_ENTRY, DATA_FILE
    }

    public ArchiveDepositInfo() {
    }

    public ArchiveDepositInfo(ArchiveDepositInfo toCopy) {
        this.object_id = toCopy.object_id;
        this.archive_id = toCopy.archive_id;
        this.deposit_id = toCopy.deposit_id;
        this.state_id = toCopy.state_id;
        this.parent_deposit_id = toCopy.parent_deposit_id;
        this.deposit_status = toCopy.deposit_status;
        this.object_type = toCopy.object_type;
        this.deposit_datetime = toCopy.deposit_datetime;
    }

    public String getArchiveId() {
        return archive_id;
    }

    public void setArchiveId(String archive_id) {
        this.archive_id = archive_id;
    }

    public String getObjectId() {
        return object_id;
    }

    public void setObjectId(String object_id) {
        this.object_id = object_id;
    }

    public String getDepositId() {
        return deposit_id;
    }

    public void setDepositId(String deposit_id) {
        this.deposit_id = deposit_id;
    }

    public String getParentDepositId() {
        return parent_deposit_id;
    }

    public void setParentDepositId(String parent_deposit_id) {
        this.parent_deposit_id = parent_deposit_id;
    }

    public String getStateId() {
        return state_id;
    }

    public void setStateId(String state_id) {
        this.state_id = state_id;
    }

    public Status getDepositStatus() {
        return deposit_status;
    }

    public void setDepositStatus(Status status) {
        this.deposit_status = status;
    }

    public Type getObjectType() {
        return object_type;
    }

    public void setObjectType(Type deposit_type) {
        this.object_type = deposit_type;
    }

    public DateTime getDepositDateTime() {
        return deposit_datetime;
    }

    public void setDepositDateTime(DateTime datetime) {
        this.deposit_datetime  = datetime;
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((archive_id == null) ? 0 : archive_id.hashCode());
        result = prime
                * result
                + ((deposit_datetime == null) ? 0 : deposit_datetime.hashCode());
        result = prime * result
                + ((deposit_id == null) ? 0 : deposit_id.hashCode());
        result = prime * result
                        + ((parent_deposit_id == null) ? 0 : parent_deposit_id.hashCode());
        result = prime * result
                + ((deposit_status == null) ? 0 : deposit_status.hashCode());
        result = prime * result
                + ((object_id == null) ? 0 : object_id.hashCode());
        result = prime * result
                + ((object_type == null) ? 0 : object_type.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ArchiveDepositInfo other = (ArchiveDepositInfo) obj;
        if (archive_id == null) {
            if (other.archive_id != null)
                return false;
        } else if (!archive_id.equals(other.archive_id))
            return false;
        if (state_id == null) {
            if (other.state_id != null)
                return false;
        } else if (!state_id.equals(other.state_id))
            return false;
        if (deposit_datetime == null) {
            if (other.deposit_datetime != null)
                return false;
        } else if (!deposit_datetime.equals(other.deposit_datetime))
            return false;
        if (deposit_id == null) {
            if (other.deposit_id != null)
                return false;
        } else if (!deposit_id.equals(other.deposit_id))
            return false;
        if (parent_deposit_id == null) {
                    if (other.parent_deposit_id != null)
                        return false;
                } else if (!parent_deposit_id.equals(other.parent_deposit_id))
                    return false;
        if (deposit_status != other.deposit_status)
            return false;
        if (object_id == null) {
            if (other.object_id != null)
                return false;
        } else if (!object_id.equals(other.object_id))
            return false;
        if (object_type != other.object_type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ArchiveDepositInfo{" +
                "archive_id='" + archive_id + '\'' +
                ", object_id='" + object_id + '\'' +
                ", state_id='" + state_id + '\'' +
                ", deposit_id='" + deposit_id + '\'' +
                ", parent_deposit_id='" + parent_deposit_id + '\'' +
                ", deposit_status=" + deposit_status +
                ", object_type=" + object_type +
                ", deposit_datetime=" + deposit_datetime +
                '}';
    }
}
