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

package org.seadva.bagit;


import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.test.framework.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.net.URLEncoder;

import static org.junit.Assert.assertEquals;


public class BagItTest extends JerseyTest {

    public BagItTest() throws Exception {
        super("org.seadva.bagit");
    }

    @Test
    public void testGetBag() {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("sparqlEpEnum", "3");

        ClientResponse response = webResource.path("acrToBag")
                .path("bag")
                .path(
                        URLEncoder.encode("tag:cet.ncsa.uiuc.edu,2008:/bean/Collection/E012014D-7379-4556-8A87-6AD262965C89")
                )
                .queryParams(params)
                .accept("application/zip")
                .get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }
}
