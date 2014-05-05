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
package org.dataconservancy.dcs.access.client.model;

import com.google.gwt.core.client.JsArray;
import org.dataconservancy.dcs.access.ui.client.model.JsModel;
/**
 * Result from calling an DCS access service search.
 */
public final class JsSearchResult
        extends JsModel{

    protected JsSearchResult() {
    }

    public static JsSearchResult create(String jsonString) {
        return (JsSearchResult) JsModel
                .parseJSON(jsonString);
    }

    @SuppressWarnings("unchecked")
    public JsArray<JsMatch> matches() {
        return (JsArray<JsMatch>) getArray("matches");
    }

    public long total() {
        return getLong("total");
    }

    // TODO for some reason offset set to empty string
    public int offset() {
        return getInt("offset");
    }
}
