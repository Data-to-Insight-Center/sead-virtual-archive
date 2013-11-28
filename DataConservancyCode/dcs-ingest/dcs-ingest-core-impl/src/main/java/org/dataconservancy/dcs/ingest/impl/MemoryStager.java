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
package org.dataconservancy.dcs.ingest.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.model.dcp.Dcp;

/**
 * Simple, non-durable memory store for staged SIPs.
 * <p>
 * Intended for testing or demonstration purposes.
 */
public class MemoryStager
        implements SipStager {

    private AtomicInteger counter = new AtomicInteger();

    private Map<String, Dcp> _map = new ConcurrentHashMap<String, Dcp>();

    private boolean finishDeletes = true;

    public void setDeleteUponRetire(boolean delete) {
        finishDeletes = delete;
    }

    public String addSIP(Dcp sip) {
        String key = generateKey();
        _map.put(key, sip);
        return key;
    }

    public Dcp getSIP(String key) {
        return _map.get(key);
    }

    public void removeSIP(String key) {
        _map.remove(key);
    }

    public void retire(String id) {
        if (finishDeletes) {
            removeSIP(id);
        }
    }

    public void updateSIP(Dcp sip, String key) {
        _map.put(key, sip);
    }

    private String generateKey() {
        return new StringBuilder().append("ing-").append(counter
                .incrementAndGet()).toString();
    }

    public Set<String> getKeys() {
        return new HashSet<String>(_map.keySet());
    }
}
