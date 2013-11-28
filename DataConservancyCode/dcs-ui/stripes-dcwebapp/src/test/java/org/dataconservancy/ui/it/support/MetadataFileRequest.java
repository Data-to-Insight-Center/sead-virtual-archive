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
package org.dataconservancy.ui.it.support;

import java.io.File;
import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import org.dataconservancy.ui.model.MetadataFile;

/**
 *
 */
public class MetadataFileRequest {

    private String parentId; // the id of the parent of the metdata file
    private String id; // the metadata file id (if one exists)
    private String name; // the name of the metadata file
    private String format; // the format of the metadata file
    private String metadataFormat; //The metadata format of the file
    private File fileToDeposit;
    
    private boolean isCollection = true;
    private String event = "";

    private final UiUrlConfig urlConfig;

    public static final String STRIPES_SAVE_DONE_EVENT = "saveAndDoneMetadataFile";
    public static final String STRIPES_SAVE_MORE_EVENT = "saveAndAddMoreMetadataFile";
    public static final String STRIPES_CANCEL_EVENT = "cancel";
    public static final String STRIPES_DELETE_EVENT = "deleteMetadataFile";
    public static final String STRIPES_PREVIEW_EVENT = "previewMetadataIngest";
    public static final String STRIPES_DISPLAY_DEPOSIT_ERRORS_EVENT = "displayDepositErrors";

    public MetadataFileRequest(UiUrlConfig urlConfig) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UiUrlConfig must not be null.");
        }
        this.urlConfig = urlConfig;
    }

    public MetadataFileRequest(MetadataFileRequest toCopy) {

        this.urlConfig = toCopy.urlConfig;

        this.parentId = toCopy.parentId;
        this.id = toCopy.id;
        this.name = toCopy.name;
        this.format = toCopy.format;
        this.metadataFormat = toCopy.metadataFormat;
        this.fileToDeposit = toCopy.fileToDeposit;
        this.isCollection = toCopy.isCollection;
        this.event = toCopy.event;
    }

    public boolean getIsCollection() {
        return isCollection;
    }
    
    public void setIsCollection(boolean isCollection) {
        this.isCollection = isCollection;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String id) {
        this.parentId = id;
    }
    
    public void setMetadataFileId(String id) {
        this.id = id;
    }
    
    public String getMetadataFileId() {
        return id;
    }
    
    public MetadataFile getMetadataFile() {
        MetadataFile file = new MetadataFile();
        file.setFormat(format);
        file.setName(name);
        file.setMetadataFormatId(metadataFormat);
        return file;
    }
    
    public void setMetadataFile(MetadataFile file) {
        format = file.getFormat();
        name = file.getName();
        metadataFormat = file.getMetadataFormatId();
    }
    
    public String getMetadataFormatId() {
        return metadataFormat;
    }
    
    public void setMetadataFormatId(String formatId) {
        this.metadataFormat = formatId;
    }
    
    public File getFileToDeposit() {
        return fileToDeposit;
    }
    
    public void setFileToDeposit(File file) {
        fileToDeposit = file;
    }
    
    public String getEvent() {
        return event;
    }
    
    public void setEvent(String event) {
        this.event = event;
    }
    
    public HttpPost asHttpPost() {

        String depositUrl = urlConfig.getMetadataFileUrl().toString();
        HttpPost post = new HttpPost(depositUrl);
        MultipartEntity entity = new MultipartEntity();
        try {
            entity.addPart("parentID", new StringBody(parentId, Charset.forName("UTF-8")));
            if (name != null && !name.isEmpty()) {
                entity.addPart("metadataFile.name", new StringBody(name, Charset.forName("UTF-8")));
            }
            
            if (metadataFormat != null && !metadataFormat.isEmpty()) {
                entity.addPart("metadataFile.metadataFormatId", new StringBody(metadataFormat, Charset.forName("UTF-8")));
            }
            
            if (id != null && !id.isEmpty()) {
                entity.addPart("metadataFileID", new StringBody(id, Charset.forName("UTF-8")));
            }
                
            if (isCollection) {
                entity.addPart("redirectUrl", new StringBody("viewCollectionDetails", Charset.forName("UTF-8")));
            }
            
            entity.addPart(event, new StringBody(event, Charset.forName("UTF-8")));           
            
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        if (fileToDeposit != null) {
            FileBody fileBody = new FileBody(fileToDeposit);
            entity.addPart("uploadedFile", fileBody);
            post.setEntity(entity);
        }
        return post;
    }

}
