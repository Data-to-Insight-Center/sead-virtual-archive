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

import org.dspace.foresite.*;
import org.dspace.foresite.jena.TripleJena;
import org.sead.acr.common.utilities.json.JSONException;
import org.seadva.bagit.model.AggregationType;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Handler;
import org.seadva.bagit.util.Constants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Handler for ORE generation
 */
public class OreGenerationHandler implements Handler{

    String sourceTerm = "http://purl.org/dc/elements/1.1/source";
    private static final String RESOURCE_MAP_SERIALIZATION_FORMAT = "RDF/XML";

    Map<String,List<String>> aggregation;
    Map<String,Map<String,List<String>>> properties;
    Map<String,AggregationType> typeProperty;
    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {

        aggregation = packageDescriptor.getAggregation();
        properties = packageDescriptor.getProperties();
        typeProperty = packageDescriptor.getType();

        try {
            toOAIORE(null, null, packageDescriptor.getPackageId(), packageDescriptor.getUnzippedBagPath(), packageDescriptor.getUnzippedBagPath()+"/data");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OREException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ORESerialiserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        //  packageDescriptor.set
        return packageDescriptor;
    }

    private static String DEFAULT_URL_PREFIX = "http://seadva.d2i.indiana.edu/";
    public OREResource getMainRem() {
        return mainRem;
    }

    public void setMainRem(OREResource mainRem) {
        this.mainRem = mainRem;
    }

    OREResource mainRem = null;
    public void toOAIORE(Aggregation agg,
                         OREResource rem,
                         String id,
                         String bagPath,
                         String dataPath)
            throws IOException, JSONException, URISyntaxException, OREException, ORESerialiserException {

        String guid = null;

        if(id.contains("/"))
            guid = id.split("/")[id.split("/").length-1];
        else
            guid = id.split(":")[id.split(":").length-1];
        AggregationType type = typeProperty.get(id);


        if(type == AggregationType.COLLECTION){

            String remId = DEFAULT_URL_PREFIX + URLEncoder.encode(id);
            agg = OREFactory.createAggregation(new URI(
                    remId + "_Aggregation"
            ));

            rem = agg.createResourceMap(
                    new URI(
                            remId
                    ));
            mainRem = rem;// why is this being done?
        }


        String title = "";


        List<String> results = aggregation.get(id);

        if(results!=null)
            for(String child:results){

                if(typeProperty.get(child) == AggregationType.COLLECTION){


                     String uri = DEFAULT_URL_PREFIX + URLEncoder.encode(child);

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
                        childguid = child.split("/")[child.split("/").length-1];
                    else
                        childguid = child.split(":")[child.split(":").length-1];

                    Predicate DC_TERMS_SOURCE =  new Predicate();
                    DC_TERMS_SOURCE.setNamespace(Vocab.dcterms_Agent.ns().toString());
                    DC_TERMS_SOURCE.setPrefix(Vocab.dcterms_Agent.schema());
                    URI sourceUri = new URI(sourceTerm);
                    DC_TERMS_SOURCE.setName("source");
                    DC_TERMS_SOURCE.setURI(sourceUri);
                    resourceMapSource.relate(DC_TERMS_SOURCE,
                            bagPath + "/" + childguid + "_oaiore.xml");                         //make this a file path or just the is and the oai-ore can be tracked from here
                    rem.addTriple(resourceMapSource);

                    agg.addAggregatedResource(subAggResource);

                    String remChildId =  DEFAULT_URL_PREFIX + URLEncoder.encode(child);

                    Aggregation newAgg = OREFactory.createAggregation(new URI(
                            remChildId+"_Aggregation"
                    ));
                    ResourceMap newRem = newAgg.createResourceMap(new URI(
                            remChildId
                    ));


                    toOAIORE(newAgg, newRem, child, bagPath, dataPath);
                }
                else{
                    String uri = DEFAULT_URL_PREFIX + URLEncoder.encode(child);

                    AggregatedResource dataResource = agg.createAggregatedResource(new URI(uri));
                    agg.addAggregatedResource(dataResource);

                    toOAIORE(agg, dataResource, child, bagPath, dataPath);
                }
            }

            Iterator props = properties.get(id).entrySet().iterator();
            while(props.hasNext()) {
                Map.Entry<String,List<String>> pair = (Map.Entry) props.next();

                for(String value: pair.getValue()){
                    Triple triple = new TripleJena();
                    triple.initialise(rem);

                    Predicate ORE_TERM_PREDICATE =  new Predicate();
                    ORE_TERM_PREDICATE.setNamespace(Vocab.dcterms_Agent.ns().toString());
                    ORE_TERM_PREDICATE.setPrefix(Vocab.dcterms_Agent.schema());
                    URI uri = new URI((String)pair.getKey());
                    ORE_TERM_PREDICATE.setName(uri.toString().substring(uri.toString().lastIndexOf("/")));
                    ORE_TERM_PREDICATE.setURI(uri);
                    triple.relate(ORE_TERM_PREDICATE, value);
                    rem.addTriple(triple);
                }
            }

        if(typeProperty.get(id)==AggregationType.COLLECTION){
            dataPath+=title+"/";
            Agent creator = OREFactory.createAgent();
            creator.addName("SEAD_VA BagItService");

            rem.addCreator(creator);

            agg.addCreator(creator);
            agg.addTitle("Transit BagIt - Sub Collection");
            FileWriter oreStream = new FileWriter(bagPath +"/" + guid + "_oaiore.xml");
            BufferedWriter ore = new BufferedWriter(oreStream);

            String resourceMapXml = "";
            ORESerialiser serial = ORESerialiserFactory.getInstance(RESOURCE_MAP_SERIALIZATION_FORMAT);
            ResourceMapDocument doc = serial.serialise((ResourceMap)rem);
            resourceMapXml = doc.toString();

            ore.write(resourceMapXml);
            ore.close();
        }


    }
}
