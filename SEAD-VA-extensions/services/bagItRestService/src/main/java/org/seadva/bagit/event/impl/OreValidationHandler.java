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

import org.apache.commons.io.IOUtils;
import org.dspace.foresite.*;
import org.sead.acr.common.utilities.json.JSONException;
import org.seadva.bagit.Exception.SEADInvalidOREException;
import org.seadva.bagit.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validate the ORE generated
 */
public class OreValidationHandler {

    /**
     * Loads the minimal metadata for Collections
     * @return
     * @throws java.io.IOException
     */
    private  Map<String, Integer> loadAcrMinimalMetadataForCollection() throws IOException{
        Map<String, Integer> minimalMetadataMap = new HashMap<String, Integer>();

        InputStream inputStream =
                Constants.class.getResourceAsStream("MinimalMetadataConfig.properties");

        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);

        String result = writer.toString();
        String[] metadata = result.trim().split("\n");

        //add 0 as the value for each predicate string in the hashmap
        for (int i = 0; i < metadata.length;i++) {
           minimalMetadataMap.put(metadata[i].trim(),0);
        }

        return minimalMetadataMap;
    }

    /**
     * Loads the minimal metadata for File
     * @return
     * @throws java.io.IOException
     */
    private  Map<String, Integer> loadAcrMinimalMetadataForFile() throws IOException{
        Map<String, Integer> minimalMetadataMap = new HashMap<String, Integer>();

        InputStream inputStream =
                Constants.class.getResourceAsStream("MinimalMetadataForFileConfig.properties");

        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);

        String result = writer.toString();

        String[] metadata = result.trim().split("\n");

        //add 0 as the value for each predicate string in the hashmap
        for (int i = 0; i < metadata.length;i++) {
            minimalMetadataMap.put(metadata[i].trim(),0);
        }

        return minimalMetadataMap;
    }

    /**
     * Validates the ORE resource passed as the argument
     * @param rem
     * @throws java.io.IOException
     * @throws org.sead.acr.common.utilities.json.JSONException
     * @throws java.net.URISyntaxException
     * @throws org.dspace.foresite.OREException
     * @throws org.dspace.foresite.ORESerialiserException
     */
    public boolean validateForMinimalOAIORE(ResourceMap rem)
            throws IOException, JSONException, URISyntaxException, OREException, ORESerialiserException, SEADInvalidOREException {

        Map<String, Integer> minimalMetadataMapForCollection = loadAcrMinimalMetadataForCollection();

        Map<String, Integer> minimalMetadataMapForFile = loadAcrMinimalMetadataForFile();

        Aggregation aggregation = rem.getAggregation();//get the aggregation from the resource map
        List<Triple> tripleList = aggregation.listTriples();//list all the triples in the aggregation

        //for each of the triples
        for(int i=0;i<tripleList.size();i++)
        {
            Object object = tripleList.get(i);
            Triple triple = (Triple)object;

            //get the predicate of the triple
            Predicate predicate = triple.getPredicate();

            //if the predicate is null, do nothing. This case arises for the predicate creators
            if(predicate==null)
            {
                continue;
            }
            else
            {
                URI predicateURI = predicate.getURI();
                //get the string for the predicate URI
                String predicateURIString = predicateURI.toString();

                //if the predicate URI string is present in the hashmap retrieve the corresponding value
                Integer isPresent = minimalMetadataMapForCollection.get(predicateURIString);

                if (isPresent != null) {
                    String objectLiteral = "";
                    try {
                        //get the object literal of the predicate
                        objectLiteral = triple.getObjectLiteral().trim();
                    } catch (OREException e) {

                    }

                    //get the predicate count from the hashmap
                    int predicateCount = isPresent.intValue();

                    //check if the object literal is null or empty for all the predicates other than the creator and increase
                    //the predicate count by 1 in the hashmap if the literal is a non empty string
                    if (!objectLiteral.equals(null) && !objectLiteral.equals("") && !predicateURIString.equals("http://purl.org/dc/terms/creator")) {
                        minimalMetadataMapForCollection.put(predicateURIString, ++predicateCount);
                    }


                    //if the predicate is a creator
                    else if(predicateURIString.equals("http://purl.org/dc/terms/creator"))
                    {
                        List<Agent> creators = rem.getAggregation().getCreators();
                        //get a list of all the creators
                        for (Agent a : creators) {
                            //check if the creators has non empty strings for name and an email id
                            if (a.getNames() != null && a.getMboxes() != null && a.getNames().size() > 0 && a.getMboxes().size() > 0)
                                if (!a.getNames().get(0).equals(null) && !a.getNames().get(0).equals("") &&!a.getMboxes().get(0).equals(null) && !a.getMboxes().get(0).equals("")) {
                                    minimalMetadataMapForCollection.put(predicateURIString, ++predicateCount);
                                }
                        }
                    }
                }
            }
        }

        //list all the aggregated resources in the aggregation
        List<AggregatedResource> aggregatedResources = rem.getAggregatedResources();
        int aggregatedResourcesCount = aggregatedResources.size();

        for(int j=0;j<aggregatedResourcesCount;j++)
        {
            AggregatedResource aggregatedResource = aggregatedResources.get(j);

            //get all the triples for the aggregated source
            tripleList = aggregatedResource.listTriples();

            for(int i=0;i<tripleList.size();i++) {
                Object object = tripleList.get(i);
                Triple triple = (Triple) object;

                Predicate predicate = triple.getPredicate();

                //get the string for the predicate URI
                URI predicateURI = predicate.getURI();
                String predicateURIString = predicateURI.toString();

                //if the predicate URI string is present in the hashmap retrieve the corresponding value
                Integer isPresent = minimalMetadataMapForFile.get(predicateURIString);

                if(isPresent!=null){
                    String objectLiteral="";
                    try {
                        //get the object literal of the predicate
                        objectLiteral = triple.getObjectLiteral().trim();
                    }
                    catch(OREException e) {

                    }

                    //get the predicate count from the hashmap
                    int predicateCount = isPresent.intValue();

                    //check if the object literal is null or empty for all the predicates and increase
                    //the predicate count by 1 in the hashmap if the literal is a non empty string
                    if(!objectLiteral.equals(null) && !objectLiteral.equals("")) {
                        minimalMetadataMapForFile.put(predicateURIString, ++predicateCount);
                    }
                }
            }
        }

        //for all the predicates in the set of minimal predicates for collection,
        // check if the predicate count is greater than 0, if not throw an exception
        for(String key:minimalMetadataMapForCollection.keySet())
        {
            try
            {
                if(minimalMetadataMapForCollection.get(key)==0)
                {
                    throw new SEADInvalidOREException("Minimal Metadata is not present for a collection "+key+" is not found for the aggregation "+aggregation);
                }
            }
            catch(SEADInvalidOREException e)
            {
                System.out.println(e.getMessage());
                return false;
            }
        }

        //for all the predicates in the set of minimal predicates for files,
        // check if all the files have the minimal metadata, if not throw an exception
        for(String key:minimalMetadataMapForFile.keySet())
        {
            try
            {
                if(minimalMetadataMapForFile.get(key)<aggregatedResourcesCount) {
                    throw new SEADInvalidOREException("Minimal Metadata is not present for a file "+key+" in the aggregation "+aggregation);
                }
            }
            catch(SEADInvalidOREException e)
            {
                System.out.println(e.getMessage());
                return false;
            }
        }

        return true;
    }
}
