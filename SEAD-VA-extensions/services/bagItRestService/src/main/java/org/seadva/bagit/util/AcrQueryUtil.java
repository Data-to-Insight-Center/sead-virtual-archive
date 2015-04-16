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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.seadva.bagit.model.AggregationType;
import org.seadva.bagit.model.MediciInstance;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

public class AcrQueryUtil {

    public static final String ACR_REST_CONTEXT = "@context";

    String query;

    public String getJsonResponse(MediciInstance t_instance, String predicate, String tagId) throws IOException, JSONException {
        query = "SELECT ?object WHERE { " +
                "<" + tagId + "> <" + predicate + "> ?object ." +
                " }";
        return getProxy(t_instance).getSparqlJSONResponse("query=" + query);
    }

    protected MediciProxy getProxy(MediciInstance t_instance) {
        MediciProxy _mp = new MediciProxy();
        _mp.setCredentials(t_instance.getUser(), t_instance.getPassword(),
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
            JSONObject sparqlObject = jsonObject.getJSONObject("sparql");
            Object child = sparqlObject.get("results");
            // first child can be empty. have to check
            if ((child instanceof String) && ("".equals(child.toString())) ) {
                return result;
            }
            JSONObject resultObject = sparqlObject.getJSONObject("results");
            if (resultObject.has("result")) {
                try {
                    resultArray.put(0, resultObject.getJSONObject("result"));
                } catch (Exception e) {
                    resultArray = resultObject.getJSONArray("result");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < resultArray.length(); i++) {
                JSONArray binding = resultArray.getJSONObject(i).getJSONArray("binding");
                String title = "";
                String id = "";
                for (int j = 0; j < binding.length(); j++) {
                    if (((String) binding.getJSONObject(j).get("name")).equalsIgnoreCase("id"))
                        id = (String) binding.getJSONObject(j).get("literal");
                    if (id.equalsIgnoreCase(attribute))
                        if (((String) binding.getJSONObject(j).get("name")).equalsIgnoreCase("name")) {
                            title = (String) binding.getJSONObject(j).get("literal");
                            result.add(title);
                        }
                }
            }
        } catch (JSONException e) {
            JSONObject binding = null;
            for (int i = 0; i < resultArray.length(); i++) {
                try {
                    binding = resultArray.getJSONObject(i).getJSONObject("binding");
                    String title = "";
                    String key = (String) binding.get("name");
                    if (key.equalsIgnoreCase(attribute)) {
                        if (binding.get("literal") instanceof String)
                            title = (String) binding.get("literal");
                        else {
                            if (binding.get("literal") instanceof Boolean) {
                                result.add(String.valueOf((Boolean) binding.get("literal")));
                                continue;
                            }
                            JSONObject temp = (JSONObject) binding.get("literal");
                            title = temp.get("content").toString();
                        }
                        result.add(title);
                    }
                } catch (JSONException e1) {
                    assert binding != null;
                    String value = null;
                    try {
                        value = (String) binding.get("uri");
                    } catch (JSONException e2) {
                        e2.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    result.add(value);
                }
            }
        }
        return result;
    }

    public Map<String, List<String>> readMetadata(MediciInstance t_instance, String tagId)
            throws IOException {
        // call ACR REST api and get a dynamic metadata set of given tagID
        String path = "/resteasy/datasets/";
        if (tagId.contains("Collection") || tagId.contains(":col_")) {
            path = "/resteasy/collections/";
        }
        String json = getProxy(t_instance).executeAuthenticatedGet(path +
                URLEncoder.encode(tagId) + "/unique", "");
              //  URLEncoder.encode(tagId) , "");

        Map<String, List<String>> metadata = new HashMap<String, List<String>>();
        try {
            JSONObject response = new JSONObject(json);
            JSONObject context = response.getJSONObject(ACR_REST_CONTEXT);
            // iterate through all children
            Iterator itr = response.keys();
            while (itr.hasNext()) {
                String child = (String) itr.next();
                // ignore context object
                if (child.equals(ACR_REST_CONTEXT)) {
                    continue;
                }
                // add predicate and value to the map
                String predicate;
                if(child.equals("Comment")){
                    predicate = "http://sead-data.net/vocab/hasComment";
                } else if(child.equals("GeoPoint")) {
                    predicate = "http://sead-data.net/vocab/hasGeoPoint";
                } else {
                    predicate = context.get(child).toString();
                }
                List<String> list = metadata.get(predicate);
                if (list == null) {
                    list = new ArrayList<String>();
                    metadata.put(predicate, list);
                }
                Object value = response.get(child);
                if (value instanceof String) {
                    list.add(value.toString());
                } else if (value instanceof JSONArray) {
                    JSONArray array = (JSONArray) value;
                    for (int i = 0; i < array.length(); i++) {
                        Object arrayItem = array.get(i);
                        if (arrayItem instanceof String) {
                            list.add(arrayItem.toString());
                        }else if(arrayItem instanceof JSONObject) {
                            list.add(arrayItem.toString());
                        }
                    }
                } else if (value instanceof JSONObject) {
                    list.add(value.toString());
                }
            }
        } catch (JSONException e) {
            // TODO : Use logging and handle exceptions
            e.printStackTrace();
        }

        return metadata;
    }

    public Map<AggregationType, List<String>> getAggregations(MediciInstance t_instance, String tagId)
            throws IOException {

        String path = "/resteasy/collections/";
;
        Map<AggregationType, List<String>> metadata = new HashMap<AggregationType, List<String>>();

            List<String> CollectionsList = new ArrayList<String>();
            List<String> FilesList = new ArrayList<String>();
            String json_collections = getProxy(t_instance).executeAuthenticatedGet(path +
                    URLEncoder.encode(tagId) + "/collections", "");
            String json_datasets = getProxy(t_instance).executeAuthenticatedGet(path +
                    URLEncoder.encode(tagId) + "/datasets", "");
            try {
                JSONObject response = new JSONObject(json_collections);
                Iterator itr = response.keys();
                while (itr.hasNext()) {
                    String child = (String) itr.next();
                    if (child.equals(ACR_REST_CONTEXT)) {
                        continue;
                    }
                    CollectionsList.add(response.getJSONObject(child).get("Identifier").toString());
                }
                response = new JSONObject(json_datasets);
                itr = response.keys();
                while (itr.hasNext()) {
                    String child = (String) itr.next();
                    if (child.equals(ACR_REST_CONTEXT)) {
                        continue;
                    }
                    FilesList.add(response.getJSONObject(child).get("Identifier").toString());
                }
            } catch (JSONException e) {
                // TODO : Use logging and handle exceptions
                e.printStackTrace();
            }
            metadata.put(AggregationType.COLLECTION , CollectionsList);
            metadata.put(AggregationType.FILE, FilesList);

        return metadata;
    }

}
