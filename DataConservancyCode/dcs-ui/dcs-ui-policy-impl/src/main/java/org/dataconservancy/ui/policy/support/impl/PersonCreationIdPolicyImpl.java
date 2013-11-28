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

import java.util.HashMap;
import java.util.Map;

/**
 * The business policy that is consulted when the {@link org.dataconservancy.ui.services.UserService}
 * creates Person object and validates the identifier on the Person.
 */
public class PersonCreationIdPolicyImpl implements IdPolicy {

    private BusinessIdState expectedState;
    private Map<BusinessIdState, IdPolicyAction> policyActionMap;

    @Override
    public IdPolicyAction getIdPolicyActionForState(BusinessIdState actualState) {
        return policyActionMap.get(actualState);
    }

    public BusinessIdState getExpectedState() {
        return expectedState;
    }

    public void setExpectedState(BusinessIdState expectedState) {
        if (expectedState == null) {
            throw new IllegalArgumentException("Expected BusinessIdState must not be null.");
        }
        this.expectedState = expectedState;
    }

    public Map<BusinessIdState, IdPolicyAction> getPolicyActionMap() {
        Map<BusinessIdState, IdPolicyAction> map = new HashMap<BusinessIdState, IdPolicyAction>();
        for (Map.Entry<BusinessIdState, IdPolicyAction> e : policyActionMap.entrySet()) {
            map.put(e.getKey(), e.getValue());
        }

        return map;
    }

    public void setPolicyActionMap(Map<BusinessIdState, IdPolicyAction> policyActionMap) {
        if (policyActionMap == null) {
            throw new IllegalArgumentException("Policy Action Map must not be null.");
        }
        Map<BusinessIdState, IdPolicyAction> map = new HashMap<BusinessIdState, IdPolicyAction>();
        for (Map.Entry<BusinessIdState, IdPolicyAction> e : policyActionMap.entrySet()) {
            map.put(e.getKey(), e.getValue());
        }

        this.policyActionMap = map;
    }
}
