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
package org.dataconservancy.ui.services;

import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class ArchiveAncestrySearcherImplTest {

    @Test
    public void testGetAncestorsOf() throws Exception {
                Collection<DcsEntity> expectedAncestors = new HashSet<DcsEntity>();
        DcsDeliverableUnit a1 = new DcsDeliverableUnit();
        DcsManifestation a2 = new DcsManifestation();

        a1.setId("du");
        a2.setId("man");
        expectedAncestors.add(a1);
        expectedAncestors.add(a2);

        DcsConnector c = mock(DcsConnector.class);
        when(c.search(anyString(), anyInt(), anyInt())).thenReturn(new CountableIteratorImpl(expectedAncestors));

        ArchiveAncestrySearcherImpl underTest = new ArchiveAncestrySearcherImpl(c);

        Collection actualAncestors = underTest.getAncestorsOf("anyId", false);
        assertEquals(expectedAncestors, actualAncestors);
    }

    @Test
    public void testGetAncestorsOfInclusive() throws Exception {
                Collection<DcsEntity> expectedAncestors = new HashSet<DcsEntity>();
        DcsDeliverableUnit a1 = new DcsDeliverableUnit();
        DcsManifestation a2 = new DcsManifestation();
        DcsFile a3 = new DcsFile();

        a1.setId("du");
        a2.setId("man");
        a3.setId("file");
        expectedAncestors.add(a1);
        expectedAncestors.add(a2);
        expectedAncestors.add(a3);

        DcsConnector c = mock(DcsConnector.class);
        when(c.search(anyString(), anyInt(), anyInt())).thenReturn(new CountableIteratorImpl(expectedAncestors));

        ArchiveAncestrySearcherImpl underTest = new ArchiveAncestrySearcherImpl(c);

        Collection actualAncestors = underTest.getAncestorsOf("anyId", true);
        assertEquals(expectedAncestors, actualAncestors);
    }
}
