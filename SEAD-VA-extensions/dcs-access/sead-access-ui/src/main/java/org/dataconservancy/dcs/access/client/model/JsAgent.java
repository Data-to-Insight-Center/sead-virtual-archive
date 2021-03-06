/*
 * Copyright 2014 The Trustees of Indiana University
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

import com.google.gwt.user.client.rpc.IsSerializable;
import org.dataconservancy.dcs.access.ui.client.model.JsModel;

/**
 * Models Data Contributor/Creator.
 */
public final class JsAgent
        extends JsModel implements IsSerializable {

    protected JsAgent() {
    }

    public String getAgentId() {
        return getString("prov:id");
    }

    public String getAgentUrl(){
        JsModel url = getObject("kom:url");
        if(url!=null)
            return ((JsUrl)url).getContent();
        else
            return null;
    }

    public String getAgentName(){
        JsModel name = getObject("kom:name");
        if(name!=null)
            return ((JsUrl)name).getContent();
        else
            return null;
    }

}
