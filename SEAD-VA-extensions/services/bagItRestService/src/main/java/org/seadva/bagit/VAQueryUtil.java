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

package org.seadva.bagit;

import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.json.JSONException;

import java.io.IOException;

public class VAQueryUtil{

	String query;
       public String getJsonResponse(MediciInstance t_instance,String queryTitle, String tagId) throws IOException, JSONException {


        	query = Query.fromTitle(queryTitle).getQuery();
        	
        	if(tagId!=null)
        		query = query.replace("tagId", tagId);
        	
           return getProxy(t_instance).getSparqlJSONResponse(query);
        }

		protected MediciProxy getProxy(MediciInstance t_instance){
			MediciProxy _mp = new MediciProxy();
			_mp. setCredentials(t_instance.getUser(), t_instance.getPassword(),
                    t_instance.getUrl(),
                    t_instance.getRemoteAPI());
	    	return _mp;
		}
		
		
}