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
package org.dataconservancy.transform.execution;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.dcp.DcpOutput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings({"rawtypes", "unchecked"})
public class MappingChainTest {

    private static final int ADDITION_AMOUNT = 2;

    private static final int MULTIPLY_AMOUNT = 10;

    private Mapper1IntegerAdditionToString mapper1 =
            new Mapper1IntegerAdditionToString();

    private Mapper2StringToInteger mapper2 = new Mapper2StringToInteger();

    private Mapper3Multiply mapper3 = new Mapper3Multiply();

    private Mapper4FormSentences mapper4 = new Mapper4FormSentences();

    @Before
    public void setUp() throws Exception {
        mapper1 = new Mapper1IntegerAdditionToString();
        mapper2 = new Mapper2StringToInteger();
        mapper3 = new Mapper3Multiply();
        mapper4 = new Mapper4FormSentences();
    }

    @Test
    public void testSingleElementChain() {
        MappingChain mappingChain = new MappingChain();

        mappingChain.setChain(new Mapping<String, Dcp, String, Dcp>() {

            @Override
            public void map(String key, Dcp val, Output<String, Dcp> output) {
                output.write(key, val);
            }
        });

        // Set up Dcp
        DcsDeliverableUnit dutop1 = new DcsDeliverableUnit();
        dutop1.setTitle("dutop1");
        dutop1.setId("du_id");

        DcsManifestation man = new DcsManifestation();
        man.setId("man_id");

        DcsManifestationFile manfile = new DcsManifestationFile();
        manfile.setPath("moo");
        DcsFile file = new DcsFile();
        file.setId("file_id");

        manfile.setRef(new DcsFileRef(file.getId()));
        man.addManifestationFile(manfile);

        DcsEvent ev = new DcsEvent();
        ev.setOutcome("outcome");
        ev.setEventType("ingest");
        ev.addTargets(new DcsEntityReference(file.getId()));
        ev.addTargets(new DcsEntityReference(dutop1.getId()));
        ev.addTargets(new DcsEntityReference(man.getId()));

        DcsEvent ev2 = new DcsEvent();
        ev2.setOutcome("outcome2");
        ev2.setEventType("ingest2");
        ev2.addTargets(new DcsEntityReference(file.getId()));
        ev2.addTargets(new DcsEntityReference(dutop1.getId()));
        ev2.addTargets(new DcsEntityReference(man.getId()));

        Dcp currentDcp = new Dcp();
        currentDcp.addDeliverableUnit(dutop1);
        currentDcp.addEvent(ev, ev2);
        currentDcp.addManifestation(man);;

        DcpOutput writer = new DcpOutput();

        // Set MappingChain.map()
        mappingChain.map("", currentDcp, writer);
        assertNotNull(writer.getValue());
        assertEquals(currentDcp, writer.getValue());
    }

    @Test
    public void testMultipleElementChain() {
        MappingChain mappingChain = new MappingChain();

        Mapping[] mappers = new Mapping[4];
        mappers[0] = mapper1;
        mappers[1] = mapper2;
        mappers[2] = mapper3;
        mappers[3] = mapper4;
        mappingChain.setChain(mappers);

        int testKeyInput = 12;
        int testValueInput = 3;
        TestOutput testOutput = new TestOutput<String, String>();

        mappingChain.map(new Integer(testKeyInput),
                         new Integer(testValueInput),
                         testOutput);

        String expectedOutputKey =
                "Output key is "
                        + Integer
                                .toString(((testKeyInput + ADDITION_AMOUNT) * MULTIPLY_AMOUNT));
        String expectedOutputValue =
                "Output value is "
                        + Integer
                                .toString(((testValueInput + ADDITION_AMOUNT) * MULTIPLY_AMOUNT));
        assertEquals(expectedOutputKey, testOutput.getKey());
        assertEquals(expectedOutputValue, testOutput.getValue());
    }

    @Test
    public void testMultiElementMultiWriteChain() {
        MappingChain mappingChain = new MappingChain();

        Mapping[] mappers = new Mapping[4];
        mappers[0] = new MultiWritingMapper(mapper1, 1);
        mappers[1] = new MultiWritingMapper(mapper2, 2);
        mappers[2] = new MultiWritingMapper(mapper3, 3);
        mappers[3] = new MultiWritingMapper(mapper4, 4);
        mappingChain.setChain(mappers);

        int testKeyInput = 12;
        int testValueInput = 3;
        TestOutput testOutput = new TestOutput<String, String>();

        mappingChain.map(new Integer(testKeyInput),
                         new Integer(testValueInput),
                         testOutput);

        String expectedOutputKey =
                "Output key is "
                        + Integer
                                .toString(((testKeyInput + ADDITION_AMOUNT) * MULTIPLY_AMOUNT));
        String expectedOutputValue =
                "Output value is "
                        + Integer
                                .toString(((testValueInput + ADDITION_AMOUNT) * MULTIPLY_AMOUNT));
        assertEquals(expectedOutputKey, testOutput.getKey());
        assertEquals(expectedOutputValue, testOutput.getValue());
    }

    @Test
    public void testNullChain() {
        String testKeyInput = "in";
        String testValueInput = "out";

        MappingChain<String, String, String, String> mapping =
                new MappingChain<String, String, String, String>();

        TestOutput testOutput = new TestOutput<String, String>();

        mapping.map(testKeyInput, testValueInput, testOutput);

        assertEquals(testKeyInput, testOutput.getKey());
        assertEquals(testValueInput, testOutput.getValue());

    }

    @Test
    public void testMultipleCallsToMap() {
        MappingChain mappingChain = new MappingChain();

        Mapping[] mappers = new Mapping[4];
        mappers[0] = mapper1;
        mappers[1] = mapper2;
        mappers[2] = mapper3;
        mappers[3] = mapper4;
        mappingChain.setChain(mappers);

        //Calling once
        int testKeyInput1 = 16;
        int testValueInput1 = 5;
        TestOutput testOutput1 = new TestOutput<String, String>();

        mappingChain.map(new Integer(testKeyInput1),
                         new Integer(testValueInput1),
                         testOutput1);

        String expectedOutputKey1 =
                "Output key is "
                        + Integer
                                .toString(((testKeyInput1 + ADDITION_AMOUNT) * MULTIPLY_AMOUNT));
        String expectedOutputValue1 =
                "Output value is "
                        + Integer
                                .toString(((testValueInput1 + ADDITION_AMOUNT) * MULTIPLY_AMOUNT));
        assertEquals(expectedOutputKey1, testOutput1.getKey());
        assertEquals(expectedOutputValue1, testOutput1.getValue());

        //calling twice
        int testKeyInput2 = 8;
        int testValueInput2 = 12;

        mappingChain.map(new Integer(testKeyInput2),
                         new Integer(testValueInput2),
                         testOutput1);

        String expectedOutputKey2 =
                "Output key is "
                        + Integer
                                .toString(((testKeyInput2 + ADDITION_AMOUNT) * MULTIPLY_AMOUNT));
        String expectedOutputValue2 =
                "Output value is "
                        + Integer
                                .toString(((testValueInput2 + ADDITION_AMOUNT) * MULTIPLY_AMOUNT));
        assertEquals(expectedOutputKey2, testOutput1.getKey());
        assertEquals(expectedOutputValue2, testOutput1.getValue());

    }

    private class Mapper1IntegerAdditionToString
            implements Mapping<Integer, Integer, String, String> {

        @Override
        public void map(Integer key, Integer val, Output<String, String> output) {
            output.write(Integer.toString((key + ADDITION_AMOUNT)).toString(),
                         Integer.toString((val + ADDITION_AMOUNT)).toString());
        }
    }

    private class Mapper2StringToInteger
            implements Mapping<String, String, Integer, Integer> {

        @Override
        public void map(String key, String val, Output<Integer, Integer> output) {
            output.write(Integer.valueOf(key), Integer.valueOf(val));
        }

    }

    private class Mapper3Multiply
            implements Mapping<Integer, Integer, Integer, Integer> {

        @Override
        public void map(Integer key,
                        Integer val,
                        Output<Integer, Integer> output) {
            int intKey = key.intValue();
            int intValue = val.intValue();
            output.write(new Integer(intKey * MULTIPLY_AMOUNT),
                         new Integer(intValue * MULTIPLY_AMOUNT));
        }

    }

    private class Mapper4FormSentences
            implements Mapping<Integer, Integer, String, String> {

        @Override
        public void map(Integer key, Integer val, Output<String, String> output) {
            String outputKey = "Output key is " + key.toString();
            String outputValue = "Output value is " + val.toString();
            output.write(outputKey, outputValue);
        }
    }

    private class MultiWritingMapper<Ki, Vi, Ko, Vo>
            implements Mapping<Ki, Vi, Ko, Vo> {

        private final Mapping<Ki, Vi, Ko, Vo> map;

        private final int count;

        public MultiWritingMapper(Mapping<Ki, Vi, Ko, Vo> delegate,
                                  int numberOfWrites) {
            map = delegate;
            count = numberOfWrites;
        }

        @Override
        public void map(Ki key, Vi val, Output<Ko, Vo> output) {
            for (int i = 0; i < count; i++) {
                map.map(key, val, output);
            }
        }

    }

    private class TestOutput<K, V>
            implements Output<K, V> {

        private K key;

        private V value;

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public void write(K key, V value) {
            this.key = key;
            this.value = value;
            System.out.println(key);
            System.out.println(value);
        }

        @Override
        public void close() {
            // TODO Auto-generated method stub

        }

    }
}
