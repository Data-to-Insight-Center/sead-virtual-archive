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
package org.dataconservancy.transform.dryvalleys;

import java.net.URI;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.transform.Output;

import junit.framework.Assert;

public class DryValleyCollectionMappingTest {
    private static final String XML = 
    "<DryValleyCollection version=\"1.0\" xmlns:gmi=\"http://eden.ign.fr/xsd/isotc211/isofull/20090316/gmi/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:gts=\"http://www.isotc211.org/2005/gts\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">\n" +
        "<gmd:language>\n" +
            "<gco:CharacterString>eng; usa</gco:CharacterString>\n" +
        "</gmd:language>\n" +
        "<gmd:characterSet>\n" +
            "<gmd:MD_CharacterSetCode codeListValue=\"UTF8\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_CharacterSetCode\">UTF8</gmd:MD_CharacterSetCode>\n" +
        "</gmd:characterSet>\n" +
        "<gmd:hierarchyLevel>\n" + 
            "<gmd:MD_ScopeCode codeListValue=\"dataset\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_ScopeCode\">dataset</gmd:MD_ScopeCode>\n" +
        "</gmd:hierarchyLevel>\n" +
        "<gmd:hierarchyLevelName>\n" +
            "<gco:CharacterString>Igneous Petrology of the McMurdo Dry Valleys of Antarctica, 1993-2008</gco:CharacterString>\n" +
        "</gmd:hierarchyLevelName>\n" +
        "<gmd:contact>\n" +
            "<gmd:CI_ResponsibleParty>\n" +
                "<gmd:individualName>\n" +
                    "<gco:CharacterString>Bruce D. Marsh</gco:CharacterString>\n" + 
                "</gmd:individualName>\n" +
                "<gmd:organisationName>\n" +
                    "<gco:CharacterString>Department of Earth and Planetary Sciences, The Johns Hopkins University</gco:CharacterString>\n" +
                "</gmd:organisationName>\n" +
                "<gmd:positionName>\n" +
                    "<gco:CharacterString>Professor of Earth and Planetary Sciences</gco:CharacterString>\n" +
                "</gmd:positionName>\n" +
                "<gmd:contactInfo>\n" +
                    "<gmd:CI_Contact>\n" +
                        "<gmd:phone>\n" +
                            "<gmd:CI_Telephone>\n" +
                                "<gmd:voice>\n" +
                                    "<gco:CharacterString/>\n" +
                                "</gmd:voice>\n" +
                            "</gmd:CI_Telephone>\n" +
                        "</gmd:phone>\n" +
                        "<gmd:address>\n" +
                            "<gmd:CI_Address>\n" +
                                "<gmd:deliveryPoint>\n" +
                                    "<gco:CharacterString>Department of Earth and Planetary Sciences, The Johns Hopkins University</gco:CharacterString>\n" +
                                "</gmd:deliveryPoint>\n" +
                                "<gmd:city>\n" +
                                    "<gco:CharacterString>Baltimore</gco:CharacterString>\n" +
                                "</gmd:city>\n" +
                                "<gmd:administrativeArea>\n" +
                                    "<gco:CharacterString>MD</gco:CharacterString>\n" +
                                "</gmd:administrativeArea>\n" +
                                "<gmd:postalCode>\n" +
                                    "<gco:CharacterString>21213</gco:CharacterString>\n" +
                                "</gmd:postalCode>\n" +
                                "<gmd:country>\n" +
                                    "<gco:CharacterString>USA</gco:CharacterString>\n" +
                                "</gmd:country>\n" +
                                "<gmd:electronicMailAddress>\n" + 
                                    "<gco:CharacterString/>\n" +
                                "</gmd:electronicMailAddress>\n" +
                            "</gmd:CI_Address>\n" +
                        "</gmd:address>\n" +
                        "<gmd:onlineResource>\n" +
                            "<gmd:CI_OnlineResource>\n" +
                                "<gmd:linkage>\n" +
                                    "<gmd:URL/>\n" +
                                "</gmd:linkage>\n" +
                            "</gmd:CI_OnlineResource>\n" +
                        "</gmd:onlineResource>\n" +
                    "</gmd:CI_Contact>\n" +
                "</gmd:contactInfo>\n" +
                "<gmd:role>\n" +
                    "<gmd:CI_RoleCode codeListValue=\"principalInvestigator\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_RoleCode\">principalInvestigator</gmd:CI_RoleCode>\n" +
                "</gmd:role>\n" +
            "</gmd:CI_ResponsibleParty>\n" +
        "</gmd:contact>\n" +
        "<gmd:dateStamp>\n" +
            "<gco:Date>2011-07-29</gco:Date>\n" +
        "</gmd:dateStamp>\n" +
        "<gmd:metadataStandardName>\n" +
            "<gco:CharacterString>ISO 19115 Geographic Information - Metadata First Edition</gco:CharacterString>\n" +
        "</gmd:metadataStandardName>\n" +
        "<gmd:metadataStandardVersion>\n" +
            "<gco:CharacterString>ISO 19115:2003</gco:CharacterString>\n" +
        "</gmd:metadataStandardVersion>\n" +
        "<gmd:dataSetURI>\n" +
            "<gco:CharacterString/>\n" +
        "</gmd:dataSetURI>\n" +
        "<gmd:identificationInfo>\n" +
            "<gmd:MD_DataIdentification>\n" +
                "<gmd:citation>\n" +
                    "<gmd:CI_Citation>\n" +
                        "<gmd:title>\n" +
                            "<gco:CharacterString>Igneous Petrology of the McMurdo Dry Valleys of Antarctica, 1993-2008</gco:CharacterString>\n" +
                        "</gmd:title>\n" +
                        "<gmd:date>\n" +
                            "<gmd:CI_Date>\n" +
                                "<gmd:date>\n" +
                                    "<gco:Date>2011</gco:Date>\n" +
                                "</gmd:date>\n" +
                                "<gmd:dateType>\n" +
                                    "<gmd:CI_DateTypeCode codeListValue=\"publication\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode\">publication</gmd:CI_DateTypeCode>\n" +
                                "</gmd:dateType>\n" +
                            "</gmd:CI_Date>\n" +
                        "</gmd:date>\n" +
                        "<gmd:editionDate>\n" +
                            "<gco:Date>2011-07-29</gco:Date>\n" +
                        "</gmd:editionDate>\n" +
                        "<gmd:identifier>\n" +
                            "<gmd:MD_Identifier>\n" +
                                "<gmd:code>\n" +
                                    "<gco:CharacterString/>\n" +
                                "</gmd:code>\n" +
                            "</gmd:MD_Identifier>\n" +
                        "</gmd:identifier>\n" +
                    "</gmd:CI_Citation>\n" +
                "</gmd:citation>\n" +
                "<gmd:abstract>\n" +
                    "<gco:CharacterString>This collection is comprised of data collected by Professor Bruce D. Marsh of the Department of Earth and Planetary Sciences at The Johns Hopkins University while doing fieldwork on the magmatic system of the McMurdo Dry Valleys of Antarctica. Field seasons included here are 1993 through 2008. This collection contains images of individual rock samples, chemical analyses of select rock samples, field photos, and field notes. The core of the collection consists of 2300+ images of rock samples and thousands of field photos. References to locations (longitude/latitude/altitude) in this collection are based on the WGS84 standard.</gco:CharacterString>\n" +
                "</gmd:abstract>\n" +
                "<gmd:purpose>\n" +
                    "<gco:CharacterString>Scientific Research</gco:CharacterString>\n" +
                "</gmd:purpose>\n" +
                "<gmd:credit>\n" +
                    "<gco:CharacterString/>\n" +
                "</gmd:credit>\n" +
                "<gmd:status>\n" +
                    "<gmd:MD_ProgressCode codeListValue=\"historicalArchive\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_ProgressCode\">historicalArchive</gmd:MD_ProgressCode>\n" +
                "</gmd:status>\n"+
                "<gmd:resourceMaintenance>\n" +
                    "<gmd:MD_MaintenanceInformation>\n" +
                        "<gmd:maintenanceAndUpdateFrequency>\n" +
                            "<gmd:MD_MaintenanceFrequencyCode codeListValue=\"notPlanned\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_MaintenanceFrequencyCode\">notPlanned</gmd:MD_MaintenanceFrequencyCode>\n" +
                        "</gmd:maintenanceAndUpdateFrequency>\n" +
                        "<gmd:updateScope>\n" +
                            "<gmd:MD_ScopeCode codeListValue=\"dataset\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_ScopeCode\">dataset</gmd:MD_ScopeCode>\n" +
                        "</gmd:updateScope>\n" +
                        "<gmd:contact>\n" +
                            "<gmd:CI_ResponsibleParty>\n" +
                                "<gmd:individualName>\n" +
                                    "<gco:CharacterString>Keith Kaneda</gco:CharacterString>\n" +
                                "</gmd:individualName>\n" +
                                "<gmd:organisationName>\n" +
                                    "<gco:CharacterString>Digital Research and Curation Center</gco:CharacterString>\n" +
                                "</gmd:organisationName>\n" +
                                "<gmd:positionName>\n" +
                                    "<gco:CharacterString>Data Scientist</gco:CharacterString>\n" +
                                "</gmd:positionName>\n" +
                                "<gmd:contactInfo>\n" +
                                    "<gmd:CI_Contact>\n" +
                                        "<gmd:phone>\n" +
                                            "<gmd:CI_Telephone>\n" +
                                                "<gmd:voice>\n" +
                                                    "<gco:CharacterString>+1 (410) 516-7741</gco:CharacterString>\n" +
                                                "</gmd:voice>\n" +
                                            "</gmd:CI_Telephone>\n" +
                                        "</gmd:phone>\n" +
                                        "<gmd:address>\n" +
                                            "<gmd:CI_Address>\n" +
                                                "<gmd:deliveryPoint>\n" +
                                                    "<gco:CharacterString>Digital Research and Curation Center, The Sheridan Libraries, The Johns Hopkins University</gco:CharacterString>\n" +
                                                "</gmd:deliveryPoint>\n" +
                                                "<gmd:city>\n" +
                                                    "<gco:CharacterString>Baltimore</gco:CharacterString>\n" +
                                                "</gmd:city>\n" +
                                                "<gmd:administrativeArea>\n" +
                                                    "<gco:CharacterString>MD</gco:CharacterString>\n" +
                                                "</gmd:administrativeArea>\n" +
                                                "<gmd:postalCode>\n" +
                                                    "<gco:CharacterString>21213</gco:CharacterString>\n" +
                                                "</gmd:postalCode>\n" +
                                                "<gmd:country>\n" +
                                                    "<gco:CharacterString>USA</gco:CharacterString>\n" +
                                                "</gmd:country>\n" +
                                                "<gmd:electronicMailAddress>\n" +
                                                    "<gco:CharacterString>kaneda@jhu.edu</gco:CharacterString>\n" +
                                                "</gmd:electronicMailAddress>\n" +
                                            "</gmd:CI_Address>\n" +
                                        "</gmd:address>\n" +
                                        "<gmd:onlineResource>\n" +
                                            "<gmd:CI_OnlineResource>\n" +
                                                "<gmd:linkage>\n" +
                                                    "<gmd:URL/>\n" +
                                                "</gmd:linkage>\n" +
                                            "</gmd:CI_OnlineResource>\n" +
                                        "</gmd:onlineResource>\n" +
                                    "</gmd:CI_Contact>\n" +
                                "</gmd:contactInfo>\n" +
                                "<gmd:role>\n" +
                                    "<gmd:CI_RoleCode codeListValue=\"pointOfContact\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_RoleCode\">pointOfContact</gmd:CI_RoleCode>\n" +
                                "</gmd:role>\n" +
                            "</gmd:CI_ResponsibleParty>\n" +
                        "</gmd:contact>\n" +
                    "</gmd:MD_MaintenanceInformation>\n" +
                "</gmd:resourceMaintenance>\n" +
                "<gmd:descriptiveKeywords>\n" +
                    "<gmd:MD_Keywords>\n" +
                        "<gmd:keyword>\n" +
                            "<gco:CharacterString>Continent > Antarctica</gco:CharacterString>\n" +
                        "</gmd:keyword>\n" +
                        "<gmd:type>\n" +
                            "<gmd:MD_KeywordTypeCode codeListValue=\"place\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_KeywordTypeCode\">place</gmd:MD_KeywordTypeCode>\n" +
                        "</gmd:type>\n" +
                        "<gmd:thesaurusName>\n" +
                            "<gmd:CI_Citation>\n" +
                                "<gmd:title>\n" +
                                    "<gco:CharacterString>NASA / GCMD Location Keywords</gco:CharacterString>\n" +
                                "</gmd:title>\n" +
                                "<gmd:date>\n" +
                                    "<gmd:CI_Date>\n" +
                                        "<gmd:date>\n" + 
                                            "<gco:Date>2007</gco:Date>\n" +
                                        "</gmd:date>\n" +
                                        "<gmd:dateType>\n" +
                                            "<gmd:CI_DateTypeCode codeListValue=\"revision\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode\">revision</gmd:CI_DateTypeCode>\n" +
                                        "</gmd:dateType>\n" +
                                    "</gmd:CI_Date>\n" +
                                "</gmd:date>\n" +
                            "</gmd:CI_Citation>\n" +
                        "</gmd:thesaurusName>\n" +
                    "</gmd:MD_Keywords>\n" +
                "</gmd:descriptiveKeywords>\n" +
                "<gmd:descriptiveKeywords>\n" +
                    "<gmd:MD_Keywords>\n" +
                        "<gmd:keyword>\n" +
                            "<gco:CharacterString>EARTH SCIENCE > Solid Earth > Rocks/Minerals > Igneous Rocks</gco:CharacterString>\n" +
                        "</gmd:keyword>\n" +
                        "<gmd:keyword>\n" +
                            "<gco:CharacterString>EARTH SCIENCE > Solid Earth > Volcanoes > Magma</gco:CharacterString>\n" +
                        "</gmd:keyword>\n" +
                        "<gmd:keyword>\n" +
                            "<gco:CharacterString>EARTH SCIENCE > Solid Earth > Geomorphology > Glacial Landforms/Processes</gco:CharacterString>\n" +
                        "</gmd:keyword>\n" +
                        "<gmd:type>\n" +
                            "<gmd:MD_KeywordTypeCode codeListValue=\"discipline\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_KeywordTypeCode\">discipline</gmd:MD_KeywordTypeCode>\n" +
                        "</gmd:type>\n" +
                        "<gmd:thesaurusName>\n" +
                            "<gmd:CI_Citation>\n" +
                                "<gmd:title>\n" +
                                    "<gco:CharacterString>NASA / GCMD Science Keywords</gco:CharacterString>\n" +
                                "</gmd:title>\n" +
                                "<gmd:date>\n" +
                                    "<gmd:CI_Date>\n" +
                                        "<gmd:date>\n" +
                                            "<gco:Date>2007</gco:Date>\n" +
                                        "</gmd:date>\n" +
                                        "<gmd:dateType>\n" +
                                            "<gmd:CI_DateTypeCode codeListValue=\"revision\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode\">revision</gmd:CI_DateTypeCode>\n" +
                                        "</gmd:dateType>\n" +
                                    "</gmd:CI_Date>\n" +
                                "</gmd:date>\n" +
                            "</gmd:CI_Citation>\n" +
                        "</gmd:thesaurusName>\n" +
                    "</gmd:MD_Keywords>\n" +
                "</gmd:descriptiveKeywords>\n" +
                "<gmd:descriptiveKeywords>\n" +
                    "<gmd:MD_Keywords>\n" +
                        "<gmd:keyword>\n" +
                            "<gco:CharacterString>Magma</gco:CharacterString>\n" +
                        "</gmd:keyword>\n" +
                        "<gmd:keyword>\n" +
                            "<gco:CharacterString>Volcano</gco:CharacterString>\n" +
                        "</gmd:keyword>\n" +
                        "<gmd:keyword>\n" +
                            "<gco:CharacterString>Antarctica</gco:CharacterString>\n" +
                        "</gmd:keyword>\n" +
                        "<gmd:type>\n" +
                            "<gmd:MD_KeywordTypeCode codeListValue=\"theme\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_KeywordTypeCode\">theme</gmd:MD_KeywordTypeCode>\n" +
                        "</gmd:type>\n" +
                        "<gmd:thesaurusName>\n" +
                        "</gmd:thesaurusName>\n" +
                    "</gmd:MD_Keywords>\n" +
                "</gmd:descriptiveKeywords>\n" +
                "<gmd:resourceConstraints>\n" +
                    "<gmd:MD_LegalConstraints>\n" +
                        "<gmd:accessConstraints>\n" +
                            "<gmd:MD_RestrictionCode codeListValue=\"otherRestrictions\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_RestrictionCode\">otherRestrictions</gmd:MD_RestrictionCode>\n" +
                        "</gmd:accessConstraints>\n" +
                        "<gmd:useConstraints>\n" +
                            "<gmd:MD_RestrictionCode codeListValue=\"otherRestrictions\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_RestrictionCode\">otherRestrictions</gmd:MD_RestrictionCode>\n" +
                        "</gmd:useConstraints>\n" +
                        "<gmd:otherConstraints>\n" +
                            "<gco:CharacterString>Access Constraints : none</gco:CharacterString>\n" +
                        "</gmd:otherConstraints>\n" +
                        "<gmd:otherConstraints>\n" +
                            "<gco:CharacterString>Use Constraints : Dataset licensed under Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0) </gco:CharacterString>\n" +
                        "</gmd:otherConstraints>\n" +
                    "</gmd:MD_LegalConstraints>\n" +
                "</gmd:resourceConstraints>\n" +
                "<gmd:language>\n" +
                    "<gco:CharacterString>eng; usa</gco:CharacterString>\n" +
                "</gmd:language>\n" +
                "<gmd:characterSet>\n" + 
                    "<gmd:MD_CharacterSetCode codeListValue=\"UTF8\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_CharacterSetCode\">UTF8</gmd:MD_CharacterSetCode>\n" +
                "</gmd:characterSet>\n" +
                "<gmd:topicCategory>\n" +
                    "<gmd:MD_TopicCategoryCode>geoscientificInformation</gmd:MD_TopicCategoryCode>\n" +
                "</gmd:topicCategory>\n" +
                "<gmd:extent>\n" +
                    "<gmd:EX_Extent id=\"boundingExtent\">\n" +
                        "<gmd:geographicElement>\n" +
                            "<gmd:EX_GeographicBoundingBox id=\"boundingGeographicBoundingBox\">\n" +
                                "<gmd:westBoundLongitude>\n" +
                                    "<gco:Decimal>160</gco:Decimal>\n" +
                                "</gmd:westBoundLongitude>\n" +
                                "<gmd:eastBoundLongitude>\n" +
                                    "<gco:Decimal>170</gco:Decimal>\n" +
                                "</gmd:eastBoundLongitude>\n" +
                                "<gmd:southBoundLatitude>\n" +
                                    "<gco:Decimal>-77</gco:Decimal>\n" +
                                "</gmd:southBoundLatitude>\n" +
                                "<gmd:northBoundLatitude>\n" +
                                    "<gco:Decimal>-78</gco:Decimal>\n" +
                                "</gmd:northBoundLatitude>\n" +
                            "</gmd:EX_GeographicBoundingBox>\n" +
                        "</gmd:geographicElement>\n" +
                        "<gmd:temporalElement>\n" +
                            "<gmd:EX_TemporalExtent id=\"boundingTemporalExtent\">\n" +
                                "<gmd:extent>\n" +
                                    "<gmd:TimePeriod>\n" +
                                        "<gmd:beginPosition>19930101</gmd:beginPosition>\n" +
                                        "<gmd:endPosition>20081231</gmd:endPosition>\n" +
                                    "</gmd:TimePeriod>\n" +
                                "</gmd:extent>\n" +
                            "</gmd:EX_TemporalExtent>\n" +
                        "</gmd:temporalElement>\n" +
                    "</gmd:EX_Extent>\n" +
                "</gmd:extent>\n" +
            "</gmd:MD_DataIdentification>\n" +
        "</gmd:identificationInfo>\n" +
        "<gmd:distributionInfo>\n" +
            "<gmd:MD_Distribution>\n" +
                "<gmd:distributionFormat>\n" +
                    "<gmd:MD_Format>\n" +
                        "<gmd:name>\n" +
                            "<gco:CharacterString>JPEG, TIFF, DOC, XML, XLSX, PPTX, PDF, MXD</gco:CharacterString>\n" +
                        "</gmd:name>\n" +
                        "<gmd:version>\n" +
                            "<gco:CharacterString/>\n" +
                        "</gmd:version>\n" +
                        "<gmd:specification>\n" +
                            "<gco:CharacterString> Online Internet (HTTP)</gco:CharacterString>\n" +
                        "</gmd:specification>\n" +
                    "</gmd:MD_Format>\n" +
                "</gmd:distributionFormat>\n" +
                "<gmd:transferOptions>\n" +
                    "<gmd:MD_DigitalTransferOptions>\n" +
                        "<gmd:onLine>\n" +
                            "<gmd:CI_OnlineResource>\n" +
                                "<gmd:linkage>\n" +
                                    "<gmd:URL/>\n" +
                                "</gmd:linkage>\n" +
                                "<gmd:description>\n" +
                                    "<gco:CharacterString>Data Access URL</gco:CharacterString>\n" +
                                "</gmd:description>\n" +
                                "<gmd:function>\n" +
                                    "<gmd:CI_OnLineFunctionCode codeListValue=\"function\" codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_OnLineFunctionCode\">download</gmd:CI_OnLineFunctionCode>\n" +
                                "</gmd:function>\n" +
                            "</gmd:CI_OnlineResource>\n" +
                        "</gmd:onLine>\n" +
                    "</gmd:MD_DigitalTransferOptions>\n" +
                "</gmd:transferOptions>\n" +
            "</gmd:MD_Distribution>\n" + 
        "</gmd:distributionInfo>\n" +
    "</DryValleyCollection>";                    		
                        
    
    private final String ENTITY_ID = "entityID";
    private GQM gqm;
    private Dcp sip;
    private final double WEST_LON = 160;
    private final double EAST_LON = 170;
    private final double NORTH_LAT = -78;
    private final double SOUTH_LAT = -77;
    
    private static class GQMTestOutput implements Output<String, GQM> {

        Map<String, GQM> results = new HashMap<String, GQM>();
        
        public void write(String key, GQM value) {
            results.put(key, value);
        }
        
        @Override
        public void close() {
            /* Does nothing */
        }

    }
    
    @Before
    public void setUp() {
        gqm = new GQM(ENTITY_ID);
        
        Point bottomLeft = new Point(SOUTH_LAT, WEST_LON);
        Point bottomRight = new Point(SOUTH_LAT, EAST_LON);
        Point topRight = new Point(NORTH_LAT, EAST_LON);
        Point topLeft = new Point(NORTH_LAT, WEST_LON);
        
        Geometry boundingBox = new Geometry(Geometry.Type.POLYGON, bottomLeft, bottomRight, topRight, topLeft);
        
        URI srid = URI.create("http://spatialreference.org/ref/epsg/4326");
        Location location = new Location(boundingBox, srid);
        gqm.getLocations().add(location);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        try {
            Date startDate = formatter.parse("19930101");
            Date endDate = formatter.parse("20081231"); 
            DateTimeInterval dti = new DateTimeInterval(startDate.getTime(), endDate.getTime());
            gqm.getIntervals().add(dti);
        } catch (ParseException e) {
        }
        
        String profile = "This collection is comprised of data collected by Professor Bruce D. Marsh of the Department of Earth and Planetary" +
        		" Sciences at The Johns Hopkins University while doing fieldwork on the magmatic system of the McMurdo " +
        		"Dry Valleys of Antarctica. Field seasons included here are 1993 through 2008. This collection contains " +
        		"images of individual rock samples, chemical analyses of select rock samples, field photos, and field notes. " +
        		"The core of the collection consists of 2300+ images of rock samples and thousands of field photos. " +
        		"References to locations (longitude/latitude/altitude) in this collection are based on the WGS84 standard.";
          
        Relation relation = new Relation(URI.create("gmd:abstract"), profile);
        gqm.getRelations().add(relation);      
        
        sip = new Dcp();
        DcsCollection col = new DcsCollection();
        DcsMetadata md = new DcsMetadata();
        
        md.setMetadata(XML);
        col.setTitle("McMurdoDryValleys");
        col.addMetadata(md);
        
        sip.addCollection(col); 
    }
    
    @Test
    public void testMapping(){
        DryValleyCollectionMapping mapping = new DryValleyCollectionMapping();
        
        GQM mappedGQM;
        GQMTestOutput output = new GQMTestOutput();
        
        mapping.map(ENTITY_ID, sip, output);
        
        mappedGQM = output.results.get(ENTITY_ID);
        
        Assert.assertEquals(gqm, mappedGQM);
        
    }   
}
