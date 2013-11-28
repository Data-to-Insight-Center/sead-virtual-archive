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
package org.dataconservancy.transform.profile;

import org.dataconservancy.model.dcp.Dcp;

/**
 * Detects the presence of a particular profile within a Dcp package.
 * <p>
 * Implementations of {@linkplain DcpProfileDetector} will look for
 * evidence of a particular structure within the entities, relationships,
 * properties, formats, etc in a Dcp.
 * </p>
 */
public interface DcpProfileDetector {

    /**
     * Returns true if the Dcp contains a particular structural format.
     * 
     * @param dcp
     *        Dcp containing various entities.
     * @return true if the structural format is detected.
     */
    public boolean hasFormat(Dcp dcp);

}
