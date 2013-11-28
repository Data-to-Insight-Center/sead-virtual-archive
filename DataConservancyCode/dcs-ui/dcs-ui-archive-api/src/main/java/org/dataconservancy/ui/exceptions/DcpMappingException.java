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
package org.dataconservancy.ui.exceptions;

/**
 * This class captures the exceptional condition and/or error encountered in mapping dcs-ui objects
 * ({@link org.dataconservancy.ui.model.Collection}, {@link org.dataconservancy.ui.model.DataItem} to and from
 * {@link org.dataconservancy.model.dcp.Dcp} objects.
 */
public class DcpMappingException extends Exception {
    private static final long serialVersionUID = 1L;
    
     public DcpMappingException() {
        super();
    }

    public DcpMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DcpMappingException(String message) {
        super(message);
    }

    public DcpMappingException(Throwable cause) {
        super(cause);
    }
}
