package org.seadva.registry.mapper.util;

import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import org.seadva.registry.database.model.obj.vaRegistry.*;
import org.seadva.registry.database.model.obj.vaRegistry.Collection;

import java.util.*;

/**
 * THIS IS NOT BEING USED
 * The ROObject is used to represent a collection with its parent
 * and children (sub-collection)
 */
public class ROObject {
    // parent points to parent's entity id
    // children contains a list of child entity ids
    BaseEntity parent;
    List<BaseEntity> children;

    // jsonContext - Holds the context contents
    Map<String, String> jsonContext = new TreeMap<String, String>();

    public ROObject(){
        parent = null;
        children = new ArrayList<BaseEntity>();
    }

    public void setParent(BaseEntity entity){
        parent = entity;
    }

    public BaseEntity getParent(){
        return parent;
    }

    public List<BaseEntity> getChildren(){
        return children;
    }

    public void appendChild(BaseEntity entity){
        children.add(entity);
    }

    public String toJSON() throws JSONException {
        // Holds the details of the json i.e., title of collection, creator of collection, size of collection
        Map obj = new LinkedHashMap();

        // subCollList - Contains the entity id's of the sub-collection
        ArrayList<String> subCollList = new ArrayList<String>();
        // fileHolder - Represents a file object which holds the file details
        JSONObject fileHolder = new JSONObject();
        // filesList - Contains the list of files (fileHolder)
        ArrayList<Object> filesList = new ArrayList<Object>();

        // Populate the context with the properties
        BaseEntity parent = getParent();
        if(parent != null){
            for(Property property:parent.getProperties()){
                jsonContext.put(
                        property.getMetadata().getMetadataElement(),
                        property.getMetadata().getMetadataSchema()+property.getMetadata().getMetadataElement());
            }
        }
        jsonContext.put("Has Subcollection","http://purl.org/dc/terms/hasPart");
        jsonContext.put("Has Files","http://purl.org/dc/terms/hasPart");

        // Populate the obj map with property values
        if(parent != null){
            for(Property property:parent.getProperties()){
                obj.put(property.getMetadata().getMetadataElement(), property.getValuestr());
            }
        }

        // Get the list of children
        List<BaseEntity> list = getChildren();

        // For each entity, if the entity is the collection
        // add the collection id to the ArrayList - subCollList
        // else if the entity is a file, get the details of
        // the file into the fileHolder
        for(BaseEntity entity:list){
            if(entity instanceof Collection)
                subCollList.add(entity.getId());
            else if(entity instanceof File){
                for(Property property:entity.getProperties()){
                    if(property.getMetadata().getId().equalsIgnoreCase("md:6")){
                        if(!property.getValuestr().equalsIgnoreCase("-1")){
                            fileHolder.put(property.getMetadata().getMetadataElement(), property.getValuestr());
                        }
                    }else
                        fileHolder.put(property.getMetadata().getMetadataElement(), property.getValuestr());
                }
                filesList.add(fileHolder.toString());
            }

        }
        // Add the details to the "obj" Map
        obj.put("@context",jsonContext);
        if(subCollList.size() > 0)
            obj.put("has Subcollection", subCollList);
        if(filesList.size() > 0)
            obj.put("Has Files", filesList);

        JSONObject trial = new JSONObject(obj);

        // Serialize the "obj" Map to JSON
        Gson gson = new Gson();
        String jsonString = gson.toJson(trial);
        jsonString = jsonString.substring(jsonString.indexOf(':')+1);
        jsonString = jsonString.substring(0, jsonString.length() - 1);
        return jsonString;
    }
}

