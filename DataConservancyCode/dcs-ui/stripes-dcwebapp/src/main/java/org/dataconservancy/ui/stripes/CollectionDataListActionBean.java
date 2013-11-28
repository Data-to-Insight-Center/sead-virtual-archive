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

package org.dataconservancy.ui.stripes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.CollectionException;
import org.dataconservancy.ui.model.Citation;
import org.dataconservancy.ui.model.CitationFormatter;
import org.dataconservancy.ui.model.CitationFormatter.CitationFormat;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.DataItemTransport;
import org.dataconservancy.ui.model.ESIPCitationFormatterImpl;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.CitationService;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.DataItemTransportService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_COLLECTION_DATA_LIST_NO_COLLECTION;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_COLLECTION_DATA_LIST_NO_COLLECTION_NAME;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_COLLECTION_DATA_LIST_NO_ITEMS;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_COLLECTION_DATA_LIST_NO_PERMISSIONS;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_COLLECTION_DATA_LIST_RETRIEVAL_ERROR;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_COLLECTION_DATA_LIST_TOTAL_DATA_ITEMS;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_COLLECTION_DOES_NOT_EXIST;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_PROJECT_FOR_COLLECTION_NOT_FOUND;

/**
 * {@code CollectionDataListActionBean } handles requests to list
 * {@link DataItem} contained in a given {@link Collection}.
 */
@UrlBinding("/collection/collection_list_data.action")
public class CollectionDataListActionBean
        extends BaseActionBean {

    /**
     * The path used to render the list of data items in the collection.
     */
    private final static String COLLECTION_DATA_LIST_PATH =
            "/pages/collection_data_list.jsp";

    private List<DataItem> collectionDataSets;

    private List<DataItemTransport> dataItemTransportList;

    private Map<CitationFormat, String> citations;

    private String currentCollectionId;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ArchiveService archiveService;

    private RelationshipService relationshipService;

    private CollectionBizService collectionBizService;

    private CitationService citationService;

    private DataItemTransportService dataItemTransportService;

    private double maxResultsPerPage = 10;

    private int page = 0;

    private int lastPage = 0;

    private String message = "";

    public CollectionDataListActionBean() {
        super();

        // Ensure desired properties are available
        try {
            assert (messageKeys
                    .containsKey(MSG_KEY_PROJECT_FOR_COLLECTION_NOT_FOUND));
            assert (messageKeys
                    .containsKey(MSG_KEY_COLLECTION_DATA_LIST_NO_COLLECTION));
            assert (messageKeys
                    .containsKey(MSG_KEY_COLLECTION_DATA_LIST_NO_PERMISSIONS));
            assert (messageKeys
                    .containsKey(MSG_KEY_COLLECTION_DATA_LIST_NO_COLLECTION_NAME));
            assert (messageKeys
                    .containsKey(MSG_KEY_COLLECTION_DATA_LIST_NO_ITEMS));
            assert (messageKeys
                    .containsKey(MSG_KEY_COLLECTION_DATA_LIST_RETRIEVAL_ERROR));
            assert (messageKeys
                    .containsKey(MSG_KEY_COLLECTION_DATA_LIST_TOTAL_DATA_ITEMS));
        } catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of "
                    + MSG_KEY_PROJECT_FOR_COLLECTION_NOT_FOUND + " ,"
                    + MSG_KEY_COLLECTION_DATA_LIST_NO_COLLECTION + " ,"
                    + MSG_KEY_COLLECTION_DATA_LIST_NO_PERMISSIONS + " ,"
                    + MSG_KEY_COLLECTION_DATA_LIST_NO_COLLECTION_NAME + " ,"
                    + MSG_KEY_COLLECTION_DATA_LIST_NO_ITEMS + " ,"
                    + MSG_KEY_COLLECTION_DATA_LIST_RETRIEVAL_ERROR + " ,"
                    + MSG_KEY_COLLECTION_DATA_LIST_TOTAL_DATA_ITEMS
                    + " is missing.");

        }
    }

    @DefaultHandler
    public Resolution renderResults() throws CollectionException,
            BizPolicyException, BizInternalException {

        Collection currentCollection = getCurrentCollection();

        if (currentCollection == null) {
            message =
                    messageKeys
                            .getProperty(MSG_KEY_COLLECTION_DATA_LIST_NO_COLLECTION);
        } else {
            // Get the first set of children
            try {
                String collectionDepositId = dataItemTransportService.getDepositId(currentCollectionId);

                ArchiveSearchResult<DataItem> results =
                        archiveService
                                .retrieveDataSetsForCollection(collectionDepositId,
                                                               (int) maxResultsPerPage,
                                                               page * (int) maxResultsPerPage);
                this.setDataItemTransportList(
                        dataItemTransportService.retrieveDataItemTransportList(results));

                generateCitations();
                collectionDataSets = new ArrayList<DataItem>();
                collectionDataSets.addAll(results.getResults());

                double totalResults = results.getResultCount();
                lastPage =
                        (int) Math.ceil(totalResults / maxResultsPerPage) - 1;

                // first check if the results contains anything, before checking the total number of results
                // results will be empty if the user specified an offset greater than the total number, for example
                if (results.getResults().isEmpty()) {
                    message =
                            String.format(messageKeys
                                                  .getProperty(MSG_KEY_COLLECTION_DATA_LIST_NO_ITEMS),
                                          currentCollection.getTitle());
                } else if (totalResults > 0) {
                    message =
                            String.format(messageKeys
                                                  .getProperty(MSG_KEY_COLLECTION_DATA_LIST_TOTAL_DATA_ITEMS),
                                          currentCollection.getTitle(),
                                          (int) totalResults);
                } else {
                    message =
                            String.format(messageKeys
                                                  .getProperty(MSG_KEY_COLLECTION_DATA_LIST_NO_ITEMS),
                                          currentCollection.getTitle());
                }

            } catch (ArchiveServiceException e) {
                message =
                        String.format(messageKeys
                                              .getProperty(MSG_KEY_COLLECTION_DATA_LIST_RETRIEVAL_ERROR),
                                      currentCollection.getTitle());
                throw new CollectionException(message, e);
            }

        }
        return new ForwardResolution(COLLECTION_DATA_LIST_PATH);
    }

    public Resolution nextPage() {
        RedirectResolution next =
                new RedirectResolution(this.getClass(), "renderResults");
        next.addParameter("page", page++);
        next.addParameter("currentCollectionId", currentCollectionId);
        return next;
    }

    public Project getProjectForCurrentCollection() throws CollectionException,
            BizInternalException, BizPolicyException {
        Project project = null;
        Collection currentCollection = getCurrentCollection();
        if (currentCollection != null && currentCollection.getId() != null
                && !currentCollection.getId().isEmpty()) {
            try {
                project =
                        relationshipService
                                .getProjectForCollection(currentCollection);
            } catch (RelationshipConstraintException e) {
                String msg =
                        String.format(MSG_KEY_PROJECT_FOR_COLLECTION_NOT_FOUND,
                                      currentCollection.getId());
                log.error(msg);
                CollectionException ce = new CollectionException(msg);
                ce.setCollectionId(currentCollection.getId());
                ce.setUserId(getAuthenticatedUser().getId());
                throw ce;
            }
        }

        return project;
    }

    public void generateCitations() throws CollectionException,
            BizInternalException, BizPolicyException, ArchiveServiceException {
        // fetch collection
        final Collection collection = getCollection(currentCollectionId);
        if (collection == null) {
            final String msg =
                    String.format(messageKeys
                                          .getProperty(MSG_KEY_COLLECTION_DOES_NOT_EXIST),
                                  currentCollectionId);
            log.debug(msg);
            throw new CollectionException(msg);
        }
        //create citation object
        Citation citation = citationService.createCitation(collection);

        citations = new HashMap<CitationFormat, String>();

        //format citation with ESIP formater
        ESIPCitationFormatterImpl citationFormatter =
                new ESIPCitationFormatterImpl();
        String ESIPcitationString = citationFormatter.formatHtml(citation);
        citations.put(CitationFormat.ESIP, ESIPcitationString);
    }

    public Map<CitationFormat, String> getCitations() {
        return this.citations;
    }

    public CitationFormat[] getCitationFormats() {
        return CitationFormatter.CitationFormat.values();
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLastPage() {
        return lastPage;
    }

    public void setLastPage(int page) {
        this.lastPage = page;
    }

    public double getMaxResultsPerPage() {
        return maxResultsPerPage;
    }

    public void setMaxResultsPerPage(double maxResultsPerPage) {
        this.maxResultsPerPage = maxResultsPerPage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    public Collection getCurrentCollection() throws BizInternalException,
            BizPolicyException {
        return getCollection(currentCollectionId);
    }

    public Collection getCollection(String objectId)
            throws BizInternalException, BizPolicyException {
        Collection collection = null;
        collection =
                collectionBizService.getCollection(objectId,
                                                   getAuthenticatedUser());
        return collection;
    }

    public void setCurrentCollectionId(String id) {
        this.currentCollectionId = id;
    }

    public String getCurrentCollectionId() {
        return this.currentCollectionId;
    }

    public void setCollectionDataSets(List<DataItem> datasets) {
        this.collectionDataSets = datasets;
    }

    public List<DataItem> getCollectionDataSets() {
        return collectionDataSets;
    }

    public void setDataItemTransportList(List<DataItemTransport> transports) {
        this.dataItemTransportList = transports;
    }

    public List<DataItemTransport> getDataItemTransportList() {
        return dataItemTransportList;
    }

    @SpringBean("archiveService")
    public void injectArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @SpringBean("relationshipService")
    public void injectRelationshipService(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @SpringBean("collectionBizService")
    public void injectCollectionBizService(CollectionBizService collectionBizService) {
        this.collectionBizService = collectionBizService;
    }

    @SpringBean("citationService")
    public void injectCitationService(CitationService citationService) {
        this.citationService = citationService;
    }

    @SpringBean("dataItemTransportService")
    public void injectDataItemTransportService(DataItemTransportService dataItemTransportService) {
        this.dataItemTransportService = dataItemTransportService;
    }
}
