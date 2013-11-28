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
 * Indicates that a user operation has violated a business policy.
 */
public class BizPolicyException extends Exception {
    public static enum Type {
        AUTHENTICATION_ERROR,
        AUTHORIZATION_ERROR,
        VALIDATION_ERROR
    };
    
    private Type exceptionType; 
    
    public BizPolicyException() {
    }

    public BizPolicyException(String message, Type type) {
        super(message);
        this.exceptionType = type;
    }

    public BizPolicyException(Throwable cause, Type type) {
        super(cause);
        this.exceptionType = type;
    }

    public BizPolicyException(String message, Throwable cause, Type type) {
        super(message, cause);
        this.exceptionType = type;
    }
    
    public Type getType() {
        return exceptionType;
    }
}
