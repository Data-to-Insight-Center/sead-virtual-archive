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
package org.dataconservancy.deposit.sword;

import java.net.URI;
import java.net.URISyntaxException;

public class SWORDPackaging {

    private final String packaging;

    private final String preference;

    public SWORDPackaging(String pkg, String pref) {
        try {
            packaging = new URI(pkg).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                "Packaging must be a URI: '" + pkg + "'");
        }
        preference = pref;
    }

    public SWORDPackaging(String pkg) {
        try {
            packaging = new URI(pkg).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                "Packaging must be a URI: '" + pkg + "'");
        }
        preference = null;
    }

    public boolean hasPreference() {
        return preference != null;
    }

    public String getPackaging() {
        return packaging;
    }

    public String getPreference() {
        return preference;
    }
}
