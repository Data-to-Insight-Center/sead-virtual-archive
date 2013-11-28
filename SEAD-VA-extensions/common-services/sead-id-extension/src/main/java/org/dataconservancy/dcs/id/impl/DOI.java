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

package org.dataconservancy.dcs.id.impl;

import org.dataconservancy.dcs.id.api.IdMetadata;
import org.dataconservancy.dcs.id.api.Identifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * DOI creation utility
 */
public class DOI implements Identifier,IdMetadata {

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    private String targetUrl;
    private String uid;
    private Map<Metadata, String> metadata;


    @Override
    public URL getUrl(){
        URL url = null;
        try {
            url = new URL(targetUrl);  //To change body of implemented methods use File | Settings | File Templates.
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return url;
    }

    @Override
    public String getUid() {
        return uid;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public Map<Metadata, String> getMetadata() {
        return metadata;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setMetadata(Map<Metadata, String> mdata) {
        this.metadata = mdata;
    }
}
