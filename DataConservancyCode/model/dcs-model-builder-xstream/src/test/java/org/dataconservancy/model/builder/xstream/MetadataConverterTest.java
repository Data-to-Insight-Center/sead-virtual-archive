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
package org.dataconservancy.model.builder.xstream;

import com.thoughtworks.xstream.XStream;
import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.model.dcp.DcpModelVersion;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class MetadataConverterTest extends AbstractXstreamConverterTest {

    private static final String SCHEMA_URI = "http://sdss.org/metadata/astroSchema.example.xsd";
    private static final String MD_BLOB = "<astro:md xmlns:astro=\"http://sdss.org/astro\">\n" +
            "          <astro:skyCoverage>all of it</astro:skyCoverage>\n" +
            "          <astro:enfOfWorldFactor>-1</astro:enfOfWorldFactor>\n" +
            "        </astro:md>\n";

    private static final String XML = "<metadata xmlns=\"" + XMLNS + "\" schemaURI=\"" + SCHEMA_URI + "\">\n" + MD_BLOB + "</metadata>";

    private DcsMetadata md;

    @Before
    public void setUp() {
        super.setUp();
        md = new DcsMetadata();
        md.setSchemaUri(SCHEMA_URI);
        md.setMetadata(MD_BLOB);
    }

    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual("Expected: " + XML + " Actual: " + x.toXML(md), XML, x.toXML(md));
        XMLAssert.assertXMLEqual("Expected: " + XML + " Actual: " + x.toXML(x.fromXML(XML)), XML, x.toXML(x.fromXML(XML)));
    }

    @Test
    public void testUnmarshal() {
        assertEquals(md, x.fromXML(XML));
        assertEquals(md, x.fromXML(x.toXML(md)));
    }

    @Test
    public void testCopyDryValleysFieldPhotoMetadata() throws XMLStreamException, IOException, SAXException {
        final String PI = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        final String mdBlob = "\n" +
                "\n" +
                "<McMurdoDryValleys xmlns=\"\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:fgdc=\"http://www.fgdc.gov/metadata/standards/\">\n" +
                "\t<FieldPhotos>\n" +
                "\t\t<FieldPhoto>\n" +
                "\t\t\t<fileName>McDV_FieldPhoto_2000_00868.tif</fileName>\n" +
                "\t\t\t<fieldSeason>2000</fieldSeason>\n" +
                "\t\t\t<photographerLatitude>-77.46864</photographerLatitude>\n" +
                "\t\t\t<photographerLongitude>162.24459</photographerLongitude>\n" +
                "\t\t\t<dc:title>McDV_FieldPhoto_2000_00868</dc:title>\n" +
                "\t\t\t<dc:subject>Geology</dc:subject>\n" +
                "\t\t\t<dc:subject>Petrology</dc:subject>\n" +
                "\t\t\t<dc:subject>Igneous Petrology</dc:subject>\n" +
                "\t\t\t<dc:subject>Glaciology</dc:subject>\n" +
                "\t\t\t<dc:description>Looking southeast across Wright Valley from Olympus Range; Goodspeed Glacier (left), Hart Glacier (center), and Meserve Glacier (right).</dc:description>\n" +
                "\t\t\t<dc:creator>Marsh, Bruce D.</dc:creator>\n" +
                "\t\t\t<dc:publisher>The Data Conservancy, Sheridan Libraries, The Johns Hopkins University</dc:publisher>\n" +
                "\t\t\t<dc:date>2000-01</dc:date>\n" +
                "\t\t\t<dc:format>image/tif</dc:format>\n" +
                "\t\t\t<dc:rights>Copyright Bruce D. Marsh, licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported license.  http://creativecommons.org/licenses/by-nc-sa/3.0/</dc:rights>\n" +
                "\t\t\t<dc:identifier>McDV_FieldPhoto_2000_00868</dc:identifier>\n" +
                "\t\t\t<dc:coverage>Antarctica</dc:coverage>\n" +
                "\t\t\t<dc:coverage>McMurdo Dry Valleys</dc:coverage>\n" +
                "\t\t\t<USGS_ANTARCTIC_NAMES>\n" +
                "\t\t\t\t<ANTARCTICA_FEATURE_ID ID=\"5831\">\n" +
                "\t\t\t\t\t<FEATURE_NAME>Goodspeed Glacier</FEATURE_NAME>\n" +
                "\t\t\t\t\t<FEATURE_CLASS>Glacier</FEATURE_CLASS>\n" +
                "\t\t\t\t\t<DESCRIPTION>A small hanging glacier on the south wall of Wright Valley, Victoria Land, between the Hart and Denton Glaciers.  Named by U.S. geologist Robert Nichols after Robert Goodspeed, geological assistant to Nichols at nearby Marble Point in the 1959-60 field season.</DESCRIPTION>\n" +
                "\t\t\t\t\t<PRIMARY_LATITUDE_DEC>-77.4833333</PRIMARY_LATITUDE_DEC>\n" +
                "\t\t\t\t\t<PRIMARY_LONGITUDE_DEC>162.45</PRIMARY_LONGITUDE_DEC>\n" +
                "\t\t\t\t</ANTARCTICA_FEATURE_ID>\n" +
                "\t\t\t\t<ANTARCTICA_FEATURE_ID ID=\"6437\">\n" +
                "\t\t\t\t\t<FEATURE_NAME>Hart Glacier</FEATURE_NAME>\n" +
                "\t\t\t\t\t<FEATURE_CLASS>Glacier</FEATURE_CLASS>\n" +
                "\t\t\t\t\t<DESCRIPTION>A small hanging glacier on the south wall of Wright Valley, Victoria Land, between the Meserve and Goodspeed Glaciers.  Named by U.S. geologist Robert Nichols for Roger Hart, geological assistant to Nichols at nearby Marble Point in the 1959-60 field season.</DESCRIPTION>\n" +
                "\t\t\t\t\t<PRIMARY_LATITUDE_DEC>-77.5</PRIMARY_LATITUDE_DEC>\n" +
                "\t\t\t\t\t<PRIMARY_LONGITUDE_DEC>162.3833333</PRIMARY_LONGITUDE_DEC>\n" +
                "\t\t\t\t</ANTARCTICA_FEATURE_ID>\n" +
                "\t\t\t\t<ANTARCTICA_FEATURE_ID ID=\"9896\">\n" +
                "\t\t\t\t\t<FEATURE_NAME>Meserve Glacier</FEATURE_NAME>\n" +
                "\t\t\t\t\t<FEATURE_CLASS>Glacier</FEATURE_CLASS>\n" +
                "\t\t\t\t\t<DESCRIPTION>A hanging glacier on the south wall of Wright Valley, Victoria Land, between Bartley Glacier and Hart Glacier.  Named by U.S. geologist Robert Nichols for William Meserve (geological assistant to Nichols) who did field work in Wright Valley during the 1959-60 field season.</DESCRIPTION>\n" +
                "\t\t\t\t\t<PRIMARY_LATITUDE_DEC>-77.5166667</PRIMARY_LATITUDE_DEC>\n" +
                "\t\t\t\t\t<PRIMARY_LONGITUDE_DEC>162.2833333</PRIMARY_LONGITUDE_DEC>\n" +
                "\t\t\t\t</ANTARCTICA_FEATURE_ID>\n" +
                "\t\t\t\t<ANTARCTICA_FEATURE_ID ID=\"11083\">\n" +
                "\t\t\t\t\t<FEATURE_NAME>Olympus Range</FEATURE_NAME>\n" +
                "\t\t\t\t\t<FEATURE_CLASS>Range</FEATURE_CLASS>\n" +
                "\t\t\t\t\t<DESCRIPTION>A primarily ice-free mountain range of Victoria Land with peaks over 2,000 m, between Victoria and McKelvey Valleys on the north and Wright Valley on the south.  Mapped by the Victoria University&apos;s Antarctic Expeditions (VUWAE), 1958-59, and named for the mythological home of the Greek gods. Peaks in the range are named for figures in Greek mythology.</DESCRIPTION>\n" +
                "\t\t\t\t\t<PRIMARY_LATITUDE_DEC>-77.4833333</PRIMARY_LATITUDE_DEC>\n" +
                "\t\t\t\t\t<PRIMARY_LONGITUDE_DEC>161.5</PRIMARY_LONGITUDE_DEC>\n" +
                "\t\t\t\t</ANTARCTICA_FEATURE_ID>\n" +
                "\t\t\t\t<ANTARCTICA_FEATURE_ID ID=\"16818\">\n" +
                "\t\t\t\t\t<FEATURE_NAME>Wright Valley</FEATURE_NAME>\n" +
                "\t\t\t\t\t<FEATURE_CLASS>Valley</FEATURE_CLASS>\n" +
                "\t\t\t\t\t<DESCRIPTION>Large E-W trending valley, formerly occupied by a glacier but now ice free except for Wright Upper Glacier at its head and Wright Lower Glacier at its mouth, in Victoria Land.  Named by the Victoria University&apos;s Antarctic Expeditions (VUWAE) (1958-59) for Sir Charles Wright, for whom the British Antarctic Expedition (BrAE) (1910-13) named the glacier at the mouth of this valley.</DESCRIPTION>\n" +
                "\t\t\t\t\t<PRIMARY_LATITUDE_DEC>-77.5166667</PRIMARY_LATITUDE_DEC>\n" +
                "\t\t\t\t\t<PRIMARY_LONGITUDE_DEC>161.8333333</PRIMARY_LONGITUDE_DEC>\n" +
                "\t\t\t\t</ANTARCTICA_FEATURE_ID>\n" +
                "\t\t\t</USGS_ANTARCTIC_NAMES> \n" +
                "\t\t\t<fgdc:metadata>\n" +
                "\t\t\t  <fgdc:idinfo>\n" +
                "\t\t\t    <fgdc:datsetid>McDV_FieldPhoto_2000_00868</fgdc:datsetid>\n" +
                "\t\t\t    <fgdc:citation>\n" +
                "\t\t\t      <fgdc:citeinfo>\n" +
                "\t\t\t        <fgdc:origin>Marsh, Bruce D.</fgdc:origin>\n" +
                "\t\t\t        <fgdc:pubdate>2011-02-15</fgdc:pubdate>\n" +
                "\t\t\t\t\t\t<fgdc:title>Hart Glacier</fgdc:title>\n" +
                "\t\t\t\t\t<fgdc:edition></fgdc:edition>\n" +
                "\t\t\t        <fgdc:pubinfo>\n" +
                "\t\t\t          <fgdc:pubplace>Baltimore, MD  U.S.A.</fgdc:pubplace>\n" +
                "\t\t\t          <fgdc:publish>The Data Conservancy, Sheridan Libraries, The Johns Hopkins University</fgdc:publish>\n" +
                "\t\t\t        </fgdc:pubinfo>\n" +
                "\t\t\t        <fgdc:onlink></fgdc:onlink>\n" +
                "\t\t\t      </fgdc:citeinfo>\n" +
                "\t\t\t    </fgdc:citation>\n" +
                "\t\t\t    <fgdc:descript>\n" +
                "\t\t\t      <fgdc:abstract>Looking southeast across Wright Valley from Olympus Range; Goodspeed Glacier (left), Hart Glacier (center), and Meserve Glacier (right).</fgdc:abstract>\n" +
                "\t\t\t      <fgdc:purpose>scientific research</fgdc:purpose>\n" +
                "\t\t\t\t  <fgdc:supplinf>Other glaciers in the field of view: Goodspeed Glacier; Meserve Glacier</fgdc:supplinf>\n" +
                "\t\t\t      <fgdc:documnts>\n" +
                "\t\t\t        <fgdc:userguid>\n" +
                "\t\t\t          <fgdc:citeinfo>\n" +
                "\t\t\t            <fgdc:origin>Marsh, Bruce D.</fgdc:origin>\n" +
                "\t\t\t            <fgdc:pubdate>2000-01</fgdc:pubdate>\n" +
                "\t\t\t\t\t\t\t<fgdc:title>Hart Glacier</fgdc:title>\n" +
                "\t\t\t            <fgdc:pubinfo>\n" +
                "\t\t\t              <fgdc:pubplace>Baltimore, MD  U.S.A.</fgdc:pubplace>\n" +
                "\t\t\t              <fgdc:publish>The Data Conservancy, Sheridan Libraries, The Johns Hopkins University</fgdc:publish>\n" +
                "\t\t\t            </fgdc:pubinfo>\n" +
                "\t\t\t            <fgdc:onlink></fgdc:onlink>\n" +
                "\t\t\t          </fgdc:citeinfo>\n" +
                "\t\t\t        </fgdc:userguid>\n" +
                "\t\t\t      </fgdc:documnts>\n" +
                "\t\t\t    </fgdc:descript>\n" +
                "\t\t\t    <fgdc:timeperd>\n" +
                "\t\t\t      <fgdc:timeinfo>\n" +
                "\t\t\t        <fgdc:sngdate>\n" +
                "\t\t\t          <fgdc:caldate>2000-01</fgdc:caldate>\n" +
                "\t\t\t        </fgdc:sngdate>\n" +
                "\t\t\t      </fgdc:timeinfo>\n" +
                "\t\t\t      <fgdc:current></fgdc:current>\n" +
                "\t\t\t    </fgdc:timeperd>\n" +
                "\t\t\t    <fgdc:status>\n" +
                "\t\t\t      <fgdc:progress></fgdc:progress>\n" +
                "\t\t\t      <fgdc:update></fgdc:update>\n" +
                "\t\t\t    </fgdc:status>\n" +
                "\t\t\t    <fgdc:spdom>\n" +
                "\t\t\t      <fgdc:bounding>\n" +
                "\t\t\t        <fgdc:westbc>+162.2446</fgdc:westbc>\n" +
                "\t\t\t        <fgdc:eastbc>+162.2446</fgdc:eastbc>\n" +
                "\t\t\t        <fgdc:northbc>-77.4686</fgdc:northbc>\n" +
                "\t\t\t        <fgdc:southbc>-77.4686</fgdc:southbc>\n" +
                "\t\t\t\t  </fgdc:bounding>\n" +
                "\t\t\t    </fgdc:spdom>\n" +
                "\t\t\t    <fgdc:keywords>\n" +
                "\t\t\t      <fgdc:theme>\n" +
                "\t\t\t        <fgdc:themekt>ISO 19115 Topic Categories</fgdc:themekt>\n" +
                "\t\t\t        <fgdc:themekey>geoscientificInformation</fgdc:themekey>\n" +
                "\t\t\t      </fgdc:theme>\n" +
                "\t\t\t      <fgdc:theme>\n" +
                "\t\t\t        <fgdc:themekt>None</fgdc:themekt>\n" +
                "\t\t\t        <fgdc:themekey>Geology</fgdc:themekey>\n" +
                "\t\t\t        <fgdc:themekey>Petrology</fgdc:themekey>\n" +
                "\t\t\t        <fgdc:themekey>Igneous Petrology</fgdc:themekey>\n" +
                "\t\t\t        <fgdc:themekey>Glaciology</fgdc:themekey>\n" +
                "\t\t\t      </fgdc:theme>\n" +
                "\t\t\t      <fgdc:place>\n" +
                "\t\t\t        <fgdc:placekt>ISO3166-1</fgdc:placekt>\n" +
                "\t\t\t        <fgdc:placekey>ANTARCTICA</fgdc:placekey>\n" +
                "\t\t\t      </fgdc:place>\n" +
                "\t\t\t    </fgdc:keywords>\n" +
                "\t\t\t    <fgdc:plainsid>\n" +
                "\t\t\t      <fgdc:platflnm></fgdc:platflnm>\n" +
                "\t\t\t      <fgdc:platfsnm></fgdc:platfsnm>\n" +
                "\t\t\t      <fgdc:instflnm></fgdc:instflnm>\n" +
                "\t\t\t      <fgdc:instshnm></fgdc:instshnm>\n" +
                "\t\t\t    </fgdc:plainsid>\n" +
                "\t\t\t    <fgdc:thelayid>\n" +
                "\t\t\t      <fgdc:numthlay></fgdc:numthlay>\n" +
                "\t\t\t      <fgdc:layrname>\n" +
                "\t\t\t        <fgdc:theme>\n" +
                "\t\t\t          <fgdc:themekt></fgdc:themekt>\n" +
                "\t\t\t          <fgdc:themekey></fgdc:themekey>\n" +
                "\t\t\t        </fgdc:theme>\n" +
                "\t\t\t      </fgdc:layrname>\n" +
                "\t\t\t    </fgdc:thelayid>\n" +
                "\t\t\t    <fgdc:accconst></fgdc:accconst>\n" +
                "\t\t\t    <fgdc:useconst>Copyright Bruce D. Marsh, licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported license.  http://creativecommons.org/licenses/by-nc-sa/3.0/</fgdc:useconst>\n" +
                "\t\t\t    <fgdc:ptcontac>\n" +
                "\t\t\t      <fgdc:cntinfo>\n" +
                "\t\t\t        <fgdc:cntperp>\n" +
                "\t\t\t          <fgdc:cntper>Keith Kaneda</fgdc:cntper>\n" +
                "\t\t\t        </fgdc:cntperp>\n" +
                "\t\t\t        <fgdc:cntaddr>\n" +
                "\t\t\t          <fgdc:addrtype>mailing</fgdc:addrtype>\n" +
                "\t\t\t          <fgdc:address>The Sheridan Libraries</fgdc:address>\n" +
                "\t\t\t          <fgdc:address>Johns Hopkins University</fgdc:address>\n" +
                "\t\t\t          <fgdc:address>3400 North Charles Street</fgdc:address>\n" +
                "\t\t\t          <fgdc:city>Baltimore</fgdc:city>\n" +
                "\t\t\t          <fgdc:state>MD</fgdc:state>\n" +
                "\t\t\t          <fgdc:postal>21218</fgdc:postal>\n" +
                "\t\t\t          <fgdc:country>USA</fgdc:country>\n" +
                "\t\t\t        </fgdc:cntaddr>\n" +
                "\t\t\t        <fgdc:cntvoice></fgdc:cntvoice>\n" +
                "\t\t\t        <fgdc:cntfax></fgdc:cntfax>\n" +
                "\t\t\t        <fgdc:cntemail></fgdc:cntemail>\n" +
                "\t\t\t      </fgdc:cntinfo>\n" +
                "\t\t\t    </fgdc:ptcontac>\n" +
                "\t\t\t  </fgdc:idinfo>\n" +
                "\t\t\t  <fgdc:distinfo>\n" +
                "\t\t\t    <fgdc:distrib>\n" +
                "\t\t\t      <fgdc:cntinfo>\n" +
                "\t\t\t        <fgdc:cntorgp>\n" +
                "\t\t\t          <fgdc:cntorg>The Data Conservancy, Sheridan Libraries, The Johns Hopkins University</fgdc:cntorg>\n" +
                "\t\t\t          <fgdc:cntper>Keith Kaneda</fgdc:cntper>\n" +
                "\t\t\t        </fgdc:cntorgp>\n" +
                "\t\t\t        <fgdc:cntaddr>\n" +
                "\t\t\t          <fgdc:addrtype>mailing</fgdc:addrtype>\n" +
                "\t\t\t          <fgdc:address>The Sheridan Libraries</fgdc:address>\n" +
                "\t\t\t          <fgdc:address>Johns Hopkins University</fgdc:address>\n" +
                "\t\t\t          <fgdc:address>3400 North Charles Street</fgdc:address>\n" +
                "\t\t\t          <fgdc:city>Baltimore</fgdc:city>\n" +
                "\t\t\t          <fgdc:state>MD</fgdc:state>\n" +
                "\t\t\t          <fgdc:postal>21218</fgdc:postal>\n" +
                "\t\t\t          <fgdc:country>USA</fgdc:country>\n" +
                "\t\t\t        </fgdc:cntaddr>\n" +
                "\t\t\t        <fgdc:cntvoice></fgdc:cntvoice>\n" +
                "\t\t\t        <fgdc:cntfax></fgdc:cntfax>\n" +
                "\t\t\t        <fgdc:cntemail></fgdc:cntemail>\n" +
                "\t\t\t      </fgdc:cntinfo>\n" +
                "\t\t\t    </fgdc:distrib>\n" +
                "\t\t\t    <fgdc:resdesc></fgdc:resdesc>\n" +
                "\t\t\t    <fgdc:distliab></fgdc:distliab>\n" +
                "\t\t\t    <fgdc:stdorder>\n" +
                "\t\t\t      <fgdc:digform>\n" +
                "\t\t\t        <fgdc:digtinfo>\n" +
                "\t\t\t          <fgdc:formname>image/tif</fgdc:formname>\n" +
                "\t\t\t        </fgdc:digtinfo>\n" +
                "\t\t\t        <fgdc:digtopt>\n" +
                "\t\t\t          <fgdc:onlinopt>\n" +
                "\t\t\t            <fgdc:computer>\n" +
                "\t\t\t              <fgdc:networka>\n" +
                "\t\t\t                <fgdc:networkr></fgdc:networkr>\n" +
                "\t\t\t              </fgdc:networka>\n" +
                "\t\t\t            </fgdc:computer>\n" +
                "\t\t\t          </fgdc:onlinopt>\n" +
                "\t\t\t        </fgdc:digtopt>\n" +
                "\t\t\t      </fgdc:digform>\n" +
                "\t\t\t      <fgdc:fees></fgdc:fees>\n" +
                "\t\t\t    </fgdc:stdorder>\n" +
                "\t\t\t  </fgdc:distinfo>\n" +
                "\t\t\t  <fgdc:metainfo>\n" +
                "\t\t\t    <fgdc:metd></fgdc:metd>\n" +
                "\t\t\t    <fgdc:metrd></fgdc:metrd>\n" +
                "\t\t\t    <fgdc:metc>\n" +
                "\t\t\t      <fgdc:cntinfo>\n" +
                "\t\t\t        <fgdc:cntperp>\n" +
                "\t\t\t          <fgdc:cntper>Keith Kaneda</fgdc:cntper>\n" +
                "\t\t\t          <fgdc:cntorg>The Data Conservancy, Sheridan Libraries, The Johns Hopkins University</fgdc:cntorg>\n" +
                "\t\t\t        </fgdc:cntperp>\n" +
                "\t\t\t        <fgdc:cntaddr>\n" +
                "\t\t\t          <fgdc:addrtype>mailing</fgdc:addrtype>\n" +
                "\t\t\t          <fgdc:address>The Sheridan Libraries</fgdc:address>\n" +
                "\t\t\t          <fgdc:address>Johns Hopkins University</fgdc:address>\n" +
                "\t\t\t          <fgdc:address>3400 North Charles Street</fgdc:address>\n" +
                "\t\t\t          <fgdc:city>Baltimore</fgdc:city>\n" +
                "\t\t\t          <fgdc:state>MD</fgdc:state>\n" +
                "\t\t\t          <fgdc:postal>21218</fgdc:postal>\n" +
                "\t\t\t          <fgdc:country>USA</fgdc:country>\n" +
                "\t\t\t        </fgdc:cntaddr>\n" +
                "\t\t\t        <fgdc:cntvoice></fgdc:cntvoice>\n" +
                "\t\t\t        <fgdc:cntfax></fgdc:cntfax>\n" +
                "\t\t\t        <fgdc:cntemail></fgdc:cntemail>\n" +
                "\t\t\t      </fgdc:cntinfo>\n" +
                "\t\t\t    </fgdc:metc>\n" +
                "\t\t\t    <fgdc:metstdn>FGDC Content Standard for Digital Geospatial Metadata</fgdc:metstdn>\n" +
                "\t\t\t    <fgdc:metstdv>FGDC-STD-012-2002</fgdc:metstdv>\n" +
                "\t\t\t  </fgdc:metainfo>\n" +
                "\t\t\t</fgdc:metadata>\n" +
                "\t\t</FieldPhoto>\n" +
                "\t</FieldPhotos>\n" +
                "</McMurdoDryValleys>";

        final String schemaUri = "urn:dryValleys:FieldPhoto:1.0";

        final DcsMetadata dcsMetadata = new DcsMetadata();
        dcsMetadata.setSchemaUri(schemaUri);

        // The metadata blob - importantly - includes an XML processing instruction.  The MetadataConverter should
        // handle the PI by stripping it out.
        dcsMetadata.setMetadata(PI + mdBlob);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        x.toXML(dcsMetadata, out);

        final String actualXML = new String(out.toByteArray());

        // N.B.: No PI in the expected XML.
        final String expectedXML =
                "<" + MetadataConverter.E_METADATA + " " + MetadataConverter.A_SCHEMA + "=\"" + schemaUri + "\" xmlns=\"" + XMLNS + "\">\n" +
                        mdBlob + "\n" +
                "</" + MetadataConverter.E_METADATA + ">";



        XMLAssert.assertXMLEqual("Expected: " + expectedXML + "\n Actual: " + actualXML + "\n", expectedXML, actualXML);

        final DcsMetadata roundTrip = (DcsMetadata) x.fromXML(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(dcsMetadata, roundTrip);
    }

}
