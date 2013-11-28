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

package org.dataconservancy.dcs.access.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.ingest.Events;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Constants 
		implements IsSerializable {

	public Map<String,String> facets =  new HashMap<String,String>();
	public static Map<String,Integer> order =  new HashMap<String,Integer>();
	public static Map<Integer,String> displayOrder =  new HashMap<Integer,String>();
	public Map<String,String> facetValues =  new HashMap<String,String>();
	public static Map<String,String> gqmNames = new HashMap<String,String>();
	
	public static final int MAX_SEARCH_RESULTS = 20;
	public static final int MAX = 2000;
	public static final int XMCCAT_MAX_SEARCH_RESULTS = 15;
	public static Map<String,String> duIds = new HashMap<String,String>(); 
	public static Map<String,String> eventMessages = new HashMap<String,String>();
	public static List<String> multiEventMessages = new ArrayList<String>();
	public static Map<String,Integer> multiEventCheck = new HashMap<String,Integer>(); 

	public Constants()
	{
		facets.put("entityType","object type");
		facets.put("author","data contributor");
		facets.put("format", "format/standard");
		facets.put("subject", "keyword");
		facets.put("location", "location");
		facets.put("primaryDataLocationName","institutional repository");
		facetValues.put("Collection","DeliverableUnit");
		
		
		order.put("object type",1);
		order.put("data contributor",5);
		order.put("format/standard", 2);
		order.put("keyword", 4);
		order.put("location", 0);
		order.put("institutional repository", 3);
		
		displayOrder.put(1, "object type");
		displayOrder.put(5, "data contributor");
		displayOrder.put(2, "format/standard");
		displayOrder.put(0, "location");
		displayOrder.put(4, "keyword");
		displayOrder.put(3, "institutional repository");
		
		eventMessages.put(Events.ARCHIVE, "Archival in Repository");
		eventMessages.put(org.seadva.ingest.Events.COLD_COPY, "Archival a cold copy of the collection");
		eventMessages.put(org.seadva.ingest.Events.MATCH_MAKING, "Match-making of repositories");
		eventMessages.put(Events.BATCH, "Performed Batch Ingest");
		eventMessages.put(Events.INGEST_START, "Ingest  Process Initiation");
		eventMessages.put(Events.CHARACTERIZATION_FORMAT, "File Chracterization");
		eventMessages.put(Events.CHARACTERIZATION_METADATA, "Metadata File Chracterization");
		eventMessages.put(Events.DEPOSIT, "Deposit  Process Initiation");
		eventMessages.put(Events.FILE_DOWNLOAD, "File Staging");
		eventMessages.put(Events.FILE_RESOLUTION_STAGED, "File Resolving");
		eventMessages.put(Events.FILE_DOWNLOAD, "File Download");
		eventMessages.put(Events.FILE_UPLOAD, "Started uploading Files");
		eventMessages.put(Events.FIXITY_DIGEST, "File Fixity Calculation");
		eventMessages.put(Events.ID_ASSIGNMENT, "Internal Identifier Assignment");
		eventMessages.put(org.seadva.ingest.Events.DOI_ID_ASSIGNMENT, "Assigned DOI");
		eventMessages.put(org.seadva.ingest.Events.DOI_ID_UPDATION, "Updated DOI target");
		eventMessages.put(Events.INGEST_START, "Started Ingest  Process");
		eventMessages.put(Events.INGEST_SUCCESS, "SEAD VA Ingest Completed Successfully");
		eventMessages.put(Events.INGEST_FAIL, "Ingest Failed");
		eventMessages.put(Events.TRANSFORM, "Finished Indexing Metadata");
		eventMessages.put(Events.TRANSFORM_FAIL, "Indexing Metadata Failed");
		eventMessages.put(Events.VIRUS_SCAN, "Performed Virus Scan on files");
		eventMessages.put(org.seadva.ingest.Events.METADATA_GENERATION, "Finished automatically generating metadata");
		
		multiEventMessages.add(Events.CHARACTERIZATION_FORMAT);
		multiEventMessages.add(Events.FILE_DOWNLOAD);
		multiEventMessages.add(Events.FILE_RESOLUTION_STAGED);
		multiEventMessages.add(Events.FILE_DOWNLOAD);
		multiEventMessages.add(Events.FILE_UPLOAD);
		multiEventMessages.add(Events.FIXITY_DIGEST);
		multiEventMessages.add(Events.ID_ASSIGNMENT);
		
		multiEventCheck.put(Events.CHARACTERIZATION_FORMAT,1);//1 implies files
		multiEventCheck.put(Events.FILE_DOWNLOAD,1);
		multiEventCheck.put(Events.FILE_RESOLUTION_STAGED,1);
		multiEventCheck.put(Events.FILE_DOWNLOAD,1);
		multiEventCheck.put(Events.FILE_UPLOAD,1);
		multiEventCheck.put(Events.FIXITY_DIGEST,1);
		multiEventCheck.put(Events.ID_ASSIGNMENT,2);//2 implies collections
		
	}
}
