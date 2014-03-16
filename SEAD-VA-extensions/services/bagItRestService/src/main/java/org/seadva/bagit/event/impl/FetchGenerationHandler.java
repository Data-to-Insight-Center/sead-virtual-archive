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

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Handler to generate fetch file
 */
public class FetchGenerationHandler implements Handler{
    Map<String,List<String>> aggregation;
    Map<String,Map<String,List<String>>> properties;
    Map<String,AggregationType> typeProperty;
    Writer fetch;

    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {

        if(packageDescriptor.getPackageId()==null)
            return packageDescriptor;
        aggregation = packageDescriptor.getAggregation();
        properties = packageDescriptor.getProperties();
        typeProperty = packageDescriptor.getType();

        FileWriter fetchStream = null;
        try {
            if(!new File(packageDescriptor.getUnzippedBagPath()).exists())
                new File(packageDescriptor.getUnzippedBagPath()).mkdirs();
            String fetchFilePath = packageDescriptor.getUnzippedBagPath() + "/fetch.txt";
            fetchStream = new FileWriter(fetchFilePath);
            fetch = new BufferedWriter(fetchStream);
            generateFetchFile(packageDescriptor.getPackageId(), "data/");
            fetch.close();
            packageDescriptor.setFetchFilePath(fetchFilePath);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return packageDescriptor;
    }
    void generateFetchFile(String id, String path) throws IOException {

        List<String> children = aggregation.get(id);

        if(children!=null)
            for(String child: children){
                if(typeProperty.get(child)==AggregationType.COLLECTION){
                    generateFetchFile(child, path+properties.get(child).get(Constants.titleTerm).get(0)+"/");
                }
                else{
                    String source = "unknown";
                    long size = 0;
                    List<String> sources = properties.get(child).get(Constants.sourceTerm);

                    if(sources!=null&& sources.size()>0)
                        source = sources.get(0);

                    List<String> sizes = properties.get(child).get(Constants.sizeTerm);
                    if(sizes!=null&&sizes.size()>0)
                        size = Long.parseLong(sizes.get(0));

                    fetch.write(source+" "+size+" "+path+properties.get(child).get(Constants.titleTerm).get(0)+"\n");
                }
            }
    }
}
