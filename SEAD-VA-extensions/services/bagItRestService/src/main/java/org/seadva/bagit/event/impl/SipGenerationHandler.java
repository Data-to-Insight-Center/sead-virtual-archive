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
package org.seadva.bagit.event.impl;

import org.apache.commons.io.FileUtils;
import org.dataconservancy.model.dcs.*;
import org.dspace.foresite.*;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Handler;
import org.seadva.bagit.model.MediciInstance;
import org.seadva.bagit.util.Constants;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadFile;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.List;

/**
 * Handler to generate SIP from ORE
 */
public class SipGenerationHandler implements Handler{

    private static Predicate DC_TERMS_IDENTIFIER = null;
    private static Predicate DC_TERMS_SOURCE = null;
    private static Predicate DC_TERMS_TITLE = null;
    private static Predicate DC_TERMS_FORMAT = null;
    private static Predicate DC_TERMS_ABSTRACT = null;

    private static Predicate CITO_IS_DOCUMENTED_BY = null;
    private static Predicate DC_TERMS_TYPE = null;

    private static Predicate CITO_DOCUMENTS = null;
    private static String DEFAULT_URL_PREFIX = "http://";


    public SipGenerationHandler() throws URISyntaxException, OREException {

        // use as much as we can from the included Vocab for dcterms:Agent
        DC_TERMS_IDENTIFIER = new Predicate();
        DC_TERMS_IDENTIFIER.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_IDENTIFIER.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_IDENTIFIER.setName("identifier");
        DC_TERMS_IDENTIFIER.setURI(new URI(Constants.identifierTerm));

        DC_TERMS_TITLE = new Predicate();
        DC_TERMS_TITLE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_TITLE.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_TITLE.setName("title");
        DC_TERMS_TITLE.setURI(new URI(Constants.titleTerm));


        DC_TERMS_FORMAT = new Predicate();
        DC_TERMS_FORMAT.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_FORMAT.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_FORMAT.setName("format");
        DC_TERMS_FORMAT.setURI(new URI(Constants.formatTerm));

        DC_TERMS_ABSTRACT = new Predicate();
        DC_TERMS_ABSTRACT.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_ABSTRACT.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_ABSTRACT.setName("abstract");
        DC_TERMS_ABSTRACT.setURI(new URI(DC_TERMS_ABSTRACT.getNamespace()
                + DC_TERMS_ABSTRACT.getName()));

        DC_TERMS_SOURCE = new Predicate();
        DC_TERMS_SOURCE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_SOURCE.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_SOURCE.setName("source");
        DC_TERMS_SOURCE.setURI(new URI(DC_TERMS_SOURCE.getNamespace()
                + DC_TERMS_SOURCE.getName()));

        // create the CITO:isDocumentedBy predicate
        CITO_IS_DOCUMENTED_BY = new Predicate();
        CITO_IS_DOCUMENTED_BY.setNamespace("http://purl.org/spar/cito/");
        CITO_IS_DOCUMENTED_BY.setPrefix("cito");
        CITO_IS_DOCUMENTED_BY.setName("isDocumentedBy");
        CITO_IS_DOCUMENTED_BY.setURI(new URI(CITO_IS_DOCUMENTED_BY.getNamespace()
                + CITO_IS_DOCUMENTED_BY.getName()));

        DC_TERMS_TYPE = new Predicate();
        DC_TERMS_TYPE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_TYPE.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_TYPE.setName("type");
        DC_TERMS_TYPE.setURI(new URI(DC_TERMS_TYPE.getNamespace()
                + DC_TERMS_TYPE.getName()));

        // create the CITO:documents predicate
        CITO_DOCUMENTS = new Predicate();
        CITO_DOCUMENTS.setNamespace(CITO_IS_DOCUMENTED_BY.getNamespace());
        CITO_DOCUMENTS.setPrefix(CITO_IS_DOCUMENTED_BY.getPrefix());
        CITO_DOCUMENTS.setName("documents");
        CITO_DOCUMENTS.setURI(new URI(CITO_DOCUMENTS.getNamespace()
                + CITO_DOCUMENTS.getName()));
    }

    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {
        try {
            generateSIP( packageDescriptor.getPackageId(), null, packageDescriptor.getUnzippedBagPath());
            String sipPath =
                    packageDescriptor.getUnzippedBagPath() +"sip.xml";

            File sipFile = new File(sipPath);

            OutputStream out = FileUtils.openOutputStream(sipFile);
            new SeadXstreamStaxModelBuilder().buildSip(sip, out);
            out.close();
            packageDescriptor.setSipPath(sipPath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packageDescriptor;
    }


    ResearchObject sip = new ResearchObject();
    public void generateSIP(String collectionId, String parentId, String unzippedDir) throws FileNotFoundException {//top collection id


        try{
            collectionId = collectionId.split("/")[collectionId.split("/").length-1];
            String duId = collectionId;

            InputStream input = new FileInputStream(new File(unzippedDir +"/"+ collectionId + "_oaiore.xml"));
            OREParser parser = OREParserFactory.getInstance("RDF/XML");
            ResourceMap rem = parser.parse(input);

            SeadDeliverableUnit du = new SeadDeliverableUnit();

           /* if(metadataTriples.size()>0){
                String[] fgdcFilePath = metadataTriples.get(0).getObjectLiteral().split("/");

                du = FgdcGenerator.fromFGDC(unzippedDir + fgdcFilePath[fgdcFilePath.length - 1], du);
            }*/
            if(du.getTitle()==null){
                TripleSelector titleSelector = new TripleSelector();
                titleSelector.setSubjectURI(rem.getURI());
                titleSelector.setPredicate(DC_TERMS_TITLE);
                List<Triple> titleTriples = rem.listAllTriples(titleSelector);

                if(titleTriples.size()>0){
                    du.setTitle(titleTriples.get(0).getObjectLiteral());
                }
            }

            TripleSelector idSelector = new TripleSelector();
            idSelector.setSubjectURI(rem.getURI());
            idSelector.setPredicate(DC_TERMS_IDENTIFIER);
            List<Triple> idTriples = rem.listAllTriples(idSelector);

            if(idTriples.size()>0){
                duId = idTriples.get(0).getObjectLiteral().replace("_Aggregation", "");
            }

            du.setId(duId);

            TripleSelector typeSelector = new TripleSelector();
            typeSelector.setSubjectURI(rem.getURI());
            typeSelector.setPredicate(DC_TERMS_TYPE);
            List<Triple> typeTriples = rem.listAllTriples(typeSelector);

            if(typeTriples.size()>0){

                for(MediciInstance instance: Constants.acrInstances){
                    if(instance.getType().equalsIgnoreCase(typeTriples.get(0).getObjectLiteral())) {
                        DcsResourceIdentifier duAltId = new DcsResourceIdentifier();
                        duAltId.setIdValue(duId);
                        duAltId.setTypeId(instance.getType());
                        du.addAlternateId(duAltId);
                        break;
                    }
                }
            }

            //get any metadata file associated
            TripleSelector tripleSelector = new TripleSelector();
            tripleSelector.setSubjectURI(rem.getURI());
            tripleSelector.setPredicate(CITO_IS_DOCUMENTED_BY);
            List<Triple> metadataTriples = rem.listAllTriples(tripleSelector);

            if(metadataTriples!=null && metadataTriples.size()>0){
                for(Triple metadataTriple: metadataTriples){
                    DcsMetadataRef metadataRef = new DcsMetadataRef();
                    metadataRef.setRef(metadataTriples.get(0).getObjectLiteral());
                    du.addMetadataRef();
                }
            }
            //get root DU

            Aggregation agg = rem.getAggregation();

            if(parentId!=null){
                DcsDeliverableUnitRef ref = new DcsDeliverableUnitRef();
                ref.setRef(parentId);
                du.addParent(ref);
            }


            sip.addDeliverableUnit(du);
            //get any children files or sub-collections associated and recursively add them
            List<AggregatedResource> aggregatedResources = agg.getAggregatedResources();
            boolean filesExist = false;
            DcsManifestation manifestation = new DcsManifestation();
            manifestation.setId(duId+"man");
            manifestation.setDeliverableUnit(duId);
            for(AggregatedResource aggregatedResource:aggregatedResources){
                List<URI> types = aggregatedResource.getTypes();
                if(types==null||types.size()==0){//file
                    if(!filesExist){
                        filesExist = true;
                    }
                    SeadFile file = new SeadFile();
                    TripleSelector selector = new TripleSelector();
                    selector.setSubjectURI(aggregatedResource.getURI());
                    selector.setPredicate(DC_TERMS_IDENTIFIER);
                    List<Triple> triples = aggregatedResource.listAllTriples(selector);
                    file.setId(triples.get(0).getObjectLiteral());


                    TripleSelector filetypeSelector = new TripleSelector();
                    filetypeSelector.setSubjectURI(rem.getURI());
                    filetypeSelector.setPredicate(DC_TERMS_TYPE);
                    List<Triple> filetypeTriples = rem.listAllTriples(filetypeSelector);

                    if(filetypeTriples.size()>0){

                        for(MediciInstance instance: Constants.acrInstances){
                            if(instance.getType().equalsIgnoreCase(filetypeTriples.get(0).getObjectLiteral())) {
                                DcsResourceIdentifier altId = new DcsResourceIdentifier();
                                altId.setIdValue(triples.get(0).getObjectLiteral());
                                altId.setTypeId(instance.getType());
                                file.addAlternateId(altId);
                                break;
                            }
                        }
                    }


                    TripleSelector sourceselector = new TripleSelector();
                    sourceselector.setSubjectURI(aggregatedResource.getURI());
                    selector.setPredicate(DC_TERMS_SOURCE);
                    List<Triple> sourcetriples = aggregatedResource.listAllTriples(selector);

                    if(sourcetriples.size()>0)
                        file.setSource(sourcetriples.get(0).getObjectLiteral());

                    selector = new TripleSelector();
                    selector.setSubjectURI(aggregatedResource.getURI());
                    selector.setPredicate(DC_TERMS_TITLE);
                    triples = aggregatedResource.listAllTriples(selector);
                    file.setName(triples.get(0).getObjectLiteral());


                    selector = new TripleSelector();
                    selector.setSubjectURI(aggregatedResource.getURI());
                    selector.setPredicate(DC_TERMS_FORMAT);
                    triples = aggregatedResource.listAllTriples(selector);
                    if(triples.size()>0){
                        DcsFormat format = new DcsFormat();
                        format.setSchemeUri(Constants.FORMAT_IANA_SCHEME);
                        format.setFormat(triples.get(0).getObjectLiteral());
                        file.addFormat(format);
                    }

                    file.setExtant(true);

                    DcsManifestationFile manifestationFile = new DcsManifestationFile();
                    DcsFileRef ref = new DcsFileRef();
                    ref.setRef(file.getId());
                    manifestationFile.setRef(ref);
                    manifestation.addManifestationFile(manifestationFile);
                    sip.addFile(file);

                }
                else //if(types.get(0)==agg.getTypes().get(0))
                {//type sub-aggregation/sub collection
                    String encodedId= aggregatedResource.getURI().toString();
                    if(encodedId.contains("uri="))
                        encodedId = encodedId.split("uri=")[1];
                    String newId = URLDecoder.decode(encodedId);
                    generateSIP(newId,duId,unzippedDir);
                }
            }
            if(filesExist)
                sip.addManifestation(manifestation);
        }
        catch (OREParserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OREException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
