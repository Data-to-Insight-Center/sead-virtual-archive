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
package org.dataconservancy.dcs.access.server.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

// TODO some ugliness because GWT doesn't support abstract superclasses or instanceof for overlay types

public class JsModel
        extends JavaScriptObject {

    public JsModel() {

    }

    public static native JavaScriptObject parseJSON(String json) /*-{
                                                                    return eval('('+json+')');
                                                                    }-*/;

    protected final native boolean has(String key) /*-{
                                                   return this[key] != undefined;
                                                   }-*/;

    protected final native JsArrayString keys() /*-{
                                                      var a = new Array();
                                                      for (var p in this) { a.push(p); }
                                                      return a;
                                                      }-*/;

    /**
     * @param key
     * @return String value or an empty string if key doesn't exist
     */
    protected final native String getString(String key) /*-{
                                                        return this[key] ? "" + this[key] : "";
                                                        }-*/;

    protected final native void set(String key, String value) /*-{
                                                              this[key] = value;
                                                              }-*/;

    protected final int getInt(String key) {
        String s = getString(key);

        if (s.isEmpty()) {
            return 0;
        }

        return Integer.parseInt(s);
    }

    protected final long getLong(String key) {
        String s = getString(key);

        if (s.isEmpty()) {
            return 0;
        }

        return Long.parseLong(s);
    }

    protected final boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    protected final native JsModel getObject(String key) /*-{
                                                         return this[key];
                                                         }-*/;

    protected final native JsArray<? extends JsModel> getArray(String key) /*-{
                                                                           return this[key];
                                                                           }-*/;

    protected final native JsArrayString getStrings(String key) /*-{
                                                                return this[key];
                                                                }-*/;

    
    protected final Boolean getBooleanObject(String key) {
        if (has(key)) {
            return getBoolean(key);
        } else {
            return null;
        }
    }

    protected static String toString(JsArrayString array) {
        if (array == null || array.length() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < array.length(); i++) {
            sb.append(array.get(i));

            if (i < array.length() - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

}
