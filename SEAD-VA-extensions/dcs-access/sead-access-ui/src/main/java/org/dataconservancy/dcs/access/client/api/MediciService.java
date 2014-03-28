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

package org.dataconservancy.dcs.access.client.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.access.client.model.DatasetRelation;
import org.dataconservancy.dcs.access.shared.CheckPointDetail;
import org.dataconservancy.dcs.access.shared.MediciInstance;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("sparql")
public interface MediciService extends RemoteService {
	
	String getSipFromBag(String bagPath, String sipPath, String bagitEp);
	public DatasetRelation getRelations();
	public String generateWfInstanceId();
	void toVAmodel(String id, String parent, MediciInstance sparqlEp,
			String tmpHome);
	
	int splitSip(String sipFilePath);
	String submitMultipleSips(String sipEndpoint, String datasetId,
			MediciInstance sparqlInstance, String sipBasePath,
			String wfInstanceId, List<String> perviousUrls, int startSipNum,
			int numberOfSips, String username, String pass,
			boolean restrictAccess, String baseUrl, String tmpHome);
	int getFileNos();

	String getBag(String tagId, MediciInstance sparqlEndpoint, String bagitEp, String tmpHome);
	void addMetadata(String fileSrc, String sipFilePath);
	CheckPointDetail restartIngest(String datasetId, String tmpHome);
	
	Map<String, String> parseJson(String json);
	List<MediciInstance> getAcrInstances();
}
