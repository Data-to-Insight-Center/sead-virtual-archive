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

package org.dataconservancy.dcs.access.client.model;

import com.google.gwt.core.client.JsArray;
import org.dataconservancy.dcs.access.ui.client.model.JsModel;

import java.util.ArrayList;
import java.util.List;

// TODO JSON seems to use metadataRefs and metadataRef...

/**
 * Models a Data Conservancy Deliverable Unit
 */
public final class JsProvDocument
        extends JsEntity {
	
	
    protected JsProvDocument() {
    }
    
	public static JsProvDocument create(String jsonString) {
        return (JsProvDocument) JsModel
                .parseJSON(jsonString);
    }
    
    @SuppressWarnings("unchecked")
	public JsArray<JsAssociatedWith> getActivities() {
        return (JsArray<JsAssociatedWith>) getArray("prov:wasAssociatedWith");
    }
    
    @SuppressWarnings("unchecked")
	public JsAssociatedWith getActivity() {
        return (JsAssociatedWith) getObject("prov:wasAssociatedWith");
    }
        
    @SuppressWarnings("unchecked")
	public JsAgent getAgent() {
        return (JsAgent) getObject("prov:agent");
    }  
    
    @SuppressWarnings("unchecked")
	public JsArray<JsAgent> getAgents() {
        return (JsArray<JsAgent>) getArray("prov:agent");
    }
    
    public int getActivityCount(){   	
    	int count = 0;
    	try{
    		JsArray<JsAssociatedWith> activities = getActivities();
    		count = activities.length();
    	}
    	catch(Exception e){
    		if(getActivity()!=null)
    			count = 1;
    	}
    	return count;
    }
    
    public List<String> getActivityIds(){   	
    	List<String> activityIds = new ArrayList<String>();
    	try{
    		JsArray<JsAssociatedWith> activities = getActivities();
    		for(int i =0; i< activities.length(); i++){
    			activityIds.add(activities.get(i).getActivityId());
    		}
    	}
    	catch(Exception e){
    		if(getActivity()!=null)
    			activityIds.add(getActivity().getActivityId());
    	}
    	return activityIds;
    }
    
    @SuppressWarnings("unchecked")
   	public JsProvEntity getEntity() {
           return (JsProvEntity) getObject("prov:entity");
    }
    
    @SuppressWarnings("unchecked")
   	public JsArray<JsProvEntity> getEntities() {
           return (JsArray<JsProvEntity>) getArray("prov:entity");
    }
    
    @SuppressWarnings("unchecked")
	public JsArray<JsGenerated> getGeneratedBys() {
        return (JsArray<JsGenerated>) getArray("prov:wasGeneratedBy");
    }
    
    @SuppressWarnings("unchecked")
   	public JsGenerated getGeneratedBy() {
           return (JsGenerated) getObject("prov:wasGeneratedBy");
       }
    
    
    public String getAgentId(String agentUrl){
    	JsArray<JsAgent> agents = getAgents();
    	try{
    		for(int i=0;i<agents.length();i++){
	    		JsAgent agent = agents.get(i);
	    		if(agent.getAgentUrl().equals(agentUrl))
	    			return agent.getAgentId();
	    	}
    	}
    	catch (Exception e) {
    		JsAgent agent = getAgent();
    		if(agent.getAgentUrl().equals(agentUrl))
    			return agent.getAgentId();
		}
    		
    	return null;
    }
    
    
    public String getAgentName(String agentUrl){
    	JsArray<JsAgent> agents = getAgents();
    	try{
    		for(int i=0;i<agents.length();i++){
	    		JsAgent agent = agents.get(i);
	    		if(agent.getAgentUrl().equals(agentUrl))
	    			return agent.getAgentName();
	    	}
    	}
    	catch (Exception e) {
    		JsAgent agent = getAgent();
    		if(agent.getAgentUrl().equals(agentUrl))
    			return agent.getAgentName();
		}
    		
    	return null;
    }
    
    public JsGenerated getEntityId(String activityId){
    	JsArray<JsGenerated> generated = getGeneratedBys();
    	try{
	    	for(int i=0;i<generated.length();i++){
	    		JsGenerated gen = generated.get(i);
		    	if(gen.getActivityId().equals(activityId))
		    		return gen;
	    	}
    	}
    	catch (Exception e) {
    		JsGenerated gen = getGeneratedBy();
    		if(gen.getActivityId().equals(activityId))
	    		return gen;
		}
    	return null;    	
    }
    
    public String getEntityUrl(String entityId){
    	JsArray<JsProvEntity> entities = getEntities();
    	try{
    		for(int i=0;i<entities.length();i++){
	    		JsProvEntity entity = entities.get(i);
	    		if(entity.getEntityId().equals(entityId))
	    			return entity.getEntityUrl();
	    	}
    	}
    	catch (Exception e) {
    		JsProvEntity entity = getEntity();
    		if(entity.getEntityId().equals(entityId))
    			return entity.getEntityUrl();
    	}
    	return null;
    }    
    
    
    public JsProvEntity getEntity(String entityId){
    	
    	JsArray<JsProvEntity> entities = getEntities();
    	try{
    		for(int i=0;i<entities.length();i++){
	    		JsProvEntity entity = entities.get(i);
	    		if(entity.getEntityId().equals(entityId))
	    			return entity;
    		}
    	}
    	catch (Exception e) {
    		JsProvEntity entity = getEntity();
    		if(entity.getEntityId().equals(entityId))
    			return entity;
    	}
    	
    	return null;
    }    
    
    
    public String getActivityId(String agentId){
    	JsArray<JsAssociatedWith> activities = getActivities();
    	try{
	    	for(int i=0;i<activities.length();i++){
	    		JsAssociatedWith activity = activities.get(i);
	    		if(activity.getAgentId().equals(agentId))
	    			return activity.getActivityId();
	    	}
    	}
    	catch (Exception e) {
    		JsAssociatedWith activity = getActivity();
    		if(activity.getAgentId().equals(agentId))
    			return activity.getActivityId();
    	}
    	
    	return null;
    }
    
    public String getEventType(String activityId){
    	JsArray<JsAssociatedWith> activities = getActivities();
    	try{
	    	for(int i=0;i<activities.length();i++){
	    		JsAssociatedWith activity = activities.get(i);
	    		if(activity.getActivityId().equals(activityId))
	    			return activity.getEventType();
	    	}
    	}
    	catch (Exception e) {
    		JsAssociatedWith activity = getActivity();
    		if(activity.getActivityId().equals(activityId))
    			return activity.getEventType();
    	}
    	return null;
    }
}
