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

import org.seadva.bagit.model.AggregationType;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Handler;
import org.seadva.bagit.util.Constants;

import java.io.File;
import java.util.*;

/**
 * Handler to parse directory aggregation in data directory
 */
public class DirectoryParseHandler implements Handler{


    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {

        aggregation = packageDescriptor.getAggregation();
        properties = packageDescriptor.getProperties();
        typeProperty = packageDescriptor.getType();

        String guid = UUID.randomUUID().toString();
        packageDescriptor.setPackageId(guid);
        List<String> existingValues = new ArrayList<String>();
        existingValues.add(packageDescriptor.getPackageName());
        Map<String,List<String>> existingProperties = new HashMap<String, List<String>>();
        existingProperties.put(Constants.titleTerm,existingValues);

        List<String> existingIdValues = new ArrayList<String>();
        existingIdValues.add(guid);
        existingProperties.put(Constants.identifierTerm,existingIdValues);

        properties.put(guid,existingProperties);

        typeProperty.put(guid,AggregationType.COLLECTION);

        getDirectoryStructure(new File(packageDescriptor.getUnzippedBagPath() + "/data"), guid);
        //getDirectoryStructure(new File(packageDescriptor.getUnzippedBagPath()), guid);
        packageDescriptor.setAggregation(aggregation);
        packageDescriptor.setProperties(properties);
        packageDescriptor.setType(typeProperty);
        return packageDescriptor;
    }

    Map<String,List<String>> aggregation;
    Map<String,Map<String,List<String>>> properties;
    Map<String,AggregationType> typeProperty;





    void getDirectoryStructure(File directory, String id){
        File[] list = directory.listFiles();
        for(File file: list){
            List<String> children = aggregation.get(id);
            if(children==null)
                children = new ArrayList<String>();
            String childId = UUID.randomUUID().toString();
            children.add(childId);

            aggregation.put(id,children);
            List<String> existingValues = new ArrayList<String>();
            existingValues.add(file.getName());
            Map<String,List<String>> existingProperties = new HashMap<String, List<String>>();
            existingProperties.put(Constants.titleTerm,existingValues);

            List<String> existingIdValues = new ArrayList<String>();
            existingIdValues.add(childId);
            existingProperties.put(Constants.identifierTerm,existingIdValues);
            properties.put(childId,existingProperties);

            List<String> existingSourceValues = new ArrayList<String>();
            existingSourceValues.add("data/"+file.getName());
            //existingSourceValues.add(file.getName());
            existingProperties.put(Constants.sourceTerm,existingSourceValues);
            properties.put(childId,existingProperties);

            if(!typeProperty.containsKey(childId)){
                if(file.isDirectory()){
                    typeProperty.put(childId, AggregationType.COLLECTION);
                }
                else{
                    typeProperty.put(childId,AggregationType.FILE);
                }
            }
        }

    }

}
