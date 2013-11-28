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
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class ArchiveParentSearcherImplTest {


    @Test
    public void testGetParentsOf() throws Exception {
        Collection<DcsEntity> expectedParents = new HashSet<DcsEntity>();
        DcsDeliverableUnit p1 = new DcsDeliverableUnit();
        DcsDeliverableUnit p2 = new DcsDeliverableUnit();
        p1.setId("parent1");
        p2.setId("parent2");
        expectedParents.add(p1);
        expectedParents.add(p2);

        DcsConnector c = mock(DcsConnector.class);
        when(c.search(anyString(), anyInt(), anyInt())).thenReturn(new CountableIteratorImpl(expectedParents));

        ArchiveParentSearcherImpl underTest = new ArchiveParentSearcherImpl(c);

        Collection actualParents = underTest.getParentsOf("anyId");
        assertEquals(expectedParents, actualParents);
    }

    @Test
    public void testGetParentsOfWithConstraint() throws Exception {
        Collection<DcsEntity> duParents = new HashSet<DcsEntity>();
        DcsDeliverableUnit p1 = new DcsDeliverableUnit();
        DcsDeliverableUnit p2 = new DcsDeliverableUnit();
        p1.setId("parent1");
        p2.setId("parent2");
        duParents.add(p1);
        duParents.add(p2);

        Collection<DcsEntity> cParents = new HashSet<DcsEntity>();
        DcsCollection p3 = new DcsCollection();
        DcsCollection p4 = new DcsCollection();
        p3.setId("parent3");
        p4.setId("parent4");
        cParents.add(p3);
        cParents.add(p4);

        final Collection<DcsEntity> mixedResults = new HashSet<DcsEntity>();
        mixedResults.addAll(duParents);
        mixedResults.addAll(cParents);

        DcsConnector c = mock(DcsConnector.class);
        when(c.search(anyString(), anyInt(), anyInt())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new CountableIteratorImpl(mixedResults);
            }
        });

        ArchiveParentSearcherImpl underTest = new ArchiveParentSearcherImpl(c);

        Collection actualParents = underTest.getParentsOf("anyId", DcsDeliverableUnit.class);
        assertEquals(duParents, actualParents);

        actualParents = underTest.getParentsOf("anyId", DcsCollection.class);
        assertEquals(cParents, actualParents);

        actualParents = underTest.getParentsOf("anyId", DcsFile.class);
        assertEquals(Collections.emptySet(), actualParents);
    }


}
