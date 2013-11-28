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

public enum Query {
	PROPOSED_FOR_PUBLICATION("proposedpublications","SELECT ?id ?name WHERE { "+
					"?s <http://sead-data.net/terms/ProposedForPublication> ?x ."+
					"?s <http://purl.org/dc/elements/1.1/identifier> ?id ."+
					"?s <http://purl.org/dc/elements/1.1/title> ?name ." +
					" }"),
	DU_TITLE("dutitle",
			   "SELECT ?title WHERE { "+
						"<tagId> <http://purl.org/dc/elements/1.1/title> ?title . "+
						" }"),
    DuCreator("ducreator","SELECT ?creator WHERE { "+
            "<tagId> <http://purl.org/dc/terms/creator> ?creator . " +
            " }"),
    DuAbstract("duabstract","SELECT ?abstract WHERE { "+
            "<tagId> <http://purl.org/dc/terms/abstract> ?abstract . "+
            " }"),

    DuSub("dusub","SELECT ?sub WHERE { "+
            "<tagId> <http://purl.org/dc/terms/hasPart> ?sub ."+
            " }"),

    File("file","SELECT ?name WHERE { "+
            "<tagId> <http://purl.org/dc/elements/1.1/title> ?name ."+
            " }"),

    FileFormat("fileformat","SELECT ?format WHERE { "+
            "<tagId> <http://purl.org/dc/elements/1.1/format> ?format ."+
            " }"),
    Date ("date","SELECT ?date WHERE { "+
            "<tagId> <http://purl.org/dc/terms/issued> ?date . " +
            " }"),
   Contact ("contact","SELECT ?contact WHERE { "+
            "<tagId> <http://sead-data.net/terms/contact> ?contact . " +
            " }"),
    Site ("site","SELECT ?site WHERE { "+
            "<tagId> <http://sead-data.net/terms/generatedAt> ?site . " +
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