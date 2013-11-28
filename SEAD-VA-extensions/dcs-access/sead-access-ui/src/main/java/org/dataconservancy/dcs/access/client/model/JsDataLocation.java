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

public final class JsDataLocation
        extends org.dataconservancy.dcs.access.ui.client.model.JsModel {

    protected JsDataLocation() {
    }

    public String getType() {
        return getString("type");
    }
    
    public String getLocation() {
        return getString("location");
    }
    
    public String getName(){
    	return getString("name");
    }

}
