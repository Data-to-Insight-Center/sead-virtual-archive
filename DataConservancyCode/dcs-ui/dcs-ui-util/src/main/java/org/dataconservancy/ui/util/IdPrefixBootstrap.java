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

package org.dataconservancy.ui.util;

/**
 * {@code IdPrefixBootstrap} is used to pre-configure id prefix for {@link org.dataconservancy.dcs.id.api.IdService} with
 * local server information.
 */
public class IdPrefixBootstrap {
    
    public static String createIdPrefix(String scheme, String hostName, String port, String contextPath) {
        String idPrefix = scheme + "://" + hostName;
        
        if (!port.isEmpty()) {
            idPrefix += ":" + port;
        } 
        
        if (!contextPath.isEmpty()) {
            if (!contextPath.startsWith("/")) {
                idPrefix += "/";
            }
            idPrefix += contextPath;
        }

        if (!idPrefix.endsWith("/")) {
            idPrefix += "/";
        }

        return idPrefix;
    }
}