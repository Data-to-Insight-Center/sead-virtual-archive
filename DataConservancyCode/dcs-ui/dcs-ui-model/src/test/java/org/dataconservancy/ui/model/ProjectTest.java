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

package org.dataconservancy.ui.model;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = {"classpath*:/test-applicationContext.xml"})
public class ProjectTest extends BaseUnitTest {

    @Autowired
    private Project projectDuplicate;

    @Autowired
    private Project projectThree;

    @Autowired
    private Project projectTwo;

    @Before
    public void setUp() {

        projectDuplicate.setId(projectOne.getId());
        projectDuplicate.setNumbers(projectOne.getNumbers());
        projectDuplicate.setName(projectOne.getName());
        projectDuplicate.setDescription(projectOne.getDescription());
        projectDuplicate.setPublisher(projectOne.getPublisher());
        projectDuplicate.setStartDate(projectOne.getStartDate());
        projectDuplicate.setEndDate(projectOne.getEndDate());
        projectDuplicate.setStorageAllocated(projectOne.getStorageAllocated());
        projectDuplicate.setStorageUsed(projectOne.getStorageUsed());
        projectDuplicate.setFundingEntity(projectOne.getFundingEntity());

        projectThree.setId(projectOne.getId());
        projectThree.setNumbers(projectOne.getNumbers());
        projectThree.setName(projectOne.getName());
        projectThree.setDescription(projectOne.getDescription());
        projectThree.setPublisher(projectOne.getPublisher());
        projectThree.setStartDate(projectOne.getStartDate());
        projectThree.setEndDate(projectOne.getEndDate());
        projectThree.setStorageAllocated(projectOne.getStorageAllocated());
        projectThree.setStorageUsed(projectOne.getStorageUsed());
        projectThree.setFundingEntity(projectOne.getFundingEntity());
    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertTrue(projectOne.equals(projectOne));
        assertFalse(projectOne.equals(projectTwo));
    }

    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(projectOne.equals(projectThree));
        assertTrue(projectThree.equals(projectOne));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(projectOne.equals(projectDuplicate));
        assertTrue(projectDuplicate.equals(projectThree));
        assertTrue(projectOne.equals(projectThree));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(projectOne.equals(projectDuplicate));
        assertTrue(projectOne.equals(projectDuplicate));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(projectOne.equals(null));
    }

}
