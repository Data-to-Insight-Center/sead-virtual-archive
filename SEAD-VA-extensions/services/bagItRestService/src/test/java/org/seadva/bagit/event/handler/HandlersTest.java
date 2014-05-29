/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seadva.bagit.event.handler;

import com.sun.jersey.test.framework.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Event;
import org.seadva.bagit.impl.ConfigBootstrap;
import org.seadva.bagit.model.MediciInstance;
import org.seadva.bagit.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Handler test cases
 */
public class HandlersTest extends JerseyTest {

    private static final Logger log =
            LoggerFactory.getLogger(HandlersTest.class);

    ConfigBootstrap configBootstrap;

    public HandlersTest() throws Exception {
        super("org.seadva.bagit.event.impl");
        Constants.homeDir = "./";
        Constants.bagDir = Constants.homeDir+"bag/";
        Constants.unzipDir = Constants.homeDir+"bag/"+"untar/";
        if(!new File(Constants.bagDir).exists()) {
            new File(Constants.bagDir).mkdirs();
        }
        if(!new File(Constants.unzipDir).exists()) {
            new File(Constants.unzipDir).mkdirs();
        }

    }
    @Before
    public void init(){
        configBootstrap = new ConfigBootstrap();
        configBootstrap.load();
    }

    @Test
    public void testUnzipHandler() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PackageDescriptor packageDescriptor = new PackageDescriptor("sample_bag.zip",getClass().getResource("../../sample_bag.zip").getPath(),null);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.UNZIP_BAG, packageDescriptor);
        assertNotNull(packageDescriptor.getUnzippedBagPath());
    }

    @Test
    public void testZipHandler() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PackageDescriptor packageDescriptor = new PackageDescriptor("sample_bag.zip", null, getClass().getResource("../../sample_bag").getPath());
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.ZIP_BAG, packageDescriptor);
        assertNotNull(packageDescriptor.getBagPath());
    }

    @Test
    /* Output goes into target/test-classes/org/seadva/bagit/ */
    public void testUntarHandler() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PackageDescriptor packageDescriptor = new PackageDescriptor("sample_bag1.tar",getClass().getResource("../../sample_bag1.tar").getPath(),null);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.UNTAR_BAG, packageDescriptor);
        assertNotNull(packageDescriptor.getUntarredBagPath());
    }

    @Test
    /* Input directory file should be placed under test/resources and output will go to bag at the top level */
    public void testTarHandler() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PackageDescriptor packageDescriptor = new PackageDescriptor("sample_bag.tar", null, getClass().getResource("../../sample_bag").getPath());
        packageDescriptor.setUntarredBagPath(getClass().getResource("../../sample_bag").getPath());
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.TAR_BAG, packageDescriptor);
        assertNotNull(packageDescriptor.getBagPath());
    }

    @Test
    public void testDirectoryParseHandler() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PackageDescriptor packageDescriptor = new PackageDescriptor("sample_bag.zip", null, getClass().getResource("../../sample_bag").getPath());
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_DIRECTORY, packageDescriptor);
        assertEquals(packageDescriptor.getProperties().size(), (new File(getClass().getResource("../../sample_bag"+"/data").getFile())).listFiles().length+1);
    }

    @Test
    public void testAcrQueryHandler() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PackageDescriptor packageDescriptor = new PackageDescriptor(null, null, null);
        packageDescriptor.setPackageId("tag:cet.ncsa.uiuc.edu,2008:/bean/Collection/E012014D-7379-4556-8A87-6AD262965C89");
        int sparqlEndpointNum = 3;
        MediciInstance instance = null;
        for(MediciInstance t_instance: Constants.acrInstances)
            if(t_instance.getId()==sparqlEndpointNum)
                instance = t_instance;
        packageDescriptor.setMediciInstance(instance);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_ACR_COLLECTION, packageDescriptor);
        assertTrue(packageDescriptor.getProperties().size()>0);
    }

    @Test
    public void testManifestGenerationHandler() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PackageDescriptor packageDescriptor = new PackageDescriptor("sample_bag.zip", null, getClass().getResource("../../sample_bag").getPath());
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_DIRECTORY, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_MANIFEST, packageDescriptor);
        assertNotNull(packageDescriptor.getManifestFilePath());
    }

    @Test
    public void testFetchGenerationHandler() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PackageDescriptor packageDescriptor = new PackageDescriptor("sample_bag.zip", null, getClass().getResource("../../sample_bag").getPath());
        packageDescriptor.setPackageId("tag:cet.ncsa.uiuc.edu,2008:/bean/Collection/E012014D-7379-4556-8A87-6AD262965C89");
        int sparqlEndpointNum = 3;
        MediciInstance instance = null;
        for(MediciInstance t_instance: Constants.acrInstances)
            if(t_instance.getId()==sparqlEndpointNum)
                instance = t_instance;
        packageDescriptor.setMediciInstance(instance);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_ACR_COLLECTION, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_FETCH, packageDescriptor);
        assertNotNull(packageDescriptor.getFetchFilePath());
    }


    @Test
    public void testOreGenerationHandlerForDir() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PackageDescriptor packageDescriptor = new PackageDescriptor("sample_bag.zip", null, getClass().getResource("../../sample_bag").getPath());
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_DIRECTORY, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_ORE, packageDescriptor);
        assertEquals(packageDescriptor.getProperties().size(), (new File(getClass().getResource("../../sample_bag"+"/data").getFile())).listFiles().length+1);
    }

    @Test
    public void testOreGenerationHandlerForFetch() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PackageDescriptor packageDescriptor = new PackageDescriptor("sample_bag.zip", null, getClass().getResource("../../sample_bag").getPath());
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_FETCH, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_ORE, packageDescriptor);
        assertTrue(packageDescriptor.getProperties().size()>0);
    }

    @Test
    public void testOreGenerationHandlerForAcr() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PackageDescriptor packageDescriptor = new PackageDescriptor("sample_bag.zip", null, getClass().getResource("../../sample_bag").getPath());
        packageDescriptor.setPackageId(
                "tag:cet.ncsa.uiuc.edu,2008:/bean/Collection/E012014D-7379-4556-8A87-6AD262965C89"
        );
        int sparqlEndpointNum = 3;
        MediciInstance instance = null;
        for(MediciInstance t_instance: Constants.acrInstances)
            if(t_instance.getId()==sparqlEndpointNum)
                instance = t_instance;
        packageDescriptor.setMediciInstance(instance);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_ACR_COLLECTION, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_ORE, packageDescriptor);
        assertTrue(packageDescriptor.getProperties().size()>0);
    }

    @Test
    public void testSipGenerationHandler() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PackageDescriptor packageDescriptor = new PackageDescriptor("sample_bag.zip", null, getClass().getResource("../../sample_bag_ore").getPath());
        packageDescriptor.setPackageId("d8d2b139-433a-4dde-b579-63d61a0d9916");
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_SIP, packageDescriptor);
        System.out.println("SIP file path:" + packageDescriptor.getSipPath());
        assertNotNull(packageDescriptor.getSipPath());
    }


}
