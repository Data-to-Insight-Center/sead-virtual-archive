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

import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ManifestationConverterTest extends AbstractXstreamConverterTest {

    private static final String ID = "urn:sdss:12345/manifestation";
    private static final String DATE_CREATED = "2010-12-03T03:23:23Z";
    private static final String DU_REF = "urn:sdss:12345";
    private static final String MF_REF = "urn:sdss:12345/FITS_FILE";
    private static final String MF_PATH = "/scans/5/";
    private static final String METADATA_REL = DcsRelationship.IS_METADATA_FOR.asString();

    private static final String XML = "<Manifestation xmlns=\"" + XMLNS + "\" id=\"" + ID + "\">\n" +
            "      <deliverableUnit ref=\"" + DU_REF + "\" />\n" +
            "      <" + ManifestationFileConverter.E_MANFILE + " ref=\"" + MF_REF + "\">\n" +
            "        <path>" + MF_PATH + "</path>\n" +
            "      </" + ManifestationFileConverter.E_MANFILE + ">\n" +
            "    </Manifestation>";
    
    private static final String XML_DATE = "<Manifestation xmlns=\"" + XMLNS + "\" id=\"" + ID + "\" dateCreated=\"" + DATE_CREATED + "\"  >\n" +
            "      <deliverableUnit ref=\"" + DU_REF + "\" />\n" +
            "      <" + ManifestationFileConverter.E_MANFILE + " ref=\"" + MF_REF + "\">\n" +
            "        <path>" + MF_PATH + "</path>\n" +
            "      </" + ManifestationFileConverter.E_MANFILE + ">\n" +
            "    </Manifestation>";

    private static final String XML_REL = "<Manifestation xmlns=\"" + XMLNS + "\" id=\"" + ID + "\" dateCreated=\"" + DATE_CREATED + "\">\n" +
            "      <deliverableUnit ref=\"" + DU_REF + "\" />\n" +
            "      <" + ManifestationFileConverter.E_MANFILE + " ref=\"" + MF_REF + "\">\n" +
            "        <path>" + MF_PATH + "</path>\n" +
            "        <" + RelationConverter.E_RELATION  + " " + RelationConverter.A_REF + "=\"" + DU_REF + "\" " + RelationConverter.A_REL + "=\"" + METADATA_REL + "\"/>\n" +
            "      </" + ManifestationFileConverter.E_MANFILE + ">\n" +
            "    </Manifestation>";

    @Test
    public void testMarshal() throws Exception {
        final DcsManifestation man = new DcsManifestation();
        man.setDeliverableUnit(DU_REF);
        man.setId(ID);
        // TODO man.setMetadata();

        final DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(MF_REF));
        mf.setPath(MF_PATH);
        man.addManifestationFile(mf);

        XMLAssert.assertXMLEqual(XML, x.toXML(man));
        XMLAssert.assertXMLEqual(XML, x.toXML(x.fromXML(XML)));
    }
    
    @Test
    public void testMarshalWithDate() throws Exception {
        final DcsManifestation man = new DcsManifestation();
        man.setDeliverableUnit(DU_REF);
        man.setId(ID);
        man.setDateCreated(DATE_CREATED);
        // TODO man.setMetadata();

        final DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(MF_REF));
        mf.setPath(MF_PATH);
        man.addManifestationFile(mf);

        XMLAssert.assertXMLEqual(XML_DATE, x.toXML(man));
        XMLAssert.assertXMLEqual(XML_DATE, x.toXML(x.fromXML(XML_DATE)));
    }

    @Test
    public void testMarshalWithRelationship() throws Exception {
        final DcsManifestation man = new DcsManifestation();
        man.setDeliverableUnit(DU_REF);
        man.setId(ID);
        man.setDateCreated(DATE_CREATED);

        final DcsRelation rel = new DcsRelation();
        rel.setRef(new DcsEntityReference(DU_REF));
        rel.setRelUri(METADATA_REL);

        final DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(MF_REF));
        mf.setPath(MF_PATH);
        mf.addRel(rel);

        man.addManifestationFile(mf);

        XMLAssert.assertXMLEqual(XML_REL, x.toXML(man));
        XMLAssert.assertXMLEqual(XML_REL, x.toXML(x.fromXML(XML_REL)));
    }

    @Test
    public void testUnmarshal() throws Exception {
        final DcsManifestation man = new DcsManifestation();
        man.setDeliverableUnit(DU_REF);
        man.setId(ID);
        // TODO man.setMetadata();

        final DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(MF_REF));
        mf.setPath(MF_PATH);
        man.addManifestationFile(mf);

        assertEquals(man, x.fromXML(XML));
        assertEquals(man, x.fromXML(x.toXML(man)));
    }
    
    @Test
    public void testUnmarshalWithDate() throws Exception {
        final DcsManifestation man = new DcsManifestation();
        man.setDeliverableUnit(DU_REF);
        man.setId(ID);
        man.setDateCreated(DATE_CREATED);        
        // TODO man.setMetadata();

        final DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(MF_REF));
        mf.setPath(MF_PATH);
        man.addManifestationFile(mf);

        assertEquals(man, x.fromXML(XML_DATE));
        assertEquals(man, x.fromXML(x.toXML(man)));
    }

    @Test
    public void testUnmarshalWithRelationship() throws Exception {
        final DcsManifestation man = new DcsManifestation();
        man.setDeliverableUnit(DU_REF);
        man.setId(ID);
        man.setDateCreated(DATE_CREATED);

        final DcsRelation rel = new DcsRelation();
        rel.setRef(new DcsEntityReference(DU_REF));
        rel.setRelUri(METADATA_REL);

        final DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(MF_REF));
        mf.setPath(MF_PATH);
        mf.addRel(rel);

        man.addManifestationFile(mf);

        log.info(XML_REL);

        assertEquals(man, x.fromXML(XML_REL));
        assertEquals(man, x.fromXML(x.toXML(man)));
    }
}
