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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class DcsEntityTest {

    private final String ID = "urn:entity:id";

    private DcsEntity entity;
    private DcsEntity entityTwo;

    private DcsResourceIdentifier altIdOne;
    private DcsResourceIdentifier altIdTwo;
    private String altIdValueOne = "lsid:1234";
    private String altIdAuthorityTwo = "wwwanddns";
    private String altIdSchemeTwo = "url";
    private String altIdValueTwo = "http://www.dataconservancy.org";
    private List<DcsResourceIdentifier> altIds = new ArrayList<DcsResourceIdentifier>();

    @Before
    public void setUp() {
        entity = new DcsEntity();
        entity.setId(ID);

        entityTwo = new DcsEntity();
        entityTwo.setId(ID);

        altIdOne = new DcsResourceIdentifier();
        altIdOne.setIdValue(altIdValueOne);

        altIdTwo = new DcsResourceIdentifier();
        altIdTwo.setAuthorityId(altIdAuthorityTwo);
        altIdTwo.setTypeId(altIdSchemeTwo);
        altIdTwo.setIdValue(altIdValueTwo);

        entity.addAlternateId(altIdOne);
        entity.addAlternateId(altIdTwo);

        altIds.add(altIdOne);
        altIds.add(altIdTwo);

        entityTwo.setAlternateIds(altIds);
    }

    @Test
    public void testIdOk() throws Exception {
        assertEquals(ID, entity.getId());
        assertNull(new DcsEntity().getId());
        entity.setId("another id");
        assertEquals("another id", entity.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetIdEmptyString() throws Exception {
        entity.setId(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetIdNullString() throws Exception {
        entity.setId(null);
    }

    @Test
    public void testAlternateIdsOk() throws Exception {
        assertEquals(2, entity.getAlternateIds().size());
        assertEquals(altIds, entity.getAlternateIds());

        final List<DcsResourceIdentifier> emptyAlternateIds = new ArrayList<DcsResourceIdentifier>();
        entity.setAlternateIds(emptyAlternateIds);
        assertEquals(emptyAlternateIds, entity.getAlternateIds());

        entity.addAlternateId(altIdOne);
        entity.addAlternateId(altIdTwo);
        assertEquals(altIds, entity.getAlternateIds());

        assertEquals(0, new DcsEntity().getAlternateIds().size());
    }

    @Test
    public void testEquals() throws Exception {
        DcsEntity entityCopy = new DcsEntity(entity);

        // reflexive
        assertTrue(entity.equals(entity));
        assertTrue(entityCopy.equals(entityCopy));
        assertTrue(entityTwo.equals(entityTwo));

        // symmetric
        assertTrue(entity.equals(entityTwo) && entityTwo.equals(entity));

        // transitive
        assertTrue(entity.equals(entityTwo));
        assertTrue(entity.equals(entityCopy));
        assertTrue(entityCopy.equals(entityTwo));

        // consistent
        assertTrue(entity.equals(entityTwo) && entity.equals(entityTwo));
        assertTrue(entityCopy.equals(entityTwo) && entityCopy.equals(entityTwo));
    }
}
