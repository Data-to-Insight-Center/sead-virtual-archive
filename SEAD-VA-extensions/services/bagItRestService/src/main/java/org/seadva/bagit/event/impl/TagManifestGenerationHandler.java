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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class TagManifestGenerationHandler implements Handler {
    Map<String,List<String>> aggregation;
    Map<String,Map<String,List<String>>> properties;
    Map<String,AggregationType> typeProperty;
    Writer manifest;

    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {

        if(packageDescriptor.getPackageId()==null)
            return packageDescriptor;
        aggregation = packageDescriptor.getAggregation();
        properties = packageDescriptor.getProperties();
        typeProperty = packageDescriptor.getType();

        FileWriter manifestStream = null;
        try {
            String manifestFilePath = packageDescriptor.getUntarredBagPath() + "/tagmanifest-sha256.txt";
            manifestStream = new FileWriter(manifestFilePath);
            manifest = new BufferedWriter(manifestStream);
            generateManifestFile(packageDescriptor.getPackageId(), "/");
            manifest.close();
            packageDescriptor.setManifestFilePath(manifestFilePath);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return packageDescriptor;
    }
    void generateManifestFile(String id, String path) throws IOException {

        List<String> children = aggregation.get(id);

        if(children!=null){
            for(String child: children){
                if(typeProperty.get(child)==AggregationType.COLLECTION){
                    generateManifestFile(child, path+properties.get(child).get(Constants.titleTerm).get(0)+"/");
                }
                else{
                    manifest.write("0000000000000000000" + "  " + path+properties.get(child).get(Constants.titleTerm).get(0)+"\n");
                }
            }
        }else{
            for(String child: children){
                if (typeProperty.get(child) == AggregationType.COLLECTION) {
                    generateManifestFile(child, path + properties.get(child).get(Constants.titleTerm).get(0) + "/");
                } else {
                    manifest.write("0000000000000000000" + "  " + path + properties.get(child).get(Constants.titleTerm).get(0) + "\n");
                }
            }
        }

    }
}
