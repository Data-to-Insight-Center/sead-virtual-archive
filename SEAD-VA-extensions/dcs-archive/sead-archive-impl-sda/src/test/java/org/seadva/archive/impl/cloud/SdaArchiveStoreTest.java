/*
 * Copyright 2014 The Trustees of Indiana University
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

package org.seadva.archive.impl.cloud;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.dataconservancy.archive.api.AIPFormatException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * Test cases for SEAD Sip deposit to SDA
 */
public class SdaArchiveStoreTest {

    static SdaArchiveStore sdaArchiveStore = null;
    private final static Properties props = new Properties();

    @BeforeClass
    public static void init() throws IOException{
        final URL defaultProps = SdaArchiveStoreTest.class.getResource("/default.properties");
        Assert.assertNotNull("Could not resolve /default.properties from the classpath.", defaultProps);
        assertTrue("default.properties does not exist.", new File(defaultProps.getPath()).exists());
        props.load(defaultProps.openStream());
        try {
            sdaArchiveStore = new SdaArchiveStore((String)props.get("sda.host"), (String)props.get("sda.user"), (String)props.get("sda.password"), (String)props.get("sda.mount"));
        } catch (JSchException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testSipUpload() throws JSchException, SftpException, AIPFormatException {
        //upload
        sdaArchiveStore.putResearchPackage(SdaArchiveStoreTest.class.getResourceAsStream("/sampleSip.xml") );
        assertTrue("SFTP upload and download did not correctly execute.", new File(System.getProperty("java.io.tmpdir") +"/").exists());
    }
}
