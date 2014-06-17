package org.seadva.data.lifecycle.support;


import edu.indiana.d2i.komadu.axis2.client.*;
import edu.indiana.d2i.komadu.query.*;
import org.apache.axis2.AxisFault;
import org.seadva.data.lifecycle.support.model.Entity;
import org.seadva.data.lifecycle.support.model.Event;
import org.seadva.registry.database.model.obj.vaRegistry.Agent;

import java.rmi.RemoteException;
import java.util.Calendar;

/**
 * Komadu Ingester
 */
public class KomaduIngester {

    KomaduServiceStub serviceStub;

    public KomaduIngester(String komaduServiceUrl) throws AxisFault {
        serviceStub = new KomaduServiceStub(
                komaduServiceUrl
                );
    }
    public void trackEvent(Event event, Agent agentEntity, Entity collection) throws Exception {


        String agentId = event.getLinkingAgentIdentifier();
        String activityId = event.getWorkflowId()+"_"+event.getEventType();

        AgentType agent = createAgent(agentId, agentEntity);

        ActivityType workflowActivity = createWorkflowActivity(activityId);

        AddAgentActivityRelationshipDocument agentActivity = AddAgentActivityRelationshipDocument.Factory.newInstance();
        AgentActivityType agentActivityType = AgentActivityType.Factory.newInstance();

        AssociationType association1 = createAssociation1(agentId, activityId);
        agentActivityType.setActivity(workflowActivity);
        agentActivityType.setAgent(agent);

        AttributesType associationAttributes = AttributesType.Factory.newInstance();
        AttributeType[] associationAttributesArr = new AttributeType[1];
        // Attribute 1
        AttributeType attribute1 = createAttribute("event-type", event.getEventType());
        associationAttributesArr[0] = attribute1;
        associationAttributes.setAttributeArray(associationAttributesArr);
        association1.setAttributes(associationAttributes);

        agentActivityType.setAssociation(association1);
        agentActivity.setAddAgentActivityRelationship(agentActivityType);



        // execute

        serviceStub.addAgentActivityRelationship(agentActivity);

        //Track activity and entity

        EntityType collectionEntity = createCollectionEntity(collection);
        AddActivityEntityRelationshipDocument activityEntity = AddActivityEntityRelationshipDocument.Factory.newInstance();
        ActivityEntityType activityEntityType = ActivityEntityType.Factory.newInstance();

        GenerationType generation = GenerationType.Factory.newInstance();
        generation.setActivityID(activityId);
        generation.setEntityID(event.getTargetId());
        generation.setLocation("SEADVA");
        generation.setTimestamp(Calendar.getInstance());

        // Attributes
        AttributesType attributes = AttributesType.Factory.newInstance();
        AttributeType[] attributesArr = new AttributeType[1];
        // Attribute 1
        AttributeType att1 = createAttribute("event", "preservation_workflow");
        attributesArr[0] = att1;
        attributes.setAttributeArray(attributesArr);
        generation.setAttributes(attributes);

        activityEntityType.setActivity(workflowActivity);
        activityEntityType.setEntity(collectionEntity);
        activityEntityType.setGeneration(generation);
        activityEntity.setAddActivityEntityRelationship(activityEntityType);
        // invoke
        serviceStub.addActivityEntityRelationship(activityEntity);

    }

    public void trackRevision(Entity previousVersion, Entity nextVersion) throws Exception {

        AddEntityEntityRelationshipDocument activityEntity = AddEntityEntityRelationshipDocument.Factory.newInstance();
        EntityEntityType entityEntityType = EntityEntityType.Factory.newInstance();

        RevisionType revision = RevisionType.Factory.newInstance();
        revision.setUsedEntityID(previousVersion.getId());
        revision.setGeneratedEntityID(nextVersion.getId());

        EntityType previousVersionEntity = createCollectionEntity(previousVersion);
        EntityType nextVersionEntity = createCollectionEntity(nextVersion);

        entityEntityType.setEntity1(previousVersionEntity);
        entityEntityType.setEntity2(nextVersionEntity);
        entityEntityType.setRevision(revision);

        activityEntity.setAddEntityEntityRelationship(entityEntityType);
        // invoke
        serviceStub.addEntityEntityRelationship(activityEntity);
    }

    public void trackDerivation(Entity previousVersion, Entity nextVersion) throws Exception {

        AddEntityEntityRelationshipDocument activityEntity = AddEntityEntityRelationshipDocument.Factory.newInstance();
        EntityEntityType entityEntityType = EntityEntityType.Factory.newInstance();

        DerivationType derivationType = DerivationType.Factory.newInstance();
        derivationType.setUsedEntityID(previousVersion.getId());
        derivationType.setGeneratedEntityID(nextVersion.getId());

        EntityType previousVersionEntity = createCollectionEntity(previousVersion);
        EntityType nextVersionEntity = createCollectionEntity(nextVersion);

        entityEntityType.setEntity1(previousVersionEntity);
        entityEntityType.setEntity2(nextVersionEntity);
        entityEntityType.setDerivation(derivationType);

        activityEntity.setAddEntityEntityRelationship(entityEntityType);
        // invoke
        serviceStub.addEntityEntityRelationship(activityEntity);
    }

    private static EntityType createCollectionEntity(Entity collectionEntity) throws Exception {
        EntityType entity = EntityType.Factory.newInstance();
        CollectionType collection = CollectionType.Factory.newInstance();
        collection.setCollectionURI(collectionEntity.getId());


        if(collectionEntity.getChildren()!=null){
            MembersType members = MembersType.Factory.newInstance();
            EntityType[] memberArray = new EntityType[collectionEntity.getChildren().size()];

            int i =0;
            for(Entity memberEntity:collectionEntity.getChildren()){
                EntityType memEntity = createFileEntity(memberEntity);
                memberArray[i] = memEntity;
                i++;
            }
            members.setMemberArray(memberArray);
            collection.setMembers(members);
        }

        entity.setCollection(collection);

        if(collectionEntity.getName()!=null){
            AttributesType attributes = AttributesType.Factory.newInstance();
            AttributeType[] attributesArr = new AttributeType[1];
            // Attribute 1
            AttributeType att1 = createAttribute("title", collectionEntity.getName());
            attributesArr[0] = att1;
            attributes.setAttributeArray(attributesArr);
            entity.setAttributes(attributes);
        }
        return entity;
    }

     private static EntityType createFileEntity(Entity fileEntity) throws Exception {
        EntityType entity = EntityType.Factory.newInstance();
        FileType file = FileType.Factory.newInstance();
        file.setFileName(fileEntity.getName());
        file.setFileURI(fileEntity.getId());
        file.setCreateDate(Calendar.getInstance());

        entity.setFile(file);
        return entity;
    }

    public GetEntityGraphResponseDocument getEntityGraph(String entityGraphURI) {
        try {
            System.out.println("\n\n Entity Graph without context \n\n");
            GetEntityGraphRequestDocument entityGraphRequest = GetEntityGraphRequestDocument.Factory.newInstance();
            GetEntityGraphRequestType entityRequestType = GetEntityGraphRequestType.Factory.newInstance();
            entityRequestType.setInformationDetailLevel(DetailEnumType.FINE);
            entityRequestType.setEntityURI(entityGraphURI);
            entityRequestType.setEntityType(edu.indiana.d2i.komadu.query.EntityEnumType.FILE);
            entityGraphRequest.setGetEntityGraphRequest(entityRequestType);
            GetEntityGraphResponseDocument entityResponse = serviceStub.getEntityGraph(entityGraphRequest);
            return entityResponse;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ActivityType createWorkflowActivity(String workflowId) throws Exception {
        ActivityType activity = ActivityType.Factory.newInstance();
        WorkflowInformationType workflow = WorkflowInformationType.Factory.newInstance();
        workflow.setWorkflowID(workflowId);
        workflow.setWorkflowNodeID("node1");
        workflow.setTimestep(20);
        // Instance of
        InstanceOfType instanceOf = InstanceOfType.Factory.newInstance();
        instanceOf.setInstanceOfID("sead_workflow");
        instanceOf.setVersion("1.0.0");
        instanceOf.setCreationTime(Calendar.getInstance());
        workflow.setInstanceOf(instanceOf);
        // Attributes
        AttributesType attributes = AttributesType.Factory.newInstance();
        AttributeType[] attributesArr = new AttributeType[1];
        // Attribute 1
        AttributeType att1 = createAttribute("domain", "preservation");
        attributesArr[0] = att1;
        attributes.setAttributeArray(attributesArr);
        workflow.setAttributes(attributes);

        activity.setWorkflowInformation(workflow);
        activity.setLocation("SEAD-VA");
        return activity;
    }

    private static AgentType createAgent(String agentId, Agent agentEntity) throws Exception {
        // Delegate Agent
        AgentType agent = AgentType.Factory.newInstance();
        // User Agent
        UserAgentType userAgent = createUserAgent(agentEntity.getFirstName()+" "+agentEntity.getLastName(), "SEAD VA", agentId, agentId);
        agent.setUserAgent(userAgent);
        agent.setType(AgentEnumType.PERSON);
        return agent;
    }

    private static AssociationType createAssociation1(String agentId, String activityId) throws Exception {
        AssociationType association = AssociationType.Factory.newInstance();
        association.setAgentID(agentId);
        association.setActivityID(activityId);
        return association;
    }

    private static UserAgentType createUserAgent(String name, String aff, String email, String id) {
        UserAgentType userAgent = UserAgentType.Factory.newInstance();
        userAgent.setFullName(name);
        userAgent.setAffiliation(aff);
        userAgent.setEmail(email);
        userAgent.setAgentID(id);
        return userAgent;
    }

    private static AttributeType createAttribute(String name, String val) throws Exception {
        AttributeType att = AttributeType.Factory.newInstance();
        att.setProperty(name);
        att.setValue(val);
        return att;
    }
}
