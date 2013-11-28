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
package org.dataconservancy.dcs.access.client.model;

import com.google.gwt.core.client.JsArray;

/**
 * An identifiable entity in the Data Conservancy model
 */
public class JsEntity
        extends org.dataconservancy.dcs.access.ui.client.model.JsEntity{

	protected JsEntity() {
	}
	 
    public final String getAbstract() {
        return getString("abstrct");
    }
    
    public final String getPubdate() {
        return getString("pubdate");
    }
    @SuppressWarnings("unchecked")
    public final JsArray<JsAlternateId> getAlternateIds() {
        return (JsArray<JsAlternateId>) getArray("alternateIds");
    }
    
    @SuppressWarnings("unchecked")
    public final JsArray<JsDataLocation> getDataLocations() {
        return (JsArray<JsDataLocation>) getArray("dataLocations");
    }
}
