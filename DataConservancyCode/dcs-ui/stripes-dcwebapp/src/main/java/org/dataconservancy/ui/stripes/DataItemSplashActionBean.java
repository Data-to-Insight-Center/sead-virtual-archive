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
package org.dataconservancy.ui.stripes;

import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.util.ArchiveSearchResult;

import static org.dataconservancy.ui.stripes.MessageKey.*;

/**
 * {@code DataItemSplashActionBean} handles requests to display information about a {@link DataItem}
 */
@UrlBinding("/dataitem/data_item_splash.action")
public class DataItemSplashActionBean extends BaseActionBean {
    
    /**
     * The path used to render the data item.
     */
    private final static String DATA_ITEM_SPLASH_PATH = "/pages/data_item_splash.jsp";
    
    private String dataItemID;
    private DataItem dataItem;
    
    private ArchiveService archiveService;
    private UserService userService;
    
    private Date depositDate;
    private Person depositor;
    private ArchiveDepositInfo.Status depositStatus;
    
    public DataItemSplashActionBean() {
        super();

        try {
            assert(messageKeys.containsKey(MSG_KEY_EMPTY_OR_INVALID_ID));
            assert(messageKeys.containsKey(MSG_KEY_ERROR_RETRIEVING_DATASET));
            assert(messageKeys.containsKey(MSG_KEY_ERROR_DATA_ITEM_NOT_FOUND));
            assert(messageKeys.containsKey(MSG_KEY_ARCHIVE_PROBLEM));
        }
        catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of "
                + MSG_KEY_EMPTY_OR_INVALID_ID + ", "
                + MSG_KEY_ERROR_RETRIEVING_DATASET + " or"
                + MSG_KEY_ERROR_DATA_ITEM_NOT_FOUND + " or"
                + MSG_KEY_ARCHIVE_PROBLEM 
                + " is missing");
        }
    }
    
    @DefaultHandler
    public Resolution render() {
        if (dataItemID != null && !dataItemID.isEmpty()) {
            try {
                archiveService.pollArchive();
            } catch (ArchiveServiceException e) {
                return new ErrorResolution(500, String.format(messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM), e.getMessage()));
            }

            List<ArchiveDepositInfo> infoList = archiveService.listDepositInfo(dataItemID, ArchiveDepositInfo.Status.DEPOSITED);

            if (infoList != null && !infoList.isEmpty()) {
                String dataItemDepositID = infoList.get(0).getDepositId();
                ArchiveSearchResult<DataItem> result = null;
                try {
                    result = archiveService.retrieveDataSet(dataItemDepositID);
                } catch (ArchiveServiceException e) {
                    return new ErrorResolution(500, String.format(messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM), e.getMessage()));
                }

                if (result != null && result.getResultCount() > 0) {
                    dataItem = result.getResults().iterator().next(); 
                    if (dataItem.getDepositDate() != null) {
                        depositDate = dataItem.getDepositDate().toDate();
                    }
                    depositStatus = archiveService.listDepositInfo(dataItemID, null).get(0).getDepositStatus();
                    depositor = userService.get(dataItem.getDepositorId());
                } else {
                    return new ErrorResolution(404, String.format(messageKeys.getProperty(MSG_KEY_ERROR_DATA_ITEM_NOT_FOUND), dataItemID));
                }
            } else {
                return new ErrorResolution(404, String.format(messageKeys.getProperty(MSG_KEY_ERROR_DATA_ITEM_NOT_FOUND), dataItemID));
            }
        } else {
            if (dataItemID == null) {
               dataItemID = ""; 
            }
            return new ErrorResolution(404, String.format(messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID), dataItemID));
        }
        
        return new ForwardResolution(DATA_ITEM_SPLASH_PATH);
    }
    
    public String getDataItemID() {
        return dataItemID;
    }
    
    public void setDataItemID(String id) {
        this.dataItemID = id;
    }
    
    public DataItem getDataItem() {
        return dataItem;
    }
    
    public void setDataItem(DataItem dataItem) {
        this.dataItem = dataItem;
    }

    public ArchiveDepositInfo.Status getDepositStatus(){
        return depositStatus;
    }

    public void setDepositStatus (ArchiveDepositInfo.Status status){
        this.depositStatus = status;
    }
    
    @Override
    public String getPageTitle() {
        if (dataItem != null) {
            return String.format(super.getPageTitle(), dataItem.getName());
        } else {
            return String.format(super.getPageTitle(), "DataItem");
        }
    }
    
    public Date getDepositDate() {
        return depositDate;
    }
    
    public Person getDepositor() {
    	return depositor;
    }
    
    public void setDepositor(Person depositor) {
    	this.depositor = depositor;
    }
    
    @SpringBean("archiveService")
    public void injectArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }
    
    @SpringBean("userService")
    public void injectUserService(UserService userService) {
    	this.userService = userService;
    }

}