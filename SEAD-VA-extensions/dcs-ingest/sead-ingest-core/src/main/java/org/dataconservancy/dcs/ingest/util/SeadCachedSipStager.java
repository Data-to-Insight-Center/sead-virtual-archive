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
package org.dataconservancy.dcs.ingest.util;

import org.apache.commons.collections.map.LRUMap;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.model.dcp.Dcp;
import org.seadva.ingest.SeadSipStager;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class SeadCachedSipStager
        implements SipStager {

    private int maxSize = 100;

    private SipStager delegate;

    private Map<String, ResearchObject> cache;

    @Required
    public void setDelegate(SipStager del) {
        delegate = del;
    }

    public void setSize(int sz) {
        maxSize = sz;
    }

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        cache = Collections.synchronizedMap(new CacheWriterMap(maxSize));
    }

    @PreDestroy
    public void shutdown() {
        for (Map.Entry<String, ResearchObject> entry : cache.entrySet()) {
            delegate.updateSIP(entry.getValue(), entry.getKey());
        }
    }

    public String addSIP(ResearchObject sip) {
        String id = delegate.addSIP(sip);
        cache.put(id, sip);
        return id;
    }

    public void retire(String id) {
        if (cache.containsKey(id)) {
            delegate.updateSIP((ResearchObject) cache.get(id), id);
            cache.remove(id);
        }
        delegate.retire(id);
    }

    public Set<String> getKeys() {
        return delegate.getKeys();
    }

    @Override
    public void updateSIP(Dcp sip, String id) {
        if (cache.containsKey(id)) {
            cache.put(id, (ResearchObject)sip);
        } else {
            delegate.updateSIP(sip, id);
        }
    }

    @Override
    public String addSIP(Dcp sip) {
        String id = delegate.addSIP(sip);
        cache.put(id, (ResearchObject)sip);
        return id;
    }

    public ResearchObject getSIP(String id) {
        if (cache.containsKey(id)) {
            // Return a copy of the cached Dcp, because Dcp is not thread-safe.
            return cache.get(id);
        } else {
            return (ResearchObject)delegate.getSIP(id);
        }
    }

    public void removeSIP(String id) {
        if (cache.containsKey(id)) {
            cache.remove(id);
        }
        delegate.removeSIP(id);

    }

    public void updateSIP(ResearchObject sip, String id) {
        if (cache.containsKey(id)) {
            cache.put(id, sip);
        } else {
            delegate.updateSIP(sip, id);
        }

    }

    private class CacheWriterMap
            extends LRUMap {

        private static final long serialVersionUID = 1L;

        public CacheWriterMap(int maxSize) {
            super(maxSize);
        }

        @Override
        protected boolean removeLRU(LinkEntry entry) {
            delegate.updateSIP((ResearchObject) entry.getValue(), (String) entry.getKey());
            return true;
        }
    }

}
