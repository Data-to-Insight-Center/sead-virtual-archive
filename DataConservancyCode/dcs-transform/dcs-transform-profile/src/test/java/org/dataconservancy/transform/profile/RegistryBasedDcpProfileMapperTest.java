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

import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.registry.api.support.RegistryEntry;
import org.dataconservancy.registry.shared.memory.InMemoryRegistry;
import org.junit.Test;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegistryBasedDcpProfileMapperTest {

    @Test
    public void testMachingMapping() {

        TestMapping testMapping = doMapping("key", "key");

        assertTrue(testMapping.called);
    }

    @Test
    public void testNonMatchingMapping() {
        TestMapping testMapping = doMapping("key", "Not the key");

        assertFalse(testMapping.called);
    }

    public TestMapping doMapping(final String definedKey,
                                 final String submittedKey) {


        final String registryEntryType = "dataconservancy:types:registry-entry:dcp-profile-mapper";

        InMemoryRegistry<Mapping<String, Dcp, String, Object>> registry =
                new InMemoryRegistry<Mapping<String, Dcp, String, Object>>(registryEntryType);

        Map<String, RegistryEntry<Mapping<String, Dcp, String, Object>>> map =
                new HashMap<String, RegistryEntry<Mapping<String, Dcp, String, Object>>>();


        final TestMapping testMapping = new TestMapping();
        RegistryBasedDcpProfileMapper<String, Object> mapper =
                new RegistryBasedDcpProfileMapper<String, Object>();

        map.put(definedKey, new RegistryEntry<Mapping<String, Dcp, String, Object>>() {

            @Override
            public String getId() {
                return definedKey;
            }

            @Override
            public Mapping<String, Dcp, String, Object> getEntry() {
                return testMapping;
            }

            @Override
            public String getEntryType() {
                return registryEntryType;
            }
        });

        registry.setEntries(map);

        mapper.setRegistry(registry);

        mapper.map(submittedKey, new Dcp(), new Output<String, Object>() {

            public void write(String key, Object value) {
                assertEquals(definedKey, key);
            }

            public void close() {
                // TODO Auto-generated method stub
                
            }
        });

        return testMapping;
    }

    private class TestMapping
            implements Mapping<String, Dcp, String, Object> {

        boolean called = false;

        @Override
        public void map(String key, Dcp val, Output<String, Object> writer) {
            called = true;
            writer.write(key, val);
        }
    }
}
