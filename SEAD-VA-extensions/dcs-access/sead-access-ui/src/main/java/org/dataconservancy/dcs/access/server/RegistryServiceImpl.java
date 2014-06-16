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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.access.client.api.RegistryService;
import org.dataconservancy.dcs.access.shared.Person;
import org.dataconservancy.dcs.access.shared.ROMetadata;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.seadva.bagit.impl.ConfigBootstrap;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.data.lifecycle.support.model.Event;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.seadva.registry.client.RegistryClient;
import org.seadva.registry.database.model.obj.vaRegistry.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.ws.http.HTTPException;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.*;

@SuppressWarnings("serial")
public class RegistryServiceImpl extends RemoteServiceServlet
  implements RegistryService
{

	@Override
	public boolean registerAgents(List<Person> persons, String registryUrl) {
	      Gson gson = new GsonBuilder()
	        .excludeFieldsWithoutExposeAnnotation()
	        .create();
	      
		for(Person person: persons){
				 String agentRegistryId = person.getRegistryId();
		        
				 Agent agent = new Agent();
		         agent.setId(agentRegistryId);
		         agent.setEntityName(person.getFirstName()+" "+person.getLastName());
		         agent.setEntityLastUpdatedTime(new Date());
		         agent.setEntityCreatedTime(new Date());
		         agent.setFirstName(person.getFirstName());
		         agent.setLastName(person.getLastName());
		         
		         AgentRole role = new AgentRole();
		         AgentRolePK agentRolePK = new AgentRolePK();
	           
		         RoleType roleType;
				try {
					roleType = getRoleByName(person.getRole().getName(), gson, registryUrl);
			         agentRolePK.setRoleType(roleType);
			         role.setId(agentRolePK);
			         agent.addAgentRole(role);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(person.getVivoId()!=null){
			     	AgentProfile profile = new AgentProfile();
			     	profile.setProfileValue(person.getVivoId());
			     	
			     	AgentProfilePK profilePK = new AgentProfilePK();
			     	profilePK.setAgent(agent);
			     	try {
						profilePK.setProfileType(getProfileType("vivo", gson, registryUrl));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			     	
			     	profile.setId(profilePK);
			     	agent.addAgentProfile(profile);
		         }
		     	
		         MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		         List<String> values = new ArrayList<String>();
		         values.add(gson.toJson(agent));
		         params.put("entity",values);

		         List<String> types = new ArrayList<String>();
		         types.add("org.seadva.registry.database.model.obj.vaRegistry.Agent");

		         params.put("type", types);
		         WebResource webResource = Client.create().resource(
		        		 registryUrl
		        		 );
		         ClientResponse response = webResource.path("resource")
		                 .path("agent")
		                 .path(
		                         URLEncoder.encode(
                                         agent.getId()
		                         )
		                 )
		                 .queryParams(params)
		                 .post(ClientResponse.class);

		         if(response.getStatus()!=200)
		             throw new HTTPException(response.getStatus());
		}
		return true;
	}
	
	
  private static RoleType getRoleByName(String role, Gson gson, String registryUrl) throws IOException {
        WebResource webResource = Client.create().resource(
        		registryUrl
        		);

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("roleType")
                .path(
                        role
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        RoleType roleType =  gson.fromJson(writer.toString(), RoleType.class);
        return roleType;
    }
	    
  
  private static ProfileType getProfileType(String profile, Gson gson, String registryUrl) throws IOException {
      WebResource webResource = Client.create().resource(
    		  registryUrl
    		  );

      MultivaluedMap<String, String> params = new MultivaluedMapImpl();

      ClientResponse response = webResource.path("resource")
              .path("profileType")
              .path(
                      profile
              )
              .queryParams(params)
              .get(ClientResponse.class);

      StringWriter writer = new StringWriter();
      IOUtils.copy(response.getEntityInputStream(), writer);
      ProfileType profileType =  gson.fromJson(writer.toString(), ProfileType.class);
      return profileType;
  }
	    
	 
  @Override
	public List<ROMetadata> getAllCOs(String repository, String agentId, String roUrl) throws IOException{
		WebResource webResource = Client.create().resource(
				roUrl
				);
         webResource = webResource.path("resource")
        		.path("listRO")
                .queryParam("type", "CurationObject")
                .queryParam("repository", repository);
                
         if(agentId!=null)
                webResource = webResource.queryParam("submitterId", agentId);
         
        ClientResponse response = webResource        
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.println(writer.toString());
        Type listType = new TypeToken<ArrayList<ROMetadata>>() {
        }.getType();
        List<ROMetadata> list = new GsonBuilder().create().fromJson(writer.toString(), listType);
		return list;
	}
	
	@Override
	public void updateSip(String sipPath, String entityId, String key, String value) throws InvalidXmlException, FileNotFoundException{
		ResearchObject sip = new SeadXstreamStaxModelBuilder().buildSip(new FileInputStream(sipPath));
		
		int changed = 0;
		java.util.Collection<DcsDeliverableUnit> deliverableUnits = sip.getDeliverableUnits();
		for(DcsDeliverableUnit entity: deliverableUnits)
			if(entity.getId().equals(entityId)){
				if(key.equals("title"))
					entity.setTitle(value);
				else if(key.equals("abstract"))
					((SeadDeliverableUnit)entity).setAbstrct(value);
				else{
					DcsMetadata dcsMetadata = new DcsMetadata();

                    String splitChar = "/";
                    if(key.contains("#"))
                        splitChar="#";
                    String ns = key.substring(0, key.lastIndexOf(splitChar));
                    dcsMetadata.setSchemaUri(ns);
					Map<String,Object> map = new HashMap<String,Object>();
                    map.put(key, value);
                    XStream xStream = new XStream(new DomDriver());
                    xStream.alias("map", Map.class);
                    String metadata = xStream.toXML(map);
                    dcsMetadata.setMetadata(metadata);
                    entity.addMetadata(dcsMetadata);
				}
				changed = 1;
		}

		if(changed==1){
			sip.setDeliverableUnits(deliverableUnits);
			saveSip(sip, sipPath);
			return;
		}

		java.util.Collection<DcsFile> files = sip.getFiles();
		for(DcsFile entity: files){
			if(entity.getId().equals(entityId)){
				if(key.equals("title"))
					entity.setName(value);
				else{
					DcsMetadata dcsMetadata = new DcsMetadata();

                    String splitChar = "/";
                    if(key.contains("#"))
                        splitChar="#";
                    String ns = key.substring(0, key.lastIndexOf(splitChar));
                    dcsMetadata.setSchemaUri(ns);
					Map<String,Object> map = new HashMap<String,Object>();
                    map.put(key, value);
                    XStream xStream = new XStream(new DomDriver());
                    xStream.alias("map", Map.class);
                    String metadata = xStream.toXML(map);
                    dcsMetadata.setMetadata(metadata);
                    entity.addMetadata(dcsMetadata);
				}
				
				changed = 1;
			}
		}
		
		if(changed==1){
			sip.setFiles(files);
			saveSip(sip, sipPath);
			return;
		}
	}
	
	private void saveSip(ResearchObject sip, String sipPath){
		try {
			new SeadXstreamStaxModelBuilder().buildSip(sip, new FileOutputStream(sipPath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void putRO(String sipPath, String roUrl){
		
		//Get SIP and convert SIP to ORE and call put method
		System.out.println("put call:"+sipPath);
		String orePath = oreConversion(sipPath);
		System.out.println("put call:"+orePath);
		WebResource webResource = Client.create().resource(
				roUrl
				);
		java.io.File file = new java.io.File(
				orePath
		);
		FileDataBodyPart fdp = new FileDataBodyPart("file", file,
		        MediaType.APPLICATION_OCTET_STREAM_TYPE);

		FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

		formDataMultiPart.bodyPart(fdp);

		ClientResponse response = webResource.path("resource")
		        .path("putro")
		        .type(MediaType.MULTIPART_FORM_DATA)
		        .post(ClientResponse.class, formDataMultiPart);
	}

	@Override
	public String getRO(String roId, String roUrl) throws IOException {
        WebResource webResource =  Client.create().resource(
        		roUrl
        		);

        ClientResponse response = webResource.path("resource")
                .path("ro")
                .path(
                        URLEncoder.encode(
                               roId
                        )
                )
                .get(ClientResponse.class);

        String id = UUID.randomUUID().toString();
        String orePath = id+"_oaiore.xml";
        
        IOUtils.copy(response.getEntityInputStream(),new FileOutputStream(orePath));
        
        System.out.println(orePath);
        
        String temp = new java.io.File(orePath).getAbsolutePath();
        String dir = temp.substring(0, temp.lastIndexOf('/'));
        //Convert ORE to SIP
        
        String sipPath = sipConversion(orePath, dir, id);
        System.out.println(sipPath);
        ResearchObject sip;
        StringWriter tempWriter = new StringWriter();

		try {
			sip = new SeadXstreamStaxModelBuilder().buildSip(new FileInputStream(sipPath));
	        new BagUploadServlet().siptoJsonConverter().toXML(new BagUploadServlet().toQueryResult(sip),  tempWriter);
	        tempWriter.append(";"+ sipPath);
		} catch (InvalidXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        return tempWriter.toString();
       
    }

	@Override
	public boolean trackEvent(String agentId, String entityId, String roUrl) {

        Event event = new Event();
        String randomId = UUID.randomUUID().toString();
        event.setEventIdentifier("event:"+randomId);//TODO generate eventId from database
        event.setEventDateTime(new Date());
        System.out.println(agentId);
        event.setLinkingAgentIdentifier(
        		agentId
        		);
        event.setWorkflowId("curation-edit:"+randomId);
        event.setTargetId(entityId);
        event.setEventType("Curation-Edit");
        
        String eventJson = new GsonBuilder().create().toJson(event);
        System.out.println(eventJson);

        WebResource  webResource = Client.create().resource(
        		roUrl
        		);
        ClientResponse response = webResource.path("resource")
                .path("putEvent")
                .queryParam("event", eventJson)
                .post(ClientResponse.class);
        System.out.println(response.getStatus());
        if(response.getStatus()==200||response.getStatus()==202)
        	return true;
        else
        	return false;
	}
	
	@Override
	public boolean makeObsolete(String entityId, String roUrl){
		 WebResource  webResource = Client.create().resource( 
				 roUrl
				 );
	        ClientResponse response = webResource.path("resource")
	                .path("obsolete")
	                .path(
	                        URLEncoder.encode(
	                                entityId
	                        		)
	                )        
	                .post(ClientResponse.class);
			return true;
	}

	@Override
	public String getROAffiliation(String entityId, String registryUrl) throws IOException, ClassNotFoundException {
		 
		String affiliation = null;

		org.seadva.registry.database.model.obj.vaRegistry.Collection collection 
				=(org.seadva.registry.database.model.obj.vaRegistry.Collection) new RegistryClient(registryUrl).getEntity(entityId, org.seadva.registry.database.model.obj.vaRegistry.Collection.class.getName());
		for(DataLocation location: collection.getDataLocations()){
			if(location.getId()!=null){
				if(location.getId().getLocationType()!=null){
					affiliation = location.getId().getLocationType().getAffiliation();
					break;
				}
			}
		}
       return affiliation;
	}
	
	@Override
	public boolean assignToAgent(String entityId, String agentId, String registryUrl) throws IOException, ClassNotFoundException {
		 Gson gson = new GsonBuilder()
	        .excludeFieldsWithoutExposeAnnotation()
	        .create();

		Relation relation = new Relation();
		
		RelationPK id = new RelationPK();
		id.setCause(new RegistryClient(registryUrl).getEntity(entityId, org.seadva.registry.database.model.obj.vaRegistry.Collection.class.getName()));
		id.setEffect(new RegistryClient(registryUrl).getEntity(agentId, org.seadva.registry.database.model.obj.vaRegistry.Agent.class.getName()));
		id.setRelationType(
//				new RegistryClient(registryUrl).getRelationByType("curatedBy") //This query causes issues - todo
				gson.fromJson( "{\"id\":\"rl:3\",\"relationElement\":\"curatedBy\",\"relationSchema\":\"http://purl.org/pav/\"}",RelationType.class)
				);
				
		relation.setId(id);	
		List<Relation> relationList = new ArrayList<Relation>();
		relationList.add(relation);		
		new RegistryClient(registryUrl).postRelation(relationList); 
        return true;
	}
	
	private String oreConversion(String sipFilePath) {

        java.io.File targetDir = new java.io.File(System.getProperty("java.io.tmpdir"));

        PackageDescriptor packageDescriptor = new PackageDescriptor(null, null, targetDir.getAbsolutePath());
        packageDescriptor.setSipPath(sipFilePath);
        try
        {
            new ConfigBootstrap().load();
            packageDescriptor = ConfigBootstrap.packageListener.execute(org.seadva.bagit.event.api.Event.PARSE_SIP, packageDescriptor);
            packageDescriptor = ConfigBootstrap.packageListener.execute(org.seadva.bagit.event.api.Event.GENERATE_ORE, packageDescriptor);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        return packageDescriptor.getOreFilePath();
    }
	
	private String sipConversion(String orePath, String dir, String rootId) {

        PackageDescriptor packageDescriptor = new PackageDescriptor(rootId, rootId, dir);
        packageDescriptor.setOreFilePath(orePath);
        packageDescriptor.setPackageId(rootId);
        try
        {
            new ConfigBootstrap().load();
            packageDescriptor = ConfigBootstrap.packageListener.execute(org.seadva.bagit.event.api.Event.GENERATE_SIP, packageDescriptor);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        return packageDescriptor.getSipPath();
    }
}