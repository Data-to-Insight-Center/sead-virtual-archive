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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dataconservancy.dcs.access.client.api.TransformerServiceAsync;
import org.dataconservancy.dcs.access.client.api.UserService;
import org.dataconservancy.dcs.access.client.api.UserServiceAsync;
import org.dataconservancy.dcs.access.server.model.ProvenanceDAOJdbcImpl;
import org.dataconservancy.dcs.access.server.util.ServerConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

public class Event 
	implements Comparable<Event>, IsSerializable {
	String eventType;
	String eventDetail;
	Date eventDate;
	int eventPercent;
	int id;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public String getEventDetail() {
		return eventDetail;
	}
	public void setEventDetail(String eventDetail) {
		this.eventDetail = eventDetail;
	}
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	
	public int getEventPercent() {
		return eventPercent;
	}
	public void setEventPercent(int eventPercent) {
		this.eventPercent = eventPercent;
	}
	
	@Override
	public int compareTo(Event anotherEvent) {
		if (!(anotherEvent instanceof Event))
		      throw new ClassCastException("An event object expected.");
		    String otherEventType = ((Event) anotherEvent).getEventType();
		    return this.eventType.compareTo(otherEventType);
	}
	
}
