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

package org.dataconservancy.dcs.access.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataconservancy.dcs.access.server.util.ServerConstants;
import org.dataconservancy.dcs.access.shared.MediciInstance;
import org.dataconservancy.model.dcs.*;
import org.dspace.foresite.*;
import org.dspace.foresite.jena.TripleJena;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadFile;
import org.seadva.model.pack.ResearchObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: kavchand
 * Date: 6/22/13
 * Time: 5:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class OreGenerator {
    private static final String RESOURCE_MAP_SERIALIZATION_FORMAT = "RDF/XML";

    private static Predicate DC_TERMS_IDENTIFIER = null;
    private static Predicate DC_TERMS_SOURCE = null;
    private static Predicate DC_TERMS_TITLE = null;
    private static Predicate DC_TERMS_FORMAT = null;
    private static Predicate DC_TERMS_ABSTRACT = null;

    private static Predicate CITO_IS_DOCUMENTED_BY = null;

    private static Predicate CITO_DOCUMENTS = null;

    //Aggregation agg = null;

    private static Log log = LogFactory.getLog(OreGenerator.class);

    public OreGenerator() throws URISyntaxException, OREException {

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

        // create the CITO:documents predicate
        CITO_DOCUMENTS = new Predicate();
        CITO_DOCUMENTS.setNamespace(CITO_IS_DOCUMENTED_BY.getNamespace());
        CITO_DOCUMENTS.setPrefix(CITO_IS_DOCUMENTED_BY.getPrefix());
        CITO_DOCUMENTS.setName("documents");
        CITO_DOCUMENTS.setURI(new URI(CITO_DOCUMENTS.getNamespace()
                + CITO_DOCUMENTS.getName()));
    }


    public ResourceMap getMainRem() {
        return mainRem;
    }

    public void setMainRem(ResourceMap mainRem) {
        this.mainRem = mainRem;
    }

    ResourceMap mainRem = null;

  
    ResearchObject sip = new ResearchObject();
    public void fromOAIORE(String id, String parentId, String unzippedDir, MediciInstance sparqlInstance) throws FileNotFoundException {//top collection id
     //probably convert to Map memory/Sip
        //start with the top most SIP
        //Find what it aggregates and do the same for lower SIPs

        try{
            String guid = null;
            if(id.contains("/"))
                guid = id.split("/")[id.split("/").length-1];
            else
                guid = id.split(":")[id.split(":").length-1];


            InputStream input = new FileInputStream(new File(unzippedDir + guid + "_oaiore.xml"));
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
                
                du = FgdcGenerator.fromFGDC(unzippedDir+fgdcFilePath[fgdcFilePath.length-1],du);
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
            //get root DU
            Aggregation agg = rem.getAggregation();
            String duId = id.replace("_Aggregation", "");
            du.setId(duId);
            if(parentId!=null){
                DcsDeliverableUnitRef ref = new DcsDeliverableUnitRef();
                ref.setRef(parentId);
                du.addParent(ref);
            }

            DcsResourceIdentifier duAltId = new DcsResourceIdentifier();
            duAltId.setIdValue(duId);
            duAltId.setTypeId(sparqlInstance.getType());//Todo change this to ACR sparql endpoint
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

                    DcsResourceIdentifier altId = new DcsResourceIdentifier();
                    altId.setIdValue(triples.get(0).getObjectLiteral());
                    altId.setTypeId(sparqlInstance.getType());
                    file.addAlternateId(altId);

                    selector = new TripleSelector();
                    selector.setSubjectURI(aggregatedResource.getURI());
                    selector.setPredicate(DC_TERMS_TITLE);
                    triples = aggregatedResource.listAllTriples(selector);
                    file.setName(triples.get(0).getObjectLiteral());


                    selector = new TripleSelector();
                    selector.setSubjectURI(aggregatedResource.getURI());
                    selector.setPredicate(DC_TERMS_FORMAT);
                    triples = aggregatedResource.listAllTriples(selector);
                    DcsFormat format = new DcsFormat();
                    format.setSchemeUri(ServerConstants.FORMAT_IANA_SCHEME);
                    format.setFormat(triples.get(0).getObjectLiteral());
                    file.addFormat(format);

                    file.setSource(sparqlInstance.getUrl()+"/api/image/download/"+file.getId());

                    file.setExtant(true);

                    DcsManifestationFile manifestationFile = new DcsManifestationFile();
                    DcsFileRef ref = new DcsFileRef();
                    ref.setRef(file.getId());
                    manifestationFile.setRef(ref);
                    manifestation.addManifestationFile(manifestationFile);
                    sip.addFile(file);
                    //format - "http://www.iana.org/assignments/media-types/"
                }
                else //if(types.get(0)==agg.getTypes().get(0))
                {//type sub-aggregation/sub collection
                    String encodedId= aggregatedResource.getURI().toString().split("uri=")[1];
                    String newId = URLDecoder.decode(encodedId);
                    fromOAIORE(newId,duId,unzippedDir,sparqlInstance);
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

