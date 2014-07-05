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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import org.dataconservancy.dcs.access.ui.client.model.JsModel;

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

    @SuppressWarnings("unchecked")
    public JsArray<JsDerivedFrom> getDerivations() {
        return (JsArray<JsDerivedFrom>) getArray("prov:wasDerivedFrom");
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

    public JsArrayString getRelatedEntities(String entityId){
        JsArrayString relatedEntities = JavaScriptObject.createArray().cast();
        JsArray<JsDerivedFrom> derivations = getDerivations();
        int i = 0;
        for(int k =0;k<derivations.length();k++){
            if(derivations.get(k).getGeneratedEntity().equals(entityId))
            {	relatedEntities.set(i, derivations.get(k).getUsedEntity());
                i++;
            }
            else if(derivations.get(k).getUsedEntity().equals(entityId))
            {
                relatedEntities.set(i, derivations.get(k).getGeneratedEntity());
                i++;
            }
        }

        return relatedEntities;
    }

    public JsArray<JsAssociatedWith> getSafeActivities(){
        JsArray<JsAssociatedWith> activities = JavaScriptObject.createArray().cast();
        int i =0;
        try{

            JsArray<JsAssociatedWith> tempActivities = getActivities();
            for(int k =0;k<tempActivities.length();k++){
                if(tempActivities.get(k)!=null && tempActivities.get(k).getAgentId()!=null){
                    activities.set(i, tempActivities.get(k));
                    i++;
                }
            }
        }
        catch(Exception e){
            if(getActivity()!=null)
                activities.set(i, getActivity());
        }

        if(getActivity()!=null && getActivity().getActivityId()!=null)
            activities.set(i, getActivity());

        return activities;
    }

    public JsArray<JsAssociatedWith> getActivities(String agentId){
        JsArray<JsAssociatedWith> activities = JavaScriptObject.createArray().cast();
        try{
            int i =0;
            JsArray<JsAssociatedWith> tempActivities = getActivities();
            for(int k =0;k<tempActivities.length();k++){
                if(tempActivities.get(k).getAgentId().equalsIgnoreCase(agentId)){
                    activities.set(i, tempActivities.get(k));
                    i++;
                }
            }
        }
        catch(Exception e){
            if(getActivity()!=null)
                activities.set(0, getActivity());
        }

        if(getActivity()!=null && getActivity().getActivityId()!=null)
            activities.set(0, getActivity());

        return activities;
    }

    public JsArray<JsGenerated> getGeneratedBysByEntity(String entityId){
        JsArray<JsGenerated> generatedBys = JavaScriptObject.createArray().cast();
        int k = 0;
        try{

            for(int i=0;i<getGeneratedBys().length();i++){
                if(getGeneratedBys().get(i)!=null && getGeneratedBys().get(i).getActivityId()!=null && getGeneratedBys().get(i).getEntity().equals(entityId))
                {
                    generatedBys.set(k, getGeneratedBys().get(i));
                    k++;
                }
            }
        }
        catch (Exception e) {
            JsGenerated gen = getGeneratedBy();
            if(gen!=null && gen.getActivityId()!=null
                    &&gen.getEntity().equals(entityId))
                generatedBys.set(k, gen);
        }
        JsGenerated gen = getGeneratedBy();
        if(gen!=null && gen.getActivityId()!=null
                &&gen.getEntity().equals(entityId))
            generatedBys.set(k, gen);
        return generatedBys;
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
                if(agent.getAgentUrl().trim().equalsIgnoreCase(agentUrl.trim()))
                    return agent.getAgentId();
            }
        }catch(Exception e){
            JsAgent agent = getAgent();
            if(agent.getAgentUrl().trim().equalsIgnoreCase(agentUrl.trim()))
                return agent.getAgentId();
        }

        JsAgent agent = getAgent();
        if(agent!=null)
            if(agent.getAgentUrl().trim().equalsIgnoreCase(agentUrl.trim()))
                return agent.getAgentId();
        return null;
    }


    public String getAgentName(String agentUrl){
        JsArray<JsAgent> agents = getAgents();
        try{
            for(int i=0;i<agents.length();i++){
                JsAgent agent = agents.get(i);
                if(agent.getAgentUrl().trim().equalsIgnoreCase(agentUrl.trim()))
                    return agent.getAgentName();
            }
        }catch(Exception e){
            JsAgent agent = getAgent();
            if(agent.getAgentUrl().trim().equalsIgnoreCase(agentUrl.trim()))
                return agent.getAgentName();
        }

        JsAgent agent = getAgent();
        if(agent!=null)
            if(agent.getAgentUrl().trim().equalsIgnoreCase(agentUrl.trim()))
                return agent.getAgentName();

        return null;
    }

    public JsGenerated getEntityId(String activityId){
        JsArray<JsGenerated> generatedBys = getSafeGeneratedBys();
        try{
            for(int i=0;i<generatedBys.length();i++){
                JsGenerated gen = generatedBys.get(i);
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

    public JsArray<JsGenerated> getSafeGeneratedBys(){
        JsArray<JsGenerated> generatedBys = JavaScriptObject.createArray().cast();
        try{
            int count = getGeneratedBys().length();
            for(int i=0;i<getGeneratedBys().length();i++){
                if(getGeneratedBys().get(i)!=null && getGeneratedBys().get(i).getActivityId()!=null)
                    generatedBys.set(i, getGeneratedBys().get(i));
            }
        }
        catch (Exception e) {
            JsGenerated gen = getGeneratedBy();
            if(gen!=null && gen.getActivityId()!=null)
                generatedBys.set(0, gen);
        }
        JsGenerated gen = getGeneratedBy();
        if(gen!=null && gen.getActivityId()!=null)
            generatedBys.set(0, gen);
        return generatedBys;
    }


    public JsArray<JsProvEntity> getSafeEntities(){
        JsArray<JsProvEntity> entities = JavaScriptObject.createArray().cast();
        int i=0;
        try{
            int count = getEntities().length();
            for(;i<getEntities().length();i++){
                if(getEntities().get(i)!=null && getEntities().get(i).getEntityId()!=null)
                    entities.set(i, getEntities().get(i));
            }
        }
        catch (Exception e) {
            JsProvEntity entity = getEntity();
            if(entity!=null && entity.getEntityId()!=null)
                entities.set(i, entity);
        }

        JsProvEntity entity = getEntity();
        if(entity!=null && entity.getEntityId()!=null)
            entities.set(i, entity);

        return entities;
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

    public JsProvEntity getEntity(String entityUrl){
        JsArray<JsProvEntity> entities = getEntities();
        try{
            for(int i=0;i<entities.length();i++){
                JsProvEntity entity = entities.get(i);
                if(entity.getEntityUrl().equals(entityUrl))
                    return entity;
            }
        }
        catch (Exception e) {
            JsProvEntity entity = getEntity();
            if(entity.getEntityUrl().equals(entityUrl))
                return entity;
        }
        return null;
    }

    public JsProvEntity getEntityById(String entityId){
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


}
