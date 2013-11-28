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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.apache.commons.io.IOUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.resources.MHFTestResources;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import junit.framework.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class ValidatingMetadataFileActionBeanTest extends BaseActionBeanTest {
    
    private static final String fgdcXmlMetadataFilePath = MHFTestResources.SAMPLE_VALID_FGDC_XML_RESOURCE_PATH;

    private static final String invalidFgdcXmlMetadataFilePath = MHFTestResources.SAMPLE_INVALID_FGDC_XML_RESOURCE_PATH;

    private static final String validBopFilePath = "/SampleXML/project-testbop.xml";
    
    private static final String invalidBopFilePath = "/SampleXML/invalid-bop.xml";

    /**
     * Tests validating an valid file against a format that is in the archive.
     * This will test against a validation performed in the MHF.
     * @throws Exception 
     */
    @Test
    public void testValidateValidTestFileAgainstRegistryFormat() throws Exception {
        File sampleMetadataFile = new File(this.getClass().getResource(fgdcXmlMetadataFilePath).getPath());
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ValidatingMetadataFileActionBean.class, authenticateUser(admin));
        rt.setParameter("metadataFormatId", MetadataFormatId.FGDC_XML_FORMAT_ID);
        rt.setParameter("sampleMetadataFile", sampleMetadataFile.getPath());
        rt.execute("validate");
        
        assertEquals(ValidatingMetadataFileActionBean.VALIDATING_METADATA_FILE_PATH + "?metadataFormatName=", rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }
    
    /**
     * Tests validating an invalid file against a format that is in the archive.
     * This will test against a validation performed in the MHF.
     * @throws Exception 
     */
    @Test
    public void testValidateInvalidTestFileAgainstRegistryFormat() throws Exception {
        File sampleMetadataFile = new File(this.getClass().getResource(invalidFgdcXmlMetadataFilePath).getPath());
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ValidatingMetadataFileActionBean.class, authenticateUser(admin));
        rt.setParameter("metadataFormatId", MetadataFormatId.FGDC_XML_FORMAT_ID);
        rt.setParameter("sampleMetadataFile", sampleMetadataFile.getPath());
        rt.execute("validate");
        
        assertEquals(ValidatingMetadataFileActionBean.VALIDATING_METADATA_FILE_PATH + "?metadataFormatName=", rt.getForwardUrl());
        assertEquals(400, rt.getResponse().getStatus());
    }
    
    /**
     * Tests validating an valid file against a format that is not yet in the archive.
     * This will test against a validation performed only in the UI.
     * @throws Exception 
     */
    @Test
    public void testValidateValidTestFileAgainstTempFormat() throws Exception {
        File sampleMetadataFile = new File(this.getClass().getResource(validBopFilePath).getPath());

        MockHttpSession adminSession = authenticateUser(admin);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, adminSession);
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
        
        rt = new MockRoundtrip(servletCtx, ValidatingMetadataFileActionBean.class, adminSession);
        rt.setParameter("metadataFormatId", "dataconservancy.org:formats:file:metadata:bop:xml");
        rt.setParameter("sampleMetadataFile", sampleMetadataFile.getPath());
        rt.execute("validate");
        
        assertEquals(ValidatingMetadataFileActionBean.VALIDATING_METADATA_FILE_PATH + "?metadataFormatName=", rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }
    
    /**
     * Tests validating an invalid file against a format that is not yet in the archive.
     * This will test against a validation performed only in the UI.
     * @throws Exception 
     */
    @Test
    public void testValidateInvalidTestFileAgainstTempFormat() throws Exception {
        File sampleMetadataFile = new File(this.getClass().getResource(invalidBopFilePath).getPath());
 
        MockHttpSession adminSession = authenticateUser(admin);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UiConfigurationActionBean.class, adminSession);
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
        
        rt = new MockRoundtrip(servletCtx, ValidatingMetadataFileActionBean.class, adminSession);
        rt.setParameter("metadataFormatId", "dataconservancy.org:formats:file:metadata:bop:xml");
        rt.setParameter("sampleMetadataFile", sampleMetadataFile.getPath());
        rt.execute("validate");
        
        assertEquals(ValidatingMetadataFileActionBean.VALIDATING_METADATA_FILE_PATH + "?metadataFormatName=", rt.getForwardUrl());
        assertEquals(400, rt.getResponse().getStatus());
    }
    
    @Test
    public void testDone() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ValidatingMetadataFileActionBean.class, authenticateUser(admin));
        rt.setParameter("redirectUrl", UiConfigurationActionBean.DISPLAY_COLLECTION_METADATA_FORMATS);
        rt.execute("done");
        
        assertEquals("/admin/uiconfig.action?%2Fpages%2Fadmindisplaycollectionmetadataformats.jsp=", rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }
}
