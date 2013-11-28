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

/**
 * {@code IdPolicy} provides a mechanism for looking up {@link IdPolicyAction policy actions} based on the
 * {@link BusinessIdState state} of the business object's identifier.
 */
public interface IdPolicy {

    /**
     * Obtain the policy action to enforce based on the {@code BusinessIdState} of the business object.  If
     * no policy action exists for the {@code actualState}, then policy enforcers can decide whether to
     * apply a default policy, or raise an exception.
     * <p/>
     * Implementations are not required to have an answer for all possible values of {@code BusinessIdState},
     * therefore clients of this interface should be prepared to handle {@code null} responses.
     *
     *
     * @param actualState the {@code BusinessIdState} of the business object
     * @return the policy action to enforce, may be {@code null}
     */
    public IdPolicyAction getIdPolicyActionForState(BusinessIdState actualState);

}
