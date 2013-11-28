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

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;


public enum Query implements IsSerializable, Serializable{
	   PROPOSED_FOR_PUBLICATION("proposedpublications","SELECT ?id ?name WHERE { "+
					"?s <http://sead-data.net/terms/ProposedForPublication> ?x ."+
					"?s <http://purl.org/dc/elements/1.1/identifier> ?id ."+
					"?s <http://purl.org/dc/elements/1.1/title> ?name ." +
					" }"),
	   DU_TITLE("dutitle",
			   "SELECT ?title WHERE { "+
						"<tagId> <http://purl.org/dc/elements/1.1/title> ?title . "+
						" }");
	    
	   private final String title;
	   private final String query;

	   Query(String title, String query) {
	        this.title = title;
	        this.query = query;
	    }
	    
	    public static Query fromTitle(String title) {
	        if (title != null) {
	          for (Query b : Query.values()) {
	            if (title.equalsIgnoreCase(b.title)) {
	              return b;
	            }
	          }
	        }
	        return null;
	      }
	    public String getQuery() {
	    	return this.query;
	      }

	    public String getTitle() {
	    	return this.title;
	     }
}