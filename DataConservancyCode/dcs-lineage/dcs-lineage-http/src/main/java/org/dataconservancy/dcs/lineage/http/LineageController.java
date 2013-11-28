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
package org.dataconservancy.dcs.lineage.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.api.LineageEntry;
import org.dataconservancy.dcs.lineage.api.LineageService;
import org.dataconservancy.dcs.lineage.http.support.RequestUtil;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcs.DcsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ACCEPT;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ENTITIES;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ETAG;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ID;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.IFMODIFIEDSINCE;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.LASTMODIFIED;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.LINEAGE;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

/**
 * Primary entry point into the Lineage HTTP API, responsible for accepting the HTTP request and returning a
 * {@link Model} object populated with {@link LineageModelAttribute attributes}.
 * <p/>
 * Generally this will entail:
 * <ul>
 *     <li>Determining the requested lineage or entity id</li>
 *     <li>Retrieving the lineage from the {@code LineageService}</li>
 *     <li>Calculating an ETag for the lineage</li>
 *     <li>Determining the last modification date of the lineage</li>
 *     <li>Recording the values of the {@code Accept} and {@code If-Modified-Since} request headers.</li>
 * </ul>
 * All of these values are placed into the returned {@code Model}.  Spring will then attempt to resolve a
 * {@link org.springframework.web.servlet.View View} which will be used to render the appropriate response provided
 * the {@code Model}.
 *
 * @see org.dataconservancy.dcs.lineage.http.view.LineageViewResolver
 */
@Controller
@RequestMapping("/lineage")
public class LineageController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final LineageService lineageService;
    private final IdService idService;
    private final LookupQueryService<DcsEntity> lookupQueryService;

    private final RequestUtil util = new RequestUtil();

    //Default values for parameters not provided by the user.
    private final String DEFAULT_TS = "-1";
    private final String DEFAULT_ID = "";
    private final String DEFAULT_FROM_TO = "-1";

    public LineageController(LineageService lineageService, IdService idService, LookupQueryService<DcsEntity> lookupQueryService) {
        this.lineageService = lineageService;
        this.idService = idService;
        this.lookupQueryService = lookupQueryService;
    }

    /**
     * Handles GET and HEAD requests in the form: {@code http://instance.org/dcs/lineage/}.
     *
     * @param mimeType
     * @param modifiedSince
     * @param request
     * @return
     */
    @RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.HEAD })
    public Model handleEmptyGetRequest(
            @RequestHeader(value = "Accept", required = false) String mimeType,
            @RequestHeader(value = "If-Modified-Since", required = false) @DateTimeFormat(iso = DATE_TIME) Date modifiedSince,
            HttpServletRequest request) {

        Model model = newModel();
        model.addAttribute(ENTITIES.name(), Collections.emptyList());
        model.addAttribute(ETAG.name(), util.calculateDigestForEntities(new ArrayList<DcsEntity>()));
        model.addAttribute(LASTMODIFIED.name(), null);
        model.addAttribute(IFMODIFIEDSINCE.name(), modifiedSince);
        model.addAttribute(ACCEPT.name(), mimeType);
        model.addAttribute(ID.name(), null);

        return model;
    }

    /**
     * Handles GET and HEAD requests in the form: {@code http://instance.org/dcs/lineage/1234}.
     *
     * @param mimeType
     * @param modifiedSince
     * @param request
     * @return
     * @throws QueryServiceException
     */
    @RequestMapping(value = "/{idpart}", method = { RequestMethod.GET, RequestMethod.HEAD })
    public Model handleLineageGetRequest(
            @RequestHeader(value = "Accept", required = false) String mimeType,
            @RequestHeader(value = "If-Modified-Since", required = false) @DateTimeFormat(iso = DATE_TIME) Date modifiedSince,
            HttpServletRequest request) throws QueryServiceException {

        String id = util.buildRequestUrl(request);              // must be a lineage id
        Lineage lineage = getLineage(id);                       // obtains the lineage
        List<DcsEntity> entities = getDcsEntityList(lineage);   // returns the DCS entities in the lineage
        Date lastModified = getLastModified(lineage);           // gets the date on the latest entity       

        Model model = newModel();
        model.addAttribute(LINEAGE.name(), lineage);
        model.addAttribute(ENTITIES.name(), entities);
        model.addAttribute(ETAG.name(), RequestUtil.calculateDigestForEntities(entities));
        model.addAttribute(LASTMODIFIED.name(), lastModified);
        model.addAttribute(IFMODIFIEDSINCE.name(), modifiedSince);
        model.addAttribute(ACCEPT.name(), mimeType);
        model.addAttribute(ID.name(), id);

        return model;
    }

    /**
     * Handles a get request for retrieving the original member of a lineage
     * by id.
     *
     * @param mimeType
     * @param modifiedSince
     * @param id
     * @return
     * @throws QueryServiceException
     */
    @RequestMapping(value = "/original", method = RequestMethod.GET)
    @ModelAttribute("entities")
    public Model handleOriginalGetRequest(
            @RequestHeader(value = "Accept", required = false) String mimeType,
            @RequestHeader(value = "If-Modified-Since", required = false) @DateTimeFormat(iso = DATE_TIME) Date modifiedSince,
            @RequestParam(value = "id", required = true) String id) throws QueryServiceException {

        Lineage lineage = getLineage(id);
        LineageEntry oldest = getOriginal(lineage);
        List<DcsEntity> entities = getDcsEntityList(oldest);
        Date lastModified = getLastModified(oldest);

        Model model = newModel();
        model.addAttribute(LINEAGE.name(), lineage);
        model.addAttribute(ENTITIES.name(), entities);
        model.addAttribute(ETAG.name(), util.calculateDigestForEntities(entities));
        model.addAttribute(LASTMODIFIED.name(), lastModified);
        model.addAttribute(IFMODIFIEDSINCE.name(), modifiedSince);
        model.addAttribute(ACCEPT.name(), mimeType);
        model.addAttribute(ID.name(), id);
        
        return model;
    }

    /**
     * Handles a get request for the latest member of a lineage by id.
     *
     * @param mimeType
     * @param modifiedSince
     * @param id
     * @param ts
     * @return
     * @throws QueryServiceException
     */
    @RequestMapping(value = "/latest", method = RequestMethod.GET)
    @ModelAttribute("entities")
    public Model handleLatestGetRequest(
            @RequestHeader(value = "Accept", required = false) String mimeType,
            @RequestHeader(value = "If-Modified-Since", required = false) @DateTimeFormat(iso = DATE_TIME) Date modifiedSince,
            @RequestParam(value = "id", required = true) String id,
            @RequestParam(value = "ts", required = false, defaultValue = DEFAULT_TS) long ts) throws QueryServiceException {

        Lineage lineage = getLineage(id);
        
        LineageEntry lineageEntry = Long.valueOf(DEFAULT_TS) == ts
                ? getLatest(lineage)
                : getLatest(id, ts);
        
        List<DcsEntity> entities = getDcsEntityList(lineageEntry);
        Date lastModified = getLastModified(lineageEntry);

        Model model = newModel();
        model.addAttribute(LINEAGE.name(), lineage);
        model.addAttribute(ENTITIES.name(), entities);
        model.addAttribute(ETAG.name(), util.calculateDigestForEntities(entities));
        model.addAttribute(LASTMODIFIED.name(), lastModified);
        model.addAttribute(IFMODIFIEDSINCE.name(), modifiedSince);
        model.addAttribute(ACCEPT.name(), mimeType);
        model.addAttribute(ID.name(), id);
        
        return model;
    }

    /**
     * Handles a get request for a lineage search by lineage id + date or pair
     * of entity ids.
     *
     * @param mimeType
     * @param modifiedSince
     * @param id
     * @param from
     * @param to
     * @return
     * @throws QueryServiceException
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ModelAttribute("entities")
    public Model handleSearchGetRequest(
            @RequestHeader(value = "Accept", required = false) String mimeType,
            @RequestHeader(value = "If-Modified-Since", required = false) @DateTimeFormat(iso = DATE_TIME) Date modifiedSince,
            @RequestParam(value = "id", required = false, defaultValue = DEFAULT_ID) String id,
            @RequestParam(value = "from", required = false, defaultValue = DEFAULT_FROM_TO) String from,
            @RequestParam(value = "to", required = false, defaultValue = DEFAULT_FROM_TO) String to) throws QueryServiceException {

        Lineage lineage;
        List<DcsEntity> entities;
        Date lastModified;

        if (DEFAULT_ID.equals(id)) {
            id = null;
        }
        if (DEFAULT_FROM_TO.equals(from)) {
            from = null;
        }
        if (DEFAULT_FROM_TO.equals(to)) {
            to = null;
        }
        
        //In this case, one or both of from and to are entity identifiers, so
        // get the lineage from the entity range.
        if ((null == from || isIdType(from, Types.DELIVERABLE_UNIT.getTypeName()))
                && (null == to || isIdType(to, Types.DELIVERABLE_UNIT.getTypeName()))
                && !(null == from && null == to)) {
            Lineage partialLineage = getLineageForEntityRange(from, to);
            lineage = null == partialLineage ? null : getLineage(from, to);
            entities = getDcsEntityList(partialLineage);
            lastModified = getLastModified(partialLineage); 
            id = null == lineage
                    ? null
                    : (null != from ? from : to);
        }
        else if (isIdType(id, Types.DELIVERABLE_UNIT.getTypeName()) || isIdType(id, Types.LINEAGE.getTypeName())) {
            //In this case, both from and to have not been set, so we return
            // the lineage of id.
            if (null == from && null == to) {
                lineage = getLineage(id);
                entities = getDcsEntityList(lineage);
                lastModified = getLastModified(lineage);
            }
            //In this case, both from and to are timestamps or undefined, so
            // return the lineage between those timestamps.
            else if ((null == from || isLong(from))
                    && (null == to || isLong(to))) {
                Lineage partialLineage = getLineageForDateRange(id, from, to);
                lineage = null == partialLineage ? null : getLineage(id);
                entities = getDcsEntityList(partialLineage);
                lastModified = getLastModified(partialLineage);
            }
            //In this case, both from and to are defined but both of them
            // are not longs.  This is an error.
            else {
                throw new IllegalArgumentException("Illegal query parameters: if supplied, from and to must both be timestamps or ids of deliverable units and id must be a lineage id or the id of a deliverable unit.  At least one of id, from, or to must be an identifier.");
            }
        }
        else if ((null == from || isLong(from))
                && (null == to || isLong(to))) {
            //In this case, both from and to are timestamps or undefined, so
            // return the lineage between those timestamps.
            Lineage partialLineage = getLineageForDateRange(id, from, to);
            lineage = null == partialLineage ? null : getLineage(id);
            entities = getDcsEntityList(partialLineage);
            lastModified = getLastModified(partialLineage);
        }
        //In this case, none of from, to, or id are the correct type of
        // identifier or timestamp to make this work.  This is an error.
        else {
            throw new IllegalArgumentException("Illegal query parameters: if supplied, from and to must both be timestamps or ids of deliverable units and id must be a lineage id or the id of a deliverable unit.  At least one of id, from, or to must be an identifier.");
        }

        Model model = newModel();
        model.addAttribute(LINEAGE.name(), lineage);
        model.addAttribute(ENTITIES.name(), entities);
        model.addAttribute(ETAG.name(), util.calculateDigestForEntities(entities));
        model.addAttribute(LASTMODIFIED.name(), lastModified);
        model.addAttribute(IFMODIFIEDSINCE.name(), modifiedSince);
        model.addAttribute(ACCEPT.name(), mimeType);
        model.addAttribute(ID.name(), id);

        return model;
    }

    /**
     * Handle a Throwable by returning a 500.
     *
     * @return
     */
    @ExceptionHandler(Throwable.class)
    public void handleThrowable(Throwable t, HttpServletRequest req, HttpServletResponse resp) {
        final String msg = "Error executing request: " + t.getMessage();
        log.error(msg, t);
        try {
            resp.sendError(500, msg);
        } catch (IOException e) {
            // Don't care
        }
    }

    //Build a new ExtendedModelMap.
    private Model newModel() {
        return new ExtendedModelMap();
    }

    //These seven methods get a lineage or lineageEntry from the lineage
    // service (with various parameters).  They return null if the lineage
    // or entry is not present.
    private Lineage getLineage(String id) {
        return lineageService.getLineage(id);
    }

    private Lineage getLineage(String id1, String id2) {
        return null == id2
                ? lineageService.getLineage(id1)
                : lineageService.getLineage(id2);
    }

    private LineageEntry getOriginal(Lineage lineage) {
        return null == lineage ? null : lineage.getOldest();
    }

    private LineageEntry getLatest(Lineage lineage) {
        return null == lineage ? null : lineage.getNewest();
    }

    private LineageEntry getLatest(String id, long ts) {
        return lineageService.getEntryForDate(id, ts);
    }

    private Lineage getLineageForEntityRange(String from, String to) {
        return lineageService.getLineageForEntityRange(from, to);
    }

    private Lineage getLineageForDateRange(String id, String from, String to) {
        long longFrom = null == from
                ? -1
                : Long.parseLong(from);
        long longTo = null == to
                ? -1
                : Long.parseLong(to);
        return lineageService.getLineageForDateRange(id, longFrom, longTo);
    }

    //These two methods get the newest lineage entry from a (sub-)lineage and
    // provide its timestamp (for use in determining if the lineage has been
    // modified since a certain date).  They all return null if no elements
    // are returned for the lineage.

    /**
     * Obtain the newest lineage entry from a (sub-)lineage and
     * provide its timestamp (for use in determining if the lineage has been
     * modified since a certain date).  This returns {@code null} if no elements
     * are returned for the lineage.
     * <p/>
     * Package-private for unit testing.
     *
     * @param lineage the lineage, may be null or an empty lineage
     * @return the most recently modified date in the lineage, or {@code null}
     */
    Date getLastModified(Lineage lineage) {
        if (lineage == null) {
            return null;
        }

        LineageEntry entry = lineage.getNewest();

        if (entry != null) {
            return new Date(entry.getEntryTimestamp());
        }

        return null;
    }

    private Date getLastModified(LineageEntry entry) {
        return null == entry
                ? null
                : new Date(entry.getEntryTimestamp());
    }
    
    //Determine if an identifier is of a certain type (used in searching).
    private boolean isIdType(String id, String type) {
        try{
            final Identifier identifier = idService.fromUid(id);
            // Normally, INFE would be thrown by the IdService impl if the return from
            // 'fromUid(String)' were to be null.  But this is hard with Mockito; when
            // IdService is mocked, 'fromUid(String)' returns null instead of throwing
            // INFE.  So we check for a null 'identifier' and throw an INFE here.
            // See also the LineageControllerTest.
            if (identifier == null) {
                throw new IdentifierNotFoundException();
            }

            return identifier.getType().equals(type);
        } catch (IdentifierNotFoundException e) {
            log.debug("Supplied id [{}] not found in idService {}", id,
                    idService.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(idService)));
            return false;
        }
    }

    //Determine if a string is a long (used in searching)
    private boolean isLong(String potential) {
        try {
            Long.parseLong(potential);  
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    //Transform a lineage into a list of DcsEntities - since the lineage
    // service returns a lineage, it is the responsibility of the API to
    // transform that into a list of DcsEntities via the Query Service.
    private List<DcsEntity> getDcsEntityList(Lineage lineage) throws QueryServiceException {
        List<DcsEntity> entities = new ArrayList<DcsEntity>();
        DcsEntity entity;

        //If the lineage is not null, iterate through it converting the
        // LineageEntries into DcsEntities.
        if (null != lineage) {
            for (LineageEntry entry : lineage) {
                entity = getDcsEntity(entry);
                if (entity != null) {
                    entities.add(entity);
                }
            }
        }

        return entities;        
    }

    //Transform a lineage entry into a DcsEntity via the lookupQueryService.
    private DcsEntity getDcsEntity(LineageEntry entry) throws QueryServiceException {
        return entry == null ? null : lookupQueryService.lookup(entry.getEntityId());
    }

    //Wrapper for producing a singleton list from a lineageEntry
    private List<DcsEntity> getDcsEntityList(LineageEntry entry) throws QueryServiceException {
        return entry == null ? new ArrayList<DcsEntity>() : Collections.singletonList(getDcsEntity(entry));
    }
}
