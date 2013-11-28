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
package org.dataconservancy.dcs.integration.lineage;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.HashMap;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockIdService implements IdService {

    static Logger log = LoggerFactory.getLogger(MockIdService.class);

    static HashMap<String, Identifier> idStoreByUID =
            new HashMap<String, Identifier>();

    static HashMap<URL, Identifier> idStoreByURI =
            new HashMap<URL, Identifier>();
  
    @Override
    public Identifier create(String type) {
       
        return null;
    }
    
    public Identifier createIdentifier(String type, String uid) {
        String id = "http://localhost:8080";
        if (type.equals(Types.LINEAGE.getTypeName())) {
            id += "/lineage/" + uid;
        } else {
            id += "/entity/" + uid;
        }
        Identifier identifier = null;
        try {
            identifier = new IdentifierImpl(type, id, new URL(id));
        } catch (MalformedURLException e) {
            throw new RuntimeException(id + " must be a valid URL.");
        }
        insertIdentifier(identifier);
        return identifier;
    }

    @Override
    public Identifier fromUid(String uid) throws IdentifierNotFoundException {
        return selectIdentifierByUid(uid);
    }

    @Override
    public Identifier fromUrl(URL url) throws IdentifierNotFoundException {
        return selectIdentifierByUri(url);
    }
    
    private void insertIdentifier(Identifier identifier) {        
        idStoreByUID.put(identifier.getUid(), identifier);
        idStoreByURI.put(identifier.getUrl(), identifier);
    }
    
    private Identifier selectIdentifierByUid(String uid)
            throws IdentifierNotFoundException {
   
        log.info("Selecting Identifier by uid from ID Store. UID: " + uid);
        if (idStoreByUID.containsKey(uid)) {
            return idStoreByUID.get(uid);
        } else
            throw new IdentifierNotFoundException();

    }

    private Identifier selectIdentifierByUri(URL uri)
            throws IdentifierNotFoundException {

        log.info("Selecting Identifier by URI from ID Store. URI: "
                + uri.toString());

        if (idStoreByURI.containsKey(uri)) {
            return idStoreByURI.get(uri);
        } else
            throw new IdentifierNotFoundException();

    }

    public class IdentifierImpl implements Identifier {
        private final String type;
        private final String uid;
        private final URL url;

        public IdentifierImpl(String type, String uid, URL url) {
            this.type = type;
            this.uid = uid;
            this.url = url;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public URL getUrl() {
            return url;
        }

        @Override
        public String getUid() {
            return uid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IdentifierImpl that = (IdentifierImpl) o;

            if (type != null ? !type.equals(that.type) : that.type != null) return false;
            if (uid != null ? !uid.equals(that.uid) : that.uid != null) return false;
            if (url != null ? !url.equals(that.url) : that.url != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (uid != null ? uid.hashCode() : 0);
            result = 31 * result + (url != null ? url.hashCode() : 0);
            return result;
        }
    }
    
}
