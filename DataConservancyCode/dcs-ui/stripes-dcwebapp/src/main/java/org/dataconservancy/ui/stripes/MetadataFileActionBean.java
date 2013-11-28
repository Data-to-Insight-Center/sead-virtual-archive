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

import java.io.File;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.dao.DisciplineDAO;
import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.events.ExceptionEvent;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.CollectionException;
import org.dataconservancy.ui.exceptions.UnknownIdentifierTypeException;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.MetadataFormatProperties;
import org.dataconservancy.ui.model.MetadataResult;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.MetadataBizService;
import org.dataconservancy.ui.services.MetadataFileBizService;
import org.dataconservancy.ui.services.MetadataFormatService;
import org.dataconservancy.ui.services.RelationshipException;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.mhf.model.builder.xstream.AttributeFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ARCHIVE_PROBLEM;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_EMPTY_OR_INVALID_ID;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_METADATA_FILE_UPLOAD_FAIL;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_METADATA_RELATIONSHIP_FAIL;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_NO_COLLECTION_FOR_OBJECT_ID;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_SESSION_LOGGED_OUT;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_UPDATING_COLLECTION;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_ADDING_METADATA_FILE;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_VALIDATOR_ENTRY_NOT_FOUND;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_FILE_ALREADY_VALIDATED;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_FORMAT_NO_VALIDATION;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_FORMAT_NOT_AVAILABLE;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_METADATA_ATTRIBUTE_PARSE_FAIL;

/**
 * {@code MetaDataActionBean} handles operations in creating, retrieving, updating, and deleting {@link MetadataFile} via the dcs-ui.
 */
@UrlBinding("/metadatafile/metadata_file.action")
public class MetadataFileActionBean extends BaseActionBean {
    
    /**
     * The path to the jsp used to add and edit metadata files.  Package-private for unit test access.
     */
    static final String METADATA_FILE_ADD_PATH = "/pages/metadata_file_add_edit.jsp";

    /**
     * The path to the JSP that displays the metadata file ingest preview.
     */
    static final String METADATA_FILE_INGEST_PREVIEW_PAGE = "/pages/metadata_file_ingest_preview.jsp";
    
    /**
     * The path to the JSP that displays the metadata file ingest preview.
     */
    private static final String METADATA_FILE_DEPOSIT_ERROR_PAGE = "/pages/metadata_file_deposit_errors.jsp";
    
    /**
     * HTTP session key that contains the current parent object
     */
    private static final String PARENT_SESSION_KEY = "parent";
    
    /**
     * HTTP session key that contains the current metadata file to be added. 
     * This will persist the file object from first addition through the preview so it can be saved.
     */
    private static final String METADATA_FILE_SESSION_KEY = "metadataFile";
    
    /**
     * Http session key that stores whether or not we are performing an update.
     */
    private static final String METADATA_UPDATE_SESSION_KEY = "isUpdate";
    
    private MetadataFile metadataFile;
    private FileBean uploadedFile;
    
    private MetadataResult validationResult;
    private MetadataResult extractionResult;
    private List<List<String>> extractedSpatialAttributes;
    private List<String> extractedTemporalAttributes;
    private List<List<String>> extractedTemporalRangeAttributes;

    private AttributeFormatUtil attributeFormatUtil = new AttributeFormatUtil();
    
    private String parentID;
    
    private String redirectUrl;
    private String metadataFileID;
    
    private String message;
    
    //The id of the selected
    private String discipline;
    
    private Object parent;
    
    private DisciplineDAO disciplineDAO;  
    
    private boolean errorFlag;
   
    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Identifier service, used to generate business ids for created collections
     */
    private IdService idService;
    
    /**
     * The relationship service is used to retrieve the metadata formats
     */
    private RelationshipService relationshipService;

    /**
     * The metadata format service to retrieve metadata format objects from their id.
     */
    private MetadataFormatService metadataFormatService;

    /**
     * The metadata biz service handles extracting metadata from the file.
     */
    private MetadataBizService metadataBizService;
    
    /**
     * The metadata file biz service handles validating, indexing and archiving the metadata file.
     */
    private MetadataFileBizService metadataFileBizService;
    
    /**
     * The archive service used to retrieve the parent of the metadata file.
     */
    private ArchiveService archiveService;
    
    /**
     * The collection biz service used to update the collection after a metadata file has been added/changed
     */
    private CollectionBizService collectionBizService;

    public MetadataFileActionBean() {
        super();
       // Ensure desired properties are available.
       try {
            assert(messageKeys.containsKey(MSG_KEY_SESSION_LOGGED_OUT));
            assert(messageKeys.containsKey(MSG_KEY_METADATA_FILE_UPLOAD_FAIL));
            assert(messageKeys.containsKey(MSG_KEY_ARCHIVE_PROBLEM));
            assert(messageKeys.containsKey(MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED));
            assert(messageKeys.containsKey(MSG_KEY_NO_COLLECTION_FOR_OBJECT_ID));
            assert(messageKeys.containsKey(MSG_KEY_EMPTY_OR_INVALID_ID));
            assert(messageKeys.containsKey(MSG_KEY_ERROR_UPDATING_COLLECTION));
            assert(messageKeys.containsKey(MSG_KEY_ERROR_ADDING_METADATA_FILE));
            assert(messageKeys.containsKey(MSG_KEY_VALIDATOR_ENTRY_NOT_FOUND));
            assert(messageKeys.containsKey(MSG_KEY_FILE_ALREADY_VALIDATED));
            assert(messageKeys.containsKey(MSG_KEY_FORMAT_NO_VALIDATION));
            assert(messageKeys.containsKey(MSG_KEY_FORMAT_NOT_AVAILABLE));
            assert(messageKeys.containsKey(MSG_KEY_METADATA_ATTRIBUTE_PARSE_FAIL));
        }
        catch (AssertionError e) {
            throw new RuntimeException("MetadataFileActionBean missing message key.");
        }
    }
    
    /**
     * Render entry form for the collection's metadata file
     * @throws UnknownIdentifierTypeException 
     */
    @DefaultHandler
    public Resolution displayMetadataFileForm() throws UnknownIdentifierTypeException {
        try {           
             parent = retrieveParent();  
             metadataFile = getMetadataFileFromSession();
        } catch (UnknownIdentifierTypeException e) {
            log.warn("Error retrieving parent: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (ArchiveServiceException e) {
            log.error("Error retrieving parent from the archive: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (BizPolicyException e) {
            if (e.getType() == BizPolicyException.Type.AUTHENTICATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_SESSION_LOGGED_OUT);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(401, msg);
            } else if (e.getType() == BizPolicyException.Type.AUTHORIZATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(403, msg);
            }
        } catch (BizInternalException e) {
            log.error("Error updating collection: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        }
        
        if (metadataFileID != null && !metadataFileID.isEmpty()) {
            try {
                loadExistingMetadataFile();
            } catch (RelationshipException e) {
                log.error("Error loading metadata file: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_METADATA_RELATIONSHIP_FAIL);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(500, msg);
            }
            setIsUpdate(true);
        } else {
            setIsUpdate(false);
        }
            
        return new ForwardResolution(METADATA_FILE_ADD_PATH);
    }
    
    public Resolution deleteMetadataFile() throws RelationshipException {
        try {
            parent = retrieveParent();
            metadataFile = metadataFileBizService.retrieveMetadataFile(metadataFileID);
        } catch (UnknownIdentifierTypeException e) {
            log.warn("Error retrieving parent: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (ArchiveServiceException e) {
            log.error("Error retrieving parent from the archive: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        }  catch (BizPolicyException e) {
            if (e.getType() == BizPolicyException.Type.AUTHENTICATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_SESSION_LOGGED_OUT);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(401, msg);
            } else if (e.getType() == BizPolicyException.Type.AUTHORIZATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(403, msg);
            }
        } catch (BizInternalException e) {
            log.error("Error updating collection: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        }
                
        if (parent instanceof Collection) {//this should work for any business object now
           try{
              metadataFileBizService.removeMetadataFile((BusinessObject)parent, metadataFile, getAuthenticatedUser());
            } catch (BizPolicyException e) {
                 if (e.getType() == BizPolicyException.Type.AUTHENTICATION_ERROR) {
                    log.warn("Biz policy exception: " + e.getMessage(), e);
                    String s = messageKeys.getProperty(MSG_KEY_SESSION_LOGGED_OUT);
                    final String msg = String.format(s, e.getMessage());
                    return new ErrorResolution(401, msg);
                } else if (e.getType() == BizPolicyException.Type.AUTHORIZATION_ERROR) {
                    log.warn("Biz policy exception: " + e.getMessage(), e);
                    String s = messageKeys.getProperty(MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED);
                    final String msg = String.format(s, e.getMessage());
                    return new ErrorResolution(403, msg);
                 }
            } catch (BizInternalException e) {
                log.error("Error updating collection: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(500, msg);
            }
            RedirectResolution resolution = new RedirectResolution(UserCollectionsActionBean.class, getRedirectUrl());
            resolution.addParameter("selectedCollectionId", ((Collection) parent).getId());
            return resolution;
        }
        
        return new RedirectResolution(this.getClass());
    }

    /**
     * Preview results of metadata validation and extraction of newly entered collection's
     * metadata file before saving the new metadata file.
     */
    public Resolution previewMetadataIngest() {
        try {
            parent = retrieveParent();
            if (metadataFileID != null && !metadataFileID.isEmpty()) {
                setIsUpdate(true);
            } else {
                setIsUpdate(false);
            }
            this.addMetadataFile();
        } catch (BizPolicyException e) {
            if (e.getType() == BizPolicyException.Type.AUTHENTICATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_SESSION_LOGGED_OUT);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(401, msg);
            } else if (e.getType() == BizPolicyException.Type.AUTHORIZATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(403, msg);
            }
        } catch (BizInternalException e) {
            log.error("Error updating collection: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (UnknownIdentifierTypeException e) {
            log.warn("Error retrieving parent: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (ArchiveServiceException e) {
            log.error("Error retrieving parent from the archive: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (CollectionException e) {
            log.error("Error updating collection: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        }

        setParentOnSession(parent);
        
        //This seems silly to pass the values back through as params      
        ForwardResolution forwardResolution = new ForwardResolution(METADATA_FILE_INGEST_PREVIEW_PAGE);
        forwardResolution.addParameter("redirectUrl", redirectUrl);
        forwardResolution.addParameter("parentID", parentID);
        forwardResolution.addParameter("metadataFileID", getMetadataFileID());

        return forwardResolution;
    }

    public Resolution displayDepositErrors() {
        try {
            parent = retrieveParent();
            if (metadataFileID != null && !metadataFileID.isEmpty()) {
                setIsUpdate(true);
            }
            else {
                setIsUpdate(false);
            }
        }
        catch (BizPolicyException e) {
            if (e.getType() == BizPolicyException.Type.AUTHENTICATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_SESSION_LOGGED_OUT);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(401, msg);
            }
            else if (e.getType() == BizPolicyException.Type.AUTHORIZATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(403, msg);
            }
        }
        catch (BizInternalException e) {
            log.error("Error updating collection: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        }
        catch (UnknownIdentifierTypeException e) {
            log.warn("Error retrieving parent: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        }
        catch (ArchiveServiceException e) {
            log.error("Error retrieving parent from the archive: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        }

        setParentOnSession(parent);
        ForwardResolution forwardResolution = new ForwardResolution(METADATA_FILE_DEPOSIT_ERROR_PAGE);
        forwardResolution.addParameter("redirectUrl", redirectUrl);
        forwardResolution.addParameter("parentID", parentID);
        forwardResolution.addParameter("metadataFileID", getMetadataFileID());
        return forwardResolution;
    }

    /**
     * Save newly entered collection's metadata file and return to the main
     * collection add page. This is somewhat of a complicated work flow. If it's a new metadata file, we save
     * the metadata file, then we need to update the collection after the file has been deposited. If it's an updated metadata file
     * we just need to update the metadata file. 
     */
    public Resolution saveAndDoneMetadataFile() {
        try {
            parent = retrieveParent();            
            saveMetadataFile(false);
        } catch (BizPolicyException e) {
            if (e.getType() == BizPolicyException.Type.AUTHENTICATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_SESSION_LOGGED_OUT);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(401, msg);
            } else if (e.getType() == BizPolicyException.Type.AUTHORIZATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(403, msg);
            }
        } catch (BizInternalException e) {
            log.error("Error updating collection: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (UnknownIdentifierTypeException e) {
            log.warn("Error retrieving parent: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (ArchiveServiceException e) {
            log.error("Error retrieving parent from the archive: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (CollectionException e) {
            log.error("Error updating collection: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        }
        if (errorFlag) {
            return displayDepositErrors();
        }
        else if (parent instanceof Collection) {
            RedirectResolution resolution = new RedirectResolution(UserCollectionsActionBean.class, getRedirectUrl());
            resolution.addParameter("selectedCollectionId", ((Collection) parent).getId());
            return resolution;
        }
        else {

            removeParentFromSession();
            removeMetadataFileFromSession();
            removeUpdateFromSession();
            return new RedirectResolution(this.getClass());
        }
    }

    /**
     * Save newly entered collection's metadata file and reload the metadata
     * page to add more
     */
    public Resolution saveAndAddMoreMetadataFile() {
        try {
            parent = retrieveParent();
            saveMetadataFile(true);
        } catch (BizPolicyException e) {
            if (e.getType() == BizPolicyException.Type.AUTHENTICATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_SESSION_LOGGED_OUT);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(401, msg);
            } else if (e.getType() == BizPolicyException.Type.AUTHORIZATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(403, msg);
            }
        } catch (BizInternalException e) {
            log.error("Error updating collection: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (CollectionException e) {
            log.error("Error updating collection: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (UnknownIdentifierTypeException e) {
            log.warn("Error retrieving parent: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (ArchiveServiceException e) {
            log.error("Error retrieving parent from the archive: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        }
        
        if (errorFlag) {
            return displayDepositErrors();
        }
        else {
            setMetadataFile(null);
            setMetadataFileID("");
            setParentOnSession(parent);
            removeMetadataFileFromSession();
            // This seems silly to pass the values back through as params
            RedirectResolution resolution = new RedirectResolution(getClass(), "displayMetadataFileForm");
            resolution.addParameter("redirectUrl", redirectUrl);
            resolution.addParameter("parentID", parentID);
            
            return resolution;
        }
    }
    
    public Resolution cancel() {

        try {
            Object parent = retrieveParent();

            if (getParentFromSession() != null) {
                collectionBizService.updateCollection((Collection) parent, getAuthenticatedUser());
                removeParentFromSession();
            }
            
            removeMetadataFileFromSession();
            removeUpdateFromSession();
            
            if (parent instanceof Collection) {
                RedirectResolution resolution = new RedirectResolution(UserCollectionsActionBean.class, getRedirectUrl());
                resolution.addParameter("selectedCollectionId", ((Collection) parent).getId());

                return resolution;
            }
            
        } catch (UnknownIdentifierTypeException e) {
            log.warn("Error retrieving parent: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (ArchiveServiceException e) {
            log.error("Error retrieving parent from the archive: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        } catch (BizPolicyException e) {
            if (e.getType() == BizPolicyException.Type.AUTHENTICATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_SESSION_LOGGED_OUT);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(401, msg);
            } else if (e.getType() == BizPolicyException.Type.AUTHORIZATION_ERROR) {
                log.warn("Biz policy exception: " + e.getMessage(), e);
                String s = messageKeys.getProperty(MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED);
                final String msg = String.format(s, e.getMessage());
                return new ErrorResolution(403, msg);
            }
        } catch (BizInternalException e) {
            log.error("Error updating collection: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        }
        return new RedirectResolution(this.getClass());
    }

    private boolean isMetadataFileUpdate() {
        return getIsUpdate();
    }
    
    /**
     * Saves the uploaded metadata file to the temporary directory, and adds it to the Collection in the
     * HTTP session.
     * @throws BizInternalException
     * @throws BizPolicyException 
     */
    private void addMetadataFile() throws CollectionException {
        //If we're working with a new metadata file instantiate one.
        if (metadataFile == null) {
            metadataFile = new MetadataFile();
        }
        
        if (getUploadedFile() != null) {
            log.debug(getUploadedFile().getFileName());
            try {
                File tempMetadataContent = File.createTempFile("metadatafile", null);
                tempMetadataContent.deleteOnExit();
                getUploadedFile().save(tempMetadataContent);
                //If the name is being supplied by the file upload instead of the user it won't be set, so set it from the file upload.
                if (metadataFile.getName() == null || metadataFile.getName().isEmpty()) {
                   metadataFile.setName(getUploadedFile().getFileName()); 
                }
                metadataFile.setSource(tempMetadataContent.toURI().toURL().toExternalForm());
                Resource r = new UrlResource(metadataFile.getSource());
                metadataFile.setSize(r.contentLength());
                metadataFile.setFormat(metadataFileBizService.getMimeType(r.getFile()));
            } catch (IOException e) {
                String msg = messageKeys.getProperty(MSG_KEY_METADATA_FILE_UPLOAD_FAIL);
                msg = String.format(MSG_KEY_METADATA_FILE_UPLOAD_FAIL, getUploadedFile().getFileName());
                log.error(msg, e);
                CollectionException ace = new CollectionException(msg);
                throw ace;
            }
        } else if (metadataFile.getSource() == null || metadataFile.getSource().isEmpty()) {
            //We're not updating an existing file and no file was provided throw an exception
            if( metadataFile == null) {
                String msg = messageKeys.getProperty(MSG_KEY_ERROR_ADDING_METADATA_FILE);
                msg = String.format(MSG_KEY_ERROR_ADDING_METADATA_FILE, metadataFile, parent, "File was not uploaded.");
                log.error(msg);
                CollectionException ace = new CollectionException(msg);
                throw ace;
            }

        } else if (getIsUpdate()) {
            //We're updating a file in the archive set it's source to null so we don't re-deposit the file from the archive.
            metadataFile.setSource(null);
        }            
        
        boolean isUpdate = isMetadataFileUpdate();
        //We're updating a file that already exists.
        if (isUpdate) {
            metadataFile.setId(metadataFileID);
        } else if (metadataFile.getId() == null) {
            //This is a new file that needs to be added. 
            metadataFile.setId(idService.create(Types.METADATA_FILE.name()).getUrl().toString());
        }
        
        if (isUpdate && getUploadedFile() == null) {
            message = String.format(messageKeys.getProperty(MSG_KEY_FILE_ALREADY_VALIDATED), metadataFile.getName());
        } else {
            MetadataFormatProperties formatProperties = metadataFormatService.getProperties(metadataFile.getMetadataFormatId());
            
            DcsMetadataFormat selectedFormat = metadataFormatService.getMetadataFormat(metadataFile.getMetadataFormatId());            
            String formatName = metadataFile.getMetadataFormatId();            
            if (selectedFormat != null) {
                formatName = selectedFormat.getName();
            }
            
            if (formatProperties != null) {
                if (formatProperties.isValidates()) {
                    validationResult = metadataBizService.validateMetadata(metadataFile);
                    
                    if (!validationResult.getValidationPeformed()) {
                        message = String.format(messageKeys.getProperty(MSG_KEY_VALIDATOR_ENTRY_NOT_FOUND), formatName);
                        sendNotification(metadataFile);
                    }
                    
                    //If the file passes validation do extraction
                    if (!validationResult.hasErrors()) {
                        extractionResult = metadataBizService.extractMetadata(metadataFile);
                        try {
                            parseExtractedAttributes(extractionResult.getAttributeSets());
                        } catch (UnsupportedEncodingException e) {
                            message = String.format(messageKeys.getProperty(MSG_KEY_METADATA_ATTRIBUTE_PARSE_FAIL), metadataFile.getName());
                            log.warn(message + ": " + e.getMessage(), e);
                        }
                    }
                } else {
                    message = String.format(messageKeys.getProperty(MSG_KEY_FORMAT_NO_VALIDATION), formatName);
                }
            } else {
                message = String.format(messageKeys.getProperty(MSG_KEY_VALIDATOR_ENTRY_NOT_FOUND), formatName);
                sendNotification(metadataFile);
            }
        }
        //Stores the metadata file on the session
        setMetadataFileOnSession(metadataFile);           
    }
    
    private void sendNotification(MetadataFile metadataFile) {
        final EventContext eventContext = getEventContext();
        eventContext.setEventClass(EventClass.EXCEPTION);
        RuntimeException re = new RuntimeException("Error peforming validation for " + metadataFile.getMetadataFormatId() + " either the validator or the properties could not be found");
        re.setStackTrace(Thread.currentThread().getStackTrace());
        eventManager.fire(eventContext, new ExceptionEvent(eventContext, re));
    }
    
    private void parseExtractedAttributes(Set<AttributeSet> attributeSets) throws UnsupportedEncodingException {
        extractedSpatialAttributes = new ArrayList<List<String>>();
        extractedTemporalAttributes = new ArrayList<String>();
        extractedTemporalRangeAttributes = new ArrayList<List<String>>();
        for (AttributeSet attributeSet : attributeSets) {
            for (Attribute attribute : attributeSet.getAttributes()) {
                if (attribute.getType().equalsIgnoreCase("DateTime")) {
                    extractedTemporalAttributes.add(this.attributeFormatUtil.formatDateTimeAttributeForPreview(attribute));
                } else if (attribute.getType().equalsIgnoreCase("DateTimeRange")) {
                    extractedTemporalRangeAttributes.add(this.attributeFormatUtil.formatDateTimeRangeAttributeForPreview(attribute));
                } else if (attribute.getType().equalsIgnoreCase("Location")) {
                    extractedSpatialAttributes.add(this.attributeFormatUtil.formatLocationAttributeForPreview(attribute));
                }
            }
        }
    }

    /**
     * Saves the uploaded metadata file to the temporary directory, and adds it to the Collection in the
     * HTTP session.
     * @param cacheUpdate true if the update should just be stored on the project and not pushed to the biz service
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    private void saveMetadataFile(boolean cacheUpdate) throws CollectionException, BizPolicyException,
            BizInternalException {
        //Retrieve the metadata file from the session
        metadataFile = getMetadataFileFromSession();
        
        // If we don't have a metadatafile, throw an exception as this is the
        // second step in the wizard.
        if( metadataFile == null) {
            String msg = messageKeys.getProperty(MSG_KEY_ERROR_ADDING_METADATA_FILE);
            msg = String.format(MSG_KEY_ERROR_ADDING_METADATA_FILE, metadataFile, parent, "File was not uploaded.");
            log.error(msg);
            CollectionException ace = new CollectionException(msg);
            throw ace;
        }

        try {
            metadataFileBizService.addNewMetadataFile((BusinessObject) parent, metadataFile, getAuthenticatedUser());
        } catch (BizInternalException e) {
            log.warn("Could not add metadata file to parent: " + e.getMessage(), e);
            String msg = messageKeys.getProperty(MSG_KEY_ERROR_ADDING_METADATA_FILE);
            msg = String.format(MSG_KEY_ERROR_ADDING_METADATA_FILE, metadataFile, parent, e);
            log.error(msg, e);

            // Setting message with the exception thrown and its cause to display on the error page.
            message = e.getMessage() + " <br/><br/> Cause: " + e.getCause().getMessage();
            errorFlag = true;
        }

        removeMetadataFileFromSession();
    }

    private void loadExistingMetadataFile() throws RelationshipException {
        if (parent instanceof Collection) {
            for ( String fileId : relationshipService.getMetadataFileIdsForBusinessObjectId(((Collection) parent).getId()) ) {
                if (fileId.equalsIgnoreCase(metadataFileID)) {
                    try {
                        metadataFile = metadataFileBizService.retrieveMetadataFile(fileId);
                    } catch (ArchiveServiceException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                    break;
                }
            }
        } else if (parent instanceof DataItem) {
            
        } 
    }
    
    private Object retrieveParent() throws UnknownIdentifierTypeException, ArchiveServiceException, BizPolicyException, BizInternalException {
        Object parent = null;
        parent = getParentFromSession();
        if (parent == null) {
            // Figure out the type of the identifier
            Identifier id = null;
            Types idType = null;
            try {
                try {
                    id = idService.fromUrl(new URL(getParentID()));
                } catch (MalformedURLException e) {
                    // the objectId may not be in the form of a URL, it may be a UID.
                    id = idService.fromUid(parentID);
                }
                if (id != null) {
                    idType = Types.valueOf(id.getType());
                }
            } catch (Exception e) {
                UnknownIdentifierTypeException uite = new UnknownIdentifierTypeException("Unknown ID [" + parentID + "] " +
                        "type [" + ((idType == null) ? "Unknown" : idType.name()) + "]", e);
                throw uite;
            }
    
            //Poll the archive to update the deposit info.
            archiveService.pollArchive();
                
            if (idType != null) {
                switch (idType) {
        
                    case COLLECTION:
                    {
                        parent = collectionBizService.getCollection(parentID, getAuthenticatedUser());
                        break;
                    }
                    case DATA_SET:
                    {
                        break;
                    }
                    
                }
            }
        }
        return parent;
    }
    
    public MetadataFile getMetadataFile() {
        //If the metadata file is null check to see if there is one on the session.
        if (metadataFile == null) {
            getMetadataFileFromSession();
        }
        
        //If there isn't a metadata file on the session stripes will instantiate this.
        return metadataFile;
    }
    
    public void setMetadataFile(MetadataFile file) {
        this.metadataFile = file;
    }
    
    public MetadataFile getMetadataFileFromSession() {
        HttpSession ses = getContext().getRequest().getSession();
        metadataFile = (MetadataFile) ses.getAttribute(METADATA_FILE_SESSION_KEY);
        return metadataFile;
    }

    public void setMetadataFileOnSession(MetadataFile metadataFile) {
        HttpSession ses = getContext().getRequest().getSession();
        ses.setAttribute(METADATA_FILE_SESSION_KEY, metadataFile);
        this.metadataFile = metadataFile;
    }
    
    public void removeMetadataFileFromSession() {
        HttpSession ses = getContext().getRequest().getSession();
        ses.removeAttribute(METADATA_FILE_SESSION_KEY);
    }
    
    public boolean getIsUpdate() {
        HttpSession ses = getContext().getRequest().getSession();
        return (Boolean) ses.getAttribute(METADATA_UPDATE_SESSION_KEY);
    }
    
    public void setIsUpdate(boolean update) {
        HttpSession ses = getContext().getRequest().getSession();
        ses.setAttribute(METADATA_UPDATE_SESSION_KEY, update);        
    }
    
    public void removeUpdateFromSession() {
        HttpSession ses = getContext().getRequest().getSession();
        ses.removeAttribute(METADATA_UPDATE_SESSION_KEY);
    }
    public void setUploadedFile(FileBean uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public FileBean getUploadedFile() {
        return uploadedFile;
    }
    
    public String getParentID() {
        return parentID;
    }
    
    public void setParentID(String id) {
        parentID = id;
    }
    
    public void setParentOnSession(Object parent) {
        HttpSession ses = getContext().getRequest().getSession();
        ses.setAttribute(PARENT_SESSION_KEY, parent);
    }
    
    public Object getParentFromSession() {
        HttpSession ses = getContext().getRequest().getSession();
        Object parent = (Object) ses.getAttribute(PARENT_SESSION_KEY);
        return parent;
    }
    
    public void removeParentFromSession() {
        HttpSession ses = getContext().getRequest().getSession();
       ses.removeAttribute(PARENT_SESSION_KEY);
    }
    
    public MetadataResult getValidationResult() {
        return validationResult;
    }
    
    public void setValidationResult(MetadataResult validationResult) {
        this.validationResult = validationResult; 
    }
    
    public MetadataResult getExtractionResult() {
        return extractionResult;
    }

    public void setExtractionResult(MetadataResult extractionResult) {
        this.extractionResult = extractionResult;
    }

    public List<List<String>> getExtractedSpatialAttributes() {
        return extractedSpatialAttributes;
    }
    
    public void setExtractedSpatialAttributes(List<List<String>> attributes) {
        this.extractedSpatialAttributes = attributes;
    }
    
    public List<String> getExtractedTemporalAttributes() {
        return extractedTemporalAttributes;
    }
    
    public void setExtractedTemporalAttributes(List<String> attributes) {
        this.extractedTemporalAttributes = attributes;
    }

    public List<List<String>> getExtractedTemporalRangeAttributes() {
        return extractedTemporalRangeAttributes;
    }

    public void setExtractedTemporalRangeAttributes(List<List<String>> extractedTemporalRangeAttributes) {
        this.extractedTemporalRangeAttributes = extractedTemporalRangeAttributes;
    }
    
    public String getDiscipline() {
        return discipline;
    }
    
    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }
    
    /**
     * Obtain a list of all Disciplines supported by this instance of the DC User Interface.
     *
     * @return the Discipline List
     */
    public List<Discipline> getDisciplines() {
        return disciplineDAO.list();
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setRedirectUrl(String url) {
        redirectUrl = url;
    }
    
    public String getMetadataFileID() {
        return metadataFileID;
    }
    
    public void setMetadataFileID(String id) {
        this.metadataFileID = id;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isErrorFlag() {
        return errorFlag;
    }
    
    public void setErrorFlag(boolean errorFlag) {
        this.errorFlag = errorFlag;
    }
    
    /**
     * Obtain a List of MetadataFormat objects for the supplied Discipline identifier.
     * 
     * @param disciplineId
     *            the identifier for the Discipline
     * @return a List of MetadataFormats for that Discipline
     */
    public List<DcsMetadataFormat> getMetadataFormatsForDiscipline(String disciplineId) {
        //TODO: call to MetadataFormatService
        
        return null;
    }

    /**
     * Obtain a Lists of MetadataFormat objects keyed by their associated discipline identifier.
     *
     * @return a List of MetadataFormats for each discipline
     */
    public Map<String, List<DcsMetadataFormat>> getMetadataFormatsWithDiscipline() {
        final Map<String, List<DcsMetadataFormat>> results = new HashMap<String, List<DcsMetadataFormat>>();

        for (Discipline d : disciplineDAO.list()) {
            final String disciplineId = d.getId();
            List<DcsMetadataFormat> metadataFormats = new ArrayList<DcsMetadataFormat>(
                    metadataFormatService.getMetadataFormatsForDiscipline(disciplineId, true));
            results.put(d.getId(), metadataFormats);
        }

        return results;
    }

    public List<DcsMetadataFormat> getBiology(){        
        return getMetadataFormatsForDiscipline("dc:discipline:Biology");
    }
    
    public List<DcsMetadataFormat> getAstronomy(){
        return getMetadataFormatsForDiscipline("dc:discipline:Astronomy");
    }
    
    public List<DcsMetadataFormat> getEarthScience(){
        return getMetadataFormatsForDiscipline("dc:discipline:EarthScience");
    }
    
    public List<DcsMetadataFormat> getNone(){
        return getMetadataFormatsForDiscipline("dc:discipline:None");
    }
    
    /**
     * Stripes-injected MetadataFormatService
     *
     * @param metadataFormatService
     */
    @SpringBean("metadataFormatService")
    public void injectMetadataFormatService(MetadataFormatService metadataFormatService){
        this.metadataFormatService = metadataFormatService;
    }
    
    /**
     * Stripes-injected IdService
     *
     * @param idService
     */
    @SpringBean("uiIdService")
    public void injectIdService(IdService idService) {
        this.idService = idService;
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
     * Stripes-injected DisciplineDAO
     *
     * @param disciplineDao
     */
    @SpringBean("disciplineDao")
    public void injectDisciplineDAO(DisciplineDAO disciplineDao) {
        disciplineDAO = disciplineDao;
    }
    
    /**
     * Stripes-injected ArchiveService
     */
    @SpringBean("archiveService")
    public void injectArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }
    
    /**
     * Stripes-inject CollectionBizService
     */
    @SpringBean("collectionBizService")
    public void injectCollectionBizService(CollectionBizService collectionBizService) {
        this.collectionBizService = collectionBizService;
    }
    
    /**
     * Stripes-inject MetadataFileBizService
     */
    @SpringBean("metadataFileBizService")
    public void injectMetadataFileBizService(MetadataFileBizService metadataFileBizService) {
        this.metadataFileBizService = metadataFileBizService;
    }
    
    /**
     * Stripes-inject MetadataBizService
     */
    @SpringBean("metadataBizService")
    public void injectMetadataBizService(MetadataBizService metadataBizService) {
        this.metadataBizService = metadataBizService;
    }
    
}
