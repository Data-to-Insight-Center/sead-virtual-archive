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
package org.dataconservancy.ui.policy.support;

/**
 * Enumerates the possible states of business identifiers on a business object, including a state that matches
 * any other state.
 */
public enum BusinessIdState {

    /**
     * The business object has an existing identifier
     */
    EXISTS,

    /**
     * The business object does not have an existing identifier
     */
    DOES_NOT_EXIST,

    /**
     * The business object's identifier can be in any state (either {@code EXISTS} or {@code DOES_NOT_EXISTS})
     */
    ANY

}
