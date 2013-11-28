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

package org.dataconservancy.dcs.access.client.model;

import org.dataconservancy.dcs.access.client.Search;

public class SearchInput{
	private Search.UserField[] userfields;
	private String[] userqueries;
	private int offset;
	private String[] facetField;
	private String[] facetValue;
	
	public SearchInput(Search.UserField[] userfields,
	    String[] userqueries, final int offset,
	    String[] facetField,
	    String[] facetValue){
		this.setUserfields(userfields);
	    this.setUserqueries(userqueries);
		this.setOffset(offset);
	    this.setFacetField(facetField);
	    this.setFacetValue(facetValue);
	}

	public Search.UserField[] getUserfields() {
		return userfields;
	}

	public void setUserfields(Search.UserField[] userfields) {
		this.userfields = userfields;
	}

	public String[] getUserqueries() {
		return userqueries;
	}

	public void setUserqueries(String[] userqueries) {
		this.userqueries = userqueries;
	}

	public String[] getFacetField() {
		return facetField;
	}

	public void setFacetField(String[] facetField) {
		this.facetField = facetField;
	}

	
	public String[] getFacetValue() {
		return facetValue;
	}

	public void setFacetValue(String[] facetValue) {
		this.facetValue = facetValue;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
}
