package org.dataconservancy.ui.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class EmailAttachment {

    private InputStream data;
    private String type;
    private String filename;
    
    
    /**
     * Basic constructor, sets default values
     */
    public EmailAttachment() {
        String empty = "";
        try {
            this.data = new ByteArrayInputStream(empty.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // Shouldn't get here, since this is a hard-coded default
            e.printStackTrace();
        }
        this.type = "text/plain";
        this.filename = "attachment.txt";
    }
    
    
    /**
     * Constructor to set the data and mime type, using a default filename
     * @param data The data to be used for the attachment
     * @param type The mime type
     */
    public EmailAttachment(InputStream data, String type) {
        this.data = data;
        this.type = type;
        this.filename = "attachment" + findExtension(type);
    }
    
    
    /**
     * Constructor to set the data, mime type, and filename
     * @param data The data to be used for the attachment
     * @param type The mime type
     * @param filename The filename to use for the attachment
     */
    public EmailAttachment(InputStream data, String type, String filename) {
        this.data = data;
        this.type = type;
        this.filename = filename;
    }
    
    
    /**
     * Set the data of the attachment
     * @param data The data to be used for the attachment
     */
    public void setData(InputStream data) {
        this.data = data;
    }
    
    /**
     * Retrieve the data of the attachment
     * @return the attachment data
     */
    public InputStream getData() {
        return data;
    }
    
    
    /**
     * Set the mime type of the attachment
     * @param type The mime type of the attachment
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Get the mime type of the attachment
     * @return The mime type
     */
    public String getType() {
        return type;
    }
    
    
    /**
     * Set the filename to be used for the attachment
     * @param filename The name of the file to be shown for the attachment
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    /**
     * Retrieve the filename to be used for the attachment
     * @return The filename of the attachment
     */
    public String getFilename() {
        return filename;
    }
    
    
    private String findExtension(String type) {
        if (type.contains("html")) {
            return ".html";
        } else if (type.contains("xml")) {
            return ".xml";
        } else {
            return "";
        }
    }
}
