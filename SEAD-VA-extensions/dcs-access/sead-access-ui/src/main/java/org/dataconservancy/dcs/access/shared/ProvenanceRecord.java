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
import java.util.Date;
import java.util.List;

import org.dataconservancy.dcs.access.shared.Status;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProvenanceRecord implements IsSerializable{
	String submitterId;
	String datasetId;
	String sipId;
	String wfInstanceId;
	String datasetTitle;
	Status status;
	Date date;
	List<Event> events;
	
	public String getDatasetTitle() {
		return datasetTitle;
	}
	public void setDatasetTitle(String datasetTitle) {
		this.datasetTitle = datasetTitle;
	}
	public String getWfInstanceId() {
		return wfInstanceId;
	}
	public void setWfInstanceId(String wfInstanceId) {
		this.wfInstanceId = wfInstanceId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public ProvenanceRecord(){
		events = new ArrayList<Event>();
	}
	public List<Event> getEvents() {
		return events;
	}
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	public void addEvent(Event event){
		this.events.add(event);
	}
	public String getSubmitterId() {
		return submitterId;
	}
	public void setSubmitterId(String submitterId) {
		this.submitterId = submitterId;
	}
	public String getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
	public String getSipId() {
		return sipId;
	}
	public void setSipId(String sipId) {
		this.sipId = sipId;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	
}
