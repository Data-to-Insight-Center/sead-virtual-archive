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

package org.dataconservancy.dcs.access.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.access.client.api.MediciService;
import org.dataconservancy.dcs.access.client.model.CollectionNode;
import org.dataconservancy.dcs.access.client.model.CollectionNode.SubType;
import org.dataconservancy.dcs.access.client.model.Creator;
import org.dataconservancy.dcs.access.client.model.DatasetRelation;
import org.dataconservancy.dcs.access.client.model.FileNode;
import org.dataconservancy.dcs.access.client.upload.RPCException;
import org.dataconservancy.dcs.access.server.model.ProvenanceDAO;
import org.dataconservancy.dcs.access.server.model.ProvenanceDAOJdbcImpl;
import org.dataconservancy.dcs.access.server.util.ByteArray;
import org.dataconservancy.dcs.access.server.util.ServerConstants;
import org.dataconservancy.dcs.access.server.util.StatusReader.Status;
import org.dataconservancy.dcs.access.shared.CheckPointDetail;
import org.dataconservancy.dcs.access.shared.Constants;
import org.dataconservancy.dcs.access.shared.Event;
import org.dataconservancy.dcs.access.shared.MediciInstance;
import org.dataconservancy.dcs.access.shared.ProvenanceRecord;
import org.dataconservancy.dcs.access.shared.Query;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dspace.foresite.OREException;
import org.sead.acr.common.utilities.json.JSONArray;
import org.sead.acr.common.utilities.json.JSONException;
import org.sead.acr.common.utilities.json.JSONObject;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadPerson;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;



/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class MediciServiceImpl extends RemoteServiceServlet implements
		MediciService {
	Logger logger = Logger.getLogger(this.getClass().toString());
	
	DatasetRelation relations = new DatasetRelation();
	ProvenanceDAO provDao;
	
	MediciUtil util;
	
	private Abdera abdera;

    private DcsModelBuilder seadbuilder;
    
    public void init() throws ServletException {
        this.abdera = new Abdera();
        this.seadbuilder = new SeadXstreamStaxModelBuilder();
        this.util = new MediciUtil();
        String path = getServletContext().getRealPath("/sead_access/");
		
        try {
			this.provDao = new ProvenanceDAOJdbcImpl(path+"/Config.properties");
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}

	@Override
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
	
	public String parseJsonAttribute(String json, String attribute) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(json);
		
			JSONObject binding = jsonObject.getJSONObject("sparql")
					.getJSONObject("results")
					.getJSONObject("result")
					.getJSONObject("binding");
			if(((String)binding.get("name")).equalsIgnoreCase(attribute))
				return (String)binding.get("literal");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	int count =0 ;

	ResearchObject masterSip;
	public void populateKids(String sipPath) {
		relations = new DatasetRelation();
		try {
			masterSip = (ResearchObject)new SeadXstreamStaxModelBuilder().buildSip(new FileInputStream(new File(sipPath)));
			for(DcsDeliverableUnit deliverableUnit:masterSip.getDeliverableUnits()){
				CollectionNode collection = relations.getDuAttrMap().get(deliverableUnit.getId());
				if(collection==null)
					 collection = new CollectionNode();
				String id = deliverableUnit.getId();
				if(id!=null)
					collection.setId(id);
				String abstrct = ((SeadDeliverableUnit)deliverableUnit).getAbstrct();
				if(abstrct!=null)
					collection.setAbstract(abstrct);
				Set<String> sites = ((SeadDeliverableUnit)deliverableUnit).getSites();
				if(sites!=null)
					if(sites.size()>0)
						collection.setSite(sites.iterator().next());
				String date = ((SeadDeliverableUnit)deliverableUnit).getPubdate();
				if(date!=null)
					collection.setDate(date);
				String title = deliverableUnit.getTitle();
				if(title!=null)
					collection.setTitle(title);
				String contact = ((SeadDeliverableUnit)deliverableUnit).getContact();
				if(contact!=null)
					collection.setContact(contact);
				Collection<SeadPerson> creators = ((SeadDeliverableUnit)deliverableUnit).getDataContributors();
				Set<Creator> tempCreators = new HashSet<Creator>(); 
				if(creators!=null)
					for(SeadPerson creator:creators){
						Creator cr = new Creator();
						cr.setCreatorId(creator.getName());
						cr.setCreatorIdType(creator.getId());
						cr.setCreatorName(creator.getIdType());
						tempCreators.add(cr);
					}
				collection.setCreators(tempCreators);
				if(deliverableUnit.getParents()!=null)
					if(deliverableUnit.getParents().size()>0){
						 String parentId = deliverableUnit.getParents().iterator().next().getRef();
						 CollectionNode parent = relations.getDuAttrMap().get(parentId);
						 if(parent==null)
							 parent = new CollectionNode();
						 List<String> siblings = parent.getSub().get(SubType.Collection);
						 if(siblings==null)
							 siblings = new ArrayList<String>();
						 siblings.add(collection.getId());
						 parent.getSub().put(SubType.Collection, siblings);
						 relations.getDuAttrMap().put(parentId, parent);
						 relations.getParentMap().put(collection.getId(), parentId);
					}
				//collection.setSub//Todo retrieve and add du.addSub(SubType.Collection, result);du.addSub(SubType.File, result);
				relations.getDuAttrMap().put(collection.getId(), collection);
			}
			for(DcsFile file: masterSip.getFiles())
			{
				FileNode fileNode = new FileNode();
				fileNode.setId(file.getId());
				fileNode.setTitle(file.getName());
				if(file.getFormats()!=null)
					if(file.getFormats().size()>0)
						fileNode.setFormat(file.getFormats().iterator().next().getFormat());
				relations.getFileAttrMap().put(file.getId(),fileNode);
			}
			
			for(DcsManifestation manifestation:masterSip.getManifestations()){
				String parentId = manifestation.getDeliverableUnit();
				CollectionNode parent = relations.getDuAttrMap().get(parentId);
				if(parent==null)
					 parent = new CollectionNode();
				List<String> siblings = parent.getSub().get(SubType.File);
				if(siblings==null)
					 siblings = new ArrayList<String>();
				for(DcsManifestationFile mfile:manifestation.getManifestationFiles()){
					String fileId = mfile.getRef().getRef();
					siblings.add(fileId);
					relations.getParentMap().put(fileId, parentId);
				}
				parent.getSub().put(SubType.File, siblings);
				relations.getDuAttrMap().put(parentId, parent);
				
			}
		} catch (InvalidXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void writeToFile(String xml, String filePath){
		 try{
			  // Create file 
			  FileWriter fstream = new FileWriter(filePath);
			  BufferedWriter out = new BufferedWriter(fstream);
			  out.write(xml);
			  //Close the output stream
			  out.close();
			  }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
	}
	
	private String readFromFile(String filePath){
		FileInputStream fisTargetFile = null;
		String targetFileStr = null;
		try {
			fisTargetFile = new FileInputStream(new File(filePath));
			try {
				targetFileStr = IOUtils.toString(fisTargetFile, "UTF-8");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(fisTargetFile!=null)
				try {
					fisTargetFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		return targetFileStr;
		
	}
	
	public String getDuTitle(String tagId, String baseUrl, MediciInstance sparqlEndpoint){
		//Http call to VA query servlet
		String url = baseUrl+ "acrcommon"+"?instance="+
				java.net.URLEncoder.encode(sparqlEndpoint.getTitle())
        		+"&"+
        		"query="+
        		java.net.URLEncoder.encode(Query.DU_TITLE.getTitle())+
        		"&"+
        		"tagid="+
        		java.net.URLEncoder.encode(tagId)
        		;
 
		URL obj;
		try {
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			 
			// optional default is GET
			con.setRequestMethod("GET");
	 
			int responseCode = con.getResponseCode();
		
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			return parseJsonAttribute(response.toString(),"title");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	

	public void addMetadata(String fileSrc, String sipFilePath) {
		
		ResearchObject masterSip = null;
		try {
			masterSip = new SeadXstreamStaxModelBuilder().buildSip(new FileInputStream(sipFilePath+".xml"));
		} catch (InvalidXmlException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(fileSrc!=null){
			Collection<DcsDeliverableUnit> dus = masterSip.getDeliverableUnits();
			String parentDu = null;
			String guid = UUID.randomUUID().toString();
			for(DcsDeliverableUnit du: dus){
				if(du.getParents()==null||du.getParents().size()==0){
					parentDu = du.getId();
					DcsMetadataRef ref = new DcsMetadataRef();
					ref.setRef(guid);
					du.addMetadataRef(ref);
				}
			}
			
			masterSip.setDeliverableUnits(dus);
			DcsFile file = new DcsFile();
			file.setId(guid);
			DcsFormat format= new DcsFormat();
			format.setFormat("http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd");
			format.setSchemeUri("http://www.iana.org/assignments/media-types/");
			file.addFormat(format);
			file.setName("FGDC");
			file.setSource(fileSrc);
			file.setExtant(true);
			
			masterSip.addFile(file);

            Collection<DcsManifestation> manifestations = masterSip.getManifestations();
			for(DcsManifestation manifestation: manifestations){
				if(manifestation.getDeliverableUnit().equals(parentDu)){
					DcsManifestationFile manifestationFile = new DcsManifestationFile();
					DcsFileRef ref = new DcsFileRef();
					ref.setRef(guid);
					manifestationFile.setRef(ref);
					manifestation.addManifestationFile(manifestationFile);
					break;
				}
			}
			masterSip.setManifestations(manifestations);
		}
	}
	String rootId = null;
	public void toVAmodel(String id, String parent, MediciInstance sparqlEndpoint, String tmpHome){
	    }

	   public HashMap<String,HashMap<String,String>> submitSIPFile(String endpoint,
			   String sipFilePath,
               String user,
               String pass,
               boolean restrictAccess) throws RPCException {
		   AbderaClient client = null;
			
			try {
			
			client = getClient(endpoint, user, pass);
			
			RequestOptions opts = new RequestOptions();
			opts.setContentType("application/xml");
			opts.setHeader("X-Packaging",
			              "http://dataconservancy.org/schemas/dcp/1.0");
			opts.setHeader("X-Verbose", "true");
			
			
			 InputStream inputStream = new FileInputStream(sipFilePath);
			 ResearchObject sip = (ResearchObject)seadbuilder.buildSip(inputStream);
			 int tmpFileNos = sip.getFiles().size();
			int tmpEntityNos = tmpFileNos+sip.getDeliverableUnits().size()+sip.getManifestations().size();
                Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();
			for(DcsDeliverableUnit du:dus){
				if(du.getParents()==null||du.getParents().size()==0){
					if(restrictAccess)
						du.setRights("restricted");
					((SeadDeliverableUnit)du).setFileNo(fileNos);
				}
			}
			sip.setDeliverableUnits(dus);
			
			
			Collection<DcsManifestation> manifestations = sip.getManifestations();
			for(DcsManifestation manifestation:manifestations){
				if(Constants.duIds.containsKey(manifestation.getDeliverableUnit()))
						manifestation.setDeliverableUnit(Constants.duIds.get(manifestation.getDeliverableUnit()));
			}
			sip.setManifestations(manifestations);
			
			ByteArray buf = new ByteArray(8 * 1024);
			seadbuilder.buildSip(sip, buf.asOutputStream());
		
				
			ClientResponse resp =
			       client.post(endpoint, 
//			    		   buf.asInputStream(),
			    		   new FileInputStream(sipFilePath),
			    		   opts);
			
			int status = resp.getStatus();
			
			StringWriter result = new StringWriter();
			resp.getDocument().writeTo(result);
			
		
			if (status == 200 || status == 201 || status == 202) {
				HashMap<String,HashMap<String,String>> resultMap = new HashMap<String,HashMap<String,String>>();
				HashMap<String,String> subResultMap = new HashMap<String,String>();
				subResultMap.put(String.valueOf(tmpEntityNos),result.toString());
				resultMap.put(String.valueOf(tmpFileNos),subResultMap);
				return resultMap;
			} else {
				 
			   throw new RPCException("Package deposit failed: " + result);
			}
			} catch (IOException e) {
			throw new RPCException(e.getMessage());
			} catch (URISyntaxException e) {
			throw new RPCException(e.getMessage());
			} catch (InvalidXmlException e) {
				throw new RPCException(e.getMessage());
			} finally {
			if (client != null) {
			   client.teardown();
			}
			}
	 }
	  
 
	@Override
	public String submitMultipleSips(String sipEndpoint,
				String datasetId,
				MediciInstance sparqlEndpoint,
			   String sipBasePath,
			   String wfInstanceId,
			   List<String> previousUrls,
			   int startSipNum,
			   int numberOfSips,
               String user,
               String pass,
               boolean restrictAccess, 
               String appBaseUrl,
               String tmpHome){
		
		

		(new DepositServiceImpl()).loadDuIds(previousUrls);

		String submitterId = (String)getThreadLocalRequest().getSession().getAttribute("email");
		String sessionType = (String)getThreadLocalRequest().getSession().getAttribute("sessionType");
		String passwordToken  = (String)getThreadLocalRequest().getSession().getAttribute("password");
		/*if(sessionType.equalsIgnoreCase("database")){
			
		}
		else{
//			passwordToken = "";//token
			submitterId = "seadva@gmail.com";
			passwordToken = new UserServiceImpl().hashPassword("password");
		}*/
		
		int i = 0;
		
		if(datasetId==null){
			String[] arr = sipBasePath.split("_sip")[0].split("/");
			datasetId = "tag:cet.ncsa.uiuc.edu,2008:/bean/Collection/"+arr[arr.length-1];
		}
		String datasetTitle = datasetId;
		
		if(sparqlEndpoint!=null)
			datasetTitle = getDuTitle(datasetId, appBaseUrl, sparqlEndpoint);
		
		SimpleDateFormat ft = 
			      new SimpleDateFormat ("yyyy-MM-dd");

		Date latestDate;
		try {
			latestDate = ft.parse( "1800-01-01");
			String statusUrl = "";
			int totalNumOfFiles = 0;
			int totalNumOfEntities = 0;
			boolean newSip = true;
			finishedFiles = 0;
		
			while(true){
				//submit sip file
				if(newSip){
				
					HashMap<String,HashMap<String,String>> resultMap
					= submitSIPFile(sipEndpoint,
								sipBasePath + "_" + startSipNum + ".xml",
								"seadva@gmail.com",
								new UserServiceImpl().hashPassword("password"), 
								false//restrict access
								);
					totalNumOfFiles = Integer.parseInt(resultMap.entrySet().iterator().next().getKey());	
					Map.Entry<String,String> subResult = resultMap.entrySet().iterator().next().getValue().entrySet().iterator().next();
					totalNumOfEntities  = Integer.parseInt(subResult.getKey());
					String result = subResult.getValue();
					int index = result.indexOf("href");
			        statusUrl =result.substring(index+6);
			        statusUrl = statusUrl.substring(0,statusUrl.indexOf("\""));
			        statusUrl = statusUrl.replace("status", "content");
			        newSip = false;
				}
				//sleep
				Thread.sleep(5*1000);//check every 30 seconds
				
				//check status -> update latest date
				//also inherently updates provenance record
				Stats stats = checkStatus(statusUrl,submitterId,datasetTitle,
						wfInstanceId,
						latestDate, 
						sipBasePath, 
						totalNumOfEntities, 
						totalNumOfFiles);
				latestDate = stats.latestDate;
				//if ingest.complete reached then
				if(stats.status==Status.Completed||stats.status==Status.Failed){
					
					//create checkpoint
					createCheckPoint(sipBasePath+"_"+(startSipNum)+".xml", statusUrl,numberOfSips, tmpHome);
					
					//Get things ready for next SIP ingest if this is not the final SIP
					startSipNum++;
					if(startSipNum>numberOfSips)
						break;
					List<String> tempList = new ArrayList<String>();
					tempList.add(statusUrl);
					(new DepositServiceImpl()).loadDuIds(tempList);
					newSip = true;
				}
			}
			
			String message = "Ingest workflow for dataset "+ datasetTitle + " finished running successfully.";
			return message;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Ingest workflow failed";
	}
		
			                      
	int finishedFiles = 0;								       
	private Stats checkStatus(String statusUrl, String submitterId, String datasetTitle, String wfInstanceId, Date latestDate, String sipBasePath, int totalNumOfEntities, int totalNumOfFiles){
		
		Map<Date, List<Event>> events = (new DepositServiceImpl()).statusUpdate(statusUrl, latestDate);
		
		List<Date> sortedDates = new ArrayList<Date>();
		Map<Date,List<Event>> tempEvents = new HashMap<Date,List<Event>>(events);
		Iterator iterator = tempEvents.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<Date, List<Event>> pair = (Map.Entry<Date, List<Event>>)iterator.next();
			sortedDates.add(pair.getKey());
			iterator.remove();
		}
		Stats stats = new Stats();
		
		Collections.sort(sortedDates);
		String previousEvent = null;
		int sortedDatesCount = 0;
		for(Date date:sortedDates){
			sortedDatesCount++;
			if(date.after(latestDate)){
					//get events from that date and fire events accordingly
					int tmpEventsCount = 0;
					List<Event> currentEvents = events.get(date);
					Collections.sort(currentEvents);
						for(final Event event:currentEvents){
							tmpEventsCount++;
							if(event.getEventType().equalsIgnoreCase(Events.FIXITY_DIGEST))//why are we skipping fixity digest?
								continue;
							//if complete, do something
								
								if(event.getEventType().equalsIgnoreCase("ingest.complete")){
									stats.latestDate = latestDate;
									stats.status = Status.Completed;
									
									ProvenanceRecord provRecord = new ProvenanceRecord();
									String[] arr = sipBasePath.split("_sip")[0].split("/");
									String datasetId = arr[arr.length-1];
									provRecord.setDatasetId(datasetId);
									provRecord.setWfInstanceId(wfInstanceId);
									provRecord.setDatasetTitle(datasetTitle);
									provRecord.setSipId(statusUrl);
									provRecord.setDate(latestDate);
									provRecord.setSubmitterId(submitterId);
									provRecord.setStatus(org.dataconservancy.dcs.access.shared.Status.fromString(stats.status.getText()));
									Event provEvent = new Event();
									provEvent.setEventPercent(100);
									provEvent.setEventType(event.getEventType());
									provEvent.setEventDate(latestDate);
									provRecord.addEvent(provEvent);
									
									provDao.insertProvenanceRecord(provRecord);
									
									return stats;
									//possibly re-submit?													
									
							}//if failed, do something
							if(event.getEventType().contains("fail")){
								
								stats.latestDate = latestDate;
								stats.status = Status.Failed;
								ProvenanceRecord provRecord = new ProvenanceRecord();
								String[] arr = sipBasePath.split("_sip")[0].split("/");
								String datasetId = arr[arr.length-1];
								provRecord.setDatasetId(datasetId);
								provRecord.setDatasetTitle(datasetTitle);
								provRecord.setSipId(statusUrl);
								provRecord.setWfInstanceId(wfInstanceId);
								provRecord.setDate(latestDate);
								provRecord.setSubmitterId(submitterId);
								provRecord.setStatus(org.dataconservancy.dcs.access.shared.Status.fromString(stats.status.getText()));
								Event provEvent = new Event();
								provEvent.setEventPercent(100);
								provEvent.setEventType(event.getEventType());
								provEvent.setEventDate(latestDate);
								provRecord.addEvent(provEvent);
			
								provDao.insertProvenanceRecord(provRecord);
								return stats;
							}
							else{//else just insert new event values in the database possibly
									
								
								stats.status = Status.Pending;
								
								if(previousEvent!=null&&previousEvent.equals(event.getEventType())&&Constants.multiEventMessages.contains(previousEvent))
								{//Case #1 - continuation of an event
									finishedFiles++;
									int number = 0;
									   if(Constants.multiEventCheck.get(event.getEventType())==1)
										   number = totalNumOfFiles;
									   else
										   number = totalNumOfEntities; 
									   
									   //In the case below, we handle completion of a multi-event (finishedFiles==number) or completion of the list of events in which case the last event could have completed upto, say 60% - both inner and outer loop (sortedDatesCount==sortedDates.size()&&tmpEventsCount==tmpEvents.size())
									if((sortedDatesCount==sortedDates.size()&&tmpEventsCount==currentEvents.size())||finishedFiles==number)//case #4 - 100% completion of a continued task 
									{	
										event.setEventPercent(finishedFiles/number*100);
										
										ProvenanceRecord provRecord = new ProvenanceRecord();
										String[] arr = sipBasePath.split("_sip")[0].split("/");
										String datasetId = arr[arr.length-1];
										provRecord.setDatasetId(datasetId);
										provRecord.setDatasetTitle(datasetTitle);
										provRecord.setSipId(statusUrl);
										provRecord.setWfInstanceId(wfInstanceId);
										provRecord.setDate(latestDate);
										provRecord.setSubmitterId(submitterId);
										provRecord.setStatus(org.dataconservancy.dcs.access.shared.Status.fromString(stats.status.getText()));//This is the status of the whole SIP and not for the particular task. For the particular task, status is indicated by percent
										Event provEvent = new Event();
										provEvent.setEventPercent(finishedFiles/number*100);
										provEvent.setEventType(event.getEventType());
										provEvent.setEventDate(latestDate);
										provRecord.addEvent(provEvent);
										provDao.insertProvenanceRecord(provRecord);
										if(finishedFiles==number)
											finishedFiles = 0;
									}
									previousEvent = event.getEventType();
								}
								else{
									if(previousEvent==null){//case #2-First event
									finishedFiles++;
										
										
										if(!Constants.multiEventMessages.contains(event.getEventType())) //case #2.a - Done only once
										{
											event.setEventPercent(
													//finishedFiles*100/1
													-10
													);
											ProvenanceRecord provRecord = new ProvenanceRecord();
											String[] arr = sipBasePath.split("_sip")[0].split("/");
											String datasetId = arr[arr.length-1];
											provRecord.setDatasetId(datasetId);
											provRecord.setDatasetTitle(datasetTitle);
											provRecord.setSipId(statusUrl);
											provRecord.setWfInstanceId(wfInstanceId);
											provRecord.setDate(latestDate);
											provRecord.setSubmitterId(submitterId);
											provRecord.setStatus(org.dataconservancy.dcs.access.shared.Status.fromString(stats.status.getText()));
											Event provEvent = new Event();
											provEvent.setEventType(event.getEventType());
											provEvent.setEventPercent(100);
											provEvent.setEventDate(latestDate);
											provRecord.addEvent(provEvent);
											provDao.insertProvenanceRecord(provRecord);
											finishedFiles = 0;
										}
										else{
										//Multi-event message first event
										//Do nothing
											//till the event changes or till end of this event in the list - it might be done only 60% during this status check
										}
										
										
										previousEvent = event.getEventType();//else case #1
									}
									else if(!previousEvent.equals(event.getEventType())){//Case #3 - New event
										finishedFiles++;
										if(!Constants.multiEventMessages.contains(event.getEventType())) //case #3.a - Done only once
										{
											event.setEventPercent(
													//finishedFiles*100/1
													-10
													);
											ProvenanceRecord provRecord = new ProvenanceRecord();
											String[] arr = sipBasePath.split("_sip")[0].split("/");
											String datasetId = arr[arr.length-1];
											provRecord.setDatasetId(datasetId);
											provRecord.setDatasetTitle(datasetTitle);
											provRecord.setSipId(statusUrl);
											provRecord.setWfInstanceId(wfInstanceId);
											provRecord.setDate(latestDate);
											provRecord.setSubmitterId(submitterId);
											provRecord.setStatus(org.dataconservancy.dcs.access.shared.Status.fromString(stats.status.getText()));
											Event provEvent = new Event();
											provEvent.setEventPercent(100);
											provEvent.setEventType(event.getEventType());
											provEvent.setEventDate(latestDate);
											provRecord.addEvent(provEvent);
											provDao.insertProvenanceRecord(provRecord);
											finishedFiles = 0;
										}
										else{
											//Multi-event message first event
											//Do nothing
												//till the event changes or till end of this event in the list - it might be done only 60% during this status check
											}
										previousEvent = event.getEventType();
										
									}
								}

							}
								//with 2 second gaps at the minimum
							latestDate = date;
						}
								
								
									
					}
						
							//Issues:
							//Some ACR instance with ip 71.x.x.x (check) keeps querying without escaping - send email about it
			}
		stats.latestDate = latestDate;
		return stats;
	}
	
    private AbderaClient getClient(String endpoint, String user, String pass)
            throws MalformedURLException, URISyntaxException {
    	
    	AbderaClient   client = new AbderaClient(abdera);

        AbderaClient.registerTrustManager(); // needed for SSL
        AbderaClient.registerScheme(AuthPolicy.BASIC, BasicScheme.class);
        client.setAuthenticationSchemePriority(AuthPolicy.BASIC);
        client.usePreemptiveAuthentication(false);
        client.addCredentials(endpoint,
                              null,
                              "basic",
                              new UsernamePasswordCredentials(user, pass));
        return client;
        
    }
	@Override
	public DatasetRelation getRelations() {
		return relations;
	}
	
	@Override
	public int getFileNos() {
		return relations.getFileAttrMap().size();
	}


	@Override
	public String getBag(String tagId, MediciInstance sparqlEndpoint, String bagitEp, String tmpHome) {
		
		String splitChar = "/";
		if(!tagId.contains("/"))
			 splitChar = ":";
		String guid = tagId.split(splitChar)[tagId.split(splitChar).length-1];
		
		Client client = Client.create();
		com.sun.jersey.api.client.WebResource webResource = client
				   .resource(bagitEp + "bag/");

	     MultivaluedMap<String, String> params = new MultivaluedMapImpl();
	     params.add("sparqlEpEnum", String.valueOf(sparqlEndpoint.getId()));
	
	     com.sun.jersey.api.client.ClientResponse response = webResource
	                .path(
	                        URLEncoder.encode(
	                                tagId
	                        )
	                )
	                .queryParams(params)
	                .accept("application/zip")
	                .get(com.sun.jersey.api.client.ClientResponse.class);
	 
        StringWriter writer = new StringWriter();
        try {
			IOUtils.copy(response.getEntityInputStream(),new FileOutputStream(tmpHome+guid+".zip"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return tmpHome+guid+".zip";
	}


	@Override
	public String getSipFromBag(String bagPath, String sipPath, String bagitEp) {
		
		 
		Client client = Client.create();
		com.sun.jersey.api.client.WebResource webResource = client
				   .resource(bagitEp + "sip/");

        File file = new File(bagPath);
        FileDataBodyPart fdp = new FileDataBodyPart("file", file,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

        formDataMultiPart.bodyPart(fdp);

        com.sun.jersey.api.client.ClientResponse response = webResource
        		.type(MediaType.MULTIPART_FORM_DATA)
                .post(com.sun.jersey.api.client.ClientResponse.class, formDataMultiPart);
        
        	try {
				IOUtils.copy(response.getEntityInputStream(),new FileOutputStream(sipPath));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	populateKids(sipPath);
			fileNos = masterSip.getFiles().size();
       
        return sipPath;
	}

		
	ResearchObject sipNew = new ResearchObject();
	
	@Override
	public int splitSip(String sipFilePath) {
		
			ResearchObject masterSip = null;
			try {
				masterSip = new SeadXstreamStaxModelBuilder().buildSip(new FileInputStream(sipFilePath+".xml"));
			} catch (InvalidXmlException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        int i=0;

            Collection<DcsFile> files = masterSip.getFiles();
	        fileNos = files.size();
	        if(fileNos>Constants.MAX)
	        {
		        if(i==0){  //#1 Make a SIP out of all DUs
		            sipNew.setDeliverableUnits(masterSip.getDeliverableUnits());
		            String sipFileName = sipFilePath+"_" + i + ".xml";
		            File sipFile = new File(sipFileName);
		           
		            OutputStream out;
					try {
						out = FileUtils.openOutputStream(sipFile);
						new DcsXstreamStaxModelBuilder().buildSip(sipNew, out);
			            out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	
		            sipNew = new ResearchObject();
		            i++;
		        }
	
		        int fileCount = 0;
		    //    int totalMans =0 ;
	
		        Map<String,DcsFile> idFileMap = new HashMap<String, DcsFile>();
		        for(DcsFile file:masterSip.getFiles()){
		            idFileMap.put(file.getId(),file);
		        }
	
		        for(DcsManifestation manifestation:masterSip.getManifestations()){
		        	//If a manifestation contains more than the allowed maximum, we make an exception and allow all the files in the same package for the sake of simplicity
		            if(manifestation.getManifestationFiles().size()>Constants.MAX){
		            	ResearchObject sipTemp = new ResearchObject();
		                sipTemp.addManifestation(manifestation);
		                
		                for(DcsManifestationFile mFile: manifestation.getManifestationFiles())
		                    sipTemp.addFile(idFileMap.get(mFile.getRef().getRef()));
		                String sipFileName = sipFilePath+"_" + i + ".xml";
		                File sipFile = new File(sipFileName);
	
		                OutputStream out;
						try {
							out = FileUtils.openOutputStream(sipFile);
						    new SeadXstreamStaxModelBuilder().buildSip(sipTemp, out);
			                out.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		                i++;
		            }
		            else{ //Otherwise we limit the number of files in a package to less than/equal to the MAX
	
		                if(fileCount+manifestation.getManifestationFiles().size()>Constants.MAX)
		                {
		                    String sipFileName = sipFilePath+"_" + i + ".xml";
		                    File sipFile = new File(sipFileName);
	
		                    OutputStream out;
							try {
								out = FileUtils.openOutputStream(sipFile);
								new DcsXstreamStaxModelBuilder().buildSip(sipNew, out);
			                    out.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							sipNew = new ResearchObject();
		                    fileCount = 0;
		                    i++;
		                }
		                sipNew.addManifestation(manifestation);
		                for(DcsManifestationFile mFile: manifestation.getManifestationFiles())
		                    sipNew.addFile(idFileMap.get(mFile.getRef().getRef()));
		                fileCount+=manifestation.getManifestationFiles().size();
		            }
		        }
	
		        String sipFileName = sipFilePath+"_" + i + ".xml";
		        File sipFile = new File(sipFileName);
	
		        OutputStream out;
				try {
					out = FileUtils.openOutputStream(sipFile);
					new SeadXstreamStaxModelBuilder().buildSip(sipNew, out);
			        out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	        
	        }
	        else{
	        	 String sipFileName = sipFilePath+"_" + i + ".xml";
			       File sipFile = new File(sipFileName);
		
			        OutputStream out;
					try {
						out = FileUtils.openOutputStream(sipFile);
						new SeadXstreamStaxModelBuilder().buildSip(masterSip, out);
				        out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}	        
	        }
	        return i;
	}

	
	static long fileNos = 0;
	
	boolean  checkpointing =false;
	@Override
	public CheckPointDetail restartIngest(String datasetId, String tmpHome){
		CheckPointDetail checkpointDetail = new CheckPointDetail();
		if(!checkpointing)
		{
			checkpointDetail.setCheckPointed(false);
			return checkpointDetail;
		}
		String splitChar = "/";
		if(!datasetId.contains("/"))
			 splitChar = ":";
		String guid = datasetId.split(splitChar)[datasetId.split(splitChar).length-1];
		String lastSip = util.checkpointedSIP(guid, tmpHome);
		
		if(lastSip==null)
			checkpointDetail.setCheckPointed(false);
		else{
			checkpointDetail.setCheckPointed(true);
			String[] array = lastSip.split("_");
					
			int sipNumber = Integer.parseInt(array[array.length-1].replace(".xml", ""));
			String sipToIngest = lastSip.replaceFirst(sipNumber+".xml", (sipNumber+1)+".xml");
			checkpointDetail.setResumeSipPath(sipToIngest);
			checkpointDetail.setNumSplitSIPs(util.checkpointedSIPCount(guid, tmpHome));
			checkpointDetail.setPreviousStatusUrls(util.getLoadIds());
		}
		return checkpointDetail;
	}

	public void createCheckPoint(String sipFilePath, String previousStatusUrl, int n, String tmpHome) {
		util.createCheckpoint(sipFilePath, previousStatusUrl, n, tmpHome);
	}

	class Stats{
		Status status;
		Date latestDate;
	}

	@Override
	public String generateWfInstanceId() {
		return UUID.randomUUID().toString();
	}
	
	@Override
	public List<MediciInstance> getAcrInstances() {
		return ServerConstants.acrInstances;
	}
}
