/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.dcs.access.client.model;

import org.dataconservancy.dcs.access.ui.client.model.JsModel;
/**
 * Models a relationship between DCS entities.
 */
public final class JsSubmitter
        extends JsModel {

    protected JsSubmitter() {
    }

    public String getSubmitterName() {
        return getString("name");
    }

    public String getSubmitterId() {
        return getString("id");
    }
    
    public String getSubmitterIdType() {
        return getString("idType");
    }
}
