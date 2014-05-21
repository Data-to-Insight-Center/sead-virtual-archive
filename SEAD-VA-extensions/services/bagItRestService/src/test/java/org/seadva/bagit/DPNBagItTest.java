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
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.junit.Test;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

import static org.junit.Assert.assertEquals;

public class DPNBagItTest extends JerseyTest {

    public DPNBagItTest() throws Exception {
        super(new WebAppDescriptor.Builder("org.seadva.bagit").
                contextParam("testPath","/src/main/webapp/").build());
    }

    @Context
    ServletContext servletConfig;
    @Test
    public void testDPNOreDataBagUtil() {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("dirpath", "/Users/Aravindh/Documents/DPN/sead/SEAD-VA-extensions/services/bagItRestService/target/test-classes/org/seadva/bagit/sample_bag");

        ClientResponse response = webResource.path("dpnBagUtil")
                .path("DPNOreDataBag")
                .queryParams(params)
                .get(ClientResponse.class);
        assertEquals(200,response.getStatus());

    }

}

