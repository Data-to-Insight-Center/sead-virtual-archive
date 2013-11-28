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

import org.dataconservancy.ui.policy.support.IdBizPolicyConsultant;
import org.dataconservancy.ui.policy.support.IdPolicy;
import org.dataconservancy.ui.policy.support.IdPolicyAction;

/**
 * The default implementation of the {@link IdBizPolicyConsultant}.  Note that this class must be
 * {@link #setBusinessObjectCreationIdPolicy(IdPolicy) initialized} with a {@code IdPolicy} before being used.
 *
 * @see IdBizPolicyConsultant
 * @see IdPolicy
 * @see IdPolicyAction
 */
public class DefaultIdBizPolicyConsultant implements IdBizPolicyConsultant {

    private IdPolicy objectCreationIdPolicy;

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public IdPolicy getBusinessObjectCreationIdPolicy() {
        return objectCreationIdPolicy;
    }

    /**
     * Set the identifier policy to be used when business objects are created.
     *
     * @param policy the IdPolicy
     * @throws IllegalArgumentException if the {@code policy} is {@code null}
     */
    public void setBusinessObjectCreationIdPolicy(IdPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("IdPolicy must not be null!");
        }
        this.objectCreationIdPolicy = policy;
    }

}
