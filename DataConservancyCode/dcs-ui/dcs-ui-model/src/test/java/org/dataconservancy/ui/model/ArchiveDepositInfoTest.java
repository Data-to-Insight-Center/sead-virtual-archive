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

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


/**
 *
 */
public class ArchiveDepositInfoTest {

    private final String depositId = "depositId";
    private final String objId = "objId";
    private final String archiveId = "archiveId";
    private final ArchiveDepositInfo.Status status = Status.PENDING;
    private final ArchiveDepositInfo.Type type = Type.DATASET;
    private final DateTime protoDt = DateTime.now();

    private ArchiveDepositInfo prototype;

    @Before
    public void setUp() {
        prototype = new ArchiveDepositInfo();
        prototype.setArchiveId(archiveId);
        prototype.setObjectId(objId);
        prototype.setDepositId(depositId);
        prototype.setDepositStatus(status);
        prototype.setObjectType(type);
        prototype.setDepositDateTime(protoDt);
    }

    /**
     * Tests that the copy constructor copies the fields properly, including that the copy is by value, not reference.
     * The copied object should be equal according to {@link Object#equals(Object)} and should have the same hash code.
     * <p/>
     *
     * @throws Exception
     */
    @Test
    public void testCopyConstructor() throws Exception {
        final ArchiveDepositInfo copy = new ArchiveDepositInfo(prototype);
        assertEquals(depositId, copy.getDepositId());
        assertEquals(objId, copy.getObjectId());
        assertEquals(archiveId, copy.getArchiveId());
        assertEquals(status, copy.getDepositStatus());
        assertEquals(type, copy.getObjectType());
        assertEquals(protoDt, copy.getDepositDateTime());

        assertEquals(prototype, copy);
        assertEquals(prototype.hashCode(), copy.hashCode());

        prototype.setArchiveId("bar");
        assertFalse(prototype.getArchiveId().equals(copy.getArchiveId()));

        prototype.setDepositId("bar");
        assertFalse(prototype.getDepositId().equals(copy.getDepositId()));

        prototype.setObjectId("bar");
        assertFalse(prototype.getObjectId().equals(copy.getObjectId()));

        prototype.setDepositStatus(Status.DEPOSITED);
        assertFalse(prototype.getDepositStatus().equals(copy.getDepositStatus()));

        prototype.setObjectType(Type.COLLECTION);
        assertFalse(prototype.getObjectType().equals(copy.getObjectType()));

        prototype.setDepositDateTime(protoDt.plusDays(1));
        assertFalse(prototype.getDepositDateTime().equals(copy.getDepositDateTime()));

        assertFalse(prototype.equals(copy));
        assertFalse(prototype.hashCode() == copy.hashCode());
    }
}
