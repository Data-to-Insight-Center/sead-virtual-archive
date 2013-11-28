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
package org.dataconservancy.ui.policy.support.impl;

import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The default implementation of the {@code PersonBizPolicyConsultant}.
 */
public class DefaultPersonBizPolicyConsultant implements PersonBizPolicyConsultant {

    private boolean enforceRegistrationStatusOnCreate = true;

    private List<RegistrationStatus> allowedRegistrationStatusOnCreate = Arrays.asList(RegistrationStatus.PENDING);

    private RegistrationStatus defaultRegistrationStatus = RegistrationStatus.PENDING;

    private Map<RegistrationStatus, List<Role>> rolesForRegistrationStatus;

    /**
     * Creates a consultant with the following default behavior:
     * <ul>
     *     <li>Registration status is enforced</li>
     *     <li>The default registration status is {@link RegistrationStatus#PENDING}</li>
     *     <li>{@link RegistrationStatus#APPROVED} users are mapped to the {@link Role#ROLE_USER}</li>
     *     <li>No other registration statues are mapped to roles.</li>
     * </ul>
     */
    public DefaultPersonBizPolicyConsultant() {
        rolesForRegistrationStatus = new HashMap<RegistrationStatus, List<Role>>();
        rolesForRegistrationStatus.put(RegistrationStatus.PENDING, Collections.<Role>emptyList());
        rolesForRegistrationStatus.put(RegistrationStatus.APPROVED, Arrays.asList(Role.ROLE_USER));
        rolesForRegistrationStatus.put(RegistrationStatus.BLACK_LISTED, Collections.<Role>emptyList());
    }

    @Override
    public List<RegistrationStatus> allowedRegistrationStatusOnCreate() {
        // defensively copy the list
        List<RegistrationStatus> statuses = new ArrayList<RegistrationStatus>();
        for (RegistrationStatus status : allowedRegistrationStatusOnCreate) {
            statuses.add(status);
        }

        return statuses;
    }

    @Override
    public boolean enforceRegistrationStatusOnCreate() {
        return enforceRegistrationStatusOnCreate;
    }

    @Override
    public RegistrationStatus getDefaultRegistrationStatus() {
        return defaultRegistrationStatus;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If {@link #setRolesForRegistrationStatus(java.util.Map) role map} doesn't contain {@code status}, an empty list
     * is returned.
     *
     * @param status the RegistrationStatus, or {@code null}
     * @return {@inheritDoc}
     */
    @Override
    public List<Role> getRolesForRegistrationStatus(RegistrationStatus status) {
        if (!rolesForRegistrationStatus.containsKey(status)) {
            return Collections.emptyList();
        }

        List<Role> roles = new ArrayList<Role>();

        if (rolesForRegistrationStatus.containsKey(status)) {
            // defensively copy the roles from the List
            for (Role r : rolesForRegistrationStatus.get(status)) {
                roles.add(r);
            }
        }

        return roles;
    }

    public void setAllowedRegistrationStatusOnCreate(List<RegistrationStatus> allowedRegistrationStatusOnCreate) {
        if (allowedRegistrationStatusOnCreate == null) {
            throw new IllegalArgumentException("List of allowed registration statuses must not be null.");
        }
        this.allowedRegistrationStatusOnCreate = allowedRegistrationStatusOnCreate;
    }

    public void setDefaultRegistrationStatus(RegistrationStatus defaultRegistrationStatus) {
        this.defaultRegistrationStatus = defaultRegistrationStatus;
    }

    public void setEnforceRegistrationStatusOnCreate(boolean enforceRegistrationStatusOnCreate) {
        this.enforceRegistrationStatusOnCreate = enforceRegistrationStatusOnCreate;
    }

    public void setRolesForRegistrationStatus(Map<RegistrationStatus, List<Role>> rolesForRegistrationStatus) {
        // Defensively copy the supplied Map and Lists.
        this.rolesForRegistrationStatus = new HashMap<RegistrationStatus, List<Role>>();
        for (Map.Entry<RegistrationStatus, List<Role>> entry : rolesForRegistrationStatus.entrySet()) {
            final RegistrationStatus status = entry.getKey();
            List<Role> roles = null;
            if (this.rolesForRegistrationStatus.containsKey(status)) {
                roles = this.rolesForRegistrationStatus.get(status);
            } else {
                roles = new ArrayList<Role>();
                this.rolesForRegistrationStatus.put(status, roles);
            }

            for (Role r : entry.getValue()) {
                roles.add(r);
            }
        }
    }
}
