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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.io.FileUtils;
import org.dataconservancy.model.dcs.*;
import org.dspace.foresite.*;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Handler;
import org.seadva.bagit.model.MediciInstance;
import org.seadva.bagit.util.Constants;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadFile;
import org.seadva.model.SeadPerson;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Handler to generate SIP from ORE
 */
public class DPNSipGenerationHandler implements Handler{

    private static Predicate DC_TERMS_IDENTIFIER = null;
    private static Predicate DC_TERMS_SOURCE = null;
    private static Predicate METS_LOCATION = null;
    private static Predicate DC_TERMS_TITLE = null;
    private static Predicate DC_TERMS_FORMAT = null;
    private static Predicate DC_TERMS_ABSTRACT = null;
    private static Predicate DC_REFERENCES = null;
    private static Predicate DC_TERMS_RIGHTS = null;
    private static Predicate DC_TERMS_CONTRIBUTOR = null;

    private static Predicate CITO_IS_DOCUMENTED_BY = null;
    private static Predicate DC_TERMS_TYPE = null;

    private static Predicate CITO_DOCUMENTS = null;
    private static String DEFAULT_URL_PREFIX = "http://";

    List<URI> knownCollectionPredicates;
    List<URI> knownFilePredicates;


    public DPNSipGenerationHandler() throws URISyntaxException, OREException {

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
        DC_TERMS_SOURCE.setURI(new URI(Constants.sourceTerm));

        DC_TERMS_CONTRIBUTOR = new Predicate();
        DC_TERMS_CONTRIBUTOR.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_CONTRIBUTOR.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_CONTRIBUTOR.setName("contributor");
        DC_TERMS_CONTRIBUTOR.setURI(new URI(Constants.contributor));

        METS_LOCATION = new Predicate();
        METS_LOCATION.setNamespace("http://www.loc.gov/METS");
        METS_LOCATION.setPrefix("http://www.loc.gov/METS");
        METS_LOCATION.setName("FLocat");
        METS_LOCATION.setURI(new URI("http://www.loc.gov/METS/FLocat"));

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

        DC_TERMS_RIGHTS = new Predicate();
        DC_TERMS_RIGHTS.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_RIGHTS.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_RIGHTS.setName("rights");
        DC_TERMS_RIGHTS.setURI(new URI(DC_TERMS_RIGHTS.getNamespace()
                + DC_TERMS_RIGHTS.getName()));

        DC_REFERENCES = new Predicate();
        DC_REFERENCES.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_REFERENCES.setPrefix(Vocab.dcterms_Agent.schema());
        DC_REFERENCES.setName("references");
        DC_REFERENCES.setURI(new URI(DC_REFERENCES.getNamespace()
                + DC_REFERENCES.getName()));

        // create the CITO:documents predicate
        CITO_DOCUMENTS = new Predicate();
        CITO_DOCUMENTS.setNamespace(CITO_IS_DOCUMENTED_BY.getNamespace());
        CITO_DOCUMENTS.setPrefix(CITO_IS_DOCUMENTED_BY.getPrefix());
        CITO_DOCUMENTS.setName("documents");
        CITO_DOCUMENTS.setURI(new URI(CITO_DOCUMENTS.getNamespace()
                + CITO_DOCUMENTS.getName()));

        knownCollectionPredicates = new ArrayList<URI>();
        knownCollectionPredicates.add(DC_TERMS_IDENTIFIER.getURI());
        knownCollectionPredicates.add(DC_TERMS_TITLE.getURI());
        knownCollectionPredicates.add(DC_TERMS_FORMAT.getURI());
        knownCollectionPredicates.add(DC_TERMS_ABSTRACT.getURI());
        knownCollectionPredicates.add(DC_TERMS_SOURCE.getURI());
        knownCollectionPredicates.add(CITO_IS_DOCUMENTED_BY.getURI());
        knownCollectionPredicates.add(DC_TERMS_TYPE.getURI());
        knownCollectionPredicates.add(DC_TERMS_RIGHTS.getURI());
        knownCollectionPredicates.add(CITO_DOCUMENTS.getURI());
        knownCollectionPredicates.add(DC_REFERENCES.getURI());
        knownCollectionPredicates.add(DC_TERMS_CONTRIBUTOR.getURI());

        knownFilePredicates = new ArrayList<URI>();
        knownFilePredicates.add(DC_TERMS_IDENTIFIER.getURI());
        knownFilePredicates.add(DC_TERMS_TITLE.getURI());
        knownFilePredicates.add(DC_TERMS_FORMAT.getURI());
        knownFilePredicates.add(METS_LOCATION.getURI());
        knownFilePredicates.add(CITO_IS_DOCUMENTED_BY.getURI());
        knownFilePredicates.add(DC_TERMS_TYPE.getURI());
        knownFilePredicates.add(new URI("http://www.openarchives.org/ore/terms/isAggregatedBy"));
        knownFilePredicates.add(DC_REFERENCES.getURI());
        knownFilePredicates.add(new URI("http://purl.org/dc/terms/isReferencedBy"));//we do only references and not referencedBy

    }

    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {
        try {
            generateSIP( packageDescriptor.getPackageId(), null, packageDescriptor.getUntarredBagPath(), packageDescriptor.getPackageName());
            String sipPath =
                    packageDescriptor.getUntarredBagPath() +"IU-tags/IU-sip.xml";

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
    public void generateSIP(String collectionId, String parentId, String untarredDir,String packageName) throws FileNotFoundException {//top collection id


        try{
            collectionId = collectionId.split("/")[collectionId.split("/").length-1];
            String duId = collectionId;

            InputStream input = new FileInputStream(new File(untarredDir +"/IU-tags/IU-"+ collectionId + "_oaiore.xml"));
            OREParser parser = OREParserFactory.getInstance("RDF/XML");
            ResourceMap rem = parser.parse(input);

            SeadDeliverableUnit du = new SeadDeliverableUnit();
            du.setType("PublishedObject");

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

            DcsResourceIdentifier dpnAltId = new DcsResourceIdentifier();
            dpnAltId.setIdValue(packageName);
            dpnAltId.setTypeId("dpnobjectid");
            du.addAlternateId(dpnAltId);

            TripleSelector idSelector = new TripleSelector();
            idSelector.setSubjectURI(rem.getURI());
            idSelector.setPredicate(DC_TERMS_IDENTIFIER);
            List<Triple> idTriples = rem.listAllTriples(idSelector);

            if(idTriples.size()>0){
                duId = idTriples.get(0).getObjectLiteral().replace("_Aggregation", "");
            }

            du.setId(duId);

            TripleSelector abstractSelector = new TripleSelector();
            abstractSelector.setSubjectURI(rem.getURI());
            abstractSelector.setPredicate(DC_TERMS_ABSTRACT);
            List<Triple> abstractTriples = rem.listAllTriples(abstractSelector);

            if(abstractTriples.size()>0){
                du.setAbstrct(abstractTriples.get(0).getObjectLiteral());
            }

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
                    metadataRef.setRef(metadataTriple.getObjectLiteral());
                    du.addMetadataRef(metadataRef);
                }
            }

            TripleSelector refTripleSelector = new TripleSelector();
            refTripleSelector.setSubjectURI(rem.getURI());
            refTripleSelector.setPredicate(DC_REFERENCES);
            metadataTriples = rem.listAllTriples(refTripleSelector);

            if(metadataTriples!=null && metadataTriples.size()>0){
                for(Triple metadataTriple: metadataTriples){
                    DcsMetadataRef metadataRef = new DcsMetadataRef();
                    metadataRef.setRef(metadataTriple.getObjectLiteral());
                    du.addMetadataRef(metadataRef);
                }
            }

            TripleSelector contributorTripleSelector = new TripleSelector();
            contributorTripleSelector.setSubjectURI(rem.getURI());
            contributorTripleSelector.setPredicate(DC_TERMS_CONTRIBUTOR);
            List<Triple> contributorTriples = rem.listAllTriples(contributorTripleSelector);

            if(contributorTriples!=null && contributorTriples.size()>0){
                for(Triple contributorTriple: contributorTriples){
                    SeadPerson person = new SeadPerson();
                    person.setName(contributorTriple.getObjectLiteral());
                    //person.setId(UUID.randomUUID().toString());
                    du.addDataContributor(person);
                }
            }


            //get any rights associated with DU
            TripleSelector rightsTripleSelector = new TripleSelector();
            rightsTripleSelector.setSubjectURI(rem.getURI());
            rightsTripleSelector.setPredicate(DC_TERMS_RIGHTS);
            List<Triple> rightsTriples = rem.listAllTriples(rightsTripleSelector);

            if(rightsTriples!=null && rightsTriples.size()>0){
                for(Triple rightsTriple: rightsTriples){
                    du.setRights(rightsTriple.getObjectLiteral());
                }
            }

            //Get all triples not already assigned to a known predicate and add them as key value pairs that still get indexed

            List<Triple> allTriples = rem.listTriples();
            if(allTriples!=null)
                for(Triple triple: allTriples){
                    if(triple.getPredicate()!=null)
                        if(!knownCollectionPredicates.contains(triple.getPredicate().getURI())){

                            String predicate = triple.getPredicate().getURI().toString();

                            String objValue;
                            if(!triple.isLiteral()){
                                Object temp = triple.getObject();
                                if(temp==null)
                                    continue;
                                objValue = temp.toString();
                            }
                            else
                                objValue = triple.getObjectLiteral();

                            DcsMetadata dcsMetadata = new DcsMetadata();

                            String splitChar = "/";
                            if(predicate.contains("#"))
                                splitChar="#";
                            String ns = predicate.substring(0, predicate.lastIndexOf(splitChar));
                            dcsMetadata.setSchemaUri(ns);


                            Map<String,Object> map = new HashMap<String,Object>();
                            map.put(predicate, objValue);
                            XStream xStream = new XStream(new DomDriver());
                            xStream.alias("map", java.util.Map.class);
                            String metadata = xStream.toXML(map);
                            dcsMetadata.setMetadata(metadata);
                            du.addMetadata(dcsMetadata);
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
                    if(triples.size()>0)
                        file.setId(triples.get(0).getObjectLiteral());
                    else
                        file.setId(UUID.randomUUID().toString());



                    TripleSelector filetypeSelector = new TripleSelector();
                    filetypeSelector.setSubjectURI(aggregatedResource.getURI());
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
                    sourceselector.setPredicate(METS_LOCATION);
                    List<Triple> sourcetriples = aggregatedResource.listAllTriples(sourceselector);

                    if(sourcetriples.size()>0)
                    {
                        if(sourcetriples.get(0).getObjectLiteral().startsWith("http"))
                            file.setSource(sourcetriples.get(0).getObjectLiteral());
                        else
                            file.setSource("file://"+untarredDir+sourcetriples.get(0).getObjectLiteral());
                    }

                    selector = new TripleSelector();
                    selector.setSubjectURI(aggregatedResource.getURI());
                    selector.setPredicate(DC_TERMS_TITLE);
                    triples = aggregatedResource.listAllTriples(selector);
                    if(triples.size()>0)
                        file.setName(triples.get(0).getObjectLiteral());


                    refTripleSelector = new TripleSelector();
                    refTripleSelector.setSubjectURI(aggregatedResource.getURI());
                    refTripleSelector.setPredicate(DC_REFERENCES);
                    metadataTriples = aggregatedResource.listAllTriples(refTripleSelector);

                    if(metadataTriples!=null && metadataTriples.size()>0){
                        for(Triple metadataTriple: metadataTriples){
                            DcsMetadataRef metadataRef = new DcsMetadataRef();
                            metadataRef.setRef(metadataTriple.getObjectLiteral());
                            file.addMetadataRef(metadataRef);
                        }
                    }

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

                    //get any metadata file associated   with file
                    TripleSelector mdTripleSelector = new TripleSelector();
                    mdTripleSelector.setSubjectURI(aggregatedResource.getURI());
                    mdTripleSelector.setPredicate(CITO_IS_DOCUMENTED_BY);
                    List<Triple> fileMetadataTriples = aggregatedResource.listAllTriples(mdTripleSelector);

                    if(fileMetadataTriples!=null && fileMetadataTriples.size()>0){
                        for(Triple metadataTriple: fileMetadataTriples){
                            DcsMetadataRef metadataRef = new DcsMetadataRef();
                            metadataRef.setRef(metadataTriple.getObjectLiteral());
                            file.addMetadataRef(metadataRef);
                        }
                    }

                    //Get all triples not already assigned to a known predicate and add them as text metadata

                    List<Triple> allFileTriples = aggregatedResource.listTriples();
                    if(allFileTriples!=null)
                        for(Triple triple: allFileTriples){
                            if(triple.getPredicate()!=null)
                                if(!knownFilePredicates.contains(triple.getPredicate().getURI())){
                                    if(!triple.isLiteral())
                                        continue;
                                    DcsMetadata dcsMetadata = new DcsMetadata();
                                    String predicate = triple.getPredicate().getURI().toString();
                                    String splitChar = "/";
                                    if(predicate.contains("#"))
                                        splitChar="#";
                                    String ns = predicate.substring(0, predicate.lastIndexOf(splitChar));
                                    dcsMetadata.setSchemaUri(ns);

                                    Map<String,String> map = new HashMap<String, String>();
                                    map.put(predicate, triple.getObjectLiteral());
                                    XStream xStream = new XStream(new DomDriver());
                                    xStream.alias("map", java.util.Map.class);
                                    String metadata = xStream.toXML(map);
                                    dcsMetadata.setMetadata(metadata);
                                    file.addMetadata(dcsMetadata);
                                }
                        }

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
                    generateSIP(newId,duId,untarredDir,"");
                }
            }
            if(filesExist)
                sip.addManifestation(manifestation);
        }
        catch (OREParserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OREException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
