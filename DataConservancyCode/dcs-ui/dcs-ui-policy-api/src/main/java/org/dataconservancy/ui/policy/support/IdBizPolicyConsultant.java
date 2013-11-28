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

import org.dataconservancy.ui.model.Person;

/**
 * Enumerates policy decision points related to handling business object identifiers.  It is expected that various
 * business services will consult this interface prior to validating and persisting a business object.
 * <p>
 * <strong>Background:</strong>
 * <p>
 * Business services (e.g. the {@link org.dataconservancy.ui.services.UserService}) are called upon to {@link org.dataconservancy.ui.services.UserService#create(Person) create}
 * the objects that they manage.  These methods accept a already-composed business object.  Prior to persisting the
 * object in the system, the business services may (and should) apply policy and validate the object.
 * </p>
 * <p>
 * It is expected that business services will consult this interface when considering the presence or absence of a
 * business object identifier on a business object.  For example: what should {@code UserService} implementations do
 * when a {@code Person} object with no identifier is supplied as an argument for the {@code UserService#create(Person)}
 * method?  The answer is supplied by {@link #getBusinessObjectCreationIdPolicy()}.
 * </p>
 * </p>
 * @see IdPolicy
 */
public interface IdBizPolicyConsultant {

    /**
     * Obtain the {@link IdPolicy} when a business service is asked to create a business object.
     * <p/>
     * This is the identifier policy to be used when a business object is creating a new business object.  It
     * tells the business service what to do with the identifier field of the business object when creating
     * the object.

     * @return the policy
     */
    public IdPolicy getBusinessObjectCreationIdPolicy();

}
