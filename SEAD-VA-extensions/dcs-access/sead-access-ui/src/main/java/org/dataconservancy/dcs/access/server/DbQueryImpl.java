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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dataconservancy.dcs.access.client.api.DbQueryService;
import org.dataconservancy.dcs.access.server.model.ProvenanceDAOJdbcImpl;
import org.dataconservancy.dcs.access.server.util.ServerConstants;
import org.dataconservancy.dcs.access.shared.ProvenaceDataset;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public final class DbQueryImpl extends RemoteServiceServlet
implements DbQueryService{
	@Override
	public List<ProvenaceDataset> getProv(String submitterId, String wfInstanceId, String  latestDateStr) {
		String path = getServletContext().getRealPath("/sead_access/");
		List<ProvenaceDataset> datasets = new ArrayList<ProvenaceDataset>();
		try {
			datasets = new ProvenanceDAOJdbcImpl(path+"/Config.properties")
			.getProvForSubmitterWf(submitterId, wfInstanceId, ServerConstants.dateFormat.parse(latestDateStr));
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return datasets;
	}
	
	public String getDate(Date date){
		return ServerConstants.dateFormat.format(date);
	}
	
}