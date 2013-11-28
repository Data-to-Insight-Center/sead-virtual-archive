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
 * Indicates that a user operation has violated a business policy.  This is the unchecked equivalent of
 * {@link BizPolicyException}.
 *
 * @see BizPolicyException
 */
public class RuntimeBizPolicyException extends RuntimeException {

    public RuntimeBizPolicyException() {

    }

    public RuntimeBizPolicyException(String message) {
        super(message);
    }

    public RuntimeBizPolicyException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuntimeBizPolicyException(Throwable cause) {
        super(cause);
    }
}
