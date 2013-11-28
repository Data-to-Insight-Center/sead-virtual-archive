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
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;

import java.util.List;

/**
 * Enumerates the policy decision points for {@link org.dataconservancy.ui.services.UserService} implementations, and provides implementers a means
 * to customize {@code UserService} policy.  Implementations of {@code UserService} are meant to consult this interface
 * when enforcing policy.
 */
public interface PersonBizPolicyConsultant {

    /**
     * If {@code true}, {@link #allowedRegistrationStatusOnCreate()} must be consulted when creating {@code Person}s.
     *
     * @return true if {@code allowedRegistrationStatusOnCreate()} should be consulted on {@code Person} creation
     */
    public boolean enforceRegistrationStatusOnCreate();

    /**
     * Returns the allowed registration statuses that created {@code Person}s can have.  If the {@code Person}
     * does not have a registration status that is in this list, then {@code UserService} implementations should
     * refuse to create the the {@code Person}, or update the {@code Person}s {@code RegistrationStatus} to one of the
     * allowed statuses.
     * <p/>
     * An empty {@code List} should be interpreted as any {@code RegistrationStatus} is allowed.  If
     * a {@code null} {@code RegistrationStatus} is allowed, then {@code null} should be a list member.
     *
     * @return a {@code List} of {@code RegistrationStatus}es that newly created {@code Person}s are allowed to have,
     * must not be {@code null}
     */
    public List<RegistrationStatus> allowedRegistrationStatusOnCreate();

    /**
     * Returns the default registration status that newly created {@code Person}s should have.  {@code UserService}
     * implementations can decide how to use this information.  For example, if a {@code Person} is being created, but
     * their {@link Person#getRegistrationStatus() Registration Status} is {@code null}, then the status returned from
     * this method could be used to set a default status.
     *
     * @return the default registration status for newly created {@code Person}s, may be {@code null}
     */
    public RegistrationStatus getDefaultRegistrationStatus();

    /**
     * Returns the roles that a {@code Person} should have, based on a {@code RegistrationStatus}.  {@code UserService}
     * implementations are expected to set the {@code Person}s roles based on their {@code RegistrationStatus}.  The
     * {@code null} reference is accepted by this method.
     * <p/>
     * An empty {@code List} should be interpreted as no {@code Role}s map to the {@code RegistrationStatus}.
     *
     * @param status the RegistrationStatus, or {@code null}
     * @return a List of {@code Roles} associated with the {@code RegistrationStatus}
     */
    public List<Role> getRolesForRegistrationStatus(RegistrationStatus status);
    
}
