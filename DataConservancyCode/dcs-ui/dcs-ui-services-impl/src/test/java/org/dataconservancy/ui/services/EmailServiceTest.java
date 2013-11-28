package org.dataconservancy.ui.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.dataconservancy.ui.model.Email;
import org.dataconservancy.ui.model.EmailAttachment;
import org.dataconservancy.ui.model.SMTPServerSettings;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

public class EmailServiceTest {
    
    private final String MESSAGE = "Test Message";
    private final String RECIPIENT = "recipient@place.org";
    private final String SENDER = "sender@here.abc";
    private final String SUBJECT = "Test Subject";
    
    private final String ATTACHMENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>test</root>";
    private final String ATTACHMENT_FILENAME = "test.xml";
    
    private GreenMail testServer;

    private final SMTPServerSettings testSmtpSettings;
    private Email testEmail;
    private final EmailService service;
    
    /**
     * Sets up the final variables that do not need to be reset with each test
     */
    public EmailServiceTest() {
        testSmtpSettings = new SMTPServerSettings();
        testSmtpSettings.setAuthenticationEnabled(false);
        testSmtpSettings.setEmailServiceEnabled(true);
        testSmtpSettings.setPassword("na");
        testSmtpSettings.setPortNumber("3025");
        testSmtpSettings.setSmtpServer("localhost");
        testSmtpSettings.setSslEnabled(false);
        testSmtpSettings.setUsername("testUser");
        
        service = new EmailService();
        service.setSmtpServerSettings(testSmtpSettings);
    }
    
    
    /**
     * Resets the email and email server for clean start with each test
     */
    @Before
    public void setUp() {
        testEmail = new Email();
        testEmail.setNotificationMessage(MESSAGE);
        testEmail.setRecipient(new String[] {RECIPIENT});
        testEmail.setSender(SENDER);
        testEmail.setSubject(SUBJECT);
        
        testServer = new GreenMail(ServerSetupTest.ALL);
        testServer.start();
    }
    
    
    /**
     * Stops the email server after each test
     */
    @After
    public void tearDown() {
        testServer.stop();
    }
    
    
    /**
     * Tests that an email with no attachments is sent as expected
     * @throws MessagingException
     * @throws IOException
     */
    @Test
    public void testEmailWithNoAttachment() throws MessagingException, IOException {
        // Send the email
        service.sendNotification(testEmail);
        
        // Check that only one message was sent
        Integer numMessages = testServer.getReceivedMessages().length;
        assertTrue("Expected only one message, got " + numMessages, numMessages == 1);
        
        // Check that the message is just a plaintext message
        MimeMessage message = testServer.getReceivedMessages()[0];
        assertTrue("Subject of message was not correct", SUBJECT.equals(message.getSubject()));
        assertTrue("Content of message was not a string as expected", message.getContent() instanceof String);
    }
    
    
    /*
     * Tests that an email with an attachment is built and sent as expected
     */
    @Test
    public void testEmailWithAttachment() throws IOException, MessagingException {
        // Set up the attachment and send the email
        testEmail.attachments().add(createNewAttachment(ATTACHMENT, ATTACHMENT_FILENAME));
        service.sendNotification(testEmail);
        
        // Check that only one message was sent
        Integer numMessages = testServer.getReceivedMessages().length;
        assertTrue("Expected only one message, got " + numMessages, numMessages == 1);
        
        // Check that the message content is correct (multipart)
        MimeMessage message = testServer.getReceivedMessages()[0];
        assertFalse("Content of message was a string, should not have been", message.getContent() instanceof String);
        assertTrue("Content of message was not Multipart as expected", message.getContent() instanceof Multipart);
        
        // Check that the first part is the message content, the second is the attachment
        Multipart parts = (Multipart)message.getContent();
        Integer numParts = parts.getCount();
        assertTrue("Expected 2 parts; body and attachment.  Instead, found " + numParts, numParts == 2);
        assertTrue("Expected first part to be the body", MESSAGE.equals(parts.getBodyPart(0).getContent()));
        assertTrue("Expected second part to be the attachment", ATTACHMENT_FILENAME.equals(parts.getBodyPart(1).getFileName()));
    }
    
    
    @Test
    public void testEmailWithMultipleAttachments() throws IOException, MessagingException {
        // Set up the attachments and send the email
        testEmail.attachments().add(createNewAttachment(ATTACHMENT, ATTACHMENT_FILENAME.replace(".xml", "1.xml")));
        testEmail.attachments().add(createNewAttachment(ATTACHMENT, ATTACHMENT_FILENAME.replace(".xml", "2.xml")));
        service.sendNotification(testEmail);
        
     // Check that only one message was sent
        Integer numMessages = testServer.getReceivedMessages().length;
        assertTrue("Expected only one message, got " + numMessages, numMessages == 1);
        
        // Check that the first part is the message content, the second and third are the attachments
        MimeMessage message = testServer.getReceivedMessages()[0];
        Multipart parts = (Multipart)message.getContent();
        Integer numParts = parts.getCount();
        assertTrue("Expected 3 parts; body and two attachments.  Instead, found " + numParts, numParts == 3);
        assertTrue("Expected first part to be the body", MESSAGE.equals(parts.getBodyPart(0).getContent()));
        assertTrue("Expected main part to have no filename", parts.getBodyPart(0).getFileName() == null);
        assertFalse("Expected second part to be an attachment", parts.getBodyPart(1).getFileName() == null);
        assertFalse("Expected third part to be an attachment", parts.getBodyPart(2).getFileName() == null);
        assertFalse("Expected attachments to be different files", parts.getBodyPart(1).getFileName().equals(parts.getBodyPart(2).getFileName()));
    }
    
    
    private EmailAttachment createNewAttachment(String data, String filename) throws UnsupportedEncodingException {
        InputStream attachmentStream = new ByteArrayInputStream(data.getBytes("UTF-8"));
        EmailAttachment attach = new EmailAttachment(attachmentStream, "application/xml", filename);
        
        return attach;
    }
}
