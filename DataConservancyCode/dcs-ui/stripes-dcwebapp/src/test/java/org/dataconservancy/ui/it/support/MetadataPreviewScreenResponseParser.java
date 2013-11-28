/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.ui.it.support;

import org.apache.tika.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class MetadataPreviewScreenResponseParser {

    private static final String FORM_CSS_CLASS = "CollectionMetadataFileForm";

    private static final String PARENT_ID_INPUT_NAME = "parentID";

    private static final String REDIRECT_URL_INPUT_NAME = "redirectUrl";

    private String parentId;

    private String redirectUrl;

    private String filename;

    private List<String> successEvents = new ArrayList<String>();

    private List<String> failureEvents = new ArrayList<String>();
    
    private String message = "";
    
    private List<String> geoSpatialAttributes = new ArrayList<String>();
    
    private List<String> temporalAttributes = new ArrayList<String>();

    public MetadataPreviewScreenResponseParser(InputStream responseContent) throws IOException {
        final Document dom = Jsoup.parse(IOUtils.toString(responseContent));

        Elements elements = dom.select("form." + FORM_CSS_CLASS);
        assertEquals("Unexpected number of form elements!", 1, elements.size());

        final Element form = elements.get(0);
        elements = form.getElementsByTag("input");
        for (Element e : elements) {
            if (PARENT_ID_INPUT_NAME.equals(e.attr("name"))) {
                parentId = e.attr("value");
            }

            if (REDIRECT_URL_INPUT_NAME.equals(e.attr("name"))) {
                redirectUrl = e.attr("value");
            }
        }

        elements = form.select("div#fileName div.metadataPreviewRight");
        assertEquals("Unexpected number of form elements!", 1, elements.size());
        filename = elements.get(0).text().trim();

        elements = form.select("div#validation");
        elements = elements.select("span.success");
        for (Element e : elements) {
            successEvents.add(e.text().trim());
        }
        
        elements = form.select("div#validation div.metadataPreviewRight");
        elements = elements.select("span.error");
        for (Element e : elements) {
            failureEvents.add(e.text().trim());
        }        
        
        elements = form.select("div#spatial_coverage div.metadataPreviewCoverageIndented");
        for (Element e : elements) {
            geoSpatialAttributes.add(e.text().trim());
        }
        
        elements = form.select("div#temporal_coverage div.metadataPreviewCoverageIndented");
        for (Element e : elements) {
            temporalAttributes.add(e.text().trim());
        }
        
        elements = form.select("div#center-message-id");
        if (!elements.isEmpty()) {
            message = elements.get(0).text().trim();
        }
    }

    public List<String> getFailureEvents() {
        return failureEvents;
    }

    public String getFilename() {
        return filename;
    }

    public String getParentId() {
        return parentId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public List<String> getSuccessEvents() {
        return successEvents;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<String> getGeoSpatialAttributes() {
        return geoSpatialAttributes;
    }
    
    public List<String> getTemporalAttributes() {
        return temporalAttributes;
    }
}
