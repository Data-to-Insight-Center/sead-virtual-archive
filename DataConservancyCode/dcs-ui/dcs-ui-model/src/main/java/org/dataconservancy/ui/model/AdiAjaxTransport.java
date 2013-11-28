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

import java.util.ArrayList;
import java.util.List;

/**
 * {@code AdiAjaxTransport} encapsulates a list of DataItemTransports and any messages from the retrieval of that list.
 */
public class AdiAjaxTransport {

    private List<DataItemTransport> dataItemTransportList = new ArrayList<DataItemTransport>();


    private String message;

    public AdiAjaxTransport() {}

    public AdiAjaxTransport(String message, List<DataItemTransport> dataItemTransportList) {
        this.message = message;
        this.dataItemTransportList = dataItemTransportList;
    }

    public void setDataItemTransportList(List<DataItemTransport> dataItemTransportList) {
        this.dataItemTransportList = dataItemTransportList;
    }

    public List<DataItemTransport> getDataItemTransportList() {
        return dataItemTransportList;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
