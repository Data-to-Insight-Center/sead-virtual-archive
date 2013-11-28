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

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.dataconservancy.ui.dao.DisciplineDAO;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.stripes.UiConfigurationActionBean;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.dataconservancy.ui.stripes.UiConfigurationActionBean.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The request object for listing Metadata Formats that are in the system.  It uses JSoup (along with other
 * collaborating objects) to parse the HTML and return the
 * {@link org.dataconservancy.ui.stripes.UiConfigurationActionBean.MetaDataFormatTransport} objects present in the
 * response.
 */
public class ListMetadataFormatRequest {

    /** The div id of the table containing the listing of Metadata Formats */
    private static final String METADATA_FORMAT_TABLE = "metadataFormatTable";

    /** The {@link #METADATA_FORMAT_TABLE} column index that contains the Metadata Format name. */
    private static final int MDF_NAME_INDEX = 0;

    /** The {@link #METADATA_FORMAT_TABLE} column index that contains the Metadata Format version. */
    private static final int MDF_VERSION_INDEX = 1;

    /** The {@link #METADATA_FORMAT_TABLE} column index that contains the Metadata Format schema URL. */
    private static final int MDF_SCHEMA_URL_INDEX = 2;

    /** The {@link #METADATA_FORMAT_TABLE} column index that contains the Metadata Format project flag. */
    private static final int MDF_APPLIES_TO_PROJECT_INDEX = 3;

    /** The {@link #METADATA_FORMAT_TABLE} column index that contains the Metadata Format collection flag. */
    private static final int MDF_APPLIES_TO_COLLECTION_INDEX = 4;

    /** The {@link #METADATA_FORMAT_TABLE} column index that contains the Metadata Format item flag. */
    private static final int MDF_APPLIES_TO_ITEM_INDEX = 5;

    /** The {@link #METADATA_FORMAT_TABLE} column index that contains the Metadata Format validates flag. */
    private static final int MDF_VALIDATES_INDEX = 7;

    /**
     * The {@link #METADATA_FORMAT_TABLE} column index that contains the Metadata Format discipline titles.  These are
     * turned into discipline ids by the {@link #disciplineDao}
     */
    private static final int MDF_DISCIPLINE_TITLE_INDEX = 6;

    /** The UI URL configuration */
    private final UiUrlConfig urlConfig;

    /** DAO for looking up Discipline objects */
    private final DisciplineDAO disciplineDao;

    public ListMetadataFormatRequest(UiUrlConfig urlConfig, DisciplineDAO disciplineDao) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UrlConfig must not be null.");
        }
        if (disciplineDao == null) {
            throw new IllegalArgumentException("Discipline DAO must not be null.");
        }

        this.urlConfig = urlConfig;
        this.disciplineDao = disciplineDao;
    }

    public HttpGet asHttpGet() {
        final String url = urlConfig.getListMetadataFormatUrl().toExternalForm();
        return new HttpGet(url);
    }

    /**
     * A convenience method for {@link #listFormats(java.io.InputStream)}.  This method retrieves the content referenced
     * by {@link UiUrlConfig#getListMetadataFormatUrl()}, then forwards to {@link #listFormats(java.io.InputStream)}.
     *
     * @param hc the HttpClient used to retrieve the HTML listing the metadata formats
     * @return a {@code List}, in document order, of the Metadata Formats in the system
     */
    public List<UiConfigurationActionBean.MetaDataFormatTransport> listFormats(HttpClient hc) {
        HttpAssert.ResponseHolder holder = new HttpAssert.ResponseHolder();
        final String url = this.asHttpGet().getURI().toString();
        HttpAssert.assertStatus(hc, url, 200, "Unable to list metadata formats!",
                holder);

        return listFormats(holder.getBody());
    }

    /**
     * Attempts to parse the HTML supplied by {@code htmlBody} into
     * {@link org.dataconservancy.ui.stripes.UiConfigurationActionBean.MetaDataFormatTransport} objects.  Typically the
     * {@code htmlBody} will be produced by retrieving the content of {@link UiUrlConfig#getListMetadataFormatUrl()
     * listing metadata formats}.
     * <p/>
     * JSoup is used internally to parse the HTML into a DOM tree.  The &lt;table> is retrieved by selecting the div
     * identifier {@link #METADATA_FORMAT_TABLE}, and each row in the table parsed and converted to a
     * {@link org.dataconservancy.ui.stripes.UiConfigurationActionBean.MetaDataFormatTransport}.
     *
     * @param htmlBody the InputStream containing an HTML document with Metadata Formats
     * @return a {@code List}, in document order, of the Metadata Formats in the system
     */
    public List<UiConfigurationActionBean.MetaDataFormatTransport> listFormats(InputStream htmlBody) {
        final List<UiConfigurationActionBean.MetaDataFormatTransport> results =
                new ArrayList<UiConfigurationActionBean.MetaDataFormatTransport>();

        final Document dom;
        try {
            final String html = IOUtils.toString(htmlBody);
            dom = Jsoup.parse(html);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        final Elements table = dom.select("table#" + METADATA_FORMAT_TABLE);
        assertNotNull(table);
        assertEquals(1, table.size());

        final Elements rows = table.get(0).getElementsByTag("tr");
        assertNotNull(rows);
        assertTrue(rows.size() > 1);

        // Skip the first row, it's a header
        for (int i = 1; i < rows.size(); i++) {
            final Element row = rows.get(i);
            results.add(toMetadataTransport(row));
        }

        return results;
    }

    /**
     * Turns a row from the {@link #METADATA_FORMAT_TABLE} into a
     * {@link org.dataconservancy.ui.stripes.UiConfigurationActionBean.MetaDataFormatTransport} object.  The discipline
     * titles are resolved to discipline identifiers using the {@link #disciplineDao}.
     *
     * @param tableRow a row from the {@link #METADATA_FORMAT_TABLE} that represents a MetaDataFormatTransport object
     * @return the MetaDataFormatTransport object that represents the {@code tableRow}
     */
    private UiConfigurationActionBean.MetaDataFormatTransport toMetadataTransport(Element tableRow) {
        final UiConfigurationActionBean.MetaDataFormatTransport mdft = new UiConfigurationActionBean()
                .getNewMetadataFormatTransport();

        // Table columns are:
        //   0) Name (text)
        //   1) Version (text)
        //   2) Schema URL (an anchor)
        //   3) Project (text)
        //   4) Collection (text)
        //   5) Item (text)
        //   6) TODO: Verify: Disciplines (text?)
        //   7) Validates (text)
        //   8) Actions (anchors, separated by a '|' character)


        final Elements columns = tableRow.getElementsByTag("td");
        assertNotNull("Unable to parse rows from the Metadata Format Table (div id " + METADATA_FORMAT_TABLE + "): " +
                        "No columns were found.", columns);
        assertEquals("Unable to parse rows from the Metadata Format Table (div id " + METADATA_FORMAT_TABLE + "): " +
                "Expected 9 columns, but found " + columns.size(), 9, columns.size());

        mdft.setName(columns.get(MDF_NAME_INDEX).text().trim());
        mdft.setVersion(columns.get(MDF_VERSION_INDEX).text().trim());
        mdft.setSchemaURL(columns.get(MDF_SCHEMA_URL_INDEX).select("a").text().trim());
        mdft.setSchemaSource(columns.get(MDF_SCHEMA_URL_INDEX).select("a").attr("href").trim());

        String applies = columns.get(MDF_APPLIES_TO_PROJECT_INDEX).text().trim();

        if (YES.equalsIgnoreCase(applies)) {
            mdft.setAppliesToProject(YES);
        } else if (NO.equalsIgnoreCase(applies)) {
            mdft.setAppliesToProject(NO);
        } else if (NOT_SPECIFIED.equalsIgnoreCase(applies)) {
            mdft.setAppliesToProject(NOT_SPECIFIED);
        }

        applies = columns.get(MDF_APPLIES_TO_COLLECTION_INDEX).text().trim();

        if (YES.equalsIgnoreCase(applies)) {
            mdft.setAppliesToCollection(YES);
        } else if (NO.equalsIgnoreCase(applies)) {
            mdft.setAppliesToCollection(NO);
        } else if (NOT_SPECIFIED.equalsIgnoreCase(applies)) {
            mdft.setAppliesToCollection(NOT_SPECIFIED);
        }

        applies = columns.get(MDF_APPLIES_TO_ITEM_INDEX).text().trim();

        if (YES.equalsIgnoreCase(applies)) {
            mdft.setAppliesToItem(YES);
        } else if (NO.equalsIgnoreCase(applies)) {
            mdft.setAppliesToItem(NO);
        } else if (NOT_SPECIFIED.equalsIgnoreCase(applies)) {
            mdft.setAppliesToItem(NOT_SPECIFIED);
        }

        applies = columns.get(MDF_VALIDATES_INDEX).text().trim();

        if (YES.equalsIgnoreCase(applies)) {
            mdft.setValidates(YES);
        } else if (NO.equalsIgnoreCase(applies)) {
            mdft.setValidates(NO);
        } else if (NOT_SPECIFIED.equalsIgnoreCase(applies)) {
            mdft.setValidates(NOT_SPECIFIED);
        }

        // Get the text of the disciplines.  For example:
        //  "Biology, Social Science"
        String disciplineText = columns.get(MDF_DISCIPLINE_TITLE_INDEX).text().trim();
        String[] disciplineTitles = new String[] {};
        if (disciplineText.length() > 0) {
            disciplineTitles = Pattern.compile(",").split(disciplineText);
        }

        if (disciplineTitles.length > 0) {
            NEXT_TITLE: for (String disciplineTitle : disciplineTitles) {
                for (Discipline d : disciplineDao.list()) {
                    if (disciplineTitle.equals(d.getTitle())) {
                        mdft.getDisciplineIds().add(d.getId());
                        continue NEXT_TITLE;
                    }
                }
            }
        }

        return mdft;
    }
}
