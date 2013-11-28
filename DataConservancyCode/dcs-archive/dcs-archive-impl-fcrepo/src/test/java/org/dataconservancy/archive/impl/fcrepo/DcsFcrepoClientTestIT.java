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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.MalformedURLException;
import java.net.URL;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import com.yourmediashelf.fedora.client.request.FindObjects;
import com.yourmediashelf.fedora.client.response.FedoraResponse;

import org.dataconservancy.archive.api.AIPFormatException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DcsFcrepoClientTestIT extends AbstractFcrepoIntegrationTest {

    @Test
    public void testFcrepoClient() {

        // This test requires that test objects already loaded
        // So we use the system objects.

        FindObjects query = new FindObjects();
        query.query("pid=fedora-system:FedoraObject-3.0");
        query.pid(true);
        query.resultFormat("xml");

        //String query =
        //    "http://localhost:8080/fedora/objects" +
        //    "?query=pid~fedora-system:FedoraObject-3.0" +
        //    "&pid=true&resultFormat=xml";

        try {

            FedoraResponse dsResponse = query.execute(fedoraClient);
            InputStream in = dsResponse.getEntityInputStream();
            String resultAsXML = convertStreamToString(in);
            boolean found =
                    resultAsXML
                            .contains("<pid>fedora-system:FedoraObject-3.0</pid>");
            Assert.assertTrue(found);

        } catch (IOException e) {
        	Assert.fail("Failed to convert XML stream - Should not happen");
        } catch (FedoraClientException e) {
        	Assert.fail("Repository client query failed: " + e);
        } catch (RuntimeException e) {
            Assert.fail("Runtime barrier reached: " + e);
        }

    }

    public String convertStreamToString(InputStream is) throws IOException {
        /*
         * To convert the InputStream to String we use the
         * BufferedReader.readLine() method. We iterate until the BufferedReader
         * return null which means there's no more data to read. Each line will
         * appended to a StringBuilder and returned as String.
         */
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }

}
