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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.tika.io.IOUtils;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.IngestPackageRequest;
import org.dataconservancy.ui.model.Person;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

/**
 *
 */
public class IngestPackageActionBeanIT extends BaseIT {

    private HttpClient hc = new DefaultHttpClient();

    @Autowired
    @Qualifier("defaultAdmin")
    private Person adminUser;

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

    @Test
    public void testSimple() throws Exception {
        File foo = new File(DepositZipFileIT.class.getResource("/SampleFilePackages/testResources.tar").toURI());

        IngestPackageRequest req = new IngestPackageRequest(urlConfig);
        req.setPackageToIngest(foo);

        HttpAssert.ResponseHolder holder = new HttpAssert.ResponseHolder();

        HttpAssert.assertStatus(hc, req.asHttpPost("application/tar"), 302, holder);
        HttpAssert.assertStatus(hc, new HttpGet(holder.getLocationHeader()), 200, holder);

        final String html = IOUtils.toString(holder.getBody());
        assertNotNull(html);
        final Document dom = Jsoup.parse(html);
        assertNotNull(dom);
        Element depositIdElement = null;
        Element depositUrlElement = null;
        for (Element elem : dom.getAllElements()) {
            if (elem.attr("name").equalsIgnoreCase("depositId")) {
                depositIdElement = elem;
            }
            else if (elem.attr("name").equalsIgnoreCase("depositUrl")) {
                depositUrlElement = elem;
            }
        }
        // Element depositIdElement = dom.getElementById("depositId");
        assertNotNull(depositIdElement);
        String depositId = depositIdElement.val();
        assertNotNull(depositId);        
        assertFalse(depositId.isEmpty());
        
        // Element depositUrlElement = dom.getElementById("depositUrl");
        assertNotNull(depositUrlElement);
        String depositUrl = depositUrlElement.val();
        assertNotNull(depositUrl);
        assertTrue(depositUrl.contains(depositId));

        // depositStatus-0 contains the 'Last Modified' message
        Element depositStatusElement = dom.getElementById("depositStatus-1");
        assertNotNull(depositStatusElement);
        String depositStatus = depositStatusElement.text();
        assertNotNull(depositStatus);
        assertTrue(depositStatus.contains(depositId));
        assertTrue(depositStatus.contains("Event:"));
        assertTrue(depositStatus.contains("testResources.tar"));
    }
}
