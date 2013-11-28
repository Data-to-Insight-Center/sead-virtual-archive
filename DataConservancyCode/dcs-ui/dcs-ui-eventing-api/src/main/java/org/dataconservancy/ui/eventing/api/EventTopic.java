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
package org.dataconservancy.ui.eventing.api;

/**
 * Possible topics an Event may have.  An Event can only have one topic.
 */
public enum EventTopic {

    /**
     * Events that carry information about an Exception that occurred in the system.
     */
    EXCEPTION,

    /**
     * Events that carry information about user registrations.
     */
    REGISTRATION,
    
    /**
     * Events that carry information about user approval.
     */
    USER_APPROVAL, 
    
    /**
     * Events that information about data item deposit.
     */
    DATA_ITEM_DEPOSIT;
}
