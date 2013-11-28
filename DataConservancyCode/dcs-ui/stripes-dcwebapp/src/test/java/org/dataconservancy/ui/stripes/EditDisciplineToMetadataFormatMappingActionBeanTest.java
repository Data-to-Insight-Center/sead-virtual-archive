/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.ui.stripes;

import net.sourceforge.stripes.mock.MockRoundtrip;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.mhf.validation.api.MetadataValidator;
import org.dataconservancy.registry.api.Registry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.dao.DisciplineDAO;
import org.dataconservancy.ui.dao.MetadataFormatPropertiesDao;
import org.dataconservancy.ui.model.MetadataFormatProperties;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.MetadataFormatService;
import org.dataconservancy.ui.services.MetadataFormatServiceRegistryImpl;
import org.dataconservancy.ui.services.RelationshipService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 */
public class EditDisciplineToMetadataFormatMappingActionBeanTest extends BaseActionBeanTest {

    private MetadataFormatService metadataFormatService;

    @Autowired
    @Qualifier("formatRegistryImpl")
    private TypedRegistry<DcsMetadataFormat> metadataFormatRegistry;

    @Autowired
    @Qualifier("metadataValidatorRegistry")
    private TypedRegistry<MetadataValidator> validatorRegistry;
    
    @Autowired
    private DisciplineDAO disciplineDao;
    
    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private RelationshipService relService;

    @Autowired
    private MetadataFormatPropertiesDao mdfPropDao;

    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;

    @Before
    public void wireMetadataFormatService() {
        metadataFormatService = new MetadataFormatServiceRegistryImpl(metadataFormatRegistry, archiveService,
                validatorRegistry, relService, mdfPropDao, disciplineDao, idService);
        GenericWebApplicationContext appContext = (GenericWebApplicationContext) servletCtx.getAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        appContext.getBeanFactory().registerSingleton("metadataFormatService", metadataFormatService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, appContext);
    }

    @Before
    public void verifyAssumptions() {
        assertEquals("Expected 5 disciplines, but found " + disciplineDao.list().size(),
                5, disciplineDao.list().size());

        /*  commented out because the POs asked for Social science to be added, but no format has been set to be
        associated with social science, causing this test to fail
        for (Discipline d : disciplineDao.list()) {
            assertTrue("Expected metadataformats for discipline " + d.getId(),
                    relService.getMetadataFormatsForDiscipline(d.getId()).size() > 0);
        }
        */
    }


    /**
     * Asserts that the discipline ids provided as url parameters are returned by this method.
     *
     * @throws Exception
     */
    @Test
    public void testGetAndSetDisciplineIds() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, EditDisciplineToMetadataFormatMappingActionBean.class, authenticateUser(admin));

        List<String> expectedIds = new ArrayList<String>();
        expectedIds.add("dc:discipline:Biology");
        expectedIds.add("dc:discipline:EarthScience");

        for (String disciplineId : expectedIds) {
            assertNotNull("Expected Discipline object " + disciplineId + " to exist!", disciplineDao.get(disciplineId));
        }

        rt.addParameter("disciplineIds", expectedIds.toArray(new String[]{}));

        final String mdfId = "dataconservancy.org:registry:metadata-format:entry:id:1";
        assertNotNull(metadataFormatService.getMetadataFormat(mdfId));
        rt.addParameter("metadataFormatId", mdfId);

        rt.execute();

        EditDisciplineToMetadataFormatMappingActionBean bean = rt.getActionBean(EditDisciplineToMetadataFormatMappingActionBean.class);

        assertEquals(expectedIds, bean.getDisciplineIds());
    }

    /**
     * Asserts that non-existent discipline ids are not set on the action bean.
     *
     * @throws Exception
     */
    @Test
    public void testSetNonExistentDisciplineId() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, EditDisciplineToMetadataFormatMappingActionBean.class, authenticateUser(admin));

        final String nonExistentId = "dc:discipline:FooBarBaz";
        assertNull(disciplineDao.get(nonExistentId));
        rt.addParameter("disciplineIds", nonExistentId);

        final String mdfId = "dataconservancy.org:registry:metadata-format:entry:id:1";
        assertNotNull(metadataFormatService.getMetadataFormat(mdfId));
        rt.addParameter("metadataFormatId", mdfId);

        rt.execute();

        EditDisciplineToMetadataFormatMappingActionBean bean = rt.getActionBean(EditDisciplineToMetadataFormatMappingActionBean.class);

        assertEquals(Collections.EMPTY_LIST, bean.getDisciplineIds());
    }

    /**
     * Asserts that metadataFormatId provided by the url parameter is returned by this method.
     *
     * @throws Exception
     */
    @Test
    public void testGetAndSetMetadataFormatId() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, EditDisciplineToMetadataFormatMappingActionBean.class, authenticateUser(admin));
        rt.addParameter("disciplineIds", "dc:discipline:Biology", "dc:discipline:Astronomy", "dc:discipline:EarthScience");
        final String mdfId = "dataconservancy.org:registry:metadata-format:entry:id:1";
        assertNotNull(metadataFormatService.getMetadataFormat(mdfId));
        rt.addParameter("metadataFormatId", mdfId);

        rt.execute();

        EditDisciplineToMetadataFormatMappingActionBean bean = rt.getActionBean(EditDisciplineToMetadataFormatMappingActionBean.class);

        assertEquals(mdfId, bean.getMetadataFormatId());
    }

    /**
     * Asserts that a non-existent metadataformat id is not set on the action bean
     *
     * @throws Exception
     */
    @Test
    public void testGetAndSetNonExistentMetadataFormatId() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, EditDisciplineToMetadataFormatMappingActionBean.class, authenticateUser(admin));
        final String mdfId = "non-existent metadata format id";
        assertNull(metadataFormatService.getMetadataFormat(mdfId));
        rt.addParameter("metadataFormatId", mdfId);

        rt.execute();

        EditDisciplineToMetadataFormatMappingActionBean bean = rt.getActionBean(EditDisciplineToMetadataFormatMappingActionBean.class);

        assertEquals(null, bean.getMetadataFormatId());
    }

    /**
     * Asserts that this method in actuality returns all disciplines.
     *
     * @throws Exception
     */
    @Test
    public void testGetAllDisciplines() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, EditDisciplineToMetadataFormatMappingActionBean.class, authenticateUser(admin));
        rt.execute();
        List<Discipline> actualDisciplines = rt.getActionBean(EditDisciplineToMetadataFormatMappingActionBean.class).getAllDisciplines();
        assertEquals(disciplineDao.list(), actualDisciplines);
    }

    /**
     * Asserts that providing a metaformatId as a url parameter will allow the action bean to return the full metadataformat object
     *
     * @throws Exception
     */
    @Test
    public void testGetMetadataFormat() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, EditDisciplineToMetadataFormatMappingActionBean.class, authenticateUser(admin));
        final String mdfId = "dataconservancy.org:formats:file:metadata:fgdc:xml";
        assertTrue(mdfId.equals(metadataFormatService.getMetadataFormat(mdfId).getId()));
        rt.addParameter("metadataFormatId", mdfId);

        rt.execute();

        EditDisciplineToMetadataFormatMappingActionBean bean = rt.getActionBean(EditDisciplineToMetadataFormatMappingActionBean.class);

        assertEquals(metadataFormatService.getMetadataFormat(mdfId), bean.getMetadataFormat());
    }

    /**
     * Asserts that providing a non-existent metaformatId as a url parameter will not allow the bean to produce a metaformat object
     *
     * @throws Exception
     */
    @Test
    public void testGetNonExistentMetadataFormat() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, EditDisciplineToMetadataFormatMappingActionBean.class, authenticateUser(admin));
        final String mdfId = "non-existent metadata format id";
        assertNull(metadataFormatService.getMetadataFormat(mdfId));
        rt.addParameter("metadataFormatId", mdfId);

        rt.execute();

        EditDisciplineToMetadataFormatMappingActionBean bean = rt.getActionBean(EditDisciplineToMetadataFormatMappingActionBean.class);

        assertNull(bean.getMetadataFormat());
    }

    /**
     * Asserts the proper forward url is used when viewing the edit discipline mapping page.
     *
     * @throws Exception
     */
    @Test
    public void testEditDisciplineMapping() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, EditDisciplineToMetadataFormatMappingActionBean.class, authenticateUser(admin));
        rt.execute("editDisciplineMapping");
        assertEquals(EditDisciplineToMetadataFormatMappingActionBean.EDIT_DISCIPLINE_MAPPING, rt.getForwardUrl());
    }

    /**
     * Asserts that a discipline is removed and two disciplines are added for a metadata format when the saveDisciplineMapping
     * event is executed.
     *
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    @DirtiesContext
    public void testSaveDisciplineMapping() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, EditDisciplineToMetadataFormatMappingActionBean.class, authenticateUser(admin));

        final String formatId = "dataconservancy.org:formats:file:metadata:fgdc:xml";
        assertNotNull(metadataFormatService.getMetadataFormat(formatId));

        final String bioDisciplineId = "dc:discipline:Biology";
        final String astroDisciplineId = "dc:discipline:Astronomy";
        final Set<String> currentDisciplines = relService.getDisciplinesForMetadataFormats(formatId);
        assertFalse(currentDisciplines.contains(bioDisciplineId));
        assertFalse(currentDisciplines.contains(astroDisciplineId));

        final List<String> expectedDisciplineIds = new ArrayList<String>();
        expectedDisciplineIds.addAll(currentDisciplines);
        expectedDisciplineIds.add(bioDisciplineId);
        expectedDisciplineIds.add(astroDisciplineId);

        for (String disciplineId : expectedDisciplineIds) {
            assertNotNull("Expected Discipline object " + disciplineId + " to exist!", disciplineDao.get(disciplineId));
        }

        rt.addParameter("mdfProperties.disciplineIds", expectedDisciplineIds.toArray(new String[]{}));
        rt.addParameter("metadataFormatId", formatId);

        rt.execute("saveDisciplineMapping");

        Set<String> disciplineIds = relService.getDisciplinesForMetadataFormats(formatId);
        assertTrue(disciplineIds.containsAll(expectedDisciplineIds));
        assertTrue(expectedDisciplineIds.containsAll(disciplineIds));
        assertTrue(expectedDisciplineIds.size() == disciplineIds.size());
    }

    /**
     * Modifies a flag of an existing metadata format.
     *
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    @DirtiesContext
    public void testSaveFlags() throws Exception {

        // Assert the format exists.
        final String mdfId = "dataconservancy.org:formats:file:metadata:fgdc:xml";
        assertNotNull(metadataFormatService.getMetadataFormat(mdfId));

        // Get its properties.
        MetadataFormatProperties mdfProps;
        if ((mdfProps = metadataFormatService.getProperties(mdfId)) == null) {
            mdfProps = new MetadataFormatProperties();
            mdfProps.setFormatId(mdfId);
        }

        // Current value of the flag
        final boolean appliesToCollection = mdfProps.isAppliesToCollection();

        // Modify the value of the flag
        MockRoundtrip rt = new MockRoundtrip(servletCtx, EditDisciplineToMetadataFormatMappingActionBean.class, authenticateUser(admin));
        rt.addParameter("mdfProperties.appliesToCollection", String.valueOf(!appliesToCollection));
        rt.addParameter("metadataFormatId", mdfId);
        rt.execute("saveDisciplineMapping");

        // Verify the flag was changed
        assertEquals(!appliesToCollection, metadataFormatService.getProperties(mdfId).isAppliesToCollection());
    }
}
