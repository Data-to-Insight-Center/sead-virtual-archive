/*
 * Copyright 2014 The Trustees of Indiana University
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

package org.dataconservancy.dcs.access.server;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.access.client.api.InputService;
import org.dataconservancy.dcs.access.server.model.Data;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class GenerateTreeInputImpl extends RemoteServiceServlet implements InputService
{
	private Data getRevisonOf(String root, String json){
		Type mapType = new TypeToken<Map<String, List<String>>>() {
        }.getType();
        Map<String, List<String>> map = new GsonBuilder().create().fromJson(json, mapType);
        Data dTemp = new Data();
        dTemp.setLink(root);
		dTemp.setTitle(root);
        if(map.containsKey(root)){
        	;
        	for(String child:map.get(root)){
				dTemp.addChild(getRevisonOf(child, json));
			}
        }
		return dTemp;

	}
	
	private String findRoot(String json){
		Type mapType = new TypeToken<Map<String, List<String>>>() {
        }.getType();
        Map<String, List<String>> map = new GsonBuilder().create().fromJson(json, mapType);
       String root = null;
       
       int set = 0;
        
		Iterator iter = map.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, List<String>> pair = (Entry<String, List<String>>) iter.next();
			if(set==0){
				root = pair.getKey();//parent
				set = 1;
			}
			for(String child:pair.getValue()){
				if(root.equalsIgnoreCase(child))
					root = pair.getKey();
			}
		}
		return root;

	}

	@Override
	public String getLineageInput(String id, String roUrl) {
	  WebResource webResource = Client.create().resource(roUrl);


        ClientResponse response = webResource.path("resource")
                .path("lineage")
                .path(
                        URLEncoder.encode(
                        		id
                        )
                )
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        try {
			IOUtils.copy(response.getEntityInputStream(), writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String json = writer.toString();
        
        if(json.startsWith("["))
        		json = json.substring(1,json.length()-1);
        json  = json.replace("id", "identifier");
		return json;
	}

}