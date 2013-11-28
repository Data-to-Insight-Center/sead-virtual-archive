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
package org.dataconservancy.ui.services;

import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.dataconservancy.ui.model.Email;
import org.dataconservancy.ui.model.EmailAttachment;
import org.dataconservancy.ui.model.Notification;
import org.dataconservancy.ui.model.SMTPServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code EmailService } is an implementation of {@link NotificationService} using emails as a method of notification.
 */

public class EmailService implements NotificationService {

    private SMTPServerSettings smtpServerSettings;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public SMTPServerSettings getSmtpServerSettings() {
        return smtpServerSettings;
    }

    public void setSmtpServerSettings(SMTPServerSettings smtpServerSettings) {
        this.smtpServerSettings = smtpServerSettings;
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(smtpServerSettings.getUsername(), smtpServerSettings.getPassword());
        }
    }

    @Override
    public void sendNotification(Notification notification) {
        if (notification instanceof Email) {
            Email email = (Email) notification;
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpServerSettings.getSmtpServer());
            props.put("mail.smtp.port", smtpServerSettings.getPortNumber());
            props.put("mail.smtp.user", smtpServerSettings.getUsername());

            if (smtpServerSettings.isSslEnabled()) {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.socketFactory.port", smtpServerSettings.getPortNumber());
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");
            }

            try {// default users admin and user used in testing will cause NPE here
                InternetAddress fromInternetAddress = new InternetAddress(email.getFromAddress());
                try {
                    Session session = null;
                    if (smtpServerSettings.isAuthenticationEnabled()) {
                        Authenticator auth = new SMTPAuthenticator();
                        session = Session.getInstance(props, auth);
                    } else {
                        session = Session.getInstance(props);
                    }
                    if (session != null) {
                        MimeMessage msg = new MimeMessage(session);
                        //msg.setText(email.getBody());
                        msg.setSubject(email.getSubject());
                        msg.setFrom(fromInternetAddress);
                        for (String toEmailAddress : email.getToAddress()) {
                            msg.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(toEmailAddress));
                        }
                        setMessageContent(msg, email);                        
                        if (smtpServerSettings.isEmailServiceEnabled()) {
                        	log.debug("Sending email to {}.", email.getToAddress());
                            Transport.send(msg);
                        } else {
                            log.debug("Cannot send email to {}: email service is disabled.", email.getToAddress());
                        }
                    }
                } catch (Exception mex) {
                    mex.printStackTrace();
                }
            } catch (Exception e){
                log.debug("From internet address could not be returned for user with email address {}", email.getFromAddress());
            }
        }
    }
    
    
    private void setMessageContent(MimeMessage msg, Email email) throws MessagingException, IOException {
        if (email.attachments().size() == 0) {
            msg.setText(email.getBody());
        } else {
            Multipart multipart = new MimeMultipart();
            MimeBodyPart mainContent = new MimeBodyPart();
            mainContent.setText(email.getBody());
            multipart.addBodyPart(mainContent);
            
            for (EmailAttachment attachment : email.attachments()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                DataSource attachmentSource = new ByteArrayDataSource(attachment.getData(), attachment.getType());
                attachmentPart.setDataHandler(new DataHandler(attachmentSource));;
                attachmentPart.setFileName(attachment.getFilename());
                
                multipart.addBodyPart(attachmentPart);
            }
            
            msg.setContent(multipart);
        }
    }
}

