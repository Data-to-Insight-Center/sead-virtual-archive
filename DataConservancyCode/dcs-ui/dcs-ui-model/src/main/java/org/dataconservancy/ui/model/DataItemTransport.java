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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code DataItemTransport} encapsulates a DataItem and its deposit information.
 */
public class DataItemTransport {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private DataItem dataItem;

    private DateTime initialDepositDate;

    private ArchiveDepositInfo.Status depositStatus;

    public DataItemTransport() {}

    public DataItemTransport(DataItem dataItem) {
        this.dataItem = dataItem;
    }

    public void setDataItem(DataItem dataItem) {
        this.dataItem = dataItem;
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    public void setInitialDepositDate(DateTime initialDepositDate) {
        this.initialDepositDate = initialDepositDate;
    }

    public DateTime getInitialDepositDate() {
        return initialDepositDate;
    }

    public void setDepositStatus(ArchiveDepositInfo.Status depositStatus) {
        this.depositStatus = depositStatus;
    }

    public ArchiveDepositInfo.Status getDepositStatus() {
        return depositStatus;
    }

    @Override
    public String toString() {
        return "dataItem=" +dataItem + ", depositStatus=" + depositStatus + " initialDepositDate=" + initialDepositDate.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataItemTransport that = (DataItemTransport) o;

        if (this.dataItem != null ? !this.dataItem.equals(that.dataItem) : that.dataItem != null) return false;
        if (this.depositStatus != null ? !this.depositStatus.equals(that.depositStatus) : that.depositStatus != null) return false;
        if (this.initialDepositDate != null ? !this.initialDepositDate.equals(that.initialDepositDate) : that.initialDepositDate != null) return false;
        return true;
    }


    @Override
    public int hashCode() {
        int result = this.dataItem != null ? this.dataItem.hashCode() : 0;
        result = 31 * result + (this.depositStatus != null ? this.depositStatus.hashCode() : 0);
        result = 31 * result + (this.initialDepositDate != null ? this.initialDepositDate.hashCode() : 0);
        return result;
    }
}
