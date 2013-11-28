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
package org.dataconservancy.dcs.lineage.http.support;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.api.LineageEntry;
import org.dataconservancy.dcs.lineage.api.LineageService;
import org.dataconservancy.dcs.lineage.http.LineageController;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcs.DcsEntity;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class TestRequestUtil {

    public static class ContextHolder {
        LineageController underTest;
        Map<String, Object> mocks = new HashMap<String, Object>();

        public LineageController getUnderTest() {
            return underTest;
        }
    }

    public static ContextHolder prepareHandleLineageGetRequest(final LineageEntry expectedEntry) throws QueryServiceException {
        ContextHolder ctx = new ContextHolder();

        // The mock Lineage Service
        final LineageService lineageService = mock(LineageService.class);
        ctx.mocks.put("lineageService", lineageService);

        // The mock Lineage
        final Lineage lineage = mock(Lineage.class);
        ctx.mocks.put("lineage", lineage);

        // The mock Query Service
        final LookupQueryService<DcsEntity> lookupQueryService = mock(LookupQueryService.class);
        ctx.mocks.put("lookupQueryService", lookupQueryService);

        if (expectedEntry != null) {
            // Mock LineageService#getLineage to return the expected lineage entry
            when(lineageService.getLineage(endsWith(expectedEntry.getLineageId()))).thenReturn(lineage);

            // Mock Lineage#Iterator to return an iterator over the expected lineage entry
            when(lineage.iterator()).then(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return Arrays.asList(expectedEntry).iterator();
                }
            });

            final DcsEntity entity = new DcsEntity();
            entity.setId("entityId");

            // Mock LookupQueryService#lookup to return the DCS entity associated with the expected linage entry
            when(lookupQueryService.lookup(expectedEntry.getEntityId())).thenReturn(entity);
        } else {
            // Mock LineageService#getLineage to return a null lineage
            when(lineageService.getLineage(anyString())).thenReturn(null);
        }

        ctx.underTest = new LineageController(lineageService, mock(IdService.class), lookupQueryService);

        return ctx;
    }

    public static void verifyHandleLineageGetRequestMocks(ContextHolder ctx, LineageEntry entry)
            throws QueryServiceException {
        // Verify the mocks were called as expected.

        if (entry == null) {
            verify((LineageService) ctx.mocks.get("lineageService")).getLineage(anyString());
        } else {
            verify((LineageService) ctx.mocks.get("lineageService")).getLineage(endsWith(entry.getLineageId()));
            verify((Lineage) ctx.mocks.get("lineage")).iterator();
            verify((LookupQueryService) ctx.mocks.get("lookupQueryService")).lookup(entry.getEntityId());
        }
    }

    public static ContextHolder prepareHandleLatestLineageGetRequest(final LineageEntry expectedEntry)
            throws QueryServiceException {
        ContextHolder ctx = new ContextHolder();

        // The mock Lineage Service
        final LineageService lineageService = mock(LineageService.class);
        ctx.mocks.put("lineageService", lineageService);

        // The mock Lineage
        final Lineage lineage = mock(Lineage.class);
        ctx.mocks.put("lineage", lineage);

        // The mock Query Service
        final LookupQueryService<DcsEntity> lookupQueryService = mock(LookupQueryService.class);
        ctx.mocks.put("lookupQueryService", lookupQueryService);

        if (expectedEntry == null) {
            // Mock the behavior of LineageService#getLineage to return null
            when(lineageService.getLineage(anyString())).thenReturn(null);
        } else {
            // Mock the behavior of LineageService#getLineage to return the mock lineage
            when(lineageService.getLineage(expectedEntry.getLineageId())).thenReturn(lineage);

            // Mock the behavior of Lineage#getNewest to return the expected LineageEntry
            when(lineage.getNewest()).thenReturn(expectedEntry);

            // The DcsEntity corresponding to the supplied LineageEntry
            final DcsEntity entity = new DcsEntity();
            entity.setId(expectedEntry.getEntityId());


            when(lookupQueryService.lookup(entity.getId())).thenReturn(entity);
        }

        ctx.underTest = new LineageController(lineageService, mock(IdService.class), lookupQueryService);

        return ctx;
    }

    public static void verifyHandleLatestLineageGetRequestMocks(ContextHolder ctx, LineageEntry entry)
            throws QueryServiceException {
        // Verify the mocks were called as expected.

        if (entry == null) {
            verify((LineageService) ctx.mocks.get("lineageService")).getLineage(anyString());
        } else {
            verify((LineageService) ctx.mocks.get("lineageService")).getLineage(entry.getLineageId());
            verify((Lineage) ctx.mocks.get("lineage")).getNewest();
            verify((LookupQueryService) ctx.mocks.get("lookupQueryService")).lookup(entry.getEntityId());
        }
    }

    public static ContextHolder prepareHandleOriginalLineageGetRequest(final LineageEntry expectedEntry)
            throws QueryServiceException {
        ContextHolder ctx = new ContextHolder();

        // The mock Lineage Service
        final LineageService lineageService = mock(LineageService.class);
        ctx.mocks.put("lineageService", lineageService);

        // The mock Lineage
        final Lineage lineage = mock(Lineage.class);
        ctx.mocks.put("lineage", lineage);

        // The mock Query Service
        final LookupQueryService<DcsEntity> lookupQueryService = mock(LookupQueryService.class);
        ctx.mocks.put("lookupQueryService", lookupQueryService);

        if (expectedEntry == null) {
            // Mock the behavior of LineageService#getLineage to return null
            when(lineageService.getLineage(anyString())).thenReturn(null);
        } else {
            // Mock the behavior of LineageService#getLineage to return the mock lineage
            when(lineageService.getLineage(expectedEntry.getLineageId())).thenReturn(lineage);

            // Mock the behavior of Lineage#getOldest to return the expected LineageEntry
            when(lineage.getOldest()).thenReturn(expectedEntry);

            // The DcsEntity corresponding to the supplied LineageEntry
            final DcsEntity entity = new DcsEntity();
            entity.setId(expectedEntry.getEntityId());


            when(lookupQueryService.lookup(entity.getId())).thenReturn(entity);
        }

        ctx.underTest = new LineageController(lineageService, mock(IdService.class), lookupQueryService);

        return ctx;
    }

    public static void verifyHandleOriginalLineageGetRequestMocks(ContextHolder ctx, LineageEntry entry)
            throws Exception {
        // Verify the mocks were called as expected.

        if (entry == null) {
            verify((LineageService) ctx.mocks.get("lineageService")).getLineage(anyString());
        } else {
            verify((LineageService) ctx.mocks.get("lineageService")).getLineage(entry.getLineageId());
            verify((Lineage) ctx.mocks.get("lineage")).getOldest();
            verify((LookupQueryService) ctx.mocks.get("lookupQueryService")).lookup(entry.getEntityId());
        }
    }

    public static ContextHolder prepareHandleSearchLineageGetRequest(final LineageEntry fromEntry, final LineageEntry toEntry)
            throws Exception {
        ContextHolder ctx = new ContextHolder();

        // The mock Lineage Service
        final LineageService lineageService = mock(LineageService.class);
        ctx.mocks.put("lineageService", lineageService);

        // The mock Lineage
        final Lineage lineage = mock(Lineage.class);
        ctx.mocks.put("lineage", lineage);

        // The mock Query Service
        final LookupQueryService<DcsEntity> lookupQueryService = mock(LookupQueryService.class);
        ctx.mocks.put("lookupQueryService", lookupQueryService);

        // The mock ID Service
        final IdService idService = mock(IdService.class);
        final Identifier id = mock(Identifier.class);
        ctx.mocks.put("identifierService", idService);

        when(idService.fromUid(anyString())).thenReturn(id);
        when(id.getType()).thenReturn(Types.DELIVERABLE_UNIT.getTypeName());

        if (fromEntry != null && toEntry != null) {
            when(lineageService.getLineageForEntityRange(fromEntry.getEntityId(), toEntry.getEntityId())).thenReturn(lineage);
            when(lineageService.getLineage(anyString())).thenReturn(lineage);

            when(lineage.iterator()).then(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return Arrays.asList(fromEntry, toEntry).iterator();
                }
            });

            final DcsEntity fromEntity = new DcsEntity();
            final DcsEntity toEntity = new DcsEntity();
            fromEntity.setId(fromEntry.getEntityId());
            toEntity.setId(toEntry.getEntityId());

            when(lookupQueryService.lookup(fromEntity.getId())).thenReturn(fromEntity);
            when(lookupQueryService.lookup(toEntity.getId())).thenReturn(toEntity);


        } else {
            
        }

        ctx.underTest = new LineageController(lineageService, idService, lookupQueryService);

        return ctx;
    }

    public static void verifyHandleSearchLineageGetRequestMocks(ContextHolder ctx, LineageEntry fromEntry, LineageEntry toEntry)
            throws Exception {
        // Verify the mocks were called as expected.

        if (fromEntry != null && toEntry != null) {
            verify((LineageService) ctx.mocks.get("lineageService")).getLineageForEntityRange(fromEntry.getEntityId(), toEntry.getEntityId());
            verify((LineageService) ctx.mocks.get("lineageService")).getLineage(anyString());
            verify((Lineage) ctx.mocks.get("lineage")).iterator();
            verify((LookupQueryService) ctx.mocks.get("lookupQueryService")).lookup(fromEntry.getEntityId());
            verify((LookupQueryService) ctx.mocks.get("lookupQueryService")).lookup(toEntry.getEntityId());
        }

    }

}
