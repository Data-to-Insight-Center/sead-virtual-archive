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
package org.dataconservancy.archive.impl.fcrepo;

import java.io.InputStream;

import java.net.URL;

import junit.framework.Assert;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;

import org.dataconservancy.archive.api.AIPFormatException;
import org.junit.Before;
import org.junit.Test;

public class DcpAipIngesterTestIT extends AbstractFcrepoIntegrationTest {

    private static final String GOOD_AIP = "/exampleAIP.xml";

    private DcpAipIngester aipIngester;

    @Before
    public void setUp() {

        // Create the ingester class.
        aipIngester = new DcpAipIngester(fedoraClient);

    }

    @Test
    public void validAipTest() throws Exception {

        try {
            InputStream aip = this.getClass().getResourceAsStream(GOOD_AIP);
            aipIngester.ingestPackage(this.getClass()
                    .getResourceAsStream(GOOD_AIP));
        } catch (AIPFormatException e) {
        	Assert.fail("AIP formatting incorrect: " + e);	
        } catch (RuntimeException e) {
        	Assert.fail("Runtime barrier reached: " + e);
        } finally {
            // clean up for next test
            deleteAllEntities();
        }

    }

}
