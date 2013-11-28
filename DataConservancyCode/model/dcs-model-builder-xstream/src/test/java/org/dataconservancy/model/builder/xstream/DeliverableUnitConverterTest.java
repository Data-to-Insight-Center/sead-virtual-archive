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

import java.io.IOException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;

import org.dataconservancy.model.dcs.*;
import org.junit.Before;
import org.junit.Test;

import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class DeliverableUnitConverterTest extends AbstractXstreamConverterTest {

    private final static String DU_ID = "urn:sdss:12345";
    private final static String LINEAGE_ID = "urn:lineage:15";
    private final static String COLL_REF = "http://dataconservancy.org/collections/SDSS_run_5";
    private final static String TITLE = "SDSS file 12345";
    private final static String CREATOR = "Astrophysical Research Consortium (ARC)";
    private final static String SUBJECT = "Astronomy";
    private final static String EXT_REF = "http://das.sdss.org/blahblahblah...";
    private final static String MD_SCHEMAURI_1 = "http://sdss.org/metadata/astroSchema.example.xsd";
    private final static String MDBLOB_1 = "<astro:md xmlns:astro=\"http://sdss.org/astro\">\n" +
            "          <astro:skyCoverage>all of it</astro:skyCoverage>\n" +
            "          <astro:enfOfWorldFactor>-1</astro:enfOfWorldFactor>\n" +
            "        </astro:md>";

    private final static String MD_SCHEMAURI_2 = "http://dryvalleys.org/metadata/iloverocks.xsd";
    private final static String MDBLOB_2 = "<rocks:md xmlns:rocks=\"http://dryvalleys.org/rocks\">\n" +
            "          <rocks:location>grand canyon</rocks:location>\n" +
            "          <rocks:types><rocks:type id=\"rock:id:1\">igneous</rocks:type><rocks:type id=\"rock:id:2\">quartz</rocks:type></rocks:types>\n" +
            "        </rocks:md>";

    private final static String MDREF_1 = "urn:sdss:12345/metadata/1";
    private final static String MDREF_2 = "urn:sdss:12345/metadata/2";
    private final static String RIGHTS = "this is a rights statement.";

    private final static String AUTHID1 = "aid123";
    private final static String TYPEID1 = "type123";
    private final static String IDVALUE1 = "id123";


    private final static String XML = "<DeliverableUnit xmlns=\"" + XMLNS + "\" id=\"" + DU_ID + "\" lineageId=\""+ LINEAGE_ID + "\">\n" +
            "\n" +
            "        <" + DeliverableUnitConverter.E_ALTERNATEID + ">" +
            "        <authorityId>" + AUTHID1 + "</authorityId> " +
            "        <typeId>" + TYPEID1 + "</typeId> " +
            "        <idValue>" + IDVALUE1 + "</idValue> " +
            "        </" + DeliverableUnitConverter.E_ALTERNATEID + ">" +
            "\n" +
            "      <!--  The collection exists, and this is its DCS identifier -->\n" +
            "      <collection ref=\"" + COLL_REF + "\" />\n" +
            "\n" +
            "      <title>" + TITLE +"</title>\n" +
            "      <creator>" + CREATOR + "</creator>\n" +
            "      <subject>" + SUBJECT + "</subject>\n" +
            "      <formerExternalRef>" + EXT_REF + "</formerExternalRef>\n" +
            "\n" +
            "      <!-- An example of metadata inline -->\n" +
            "      <metadata schemaURI=\"" + MD_SCHEMAURI_1 + "\">\n" + MDBLOB_1 +
            "      </metadata>\n" +
            "\n" +
            "      <!--\n" +
            "        An example of metadata that exists as a file, in this case it is\n" +
            "        submitted in the SIP\n" +
            "      -->\n" +
            "      <metadata ref=\"" + MDREF_1 + "\" />\n" +
            "\n" +
            "      <!--\n" +
            "        An example of metadata that exists as a file, in this case it is\n" +
            "        submitted in the SIP\n" +
            "      -->\n" +
            "      <metadata ref=\"" + MDREF_2 + "\" />\n" +
            "\n" +
            "      <!-- An example of metadata inline -->\n" +
            "      <metadata schemaURI=\"" + MD_SCHEMAURI_2 + "\">\n" + MDBLOB_2 +
            "      </metadata>\n" +
            "\n" +
            "    </DeliverableUnit>\n";

    private final static String XML_RIGHTS = "<DeliverableUnit xmlns=\"" + XMLNS + "\" id=\"" + DU_ID + "\">\n" +
            "\n" +
            "      <title>" + TITLE +"</title>\n" +
            "\n" +
            "      <rights>" + RIGHTS + "</rights>\n" +
            "    </DeliverableUnit>\n";

    private DcsDeliverableUnit du;

    @Before
    public void setUp() {
        super.setUp();

        du = new DcsDeliverableUnit();
        du.setId(DU_ID);
        du.setLineageId(LINEAGE_ID);
        du.setTitle(TITLE);
        du.addCreator(CREATOR);
        du.addSubject(SUBJECT);
        du.addFormerExternalRef(EXT_REF);

        final DcsResourceIdentifier rid = new DcsResourceIdentifier();
        rid.setAuthorityId(AUTHID1);
        rid.setTypeId(TYPEID1);
        rid.setIdValue(IDVALUE1);
        du.addAlternateId(rid);

        final DcsMetadataRef mdRef1 = new DcsMetadataRef();
        mdRef1.setRef(MDREF_1);
        du.addMetadataRef(mdRef1);

        final DcsMetadata mdInline1 = new DcsMetadata();
        mdInline1.setSchemaUri(MD_SCHEMAURI_1);
        mdInline1.setMetadata(MDBLOB_1);
        du.addMetadata(mdInline1);

        final DcsMetadataRef mdRef2 = new DcsMetadataRef();
        mdRef2.setRef(MDREF_2);
        du.addMetadataRef(mdRef2);
        
        final DcsMetadata mdInline2 = new DcsMetadata();
        mdInline2.setSchemaUri(MD_SCHEMAURI_2);
        mdInline2.setMetadata(MDBLOB_2);
        du.addMetadata(mdInline2);

        final DcsCollectionRef collRef = new DcsCollectionRef();
        collRef.setRef(COLL_REF);
        du.addCollection(collRef);
    }

    @Test
    public void testMarshal() throws IOException, SAXException {
        // Because the java object model uses Sets (which have no ordering), we have to
        // tell XMLUnit to ignore the order of the elements by telling it to match
        // the elements to compare using their element names and attribute values.
        // only the elements with the same name and attribute values are compared for equality.
        Diff d = new Diff(XML, x.toXML(du));
        d.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual("Expected: " + XML + " Actual: " + x.toXML(du), d, true);
    }

    @Test
    public void testMarshalRoundTrip() throws IOException, SAXException {
        Diff d = new Diff(XML, x.toXML(x.fromXML(XML)));
        d.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual("Expected: " + XML + " Actual: " + x.toXML(du), d, true);
    }

    @Test
    public void testUnmarshal() {
        assertEquals(du, x.fromXML(XML));
        assertEquals(du, x.fromXML(x.toXML(du)));
    }

    @Test
    public void testWithRights() throws IOException, SAXException {
        final String rights = RIGHTS;
        final DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(DU_ID);
        du.setTitle(TITLE);
        du.setRights(rights);

        // one-way tests
        assertEquals(du, x.fromXML(XML_RIGHTS));
        XMLAssert.assertXMLEqual(XML_RIGHTS, x.toXML(du));

        // Round-trip tests
        assertEquals(du, x.fromXML(x.toXML(du)));
        XMLAssert.assertXMLEqual(XML_RIGHTS, x.toXML(x.fromXML(XML_RIGHTS)));
    }
}
