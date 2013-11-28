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
package org.dataconservancy.dcs.id.impl;

import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {

    /**
     * Create the test case
     * 
     * @param testName
     *        name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * testIdentifierCreation
     * 
     * @throws IdentifierSyntaxException
     */
    public void testIdentifierCreation() {

        MemoryIdServiceImpl idSvc = new MemoryIdServiceImpl();

        idSvc.create("type");

    }

    public void testIdentifierUidUnique() {

        MemoryIdServiceImpl idSvc = new MemoryIdServiceImpl();

        Identifier id1 = idSvc.create("type");
        Identifier id2 = idSvc.create("type");

        assertTrue(!id1.getUid().equals(id2.getUid()));
    }

    public void testIdentifierRetreival() throws IdentifierNotFoundException {

        MemoryIdServiceImpl idSvc = new MemoryIdServiceImpl();

        Identifier id1 = idSvc.create("type");
        Identifier id2 = (Identifier) idSvc.fromUid(id1.getUid());

        assertTrue(id1.getUrl().equals(id2.getUrl()));
    }
}
