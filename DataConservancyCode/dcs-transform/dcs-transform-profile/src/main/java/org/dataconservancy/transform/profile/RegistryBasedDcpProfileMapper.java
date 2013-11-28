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
import org.dataconservancy.registry.api.Registry;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;
import org.springframework.beans.factory.annotation.Required;

/**
 * Executes mappings based upon {@link Registry} lookups.
 * <p>
 * Given a key-value pair with a profile URI as a key and a Dcp as a value, look
 * up the appropriate {@link Mapping} impl based upon the profile URI, and
 * execute its mapping.
 * </p>
 * <p>
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setRegistry(Registry)}</dt>
 * <dd><b>Required</b>. This is the registry which maps dcp profile URIs to
 * Mapping impls</dd>
 * </dl>
 * </p>
 */
public class RegistryBasedDcpProfileMapper<Ko, Vo>
        implements Mapping<String, Dcp, Ko, Vo>  {

    private Registry<Mapping<String, Dcp, Ko, Vo>> registry;

    @Required
    public void setRegistry(Registry<Mapping<String, Dcp, Ko, Vo>> reg) {
        this.registry = reg;
    }

    /**
     * Lookup and execute the matching {@link Mapping} impl.
     * 
     * @param key
     *        String containing a Dcp profile URI.
     * @param dcp
     *        Dcp to be transformed.
     * @param writer
     *        Output of the mapping will be written to this.
     */
    public void map(String key, Dcp dcp, Output<Ko, Vo> writer) {
        if (registry.containsKey(key)) {
            registry.get(key).map(key, dcp, writer);
        }
    }
}
