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
import org.dataconservancy.ui.policy.support.IdPolicy;
import org.dataconservancy.ui.policy.support.IdPolicyAction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Basic unit tests for the DefaultIdBizPolicyConsultant.
 */
public class DefaultIdBizPolicyConsultantTest {


    /**
     * Verifies that the initial staate of the creation id policy is {@code null}
     *
     * @throws Exception
     */
    @Test
    public void testGetBusinessObjectCreationIdPolicy() throws Exception {
        DefaultIdBizPolicyConsultant underTest = new DefaultIdBizPolicyConsultant();
        assertEquals(null, underTest.getBusinessObjectCreationIdPolicy());
    }

    /**
     * Verifies that you can't set a null id creation policy.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetBusinessObjectCreationIdPolicyWithNullPolicy() throws Exception {
        DefaultIdBizPolicyConsultant underTest = new DefaultIdBizPolicyConsultant();
        underTest.setBusinessObjectCreationIdPolicy(null);
    }

    /**
     * Verifies that what was set is what you get
     *
     * @throws Exception
     */
    @Test
    public void testSetAndGet() throws Exception {
        DefaultIdBizPolicyConsultant underTest = new DefaultIdBizPolicyConsultant();
        final IdPolicy idp = new IdPolicy() {
            @Override
            public IdPolicyAction getIdPolicyActionForState(BusinessIdState actualState) {
                // Default method body
                return null;
            }
        };

        underTest.setBusinessObjectCreationIdPolicy(idp);
        assertTrue(idp == underTest.getBusinessObjectCreationIdPolicy());
    }
}
