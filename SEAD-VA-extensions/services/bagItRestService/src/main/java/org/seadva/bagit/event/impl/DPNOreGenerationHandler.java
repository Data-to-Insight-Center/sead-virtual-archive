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
import java.io.File;
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
public class DPNOreGenerationHandler implements Handler{

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

        String guid = null;

        if(packageDescriptor.getPackageId().contains("/"))
            guid = packageDescriptor.getPackageId().split("/")[packageDescriptor.getPackageId().split("/").length-1];
        else
            guid = packageDescriptor.getPackageId().split(":")[packageDescriptor.getPackageId().split(":").length-1];
        packageDescriptor.setOreFilePath(packageDescriptor.getUnzippedBagPath() +"/" + guid + "_oaiore.xml");
        return packageDescriptor;
    }

    private static String DEFAULT_URL_PREFIX = "http://seadva.d2i.indiana.edu/";

    /**
     * Creates an ORE for a given id  that is passed as argument
     * @param agg
     * @param rem
     * @param id
     * @param bagPath
     * @param dataPath
     * @throws java.io.IOException
     * @throws org.sead.acr.common.utilities.json.JSONException
     * @throws java.net.URISyntaxException
     * @throws org.dspace.foresite.OREException
     * @throws org.dspace.foresite.ORESerialiserException
     */
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

            String remId = id;

            if(!remId.startsWith("http:"))
                remId = DEFAULT_URL_PREFIX + URLEncoder.encode(id);
            agg = OREFactory.createAggregation(new URI(
                    remId + "_Aggregation"
            ));

            rem = agg.createResourceMap(
                    new URI(
                            remId
                    ));
        }


        String title = "";


        List<String> results = aggregation.get(id);

        if(results!=null)           //This part checks for child entities (files or sub-collections) so their properties can also be populated in the ORE.
            for(String child:results){

                /*
                If current entity has child ids, then it checks whether they are collection or file  and recursively calls the method to populate child properties in the ORE
                 */
                if(typeProperty.get(child) == AggregationType.COLLECTION){
                    //If collection, a  new ORE file is generated for the aggregation and the toOAIORE is called recursively to
                    // populate the properties of the collection


                    String uri = child;
                    /*try {
                        new URI(child);
                    } catch (URISyntaxException x) {*/
                    if(!child.startsWith("http:"))
                        uri = DEFAULT_URL_PREFIX + URLEncoder.encode(child);
                    //}

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
                    URI sourceUri = new URI(Constants.sourceTerm);
                    DC_TERMS_SOURCE.setName("FLocat");
                    DC_TERMS_SOURCE.setURI(sourceUri);
                    resourceMapSource.relate(DC_TERMS_SOURCE,
                            bagPath + "/" + childguid + "_oaiore.xml");
                    rem.addTriple(resourceMapSource);

                    agg.addAggregatedResource(subAggResource);

                    String remChildId = child;
                    /*try {
                        new URI(child);
                    } catch (URISyntaxException x) {*/
                    if(!child.startsWith("http:"))
                        remChildId = DEFAULT_URL_PREFIX + URLEncoder.encode(child);
                    //}

                    Aggregation newAgg = OREFactory.createAggregation(new URI(
                            remChildId+"_Aggregation"
                    ));
                    ResourceMap newRem = newAgg.createResourceMap(new URI(
                            remChildId
                    ));


                    toOAIORE(newAgg, newRem, child, bagPath, dataPath);  //the new aggregation and  resourceMap is passed recursively, so that the sub-collection(sub-aggregation) properties are populated
                }
                else{
                    /*
                    If child is a file, the toOAIORE method is recursively called to populate the properties of the file.
                     */
                    String uri = child;
                    if(!uri.startsWith("http:"))
                        uri = DEFAULT_URL_PREFIX + URLEncoder.encode(child);

                    AggregatedResource dataResource = agg.createAggregatedResource(new URI(uri));
                    agg.addAggregatedResource(dataResource);   // The dataResource for the file is added to the current aggregation i.e connection is made between the aggregation and file resource

                    toOAIORE(agg, dataResource, child, bagPath, dataPath); //the file resource is passed recursively, so that the file properties are populated in the recursive call
                }
            }

        //This section gets properties of the current entity (collection/file) and puts them in the ORE map. The properties data structure should be populated beforehand by querying ACR for metadata by ACRQueryHandler
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

        //Finally, a check is made to see if the entity is a Collection (if file, do nothing). If it is a collection, then the ORE strcuture in memory is serialized as a file.
        if(typeProperty.get(id)==AggregationType.COLLECTION){
            dataPath+=title+"/";
            Agent creator = OREFactory.createAgent();
            creator.addName("SEAD_VA BagItService");

            rem.addCreator(creator);

            agg.addCreator(creator);
            agg.addTitle("Transit BagIt - Sub Collection");
            String oreDir = bagPath+"/IU-tags";
            if(!new File(oreDir).exists()) {
                new File(oreDir).mkdirs();
            }
            FileWriter oreStream = new FileWriter(oreDir +"/IU-" + guid + "_oaiore.xml");
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
