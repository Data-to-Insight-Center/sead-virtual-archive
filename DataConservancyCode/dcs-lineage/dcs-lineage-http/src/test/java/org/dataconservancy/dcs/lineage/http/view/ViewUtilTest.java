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
package org.dataconservancy.dcs.lineage.http.view;

import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.http.LineageModelAttribute;
import org.dataconservancy.model.dcs.DcsEntity;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ACCEPT;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ENTITIES;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.IFMODIFIEDSINCE;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.LINEAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class ViewUtilTest {

    @Test
    public void testGetLineage() throws Exception {
        ModelAndView mv = new ModelAndView();

        assertNull(ViewUtil.getLineage(mv));

        Lineage mockLineage = mock(Lineage.class);
        mv.addObject(LINEAGE.name(), mockLineage);

        assertEquals(mockLineage, ViewUtil.getLineage(mv));
    }

    @Test
    public void testGetAccept() throws Exception {
        ModelAndView mv = new ModelAndView();

        assertNull(ViewUtil.getAccept(mv));

        mv.addObject(ACCEPT.name(), "foo");

        assertEquals("foo", ViewUtil.getAccept(mv));
    }

    @Test
    public void testGetEntities() throws Exception {
        ModelAndView mv = new ModelAndView();
        List<DcsEntity> entities = Collections.emptyList();

        assertNull(ViewUtil.getEntities(mv));

        mv.addObject(ENTITIES.name(), entities);

        assertEquals(entities, ViewUtil.getEntities(mv));
    }

    @Test
    public void testGetIfLastModified() throws Exception {
        ModelAndView mv = new ModelAndView();

        final Date actualDate = Calendar.getInstance().getTime();
        final DateTime expectedDate = new DateTime(actualDate);

        assertNull(ViewUtil.getEntities(mv));

        mv.addObject(IFMODIFIEDSINCE.name(), actualDate);

        assertEquals(expectedDate, ViewUtil.getIfModifiedSince(mv));
    }
}
