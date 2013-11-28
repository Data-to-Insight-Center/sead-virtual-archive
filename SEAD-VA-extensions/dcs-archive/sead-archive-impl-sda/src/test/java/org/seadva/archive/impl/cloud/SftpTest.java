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

package org.seadva.archive.impl.cloud;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seadva.model.SeadRepository;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for SDA deposit
 */
public class SftpTest{

    static Sftp sftp = null;
    private final static Properties props = new Properties();

    @BeforeClass
    public static void init() throws IOException{
        final URL defaultProps = SftpTest.class.getResource("/default.properties");
        Assert.assertNotNull("Could not resolve /default.properties from the classpath.", defaultProps);
        assertTrue("default.properties does not exist.", new File(defaultProps.getPath()).exists());
        props.load(defaultProps.openStream());
        try {
            sftp = new Sftp((String)props.get("sda.host"), (String)props.get("sda.user"), (String)props.get("sda.password"), (String)props.get("sda.mount"));
        } catch (JSchException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testTransferFile() throws JSchException, SftpException {
        //upload
        String fileName = "testfile.txt";
        sftp.uploadFile(SftpTest.class.getResource("/"+fileName).getPath(),fileName,false);
        //download
        sftp.downloadFile(".",fileName,System.getProperty("java.io.tmpdir") +"/");
        assertTrue("SFTP upload and download did not correctly execute.", new File(System.getProperty("java.io.tmpdir") +"/").exists());
    }

    @Test
    public void testCreateDirectoryFile(){
        //create dir
        sftp.createDirectory("test_sead");
    }
}
