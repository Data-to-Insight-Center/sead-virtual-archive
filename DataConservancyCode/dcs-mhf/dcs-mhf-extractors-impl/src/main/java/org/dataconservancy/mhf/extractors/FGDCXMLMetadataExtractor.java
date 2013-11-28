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

package org.dataconservancy.mhf.extractors;


import org.dataconservancy.mhf.extractor.api.ExtractionException;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.model.builder.api.AttributeValueBuilder;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.*;
import org.dataconservancy.mhf.representations.AttributeSetMetadataRepresentation;
import org.dataconservancy.mhf.representations.DateTimeRange;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * {@code FGDCXMLMetadataExtractor} is responsible for extracting information from an FGDC formatted metadata file following
 * the specifications of FGDC-STD-001-1998 standard at {@linktourl http://www.fgdc.gov/metadata/csdgm/}.
 * <p/>
 * Fields expexted to be extracted are:
 * <ul>
 *     <li>{@code pubdate} - publication date</li>
 *     <li>{@code timeperd} - time period content of information
 *          <ul>
 *              <li>{@code currentness} - indicates what the associated time period describe. such as "ground condition"
 *                   (how up-to-date the dataset is), or "publication date" - when dataset was publised.</li>
 *              <li>{@code timeinfo} - time values</li>
 *          </ul>
 *          This extractor is currently not extracting information.
 *     </li>
 *     <li>{@code spdom} - geographical domain of the dataset</li>
 *     <li>{@code keyword} - words or phrases summarizing an aspect of the dataset</li>
 *
 * </ul>
 * <p/>
 * FGDC metadata files come in multiple file formats. This implementation handles FGDC metadata express in form of XML file.
 */
public class FGDCXMLMetadataExtractor extends BaseMetadataExtractor {

    static final String SPATIAL_DOMAIN_TAGNAME = "spdom";
    static final String PUBLICATION_DATE_TAGNAME = "pubdate";
    static final String TIME_PERIOD_OF_CONTENT_TAGNAME = "timeperd";
    static final String CURRENTNESS_REFERENCE_TAGNAME = "current";
    static final String PUBLICATION_DATE_CURRENTNESS_TEXT = "publication date";
    static final String GROUND_CONDITION_CURRENTNESS_TEXT = "ground condition";
    static final String TIME_INFO_TAG_NAME = "timeinfo";
    static final String CALENDAR_DATE_TAG_NAME = "caldate";
    static final String BEGINING_DATE_TAG_NAME = "begdate";
    static final String ENDING_DATE_TAG_NAME = "enddate";
    static final String DATE_RANGE_TAG_NAME = "rngdates";
    static final String MULTIPLE_DATE_TAG_NAME = "mdattim";
    static final String SINGLE_DATE_TAG_NAME = "sngdate";
    static final String BOUNDING_BOX_TAG_NAME = "bounding";
    static final String WEST_BOUNDING_COOR_TAG_NAME = "westbc";
    static final String EAST_BOUNDING_COOR_TAG_NAME = "eastbc";
    static final String NORTH_BOUNDING_COOR_TAG_NAME = "northbc";
    static final String SOUTH_BOUNDING_COOR_TAG_NAME = "southbc";
    static final String KEYWORD_TAG_NAME = "keywords";
    static final String THEME_KEYWORD_TAG_NAME = "themekey";
    static final String THEME_THESAURUS_TAG_NAME = "themekt";
    static final String PLACE_KEYWORD_TAG_NAME = "placekey";
    static final String PLACE_THESAURUS_TAG_NAME = "placekt";
    static final String TEMPORAL_KEYWORD_TAG_NAME = "tempkey";
    static final String TEMPORAL_THESAURUS_TAG_NAME = "tempkt";
    static final String STRATUM_KEYWORD_TAG_NAME = "stratkey";
    static final String STRATUM_THESAURUS_TAG_NAME = "stratkt";

    static final DateTimeFormatter fullDateFormatter = DateTimeFormat.forPattern("yyyyMMdd");
    static final DateTimeFormatter yearFormatter = DateTimeFormat.forPattern("yyyy");

    private final AttributeValueBuilder avBuilder;
    private final MetadataObjectBuilder moBuilder;

    public FGDCXMLMetadataExtractor(MetadataObjectBuilder moBuilder, AttributeValueBuilder avBuilder) {
        if (avBuilder == null || moBuilder == null) {
            throw new IllegalArgumentException("Builders must not be null.");
        }

        this.avBuilder = avBuilder;
        this.moBuilder = moBuilder;
    }

    /**
     * FGDC metadata file contains a lot of information, out of which this extractor is expected to extract spatial, temporal,
     * and keywords.
     * @param mi {@link MetadataInstance} and FGDC metadata instance expressed in the form of XML {@link java.io.InputStream}
     * @return a collection {@link MetadataRepresentation} extracted from the {@link MetadataInstance}
     * @throws ExtractionException
     */
    @Override
    public Collection<MetadataRepresentation> extractMetadata(MetadataInstance mi) throws ExtractionException {

        Collection<MetadataRepresentation> extractedMetadata = new ArrayList<MetadataRepresentation>();

        AttributeSet  spatialAttributeSet;
        AttributeSet  temporalAttributeSet;
        AttributeSet  keywordAttributeSet;


        //Input check
        if (mi == null) {
            throw new IllegalArgumentException(NULL_INSTANCE);
        }
        if (!mi.getFormatId().equals(MetadataFormatId.FGDC_XML_FORMAT_ID)) {
            throw new IllegalArgumentException(INVALID_FORMAT_ERROR);
        }

        //prepare working objects
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            // get a builder to create a DOM document
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(mi.getContent());

            spatialAttributeSet = extractSpatialMetadata(document);
            keywordAttributeSet = extractKeywordMetadata(document);
            temporalAttributeSet = extractTemporalMetadata(document, PUBLICATION_DATE_CURRENTNESS_TEXT);

            if (temporalAttributeSet.getAttributes().size() == 0) {
                temporalAttributeSet = extractTemporalMetadata(document, GROUND_CONDITION_CURRENTNESS_TEXT);
            }
            /**************************************************************************************************************
             * PARSING pubdate ELEMENT
             *************************************************************************************************************/
            if (temporalAttributeSet != null) {
                temporalAttributeSet.getAttributes().add(extractPublicationDate(document));
            } else {
                temporalAttributeSet = new MetadataAttributeSet(MetadataAttributeSetName.TEMPORAL_METADATA);
                temporalAttributeSet.getAttributes().add(extractPublicationDate(document));
            }
            /**************************************************************************************************************
             * PARSING pubdate ELEMENT
             *************************************************************************************************************/
            MetadataRepresentation spatialMetadataRepresentation = new AttributeSetMetadataRepresentation(spatialAttributeSet);
            MetadataRepresentation keywordMetadataRepresentation = new AttributeSetMetadataRepresentation(keywordAttributeSet);
            MetadataRepresentation temporalMetadataRepresentation = new AttributeSetMetadataRepresentation(temporalAttributeSet);

            extractedMetadata.add(spatialMetadataRepresentation);
            extractedMetadata.add(temporalMetadataRepresentation);
            extractedMetadata.add(keywordMetadataRepresentation);

            return extractedMetadata;

        } catch (SAXException e) {
            throw new ExtractionException(UNPARSABLE_XML + e.getMessage());
        } catch (IOException e) {
            throw new ExtractionException(FAILED_INPUTSTREAM_READING + e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new ExtractionException("Exception occurred when attempting to parse MetadataInstance InputStream. " + e.getMessage());
        } finally {
            try {
                mi.getContent().close();
            } catch (IOException e) {
                throw new ExtractionException(FAILED_CLOSE + e.getMessage());
            }
        }

    }

    /**
     * Extract keyword values from {@code keyword} section of the document.
     * @param document
     * @return
     */
    private AttributeSet extractKeywordMetadata(Document document) {

        NodeList keywordsNodeList = document.getDocumentElement().getElementsByTagName(KEYWORD_TAG_NAME).item(0).getChildNodes();
        AttributeSet attributeSet = new MetadataAttributeSet(MetadataAttributeSetName.KEYWORD_METADATA);
        Set<Attribute> extractedAttributes = null;
        for (int i = 0; i < keywordsNodeList.getLength(); i++) {
            if (keywordsNodeList.item(i).getNodeName().equals("theme")) {
                //call extract theme keyword method
                extractedAttributes = extractKeywordsMetadata(keywordsNodeList.item(i), THEME_KEYWORD_TAG_NAME, THEME_THESAURUS_TAG_NAME);
            } else if (keywordsNodeList.item(i).getNodeName().equals("place")) {
                //call extract place keyword method
                extractedAttributes = extractKeywordsMetadata(keywordsNodeList.item(i), PLACE_KEYWORD_TAG_NAME, PLACE_THESAURUS_TAG_NAME);
            } else if (keywordsNodeList.item(i).getNodeName().equals("temporal")) {
                //call extract temporal keyword method
                extractedAttributes = extractKeywordsMetadata(keywordsNodeList.item(i), TEMPORAL_KEYWORD_TAG_NAME, TEMPORAL_THESAURUS_TAG_NAME);
            } else if (keywordsNodeList.item(i).getNodeName().equals("stratum")) {
                //call extract stratum keyword method
                extractedAttributes = extractKeywordsMetadata(keywordsNodeList.item(i), STRATUM_KEYWORD_TAG_NAME, STRATUM_THESAURUS_TAG_NAME);
            }

            if (extractedAttributes != null) {
                attributeSet.getAttributes().addAll(extractedAttributes);
                extractedAttributes = null;
            }
        }

        return attributeSet;
    }
    private Set<Attribute> extractKeywordsMetadata(Node placeNode, String keywordTagName, String keywordThesaurusTagname) {
        NodeList childNodes = placeNode.getChildNodes();

        Set<Attribute> attributeSet = new HashSet<Attribute>();
        Attribute tempAttribute = null;

        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeName().equals(keywordTagName)) {
                //TODO: should this be a full text type instead of string?
                tempAttribute = new MetadataAttribute(MetadataAttributeName.KEYWORD, MetadataAttributeType.STRING, childNodes.item(i).getTextContent());
            } else if (childNodes.item(i).getNodeName().equals(keywordThesaurusTagname)) {
                tempAttribute = new MetadataAttribute(MetadataAttributeName.KEYWORD_THESAURUS, MetadataAttributeType.STRING, childNodes.item(i).getTextContent());
            }
            if (tempAttribute != null) {
                attributeSet.add(tempAttribute);
            }
        }
        return attributeSet;
    }

    private AttributeSet extractSpatialMetadata(Document document) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        avBuilder.buildLocation(extractLocation(document),out);
        Attribute spatialAttribute = new MetadataAttribute(MetadataAttributeName.SPATIAL_DOMAIN,
                MetadataAttributeType.LOCATION,
                new String(out.toByteArray()));

        AttributeSet attributeSet = new MetadataAttributeSet(MetadataAttributeSetName.SPATIAL_METADATA);
        attributeSet.getAttributes().add(spatialAttribute);

        return attributeSet;
    }

    /**
     * Produces a named temporal AttributeSet from a Document object. Temporal information is extraction from {@code timeperd}
     * section.
     *
     * @param document
     * @param targetMetadataContent
     * @return
     */
    private AttributeSet extractTemporalMetadata(Document document, String targetMetadataContent) {

        AttributeSet temporalAttributeSet = new MetadataAttributeSet(MetadataAttributeSetName.TEMPORAL_METADATA);


        /**************************************************************************************************************
         * PARSING TIME PERIOD OF CONTENT
         *************************************************************************************************************/
        NodeList timePeriodNodeList = document.getDocumentElement()
                .getElementsByTagName(TIME_PERIOD_OF_CONTENT_TAGNAME).item(0).getChildNodes();

        NodeList timeInfoNodeList = null;
        boolean foundPublicationDate = false;

        for (int i = 0; i < timePeriodNodeList.getLength(); i++) {
            if (timePeriodNodeList.item(i).getNodeName().equals(CURRENTNESS_REFERENCE_TAGNAME) && timePeriodNodeList.item(i).getTextContent().equals(targetMetadataContent)) {
                for (int j = 0; j < timePeriodNodeList.getLength(); j++) {
                    if (timePeriodNodeList.item(j).getNodeName().equals(TIME_INFO_TAG_NAME)) {
                        timeInfoNodeList = timePeriodNodeList.item(j).getChildNodes();
                        break;
                    }
                }
                break;
            }
        }

        DateTime singleDateTime = null;
        DateTimeRange dateTimeRange = null;
        Set<DateTime> multipleDateTime = null;

        //No valid temporal information was found
        if (timeInfoNodeList == null) {
            return null;
        }

        //Temporal information was found
        for (int i = 0; i < timeInfoNodeList.getLength(); i++) {
            if (timeInfoNodeList.item(i).getNodeName().equals(DATE_RANGE_TAG_NAME) ||
                    timeInfoNodeList.item(i).getNodeName().equals(MULTIPLE_DATE_TAG_NAME) ||
                    timeInfoNodeList.item(i).getNodeName().equals(SINGLE_DATE_TAG_NAME)) {
                if (timeInfoNodeList.item(i).getNodeName().equals(SINGLE_DATE_TAG_NAME)) {
                    singleDateTime = parseSingleDateTime(timeInfoNodeList.item(i));
                } else if (timeInfoNodeList.item(i).getNodeName().equals(MULTIPLE_DATE_TAG_NAME)) {
                    multipleDateTime = parseMultipleDateTime(timeInfoNodeList.item(i));
                } else {
                    dateTimeRange = parseDateTimeRange(timeInfoNodeList.item(i));
                }
                break;
            }
        }

        String commonAttributeName = null;
        if (targetMetadataContent.equals(GROUND_CONDITION_CURRENTNESS_TEXT)) {
            commonAttributeName = MetadataAttributeName.DATA_SET_GROUND_CONDITION_TIME;
        } else if (targetMetadataContent.equals(PUBLICATION_DATE_CURRENTNESS_TEXT)) {
            commonAttributeName = MetadataAttributeName.PUBLICATION_DATE;
        }

        if (singleDateTime != null) {
            temporalAttributeSet.getAttributes().add(createSingleDateTimeAttribute(commonAttributeName, singleDateTime));
        } else if (dateTimeRange != null) {
            temporalAttributeSet.getAttributes().add((createDateTimeRangeAttribute(commonAttributeName, dateTimeRange)));
        } else if (multipleDateTime != null) {
            temporalAttributeSet.getAttributes().addAll(createMultipleDateTimeAttribute(commonAttributeName, multipleDateTime));
        }

        /*************************************************************************************************************
         * END PARSING TIME PERIOD OF CONTENT
         *************************************************************************************************************/
        return temporalAttributeSet;
    }

    /**
     * Extracting publication date of the Identification section, under {@code pubdate} tag
     * @param document
     * @return
     */
    private Attribute extractPublicationDate(Document document) {
        MetadataAttribute publicationDateAttribute = new MetadataAttribute();
        publicationDateAttribute.setName(MetadataAttributeName.PUBLICATION_DATE);
        publicationDateAttribute.setType(MetadataAttributeType.DATE_TIME);

        DateTime publicationDate;
        NodeList nodeList = document.getDocumentElement().getElementsByTagName(PUBLICATION_DATE_TAGNAME);
        publicationDate = parseDateTime(nodeList.item(0).getTextContent());
        publicationDateAttribute.setValue(String.valueOf(publicationDate));

        return publicationDateAttribute;
    }

    /**
     * Given a single DateTime object, produces an Attribute representing the date
     * @param attributeName
     * @param singleDateTime
     * @return
     */
    private Attribute createSingleDateTimeAttribute(String attributeName, DateTime singleDateTime) {
        return new MetadataAttribute(attributeName, MetadataAttributeType.DATE_TIME,
                String.valueOf(singleDateTime));
    }

    /**
     * Given a DateTimeRange object, produces an Attribute object representing that date range
     * @param attributeName
     * @param dateTimeRange
     * @return
     */
    private Attribute createDateTimeRangeAttribute(String attributeName, DateTimeRange dateTimeRange) {
        MetadataAttribute dateTimeRangeAttribute = new MetadataAttribute();
        dateTimeRangeAttribute.setName(attributeName);
        dateTimeRangeAttribute.setType(MetadataAttributeType.DATE_TIME_RANGE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        avBuilder.buildDateTimeRange(dateTimeRange, baos);
        dateTimeRangeAttribute.setValue(new String(baos.toByteArray()));
        return dateTimeRangeAttribute;
    }

    /**
     * Given a set of date time object, produces a set of Attributes containing the same information
     * @param attributeName
     * @param multipleDateTime
     * @return
     */
    private Set<Attribute> createMultipleDateTimeAttribute(String attributeName, Set<DateTime> multipleDateTime) {
        Set<Attribute> multipleDateTimeSet = new HashSet<Attribute>();
        MetadataAttribute dateTimeAttribute = null;
        for (DateTime dateTime : multipleDateTime) {
            dateTimeAttribute = new MetadataAttribute(attributeName, MetadataAttributeType.DATE_TIME, String.valueOf(dateTime));
            multipleDateTimeSet.add(dateTimeAttribute);
        }
        return multipleDateTimeSet;
    }

    /**
     * Helper method that parses a node of single date into a DateTime object
     * @param singleDateNode
     * @return
     */
    private DateTime parseSingleDateTime(Node singleDateNode) {
        NodeList singleDateChildNodes = singleDateNode.getChildNodes();
        DateTime singleDate = null;
        for (int i = 0; i < singleDateChildNodes.getLength(); i++) {
            if (singleDateChildNodes.item(i).getNodeName().equals(CALENDAR_DATE_TAG_NAME)) {
                singleDate = parseDateTime(singleDateChildNodes.item(i).getTextContent());
            }
        }
        return singleDate;
    }

    /**
     * Helper method that parses a node of date range into a DateTimeRange object
     * @param dateRangeNode
     * @return
     */
    private DateTimeRange parseDateTimeRange(Node dateRangeNode) {
        NodeList dateRangeChildNodes = dateRangeNode.getChildNodes();
        DateTime startDateTime = null;
        DateTime endDateTime = null;
        for (int i = 0; i < dateRangeChildNodes.getLength(); i++) {
            if (dateRangeChildNodes.item(i).getNodeName().equals(BEGINING_DATE_TAG_NAME)) {
                startDateTime = parseDateTime(dateRangeChildNodes.item(i).getTextContent());
            }
            if (dateRangeChildNodes.item(i).getNodeName().equals(ENDING_DATE_TAG_NAME)) {
                endDateTime = parseDateTime(dateRangeChildNodes.item(i).getTextContent());
            }
        }

        return new DateTimeRange(startDateTime, endDateTime);
    }

    /**
     * Helper method that parses a multiple dates node into a set of DateTime object.
     * @param multipleDateNode
     * @return
     */
    private Set<DateTime> parseMultipleDateTime(Node multipleDateNode) {
        NodeList multipleDateChildNode = multipleDateNode.getChildNodes();
        Set<DateTime> dateTimeSet = new HashSet<DateTime>();
        DateTime tempDate;
        for (int i = 0; i < multipleDateChildNode.getLength(); i++) {
            tempDate = parseSingleDateTime(multipleDateChildNode.item(i));
            if (tempDate != null) {
                dateTimeSet.add(tempDate);
            }
        }
        return dateTimeSet;
    }

    /**
     * Extraction of simple coordinates information. Does not handle G-Polygon coordinate and exclusion.
     * <p/>
     * Spatial information is being extracted from {@code spdom} section.
     *
     *
     * @param document
     * @return
     */
    private Location extractLocation(Document document) throws ExtractionException {
        //Get to list of spatial domain child nodes
        NodeList spatialDomainChildNodesList = document.getDocumentElement().getElementsByTagName(SPATIAL_DOMAIN_TAGNAME).item(0).getChildNodes();

        //Western most bounding point
        Double westBC = null;
        //Eastern most bounding point
        Double eastBC = null;
        //Northern most bounding point
        Double northBC = null;
        //Southern most bounding point
        Double southBC = null;

        for (int i = 0; i < spatialDomainChildNodesList.getLength(); i++) {
            if (spatialDomainChildNodesList.item(i).getNodeName().equalsIgnoreCase(BOUNDING_BOX_TAG_NAME)) {
                //Get to list of child nodes inside of the SIMPLE bounding box
                NodeList boundingBoxChildNodesList = spatialDomainChildNodesList.item(i).getChildNodes();
                for (int j = 0; j < boundingBoxChildNodesList.getLength(); j++) {
                    try {
                        if (boundingBoxChildNodesList.item(j).getNodeName().equalsIgnoreCase(WEST_BOUNDING_COOR_TAG_NAME)) {
                            westBC = new Double(boundingBoxChildNodesList.item(j).getTextContent());
                        } else if (boundingBoxChildNodesList.item(j).getNodeName().equalsIgnoreCase(EAST_BOUNDING_COOR_TAG_NAME)) {
                            eastBC = new Double(boundingBoxChildNodesList.item(j).getTextContent());
                        } else if (boundingBoxChildNodesList.item(j).getNodeName().equalsIgnoreCase(SOUTH_BOUNDING_COOR_TAG_NAME)) {
                            southBC = new Double(boundingBoxChildNodesList.item(j).getTextContent());
                        } else if (boundingBoxChildNodesList.item(j).getNodeName().equalsIgnoreCase(NORTH_BOUNDING_COOR_TAG_NAME)) {
                            northBC = new Double(boundingBoxChildNodesList.item(j).getTextContent());
                        }
                    } catch (NumberFormatException e) {
                        throw new ExtractionException("Unacceptable value encountered when extracting spatial domain values from fgdc metadata file. " + e.getMessage());
                    }
                }
            }
        }

        if (westBC == null || eastBC == null || northBC == null || southBC == null) {
            throw new ExtractionException("All four values for the spatial bounding box are expected. One (or more) is missing. ");
        }

        Point boundingBoxPoint1 = new Point(westBC, southBC);
        Point boundingBoxPoint2 = new Point(eastBC, southBC);
        Point boundingBoxPoint3 = new Point(eastBC, northBC);
        Point boundingBoxPoint4 = new Point(westBC, northBC);

        Geometry geometry = new Geometry(Geometry.Type.POLYGON,
                boundingBoxPoint1, boundingBoxPoint2,
                boundingBoxPoint3, boundingBoxPoint4);

        return new Location(geometry, null);
    }

    private DateTime parseDateTime(String dateTimeString) {
        DateTime resultingDate;
        try {
            resultingDate = fullDateFormatter.parseDateTime(dateTimeString);
        } catch (IllegalArgumentException e) {
            try {
                resultingDate = yearFormatter.parseDateTime(dateTimeString);
            } catch (IllegalArgumentException ex) {
                resultingDate = null;
            }
        }
        return resultingDate;
    }

}
