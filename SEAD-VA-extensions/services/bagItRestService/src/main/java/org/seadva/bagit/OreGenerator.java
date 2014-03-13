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

package org.seadva.bagit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataconservancy.model.dcs.*;
import org.dspace.foresite.*;
import org.dspace.foresite.jena.TripleJena;
import org.sead.acr.common.utilities.json.JSONException;
import org.seadva.bagit.model.CollectionNode;
import org.seadva.bagit.model.DatasetRelation;
import org.seadva.bagit.model.FileNode;
import org.seadva.bagit.model.MediciInstance;
import org.seadva.bagit.util.Constants;
import org.seadva.bagit.util.FgdcGenerator;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadFile;
import org.seadva.model.pack.ResearchObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * To ORE and back (used inside bag)
 */
public class OreGenerator {
    private static final String RESOURCE_MAP_SERIALIZATION_FORMAT = "RDF/XML";

    private static Predicate DC_TERMS_IDENTIFIER = null;
    private static Predicate DC_TERMS_TITLE = null;
    private static Predicate DC_TERMS_FORMAT = null;
    private static Predicate DC_TERMS_ABSTRACT = null;

    private static Predicate CITO_IS_DOCUMENTED_BY = null;
    private static Predicate DC_TERMS_TYPE = null;

    private static Predicate CITO_DOCUMENTS = null;

    VAQueryUtil util;


    private static Log log = LogFactory.getLog(OreGenerator.class);

    public OreGenerator() throws URISyntaxException, OREException {
        util=new VAQueryUtil();
        // use as much as we can from the included Vocab for dcterms:Agent
        DC_TERMS_IDENTIFIER = new Predicate();
        DC_TERMS_IDENTIFIER.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_IDENTIFIER.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_IDENTIFIER.setName("identifier");
        DC_TERMS_IDENTIFIER.setURI(new URI(DC_TERMS_IDENTIFIER.getNamespace()
                + DC_TERMS_IDENTIFIER.getName()));

        DC_TERMS_TITLE = new Predicate();
        DC_TERMS_TITLE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_TITLE.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_TITLE.setName("title");
        DC_TERMS_TITLE.setURI(new URI(DC_TERMS_TITLE.getNamespace()
                + DC_TERMS_TITLE.getName()));


        DC_TERMS_FORMAT = new Predicate();
        DC_TERMS_FORMAT.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_FORMAT.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_FORMAT.setName("format");
        DC_TERMS_FORMAT.setURI(new URI(DC_TERMS_FORMAT.getNamespace()
                + DC_TERMS_FORMAT.getName()));

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

    private static String DEFAULT_URL_PREFIX = "http://seadva.d2i.indiana.edu/";

    public OREResource getMainRem() {
        return mainRem;
    }

    public void setMainRem(OREResource mainRem) {
        this.mainRem = mainRem;
    }

    OREResource mainRem = null;

    private static Predicate DC_TERMS_SOURCE = null;

    public void toOAIORE(Aggregation agg,
                             OREResource rem,
                             String tagId,
                             String type, String bagPath,
                             String dataPath,
                             MediciInstance mediciInstance,
                             BufferedWriter fetch, BufferedWriter manifest) throws IOException, JSONException, URISyntaxException, OREException, ORESerialiserException {

        String guid = null;

        if(tagId.contains("/"))
            guid = tagId.split("/")[tagId.split("/").length-1];
        else
            guid = tagId.split(":")[tagId.split(":").length-1];
        String json = "";

        if(dataPath==null)
            dataPath = "data/";
        if(type.equals("collection")){



            String remId = DEFAULT_URL_PREFIX + URLEncoder.encode(tagId);
            if(mediciInstance!=null)
                remId =  mediciInstance.getUrl()+"#collection?uri="+ URLEncoder.encode(tagId);
            agg = OREFactory.createAggregation(new URI(
                    remId + "_Aggregation"
            ));

            rem = agg.createResourceMap(
                    new URI(
                            remId
                    ));
            mainRem = rem;
        }
        Iterator iterator = Constants.metadataPredicateMap.entrySet().iterator();
        String title = "";
        long size =0;
        while (iterator.hasNext()){



            Map.Entry pair = (Map.Entry)iterator.next();

            json = util.getJsonResponse(mediciInstance, (String) pair.getKey(),tagId);
            List<String> results = util.parseJsonAttribute(json, "object");
            if(((String) pair.getKey()).contains("hasPart")){
                for(String child:results){



                    if(child.contains("Collection")){


                        //create new file and aggregation for this sub-collection
                        FileWriter oreStream = new FileWriter(bagPath + guid + "_oaiore.xml");

                        String uri = DEFAULT_URL_PREFIX + URLEncoder.encode(child);
                        if(mediciInstance!=null)
                            uri =  mediciInstance.getUrl()+"#collection?uri="+ URLEncoder.encode(child);

                        //Based on http://www.openarchives.org/ore/0.1/datamodel#nestedAggregations
                        AggregatedResource subAggResource = agg.createAggregatedResource(
                                new URI(
                                        uri
                                )

                        );
                        subAggResource.addType(new URI(agg.getTypes().get(0).toString()));//type aggregation

                        Triple resourceMapSource = new TripleJena();
                        resourceMapSource.initialise(subAggResource);

                        String childguid = null;
                        if(child.contains("/"))
                            childguid = tagId.split("/")[child.split("/").length-1];
                        else
                            childguid = tagId.split(":")[child.split(":").length-1];

                        resourceMapSource.relate(DC_TERMS_SOURCE,
                                bagPath+ childguid + "_oaiore.xml");                         //make this a file path or just the is and the oai-ore can be tracked from here
                        rem.addTriple(resourceMapSource);

                        agg.addAggregatedResource(subAggResource);

                        String remChildId =  DEFAULT_URL_PREFIX + URLEncoder.encode(child);
                        if(mediciInstance!=null)
                            remChildId = mediciInstance.getUrl()+"#collection?uri="+ URLEncoder.encode(child);

                        Aggregation newAgg = OREFactory.createAggregation(new URI(
                                remChildId+"_Aggregation"
                        ));
                        ResourceMap newRem = newAgg.createResourceMap(new URI(
                                remChildId
                        ));


                        toOAIORE(newAgg, newRem, child, "collection",bagPath, dataPath, mediciInstance, fetch, manifest);
                    }
                    else{
                        String uri = DEFAULT_URL_PREFIX + URLEncoder.encode(child);
                        if(mediciInstance!=null)
                            uri =  mediciInstance.getUrl()+"#dataset?uri="+URLEncoder.encode(child);

                        AggregatedResource dataResource = agg.createAggregatedResource(new URI(uri));
                        agg.addAggregatedResource(dataResource);

                        toOAIORE(agg, dataResource, child, "file",bagPath, dataPath, mediciInstance, fetch, manifest);
                    }
                }
            }
            else{

                for(String result: results){
                    if(((String) pair.getKey()).contains("title"))
                        title = result;
                    if(((String) pair.getKey()).contains("size"))
                        size = Long.parseLong(result);

                    Triple triple = new TripleJena();
                    triple.initialise(rem);

                    Predicate ORE_TERM_PREDICATE =  new Predicate();
                    ORE_TERM_PREDICATE.setNamespace(Vocab.dcterms_Agent.ns().toString());
                    ORE_TERM_PREDICATE.setPrefix(Vocab.dcterms_Agent.schema());
                    URI uri = new URI(Constants.metadataPredicateMap.get((String)pair.getKey()));
                    ORE_TERM_PREDICATE.setName(uri.toString().substring(uri.toString().lastIndexOf("/")));
                    ORE_TERM_PREDICATE.setURI(uri);
                    triple.relate(ORE_TERM_PREDICATE, result);
                    rem.addTriple(triple);

                }


            }

        }
        if(type.equals("collection")){
            dataPath+=title+"/";
            Agent creator = OREFactory.createAgent();
            creator.addName("SEAD_VA BagItService");

            rem.addCreator(creator);

            agg.addCreator(creator);
            agg.addTitle("Transit BagIt - Sub Collection");
            FileWriter oreStream = new FileWriter(bagPath + guid + "_oaiore.xml");
            BufferedWriter ore = new BufferedWriter(oreStream);

            String resourceMapXml = "";
            ORESerialiser serial = ORESerialiserFactory.getInstance(RESOURCE_MAP_SERIALIZATION_FORMAT);
            ResourceMapDocument doc = serial.serialise((ResourceMap)rem);
            resourceMapXml = doc.toString();

            ore.write(resourceMapXml);
            ore.close();
        }
        else {
            Triple triple = new TripleJena();
            triple.initialise(rem);

            Predicate ORE_TERM_PREDICATE =  new Predicate();
            ORE_TERM_PREDICATE.setNamespace(Vocab.dcterms_Agent.ns().toString());
            ORE_TERM_PREDICATE.setPrefix(Vocab.dcterms_Agent.schema());
            URI uri = new URI("http://purl.org/dc/elements/1.1/source");
            ORE_TERM_PREDICATE.setName("source");
            ORE_TERM_PREDICATE.setURI(uri);
            triple.relate(ORE_TERM_PREDICATE, mediciInstance.getUrl()+ "/api/image/download/"+tagId);
            rem.addTriple(triple);
            fetch.write(mediciInstance.getUrl()+ "/api/image/download/"+tagId+" "+size+" "+dataPath+title+"\n");
            manifest.write("0000000000000000000" + "  " +dataPath+title+"\n");
        }

    }

    ResearchObject sip = new ResearchObject();
    public void fromOAIORE(String collectionId, String parentId, String unzippedDir) throws FileNotFoundException {//top collection id
     //probably convert to Map memory/Sip
        //start with the top most SIP
        //Find what it aggregates and do the same for lower SIPs

        try{
            collectionId = collectionId.split("/")[collectionId.split("/").length-1];
            String duId = collectionId;

            InputStream input = new FileInputStream(new File(unzippedDir + collectionId + "_oaiore.xml"));
            OREParser parser = OREParserFactory.getInstance("RDF/XML");
            ResourceMap rem = parser.parse(input);
            //get any metadata file associated
            TripleSelector tripleSelector = new TripleSelector();
            tripleSelector.setSubjectURI(rem.getURI());
            tripleSelector.setPredicate(CITO_IS_DOCUMENTED_BY);
            List<Triple> metadataTriples = rem.listAllTriples(tripleSelector);
            SeadDeliverableUnit du = new SeadDeliverableUnit();

            if(metadataTriples.size()>0){
                String[] fgdcFilePath = metadataTriples.get(0).getObjectLiteral().split("/");

                du = FgdcGenerator.fromFGDC(unzippedDir + fgdcFilePath[fgdcFilePath.length - 1], du);
            }
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
                    //format - "http://www.iana.org/assignments/media-types/">application/octet-stream</id>
                }
                else //if(types.get(0)==agg.getTypes().get(0))
                {//type sub-aggregation/sub collection
                    String encodedId= aggregatedResource.getURI().toString().split("uri=")[1];
                    String newId = URLDecoder.decode(encodedId);
                    fromOAIORE(newId,duId,unzippedDir);
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

