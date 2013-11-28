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
package org.dataconservancy.model.gqm;

import java.net.URI;

/**
 * The subject of the relation is the GQM which contains the relation.
 */
public class Relation {
    private URI predicate;
    private String object;

    public Relation(URI predicate, String object) {
        this.predicate = predicate;
        this.object = object;
    }

    public URI getPredicate() {
        return predicate;
    }

    public void setPredicate(URI predicate) {
        this.predicate = predicate;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public int hashCode() {
        return object == null ? 0 : object.hashCode();
    }

    public boolean equals(Object o) {
        Relation r = (Relation) o;

        if (r == null) {
            return false;
        }

        return Util.equals(predicate, r.predicate)
                && Util.equals(object, r.object);
    }

    public String toString() {
        return (predicate == null ? "" : predicate) + " -> "
                + (object == null ? "" : object);
    }
}
