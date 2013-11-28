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
package org.dataconservancy.deposit;

import java.io.InputStream;

import java.util.Map;

/**
 * Primary deposit interface.
 * <p>
 * Represents a service that is capable of managing deposited content and
 * representations of their status within deposit workflows.
 * </p>
 */
public interface DepositManager {

    /**
     * Deposit some unit of content.
     * 
     * @param content
     *        Content stream of the item being deposited.
     * @param contentType
     *        MIME type which describes deposited content. Null means 'unknown'
     *        or 'not applicable'
     * @param packaging
     *        Specific packaging format of the item being deposited. Null means
     *        'unknown' or 'not applicable.' Packaging semantics may vary, and
     *        should be defined and documented by implementing classes.
     * @param metadata
     *        Map containing key/value pairs, generally following HTTP/1.1
     *        conventions, to provide additional information to optimize the
     *        deposit process. May be null or empty.
     * @return Initial status of deposited content.
     * @throws PackageException
     *         if the deposit package is not well formed or not supported.
     */
    public DepositInfo deposit(InputStream content,
                               String contentType,
                               String packaging,
                               Map<String, String> metadata)
            throws PackageException;

    /**
     * Retrieve any information related to a particular deposit.
     * 
     * @param id
     *        Deposit identifier, as per {@link DepositInfo#getDepositID()}.
     * @return Deposit information at the time of invocation.
     */
    public DepositInfo getDepositInfo(String id);

    /**
     * Retrieve an identifier for this specific deposit manager.
     * <p>
     * This identifier should ideally be locally unique. It may be used to
     * differentiate and address deposit managers unambiguously by id in a
     * consistent way.
     * </p>
     * 
     * @return Opaque string containing manager ID. Should not be null.
     */
    public String getManagerID();
}
