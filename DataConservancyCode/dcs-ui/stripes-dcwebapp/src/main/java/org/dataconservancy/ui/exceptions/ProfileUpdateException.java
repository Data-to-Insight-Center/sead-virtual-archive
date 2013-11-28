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
 * This exception is used to capture exceptional contition or error encountered when updating user profile.
 */
public class ProfileUpdateException extends BaseUiException {

    /**
     * The user ID of the profile being updated.
     */
    private String userId;

    public ProfileUpdateException() {
    }

    public ProfileUpdateException(Throwable cause) {
        super(cause);
    }

    public ProfileUpdateException(String message) {
        super(message);
    }

    public ProfileUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * The user ID of the profile being updated.
     *
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * The user ID of the profile being updated.
     *
     * @param userId the user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
