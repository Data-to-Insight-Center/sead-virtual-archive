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
package org.dataconservancy.ui.it.support;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.test.support.BaseSpringAwareTest;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Abstract base IT.  Currently provides {@code protected} access to commonly used IT support classes.  It
 * loads the following Spring Application Contexts in order, from the classpath:
 * <ol>
 *     <li>{@code /test-applicationContext.xml}</li>
 *     <li>{@code /applicationContext.xml}</li>
 * </ol>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:/org/dataconservancy/ui/config/test-applicationContext.xml",
                       "classpath:/test-applicationContext.xml",
                       "classpath*:/org/dataconservancy/ui/config/applicationContext.xml",
                       "classpath*:/org/dataconservancy/config/applicationContext.xml",
                       "classpath*:/org/dataconservancy/mhf/config/applicationContext.xml",
                       "classpath*:/org/dataconservancy/registry/config/applicationContext.xml",
                       "classpath*:/org/dataconservancy/model/config/applicationContext.xml",
                       "classpath*:/org/dataconservancy/access/config/applicationContext.xml",
                       "classpath*:/org/dataconservancy/packaging/config/applicationContext.xml"})
public abstract class BaseIT extends BaseSpringAwareTest {

    @Autowired
    protected UiUrlConfig urlConfig;

    @Autowired
    @Qualifier("dcsConnector")
    protected DcsConnector connector;

    @Autowired
    protected RequestFactory reqFactory;

    @Autowired
    protected ArchiveSupport archiveSupport;

    @Autowired
    @Qualifier("uiIdService")
    protected IdService idService;

    /**
    * Util method creating a small file for deposit.  For the purposes of this test, the content of the file
    * does not matter.  This file can be re-used for all of the tests.
    *
    * @return the file to deposit
    */
    protected static File createSampleDataFile(String namePrefix, String fileNameExtenstion) {
        File sampleDataFile = null;
        if(namePrefix == null){
            namePrefix = "";
        }
        if(fileNameExtenstion == null){
            fileNameExtenstion = "";
        }
        try {
            sampleDataFile = java.io.File.createTempFile(namePrefix, fileNameExtenstion);
            FileOutputStream tmpFileOut = new FileOutputStream(sampleDataFile);
            Random random = new Random();
            final int lineCount = 10;
            for (int i = 0; i < lineCount; i++) {
                IOUtils.write("This is a line of content.", tmpFileOut);
            }
            tmpFileOut.close();
            return  sampleDataFile;
        } catch (IOException e) {
            throw new RuntimeException("Error creating test Data File: " + e.getMessage(), e);
        }
    }

     protected void freeResponse(HttpResponse response) {
        HttpEntity entity;
        if ((entity = response.getEntity()) != null) {
            try {
                final InputStream entityBody = entity.getContent();

                entityBody.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
