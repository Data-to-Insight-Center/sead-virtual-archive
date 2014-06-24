/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.archive.impl.dspacerepo;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsFile;
import org.junit.BeforeClass;
import org.junit.Test;
import org.purl.eprint.epdcx.x20061116.*;
import org.seadva.model.SeadRepository;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Test cases for DSpace deposit
 */
public class SeadDSpaceTest {

    static Credential credential = null;
    static String seadCommunity = null;
    @BeforeClass
    public static void init() throws IOException, InvalidXmlException, XmlPullParserException {
        Map<String,Credential> repoCredentials = Util.loadCredentials(SeadDSpace.class.getResource("/RepositoryCredentials.xml").openStream());
        ResearchObject pkg = new SeadXstreamStaxModelBuilder().buildSip(SeadDSpaceTest.class.getResourceAsStream("/sip_dspace.xml"));
        for(SeadRepository institionalRepository:pkg.getRepositories()){
            seadCommunity =  institionalRepository.getUrl();
            credential = repoCredentials.get((String)institionalRepository.getIrId());
        }
    }

    //Test creation of a community in DSpace
    @Test
    public void testCreateCommunity() throws IOException, XmlPullParserException, AIPFormatException, InvalidXmlException {

//        seadCommunity = "http://poplar.dlib.indiana.edu:8250/sead/communities/202";   //IU
        seadCommunity = "http://seadtest.ideals.illinois.edu/sead/communities/2";     //UIUC
        DSpaceCommunity comm = new SeadDSpace(credential.getUsername(),credential.getPassword()).createSubCommunity(seadCommunity,"Test sead community","test");
        assertNotNull(comm);
        assertNotNull(comm.getHandle());
        assertNotNull(comm.getId());
    }

    static String collection = null;
    @Test
    public void testCreateCollection() throws IOException, XmlPullParserException, AIPFormatException, InvalidXmlException {
        seadCommunity = "http://poplar.dlib.indiana.edu:8250/sead/communities/202";   //IU
        seadCommunity = "http://seadtest.ideals.illinois.edu/sead/communities/2";     //UIUC
        collection = new SeadDSpace(credential.getUsername(),credential.getPassword()).createCollection(seadCommunity,"Test sead collection");
        assertNotNull(collection);
    }

    @Test
    public void testSWORDUpload() throws IOException, AIPFormatException, XmlPullParserException, InvalidXmlException {

          collection = "http://seadtest.ideals.illinois.edu/sword/deposit/123456789/366";
//        collection = "http://poplar.dlib.indiana.edu:8250/sword/deposit/2022/16306";
        for(int i =0;i<1;i++){
        Map<String,String> result = new SeadDSpace(credential.getUsername(),credential.getPassword()).uploadPackage(
                collection
                , true
                , SeadDSpaceTest.class.getResource("/example.zip").getPath()
        );
            assertEquals(5, result.size());
        }

    }

    @Test
    public void testDescriptiveMetadataCreation(){
        String expectedXml = "<ns:descriptionSet xmlns:ns=\"http://purl.org/eprint/epdcx/2006-11-16/\">" +
                "<ns:description ns:resourceId=\"sword-mets-epdcx-1\">" +
                "<ns:statement ns:propertyURI=\"http://purl.org/dc/elements/1.1/type\" ns:valueURI=\"http://purl.org/eprint/entityType/ScholarlyWork\"/>" +
                "<ns:statement ns:propertyURI=\"http://purl.org/dc/elements/1.1/title\" ns:valueURI=\"title\"/>" +
                "<ns:statement ns:propertyURI=\"http://purl.org/dc/terms/abstract\" ns:valueURI=\"abstr\"/><ns:statement ns:propertyURI=\"http://purl.org/dc/elements/1.1/creator\" ns:valueURI=\"creator\"/>" +
                "<ns:statement ns:propertyURI=\"http://purl.org/dc/elements/1.1/identifier\" ns:valueURI=\"dummyDOI\">" +
                "<ns:valueString ns:sesURI=\"http://purl.org/dc/terms/URI\"/></ns:statement><ns:statement ns:propertyURI=\"http://purl.org/eprint/terms/isExpressedAs\" ns:valueURI=\"sword-mets-expr-1\"/>" +
                "</ns:description><ns:description ns:resourceId=\"sword-mets-expr-1\">" +
                "<ns:statement ns:propertyURI=\"http://purl.org/dc/elements/1.1/type\" ns:valueURI=\"http://purl.org/eprint/entityType/Expression\"/>" +
                "<ns:statement ns:propertyURI=\"http://purl.org/dc/elements/1.1/language\" ns:vesURI=\"http://purl.org/dc/terms/RFC3066\" ns:valueURI=\"en\"/></ns:description></ns:descriptionSet>";
        DescriptionSetDocument descriptionSetDocument = DescriptionSetDocument.Factory.newInstance();
        DescriptionSetElement descriptionSetElement = descriptionSetDocument.addNewDescriptionSet();
        DescriptionElement descriptionElement = descriptionSetElement.addNewDescription();

        descriptionElement.setResourceId("sword-mets-epdcx-1");
        StatementElement statementElement = descriptionElement.addNewStatement();
        statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/type");
        statementElement.setValueURI("http://purl.org/eprint/entityType/ScholarlyWork");

        statementElement = descriptionElement.addNewStatement();
        statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/title");
        statementElement.setValueURI("title");


        statementElement = descriptionElement.addNewStatement();
        statementElement.setPropertyURI("http://purl.org/dc/terms/abstract");
        statementElement.setValueURI("abstr");

        statementElement = descriptionElement.addNewStatement();
        statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/creator");
        statementElement.setValueURI("creator");

        statementElement = descriptionElement.addNewStatement();
        statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/identifier");
        statementElement.setValueURI("dummyDOI");
        ValueStringElement valueStringElement = statementElement.addNewValueString();
        valueStringElement.setSesURI("http://purl.org/dc/terms/URI");


        statementElement = descriptionElement.addNewStatement();
        statementElement.setPropertyURI("http://purl.org/eprint/terms/isExpressedAs");
        statementElement.setValueURI("sword-mets-expr-1");

        DescriptionElement descriptionElement_expr = descriptionSetElement.addNewDescription();
        descriptionElement_expr.setResourceId("sword-mets-expr-1");

        statementElement = descriptionElement_expr.addNewStatement();
        statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/type");
        statementElement.setValueURI("http://purl.org/eprint/entityType/Expression");

        statementElement = descriptionElement_expr.addNewStatement();
        statementElement.setPropertyURI("http://purl.org/dc/elements/1.1/language");
        statementElement.setVesURI("http://purl.org/dc/terms/RFC3066");
        statementElement.setValueURI("en");

        assertEquals(expectedXml,descriptionSetDocument.xmlText());
    }


    @Test
    public void testSipUploadUIUC() throws FileNotFoundException, AIPFormatException, InvalidXmlException {
        ResearchObject sip = new SeadXstreamStaxModelBuilder().buildSip(SeadDSpaceTest.class.getResourceAsStream("/sip_dspace.xml"));
        Collection<DcsFile> files = sip.getFiles();
        for(DcsFile file:files){
            file.setSource("file://"+SeadDSpace.class.getResource("/vortex_mining.xlsx").getPath());
        }
        sip.setFiles(files);
        new SeadXstreamStaxModelBuilder().buildSip(sip, new FileOutputStream(SeadDSpace.class.getResource("/sip_dspace.xml").getPath()));
        new DspaceRepoArchiveStore().putResearchPackage(SeadDSpaceTest.class.getResourceAsStream("/sip_dspace.xml"));

    }

    @Test
    public void testSipUploadIU() throws FileNotFoundException, AIPFormatException, InvalidXmlException {
        ResearchObject sip = new SeadXstreamStaxModelBuilder().buildSip(SeadDSpaceTest.class.getResourceAsStream("/sip_dspace_iu.xml"));
        Collection<DcsFile> files = sip.getFiles();
        for(DcsFile file:files){
            file.setSource("file://"+SeadDSpace.class.getResource("/vortex_mining.xlsx").getPath());
        }
        sip.setFiles(files);
        new SeadXstreamStaxModelBuilder().buildSip(sip, new FileOutputStream(SeadDSpace.class.getResource("/sip_dspace_iu.xml").getPath()));
        new DspaceRepoArchiveStore().putResearchPackage(SeadDSpaceTest.class.getResourceAsStream("/sip_dspace_iu.xml"));
    }
}
