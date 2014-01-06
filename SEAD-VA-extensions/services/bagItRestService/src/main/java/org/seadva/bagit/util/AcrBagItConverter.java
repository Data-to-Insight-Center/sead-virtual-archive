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

package org.seadva.bagit.util;

import com.sun.syndication.feed.rss.Guid;
import org.apache.commons.io.IOUtils;
import org.dspace.foresite.*;
import org.sead.acr.common.utilities.json.JSONException;
import org.seadva.bagit.model.*;
import org.seadva.bagit.MediciServiceImpl;
import org.seadva.bagit.OreGenerator;

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
    //#2 Returns path to zipped file
    public String convertRdfToBagit(String collectionId,
                                    MediciInstance mediciInstance
                                    ) throws IOException, JSONException {

        mediciService.populateKids(
            collectionId,
            "collection",
            mediciInstance
        );
        return generateORE(collectionId,mediciInstance);
    }
        private String generateORE(String collectionId,MediciInstance mediciInstance) throws IOException, JSONException {
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
            e.printStackTrace();
            return null;
        } catch (OREException e) {
            e.printStackTrace();
            return null;
        } catch (ORESerialiserException e) {
            e.printStackTrace();
            return null;
        }

        fetch.close();
        manifest.close();


        String zippedBag = Constants.bagDir+"/"+guid+".zip";
        ZipUtil.zipDirectory(new File(bagPath), zippedBag);
        return zippedBag;
    }

    Map<String,List<Node>> parents = new HashMap<String, List<Node>>();
    Map<String,String> ids = new HashMap<String, String>();

    public String convertFecthToORE(File fetchFile) throws IOException, JSONException {

        StringWriter writer = new StringWriter();
        IOUtils.copy(new FileInputStream(fetchFile), writer);
        String fetchContent = writer.toString();


        String[] fetchArray = fetchContent.split("[ \n]");

        for(int i=0;i<fetchArray.length-2;i+=3){
            String source = fetchArray[i];
            String size = fetchArray[i+1];
            String path = fetchArray[i+2];

            String[] collectionPath = path.split("/");

            for(int j=1;j<collectionPath.length;j++){
                //excluding j=0 since it is always "data"
                //Does not handle case of having sample collection name along the path
                String id = "";
                if(!ids.containsKey(collectionPath[j])){
                    id = UUID.randomUUID().toString();
                    ids.put(collectionPath[j], id);
                }
                else
                    continue;

                List<Node> children  = new ArrayList<Node>();
                if(j>1){
                    if(parents.containsKey(collectionPath[j-1]))
                        children = parents.get(collectionPath[j-1]);
                }
                else{
                    if(parents.containsKey("null"))
                        children = parents.get("null");
                }

                if(j==collectionPath.length-1){
                    FileNode fileNode = new FileNode();
                    fileNode.setTitle(collectionPath[j]);
                    fileNode.setId(id);
                    fileNode.setSource(source);
                    fileNode.setFileSize(Long.parseLong(size));
                    children.add(fileNode);
                }
                else{
                    CollectionNode collectionNode = new CollectionNode();
                    collectionNode.setTitle(collectionPath[j]);
                    collectionNode.setId(id);
                    children.add(collectionNode);
                }

                if(j>1)
                    parents.put(collectionPath[j-1],children);
                else
                    parents.put("null",children);

            }
        }

        createORE("null");
        return generateORE(ids.get(parents.get("null").get(0).getTitle()),null);

    }

    private void createORE(String parent){

        List<Node> children = parents.get(parent);


        for(Node child:children){
            MediciServiceImpl.relations.getParentMap().put(ids.get(child.getTitle()),parent);

            if(!parents.containsKey(child.getTitle()))   {//file
                if(parent.equalsIgnoreCase("null"))
                    continue;
                MediciServiceImpl.relations.getFileAttrMap().put(child.getId(),(FileNode)child);
                CollectionNode parentDu = MediciServiceImpl.relations.getDuAttrMap().get(ids.get(parent));
                parentDu.addSub(CollectionNode.SubType.File, child.getId());
                MediciServiceImpl.relations.getDuAttrMap().put(ids.get(parent),parentDu);
            }
            else{

                if(!parent.equalsIgnoreCase("null"))      {
                    CollectionNode parentDu = MediciServiceImpl.relations.getDuAttrMap().get(ids.get(parent));
                    parentDu.addSub(CollectionNode.SubType.Collection, ids.get(child.getTitle()));
                    MediciServiceImpl.relations.getDuAttrMap().put(ids.get(parent),parentDu);
                }
                MediciServiceImpl.relations.getDuAttrMap().put(child.getId(), (CollectionNode)child);
                createORE(child.getTitle());
             }
        }
    }

    private static final String RESOURCE_MAP_SERIALIZATION_FORMAT = "RDF/XML";




    static BufferedWriter fetch;
    static BufferedWriter manifest;
    static BufferedWriter fgdc;


}
