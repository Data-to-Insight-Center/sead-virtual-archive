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

import org.apache.commons.io.IOUtils;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.sead.acr.common.utilities.json.JSONException;
import org.sead.acr.common.utilities.json.JSONObject;

import javax.servlet.ServletException;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class MediciServiceImpl {
    Logger logger = Logger.getLogger(this.getClass().toString());

    public static DatasetRelation relations = new DatasetRelation();

    VAQueryUtil util;
    public MediciServiceImpl(){util=new VAQueryUtil();}




   public void init() throws ServletException {
   }

    public List<String>  parseJsonAttribute(String json, String attribute) {
        List<String> result = new ArrayList<String>();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);

            JSONObject binding = jsonObject.getJSONObject("sparql")
                    .getJSONObject("results")
                    .getJSONObject("result")
                    .getJSONObject("binding");
            if(((String)binding.get("name")).equalsIgnoreCase(attribute))
                result.add((String) binding.get("literal"));

            return result;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    static int i=0;
    public void populateKids(String tagId, String type, MediciInstance mediciInstance) throws IOException, JSONException {

        String json = "";

        if(type.equals("collection")){

            json = util.getJsonResponse(mediciInstance,Query.DU_TITLE.getTitle(),tagId);

            CollectionNode du = new CollectionNode();
            du.setTitle(parseJsonAttribute(json, "title").get(0));


            json = util.getJsonResponse(mediciInstance,Query.DuCreator.getTitle(),tagId);

            List<String> creators =parseJsonAttribute(json,"creator");


            Set<Creator> tempCreators = new HashSet<Creator>();
            if(creators!=null)
                for(String creator:creators){
                    Creator cr = new Creator();
                    int splitCreator = creator.indexOf(':');

                    if(splitCreator==-1)
                    {
                        cr.setCreatorId(creator);
                        cr.setCreatorIdType(creator);
                        cr.setCreatorName("unknown");
                    }
                    else{
                        String creatorName = creator.substring(0,splitCreator);
                        String creatorId = creator.substring(splitCreator+1);
                        String creatorIdType = "vivo";

                        cr.setCreatorId(creatorId);
                        cr.setCreatorIdType(creatorIdType);
                        cr.setCreatorName(creatorName);
                    }
                    tempCreators.add(cr);
                }
            du.setCreators(tempCreators);

            json = util.getJsonResponse(mediciInstance, Query.DuAbstract.getTitle(), tagId);

            List<String> abstrct =parseJsonAttribute(json,"abstract");

            if(abstrct!=null)
                if(abstrct.size()>0)
                    du.setAbstract(abstrct.get(0));

            String xmlDate  = "";
            json = util.getJsonResponse(mediciInstance, Query.Date.getTitle(), tagId);

            List<String> dates =parseJsonAttribute(json,"date");

            if(dates!=null)
                if(dates.size()>0)
                    du.setDate(dates.get(0));

            json = util.getJsonResponse(mediciInstance, Query.Contact.getTitle(), tagId);


            List<String> contacts =parseJsonAttribute(json,"contact");

            if(contacts!=null)
                if(contacts.size()>0)
                    du.setContact(contacts.get(0));

            json = util.getJsonResponse(mediciInstance,Query.Site.getTitle(),tagId);

            List<String> sites =parseJsonAttribute(json,"site");

            if(sites!=null)
                if(sites.size()>0)
                    du.setSite(sites.get(0));

            json =util.getJsonResponse(mediciInstance, Query.DuSub.getTitle(), tagId);

            List<String> children = parseJsonAttribute(json, "sub");

            du.setId(tagId);

            for(String result:children){
                relations.getParentMap().put(result, tagId);
                if(result.contains("Collection")){
                    du.addSub(CollectionNode.SubType.Collection, result);
                    populateKids(result, "collection",mediciInstance);
                }
                else
                {
                    du.addSub(CollectionNode.SubType.File, result);
                    populateKids(result, "file",mediciInstance);
                }
            }

            relations.getDuAttrMap().put(tagId, du);
        }
        else
        {
            json = util.getJsonResponse(mediciInstance, Query.File.getTitle(), tagId);

            FileNode fileNode = new FileNode();
            fileNode.setTitle(parseJsonAttribute(json,"name").get(0));
            fileNode.setId(tagId);

            json = util.getJsonResponse(mediciInstance, Query.FileFormat.getTitle(), tagId);

            List<String> formats = parseJsonAttribute(json, "format");
            for(String format:formats){
                fileNode.addFormat(format);
            }

            relations.getFileAttrMap().put(tagId,fileNode);
        }



    }

}