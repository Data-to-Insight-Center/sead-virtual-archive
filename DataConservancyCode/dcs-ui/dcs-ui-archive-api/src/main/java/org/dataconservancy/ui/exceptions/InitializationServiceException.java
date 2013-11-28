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
 * Wraps exceptions related to system initialization.
 */
public class InitializationServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    public InitializationServiceException() {
        super();
    }

    public InitializationServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitializationServiceException(String message) {
        super(message);
    }

    public InitializationServiceException(Throwable cause) {
        super(cause);
    }

}
