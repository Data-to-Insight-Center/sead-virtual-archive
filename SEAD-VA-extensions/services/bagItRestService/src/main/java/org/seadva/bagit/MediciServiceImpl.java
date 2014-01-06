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

import org.sead.acr.common.utilities.json.JSONArray;
import org.sead.acr.common.utilities.json.JSONException;
import org.sead.acr.common.utilities.json.JSONObject;
import org.seadva.bagit.model.*;

import javax.servlet.ServletException;
import java.io.IOException;
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

    public Map<String,String> parseJson(String json) {
        Map<String,String> result = new HashMap<String, String>();

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);

            JSONObject resultObject = jsonObject.getJSONObject("sparql")
                    .getJSONObject("results");

            JSONArray resultArray = new JSONArray();
            if(resultObject.has("result")){
                try{
                    resultArray.put(0, resultObject.getJSONObject("result"));
                }
                catch(Exception e){
                    resultArray = resultObject.getJSONArray("result");
                }

            }

            for(int i =0; i< resultArray.length();i++){
                JSONArray binding = resultArray.getJSONObject(i).getJSONArray("binding");
                String title = "";
                String id = "";
                for(int j =0; j< binding.length();j++){
                    if(((String)binding.getJSONObject(j).get("name")).equalsIgnoreCase("id"))
                        id = (String)binding.getJSONObject(j).get("literal");
                    if(((String)binding.getJSONObject(j).get("name")).equalsIgnoreCase("name"))
                        title = (String)binding.getJSONObject(j).get("literal");
                }
                result.put(id, title);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public List<String> parseJsonAttribute(String json, String attribute) {

        List<String> result = new ArrayList<String>();

        JSONObject jsonObject;
        JSONArray resultArray = new JSONArray();
        try {
            jsonObject = new JSONObject(json);


            JSONObject resultObject = jsonObject.getJSONObject("sparql")
                    .getJSONObject("results");

            if(resultObject.has("result")){
                try{
                    resultArray.put(0, resultObject.getJSONObject("result"));
                }
                catch(Exception e){
                    resultArray = resultObject.getJSONArray("result");
                }

            }
        }catch (JSONException e) {
            System.out.println(json);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try{
            for(int i =0; i< resultArray.length();i++){
                JSONArray binding = resultArray.getJSONObject(i).getJSONArray("binding");
                String title = "";
                String id = "";
                for(int j =0; j< binding.length();j++){
                    if(((String)binding.getJSONObject(j).get("name")).equalsIgnoreCase("id"))
                        id = (String)binding.getJSONObject(j).get("literal");
                    if(id.equalsIgnoreCase(attribute))
                        if(((String)binding.getJSONObject(j).get("name")).equalsIgnoreCase("name"))
                        {
                            title = (String)binding.getJSONObject(j).get("literal");
                            result.add(title);
                        }
                }
            }
        }
        catch (JSONException e) {
            JSONObject binding = null;
            try {
                for(int i =0; i< resultArray.length();i++){
                    binding = resultArray.getJSONObject(i).getJSONObject("binding");
                    String title = "";
                    String key = (String)binding.get("name");
                    if(key.equalsIgnoreCase(attribute))
                    {
                        if(binding.get("literal") instanceof String)
                            title = (String)binding.get("literal");
                        else{
                            JSONObject temp = (JSONObject)binding.get("literal");
                            title = temp.get("content").toString();
                        }
                        result.add(title);
                    }
                }

            } catch (JSONException e1) {
                assert binding != null;
                String value = null;
                try {
                    value = (String)binding.get("uri");
                } catch (JSONException e2) {
                    e2.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                result.add(value);
            }

        }
        return result;
    }
    public void populateKids(String tagId, String type, MediciInstance mediciInstance) throws IOException, JSONException {

        String json = "";

        if(type.equals("collection")){

            json = util.getJsonResponse(mediciInstance, Query.DU_TITLE.getTitle(),tagId);

            CollectionNode du = new CollectionNode();
            List<String> titles = parseJsonAttribute(json, "title");
            if(titles.size()>0)
                du.setTitle(titles.get(0));


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

            json = util.getJsonResponse(mediciInstance, Query.FileSize.getTitle(), tagId);
            fileNode.setFileSize(Integer.parseInt(parseJsonAttribute(json,"size").get(0)));
            fileNode.setSource(mediciInstance.getUrl()+ "/api/image/download/"+tagId);

            relations.getFileAttrMap().put(tagId,fileNode);
        }

    }

}