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
package org.dataconservancy.ui.api.support;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: HanhVu
 * Date: 6/7/12
 * Time: 5:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockHttpServletRequestFactory {

    public static MockHttpServletRequest newMockRequest(String method,
                                                        String requestUri,
                                                        String host,
                                                        int port) {
        MockHttpServletRequest req =
                new MockHttpServletRequest(method, requestUri);
        req.setRemoteHost(host);
        req.setContentType("application/x-www-form-urlencoded");
        req.setRemotePort(port);
        if (port == 443) {
            req.setScheme("https");
            req.setSecure(true);
        } else {
            req.setScheme("http");
            req.setSecure(false);
        }

        return req;
    }
}
