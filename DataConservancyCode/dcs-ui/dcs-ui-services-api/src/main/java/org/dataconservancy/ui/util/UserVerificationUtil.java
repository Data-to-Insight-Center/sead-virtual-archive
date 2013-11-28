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

package org.dataconservancy.ui.util;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.UserService;

/**
 * This is a short utility class to help the biz services determine if a user
 * is legitimate.
 */
public class UserVerificationUtil {

    /**
     * Determine if the provided user is equal to the user defined in the
     * provided userService.  This returns a non-null user if and only if the
     * user is present in the userService and is equal to the user with the
     * same id.  It returns null if the user is null, the userService is null.
     * @param userService the UserService to check for the user.
     * @param user the Person to verify
     * @return a valid person iff the user is in the user service and is equal
     *         the returned user.
     */
    public static Person VerifyUser(UserService userService, Person user) {
        //Ensure that the user and the userService are non-null
        if (user == null || userService == null) {
            return null;
        }

        //Pull the user from the userService
        Person USuser = userService.get(user.getId());

        //Ensure that the returned user is not null and is equal to the
        // original user.  If so, return the user retrieved from the
        // userService.
        if (USuser != null && user.equals(USuser) && ((user.getRoles() == null && USuser.getRoles() == null)
                || user.getRoles().equals(USuser.getRoles()))) {
            return USuser;
        }

        //In all other cases, return null
        return null;
    }
}
