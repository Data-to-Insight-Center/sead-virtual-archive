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

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class DcpConverterTest extends AbstractXstreamConverterTest {

    private static final String DU_ID = "urn:sdss:12345";
    private static final String DU_COLL_REF = "http://dataconservancy.org/collections/SDSS_run_5";
    private static final String DU_TITLE = "SDSS file 12345";
    private static final String DU_CREATOR = "Astrophysical Research Consortium (ARC)";
    private static final String DU_SUBJECT = "Astronomy";
    private static final String DU_EXTREF = "http://das.sdss.org/blahblahblah...";
    private static final String DU_MDREF = "urn:sdss:12345/metadata";

    private static final String DU_MD_INLINE_SCHEMA = "http://sdss.org/metadata/astroSchema.example.xsd";
    private static final String DU_MD_BLOB = "<astro:md xmlns:astro=\"http://sdss.org/astro\">\n" +
            "          <astro:skyCoverage>all of it</astro:skyCoverage>\n" +
            "          <astro:enfOfWorldFactor>-1</astro:enfOfWorldFactor>\n" +
            "        </astro:md>";

    private static final String DU_MD_INLINE = "<metadata schemaURI=\"" + DU_MD_INLINE_SCHEMA + "\">\n" +
            "        " + DU_MD_BLOB + "\n" +
            "      </metadata>";

    private static final String MANIFESTATION_ID = "urn:sdss:12345/manifestation";

    private static final String MF_FILEPATH = "/scans/5/";

    private static final String FILE_ID1 = "urn:sdss:12345/FITS_FILE";
    private static final String FILE_SRC1 = "http://sdss.org/files/fits/12345.fits";
    private static final String FILE_NAME1 = "12345.fits";
    private static final String FILE_EXT1 = "false";

    private static final String FORMAT_SCHEMEID1 = "http://www.nationalarchives.gov.uk/PRONOM/";
    private static final String FORMAT1 = "x-fmt/383";
    private static final String FORMAT_NAME1 = "FITS";
    private static final String FORMAT_VERSION1 = "3.0";

    private static final String FILE_ID2 = "urn:sdss:12345/metadata";
    private static final String FILE_SRC2 = "urn:dcs:uploads/0x440";
    private static final String FILE_NAME2 = "fitsDerivedMetadata.csv";
    private static final String FILE_EXT2 = "true";

    private static final String FORMAT_SCHEMEID2 = "http://www.nationalarchives.gov.uk/PRONOM/";
    private static final String FORMAT2 = "x-fmt/18";
    private static final String FORMAT_SCHEMEID3 = "http://www.iana.org/assignments/media-types/";
    private static final String FORMAT3 = "text/csv";

    private static final String FIXITY_ALGO = "md5";
    private static final String FIXITY_VALUE = "fe5b3b4f78b9bf3ae21cd52c2f349174";


    private final String XML = "<dcp xmlns=\"" + XMLNS + "\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "  xsi:schemaLocation=\"" + XMLNS + " " + XMLNS +  "\">\n" +
            "\n" +
            "  <DeliverableUnits>\n" +
            "    <DeliverableUnit id=\"" + DU_ID + "\">\n" +
            "\n" +
            "      <!--  The collection exists, and this is its DCS identifier -->\n" +
            "      <collection ref=\"" + DU_COLL_REF + "\" />\n" +
            "\n" +
            "      <title>" + DU_TITLE + "</title>\n" +
            "      <creator>" + DU_CREATOR + "</creator>\n" +
            "      <subject>" + DU_SUBJECT + "</subject>\n" +
            "      <formerExternalRef>" + DU_EXTREF + "</formerExternalRef>\n" +
            "\n" +
            "      <!-- An example of metadata inline -->\n" +
            "      " + DU_MD_INLINE + "\n" +
            "\n" +
            "      <!--\n" +
            "        An example of metadata that exists as a file, in this case it is\n" +
            "        submitted in the SIP\n" +
            "      -->\n" +
            "      <metadata ref=\"" + DU_MDREF + "\" />\n" +
            "\n" +
            "    </DeliverableUnit>\n" +
            "  </DeliverableUnits>\n" +
            "\n" +
            "\n" +
            "  <Manifestations>\n" +
            "    <Manifestation id=\"" + MANIFESTATION_ID + "\">\n" +
            "      <deliverableUnit ref=\"" + DU_ID + "\" />\n" +
            "      <manifestationFile ref=\"" + FILE_ID1 + "\">\n" +
            "        <path>" + MF_FILEPATH + "</path>\n" +
            "      </manifestationFile>\n" +
            "    </Manifestation>\n" +
            "  </Manifestations>\n" +
            "\n" +
            "\n" +
            "  <Files>\n" +
            "\n" +
            "    <!--\n" +
            "      An example of an externally located file that will not be\n" +
            "      preserved in the DCS archive (e.g. DCS will curate it, but not\n" +
            "      transfer the bytes to the archive. It will remain an external\n" +
            "      reference)\n" +
            "    -->\n" +
            "    <File id=\"" + FILE_ID1 + "\" src=\"" + FILE_SRC1 + "\">\n" +
            "      <fileName>" + FILE_NAME1 + "</fileName>\n" +
            "      <extant>" + FILE_EXT1 + "</extant>\n" +
            "      <format>\n" +
            "        <id scheme=\""+ FORMAT_SCHEMEID1 + "\">" + FORMAT1 + "</id>\n" +
            "        <name>" + FORMAT_NAME1 + "</name>\n" +
            "        <version>" + FORMAT_VERSION1 + "</version>\n" +
            "      </format>\n" +
            "    </File>\n" +
            "\n" +
            "    <!--\n" +
            "      An example of a file that has been previously uploaded to the DCS\n" +
            "      ingest API. The src value was returned by the ingest API and used\n" +
            "      here.\n" +
            "    -->\n" +
            "    <File id=\"" + FILE_ID2 + "\" src=\"" + FILE_SRC2 + "\">\n" +
            "      <fileName>" + FILE_NAME2 + "</fileName>\n" +
            "      <extant>" + FILE_EXT2 + "</extant>\n" +
            "      <fixity algorithm=\"" + FIXITY_ALGO + "\">" + FIXITY_VALUE + "</fixity>\n" +
            "      <format>\n" +
            "        <id scheme=\"" + FORMAT_SCHEMEID2 + "\">" + FORMAT2 + "</id>\n" +
            "      </format>\n" +
            "      <format>\n" +
            "        <id scheme=\"" + FORMAT_SCHEMEID3 + "\">" + FORMAT3 + "</id>\n" +
            "      </format>\n" +
            "    </File>\n" +
            "\n" +
            "  </Files>\n" +
            "</dcp>";

    private Dcp sip;

    @Before
    public void setUp() {
        super.setUp();
        final DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(DU_ID);
        du.setTitle(DU_TITLE);
        du.addCreator(DU_CREATOR);
        du.addSubject(DU_SUBJECT);
        du.addFormerExternalRef(DU_EXTREF);

        final DcsCollectionRef collRef = new DcsCollectionRef();
        collRef.setRef(DU_COLL_REF);
        du.addCollection(collRef);

        final DcsMetadataRef mdRef = new DcsMetadataRef();
        mdRef.setRef(DU_MDREF);
        du.addMetadataRef(mdRef);

        final DcsMetadata mdInline = new DcsMetadata();
        mdInline.setSchemaUri(DU_MD_INLINE_SCHEMA);
        mdInline.setMetadata(DU_MD_BLOB);
        du.addMetadata(mdInline);

        final DcsManifestation man = new DcsManifestation();
        man.setId(MANIFESTATION_ID);
        man.setDeliverableUnit(DU_ID);

        final DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(FILE_ID1));
        mf.setPath(MF_FILEPATH);
        man.addManifestationFile(mf);

        final DcsFile fileOne = new DcsFile();
        fileOne.setId(FILE_ID1);
        fileOne.setSource(FILE_SRC1);
        fileOne.setName(FILE_NAME1);
        fileOne.setExtant(Boolean.parseBoolean(FILE_EXT1));
        final DcsFormat fileOneFmt = new DcsFormat();
        fileOneFmt.setSchemeUri(FORMAT_SCHEMEID1);
        fileOneFmt.setFormat(FORMAT1);
        fileOneFmt.setVersion(FORMAT_VERSION1);
        fileOneFmt.setName(FORMAT_NAME1);
        fileOne.addFormat(fileOneFmt);

        final DcsFile fileTwo = new DcsFile();
        fileTwo.setId(FILE_ID2);
        fileTwo.setSource(FILE_SRC2);
        fileTwo.setName(FILE_NAME2);
        fileTwo.setExtant(Boolean.parseBoolean(FILE_EXT2));

        final DcsFormat fileTwoFmt1 = new DcsFormat();
        fileTwoFmt1.setSchemeUri(FORMAT_SCHEMEID2);
        fileTwoFmt1.setFormat(FORMAT2);
        fileTwo.addFormat(fileTwoFmt1);

        final DcsFormat fileTwoFmt2 = new DcsFormat();
        fileTwoFmt2.setSchemeUri(FORMAT_SCHEMEID3);
        fileTwoFmt2.setFormat(FORMAT3);
        fileTwo.addFormat(fileTwoFmt2);

        final DcsFixity fixity = new DcsFixity();
        fixity.setAlgorithm(FIXITY_ALGO);
        fixity.setValue(FIXITY_VALUE);
        fileTwo.addFixity(fixity);

        sip = new Dcp();
        sip.addDeliverableUnit(du);
        sip.addManifestation(man);
        sip.addFile(fileOne);
        sip.addFile(fileTwo);
    }


    @Test
    public void testMarshal() throws IOException, SAXException {
        Diff diff = new Diff(XML, x.toXML(sip));
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual("Expected: " + XML + " Actual: " + x.toXML(sip), diff, true);

        diff = new Diff(XML, x.toXML(x.fromXML(XML)));
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual("Expected: " + XML + " Actual: " + x.toXML(x.fromXML(XML)), diff, true);
    }

    @Test
    public void testUnmarshal() {
        assertEquals(sip, x.fromXML(XML));
    }

}
