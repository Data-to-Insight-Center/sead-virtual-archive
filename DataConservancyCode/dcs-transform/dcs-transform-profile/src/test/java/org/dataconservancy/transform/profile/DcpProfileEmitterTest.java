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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.transform.Output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DcpProfileEmitterTest {

    @Test
    public void testMatch() {
        final String MATCHING_KEY = "key1";
        List<String> matches = doSimpleMatch(MATCHING_KEY, "key2", true);
        assertEquals(1, matches.size());
        assertTrue(matches.contains(MATCHING_KEY));
    }

    @Test
    public void testNonMatch() {
        List<String> matches = doSimpleMatch("key1", "key2", false);
        assertEquals(0, matches.size());
    }

    private List<String> doSimpleMatch(String matchKey,
                                       String nonMatchKey,
                                       boolean shouldMatch) {
        DcpProfileEmitter emitter = new DcpProfileEmitter();

        Map<String, DcpProfileDetector> detectors =
                new HashMap<String, DcpProfileDetector>();

        detectors.put(matchKey, shouldMatch ? new AlwaysHasFormat()
                : new NeverHasFormat());
        detectors.put(nonMatchKey, new NeverHasFormat());

        emitter.setDetectorMap(detectors);

        final Dcp dcp = new Dcp();
        final List<String> matches = new ArrayList<String>();

        emitter.map(matchKey, dcp, new Output<String, Dcp>() {

            public void write(String key, Dcp value) {
                assertTrue(value == dcp);
                matches.add(key);
            }

            public void close() {
            }
        });

        return matches;
    }

    private class AlwaysHasFormat
            implements DcpProfileDetector {

        public boolean hasFormat(Dcp dcp) {
            return true;
        }
    }

    private class NeverHasFormat
            implements DcpProfileDetector {

        public boolean hasFormat(Dcp dcp) {
            return false;
        }
    }
}
