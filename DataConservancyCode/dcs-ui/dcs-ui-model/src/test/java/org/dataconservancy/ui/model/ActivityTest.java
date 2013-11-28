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

import org.joda.time.DateTime;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: HanhVu Date: 2/9/12 Time: 10:46 AM To change
 * this template use File | Settings | File Templates.
 */
@ContextConfiguration(locations = {"classpath*:/test-applicationContext.xml"})
public class ActivityTest
        extends BaseUnitTest {

    private Activity one = new Activity();

    private Activity two = new Activity();

    private Activity three = new Activity();

    private Activity different = new Activity();

    @Before
    public void setUp() {

        DateTime activityDate = new DateTime();
        one.setActor(user);
        one.setDateTimeOfOccurrence(activityDate);
        one.setDescription("Activity description");
        one.setType(Activity.Type.COLLECTION_DEPOSIT);

        two.setActor(user);
        two.setDateTimeOfOccurrence(activityDate);
        two.setDescription("Activity description");
        two.setType(Activity.Type.COLLECTION_DEPOSIT);

        three.setActor(user);
        three.setDateTimeOfOccurrence(activityDate);
        three.setDescription("Activity description");
        three.setType(Activity.Type.COLLECTION_DEPOSIT);

        different.setActor(admin);
        different.setDateTimeOfOccurrence(new DateTime());
        different.setDescription("Activity description 2");
        different.setType(Activity.Type.DATASET_DEPOSIT);

    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertTrue(one.equals(one));
        assertFalse(one.equals(different));
    }

    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(one.equals(two));
        assertTrue(two.equals(one));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(one.equals(two));
        assertTrue(two.equals(three));
        assertTrue(one.equals(three));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(one.equals(two));
        assertTrue(one.equals(two));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(one.equals(null));
    }
}
