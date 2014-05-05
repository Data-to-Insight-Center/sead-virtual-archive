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

import org.dspace.foresite.Predicate;
import org.dspace.foresite.Triple;
import org.dspace.foresite.Vocab;
import org.dspace.foresite.jena.TripleJena;
import org.sead.acr.common.utilities.json.JSONException;
import org.seadva.bagit.util.AcrQueryUtil;
import org.seadva.bagit.model.AggregationType;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Handler;
import org.seadva.bagit.model.MediciInstance;
import org.seadva.bagit.util.Constants;

import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * Handler for Querying ACR sparql endpoint
 */
public class AcrQueryHandler implements Handler{
    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {

        String tagId = packageDescriptor.getPackageId();
        if(tagId==null)
            return packageDescriptor;


        aggregation = packageDescriptor.getAggregation();
        properties = packageDescriptor.getProperties();
        typeProperty = packageDescriptor.getType();
        typeProperty.put(tagId,AggregationType.COLLECTION);

        try {
            populateAggregation(tagId, packageDescriptor.getMediciInstance());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        List<String> titles = properties.get(tagId).get(Constants.titleTerm);

        if(titles!=null&&titles.size()>0){
            packageDescriptor.setPackageName(titles.get(0));
            //packageDescriptor.setUnzippedBagPath(packageDescriptor.getUnzippedBagPath() + titles.get(0));
        }

        packageDescriptor.setAggregation(aggregation);
        packageDescriptor.setProperties(properties);
        packageDescriptor.setType(typeProperty);
        return packageDescriptor;
    }

    Map<String,List<String>> aggregation;
    Map<String,Map<String,List<String>>> properties;
    Map<String,AggregationType> typeProperty;

    AcrQueryUtil util = new AcrQueryUtil();

    void populateAggregation(String tagId,
                             MediciInstance mediciInstance
                             ) throws IOException, JSONException {

        String json = "";
        Iterator iterator = Constants.metadataPredicateMap.entrySet().iterator();
        while (iterator.hasNext()){

            Map.Entry pair = (Map.Entry)iterator.next();

            json = util.getJsonResponse(mediciInstance, (String) pair.getKey(),tagId);
            List<String> results = util.parseJsonAttribute(json, "object");
            if(((String) pair.getKey()).contains("hasPart")){
                for(String child:results){

                    List<String> children = aggregation.get(tagId);
                    if(children==null)
                        children = new ArrayList<String>();
                    children.add(child);

                    aggregation.put(tagId,children);

                    if(!typeProperty.containsKey(child)){
                        if(child.contains("Collection"))
                            typeProperty.put(child, AggregationType.COLLECTION);
                        else{
                            typeProperty.put(child,AggregationType.FILE);
                            Map<String,List<String>> existingProperties;
                            if(properties.containsKey(child))
                                existingProperties = properties.get(child);
                            else
                                existingProperties = new HashMap<String, List<String>>();
                            List<String> existingValues = existingProperties.get(Constants.sourceTerm);
                            if(existingValues==null)
                                existingValues = new ArrayList<String>();
                            existingValues.add(mediciInstance.getUrl()+ "/api/image/download/"+child);
                            existingProperties.put(Constants.sourceTerm,existingValues);
                            properties.put(child,existingProperties);
                        }
                    }
                    populateAggregation(child, mediciInstance);
                }
            }
            else{

                Map<String,List<String>> existingProperties;
                if(properties.containsKey(tagId))
                    existingProperties = properties.get(tagId);
                else
                    existingProperties = new HashMap<String, List<String>>();
                for(String result: results){
                    List<String> existingValues = existingProperties.get((String) pair.getKey());
                    if(existingValues==null)
                        existingValues = new ArrayList<String>();
                    existingValues.add(result);
                    existingProperties.put((String)pair.getValue(),existingValues);
                }
                properties.put(tagId,existingProperties);
            }

        }

    }
}
