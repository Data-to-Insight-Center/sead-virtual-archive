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
package org.dataconservancy.dcs.query.endpoint.utils.dcpsolr;

import javax.servlet.http.HttpServletRequest;

public enum ResultFormat {
    DCP("application/xml"), JSON("application/javascript"), JAVASCRIPT(
            "application/javascript");

    private final String mimetype;

    ResultFormat(String format) {
        this.mimetype = format;
    }

    public String mimeType() {
        return mimetype;
    }

    public static ResultFormat find(HttpServletRequest req) {
        String type = req.getHeader("Accept");
        
        if (type == null || type.isEmpty() || type.startsWith("*/")) {
            // Handle case of <script src> not being able to set headers
            String jsoncallback = req.getParameter("callback");

            if (jsoncallback != null) {
                return JAVASCRIPT;
            }
            
            return DCP;
        }

        // TODO correctly parse Accept header

        for (ResultFormat fmt : values()) {
            if (type.contains(fmt.mimetype)) {
                return fmt;
            }
        }

        return null;
    }
}
