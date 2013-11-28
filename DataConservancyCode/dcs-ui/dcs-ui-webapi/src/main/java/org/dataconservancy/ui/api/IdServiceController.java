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
package org.dataconservancy.ui.api;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.api.support.RequestUtil;
import org.dataconservancy.ui.services.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is a controller which exposes the {@link IdService} instance of the DCS User Interface by layering an HTTP
 * API on top of {@code IdService} creation methods.  It only supports creating {@link Identifier}s with the ID Types
 * enumeration specified in {@link Types}.
 * <p/>
 * This controller should only be used by developers, and is not meant to be exposed or used in production environments.
 * However, currently there are not any mechanisms in place to prevent this API from being invoked.
 */
@RequestMapping("/id")
public class IdServiceController extends BaseController {

    private IdService idService;

    public IdServiceController(IdService idService, UserService userService, RequestUtil requestUtil) {
        super(userService, requestUtil);
        this.idService = idService;
    }

    /**
     * Attempts to create an identifier with the supplied {@code type}.  If the type is not acceptable, a {@code 400}
     * is returned, otherwise a {@code 201} is returned, with a {@code Location} header with the value of the created
     * identifier.
     *
     * @param type the type of identifier to create, from the Id.Type enumeration in {@link Id}
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @throws IOException if there is an error committing the response
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public void handleDataItemGetRequest(@RequestParam(required = true) String type,
                                         HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Insure that the supplied type is acceptable.
        Types idType = null;
        try {
            idType = Types.valueOf(type);
        } catch (IllegalArgumentException e) {
            String message = "Cannot create identifier of type [" + type + "]";
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }

        // Create the Identifier
        Identifier id = idService.create(idType.name());

        // String form of the id
        String idAsString = id.getUrl().toString();

        // Return the String form of the identifier in a "Location" header
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setHeader("Location", idAsString);

        response.flushBuffer();
    }

}
