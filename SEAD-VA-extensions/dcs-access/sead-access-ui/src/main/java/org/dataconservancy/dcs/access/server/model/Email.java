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

package org.dataconservancy.dcs.access.server.model;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Email {

	Properties props;
	String from;
	String host;
	String username;
	String password;
	
	public Email(String type, String fromAddress, String pwd){
		if(type.equalsIgnoreCase("gmail"))
			host = "smtp.gmail.com";
		else if(type.equalsIgnoreCase("iu"))
			host = "mail-relay.iu.edu";

		from = fromAddress;
		password =pwd;
		props = System.getProperties();
		props.put("mail.smtp.host", host);
		username = from;
		password = pwd;
		props.put("mail.smtp.port", "587"); 
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.auth", "true");
	}
	
	public boolean sendEmail(String[] toAddress, String subject, String messageStr)
	{
		try
		{
			Session session = Session.getInstance(props,
	                new javax.mail.Authenticator() {
	                    protected PasswordAuthentication getPasswordAuthentication() {
	                        return new PasswordAuthentication(username, password);
	                    }
	                });
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
		
			InternetAddress[] addressTo = new InternetAddress[toAddress.length];
            for (int i = 0; i < addressTo.length; i++)
            {
                addressTo[i] = new InternetAddress(toAddress[i]);
            }
           
            message.setRecipients(Message.RecipientType.TO, addressTo);
		
			message.setSubject(subject);
			message.setText(messageStr);
			Transport transport = session.getTransport("smtp");
			transport.connect(host,from,password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
