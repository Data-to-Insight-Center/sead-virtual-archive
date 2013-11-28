/*
 * Copyright 2013 Johns Hopkins University
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
 * This exception is thrown by PasswordResetService to indicate an exceptional condition or error encountered when managing
 * {@link org.dataconservancy.ui.model.PasswordResetRequest} objects.
 */
public class PasswordResetServiceException extends Exception {
    public PasswordResetServiceException() {
        super();
    }

    public PasswordResetServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasswordResetServiceException(String message) {
        super(message);
    }

    public PasswordResetServiceException(Throwable cause) {
        super(cause);
    }
}
