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

import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.json.JSONArray;
import org.sead.acr.common.utilities.json.JSONException;
import org.sead.acr.common.utilities.json.JSONObject;
import org.seadva.bagit.model.MediciInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AcrQueryUtil{

	String query;
       public String getJsonResponse(MediciInstance t_instance,String predicate, String tagId) throws IOException, JSONException {
           query ="SELECT ?object WHERE { "+
                   "<"+tagId+"> <"+predicate+"> ?object ."+
                   " }";
           return getProxy(t_instance).getSparqlJSONResponse("query="+query);
        }

		protected MediciProxy getProxy(MediciInstance t_instance){
			MediciProxy _mp = new MediciProxy();
			_mp. setCredentials(t_instance.getUser(), t_instance.getPassword(),
                    t_instance.getUrl(),
                    t_instance.getRemoteAPI());
	    	return _mp;
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
            for(int i =0; i< resultArray.length();i++){
                try {

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
        }
        return result;
    }
}