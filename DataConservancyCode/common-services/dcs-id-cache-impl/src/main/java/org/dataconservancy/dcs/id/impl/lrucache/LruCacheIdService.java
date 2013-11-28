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
package org.dataconservancy.dcs.id.impl.lrucache;

import org.apache.commons.collections.map.LRUMap;
import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class LruCacheIdService implements IdService, BulkIdCreationService {

    private final IdService delegate;

    @SuppressWarnings("unchecked")
    private Map<String, Identifier> idCache =
            Collections.<String, Identifier>synchronizedMap(new LRUMap(5000));

    @SuppressWarnings("unchecked")
    private Map<URL, Identifier> urlCache =
            Collections.<URL, Identifier>synchronizedMap(new LRUMap(5000));

    public LruCacheIdService(IdService delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate ID Service must not be null.");
        }
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    public LruCacheIdService(IdService delegate, int cacheSize) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate ID Service must not be null.");
        }
        this.delegate = delegate;

        if (cacheSize < 1) {
            throw new IllegalArgumentException("Cache size must be a positive integer.");
        }

        idCache = Collections.<String, Identifier>synchronizedMap(new LRUMap(cacheSize));
        urlCache = Collections.<URL, Identifier>synchronizedMap(new LRUMap(cacheSize));
    }

    @Override
    public Identifier create(String type) {
        Identifier id = delegate.create(type);
        idCache.put(id.getUid(), id);
        urlCache.put(id.getUrl(), id);
        return id;
    }

    @Override
    public List<Identifier> create(int count, String type) {
        if (!(delegate instanceof BulkIdCreationService)) {
            throw new UnsupportedOperationException("Delegate does not implement the BulkIdCreationService interface.");
        }

        List<Identifier> ids = ((BulkIdCreationService) delegate).create(count, type);
        for (Identifier id : ids) {
            idCache.put(id.getUid(), id);
            urlCache.put(id.getUrl(), id);
        }

        return ids;
    }

    @Override
    public Identifier fromUid(String uid) throws IdentifierNotFoundException {
        Identifier identifier = null;
        if (idCache.containsKey(uid)) {
            identifier = idCache.get(uid);
        }

        if (identifier != null) {
            return identifier;
        }

        identifier = delegate.fromUid(uid);
        idCache.put(uid, identifier);
        urlCache.put(identifier.getUrl(), identifier);
        return identifier;
    }

    @Override
    public Identifier fromUrl(URL url) throws IdentifierNotFoundException {
        Identifier identifier = null;
        if (urlCache.containsKey(url)) {
            identifier = urlCache.get(url);
        }

        if (identifier != null) {
            return identifier;
        }

        identifier = delegate.fromUrl(url);
        idCache.put(identifier.getUid(), identifier);
        urlCache.put(url, identifier);
        return identifier;
    }
}
