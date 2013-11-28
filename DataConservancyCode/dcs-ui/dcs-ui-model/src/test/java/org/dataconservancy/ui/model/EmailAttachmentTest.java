package org.dataconservancy.ui.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class EmailAttachmentTest {
    
    private final String FAKEDATA = "THIS IS THE FAKE DATA";
    private InputStream filedata;
    
    @Before
    public void setUp() throws UnsupportedEncodingException {
        filedata = new ByteArrayInputStream(FAKEDATA.getBytes("UTF-8"));
    }
    
    /**
     * Test that the default attachment is empty and valid
     * @throws IOException
     */
    @Test
    public void testDefaultEmptyAttachment() throws IOException {
        EmailAttachment att = new EmailAttachment();
        
        // Empty data, should immediately return end of file
        assertTrue(att.getData().read() == -1);
        
        // Plain text
        assertTrue(att.getType().equals("text/plain"));
        
        // Should have some kind of filename
        assertFalse(att.getFilename().isEmpty());
    }
    
    
    /**
     * Verify that xml filetypes produce the right default extension
     */
    @Test
    public void testXMLTypeHasCorrectExtension() {
        EmailAttachment att = new EmailAttachment(filedata, "appliation/xml");
        
        assertTrue(att.getFilename().endsWith(".xml"));
    }
    
    
    /**
     * Verify that html filetypes produce the right default extension
     */
    @Test
    public void testHTMLTypeHasCorrectExtension() {
        EmailAttachment att = new EmailAttachment(filedata, "text/html");
        
        assertTrue(att.getFilename().endsWith(".html"));
    }
    
    
    @Test
    public void testAttachmentStreamIsValid() throws IOException {
        EmailAttachment att = new EmailAttachment(filedata, "text/plain");
        
        // The stream should not be empty
        assertFalse(att.getData().read() == -1);
    }
}
