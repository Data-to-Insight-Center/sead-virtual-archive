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
package org.dataconservancy.ui.model;

/**
 * {@code RegistrationStatus} represents users' registration in the system. Newly registered user, before being approved
 * by system administrator will has {@code PENDING} status. After being approved by system administrator, user has {@code APPROVED} status.
 * {@code BLACK_LISTED} status is reserved for users who are not allowed to use the system or re-register on the system.
 */
public enum RegistrationStatus {
    APPROVED("Approved"), PENDING("Pending"), BLACK_LISTED("Black-listed");

    private String stringValue;

    RegistrationStatus(String str) {
        stringValue = str;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
