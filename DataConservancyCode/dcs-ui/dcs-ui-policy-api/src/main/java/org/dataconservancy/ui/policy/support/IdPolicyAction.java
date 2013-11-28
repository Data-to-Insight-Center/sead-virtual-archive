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
 * These are the supported actions that can be taken by enforcers if identifier policy.
 * Here is an example matrix that a policy enforcement mechanism may use:
 * <table>
 *     <tr>
 *         <th colspan="3">Actual State</th>
 *     </tr>
 *     <tr>
 *         <th>Expected State</th><th>Exists</th><th>Does not Exist</th>
 *     </tr>
 *     <tr>
 *         <th>Exists</th><td>{@link #ACCEPT}: Accept the existing identifier</td>
 *                        <td>{@link #REJECT}: Reject the object (the caller is supposed to supply the identifier)</td>
 *     </tr>
 *     <tr>
 *         <th>Does not exist</th>
 *                        <td>{@link #SUBSTITUTE}: Copy the existing identifier to a alternate id field and mint a new primary identifier</td>
 *                        <td>{@link #REPLACE}: Replace the blank identifier with a newly minted identifier</td>
 *     </tr>
 * </table>
 */
public enum IdPolicyAction {

    /**
     * Accept the current identifier state.  This means that the value of the identifier is accepted as is,
     * and not munged.
     */
    ACCEPT,

    /**
     * Reject the current identifier state.  Normally this means raising an exception.
     */
    REJECT,

    /**
     * Overwrite the current identifier state.  This might mean minting a new ID and replacing the existing
     * value of an identifier.
     */
    REPLACE,

    /**
     * Substitute the current identifier state.  This might mean copying the existing identifier value to
     * an alternate id field, and replacing it with a newly minted ID.
     */
    SUBSTITUTE

}
