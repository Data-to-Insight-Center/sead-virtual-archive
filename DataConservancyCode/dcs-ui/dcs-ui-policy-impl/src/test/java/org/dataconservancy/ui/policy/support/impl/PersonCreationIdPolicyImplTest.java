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

import org.dataconservancy.ui.policy.support.BusinessIdState;
import org.dataconservancy.ui.policy.support.IdPolicyAction;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.dataconservancy.ui.policy.support.BusinessIdState.ANY;
import static org.dataconservancy.ui.policy.support.BusinessIdState.DOES_NOT_EXIST;
import static org.dataconservancy.ui.policy.support.BusinessIdState.EXISTS;
import static org.dataconservancy.ui.policy.support.IdPolicyAction.ACCEPT;
import static org.dataconservancy.ui.policy.support.IdPolicyAction.REJECT;
import static org.dataconservancy.ui.policy.support.IdPolicyAction.REPLACE;
import static org.dataconservancy.ui.policy.support.IdPolicyAction.SUBSTITUTE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests around the behavior of the PersonCreationIdPolicy class.
 */
public class PersonCreationIdPolicyImplTest {

    /**
     * Verifies that all permissible values of IdPolicy can be obtained by
     * looking up the business ID state.
     *
     * @throws Exception
     */
    @Test
    public void testGetIdPolicyActionForState() throws Exception {
        PersonCreationIdPolicyImpl underTest = new PersonCreationIdPolicyImpl();

        Map<BusinessIdState, IdPolicyAction> policyActionMap = new HashMap<BusinessIdState, IdPolicyAction>();
        policyActionMap.put(EXISTS, ACCEPT);
        policyActionMap.put(DOES_NOT_EXIST, REJECT);
        policyActionMap.put(ANY, SUBSTITUTE);
        policyActionMap.put(null, REPLACE);

        underTest.setPolicyActionMap(policyActionMap);
        
        assertEquals(REPLACE, underTest.getIdPolicyActionForState(null));
        assertEquals(SUBSTITUTE, underTest.getIdPolicyActionForState(ANY));
        assertEquals(REJECT, underTest.getIdPolicyActionForState(DOES_NOT_EXIST));
        assertEquals(ACCEPT, underTest.getIdPolicyActionForState(EXISTS));

        // Clear the map
        policyActionMap.clear();
        underTest.setPolicyActionMap(policyActionMap);

        // note that null can be returned
        assertNull(underTest.getIdPolicyActionForState(EXISTS));
    }

    /**
     * Simple test verifying the expected state can be set and retrieved
     * 
     * @throws Exception
     */
    @Test
    public void testExpectedState() throws Exception {
        PersonCreationIdPolicyImpl underTest = new PersonCreationIdPolicyImpl();
        underTest.setExpectedState(EXISTS);
        assertEquals(EXISTS, underTest.getExpectedState());
        underTest.setExpectedState(DOES_NOT_EXIST);
        assertEquals(DOES_NOT_EXIST, underTest.getExpectedState());
    }

    /**
     * Test verifying the policy action map can be retrieved, and that it is defensively copied upon retrieval
     * 
     * @throws Exception
     */
    @Test
    public void testGetPolicyActionMap() throws Exception {
        PersonCreationIdPolicyImpl underTest = new PersonCreationIdPolicyImpl();

        Map<BusinessIdState, IdPolicyAction> policyActionMap = new HashMap<BusinessIdState, IdPolicyAction>();
        policyActionMap.put(EXISTS, ACCEPT);
        policyActionMap.put(DOES_NOT_EXIST, REJECT);
        policyActionMap.put(ANY, SUBSTITUTE);
        policyActionMap.put(null, REPLACE);

        // Assert the map can be set and retrieved
        underTest.setPolicyActionMap(policyActionMap);
        assertEquals(policyActionMap, underTest.getPolicyActionMap());

        // Assert that the map is defensively copied when retrieved
        underTest.getPolicyActionMap().clear();
        assertTrue(policyActionMap.equals(underTest.getPolicyActionMap()));
    }

    /**
     * Test verifying the policy action map can be set, and that it is defensively copied upon setting
     *
     * @throws Exception
     */
    @Test
    public void testSetPolicyActionMap() throws Exception {
        PersonCreationIdPolicyImpl underTest = new PersonCreationIdPolicyImpl();

        Map<BusinessIdState, IdPolicyAction> policyActionMap = new HashMap<BusinessIdState, IdPolicyAction>();
        policyActionMap.put(EXISTS, ACCEPT);
        policyActionMap.put(DOES_NOT_EXIST, REJECT);
        policyActionMap.put(ANY, SUBSTITUTE);
        policyActionMap.put(null, REPLACE);

        // Assert the map can be set and retrieved
        underTest.setPolicyActionMap(policyActionMap);
        assertEquals(policyActionMap, underTest.getPolicyActionMap());

        // Assert that the map is defensively copied when set
        policyActionMap.clear();
        assertFalse(policyActionMap.equals(underTest.getPolicyActionMap()));
    }

    /**
     * Verifies that null expected business id states aren't allowed
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNullExpectedState() throws Exception {
        new PersonCreationIdPolicyImpl().setExpectedState(null);
    }

    /**
     * Verifies that null map instances aren't allowed
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNullPolicyActionMap() throws Exception {
        new PersonCreationIdPolicyImpl().setPolicyActionMap(null);
    }
}
