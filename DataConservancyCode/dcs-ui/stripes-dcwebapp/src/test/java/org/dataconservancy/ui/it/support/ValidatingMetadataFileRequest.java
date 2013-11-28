/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.ui.it.support;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.ui.model.MetadataFile;

/**
 *
 */
public class ValidatingMetadataFileRequest {
    
    
    private static final String STRIPES_EVENT = "validate";
    private UiUrlConfig urlConfig;
    private File fileToTest;
    private String formatId;
    private MetadataFile metadataFile;
    
    public ValidatingMetadataFileRequest(UiUrlConfig urlConfig) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UiUrlConfig must not be null.");
        }
        this.urlConfig = urlConfig;
    }
    
    public File getFileToTest() {
        return fileToTest;
    }
    
    public void setFileToTest(File fileToTest) {
        this.fileToTest = fileToTest;
    }
    
    public String getFormatId() {
        return formatId;
    }
    
    public void setFormatId(String formatId) {
        this.formatId = formatId;
    }
    
    public void setUpMetadataFile() throws URISyntaxException, MalformedURLException {
        metadataFile = new MetadataFile("metadata:file1", null, "text/xml", "metadata:name:Sample2.xml", null,
                formatId, "");
        metadataFile.setSource(fileToTest.toURI().toURL().toExternalForm());
        metadataFile.setPath(fileToTest.getPath());
    }

    public HttpPost asHttpPost() {
        if (fileToTest == null) {
            throw new IllegalStateException("File not set: call setFileToTest(File) first");
        }
        
        String validatingMetadataFileUrl = urlConfig.getAdminValidatingMetadataFilePathPostUrl().toString();
        HttpPost post = new HttpPost(validatingMetadataFileUrl);
        MultipartEntity entity = new MultipartEntity();
        try {
            entity.addPart(STRIPES_EVENT, new StringBody("Validate", Charset.forName("UTF-8")));
            entity.addPart("metadataFormatId", new StringBody(formatId, Charset.forName("UTF-8")));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        FileBody fileBody = new FileBody(fileToTest);
        entity.addPart("sampleMetadataFile", fileBody);
        post.setEntity(entity);

        return post;
    }
    
}
