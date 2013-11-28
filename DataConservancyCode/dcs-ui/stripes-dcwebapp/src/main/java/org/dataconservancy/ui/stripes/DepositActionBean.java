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

package org.dataconservancy.ui.stripes;

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_COLLECTION_DOES_NOT_EXIST;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_DATASET_TO_UPDATE_NOT_FOUND;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_DATASET_UPDATE_PENDING;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_CHECKING_PERMISSIONS;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_DEPOSITING_FILE;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_EMPTY_ZIP_FILE;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_LACKING_DEPOSIT_PERMISSION;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_READING_FILE;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_RETRIEVING_DATASET;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_PROJECT_FOR_COLLECTION_NOT_FOUND;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_NEW_DATA_AVAILABILITY_MESSAGE;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.FlashScope;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.Validate;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.events.DataItemDepositEvent;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.CollectionException;
import org.dataconservancy.ui.exceptions.DepositException;
import org.dataconservancy.ui.exceptions.PackageException;
import org.dataconservancy.ui.exceptions.UnpackException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Package;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.services.*;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.dataconservancy.ui.util.PackageExtractor;
import org.dataconservancy.ui.util.PackageSelector;
import org.dataconservancy.ui.util.UiBaseUrlConfig;
import org.dataconservancy.ui.util.ZipPackageExtractor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * {@code DepositActionBean} is responsible for aggregating information collection from the UI, applying business logic
 * on deposit attempts and deposit {@link DataItem} into the archive.
 */
@UrlBinding("/deposit/deposit.action")
public class DepositActionBean extends BaseActionBean {
    
    // ///////////////////
    // Logger
    // //////////////////
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private static final String SINGLE_FILE_DEPOSIT_FORM_PATH = "/pages/deposit.jsp";
    
    private static final String VIEW_COLLECTION_LIST = "/pages/usercollections.jsp";
    
    private static final String SINGLE_FILE_UPDATE_FORM_PATH = "/pages/updateFile.jsp";
  
    private String extractDirectory;
    
    private String currentCollectionId;
    
    private DataItem dataItem = new DataItem();
    
    private Package depositPackage = new Package();
    
    @Validate(required = true, on = { "deposit", "update" })
    private FileBean uploadedFile;
    
    private List<String> depositIds = new ArrayList<String>();
    
    private boolean isContainer;    
    
    private UiBaseUrlConfig uiBaseUrlConfig;
    
    // The business id of the file to update
    private String datasetToUpdateId;
    
    // The message to be displayed in the returned error resolution if one occurs
    private String errorMsg = "";
    
    // ///////////////
    // Services
    // ///////////////
    private RelationshipService relationshipService;
    
    private PackageService packageService;
    
    private ArchiveService archiveService;
    
    private IdService idService;
    
    private CollectionBizService collectionBizService;
    
    private FileBizService fileBizService;

    private AuthorizationService authorizationService;
    
    private PackageSelector packageSelector;
    
    private String redirectUrl;
    
    public DepositActionBean() {
        super();
        
        // Ensure desired properties are available.
        try {
            assert (messageKeys.containsKey(MSG_KEY_COLLECTION_DOES_NOT_EXIST));
            assert (messageKeys.containsKey(MSG_KEY_ERROR_READING_FILE));
            assert (messageKeys.containsKey(MSG_KEY_ERROR_LACKING_DEPOSIT_PERMISSION));
            assert (messageKeys.containsKey(MSG_KEY_ERROR_DEPOSITING_FILE));
            assert (messageKeys.containsKey(MSG_KEY_ERROR_EMPTY_ZIP_FILE));
            assert (messageKeys.containsKey(MSG_KEY_ERROR_RETRIEVING_DATASET));
            assert (messageKeys.containsKey(MSG_KEY_ERROR_CHECKING_PERMISSIONS));
            assert (messageKeys.containsKey(MSG_KEY_DATASET_UPDATE_PENDING));
            assert (messageKeys.containsKey(MSG_KEY_DATASET_TO_UPDATE_NOT_FOUND));
            assert (messageKeys.containsKey(MSG_KEY_ERROR_LACKING_DEPOSIT_PERMISSION));
            assert (messageKeys.containsKey(MSG_KEY_NEW_DATA_AVAILABILITY_MESSAGE));
        }
        catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of " + MSG_KEY_COLLECTION_DOES_NOT_EXIST
                    + ", " + MSG_KEY_ERROR_READING_FILE + ", " + MSG_KEY_ERROR_LACKING_DEPOSIT_PERMISSION + ", "
                    + MSG_KEY_ERROR_DEPOSITING_FILE + ", " + MSG_KEY_ERROR_EMPTY_ZIP_FILE + ", "
                    + MSG_KEY_ERROR_RETRIEVING_DATASET + ", " + MSG_KEY_ERROR_CHECKING_PERMISSIONS + ", "
                    + MSG_KEY_DATASET_UPDATE_PENDING + ", " + MSG_KEY_DATASET_TO_UPDATE_NOT_FOUND + ", "
                    + MSG_KEY_ERROR_LACKING_DEPOSIT_PERMISSION + " is missing.");
        }
        
    }
    
    public Package getDepositPackage() {
        return depositPackage;
    }
    
    public void setDepositPackage(Package depositPackage) {
        this.depositPackage = depositPackage;
    }
    
    public boolean isContainer() {
        return isContainer;
        
    }
    
    public void setIsContainer(boolean container) {
        isContainer = container;
    }
    
    public FileBean getUploadedFile() {
        return uploadedFile;
    }
    
    public void setUploadedFile(FileBean uploadedFile) {
        this.uploadedFile = uploadedFile;
    }
    
    public String getCurrentCollectionId() {
        return currentCollectionId;
    }
    
    public void setCurrentCollectionId(String currentCollectionId) {
        this.currentCollectionId = currentCollectionId;
    }
    
    public void setDataSet(DataItem dataItem) {
        this.dataItem = dataItem;
    }
    
    public DataItem getDataSet() {
        return dataItem;
    }
    
    public void setDatasetToUpdateId(String id) {
        datasetToUpdateId = id;
    }
    
    public String getDatasetToUpdateId() {
        return datasetToUpdateId;
    }
    
    public String getMaxSizeInfoMessage() {
        // TODO: find a way to grab the value of the init parameter in web.xml
        // which sets this. it may be possible to have maven interpolate
        // this value in web.xml from a location accessible to this action bean.
        // for now we hard code the limit.
        
        return "File upload limit is 2GB.";
    }
    
    /**
     * This method returns a list of all the deposit ids for a multi file deposit.
     */
    public List<String> getDepositIds() {
        return depositIds;
    }
    
    /**
     * This method sets the IDs of the deposits.
     */
    public void setDepositIds(List<String> depositIds) {
        this.depositIds = depositIds;
    }
    
    @DefaultHandler
    public Resolution render() {
        return new ForwardResolution(SINGLE_FILE_DEPOSIT_FORM_PATH);
    }
    
    public Resolution deposit() throws DepositException, BizInternalException, BizPolicyException {
        Resolution resultResolution;
        if (redirectUrl.contains("usercollections.jsp")) {
            resultResolution = returnToCollectionList(currentCollectionId);
        }
        else {
            resultResolution = returnToCollectionView(currentCollectionId);
        }
        
        int resultCode = checkPermissionsAndDeposit();
        
        if (resultCode == 400 || resultCode == 500) {
            resultResolution = new ErrorResolution(resultCode, errorMsg);
        }
        else if (resultCode == 302) {
            resultResolution = render();
        }
        
        return resultResolution;
    }
    
    /**
     * Returns a resolution to show the page for updating a file. This should be used only when a file has been selected
     * to be updated.
     */
    public Resolution renderUpdateForm() {
        return new ForwardResolution(SINGLE_FILE_UPDATE_FORM_PATH);
    }
    
    public Resolution update() throws DepositException, BizInternalException, BizPolicyException {

        // Is returning to CollectionDataList as it is the only entry point to here. But Newly updated file may not
        // appear
        // or be reflected on this listing page because the deposit isn't immediate.
        Resolution resultResolution = returnToCollectionDataList(currentCollectionId);
        int resultCode = 200;
        DataItem dataSetToUpdate = null;
        try {
            dataSetToUpdate = checkIfDataSetExists(datasetToUpdateId);
        }
        catch (ArchiveServiceException e) {
            log.debug("Error getting the existing DataItem associated with objectId {}: {}", datasetToUpdateId, e);
            DepositException depositEx = new DepositException(String.format(
                    messageKeys.getProperty(MSG_KEY_ERROR_RETRIEVING_DATASET), datasetToUpdateId), e);
            depositEx.setUserId(((getAuthenticatedUser() != null) ? getAuthenticatedUser().getEmailAddress()
                    : "Anonymous"));
            depositEx.setHttpStatusCode(500);
            depositEx.setDatasetId(datasetToUpdateId);
            depositEx.setCollectionId(currentCollectionId);
            depositEx.setContainer(isContainer);
            depositEx.setDepositedFile(uploadedFile);
            throw depositEx;
        }
        
        if (dataSetToUpdate != null) {
            try {
                if (checkParentCollectionPermissions(dataSetToUpdate)) {
                    resultCode = checkPermissionsAndDeposit();
                }
                else {
                    resultCode = 400;
                }
            }
            catch (RelationshipConstraintException e) {
                resultCode = 500;
                errorMsg = messageKeys.getProperty(MSG_KEY_ERROR_CHECKING_PERMISSIONS);
            }
        }
        else if (resultCode != 500) {
            resultCode = 400;
        }
        
        if (resultCode == 400 || resultCode == 500) {
            resultResolution = new ErrorResolution(resultCode, errorMsg);
        }
        else if (resultCode == 302) {
            resultResolution = renderUpdateForm();
        }
        
        return resultResolution;
    }
    
    public DataItem checkIfDataSetExists(String objectId) throws ArchiveServiceException {
        
        DataItem existingDataSet = null;
        
        // Make sure we have most up to date deposit status.
        archiveService.pollArchive();
        
        if (objectId != null && !objectId.isEmpty()) {
            List<ArchiveDepositInfo> infoList = archiveService.listDepositInfo(objectId,
                    ArchiveDepositInfo.Status.DEPOSITED);
            
            if (infoList != null && !infoList.isEmpty()) {
                String depositId = infoList.get(0).getDepositId();
                ArchiveSearchResult<DataItem> result = archiveService.retrieveDataSet(depositId);
                Iterator<DataItem> resultIter = result.getResults().iterator();
                if (resultIter.hasNext()) {
                    existingDataSet = resultIter.next();
                }
            }
            else {
                infoList = archiveService.listDepositInfo(objectId, ArchiveDepositInfo.Status.PENDING);
                if (infoList != null && !infoList.isEmpty()) {
                    errorMsg = String.format(messageKeys.getProperty(MSG_KEY_DATASET_UPDATE_PENDING), objectId);
                }
                else {
                    errorMsg = String.format(messageKeys.getProperty(MSG_KEY_DATASET_TO_UPDATE_NOT_FOUND), objectId);
                }
            }
        }
        
        return existingDataSet;
    }
    
    public boolean checkParentCollectionPermissions(DataItem dataItem) throws RelationshipConstraintException {
        boolean hasPermissionToDeposit = false;
        Collection parentCollection = relationshipService.getCollectionForDataSet(dataItem);
        // If the parent collection is the same as the collection were in return true, the deposit will check
        // permissions
        if (parentCollection != null && parentCollection.getId().equalsIgnoreCase(currentCollectionId)) {
            hasPermissionToDeposit = true;
        }
        else {
            // Check to make sure the user adding the collection is a depositor for this collection, or a system admin
            Person currentUser = getAuthenticatedUser();
            
            // TODO: Refactor out to a Collection Business Service?
            if (!authorizationService.canDepositToCollection(currentUser, parentCollection)) {
                errorMsg = String.format(messageKeys.getProperty(MSG_KEY_ERROR_LACKING_DEPOSIT_PERMISSION),
                        currentUser.getId(), parentCollection.getId());
            }
            else {
                hasPermissionToDeposit = true;
            }
        }
        
        return hasPermissionToDeposit;
    }
    
    /**
     * Checks for proper permissions and attempts to deposit the file
     * 
     * @returns The error code for the resolution if an error, 302 if the deposit wasn't successful and should be
     *          redirected back, and 200 for a successful deposit.
     */
    private int checkPermissionsAndDeposit() throws DepositException, BizInternalException, BizPolicyException {
        int returnCode = 200;
        getContext().getMessages().clear();
        
        final Collection currentCollection = getCollection(currentCollectionId);
        boolean canDepositToCollection = false;
        if (currentCollection == null) {
            final String msg = String.format(messageKeys.getProperty(MSG_KEY_COLLECTION_DOES_NOT_EXIST),
                    currentCollectionId);
            log.error(msg);
            DepositException depositEx = new DepositException(msg);
            depositEx.setCollectionId(currentCollectionId);
            depositEx.setContainer(isContainer);
            if (dataItem != null) {
                depositEx.setDatasetId(dataItem.getId());
                depositEx.setDatasetName(dataItem.getName());
            }
            depositEx.setDepositedFile(uploadedFile);
            depositEx.setHttpStatusCode(400);
            depositEx.setUserId(((getAuthenticatedUser() != null) ? getAuthenticatedUser().getId() : "Anonymous"));
            throw depositEx;
        }
        else {
            // Check to make sure the user adding the collection is a depositor for this collection, or a system admin
            final Person currentUser = getAuthenticatedUser();
            
            // TODO: Refactor out to a Collection Business Service?
            try {
                canDepositToCollection = authorizationService.canDepositToCollection(currentUser, currentCollection);
            } catch (RelationshipConstraintException e) {
                throw new BizInternalException("Exception occured when checking for deposit permission. \n" + e);
            }
            if (!canDepositToCollection) {
                final String msg = String.format(messageKeys.getProperty(MSG_KEY_ERROR_LACKING_DEPOSIT_PERMISSION),
                        currentUser.getId(), currentCollection.getId());
                DepositException depositEx = new DepositException(msg);
                depositEx.setCollectionId(currentCollectionId);
                depositEx.setContainer(isContainer);
                if (dataItem != null) {
                    depositEx.setDatasetId(dataItem.getId());
                    depositEx.setDatasetName(dataItem.getName());
                }
                depositEx.setDepositedFile(uploadedFile);
                depositEx.setHttpStatusCode(400);
                depositEx.setUserId(currentUser.getId());
                throw depositEx;
            }
            else {
                depositData();
                sendNotification(depositPackage);
            }
        }
        
        return returnCode;
    }
    
    /**
     * <p>
     * Takes the uploaded file, and isContainer flag to determine the route of deposit.
     * </p>
     * <p>
     * </p>
     * 
     * @throws {@code IOException} when reading content of uploaded file failed.
     * @throws {@link org.dataconservancy.ui.exceptions.UnpackException} when the file provided to be un-zipped and
     *         deposit could not be un-zipped. Exception should not occur in the case of single, regular file deposited.
     * @throws {@link org.dataconservancy.ui.exceptions.DepositException} when an empty zipped file is request to be
     *         un-zipped and deposited.
     */
    private void depositData() throws DepositException, BizInternalException, BizPolicyException {
        // create datasets list
        List<DataItem> preparedDataSets = new ArrayList<DataItem>();
        
        //Extractor that will be used in the event of a package file.
        PackageExtractor extractor = null;
        
        // Package depositPackage = new Package();
        if (depositPackage.getId() == null)
            depositPackage.setId(idService.create(Types.PACKAGE.name()).getUrl().toString());
        final Person currentUser = getAuthenticatedUser();
        final String currentUserId = (getAuthenticatedUser() != null) ? getAuthenticatedUser().getId() : "Anonymous";
        
        // determine which path data should go through
        if (isContainer == false) {
            // ********************************************
            // if a simple file, create simple dataset
            // ********************************************
            DataItem preparedDataSet = new DataItem(dataItem);
            
            if (preparedDataSet.getId() == null) {
                if (datasetToUpdateId == null || datasetToUpdateId.isEmpty()) {
                    preparedDataSet.setId(idService.create(Types.DATA_SET.name()).getUrl().toString());
                }
                else {
                    preparedDataSet.setId(datasetToUpdateId);
                }
            }
            
            if (preparedDataSet.getDepositDate() == null) {
                preparedDataSet.setDepositDate(new DateTime());
            }
            
            if (preparedDataSet.getName() == null) {
                preparedDataSet.setName(uploadedFile.getFileName());
            }
            
            if (preparedDataSet.getDepositorId() == null) {
                preparedDataSet.setDepositorId(currentUser.getId());
            }

            preparedDataSet.setParentId(currentCollectionId);
            
            // add simple file to preparedDataSet
            try {
                final DataFile df = loadDataFile(preparedDataSet.getId(), uploadedFile);
                df.setParentId(preparedDataSet.getId());
                preparedDataSet.addFile(df);
            }
            catch (IOException e) {
                DepositException depositException = new DepositException(
                        messageKeys.getProperty(MSG_KEY_ERROR_READING_FILE), e);
                depositException.setMessageKey(MSG_KEY_ERROR_READING_FILE);
                depositException.setDepositedFile(uploadedFile);
                depositException.setCollectionId(currentCollectionId);
                depositException.setDatasetId(preparedDataSet.getId());
                depositException.setDatasetName(preparedDataSet.getName());
                depositException.setHttpStatusCode(500);
                depositException.setUserId(currentUserId);
                throw depositException;
            }
            
            // add to list
            preparedDataSets.add(preparedDataSet);
            // set up single deposit package for this deposit
            depositPackage.setPackageFileName(uploadedFile.getFileName());
            depositPackage.setPackageType(Package.PackageType.SIMPLE_FILE);
            depositPackage.addFile(preparedDataSet.getId(), uploadedFile.getFileName());
            
            // ********************************************
            // if a single zip file, intended (by user) to be deposited singly, but is expanded anyways
            // ********************************************
            // call {@link org}getPreparedDataSet(uploadedFile)
        }
        // if batch deposit (via zipped file),
        else {
            // unpack zip file
            List<File> unzippedFiles = null;
            
            try {
                //Check that the file is a package file
                Map<String, String> metadata = new HashMap<String, String>();
                metadata.put("Content-Disposition", "attachment; filename=\"" + uploadedFile.getFileName() + "\"");
                extractor = packageSelector.selectPackageExtractor(uploadedFile.getInputStream(), metadata);
                if (extractor == null) {
                    UnpackException e = new UnpackException("File was not a recognized package type " + uploadedFile.getFileName());
                    e.setError("File was not a recognized package type");
                    log.error(e.getMessage(), e);
                    throw e;
                }
                unzippedFiles = extractor.getFilesFromPackageFileBean(uploadedFile.getFileName(), uploadedFile);
            }
            catch (UnpackException e) {
                log.error(e.getMessage(), e);
                final String userMessage = String.format(messageKeys.getProperty(MSG_KEY_ERROR_DEPOSITING_FILE),
                        uploadedFile.getFileName(), e.getError());
                DepositException depositException = new DepositException(userMessage, e);
                depositException.setMessageKey(MSG_KEY_ERROR_DEPOSITING_FILE);
                depositException.setDepositedFile(uploadedFile);
                depositException.setCollectionId(currentCollectionId);
                depositException.setContainer(isContainer);
                depositException.setDatasetName(uploadedFile.getFileName());
                depositException.setHttpStatusCode(500);
                depositException.setUserId(currentUserId);
                throw depositException;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                final String userMessage = String.format(messageKeys.getProperty(MSG_KEY_ERROR_DEPOSITING_FILE),
                        uploadedFile.getFileName(), e.getMessage());
                DepositException depositException = new DepositException(userMessage, e);
                depositException.setMessageKey(MSG_KEY_ERROR_DEPOSITING_FILE);
                depositException.setDepositedFile(uploadedFile);
                depositException.setCollectionId(currentCollectionId);
                depositException.setContainer(isContainer);
                depositException.setDatasetName(uploadedFile.getFileName());
                depositException.setHttpStatusCode(500);
                depositException.setUserId(currentUserId);
            }
            
            // I believe that empty zip files will throw an exception in the earlier call to
            // zipExtractor.getFilesFromPackageFileBean, so I'm uncertain if this code block is ever
            // executed.
            if (unzippedFiles == null || unzippedFiles.size() == 0) {
                DepositException depositException = new DepositException("Provided zip file was empty.");
                depositException.setDepositedFile(uploadedFile);
                depositException.setCollectionId(currentCollectionId);
                depositException.setContainer(isContainer);
                depositException.setDatasetName(uploadedFile.getFileName());
                depositException.setUserId(currentUserId);
                depositException.setHttpStatusCode(500);
                throw depositException;
            }
            
            DataItem preparedDataSet;
            // setup deposit package
            depositPackage.setPackageFileName(uploadedFile.getFileName());
            depositPackage.setPackageType(Package.PackageType.ZIP);
            
            for (File file : unzippedFiles) {
                if (!file.isDirectory()) {
                    // create a dataset for each file
                    preparedDataSet = new DataItem();
                    preparedDataSet.setId(idService.create(Types.DATA_SET.name()).getUrl().toString());
                    preparedDataSet.setParentId(currentCollectionId);
                    preparedDataSet.setDepositDate(new DateTime());
                    preparedDataSet.setDepositorId(currentUser.getId());
                    preparedDataSet.setName(file.getName());
                    
                    // add each file to its own dataset
                    try {
                        preparedDataSet.addFile(loadDataFile(preparedDataSet.getId(), file));
                    }
                    catch (IOException e) {
                        DepositException depositException = new DepositException(
                                messageKeys.getProperty(MSG_KEY_ERROR_READING_FILE), e);
                        depositException.setDepositedFile(uploadedFile);
                        depositException.setCollectionId(currentCollectionId);
                        depositException.setContainer(isContainer);
                        depositException.setDatasetName(uploadedFile.getFileName());
                        depositException.setHttpStatusCode(500);
                        depositException.setUserId(currentUserId);
                        throw depositException;
                    }
                    
                    // add each dataset to list
                    preparedDataSets.add(preparedDataSet);
                    // add dataset id and file name to deposit package
                    depositPackage.addFile(preparedDataSet.getId(), file.getName());
                }
            }
        }
        
        final String parentDepositId = archiveService
                .listDepositInfo(currentCollectionId, ArchiveDepositInfo.Status.DEPOSITED).get(0).getDepositId();
        
        // Keep track of any datasets that fail deposit: File name to error message
        final Map<String, String> filesFailedDeposit = new HashMap<String, String>();
        
        try {
            // put depositPackage into database
            packageService.create(depositPackage);

            // if putting the deposit package into the database succeeds, then
            // loop through datasets list
            // we track whether anything actually gets deposited
            int depositedDataItems = 0;
            for (DataItem ds : preparedDataSets) {
                // deposit each datasets, and we pass the parent deposit id to insure that
                // all deposited datasets end up in the same collection (imagine if the collection was
                // updated while this loop was executing, datasets would end up in different collections)
                // IF either the archive deposit or the relationship insertion fails, error message would be logged
                // and the loop will continue
                try {
                    depositDataSet(parentDepositId, ds);
                    //if this doesn't throw an exception, count it as a success
                    depositedDataItems++;
                }
                catch (ArchiveServiceException e) {
                    final String msg = "Error depositing DataItem " + ds + " to collection " + currentCollectionId
                            + ": " + e.getMessage();
                    filesFailedDeposit.put(ds.getFiles().get(0).getName(), msg);
                    log.error(msg, e);
                }
                catch (RelationshipConstraintException e) {
                    final String msg = "Error adding aggregation relationship for DataItem " + ds + " to collection "
                            + currentCollectionId + ": " + e.getMessage();
                    log.error(msg, e);
                }
            }
            if(depositedDataItems > 0){
                addMessage(getContext().getRequest(),
                        messageKeys.getProperty(MSG_KEY_NEW_DATA_AVAILABILITY_MESSAGE));
            }
            
        }
        catch (PackageException e) {
            log.error("Exception occurred trying to create deposit package" + depositPackage, e);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        // have the extractor delete temp files
        if (isContainer) {
            extractor.cleanUpExtractedPackage(new File(extractDirectory + "/" + uploadedFile.getFileName().substring(0, uploadedFile.getFileName().length()-4)));
        }
        
        // Throw a deposit exception if there were failed files.
        // TODO: Provide a way to pass in a list of failed files - currently this just passes the name of the zip
        // which is pretty useless.
        if (filesFailedDeposit.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> e : filesFailedDeposit.entrySet()) {
                sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
            final String msg = String.format(messageKeys.getProperty(MSG_KEY_ERROR_DEPOSITING_FILE), sb.toString());
            DepositException depositException = new DepositException(msg);
            depositException.setDepositedFile(uploadedFile);
            depositException.setCollectionId(currentCollectionId);
            depositException.setContainer(isContainer);
            depositException.setDatasetName(uploadedFile.getFileName());
            depositException.setHttpStatusCode(500);
            depositException.setUserId(currentUserId);
            throw depositException;
        }
        
    }
    
    /**
     * *****DO NOT delete***** This method returns a single prepared {@link org.dataconservancy.ui.model.DataItem} that
     * contains multiple file representations of the multiple files resulted from unpackaging an uploaded zip file.
     * 
     * @return
     * @throws IOException
     */
    private DataItem getPreparedDataSet(FileBean uploadedFileBean) throws Exception {
        DataItem preparedDataSet = new DataItem();
        if (dataItem.getId() == null)
            preparedDataSet.setId(idService.create(Types.DATA_SET.name()).getUrl().toString());
        else
            preparedDataSet.setId(dataItem.getId());
        preparedDataSet.setDepositDate(new DateTime());
        preparedDataSet.setParentId(this.currentCollectionId);
        
        Person currentUser = getAuthenticatedUser();
        preparedDataSet.setDepositorId(currentUser.getId());
        
        // dataset's name is the name of the uploaded file
        preparedDataSet.setName(uploadedFile.getFileName());
        List<File> extractedFiles = null;
        PackageExtractor extractor = null;
        try {
            //Check that the file is a package file
            Map<String, String> metadata = new HashMap<String, String>();
            metadata.put("Content-Disposition", "attachment; filename=\"" + uploadedFile.getFileName() + "\"");
            extractor = packageSelector.selectPackageExtractor(uploadedFile.getInputStream(), metadata);
            if (extractor == null) {
                UnpackException e = new UnpackException("File was not a recognized package type");
                e.setError("File was not a recognized package type");
                throw e;
            }
            extractor.setExtractDirectory(extractDirectory);
            extractedFiles = extractor.getFilesFromPackageFileBean(uploadedFileBean.getFileName(), uploadedFileBean);           
        }
        catch (UnpackException e) {
            log.error("Exception occurred trying to unpack files" + uploadedFileBean, e);
            throw e;
        }
        
        for (File file : extractedFiles) {
            preparedDataSet.addFile(loadDataFile(dataItem.getId(), file));
        }
        
        return preparedDataSet;
    }
    
    private void depositDataSet(String parentDepositId, DataItem dataItem) throws ArchiveServiceException,
            RelationshipConstraintException, BizInternalException, BizPolicyException {
        
        Collection currentCollection = getCollection(currentCollectionId);
        // Attempt to insert relationship for dataset and collection
        List<DataFile> files = null;
        try {
            // Add the relationships to the collection and data files
            relationshipService.addDataSetToCollection(dataItem, currentCollection);
            // After relationship is inserted, deposit dataset
            depositIds.add(archiveService.deposit(parentDepositId, dataItem));
            relationshipService.updateDataFileRelationshipForDataSet(dataItem);
        }
        catch (RelationshipConstraintException e) {
            // if relationship assertion fail: log and throw exception
            final String msg = "Error adding aggregation relationship for DataItem " + dataItem + " to collection "
                    + currentCollectionId + ": " + e.getMessage();
            relationshipService.removeDataSetFromCollection(dataItem, currentCollection);
            for (DataFile dataFile : dataItem.getFiles()) {
                relationshipService.removeDataFileFromDataSet((DataFile) dataFile, dataItem);
            }
            log.error(msg, e);
            throw e;
        }
        catch (ArchiveServiceException e) {
            // if deposit to archive fail, remove the relationship, log and throw exception
            relationshipService.removeDataSetFromCollection(dataItem, currentCollection);
            for (DataFile dataFile : dataItem.getFiles()) {
                relationshipService.removeDataFileFromDataSet((DataFile) dataFile, dataItem);
            }
            final String msg = "Error depositing the dataset " + dataItem + " into the archive";
            log.error(msg, e);
            throw e;
        }
    }
    
    public Resolution cancel() {
        if (redirectUrl.contains("usercollections.jsp")) {
            return returnToCollectionList(currentCollectionId);
        }
        else {
            return returnToCollectionView(currentCollectionId);
        }
    }
    
    private Resolution returnToCollectionDataList(String currentCollectionId) {
        RedirectResolution resolution = new RedirectResolution(CollectionDataListActionBean.class, "renderResults");
        resolution.addParameter("currentCollectionId", currentCollectionId);
        return resolution;
    }
    
    private Resolution returnToCollectionView(String currentCollectionId) {
        RedirectResolution res = new RedirectResolution(UserCollectionsActionBean.class, "viewCollectionDetails");
        res.addParameter("selectedCollectionId", currentCollectionId);
        res.flash(this);
        return res;
    }
    
    private Resolution returnToCollectionList(String currentCollectionId) {
        RedirectResolution res = new RedirectResolution(UserCollectionsActionBean.class, "render");
        res.addParameter("selectedCollectionId", currentCollectionId);
        res.flash(this);
        return res;
    }
    
    @SuppressWarnings("unchecked")
    public void addMessage(HttpServletRequest request, String message) {
        FlashScope fs = FlashScope.getCurrent(request, true);
        List<String> messages = (List<String>) fs.get("inform");
        if (messages == null) {
            messages = new ArrayList<String>();
            fs.put("inform", messages);
        }
        messages.add(message);
    }
    
    private DataFile loadDataFile(String parentId, File incomingFile) throws IOException, MalformedURLException {
        try {
            DataFile dataFile = new DataFile();
            // incomingFile = File.createTempFile("datafile", null);
            // uploadedFile.save(incomingFile);
            dataFile.setId(idService.create(Types.DATA_FILE.name()).getUrl().toString());
            dataFile.setParentId(parentId);
            dataFile.setSource(incomingFile.toURI().toURL().toExternalForm());
            dataFile.setName(incomingFile.getName());
            
            Resource r = new UrlResource(dataFile.getSource());
            dataFile.setSize(r.contentLength());
            dataFile.setFormat(fileBizService.getMimeType(r.getFile()));
            // Get the unpack directory and remove that from the parent of the file.
            int fileExtensionOffset = 4;
            if (uploadedFile.getFileName().endsWith(".gz")) {
                fileExtensionOffset = 7;
            } else if (uploadedFile.getFileName().endsWith(".gzip")) {
                fileExtensionOffset = 8;
            }
            String unpackDirectory = extractDirectory + "/" + uploadedFile.getFileName().substring(0, uploadedFile.getFileName().length()-fileExtensionOffset);

            String fileParent = incomingFile.getParent().substring(unpackDirectory.length());
            dataFile.setPath(fileParent);
            
            return dataFile;
        }
        catch (IOException e) {
            log.error("Failed during file upload", e);
            throw e;
        }
    }
    
    // For use of card DC742 that only requires 1 file deposit
    private DataFile loadDataFile(String parentId, FileBean uploadedFile) throws IOException {
        DataFile dataFile = null;
        
        try {
            File tmp = File.createTempFile("metadatafile", null);
            tmp.deleteOnExit();
            uploadedFile.save(tmp);
            dataFile = new DataFile();
            dataFile.setId(idService.create(Types.DATA_FILE.name()).getUrl().toString());
            dataFile.setSource(tmp.toURI().toURL().toExternalForm());
            dataFile.setName(uploadedFile.getFileName());
            dataFile.setParentId(parentId);
            
            Resource r = new UrlResource(dataFile.getSource());
            dataFile.setSize(r.contentLength());
            dataFile.setFormat(fileBizService.getMimeType(r.getFile()));
        }
        catch (IOException e) {
            log.error("Failed during file upload", e);
            throw e;
        }
        return dataFile;
    }
    
    public Collection getCollection(String objectId) throws BizInternalException, BizPolicyException {
        return collectionBizService.getCollection(objectId, getAuthenticatedUser());
    }
    
    private void sendNotification(Package dataPackage) {
        final EventContext eventContext = getEventContext();
        eventContext.setEventClass(EventClass.AUDIT);
        eventManager.fire(eventContext, new DataItemDepositEvent(eventContext, dataPackage));
    }
    
    @SpringBean("archiveService")
    public void injectArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }
    
    /**
     * Stripes-injected RelationshipService
     * 
     * @param relationshipService
     */
    @SpringBean("relationshipService")
    public void injectRelationshipService(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }
    
    /**
     * Stripes-injected IdService
     * 
     * @param idService
     */
    @SpringBean("uiIdService")
    private void injectIdService(IdService idService) {
        this.idService = idService;
    }
    
    @SpringBean("dcsUiBaseUrlConfig")
    private void injectUiBaseUrlConfig(UiBaseUrlConfig uiBaseUrlConfig) {
        this.uiBaseUrlConfig = uiBaseUrlConfig;
    }
    
    @SpringBean("packageService")
    public void injectPackageService(PackageService packageService) {
        this.packageService = packageService;
    }
    
    @SpringBean("collectionBizService")
    public void injectCollectionBizService(CollectionBizService collectionBizService) {
        this.collectionBizService = collectionBizService;
    }
    
    @SpringBean("fileBizService")
    public void injectFileBizService(FileBizService fileBizService) {
        this.fileBizService = fileBizService;
    }

    @SpringBean("authorizationService")
    public void injectAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }
    
    @SpringBean("packageSelector")
    public void injectPackageSelector(PackageSelector packageSelector) {
        this.packageSelector = packageSelector;
    }
    
    @SpringBean("extractDirectory")
    public void injectExtractDirectory(String extractDirectory) {
        this.extractDirectory = extractDirectory;
    }
    
    public Collection getCurrentCollection() throws CollectionException, BizInternalException, BizPolicyException,
            ArchiveServiceException {
        return getCollection(currentCollectionId);
    }
    
    public Project getProjectForCurrentCollection() throws CollectionException, BizInternalException,
            BizPolicyException, ArchiveServiceException {
        Project project = null;
        Collection currentCollection = getCurrentCollection();
        if (currentCollection != null && currentCollection.getId() != null && !currentCollection.getId().isEmpty()) {
            try {
                project = relationshipService.getProjectForCollection(currentCollection);
            }
            catch (RelationshipConstraintException e) {
                String msg = String.format(MSG_KEY_PROJECT_FOR_COLLECTION_NOT_FOUND, currentCollection.getId());
                log.error(msg);
                CollectionException ce = new CollectionException(msg);
                throw ce;
            }
        }
        
        return project;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
    
}
