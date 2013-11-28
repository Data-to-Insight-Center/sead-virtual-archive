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
package org.dataconservancy.dcs.query.api;

/**
 * An object which matched a query together with context for the match.
 */
public class QueryMatch<T> {
    private final T object;

    private final String context;

    public QueryMatch(T object, String context) {
        this.object = object;
        this.context = context;
    }

    /**
     * @return the matched object
     */
    public T getObject() {
        return object;
    }

    /**
     * The form of context is implementation dependent.
     * 
     * @return the context of the matched object or null
     */
    public String getContext() {
        return context;
    }

    public String toString() {
        return "{" + object + ", " + (context == null ? "" : context) + "}";
    }
}
