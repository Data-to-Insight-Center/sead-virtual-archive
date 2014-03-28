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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dataconservancy.dcs.access.server.util.ServerConstants;
import org.dataconservancy.dcs.access.shared.MediciInstance;
import org.dataconservancy.dcs.access.shared.Query;
import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.MediciProxy;

public class VAQueryServlet extends SparqlQueryServlet {

	MediciInstance instance;
	String query;
	String acrUsername;
        protected void doGet(HttpServletRequest request,
                        HttpServletResponse response) throws
                        ServletException, IOException {

        	String mediciTitle = (String)request.getParameter("instance").replace("+"," ");
        	String queryTitle = (String)request.getParameter("query").replace("+"," ");
        	String tagId = (String)request.getParameter("tagid");
        	
        	for(MediciInstance t_instance:ServerConstants.acrInstances)
        		if(t_instance.getTitle().equalsIgnoreCase(mediciTitle))
        			instance = t_instance;
        	query = Query.fromTitle(queryTitle).getQuery();
        	
        	if(tagId!=null)
        		query = query.replace("tagId", tagId);
        	
            super.doGet(request, response);
        }

		@Override
		protected String getQuery(String tagID) {
			return query;
		}
		@Override
		protected MediciProxy getProxy(){
			MediciProxy _mp = new MediciProxy();
			_mp. setCredentials(instance.getUser(), instance.getPassword(), 
	            		 instance.getUrl(),
	            		 instance.getRemoteAPI());
	    	return _mp;
		}
		
		
}