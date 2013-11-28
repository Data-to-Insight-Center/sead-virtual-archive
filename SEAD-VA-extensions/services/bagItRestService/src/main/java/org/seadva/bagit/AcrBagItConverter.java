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

package org.seadva.bagit;

import org.dspace.foresite.*;
import org.sead.acr.common.utilities.json.JSONException;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

/**
 * ACR metadata to BagIt converter
 */
public class AcrBagItConverter {


    MediciServiceImpl mediciService;
    public AcrBagItConverter(){
        mediciService = new MediciServiceImpl();
    }

    //#1 Convert ACR metadata into a bag
    //Returns path to zipped file
    public String convertRdfToBagit(String collectionId,
                                    MediciInstance mediciInstance
                                    ) throws IOException, JSONException {

        mediciService.populateKids(
            collectionId,
            "collection",
            mediciInstance
        );

        Map<String,List<FileNode>> existingFiles = new HashMap<String, List<FileNode>>();

        Map<String,CollectionNode>  tempDuMp = new HashMap<String, CollectionNode>(MediciServiceImpl.relations.getDuAttrMap());
        Iterator iterator = tempDuMp.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry pairs = (Map.Entry)iterator.next();
            List<FileNode> tempFileNodes = new ArrayList<FileNode>();

            List<String> files = ((CollectionNode)pairs.getValue()).getSub().get(CollectionNode.SubType.File);
            if(files!=null)
                for(String file:files){
                    tempFileNodes.add(MediciServiceImpl.relations.getFileAttrMap().get(file));
                }
            existingFiles.put((String)pairs.getKey(),tempFileNodes);

            iterator.remove();
        }



        String guid = null;
        if(collectionId.contains("/"))
            guid = collectionId.split("/")[collectionId.split("/").length-1];
        else
            guid = collectionId.split(":")[collectionId.split(":").length-1];

        String bagPath = Constants.bagDir+guid+"/";
        (new File(bagPath)).mkdirs();
        FileWriter fstream = new FileWriter( bagPath+ "fetch.txt");
        FileWriter manifestStream = new FileWriter(bagPath + "manifest.txt");
        FileWriter fgdcStream = new FileWriter(bagPath + guid + "_fgdc.xml");
        fetch = new BufferedWriter(fstream);
        manifest = new BufferedWriter(manifestStream);


        manifest.write("0000000000000000000" + "  " + "data/" + guid + "_fgdc.xml");

        fgdc = new BufferedWriter(fgdcStream);

        String xml = FgdcGenerator.createFGDC((CollectionNode) MediciServiceImpl.relations.getDuAttrMap().get(collectionId));
        fgdc.write(xml);
        fgdc.close();

        String dataPath = "data/";
        try {
            OreGenerator oreGenerator = new OreGenerator();
            oreGenerator.toOAIORE(null,null,MediciServiceImpl.relations,
                    collectionId,
                    collectionId,
                    existingFiles,
                    bagPath,
                    dataPath,
                    mediciInstance,
                    fetch,
                    manifest);

        } catch (URISyntaxException e) {
            return null;
        } catch (OREException e) {
            return null;
        } catch (ORESerialiserException e) {
            return null;
        }

        fetch.close();
        manifest.close();


        String zippedBag = Constants.bagDir+"/"+guid+".zip";
        ZipUtil.zipDirectory(new File(bagPath), zippedBag);
        return zippedBag;
    }
    private static final String RESOURCE_MAP_SERIALIZATION_FORMAT = "RDF/XML";
    //static int i=0;



    static BufferedWriter fetch;
    static BufferedWriter manifest;
    static BufferedWriter fgdc;


}
