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
import org.sead.acr.common.utilities.json.JSONException;
import org.seadva.bagit.model.AggregationType;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Handler;
import org.seadva.bagit.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Handler to parse fetch file for aggregations
 */
public class FetchParseHandler implements Handler{


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

        ids.put("/",guid);

        try {
            convertFecthToORE(new File(packageDescriptor.getUnzippedBagPath() + "/fetch.txt"), guid);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        packageDescriptor.setAggregation(aggregation);
        packageDescriptor.setProperties(properties);
        packageDescriptor.setType(typeProperty);
        return packageDescriptor;
    }

    Map<String,List<String>> aggregation;
    Map<String,Map<String,List<String>>> properties;
    Map<String,AggregationType> typeProperty;


    Map<String,String> ids = new HashMap<String, String>();

    public void convertFecthToORE(File fetchFile, String id) throws IOException, JSONException {

        StringWriter writer = new StringWriter();
        IOUtils.copy(new FileInputStream(fetchFile), writer);
        String fetchContent = writer.toString();


        String[] fetchArray = fetchContent.split("[ \n]");


        for(int i=0;i<fetchArray.length-2;i+=3){
            String[] collection = fetchArray[i+2].split("/");
            String path = "/";

            for(int j=1;j<collection.length;j++){

                path += collection[j];

                String guid;
                if(ids.containsKey(path))
                    guid = ids.get(path);
                else{
                    guid = UUID.randomUUID().toString();
                    ids.put(path,guid);
                    if(j==1){
                        List<String> firstChildren = aggregation.get(id);
                        if(firstChildren==null)
                            firstChildren = new ArrayList<String>();
                        if(!firstChildren.contains(guid))
                            firstChildren.add(guid);
                        aggregation.put(id,firstChildren);
                    }
                }




                if(j<collection.length-1){
                    String chidGuid;
                    if(ids.containsKey(path+"/"+collection[j+1]))
                        chidGuid = ids.get(path+"/"+collection[j+1]);
                    else{
                        chidGuid = UUID.randomUUID().toString();
                        ids.put(path+"/"+collection[j+1],chidGuid);
                    }
                    List<String> currentChildren = aggregation.get(guid);

                    if(currentChildren==null)
                        currentChildren = new ArrayList<String>();
                    if(!currentChildren.contains(chidGuid))
                        currentChildren.add(chidGuid);
                    aggregation.put(guid,currentChildren);
                    if(!typeProperty.containsKey(ids.get(path)))
                        typeProperty.put(ids.get(path), AggregationType.COLLECTION);
                }
                else{
                    typeProperty.put(ids.get(path), AggregationType.FILE);
                }


                Map<String,List<String>> existingProperties;

                if(!properties.containsKey(guid))
                   existingProperties  = new HashMap<String, List<String>>();
                else
                    existingProperties = properties.get(guid);

                List<String> existingValues = new ArrayList<String>();
                existingValues.add(collection[j]);

                existingProperties.put(Constants.titleTerm,existingValues);
                List<String> existingIdValues = new ArrayList<String>();
                existingIdValues.add(guid);
                existingProperties.put(Constants.identifierTerm,existingIdValues);
                properties.put(guid,existingProperties);

                path+="/";
            }


        }
    }


}
