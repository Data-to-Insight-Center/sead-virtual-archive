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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.net.URLDecoder;

import java.util.Date;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.api.support.RequestUtil;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.BizPolicyException.Type;
import org.dataconservancy.ui.exceptions.PersonUpdateException;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.util.ETagCalculator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ETAG;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.LAST_MODIFIED;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.LOCATION;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
/**
 * Entry point to obtaining representations of Persons.  Currently the specification of the HTTP API is being
 * tracked <a href="https://scm.dataconservancy.org/confluence/x/YgCV">here</a>
 *
 * @see <a href="https://scm.dataconservancy.org/confluence/x/YgCV">https://scm.dataconservancy.org/confluence/x/YgCV</a>
 */
@Controller
@RequestMapping("/person")
public class PersonController extends BaseController{

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final BusinessObjectBuilder builder;

    public PersonController(UserService userService,
                            BusinessObjectBuilder builder,
                            RequestUtil requestUtil) {
        super(userService, requestUtil);
        this.builder = builder;
    }

    /**
     * Handles empty get call this will return a list of users in the system.
     * Not yet implemented
     *
     * @param mimeType
     * @param modifiedSince
     * @param request
     * @throws java.io.IOException
     * @throws org.dataconservancy.ui.exceptions.BizPolicyException
     */
    @RequestMapping(method = {RequestMethod.GET})
    public void handleEmptyGetRequest(@RequestHeader(value = "Accept", required = false) String mimeType,
                                      @RequestHeader(value = "If-Modified-Since", required = false) @DateTimeFormat(iso = DATE_TIME) Date modifiedSince,
                                      HttpServletRequest request,
                                      HttpServletResponse resp)
            throws ServletException, IOException, BizPolicyException {

        resp.setStatus(HttpStatus.SC_FORBIDDEN);
        resp.getWriter().println("Not yet implemented.");
    }

    /**
     * Handles get request with an id, this returns the serialized person
     * identified by the id. Not yet implemented.
     *
     * @param idpart
     * @param mimeType
     * @param modifiedSince
     * @param request
     * @throws org.dataconservancy.ui.exceptions.BizPolicyException
     */
    @RequestMapping(value = "/{idpart}", method = {RequestMethod.GET})
    public void handlePersonGetRequest(@PathVariable String idpart,
                                        @RequestHeader(value = "Accept", required = false) String mimeType,
                                        @RequestHeader(value = "If-Modified-Since", required = false) @DateTimeFormat(iso = DATE_TIME) Date modifiedSince,
                                        HttpServletRequest request,
                                        HttpServletResponse resp)
            throws IOException, BizPolicyException {

        resp.setStatus(HttpStatus.SC_FORBIDDEN);
        resp.getWriter().println("Not yet implemented.");
    }


    /**
     * Handles the posting of a new person, this will add a new person to the
     * system with registration status of pending.
     *
     * @throws org.dataconservancy.model.builder.InvalidXmlException
     * @throws java.io.IOException
     */
    @RequestMapping(method = {RequestMethod.POST})
    public void handlePersonPostRequest(@RequestHeader(value = "Accept", required = false) String mimeType,
                                        @RequestBody byte[] content,
                                        HttpServletRequest req,
                                        HttpServletResponse resp)
            throws BizInternalException, BizPolicyException,
            InvalidXmlException, IOException {
        if (req.getContentType().contains("application/x-www-form-urlencoded")) {
            content = URLDecoder.decode(new String(content, "UTF-8"), "UTF-8").getBytes("UTF-8");
        }
        //use businessobjectBuilder to deserialize content into a bop -> set of person
        Set<Person> postedPersonSet = builder.buildBusinessObjectPackage(new ByteArrayInputStream(content)).getPersons();
        //currently, only handle request to create 1 person
        //A request is considered BAD if it contains 0 person, or more than 1 person
        if (postedPersonSet.size() != 1) {
            try {
                resp.setStatus(HttpStatus.SC_BAD_REQUEST);
                resp.getWriter()
                        .print("Only one new person can be requested to be created via this API at a time.");
                resp.getWriter().flush();
                resp.getWriter().close();
            } catch (Exception ee) {
                log.debug("Handling exception", ee);
            }
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Person person = postedPersonSet.iterator().next();
            try {
                Person createPerson = userService.create(person);
                 //Serialize the created Person to return
                Bop businessObjectPackage = new Bop();
                businessObjectPackage.addPerson(createPerson);

                resp.setStatus(HttpStatus.SC_OK);

                resp.setHeader(LOCATION, createPerson.getId());
                resp.setHeader(LAST_MODIFIED, DateTime.now().toString());
                builder.buildBusinessObjectPackage(businessObjectPackage,
                        baos);
                resp.setHeader(ETAG, ETagCalculator.calculate(Integer.toString(businessObjectPackage.hashCode())));
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("text/xml");
                resp.setContentLength(baos.size());
                baos.writeTo(resp.getOutputStream());
            } catch (PersonUpdateException re) {
                resp.setStatus(HttpStatus.SC_BAD_REQUEST);
                resp.getOutputStream().println(re.getMessage());
                resp.getWriter().flush();
                resp.getWriter().close();
            }
        }
    }

    /**
     * Handles updating a person matching the given id. Note: Dates should be in
     * the format dd mm yyyy
     *
     * @param idpart
     * @throws org.dataconservancy.model.builder.InvalidXmlException
     * @throws org.dataconservancy.ui.exceptions.BizPolicyException
     * @throws java.io.IOException
     */
    @RequestMapping(value = "/{idpart}", method = {RequestMethod.PUT})
    public void handleUpdatePersonRequest(@PathVariable String idpart,
                                           @RequestHeader(value = "Accept", required = false) String mimeType,
                                           @RequestBody byte[] content,
                                           HttpServletRequest req,
                                           HttpServletResponse resp)
            throws BizInternalException, InvalidXmlException,
            BizPolicyException, IOException {

        resp.setStatus(HttpStatus.SC_FORBIDDEN);
        resp.getWriter().println("Not yet implemented.");
    }

    @ExceptionHandler(PersonUpdateException.class)
    public void handleException(PersonUpdateException e,
                                HttpServletRequest req,
                                HttpServletResponse resp) {
        log.debug("Handling exception [" + e.getClass() + "] ["
                + e.getMessage() + "]");

        resp.setStatus(HttpStatus.SC_BAD_REQUEST);

        try {
            resp.getWriter().print(e.getMessage());
            resp.getWriter().flush();
            resp.getWriter().close();
        } catch (Exception ee) {
            log.debug("Handling exception", e);
        }
    }

}
