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

import junit.framework.Assert;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.mhf.validation.api.MetadataValidator;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.dao.DisciplineDAO;
import org.dataconservancy.ui.dao.MetadataFormatPropertiesDao;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.model.MetadataFormatProperties;
import org.dataconservancy.ui.model.MetadataResult;
import org.dataconservancy.ui.services.MetadataFormatService;
import org.dataconservancy.ui.services.MetadataFormatServiceRegistryImpl;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.stripes.UiConfigurationActionBean.MetadataFormatDescriptor;
import org.dataconservancy.ui.stripes.UiConfigurationActionBean.Namespace;

import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.util.*;

import static org.junit.Assert.*;

/**
 *
 */
public class UiConfigurationActionBeanTest extends BaseActionBeanTest {

    private MetadataFormatService metadataFormatService;

    @Autowired
    @Qualifier("formatRegistryImpl")
    private TypedRegistry<DcsMetadataFormat> metadataFormatRegistry;

    @Autowired
    @Qualifier("metadataValidatorRegistry")
    private TypedRegistry<MetadataValidator> validatorRegistry;
    
    @Autowired
    private RelationshipService relationshipService;
    
    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private DisciplineDAO disciplineDao;

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

    /**
     * Asserts that the MetadataFormats known to the MetadataService are the same MetadataFormats returned by
     * the action bean.
     *
     * @throws Exception
     */
    @Test
    public void testGetMetadataFormats() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.execute();

        Set<DcsMetadataFormat> expectedMdfs = metadataFormatService.getMetadataFormats();
        assertTrue("Expected to have at least one metadata format!", expectedMdfs.size() > 1);

        Set<DcsMetadataFormat> actualMdfs = new HashSet<DcsMetadataFormat>();
        actualMdfs.addAll(rt.getActionBean(UiConfigurationActionBean.class).getMetadataFormats());

        assertEquals(expectedMdfs, actualMdfs);
    }

    /**
     * Asserts that the MetadataFormats are mapped to the expected Disciplines.
     *
     * @throws Exception
     */
    @Test
    public void testGetDisciplinesForMetadataFormat() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.execute();

        assertTrue("Expected to have at least one metadata format!", metadataFormatService.getMetadataFormats().size() > 1);

        Map<String, List<Discipline>> expectedMap = new HashMap<String, List<Discipline>>();
        for (DcsMetadataFormat mdf : metadataFormatService.getMetadataFormats()) {
            List<Discipline> disciplines = null;
            if (expectedMap.containsKey(mdf.getId())) {
                disciplines = expectedMap.get(mdf.getId());
            } else {
                disciplines = new ArrayList<Discipline>();
                expectedMap.put(mdf.getId(), disciplines);
            }

            for (String disciplineId : relationshipService.getDisciplinesForMetadataFormats(mdf.getId())) {
                Discipline d = disciplineDao.get(disciplineId);
                assertNotNull("Expected Discipline for id " + disciplineId + ", but it was null.", d);
                disciplines.add(d);
            }
        }

        assertTrue("Expected at least one discipline mapping", expectedMap.size() > 1);

        Map<String, List<Discipline>> actualMap = rt.getActionBean(UiConfigurationActionBean.class).getDisciplinesForMetadataFormat();

        assertEquals(expectedMap, actualMap);
    }

    /**
     * Asserts that the ActionBean will return the correct list of discipline ids for a given metadataformat.
     *
     * @throws Exception
     */
    @Test
    public void testGetDisciplineIdsForMetadataFormat() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.execute();

        assertTrue("Expected to have at least one metadata format!", metadataFormatService.getMetadataFormats().size() > 1);

        Map<String, List<String>> expectedMap = new HashMap<String, List<String>>();
        for (DcsMetadataFormat mdf : metadataFormatService.getMetadataFormats()) {
            List<String> disciplines = null;
            if (expectedMap.containsKey(mdf.getId())) {
                disciplines = expectedMap.get(mdf.getId());
            } else {
                disciplines = new ArrayList<String>();
                expectedMap.put(mdf.getId(), disciplines);
            }

            for (String disciplineId : relationshipService.getDisciplinesForMetadataFormats(mdf.getId())) {
                Discipline d = disciplineDao.get(disciplineId);
                assertNotNull("Expected Discipline for id " + disciplineId + ", but it was null.", d);
                disciplines.add(disciplineId);
            }
        }

        assertTrue("Expected at least one discipline mapping", expectedMap.size() > 1);

        Map<String, List<String>> actualMap = rt.getActionBean(UiConfigurationActionBean.class).getDisciplineIdsForMetadataFormat();

        assertEquals(expectedMap, actualMap);
    }

    @Test
    public void testDefaultResolution() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.execute();
        assertEquals(UiConfigurationActionBean.DISPLAY_CONFIGURATION_PAGE, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }

    @Test
    public void testDisplayOverallConfiguration() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.execute("displayOverallConfiguration");
        assertEquals(UiConfigurationActionBean.DISPLAY_CONFIGURATION_PAGE, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }

    @Test
    public void testDisplayMetadataFormatList() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.execute("displayMetadataFormatList");
        assertEquals(UiConfigurationActionBean.DISPLAY_COLLECTION_METADATA_FORMATS, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }

    @Test
    public void testDisplayEditDisciplineMapping() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.execute("displayEditDisciplineMapping");
        assertEquals(UiConfigurationActionBean.EDIT_DISCIPLINE_TO_COLLECTION_METADATA_FORMAT, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }

    /**
     * Test that validation logic exist to catch empty metadataFormat name submission
     */

    @Test
    public void testSubmitEmptyMetadataFormatName() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.setParameter("newMetadataFormatTransport.version", "1.0");
        rt.setParameter("newMetadataFormatTransport.schemaURL", "http://blah.blah");
        rt.setParameter("newMetadataFormatTransport.appliesToProject", "Yes");
        rt.setParameter("newMetadataFormatTransport.appliesToCollection", "Yes");
        rt.setParameter("newMetadataFormatTransport.appliesToItem", "Yes");
        rt.setParameter("newMetadataFormatTransport.validates", "Yes");
        rt.setParameter("newMetadataFormatTransport.disciplineIds[0]", "dc:discipline:SocialScience");
        rt.execute("addNewFormat");
        UiConfigurationActionBean bean = rt.getActionBean(UiConfigurationActionBean.class);
        Assert.assertEquals(1,bean.getContext().getValidationErrors().size());
        Assert.assertEquals(1,bean.getContext().getValidationErrors().size());
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.name"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.version"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.schemaURL"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.appliesToCollection"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.appliesToItem"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.validates"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.appliesToProject"));
    }

    /**
     * Test that validation logic exist to catch empty metadataFormat verion submission
     */
    @Test
    public void testSubmitEmptyMetadataFormatVersion() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.setParameter("newMetadataFormatTransport.name", "Format Name");
        rt.setParameter("newMetadataFormatTransport.schemaURL", "http://blah.blah");
        rt.setParameter("newMetadataFormatTransport.appliesToProject", "Yes");
        rt.setParameter("newMetadataFormatTransport.appliesToCollection", "Yes");
        rt.setParameter("newMetadataFormatTransport.appliesToItem", "Yes");
        rt.setParameter("newMetadataFormatTransport.validates", "Yes");
        rt.setParameter("newMetadataFormatTransport.disciplineIds[0]", "dc:discipline:SocialScience");

        rt.execute("addNewFormat");
        UiConfigurationActionBean bean = rt.getActionBean(UiConfigurationActionBean.class);
        Assert.assertEquals(1,bean.getContext().getValidationErrors().size());
        Assert.assertEquals(1,bean.getContext().getValidationErrors().size());
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.name"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.version"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.schemaURL"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.appliesToCollection"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.appliesToItem"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.validates"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.appliesToProject"));
    }
    /**
     * Test that validation logic exist to catch invalid metadataFormat schema url submission
     */
    @Test
    public void testSubmitInvalidMetadataFormatSchemaURL() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.setParameter("newMetadataFormatTransport.name", "Format Name");
        rt.setParameter("newMetadataFormatTransport.version", "Format Version");
        rt.setParameter("newMetadataFormatTransport.schemaURL", "Format URL");
        rt.setParameter("newMetadataFormatTransport.appliesToProject", "Yes");
        rt.setParameter("newMetadataFormatTransport.appliesToCollection", "Yes");
        rt.setParameter("newMetadataFormatTransport.appliesToItem", "Yes");
        rt.setParameter("newMetadataFormatTransport.validates", "Yes");
        rt.setParameter("newMetadataFormatTransport.disciplineIds[0]", "dc:discipline:SocialScience");

        rt.execute("addNewFormat");
        UiConfigurationActionBean bean = rt.getActionBean(UiConfigurationActionBean.class);
        Assert.assertEquals(1,bean.getContext().getValidationErrors().size());
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.name"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.version"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.schemaURL"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.appliesToCollection"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.appliesToItem"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.validates"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.appliesToProject"));
    }
    /**
     * Shows that invalid values for flags (appliesToCollection/project/item/validates) don't result in validation
     * errors.  The values are considered "valid" because any value other than "Yes" is considered to be a "No".
     */
    @Test
    public void testSubmitInvalidMetadataFormatProjectFlag() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.setParameter("newMetadataFormatTransport.name", "Format Name");
        rt.setParameter("newMetadataFormatTransport.version", "Format Version");
        rt.setParameter("newMetadataFormatTransport.schemaURL", "http://blah.blah");
        rt.setParameter("newMetadataFormatTransport.appliesToProject", "Invalid value");
        rt.setParameter("newMetadataFormatTransport.appliesToCollection", "Invalid value");
        rt.setParameter("newMetadataFormatTransport.appliesToItem", "Invalid value");
        rt.setParameter("newMetadataFormatTransport.validates", "Invalid value");
        rt.setParameter("newMetadataFormatTransport.disciplineIds[0]", "dc:discipline:SocialScience");

        rt.execute("addNewFormat");
        UiConfigurationActionBean bean = rt.getActionBean(UiConfigurationActionBean.class);
        Assert.assertEquals(0,bean.getContext().getValidationErrors().size());
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.name"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.version"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.schemaURL"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.appliesToCollection"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.appliesToItem"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.validates"));
        Assert.assertFalse(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.appliesToProject"));
    }

    /**
     * Insures that validation logic catches an empty request.  As a reminder, empty values for the flags
     * (appliesToCollection/project/item/validates) are considered valid now, they are just simply converted to "No".
     *
     * @throws Exception
     * @see #testSubmitInvalidMetadataFormatProjectFlag() 
     */
    @Test
    public void testSubmitEmptyRequest() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.execute("addNewFormat");
        UiConfigurationActionBean bean = rt.getActionBean(UiConfigurationActionBean.class);
        Assert.assertEquals(3,bean.getContext().getValidationErrors().size());
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.name"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.version"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("newMetadataFormatTransport.schemaURL"));
    }
    
    /**
     * Tests that adding a new format displays the correct information on the form.
     * @throws Exception
     */
    @Test
    public void testAddNewFormat() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.setParameter("newMetadataFormatTransport.name", "Format Name");
        rt.setParameter("newMetadataFormatTransport.version", "Format Version");
        rt.setParameter("newMetadataFormatTransport.schemaURL", "http://dataconservancy.org/schemas/bop/1.0");
        rt.setParameter("newMetadataFormatTransport.appliesToProject", "Yes");
        rt.setParameter("newMetadataFormatTransport.appliesToCollection", "Yes");
        rt.setParameter("newMetadataFormatTransport.appliesToItem", "Yes");
        rt.setParameter("newMetadataFormatTransport.validates", "Yes");
        rt.setParameter("newMetadataFormatTransport.disciplineIds[0]", "dc:discipline:SocialScience");

        rt.execute("addNewFormat");
        UiConfigurationActionBean bean = rt.getActionBean(UiConfigurationActionBean.class);
        Assert.assertEquals(0,bean.getContext().getValidationErrors().size());
        assertEquals(UiConfigurationActionBean.SCHEMA_VALIDATION_RESULT, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
        
        MetadataResult schemaResult = bean.getSchemaValidationResult();
        assertNotNull(schemaResult);
        
        assertEquals(0, schemaResult.getMetadataValidationErrors().size());
        assertEquals(2, schemaResult.getMetadataValidationSuccesses().size());
        
        DcsMetadataFormat format = bean.getMetadataFormat();
        assertNotNull(format);
        
        assertEquals("Format Name", format.getName());
        assertEquals("Format Version", format.getVersion());
        
        assertEquals(1, format.getSchemes().size());
        assertEquals("http://dataconservancy.org/schemas/bop/1.0", format.getSchemes().iterator().next().getSchemaUrl());
        
        MetadataFormatDescriptor descriptor = bean.getMetadataFormatDescription();
        assertEquals("Format Name", descriptor.getName());
        assertEquals("Format Version", format.getVersion());
        
        assertEquals(2, descriptor.getNamespaces().size());
        boolean foundPrefixedNamespace = false;
        boolean foundUnPrefixedNamespace = false;
        for (Namespace namespace : descriptor.getNamespaces()) {
            if (namespace.getPrefix().isEmpty()) {
                assertEquals("http://www.w3.org/2001/XMLSchema", namespace.getName());
                foundUnPrefixedNamespace = true;
            } else {
                assertEquals("bop", namespace.getPrefix());
                assertEquals("http://dataconservancy.org/schemas/bop/1.0", namespace.getName());
                foundPrefixedNamespace = true;
            }
        }
        
        assertTrue(foundPrefixedNamespace);
        assertTrue(foundUnPrefixedNamespace);
    }

    /**
     * TODO: Ignored because the website used to load the scheme from fgdc.gov is down/inaccessible causing test failure.
     * @throws Exception
     */
    @Ignore
    @Test
    public void testAddNewInvalidSchemaFile() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.setParameter("newMetadataFormatTransport.name", "Format Name");
        rt.setParameter("newMetadataFormatTransport.version", "Format Version");
        rt.setParameter("newMetadataFormatTransport.schemaURL", "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998-sect08.xsd");
        rt.setParameter("newMetadataFormatTransport.appliesToProject", "Yes");
        rt.setParameter("newMetadataFormatTransport.appliesToCollection", "Yes");
        rt.setParameter("newMetadataFormatTransport.appliesToItem", "Yes");
        rt.setParameter("newMetadataFormatTransport.validates", "Yes");
        rt.setParameter("newMetadataFormatTransport.disciplineIds[0]", "dc:discipline:SocialScience");

        rt.execute("addNewFormat");
        UiConfigurationActionBean bean = rt.getActionBean(UiConfigurationActionBean.class);
        Assert.assertEquals(0,bean.getContext().getValidationErrors().size());
        assertEquals(UiConfigurationActionBean.SCHEMA_VALIDATION_RESULT, rt.getForwardUrl());
        assertEquals(400, rt.getResponse().getStatus());
        
        MetadataResult schemaResult = bean.getSchemaValidationResult();
        assertNotNull(schemaResult);
        
        assertEquals(2, schemaResult.getMetadataValidationErrors().size());
        assertEquals(0, schemaResult.getMetadataValidationSuccesses().size());
        
        DcsMetadataFormat format = bean.getMetadataFormat();
        assertNotNull(format);
        
        assertEquals("Format Name", format.getName());
        assertEquals("Format Version", format.getVersion());
        
        assertEquals(1, format.getSchemes().size());
        assertEquals("http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998-sect08.xsd", format.getSchemes().iterator().next().getSchemaUrl());
    }
    
    @Test
    public void testAddNewUrlNonSchema() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));
        rt.setParameter("newMetadataFormatTransport.name", "Format Name");
        rt.setParameter("newMetadataFormatTransport.version", "Format Version");
        rt.setParameter("newMetadataFormatTransport.schemaURL", "http://www.dataconservancy.org");
        rt.setParameter("newMetadataFormatTransport.appliesToProject", "Yes");
        rt.setParameter("newMetadataFormatTransport.appliesToCollection", "Yes");
        rt.setParameter("newMetadataFormatTransport.appliesToItem", "Yes");
        rt.setParameter("newMetadataFormatTransport.validates", "Yes");
        rt.setParameter("newMetadataFormatTransport.disciplineIds[0]", "dc:discipline:SocialScience");

        rt.execute("addNewFormat");
        UiConfigurationActionBean bean = rt.getActionBean(UiConfigurationActionBean.class);
        Assert.assertEquals(0,bean.getContext().getValidationErrors().size());
        assertEquals(UiConfigurationActionBean.SCHEMA_VALIDATION_RESULT, rt.getForwardUrl());
        assertEquals(400, rt.getResponse().getStatus());
        
        MetadataResult schemaResult = bean.getSchemaValidationResult();
        assertNotNull(schemaResult);
        
        assertEquals(4, schemaResult.getMetadataValidationErrors().size());
        assertEquals(0, schemaResult.getMetadataValidationSuccesses().size());
        
        DcsMetadataFormat format = bean.getMetadataFormat();
        assertNotNull(format);
        
        assertEquals("Format Name", format.getName());
        assertEquals("Format Version", format.getVersion());
        
        assertEquals(1, format.getSchemes().size());
        assertEquals("http://www.dataconservancy.org", format.getSchemes().iterator().next().getSchemaUrl());
    }

    @DirtiesDatabase
    @Test
    public void testRemoveFormat() throws Exception {

        String xsdFormatId = "dataconservancy.org:formats:file:xsd:2004";
        String fgdcFormatId = "dataconservancy.org:formats:file:metadata:fgdc:xml";
        //verify assumption:
        assertNotNull(metadataFormatService.getMetadataFormat(fgdcFormatId));
        assertNotNull(metadataFormatService.getMetadataFormat(xsdFormatId));

        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, authenticateUser(admin));


        rt.setParameter("metadataFormatId", fgdcFormatId);
        rt.execute("removeFormat");

        UiConfigurationActionBean bean = rt.getActionBean(UiConfigurationActionBean.class);
        Assert.assertEquals(0,bean.getContext().getValidationErrors().size());

        MetadataFormatProperties props = metadataFormatService.getProperties(fgdcFormatId);
        assertFalse(props.isActive());

        Set<DcsMetadataFormat> expectedActiveMdfs = metadataFormatService.getMetadataFormats(true);
        //There assertion depends on the assumption that the METADATA_FORMAT_PROPERTIES table are only bootstrapped with
        //fgdc and xsd format as verified above
        assertEquals(1, expectedActiveMdfs.size());
        assertEquals(1, bean.getMetaDataFormatTransportList().size());
        assertEquals(xsdFormatId, bean.getMetaDataFormatTransportList().get(0).getId());

    }
}
