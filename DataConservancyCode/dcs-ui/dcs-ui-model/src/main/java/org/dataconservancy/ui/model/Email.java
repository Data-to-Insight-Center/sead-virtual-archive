/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.ui.model;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code Email} is an implementation of Notification that uses emails to notify.
 */
public class Email implements Notification{

	private String fromAddress;
	private String [] toAddress;
	private String subject;
	private String body;
	private List<EmailAttachment> attachments;
	
	public Email()
	{
		attachments = new ArrayList<EmailAttachment>(1);
	}

	public String getSubject() {
		return subject;
	}
	
	@Override
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String[] getToAddress() {
		return toAddress;
	}
	
	public String getFromAddress() {
		return fromAddress;
	}

	public String getBody() {
		return body;
	}	
	
	public List<EmailAttachment> attachments() {
	    return attachments;
	}

    @Override
    public void setNotificationMessage(String message) {
        this.body = message;
    }
    
    @Override
    public void setRecipient(String[] recipients) {
        this.toAddress = recipients;
    }
    
    @Override
    public void setSender(String sender) {
        this.fromAddress = sender;
    }
			
}
