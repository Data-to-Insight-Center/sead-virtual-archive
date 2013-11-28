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
import java.nio.charset.Charset;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.dataconservancy.ui.model.DataItem;
import org.joda.time.DateTime;

/**
 *
 */
public class DepositRequest {
    
    private String collectionId; // currentCollectionId
    
    private String name; // dataSet.name
    private String description; // dataSet.description
    private DateTime depositDate;
    private String depositorId;
    private String dataItemIdentifier;
    private String packageId;
    
    private boolean isContainer = false;
    private boolean isUpdate = false;
    
    private File fileToDeposit; // uploadedFile
    
    private final UiUrlConfig urlConfig;
    
    private static final String STRIPES_DEPOSIT_EVENT = "deposit";
    private static final String STRIPES_UPDATE_EVENT = "update";
    private boolean dataSetSet = false;
    
    public DepositRequest(UiUrlConfig urlConfig) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UiUrlConfig must not be null.");
        }
        this.urlConfig = urlConfig;
    }
    
    public DataItem getDataItem() {
        if (!dataSetSet) {
            throw new IllegalStateException("DataItem not set: call setDataSet(DataItem) first.");
        }
        
        DataItem ds = new DataItem();
        ds.setName(name);
        ds.setDepositDate(depositDate);
        ds.setDepositorId(depositorId);
        ds.setId(dataItemIdentifier);
        ds.setDescription(description);
        return ds;
    }
    
    public void setDataItem(DataItem ds) {
        this.name = ds.getName();
        this.description = ds.getDescription();
        this.depositDate = ds.getDepositDate();
        this.depositorId = ds.getDepositorId();
        this.dataItemIdentifier = ds.getId();
        this.dataSetSet = true;
    }
    
    public boolean isContainer() {
        return isContainer;
    }
    
    public org.dataconservancy.ui.model.Package getPackage() {
        org.dataconservancy.ui.model.Package thePackage = new org.dataconservancy.ui.model.Package();
        thePackage.setId(this.packageId);
        return thePackage;
    }
    
    public void setPackage(org.dataconservancy.ui.model.Package thePackage) {
        this.packageId = thePackage.getId();
    }
    
    public void setContainer(boolean container) {
        isContainer = container;
    }
    
    public void setIsUpdate(boolean update) {
        this.isUpdate = update;
    }
    
    public boolean isUpdate() {
        return isUpdate;
    }
    
    public String getDataItemIdentifier() {
        return dataItemIdentifier;
    }
    
    public void setDataItemIdentifier(String id) {
        this.dataItemIdentifier = id;
    }
    
    public String getCollectionId() {
        return collectionId;
    }
    
    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }
    
    public File getFileToDeposit() {
        return fileToDeposit;
    }
    
    public void setFileToDeposit(File fileToDeposit) {
        this.fileToDeposit = fileToDeposit;
    }
    
    public HttpPost asHttpPost() {
        if (!dataSetSet) {
            throw new IllegalStateException("DataItem not set: call setDataSet(DataItem) first.");
        }
        
        if (fileToDeposit == null) {
            throw new IllegalStateException("File not set: call setFileToDeposit(File) first");
        }
        
        if (collectionId == null || collectionId.isEmpty()) {
            throw new IllegalStateException("Collection id not set: call setCollectionId(String) first.");
        }
        
        if (isUpdate && (dataItemIdentifier == null || dataItemIdentifier.isEmpty())) {
            throw new IllegalStateException(
                    "Identifer is not set Identifier must be set: callSetDataSetIdentifier or pass in an ID in the constructor");
        }
        
        if (null == packageId)
            packageId = "";
        
        String depositUrl = urlConfig.getDepositUrl().toString()
                + "?redirectUrl=/pages/usercollections.jsp?currentCollectionId=" + collectionId;
        HttpPost post = new HttpPost(depositUrl);
        MultipartEntity entity = new MultipartEntity();
        try {
            entity.addPart("currentCollectionId", new StringBody(collectionId, Charset.forName("UTF-8")));
            entity.addPart("dataSet.name", new StringBody(name, Charset.forName("UTF-8")));
            entity.addPart("dataSet.description", new StringBody(description, Charset.forName("UTF-8")));
            entity.addPart("dataSet.id", new StringBody(dataItemIdentifier, Charset.forName("UTF-8")));
            entity.addPart("depositPackage.id", new StringBody(packageId, Charset.forName("UTF-8")));
            entity.addPart("isContainer",
                    new StringBody(Boolean.valueOf(isContainer()).toString(), Charset.forName("UTF-8")));
            if (isUpdate) {
                entity.addPart("datasetToUpdateId", new StringBody(dataItemIdentifier, Charset.forName("UTF-8")));
                entity.addPart(STRIPES_UPDATE_EVENT, new StringBody("Update", Charset.forName("UTF-8")));
            }
            else {
                entity.addPart(STRIPES_DEPOSIT_EVENT, new StringBody("Deposit", Charset.forName("UTF-8")));
            }
            
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        FileBody fileBody = new FileBody(fileToDeposit);
        entity.addPart("uploadedFile", fileBody);
        post.setEntity(entity);
        
        return post;
    }
    
    // http://localhost:8080/Deposit.action?render=&currentCollectionId=http%3A%2F%2Fdms.jhu.edu%2F2
    
    // http://localhost:8080/Deposit.action?render=&currentCollectionId=http%3A%2F%2Fdms.jhu.edu%2F2
    // /Deposit.action
    // String depositUrl = "http://localhost:8080/deposit/deposit.action?currentCollectionId=" + URLEncoder.encode(new
    // URL(collectionId).toExternalForm(), "UTF-8");
    // String datasetName = "Sample Dataset";
    // String datasetDescription = "Sample Dataset Description";
    // HttpPost post = new HttpPost(depositUrl);
    // MultipartEntity entity = new MultipartEntity();
    // entity.addPart("currentCollectionId", new StringBody(collectionId, Charset.forName("UTF-8")));
    // entity.addPart("dataSet.name", new StringBody(datasetName, Charset.forName("UTF-8")));
    // entity.addPart("dataSet.description", new StringBody(datasetDescription, Charset.forName("UTF-8")));
    // FileBody fileBody = new FileBody(sampleDataFile);
    // entity.addPart("uploadedFile", fileBody);
    // entity.addPart("deposit", new StringBody("Deposit", Charset.forName("UTF-8")));
    // post.setEntity(entity);
    
}
