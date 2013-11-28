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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import org.dataconservancy.ui.model.AdiAjaxTransport;
import org.dataconservancy.ui.model.DataItemTransport;
import org.dataconservancy.ui.model.builder.xstream.AdiAjaxDataItemConverter;
import org.dataconservancy.ui.model.builder.xstream.AdiAjaxDateTimeConverter;
import org.dataconservancy.ui.model.builder.xstream.DataFileConverter;
import org.dataconservancy.ui.model.builder.xstream.DataItemTransportConverter;
import org.dataconservancy.ui.services.DataItemTransportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.*;


/**
 * AJAX endpoints to obtain lists of Data Items to be used for asynchronously updating Data Item listings.
 * Currently the specification of the HTTP API is being tracked <a href="https://scm.dataconservancy.org/confluence/display/IRD/DC-1242+Archive+Deposit+Info+%28ADI%29+AJAX+Endpoint">here</a>
 *
 * @see <a href="https://scm.dataconservancy.org/confluence/display/IRD/DC-1242+Archive+Deposit+Info+%28ADI%29+AJAX+Endpoint">https://scm.dataconservancy.org/confluence/display/IRD/DC-1242+Archive+Deposit+Info+%28ADI%29+AJAX+Endpoint</a>
 */
@Controller
@RequestMapping("/adi")
public class AdiAjaxController {

    protected final static String COLLECTION_TOTAL_NUMBER_DATA_ITEMS = "Total Number of Data Items in %s: %d";
    protected final static String COLLECTION_NO_DATA_ITEMS_FOUND = "No Data Items were found for %s.";
    protected final static String COLLECTION_RETRIEVAL_ERROR = "Error retrieving data sets for %s, please try again.";
    protected final static String COLLECTION_NOT_FOUND = "Collection not found, check to ensure this collection has completed deposit.";

    protected final static String DATA_ITEMS_NOT_UPDATED = "Couldn't update Data Item(s): ";
    protected final static String DATA_ITEMS_RETRIEVAL_ERROR = "Error retrieving Data Item %s, please try again.";
    protected final static String DATA_ITEMS_TOTAL_NUMBER_UPDATED =  "Total Number of Data Items updated: %d";
    protected final static String NO_DATA_ITEMS_UPDATED = "  No Data Items were updated.";
    protected final static String DATA_ITEMS_NOT_PROVIDED = "No Data Items given to update.";
    
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private DataItemTransportService dataItemTransportService;

    private XStream xstream;

    private double maxResultsPerPage = 10;
    
    private int page = 0;
    

    public AdiAjaxController(DataItemTransportService dataItemTransportService) {

        if (dataItemTransportService == null) {
            throw new IllegalArgumentException("Data Item Transport Service must not be null.");
        }

        this.dataItemTransportService = dataItemTransportService;
        
        // Create the xstream instance that will serialize objects into JSON.
        xstream = new XStream( new JsonHierarchicalStreamDriver() {
            public HierarchicalStreamWriter createWriter(Writer writer) {
                return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
            }
        });

        // Register specific object converters to serialize business objects.
        this.xstream.setMode(XStream.NO_REFERENCES);
        this.xstream.registerConverter(new DataItemTransportConverter());
        this.xstream.registerConverter(new AdiAjaxDataItemConverter());
        this.xstream.registerConverter(new DataFileConverter());
        this.xstream.registerConverter(new AdiAjaxDateTimeConverter());
    }

    @RequestMapping(value = "/data-item-transports.json/collection", method = RequestMethod.GET, params = {"id"})
    public void getDataItemTransportsForCollection(@RequestParam String id, HttpServletResponse response)
            throws IOException {
        List<DataItemTransport> dataItemTransportList = new ArrayList<DataItemTransport>();
        String message = null;
        if (id != null && !id.isEmpty()) {
            try {
                dataItemTransportList = this.dataItemTransportService.retrieveDataItemTransportList(id, (int) maxResultsPerPage, (int) maxResultsPerPage * page);
                
                int sizeDataItemTransportList = dataItemTransportList.size();
                
                if (sizeDataItemTransportList > 0) {
                    message = String.format(COLLECTION_TOTAL_NUMBER_DATA_ITEMS, id, sizeDataItemTransportList);
                } else {
                    message = String.format(COLLECTION_NO_DATA_ITEMS_FOUND, id);
                }
            } catch (Exception except) {
                message = String.format(COLLECTION_RETRIEVAL_ERROR, id);
                log.warn(except.getMessage());
            }
        } else {
            message = COLLECTION_NOT_FOUND;
        }

        AdiAjaxTransport adiAjaxTransport = new AdiAjaxTransport(message, dataItemTransportList);
        String jsonResponse = xstream.toXML(adiAjaxTransport);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(CONTENT_LENGTH, String.valueOf(jsonResponse.length()));
        response.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        response.flushBuffer();

        // Send the Response
        final ServletOutputStream servletOutputStream = response.getOutputStream();
        xstream.toXML(adiAjaxTransport, servletOutputStream);
        servletOutputStream.flush();
        servletOutputStream.close();
    }

    @RequestMapping(value = "/data-item-transports.json/data-items", method = RequestMethod.GET, params = "ids")
    public void getDataItemTransportsForDataItems(@RequestParam String ids, HttpServletResponse response)
            throws IOException  {
        StringBuilder messageBuilder = new StringBuilder();
        List<DataItemTransport> dataItemTransportList = new ArrayList<DataItemTransport>();
        if (ids != null && !ids.isEmpty()) {
            List<String> dataItemIdList = Arrays.asList(ids.split(","));
            boolean firstNotUpdated = true;
            for (String dataItemId : dataItemIdList) {
                try {
                    DataItemTransport dataItemTransport = this.dataItemTransportService.retrieveDataItemTransport(dataItemId);
                    
                    if (dataItemTransport != null) {
                        dataItemTransportList.add(dataItemTransport);
                    } else {
                        if (firstNotUpdated) {
                            messageBuilder.append(DATA_ITEMS_NOT_UPDATED);
                            firstNotUpdated = false;
                        } else {
                            messageBuilder.append(", ");
                        }
                        messageBuilder.append("\"" + dataItemId + "\"");
                    }
                } catch (Exception except) {
                    messageBuilder.append(String.format(DATA_ITEMS_RETRIEVAL_ERROR, dataItemId));
                    log.warn(except.getMessage());
                }
            }

            int sizeDataItemTransportList = dataItemTransportList.size();

            if (sizeDataItemTransportList > 0) {
                messageBuilder.append(String.format(DATA_ITEMS_TOTAL_NUMBER_UPDATED, sizeDataItemTransportList));
            } else {
                messageBuilder.append(NO_DATA_ITEMS_UPDATED);
            }
        } else {
            messageBuilder.append(DATA_ITEMS_NOT_PROVIDED);
        }
        AdiAjaxTransport adiAjaxTransport = new AdiAjaxTransport(messageBuilder.toString(), dataItemTransportList);
        String jsonResponse = xstream.toXML(adiAjaxTransport);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(CONTENT_LENGTH, String.valueOf(jsonResponse.length()));
        response.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        response.flushBuffer();

        // Send the Response
        final ServletOutputStream servletOutputStream = response.getOutputStream();
        xstream.toXML(adiAjaxTransport, servletOutputStream);
        servletOutputStream.flush();
        servletOutputStream.close();
    }

    public double getMaxResultsPerPage() {
        return maxResultsPerPage;
    }

    public void setMaxResultsPerPage(double maxResultsPerPage) {
        this.maxResultsPerPage = maxResultsPerPage;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
