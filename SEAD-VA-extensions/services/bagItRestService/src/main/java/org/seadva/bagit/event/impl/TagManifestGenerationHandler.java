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
import java.security.MessageDigest;
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
        String bagPath = packageDescriptor.getUntarredBagPath();
        String manifestFilePath = packageDescriptor.getUntarredBagPath() + "/tagmanifest-sha256.txt";
        try {
            manifestStream = new FileWriter(manifestFilePath);
            manifest = new BufferedWriter(manifestStream);
            File dir = new File(bagPath);
            File[] dirListing = dir.listFiles();
            if(dirListing != null){
                for(File child : dirListing){
                    if (!child.isDirectory()) {
                        String file = child.toString();
                        int pos;
                        pos = file.lastIndexOf('/');
                        String fileName = file.substring(pos+1);
                        System.out.println("TagFileName is :"+fileName);

                        if (!fileName.equals("tagmanifest-sha256.txt")) {
                            try{
                                String fixity = generateCheckSum(file,"SHA1");
                                manifest.write(fixity + "  " + fileName+"\n");
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        System.out.println("File name is: " + child.toString());
                    }
                }
            }
            //generateManifestFile(packageDescriptor.getPackageId(), "/");
            manifest.close();
            packageDescriptor.setManifestFilePath(manifestFilePath);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return packageDescriptor;
    }

    private String generateCheckSum(String filePath, String algorithm) throws Exception{
        System.out.println("ManifestGenerateCheckSum:" + filePath);
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        FileInputStream fileInputStream = new FileInputStream( filePath);
        byte[] dataBytes = new byte[1024];
        int bytesRead = 0;
        while((bytesRead = fileInputStream.read(dataBytes)) != -1){
            messageDigest.update(dataBytes,0,bytesRead);
        }
        byte[] digestBytes = messageDigest.digest();
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < digestBytes.length; i++) {
            sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        System.out.println(algorithm+" Checksum for the file: " + sb.toString());
        fileInputStream.close();
        return sb.toString();

    }
}
