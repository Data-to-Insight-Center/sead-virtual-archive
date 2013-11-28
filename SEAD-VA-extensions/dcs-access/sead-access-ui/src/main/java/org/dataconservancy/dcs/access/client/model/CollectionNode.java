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

package org.dataconservancy.dcs.access.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.view.client.ProvidesKey;

public class CollectionNode   implements java.io.Serializable{//implements Comparable<CollectionNode>

	public enum SubType {
	    Collection,File 
	}
    /**
     * The key provider that provides the unique ID of a contact.
     */
    public static final ProvidesKey<CollectionNode> KEY_PROVIDER = new ProvidesKey<CollectionNode>() {
      @Override
      public Object getKey(CollectionNode item) {
        return item == null ? null : item.getId();
      }
    };

    private String title;
    private String abstrct;
    private String date;
    private String site;
    private String contact;
    private String id;
    private Set<Creator> creators;
	private Map<SubType,List<String>> sub;
	
	public CollectionNode(){
		creators = new HashSet<Creator>();
		sub = new HashMap<SubType,List<String>>();
	}
    
    public String getId(){
	   return this.id;
    }
    public void setId(String id){
 	   this.id = id;
     }
    public String getTitle(){
 	   return this.title;
     }
    public void setTitle(String title){
 	   this.title = title;
     }
    public String getAbstract(){
  	   return this.abstrct;
      }
     public void setAbstract(String abstrct){
  	   this.abstrct = abstrct;
      }

     public Set<Creator> getCreators() {
 		return creators;
 	}


 	public void setCreators(Set<Creator> creators) {
 		this.creators = creators;
 	}
 	
 	public void addCreator(Creator creator){
 		this.creators.add(creator);
 	}


 	public Map<SubType,List<String>> getSub() {
 		return sub;
 	}


 	public void setSub(Map<SubType,List<String>> sub) {
 		this.sub = sub;
 	}
 	
 	public void addSub(SubType type,String subStr){
 		List<String> children;
 		if(this.sub.containsKey(type))
 			children = sub.get(type);
 		else
 			children = new ArrayList<String>();
 		children.add(subStr);
 			
 		this.sub.put(type,children);
 	}
     
     
    public int compareTo(CollectionNode o) {
      return (o == null || o.id == null) ? -1 : -o.id.compareTo(id);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof CollectionNode) {
        return id == ((CollectionNode) o).getId();
      }
      return false;
    }

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

  
  }
