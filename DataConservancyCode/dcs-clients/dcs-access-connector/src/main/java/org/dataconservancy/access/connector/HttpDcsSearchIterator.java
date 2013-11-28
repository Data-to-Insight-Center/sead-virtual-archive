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
package org.dataconservancy.access.connector;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
class HttpDcsSearchIterator implements CountableIterator<DcsEntity> {


    /**
     * String format for the search query url.
     * Parameters: url, solr query, offset, max
     */
    private static final String SEARCH_QUERY_URL_OFFSET = "%s/?q=%s&offset=%s";

    private static final String SEARCH_QUERY_URL_OFFSET_AND_MAX = "%s/?q=%s&offset=%s&max=%s";

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DcsModelBuilder builder;
    private final HttpClient httpclient;
    private final DcsConnectorConfig config;
    private final String query;

    // Mutable state

    /**
     * The entities returned by the most recent search query
     */
    private Iterator<DcsEntity> currentItr;

    /**
     * The number of entities in the {@link #currentItr}
     */
    private int currentItrSize;

    /**
     * The {@link #currentItr}'s current offset into total the results
     */
    private int currentOffset = -1;

    /**
     * The total number of results for {@link #query}
     */
    private int totalHits = -1;
    
    /**
     * The max number of results to be returned by {@link #query}
     */
    private int maxResults = -1;
    
    /**
     * The offset of where the data should be searched
     */
    private int searchOffset = 0;
    
    /**
     * The location of the iter in the set of data
     */
    private int currentIterPosition = 0;

    HttpDcsSearchIterator(HttpClient client, DcsConnectorConfig config, DcsModelBuilder builder, String query) {
        if (client == null) {
            throw new IllegalArgumentException("HttpClient must not be null.");
        }

        if (query == null || query.trim().length() == 0) {
            throw new IllegalArgumentException("Query must not be null or the empty string.");
        }

        if (builder == null) {
            throw new IllegalArgumentException("Model builder must not be null.");
        }

        if (config == null) {
            throw new IllegalArgumentException("DcsConnectorConfig must not be null.");
        }

        this.httpclient = client;
        this.config = config;
        this.query = query;
        this.builder = builder;
        this.maxResults = -1;
        this.searchOffset = 0;
        this.currentIterPosition = 0;

        try {
            refreshCurrentIterator();
        } catch (Exception e) {
            throw new RuntimeException("Could not construct the search iterator:" + e.getMessage(), e);
        }

    }
    
    HttpDcsSearchIterator(HttpClient client, DcsConnectorConfig config, DcsModelBuilder builder, String query, int maxResults, int offset) {
        if (client == null) {
            throw new IllegalArgumentException("HttpClient must not be null.");
        }

        if (query == null || query.trim().length() == 0) {
            throw new IllegalArgumentException("Query must not be null or the empty string.");
        }

        if (builder == null) {
            throw new IllegalArgumentException("Model builder must not be null.");
        }

        if (config == null) {
            throw new IllegalArgumentException("DcsConnectorConfig must not be null.");
        }

        this.httpclient = client;
        this.config = config;
        this.query = query;
        this.builder = builder;
        this.maxResults = maxResults;
        this.searchOffset = offset;
        this.currentIterPosition = 0;

        try {
            refreshCurrentIterator();
        } catch (Exception e) {
            throw new RuntimeException("Could not construct the search iterator:" + e.getMessage(), e);
        }
    }


    @Override
    public boolean hasNext() {
        
        //If max results is set, stop the iterator from refreshing to the next set of data. 
        if (maxResults > 0) {
            if (currentIterPosition >= maxResults) {
                return false;
            }
        }
        
        try {
            refreshCurrentIterator();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            reset();
        } catch (InvalidXmlException e) {
            log.error(e.getMessage(), e);
            reset();
        }

        if (currentItr == null) {
            return false;
        }

        return currentItr.hasNext();
    }

    @Override
    public DcsEntity next() {
        try {
            refreshCurrentIterator();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            reset();
        } catch (InvalidXmlException e) {
            log.error(e.getMessage(), e);
            reset();
        }

        if (currentItr != null) {
            currentIterPosition++;
            return currentItr.next();
        }

        throw new NoSuchElementException();
    }

    public int getTotalHits() {
        return totalHits;
    }
    
    /**
     * Returns the total search count. Note: Could be more than the max results passed in.
     */
    public long count() {
        return totalHits;
    }

    /**
     * Exposed for unit testing only.
     * 
     * @return
     */
    Iterator<DcsEntity> getCurrentItr() {
        return currentItr;
    }

    /**
     * Initializes the {@link #currentItr} with entities, or refreshes the {@link #currentItr} if it is expired.
     * Method is package-private for unit testing.
     *
     * @throws IOException
     * @throws InvalidXmlException
     */
    void refreshCurrentIterator() throws IOException, InvalidXmlException {
        if (currentItr == null) {
            // If we haven't performed a search yet, perform the search and assemble the results.
            final HttpResponse response = performQuery(searchOffset, maxResults);
            totalHits = Integer.parseInt(response.getFirstHeader("X-TOTAL-MATCHES").getValue());
            if (totalHits > 0) {
                final Set<DcsEntity> entities = parseQueryResponse(response);
                // Set state
                currentItrSize = entities.size();
                currentItr = entities.iterator();
                currentOffset = 0;
                currentIterPosition = 0;
            } else {
                // we need to ensure that the response is read to free up the connection
                if (response.getEntity() != null) {
                    response.getEntity().consumeContent();
                }
            }
        } else {
            // Check to see if the current iterator has expired, and if we should obtain more results.
            //
            // - If the current iterator hasn't expired, we just return the result of currentItr.next().
            // - If the current iterator has expired, then check to see if there are more results available:
            //     -- The current result offset plus the number of results held by the iterator will
            //        be less than the total number of results
            if (!currentItr.hasNext()) {
                final int currentHitCount = currentOffset + currentItrSize + searchOffset;
                log.trace("Current hit count: {} (offset: {}, iterator size: {}, total hits: {})",
                        new Object[] {currentHitCount, currentOffset, currentItrSize, totalHits} );
                if ((currentHitCount < totalHits)) {
                    final HttpResponse response = performQuery(currentHitCount, maxResults);
                    final Set<DcsEntity> entities = parseQueryResponse(response);

                    // Set state
                    currentItrSize = entities.size();
                    currentItr = entities.iterator();
                    currentOffset = currentHitCount;
                    currentIterPosition = 0;
                    log.trace("Updated iterator state: offset: {} iterator size: {}", currentOffset, currentItrSize);
                }
            }
        }
    }

    /**
     * Performs a search query using {@link #query}, using the supplied offset and max.
     *
     * @param offset the offset into the search results
     * @param max    the max number of results to return
     * @return the HttpResponse
     * @throws IOException              if an error occurs executing the query
     * @throws IllegalArgumentException if <code>offset</code> is less than zero
     */
    private HttpResponse performQuery(int offset, int max) throws IOException {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be less than zero.");
        }

        final String queryUrl;
        if (max < 1) {
            queryUrl = String.format(SEARCH_QUERY_URL_OFFSET, config.getAccessHttpUrl() + "/query", QueryUtil.encodeURLPath(query), offset);
        } else {
            queryUrl = String.format(SEARCH_QUERY_URL_OFFSET_AND_MAX, config.getAccessHttpUrl() + "/query", QueryUtil.encodeURLPath(query), offset, max);
        }

        log.trace("Executing encoded query: [{}] (decoded query: [{}])", queryUrl, query);
        final HttpResponse response = httpclient.execute(new HttpGet(queryUrl));
        if (response.getStatusLine().getStatusCode() != 200) {
            // read the entity stream if it exists to free up the connection
            if (response.getEntity() != null) {
                response.getEntity().consumeContent();
            }
            throw new HttpIoException("HttpResponse object does not contain a successful response (wanted: 200, was: "
                    + response.getStatusLine().getStatusCode() + " for query " + queryUrl + ")");
        }

        return response;
    }

    /**
     * Examines the response of a search result, and extracts the returned entities.
     *
     * @param response the HttpResponse object
     * @return the set of entities contained in the response
     * @throws InvalidXmlException if the response contains invalid DCP xml
     * @throws IOException         if there is a problem obtaining the response's content
     */
    private Set<DcsEntity> parseQueryResponse(HttpResponse response) throws InvalidXmlException, IOException {
        if (response == null) {
            throw new IllegalArgumentException("HttpResponse must not be null");
        }

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IllegalArgumentException("HttpResponse object does not contain a successful response.");
        }

        final InputStream responseIn = response.getEntity().getContent();
        final Dcp results = builder.buildSip(responseIn);
        final Set<DcsEntity> entities = new HashSet<DcsEntity>();
        entities.addAll(results.getCollections());
        entities.addAll(results.getDeliverableUnits());
        entities.addAll(results.getFiles());
        entities.addAll(results.getManifestations());
        entities.addAll(results.getEvents());
        response.getEntity().consumeContent();
        responseIn.close();
        return entities;
    }

    /**
     * Resets the state of the iterator, as if it had just been constructed.
     */
    private void reset() {
        currentItr = null;
        currentItrSize = -1;
        currentOffset = -1;
        totalHits = -1;
        currentIterPosition = 0;
    }

    /**
     * Not supported.  Always throws UnsupportedOperationException.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported for " + this.getClass().getName());
    }

    @Override
    public String toString() {
        return "HttpDcsSearchIterator{" +
                "config=" + config +
                ", query='" + query + '\'' +
                ", currentItrSize=" + currentItrSize +
                ", currentOffset=" + currentOffset +
                ", totalHits=" + totalHits +
                ", currentItr=" + currentItr +
                '}';
    }
}
