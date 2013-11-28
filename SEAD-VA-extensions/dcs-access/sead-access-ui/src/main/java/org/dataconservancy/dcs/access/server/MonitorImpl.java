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
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dataconservancy.dcs.access.server.model.ProvenanceDAOJdbcImpl;
import org.dataconservancy.dcs.access.shared.ProvenaceDataset;
import org.dataconservancy.dcs.access.server.util.ServerConstants;



/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class MonitorImpl extends HttpServlet {
	Logger logger = Logger.getLogger(this.getClass().toString());

	 @Override
	  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	      throws ServletException, IOException {
		 	String submitterId = req.getParameter("email");
			String wfInstanceId = req.getParameter("wfInstanceId");
			String latestDateStr = req.getParameter("latestTime");
			PrintWriter out = resp.getWriter();
			String jsonString;
			try {
				String path = getServletContext().getRealPath("/sead_access/");
				ProvenanceDAOJdbcImpl provjdbc = new ProvenanceDAOJdbcImpl(path+"/Config.properties");
				List<ProvenaceDataset> datasets;
				if(wfInstanceId==null)
					datasets = provjdbc.getProvenanceForSubmitter(submitterId);
				else
					datasets = provjdbc.getProvForSubmitterWf(submitterId, wfInstanceId, ServerConstants.dateFormat.parse(latestDateStr));
				jsonString = "[";//datasets.toString();
				int i = 0;
				for(ProvenaceDataset dataset:datasets){
					if(i<datasets.size()-1)
						jsonString += provjdbc.toString(dataset)+",";
					else
						jsonString += provjdbc.toString(dataset);
					i++;
				}
				jsonString+="]";
				out.write(jsonString);

			} 
			catch (InstantiationException e) {
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
			out.flush();
	}


}