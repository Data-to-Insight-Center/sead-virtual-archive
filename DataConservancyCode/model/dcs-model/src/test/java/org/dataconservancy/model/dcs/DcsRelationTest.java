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
package org.dataconservancy.model.dcs;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class DcsRelationTest {

    private static final String target = "entityId";
    private static final DcsRelationship rel = DcsRelationship.IS_METADATA_FOR;
    private static final String relUri = rel.asString();
    private DcsRelation underTest;


    @Before
    public void setUp() {
        underTest = new DcsRelation();
        underTest.setRef(new DcsEntityReference(target));
        underTest.setRelUri(relUri);
    }


    @Test
    public void testCopyConstructorA() {
        assertEquals(underTest, new DcsRelation(underTest));
    }

    @Test
    public void testCopyConstructorB() {
        underTest = new DcsRelation(relUri, target);
        assertEquals(underTest, new DcsRelation(underTest));
    }

    @Test
    public void testCopyConstructorC() {
        underTest = new DcsRelation(rel, target);
        assertEquals(underTest, new DcsRelation(underTest));
    }

    @Test
    public void testCopyConstructorD() {
        underTest = new DcsRelation(relUri, new DcsEntityReference(target));
        assertEquals(underTest, new DcsRelation(underTest));
    }

    @Test
    public void testCopyConstructorE() {
        underTest = new DcsRelation(rel, new DcsEntityReference(target));        
        assertEquals(underTest, new DcsRelation(underTest));
    }

    @Test
    public void testCopyConstructorF() {
        underTest = new DcsRelation();
        underTest.setRef(new DcsEntityReference(target));
        underTest.setRelUri(relUri);
        assertEquals(underTest, new DcsRelation(underTest));
    }

    @Test
    public void testCopyConstructorG() {
        underTest = new DcsRelation(rel, new DcsEntityReference(target));
        assertEquals(underTest, new DcsRelation(new DcsRelation(underTest)));
    }
}
