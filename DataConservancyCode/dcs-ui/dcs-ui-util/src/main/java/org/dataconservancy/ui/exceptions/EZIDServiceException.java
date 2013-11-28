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
 * This exception is thrown by {@link org.dataconservancy.ui.services.EZIDService} when errors occur in creating Citable
 * Locator of a {@link org.dataconservancy.ui.model.Collection}.
 */
public class EZIDServiceException extends Exception {

    public EZIDServiceException() {
    }

    public EZIDServiceException(String message) {
        super(message);
    }

    public EZIDServiceException(Throwable cause) {
        super(cause);
    }

    public EZIDServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}