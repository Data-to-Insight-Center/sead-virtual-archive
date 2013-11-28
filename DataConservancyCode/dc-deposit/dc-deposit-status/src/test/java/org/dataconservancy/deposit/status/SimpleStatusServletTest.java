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
package org.dataconservancy.deposit.status;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import org.junit.Assert;
import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;

public class SimpleStatusServletTest {

    /** Generated URLs should start with baseURL */
    @Test
    public void baseURLTest() {
        final String BASEURL = "http://example.org/test";
        final String MGR_ID = "id:1";
        DepositDocumentServlet status = new DepositStatusServlet();
        status.setBaseURL(BASEURL);
        status.setDepositManagers(getTestManagers(MGR_ID));
        String url = status.getURL(new MockDepositInfo("id", MGR_ID));

        Assert.assertTrue("URL should start with BaseURL", url
                .startsWith(BASEURL));
    }

    /** Simple end-to-end url generation and status resolution test */
    @Test
    public void simpleRetrievalTest() throws Exception {
        doBasicRetrieval("simple_deposit_id", "simple_mgr_id");
    }

    /** End-to-end test using very troublesome IDs */
    @Test
    public void troublesomIdCharactersTest() throws Exception {
        doBasicRetrieval("/|\\\u00fc?#//+", "//++&&%%(\u00ea){}##?*");
    }

    private void doBasicRetrieval(String depositid, String mgrid)
            throws Exception {
        final String BASEURL = "http://example.org/webapp/depositServlet";
        final String MGR_ID_1 = "id_1";

        final String STATUS_CONTENT = "status content";
        final String STATUS_MIME = "text/foo";

        DepositDocumentServlet serv = new DepositStatusServlet();
        serv.setBaseURL(BASEURL);

        List<StatusTestDepositManager> mgrs = getTestManagers(MGR_ID_1, mgrid);
        serv.setDepositManagers(mgrs);

        MockDepositInfo status = new MockDepositInfo(depositid, mgrid);
        MockDepositDocument statusDoc = new MockDepositDocument();
        statusDoc.setInputStream(IOUtils.toInputStream(STATUS_CONTENT));
        statusDoc.setMimeType(STATUS_MIME);
        status.setDepositStatus(statusDoc);

        mgrs.get(1).setDepositStatus(depositid, status);

        String url = serv.getURL(status);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", url);
        request.setPathInfo(url.replace(BASEURL, ""));

        MockHttpServletResponse response = new MockHttpServletResponse();

        serv.doGet(request, response);

        assertEquals("Wrong response http code",
                     HttpServletResponse.SC_OK,
                     response.getStatus());
        assertEquals("Wrong response mime type",
                     response.getContentType(),
                     STATUS_MIME);
        assertEquals("Did not return correct content", STATUS_CONTENT, response
                .getContentAsString());
    }

    private List<StatusTestDepositManager> getTestManagers(String... ids) {
        ArrayList<StatusTestDepositManager> mgrs =
                new ArrayList<StatusTestDepositManager>();
        for (String id : ids) {
            mgrs.add(new StatusTestDepositManager(id));
        }
        return mgrs;
    }
}
