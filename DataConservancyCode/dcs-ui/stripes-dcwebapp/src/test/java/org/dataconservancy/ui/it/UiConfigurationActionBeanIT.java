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
package org.dataconservancy.ui.it;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.ui.dao.DisciplineDAO;
import org.dataconservancy.ui.it.support.*;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.stripes.UiConfigurationActionBean;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @see ValidatingMetadataFileIT
 */
public class UiConfigurationActionBeanIT extends BaseIT {

    private static final String FGDC_1998_SCHEMA_URL = "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd";

    private static final String FGDC_1998_SEC_08_SCHEMA_URL =
            "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998-sect08.xsd";

    private static final String MAVEN_MODEL_4_0_0_SCHEMA_URL = "http://maven.apache.org/xsd/maven-4.0.0.xsd";

    private HttpClient hc = new DefaultHttpClient();

    @Autowired
    @Qualifier("defaultAdmin")
    private Person adminUser;

    @Autowired
    private DisciplineDAO disciplineDao;

    @Before
    public void loginAdmin() {
        // Login as admin
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(adminUser)
                .asHttpPost(), 302);
    }

    @After
    public void logoutAdmin() {
        HttpAssert.assertStatus(hc, reqFactory.createLogoutRequest().asHttpGet(), 200);
    }

    /**
     * Insures that an XSD schema, composed of multiple schema documents (via &lt;xsd:include>), can be
     * added to the system.
     *
     * @throws Exception
     */
    //TODO: unignore when fgdc site comes back or different url can be used to load schema
    @Ignore
    @Test
    public void testAddFgdcMetadataFormat() throws Exception {
        List<UiConfigurationActionBean.MetaDataFormatTransport> mdfs = getMdfs();
        int currentMdfCount = mdfs.size();

        final String name = UUID.randomUUID().toString();  // A unique ID
        final String version = this.getClass().getSimpleName() + " FGDC 1998";
        final Boolean appliesToCollection = Boolean.TRUE;
        final Boolean appliesToProject = Boolean.FALSE;
        final Boolean appliesToItem = Boolean.TRUE;
        final Boolean validates = Boolean.TRUE;
        
        final UiConfigurationActionBean.MetaDataFormatTransport mdft = new UiConfigurationActionBean()
                .getNewMetadataFormatTransport();

        AddMetadataFormatRequest request = new AddMetadataFormatRequest(urlConfig);
        mdft.setName(name);
        mdft.setVersion(version);
        mdft.setSchemaURL(FGDC_1998_SCHEMA_URL);
        mdft.setSchemaSource(FGDC_1998_SCHEMA_URL);
        mdft.setValidates(validates);
        mdft.setAppliesToCollection(appliesToCollection);
        mdft.setAppliesToProject(appliesToProject);
        mdft.setAppliesToItem(appliesToItem);
        mdft.setDisciplineIds(Arrays.asList("dc:discipline:Biology"));

        // Insure that the current list of Metadata Formats doesn't include the format we're about to add
        assertFalse(mdfs.contains(mdft));

        HttpAssert.assertStatus(hc, request.asHttpPost(mdft), 200);
        // Now we need to persist the format in the system by emulating a click on the "save" button
        HttpAssert.assertStatus(hc, new SaveMetadataFormatRequest(urlConfig).asHttpPost(), 200);

        // Insure that the list of Metadata Formats contains the format we just added
        mdfs = getMdfs();
        assertTrue(mdfs.contains(mdft));
        assertEquals(currentMdfCount + 1, mdfs.size());
    }

    /**
     * Insures that an XSD schema, composed of a single schema document (no &lt;xsd:include> statements), can be
     * added to the system.
     *
     * @throws Exception
     */
    @Test
    public void testAddMavenPomMetadataFormat() throws Exception {
        // Get a count of the current number of metadata formats in the system
        List<UiConfigurationActionBean.MetaDataFormatTransport> mdfts = getMdfs();
        int mdfCount = mdfts.size();

        // Compose the mdft to add
        final UiConfigurationActionBean.MetaDataFormatTransport mdft = new UiConfigurationActionBean().getNewMetadataFormatTransport();
        final AddMetadataFormatRequest req = new AddMetadataFormatRequest(urlConfig);
        // A unique name insures that this Metadata Format doesn't exist yet in the system (but we verify this
        // assumption anyway)
        final String name = UUID.randomUUID().toString();
        final boolean validates = true;
        final String version = this.getClass().getSimpleName() + " Maven 4.0.0 POM";
        final boolean appliesToCollection = false;
        final boolean appliesToProject = false;
        final boolean appliesToItem = true;
        final List<String> disciplineIds = Arrays.asList("dc:discipline:Biology");
        mdft.setName(name);
        mdft.setVersion(version);
        mdft.setSchemaURL(MAVEN_MODEL_4_0_0_SCHEMA_URL);
        mdft.setSchemaSource(MAVEN_MODEL_4_0_0_SCHEMA_URL);
        mdft.setValidates(validates);
        mdft.setAppliesToCollection(appliesToCollection);
        mdft.setAppliesToProject(appliesToProject);
        mdft.setAppliesToItem(appliesToItem);
        mdft.setDisciplineIds(disciplineIds);

        // Insure that the new Metadata Format being added isn't in the list of existing metadata formats
        assertFalse(mdfts.contains(mdft));

        // Add the metadata format
        HttpAssert.ResponseHolder holder = new HttpAssert.ResponseHolder();

        HttpAssert.assertStatus(hc, req.asHttpPost(mdft), 200, holder);
        
        final String html = IOUtils.toString(holder.getBody());
        assertNotNull(html);
        final Document dom = Jsoup.parse(html);
        assertNotNull(dom);
        Element nameElement = dom.getElementById("schemaName");
        assertNotNull(nameElement);
        String testText = nameElement.text();
        assertTrue(nameElement.text().equalsIgnoreCase("Schema Name: " + name));
        
        Element versionElement = dom.getElementById("schemaVersion");
        assertNotNull(versionElement);
        assertTrue(versionElement.text().equalsIgnoreCase("Version: " + version));
    
        Element namespacesElement = dom.getElementById("namespaces");
        //assertEquals(2, namespacesElement.childNodeSize());
       
        Elements namespaceElements = namespacesElement.children();
        boolean foundPrefixedNamespace = false;
        boolean foundNamespace = false;
        for (Element namespaceElement : namespaceElements) {
            String namespaceText = namespaceElement.text();
            if (namespaceText.contains("Namespace:")) {
                if (namespaceText.contains("Prefix")) {
                    assertTrue(namespaceText.equalsIgnoreCase("Namespace: http://www.w3.org/2001/XMLSchema Prefix: xs"));
                    foundPrefixedNamespace = true;
                } else {
                    assertTrue(namespaceText.equalsIgnoreCase("Namespace: http://maven.apache.org/POM/4.0.0"));
                    foundNamespace = true;
                }
            }
        }
        
        assertTrue(foundPrefixedNamespace);
        assertTrue(foundNamespace);
        
        // Now we need to persist the format in the system by emulating a click on the "save" button
        HttpAssert.assertStatus(hc, new SaveMetadataFormatRequest(urlConfig).asHttpPost(), 200);

        // insure that the format we've added was added properly (all the values for table columns were
        // persisted properly)
        mdfts = getMdfs();
        assertTrue(mdfts.contains(mdft));
        assertEquals(mdfCount + 1, mdfts.size());
    }

    /**
     * Insures that adding a schema that points to a non-existent url fails.
     *
     * @throws Exception
     */
    @Test
    public void testAddSchemaWith404Url() throws Exception {

        // Be assured the URL doesn't exist (but the host does)
        final String nonExistentUrl = "http://www.google.com/hubba";
        HttpAssert.assertStatus(hc, nonExistentUrl, 404);

        // Get a count of the current number of metadata formats in the system
        List<UiConfigurationActionBean.MetaDataFormatTransport> mdfts = getMdfs();
        int mdfCount = mdfts.size();

        // Compose the mdft to add
        final UiConfigurationActionBean.MetaDataFormatTransport mdft = new UiConfigurationActionBean().
                getNewMetadataFormatTransport();
        final AddMetadataFormatRequest req = new AddMetadataFormatRequest(urlConfig);
        // A unique name insures that this Metadata Format doesn't exist yet in the system (but we verify this
        // assumption anyway)
        final String name = UUID.randomUUID().toString();
        final boolean validates = true;
        final String version = this.getClass().getSimpleName() + " Maven 4.0.0 POM";
        final boolean appliesToCollection = false;
        final boolean appliesToProject = false;
        final boolean appliesToItem = true;
        final List<String> disciplineIds = Arrays.asList("dc:discipline:Biology");
        mdft.setName(name);
        mdft.setVersion(version);
        mdft.setSchemaURL(nonExistentUrl);
        mdft.setSchemaSource(nonExistentUrl);
        mdft.setValidates(validates);
        mdft.setAppliesToCollection(appliesToCollection);
        mdft.setAppliesToProject(appliesToProject);
        mdft.setAppliesToItem(appliesToItem);
        mdft.setDisciplineIds(disciplineIds);

        // Insure that the new Metadata Format being added isn't in the list of existing metadata formats
        assertFalse(mdfts.contains(mdft));

        // Add the metadata format
        HttpAssert.assertStatus(hc, req.asHttpPost(mdft), 400);

        // insure that the format wasn't added
        mdfts = getMdfs();
        assertFalse(mdfts.contains(mdft));
        assertEquals(mdfCount, mdfts.size());
    }

    /**
     * Insures that adding a schema that fails XSD validation cannot be added to the system
     *
     * @throws Exception
     */
    @Test
    public void testAddInvalidXsdSchema() throws Exception {

        // Get a count of the current number of metadata formats in the system
        List<UiConfigurationActionBean.MetaDataFormatTransport> mdfts = getMdfs();
        int mdfCount = mdfts.size();

        // Compose the mdft to add
        final UiConfigurationActionBean.MetaDataFormatTransport mdft = new UiConfigurationActionBean().
                getNewMetadataFormatTransport();
        final AddMetadataFormatRequest req = new AddMetadataFormatRequest(urlConfig);
        // A unique name insures that this Metadata Format doesn't exist yet in the system (but we verify this
        // assumption anyway)
        final String name = UUID.randomUUID().toString();
        final boolean validates = true;
        final String version = this.getClass().getSimpleName() + " Maven 4.0.0 POM";
        final boolean appliesToCollection = false;
        final boolean appliesToProject = false;
        final boolean appliesToItem = true;
        final List<String> disciplineIds = Arrays.asList("dc:discipline:Biology");
        mdft.setName(name);
        mdft.setVersion(version);
        mdft.setSchemaURL(FGDC_1998_SEC_08_SCHEMA_URL);
        mdft.setSchemaSource(FGDC_1998_SEC_08_SCHEMA_URL);
        mdft.setValidates(validates);
        mdft.setAppliesToCollection(appliesToCollection);
        mdft.setAppliesToProject(appliesToProject);
        mdft.setAppliesToItem(appliesToItem);
        mdft.setDisciplineIds(disciplineIds);

        // Insure that the new Metadata Format being added isn't in the list of existing metadata formats
        assertFalse(mdfts.contains(mdft));

        // Add the metadata format
        HttpAssert.assertStatus(hc, req.asHttpPost(mdft), 400);

        // insure that the format wasn't added
        mdfts = getMdfs();
        assertFalse(mdfts.contains(mdft));
        assertEquals(mdfCount, mdfts.size());
    }

    /**
     * Test the removal of MetadataFormat from the user interface. When user sends request to remove a metadata format,
     * that metadata format no longer appears on the page which lists available metadata formats
     */
    //TODO: unignore when fgdc site comes back or different url can be used to load schema
    @Ignore
    @Test
    public void testRemoveMetadataFormat() {
        //Added a MetadataFormat to run the remove test one
        List<UiConfigurationActionBean.MetaDataFormatTransport> mdfs = getMdfs();
        int initialMdfCount = mdfs.size();

        final String name = UUID.randomUUID().toString();  // A unique ID
        final String version = this.getClass().getSimpleName();
        final Boolean appliesToCollection = Boolean.TRUE;
        final Boolean appliesToProject = Boolean.FALSE;
        final Boolean appliesToItem = Boolean.TRUE;
        final Boolean validates = Boolean.TRUE;

        final UiConfigurationActionBean.MetaDataFormatTransport mdft = new UiConfigurationActionBean()
                .getNewMetadataFormatTransport();

        AddMetadataFormatRequest request = new AddMetadataFormatRequest(urlConfig);
        mdft.setName(name);
        mdft.setVersion(version);
        mdft.setSchemaURL(FGDC_1998_SCHEMA_URL);
        mdft.setSchemaSource(FGDC_1998_SCHEMA_URL);
        mdft.setValidates(validates);
        mdft.setAppliesToCollection(appliesToCollection);
        mdft.setAppliesToProject(appliesToProject);
        mdft.setAppliesToItem(appliesToItem);
        mdft.setDisciplineIds(Arrays.asList("dc:discipline:Biology"));

        HttpAssert.assertStatus(hc, request.asHttpPost(mdft), 200);

        // Now we need to persist the format in the system by emulating a click on the "save" button
        HttpAssert.assertStatus(hc, new SaveMetadataFormatRequest(urlConfig).asHttpPost(), 200);

        // Insure that the list of Metadata Formats contains the newly added format, so that it can be removed.
        mdfs = getMdfs();
        assertTrue(mdfs.contains(mdft));
        assertEquals(initialMdfCount + 1, mdfs.size());

        // Get a count of the current number of metadata formats in the system
        int intermediateMdfCount = mdfs.size();

        //forming the id of format to be removed, based on what is known about the format when it was created
        String idToRemove = "dataconservancy.org:registry:metadata-format:entry:id:" + name + ":" + version;

        //forming remove request
        RemoveMetadataFormatRequest removeRequest = new RemoveMetadataFormatRequest(urlConfig);

        //Remove metadata format
        HttpAssert.assertStatus(hc, removeRequest.asHttpPost(idToRemove), 200)
        ;
        // Get a count of the current number of metadata formats in the system
        mdfs = getMdfs();
        int newMdfCount = mdfs.size();
        //Test that the count of metadata format displayed has decreased by 1
        assertEquals(intermediateMdfCount - 1, newMdfCount);
        //Test that the list of displayed metadata formats does not contained the removed formats
        assertFalse(mdfs.contains(mdft));
    }

    /**
     * Returns the current number of Metadata Formats that are registered in the system.
     *
     * @return
     */
    private int getCurrentMdfCount() {
        // Get a count of the current number of metadata formats in the system
        List<UiConfigurationActionBean.MetaDataFormatTransport> formats =
                new ListMetadataFormatRequest(urlConfig, disciplineDao).listFormats(hc);
        assertNotNull(formats);
        return formats.size();
    }

    /**
     * Returns the current Metadata Formats that are registered in the system.
     * @return
     */
    private List<UiConfigurationActionBean.MetaDataFormatTransport> getMdfs() {
        return new ListMetadataFormatRequest(urlConfig, disciplineDao).listFormats(hc);
    }

}
