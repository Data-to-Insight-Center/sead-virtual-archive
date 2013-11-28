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
package org.dataconservancy.ui.dcpmap;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.ui.exceptions.DcpMappingException;

/**
 * Abstraction to perform transformations between dcs-ui data models ({@link org.dataconservancy.ui.model.Collection},
 * {@link org.dataconservancy.ui.model.DataItem}) and their corresponding {@link org.dataconservancy.model.dcp.Dcp}s.
 * @param <T>
 */
public interface DcpMapper<T> {
    /**
     * Map an object to a Dcp package. The DcsFile entities in the package will
     * have their sources set to urls to deposit. Those files need to be uploaded
     * and the source attribute replaced with the returned identifier.
     * 
     * @param parent_entity_id parent of toplevel entities created in the package, null for no parent
     * @param object
     * @return package
     * @throws DcpMappingException
     */
    Dcp toDcp(String parent_entity_id, T object) throws DcpMappingException;

    /**
     * Map a Dcp package to an object.
     * 
     * @param dcp
     * @return object
     */
    T fromDcp(Dcp dcp) throws DcpMappingException;
}
