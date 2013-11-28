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

package org.dataconservancy.archive.impl.cloud;

import org.dataconservancy.archive.api.AIPFormatException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class AmazonS3ArchiveStoreTest {
    private final static Properties props = new Properties();

    @BeforeClass
    public static void init() throws IOException {
        final URL defaultProps = AmazonS3ArchiveStoreTest.class.getResource("/default.properties");
        Assert.assertNotNull("Could not resolve /default.properties from the classpath.", defaultProps);
        assertTrue("default.properties does not exist.", new File(defaultProps.getPath()).exists());
        props.load(defaultProps.openStream());
    }
        String EXAMPLE_FILE ="../resources/ManyRelationships.xml";
    @Test
    public void testPackageCreation() throws AIPFormatException, FileNotFoundException {

        InputStream dcp = new FileInputStream(new File(EXAMPLE_FILE));
        AmazonS3ArchiveStore archiveStore = null;
        try {
            archiveStore = new AmazonS3ArchiveStore((String)props.get("s3.acccessKey"),(String)props.get("s3.secretKey"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        archiveStore.putPackage(dcp);
    }

}
