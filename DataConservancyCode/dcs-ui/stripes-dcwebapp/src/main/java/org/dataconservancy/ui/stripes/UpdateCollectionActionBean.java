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

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_COULD_NOT_DEPOSIT_COLLECTION;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_METADATA_FILE_UPLOAD_FAIL;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_PROJECT_FOR_COLLECTION_NOT_FOUND;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_SESSION_LOGGED_OUT;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_SYS_OR_PROJECT_ADMIN_REQUIRED_TO_CREATE_COLLECTION;

import java.util.List;

import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.apache.http.HttpStatus;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.CollectionException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.ContactInfo;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.AuthorizationService;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.stripes.ext.JodaDateTimeTypeConverter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Action Bean for the collection business object. It handles rendering a a form to collection information
 * for updating of collection and the updating collections.
 */

@UrlBinding("/collection/collection_update.action")
public class UpdateCollectionActionBean extends BaseActionBean {
    
    /**
     * HTTP session key that contains the current Collection object
     */
    private static final String COLLECTION_SESSION_KEY = "updated_collection";
    
    /**
     * HTTP session key that contains the current Project identifier
     */
    private static final String PROJECT_ID_SESSION_KEY = "project_id";
    
    /**
     * The path to the jsp used to update collection information. Package-private for unit test access.
     */
    static final String COLLECTION_UPDATE_PATH = "/pages/collection_update.jsp";
    
    /**
     * The path to the jsp used to get contact information. Package-private for unit test access.
     */
    static final String COLLECTION_CONTACT_INFO_PATH = "/pages/collection_contact_info_add.jsp";
    
    /**
     * The path to the jsp used to get collection metadata. Package-private for unit test access.
     */
    static final String COLLECTION_METADATA_FILE_PATH = "/pages/collection_metadata_file_add.jsp";
    
    /**
     * The path to the jsp used to view collections. Package-private for unit test access.
     */
    static final String VIEW_COLLECTION_PATH = "/pages/collection_view.jsp";
    
    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * The archive service to poll for deposit status
     */
    private ArchiveService archiveService;
    
    /**
     * The authorization service is used to check user's permission for each operation
     */
    private AuthorizationService authorizationService;
    
    private FileBean uploadedFile;
    
    private RelationshipService relationshipService;
    
    /**
     * The Project identifier that this Collection is associated with.
     */
    private String projectId = "";
    
    private int selectedAlternateIdIndex = -1;
    
    private int selectedCreatorIndex = -1;
    
    private int selectedContactInfoIndex = -1;
    
    /**
     * Collection ID will be null if a Collection does not exist in the session. Package-private: only to be used by
     * unit tests.
     */
    private String collectionId;
    
    private Collection collection;
    
    @ValidateNestedProperties({
            @Validate(field = "name", required = true, on = { "saveAndDoneContactInfo", "saveAndAddMoreContactInfo" }),
            @Validate(field = "emailAddress", required = true, on = { "saveAndDoneContactInfo",
                    "saveAndAddMoreContactInfo" }, converter = EmailTypeConverter.class) })
    private ContactInfo contactInfo;
    
    @ValidateNestedProperties({ @Validate(field = "name", required = true, on = { "saveAndAddMoreMetadataFile",
            "saveAndDoneMetadataFile" }) })
    private MetadataFile metadataFile = new MetadataFile();
    
    private CollectionBizService collectionBizService;
    
    public UpdateCollectionActionBean() {
        super();
        // Ensure desired properties are available.
        try {
            assert (messageKeys.containsKey(MSG_KEY_SESSION_LOGGED_OUT));
            assert (messageKeys.containsKey(MSG_KEY_SYS_OR_PROJECT_ADMIN_REQUIRED_TO_CREATE_COLLECTION));
            assert (messageKeys.containsKey(MSG_KEY_COULD_NOT_DEPOSIT_COLLECTION));
            assert (messageKeys.containsKey(MSG_KEY_METADATA_FILE_UPLOAD_FAIL));
        }
        catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of " + MSG_KEY_SESSION_LOGGED_OUT + ", "
                    + MSG_KEY_SYS_OR_PROJECT_ADMIN_REQUIRED_TO_CREATE_COLLECTION + ", "
                    + MSG_KEY_METADATA_FILE_UPLOAD_FAIL + ", " + MSG_KEY_COULD_NOT_DEPOSIT_COLLECTION + " is missing.");
        }
        
    }
    
    // ///////////////////////
    //
    // Stripes Resolutions
    //
    // ///////////////////////
    
    /**
     * Displays the form used to update a Collection
     * 
     * @return the Resolution
     */
    @DefaultHandler
    @DontValidate
    public Resolution displayCollectionUpdateForm() throws BizInternalException, BizPolicyException,
            RelationshipConstraintException, ArchiveServiceException {
        Person currentUser = getAuthenticatedUser();
        collection = getCollection();
        if (collection == null) {
            return new ErrorResolution(HttpStatus.SC_NOT_FOUND, "Collection with id " + collectionId
                    + " could not be found.");
        }
        
        // render the udpate form
        return new ForwardResolution(COLLECTION_UPDATE_PATH);
    }
    
    /**
     * Deposit Collection into the archive via Archive Service
     * 
     * @return res
     * @throws CollectionException
     * @throws BizPolicyException
     * @throws BizInternalException
     * @throws ArchiveServiceException
     */
    public Resolution updateCollection() throws CollectionException, BizPolicyException, BizInternalException,
            ArchiveServiceException {
        Person currentUser = getAuthenticatedUser();
        // Check to make sure user is still logged in
        
        /*
         * if (currentUser == null) { final String msg = messageKeys.getProperty(MSG_KEY_SESSION_LOGGED_OUT);
         * log.info(msg); return new ErrorResolution(HttpStatus.SC_BAD_REQUEST, msg); }
         */
        
        // Check and cull empty creators
        // This can happen if the user added a creator field they ended up not using.
        List<PersonName> creators = getCollection().getCreators();
        if (creators != null && creators.size() > 0) {
            for (int i = creators.size() - 1; i >= 0; i--) {
                if (creators.get(i) == null || creators.get(i).isEmpty()) {
                    creators.remove(i);
                }
            }
        }
        
        // Check and cull empty alternate ids
        // This can happen if the user added a alternate id field they ended up not using.
        List<String> alternateIds = getCollection().getAlternateIds();
        if (alternateIds != null && alternateIds.size() > 0) {
            for (int i = alternateIds.size() - 1; i >= 0; i--) {
                if (alternateIds.get(i) == null || alternateIds.get(i).isEmpty()) {
                    alternateIds.remove(i);
                }
            }
        }
        
        collection = getCollection();
        if (collection == null) {
            return new ErrorResolution(HttpStatus.SC_NOT_FOUND, "Collection with id " + collectionId
                    + " could not be found.");
        }
        
        collection.setDepositDate(new DateTime());
        
        collectionId = collectionBizService.updateCollection(collection, currentUser);
        clearCollection();
        RedirectResolution res = new RedirectResolution(UserCollectionsActionBean.class, "viewCollectionDetails");
        res.addParameter("selectedCollectionId", collectionId);
        return res;
    }
    
    /**
     * Clear collection from the session. Abandon changes.
     */
    public Resolution cancel() {
        clearCollection();
        RedirectResolution res = new RedirectResolution(UserCollectionsActionBean.class, "viewCollectionDetails");
        res.addParameter("selectedCollectionId", collectionId);
        return res;
    }
    
    /**
     * Render entry form for the collection's contact info
     */
    public Resolution displayContactInfoForm() {
        ForwardResolution res = new ForwardResolution(COLLECTION_CONTACT_INFO_PATH);
        res.addParameter("collectionId", collectionId);
        return res;
    }
    
    /**
     * Save newly entered contact info for the collection and return to the main collection update page
     */
    public Resolution saveAndDoneContactInfo() throws BizInternalException, BizPolicyException, ArchiveServiceException {
        saveContactInfo();
        ForwardResolution res = new ForwardResolution(COLLECTION_UPDATE_PATH);
        res.addParameter("collectionId", collectionId);
        return res;
    }
    
    /**
     * Save newly entered contact info for the collection and reload contact info page to add more
     */
    public Resolution saveAndAddMoreContactInfo() throws BizInternalException, BizPolicyException,
            ArchiveServiceException {
        saveContactInfo();
        contactInfo = null;
        RedirectResolution res = new RedirectResolution(this.getClass(), "displayContactInfoForm");
        res.addParameter("collectionId", collectionId);
        return res;
    }
    
    /**
     * Remove current contact info from the collection
     */
    public Resolution deleteContactInfo() throws BizInternalException, BizPolicyException, ArchiveServiceException {
        List<ContactInfo> cis = getCollection().getContactInfoList();
        if (selectedContactInfoIndex >= 0 && selectedContactInfoIndex < cis.size()) {
            cis.remove(selectedContactInfoIndex);
        }
        RedirectResolution res = new RedirectResolution(this.getClass(), "displayCollectionUpdateForm");
        res.addParameter("collectionId", collectionId);
        return res;
    }
    
    /**
     * Remove current creator from the collection
     */
    public Resolution deleteCreator() throws BizInternalException, BizPolicyException, ArchiveServiceException {
        
        List<PersonName> creators = getCollection().getCreators();
        
        if (selectedCreatorIndex >= 0 && selectedCreatorIndex < creators.size()) {
            creators.remove(selectedCreatorIndex);
        }
        RedirectResolution res = new RedirectResolution(this.getClass(), "displayCollectionUpdateForm");
        res.addParameter("collectionId", collectionId);
        return res;
    }
    
    /**
     * Remove current contact info from the collection
     */
    public Resolution deleteAlternateId() throws BizInternalException, BizPolicyException, ArchiveServiceException {
        List<String> ids = getCollection().getAlternateIds();
        if (selectedAlternateIdIndex >= 0 && selectedAlternateIdIndex < ids.size()) {
            ids.remove(selectedAlternateIdIndex);
        }
        RedirectResolution res = new RedirectResolution(this.getClass(), "displayCollectionUpdateForm");
        res.addParameter("collectionId", collectionId);
        return res;
    }
    
    // ///////////////////////
    //
    // Private/helper methods
    //
    // ///////////////////////
    
    /**
     * Removes the Collection object from the HTTP session.
     */
    private void clearCollection() {
        HttpSession ses = getContext().getRequest().getSession();
        ses.removeAttribute(collectionId);
    }
    
    /**
     * Adds the contact information to the Collection that is in the HTTP session
     */
    private void saveContactInfo() throws BizInternalException, BizPolicyException, ArchiveServiceException {
        if (contactInfo != null) {
            getCollection().addContactInfo(contactInfo);
        }
    }
    
    /**
     * This is the collection that we are adding/viewing. If the Collection exists on the HTTP session, it is returned.
     * If no Collection exists on the session, a new Collection is created, a new ID is generated, and the Collection is
     * placed onto the session. In all cases, the {@link #collectionId} field of this bean is set.
     */
    @ValidateNestedProperties({ @Validate(field = "title", required = true, on = { "updateCollection" }),
            @Validate(field = "summary", required = true, on = { "updateCollection" }),
            @Validate(field = "publicationDate", converter = JodaDateTimeTypeConverter.class),
            @Validate(field = "creators[0].familyNames", required = true, on = { "updateCollection" }) })
    public Collection getCollection() throws ArchiveServiceException, BizInternalException, BizPolicyException {
        HttpSession ses = getContext().getRequest().getSession();
        
        Collection collection = (Collection) ses.getAttribute(collectionId);
        
        if (collection == null) {
            if (collectionId != null) {
                archiveService.pollArchive();
                collection = collectionBizService.getCollection(collectionId, getAuthenticatedUser());
                if (collection == null) {
                    final String msg = "Collection " + collectionId + " doesn't exist.";
                    log.debug(msg);
                    // return new ErrorResolution(HttpStatus.SC_NOT_FOUND, msg);
                }
                ses.setAttribute(collectionId, collection);
            }
            else {
                throw new RuntimeException("collection id cannot be null");
            }
        }
        
        if (collection != null) {
            collectionId = collection.getId();
        }
        return collection;
        
    }
    
    // ///////////////////////
    //
    // Accessors
    //
    // ///////////////////////
    
    /**
     * Package-private for unit testing
     * 
     * @return
     */
    String getCollectionId() {
        return collectionId;
    }
    
    public void setCollectionId(String id) {
        collectionId = id;
    }
    
    public ContactInfo getContactInfo() {
        return contactInfo;
    }
    
    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }
    
    public int getSelectedCreatorIndex() {
        return selectedCreatorIndex;
    }
    
    public void setSelectedCreatorIndex(int selectedCreatorIndex) {
        this.selectedCreatorIndex = selectedCreatorIndex;
    }
    
    public int getSelectedContactInfoIndex() {
        return selectedContactInfoIndex;
    }
    
    public void setSelectedContactInfoIndex(int selectedContactInfoIndex) {
        this.selectedContactInfoIndex = selectedContactInfoIndex;
    }
    
    public int getSelectedAlternateIdIndex() {
        return selectedAlternateIdIndex;
    }
    
    public void setSelectedAlternateIdIndex(int selectedAlternateIdIndex) {
        this.selectedAlternateIdIndex = selectedAlternateIdIndex;
    }
    
    public String getProjectId() {
        HttpSession ses = getContext().getRequest().getSession();
        projectId = (String) ses.getAttribute(PROJECT_ID_SESSION_KEY);
        return projectId;
    }
    
    public void setProjectId(String _projectId) {
        projectId = _projectId;
        HttpSession ses = getContext().getRequest().getSession();
        ses.setAttribute(PROJECT_ID_SESSION_KEY, projectId);
    }
    
    public Collection getCollection(String objectId) throws BizInternalException, BizPolicyException {
        return collectionBizService.getCollection(objectId, getAuthenticatedUser());
    }
    
    public Collection getCurrentCollection() throws CollectionException, BizInternalException, BizPolicyException,
            ArchiveServiceException {
        return getCollection(collectionId);
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
    
    /**
     * Polls the archive, and returns the status of the object identified by {@code businessId}. The returned status may
     * be {@code null}. If the status of the object is equal to
     * {@link org.dataconservancy.ui.model.ArchiveDepositInfo.Status#DEPOSITED}, this method immediately returns without
     * further polling.
     * <p/>
     * The archive is polled {@code times} number of times, waiting {@code delayFactorInMs * times} between each call to
     * {@link org.dataconservancy.ui.services.ArchiveService#pollArchive()}. For example, if this method is called with
     * a 1000ms delay factor, and times equal to 3, the first call to {@code ArchiveService#pollArchive()} will happen
     * with no delay. If the status is not equal to DEPOSITED, then the next call is delayed 1000ms. Subsequent calls
     * will be delayed by 2000ms and 3000ms, respectively.
     * 
     * @param delayFactorInMs
     *            used to calculate the delay in milliseconds between calls to {@code ArchiveService#pollArchive()}
     * @param times
     *            the number of times to poll the archive
     * @param businessId
     *            the business object identifier
     * @return the archival status of the business object, may be {@code null}
     */
    private ArchiveDepositInfo.Status poll(int delayFactorInMs, int times, String businessId) {
        ArchiveDepositInfo.Status status = null;
        int count = 0;
        do {
            final long sleepInterval = count * delayFactorInMs;
            log.debug("Polling archive ({}) {} of {} times; sleeping {} ms", new Object[] { businessId, count,
                    (times - 1), sleepInterval });
            try {
                Thread.sleep(sleepInterval);
                try {
                    archiveService.pollArchive();
                }
                catch (ArchiveServiceException e) {
                    log.error("Error polling archive: " + e.getMessage(), e);
                }
            }
            catch (InterruptedException e) {
                // ignore
            }
            List<ArchiveDepositInfo> info = archiveService.listDepositInfo(businessId,
                    ArchiveDepositInfo.Status.DEPOSITED);
            if (!info.isEmpty()) {
                status = info.get(0).getDepositStatus();
            }
            count++;
        }
        while (status != ArchiveDepositInfo.Status.DEPOSITED && count < times);
        log.debug("Exiting archive poll for {}, status {}", businessId, status);
        return status;
    }
    
    // ///////////////////////
    //
    // Stripes-injected Services
    //
    // ///////////////////////
    
    /**
     * Stripes-injected collectionBizService
     * 
     * @param collectionBizService
     */
    @SpringBean("collectionBizService")
    public void injectCollectionBizService(CollectionBizService collectionBizService) {
        this.collectionBizService = collectionBizService;
    }
    
    /**
     * Stripes-injected ArchiveService
     * 
     * @param archiveService
     */
    @SpringBean("archiveService")
    public void injectArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }
    
    /**
     * Stripes-injected AuthorizationService
     * 
     * @param authorizationService
     */
    @SpringBean("authorizationService")
    public void injectAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
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
    
}
