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
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dataconservancy.ui.model.RegistrationStatus.APPROVED;
import static org.dataconservancy.ui.model.RegistrationStatus.BLACK_LISTED;
import static org.dataconservancy.ui.model.RegistrationStatus.PENDING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the DefaultPersonBizPolicyConsultant.
 */
public class DefaultPersonBizPolicyConsultantTest {

    /**
     * Verifies that all manner of valid lists of registration statues are accepted by the policy consultant.
     *
     * @throws Exception
     */
    @Test
    public void testAllowedRegistrationStatusOnCreate() throws Exception {
        final DefaultPersonBizPolicyConsultant underTest = new DefaultPersonBizPolicyConsultant();

        final List<RegistrationStatus> emptyList = Collections.emptyList();
        final List<RegistrationStatus> pending = Arrays.asList(PENDING);
        final List<RegistrationStatus> approved = Arrays.asList(APPROVED);
        final List<RegistrationStatus> blacklisted = Arrays.asList(BLACK_LISTED);
        final List<RegistrationStatus> all = Arrays.asList(PENDING, APPROVED, BLACK_LISTED);
        final List<RegistrationStatus> withNull = Arrays.asList(null, PENDING, APPROVED, BLACK_LISTED);

        underTest.setAllowedRegistrationStatusOnCreate(emptyList);
        assertEquals(emptyList, underTest.allowedRegistrationStatusOnCreate());

        underTest.setAllowedRegistrationStatusOnCreate(pending);
        assertEquals(pending, underTest.allowedRegistrationStatusOnCreate());

        underTest.setAllowedRegistrationStatusOnCreate(approved);
        assertEquals(approved, underTest.allowedRegistrationStatusOnCreate());

        underTest.setAllowedRegistrationStatusOnCreate(blacklisted);
        assertEquals(blacklisted, underTest.allowedRegistrationStatusOnCreate());

        underTest.setAllowedRegistrationStatusOnCreate(all);
        assertEquals(all, underTest.allowedRegistrationStatusOnCreate());

        underTest.setAllowedRegistrationStatusOnCreate(withNull);
        assertEquals(withNull, underTest.allowedRegistrationStatusOnCreate());
    }

    /**
     * Verifies that you can't pass in a null list to allowed registration status
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAllowedRegistrationsWithNullReference() throws Exception {
        new DefaultPersonBizPolicyConsultant().setAllowedRegistrationStatusOnCreate(null);
    }

    /**
     * Simple test insuring that the enforceRegistrationStatus flag is set.
     * @throws Exception
     */
    @Test
    public void testEnforceRegistrationStatusOnCreate() throws Exception {
        DefaultPersonBizPolicyConsultant underTest = new DefaultPersonBizPolicyConsultant();
        underTest.setEnforceRegistrationStatusOnCreate(false);
        assertEquals(false, underTest.enforceRegistrationStatusOnCreate());
        underTest.setEnforceRegistrationStatusOnCreate(true);
        assertEquals(true, underTest.enforceRegistrationStatusOnCreate());
    }

    /**
     * Simple test insuring that all valid default RegistrationStatus values are allowed.
     *
     * @throws Exception
     */
    @Test
    public void testGetDefaultRegistrationStatus() throws Exception {
        DefaultPersonBizPolicyConsultant underTest = new DefaultPersonBizPolicyConsultant();

        underTest.setDefaultRegistrationStatus(null);
        assertEquals(null, underTest.getDefaultRegistrationStatus());

        underTest.setDefaultRegistrationStatus(APPROVED);
        assertEquals(APPROVED, underTest.getDefaultRegistrationStatus());

        underTest.setDefaultRegistrationStatus(BLACK_LISTED);
        assertEquals(BLACK_LISTED, underTest.getDefaultRegistrationStatus());

        underTest.setDefaultRegistrationStatus(PENDING);
        assertEquals(PENDING, underTest.getDefaultRegistrationStatus());
    }

    @Test
    public void testGetRolesForRegistrationStatus() throws Exception {
        DefaultPersonBizPolicyConsultant underTest = new DefaultPersonBizPolicyConsultant();
        Map<RegistrationStatus, List<Role>> statusRoleMap = new HashMap<RegistrationStatus, List<Role>>();

        // An uninitialized / empty map.
        underTest.setRolesForRegistrationStatus(statusRoleMap);

        assertTrue(underTest.getRolesForRegistrationStatus(PENDING).isEmpty());
        assertTrue(underTest.getRolesForRegistrationStatus(APPROVED).isEmpty());
        assertTrue(underTest.getRolesForRegistrationStatus(BLACK_LISTED).isEmpty());
        assertTrue(underTest.getRolesForRegistrationStatus(null).isEmpty());

        List<Role> expectedRoles = Arrays.asList(Role.ROLE_ADMIN);

        // Initialize the map with the PENDING status
        statusRoleMap.put(PENDING, expectedRoles);
        underTest.setRolesForRegistrationStatus(statusRoleMap);
        assertEquals(expectedRoles, underTest.getRolesForRegistrationStatus(PENDING));

        // Initialize the map with the APPROVED status
        statusRoleMap.clear();
        statusRoleMap.put(APPROVED, expectedRoles);
        underTest.setRolesForRegistrationStatus(statusRoleMap);
        assertEquals(expectedRoles, underTest.getRolesForRegistrationStatus(APPROVED));

        // Initialize the map with the BLACK_LISTED status
        statusRoleMap.clear();
        statusRoleMap.put(BLACK_LISTED, expectedRoles);
        underTest.setRolesForRegistrationStatus(statusRoleMap);
        assertEquals(expectedRoles, underTest.getRolesForRegistrationStatus(BLACK_LISTED));

        // Initialize the map with the null status
        statusRoleMap.clear();
        statusRoleMap.put(null, expectedRoles);
        underTest.setRolesForRegistrationStatus(statusRoleMap);
        assertEquals(expectedRoles, underTest.getRolesForRegistrationStatus(null));

        // Make sure that a list of roles works
        expectedRoles = Arrays.asList(Role.ROLE_USER, Role.ROLE_ADMIN);
        statusRoleMap.clear();
        statusRoleMap.put(APPROVED, expectedRoles);
        underTest.setRolesForRegistrationStatus(statusRoleMap);
        assertEquals(expectedRoles, underTest.getRolesForRegistrationStatus(APPROVED));
    }

    /**
     * Insures that the getters and setters for the registration roles map/list are defensively copying references.
     *
     * @throws Exception
     */
    @Test
    public void testDefensiveRolesForRegistrationStatus() throws Exception {
        DefaultPersonBizPolicyConsultant underTest = new DefaultPersonBizPolicyConsultant();
        Map<RegistrationStatus, List<Role>> statusRoleMap = new HashMap<RegistrationStatus, List<Role>>();

        underTest.setRolesForRegistrationStatus(statusRoleMap);
        assertEquals(Collections.<Role>emptyList(), underTest.getRolesForRegistrationStatus(PENDING));

        // Mutate the statusRoleMap
        statusRoleMap.put(PENDING, Arrays.asList(Role.ROLE_USER));

        // Verify that mutating the statusRoleMap doesn't mutate the state of the status role map held by the consultant
        assertEquals(Collections.<Role>emptyList(), underTest.getRolesForRegistrationStatus(PENDING));

        // Now, actually set the statusRoleMap
        underTest.setRolesForRegistrationStatus(statusRoleMap);
        assertEquals(Arrays.asList(Role.ROLE_USER), underTest.getRolesForRegistrationStatus(PENDING));

        // Verify that mutating the returned list doesn't mutate the state of the status role map held by the consultant
        underTest.getRolesForRegistrationStatus(PENDING).add(Role.ROLE_ADMIN);
        assertEquals(Arrays.asList(Role.ROLE_USER), underTest.getRolesForRegistrationStatus(PENDING));
    }

    /**
     * Verifies the initial state of the policy consultant per the javadoc.
     *
     * @throws Exception
     */
    @Test
    public void testInitalState() throws Exception {
        DefaultPersonBizPolicyConsultant underTest = new DefaultPersonBizPolicyConsultant();
        assertEquals(PENDING, underTest.getDefaultRegistrationStatus());
        assertEquals(Arrays.asList(PENDING), underTest.allowedRegistrationStatusOnCreate());
        assertTrue(underTest.enforceRegistrationStatusOnCreate());
        assertEquals(Arrays.asList(Role.ROLE_USER), underTest.getRolesForRegistrationStatus(APPROVED));
        assertEquals(Collections.<Role>emptyList(), underTest.getRolesForRegistrationStatus(PENDING));
        assertEquals(Collections.<Role>emptyList(), underTest.getRolesForRegistrationStatus(BLACK_LISTED));
    }
}
