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
package org.dataconservancy.dcs.id.impl.hibernate;

import java.net.MalformedURLException;
import java.net.URL;

import org.dataconservancy.dcs.id.api.Identifier;

public class IdWrapper
        implements Identifier {

    private final TypeInfo identity;

    private final String urlPrefix;

    public IdWrapper(TypeInfo info, String prefix) {
        identity = info;
        urlPrefix = prefix;
    }

    public URL getUrl() {
        try {
            return new URL(urlPrefix + identity.getId());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getType() {
        return identity.getType();
    }

    public String getUid() {
        return Long.toString(identity.getId());
    }

    public TypeInfo getPersistentIdentity() {
        return this.identity;
    }

    static long getId(String uid) {
        try {
            return new Long(uid);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Identifier)) {
            return false;
        }

        Identifier i = (Identifier) o;

        return i.getUid().equals(getUid()) && i.getType().equals(getType());
    }

    public int hashCode() {
        return getUid().hashCode();
    }

}
