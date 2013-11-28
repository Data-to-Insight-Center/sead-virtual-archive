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
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_SESSION_LOGGED_OUT;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_SYS_OR_PROJECT_ADMIN_REQUIRED_TO_CREATE_COLLECTION;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_POLL_TIMEOUT_WHEN_DEPOSITING_COLLECTION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
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
import net.sourceforge.stripes.mock.MockHttpServletRequest;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.dao.DisciplineDAO;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.CollectionException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.ContactInfo;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.services.*;
import org.dataconservancy.ui.stripes.ext.JodaDateTimeTypeConverter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Action Bean for the collection business object. It handles viewing collections and adding collections.
 * The capability for updating collections should be trivial to add at a later date.
 **/

@UrlBinding("/collection/collection_add.action")
public class AddCollectionActionBean extends org.dataconservancy.ui.stripes.BaseActionBean {
    
    /**
     * HTTP session key that contains the current Collection object
     */
    private static final String COLLECTION_SESSION_KEY = "collection";
    
    /**
     * HTTP session key that contains the current Project identifier
     */
    private static final String PROJECT_ID_SESSION_KEY = "projectId";
    
    /**
     * HTTP session key that contains the parent collection ID
     */
    private static final String PARENT_COLLECTION_ID_SESSION_KEY = "parentCollectionId";
    
    /**
     * The path to the jsp used to add collection information. Package-private for unit test access.
     */
    static final String COLLECTION_ADD_PATH = "/pages/collection_add.jsp";
    
    /**
     * The path to the jsp used to add contact information. Package-private for unit test access.
     */
    static final String COLLECTION_CONTACT_INFO_PATH = "/pages/collection_contact_info_add.jsp";
    
    /**
     * The path to the jsp used to add collection metadata. Package-private for unit test access.
     */
    static final String COLLECTION_METADATA_FILE_PATH = "/pages/collection_metadata_file_add.jsp";
    
    /**
     * The path to the jsp used to view collections. Package-private for unit test access.
     */
    static final String VIEW_COLLECTION_PATH = "/pages/collection_list.jsp";
    
    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Identifier service, used to generate business ids for created collections
     */
    private IdService idService;
    
    /**
     * The archive service is used to create (and later update) the collection.
     */
    private ArchiveService archiveService;
    
    /**
     * The project business service is used to get a project from an id.
     */
    private ProjectBizService projectBizService;

    /**
     * The collection business service is used to create collection in the system.
     */
    private CollectionBizService collectionBizService;

    /**
     * The relationship service is used to relate a collection to a project.
     */
    private RelationshipService relationshipService;
    
    /**
     * The metadata format service to retrieve metadata format objects from their id.
     */
    private MetadataFormatService metadataFormatService;
    
    /**
     * The metadata file business service is used to handle metadata files that are added to a collection.
     */
    private MetadataFileBizService metadataFileBizService;

    /**
     * The authorization service determines if use has permission to perform various actions
     */
    private AuthorizationService authorizationService;
    
    private FileBean uploadedFile;
    
    /**
     * The Project identifier that this Collection is associated with.
     */
    private String projectId = "";
    
    /**
     * The Project object that this Collection is associated with.
     */
    private Project project;
    
    private int metadata_file_index = -1;
    
    private int contactInfoIndex = -1;
    
    private DisciplineDAO disciplineDAO;
    
    private RedirectResolution metadataFileAddRedirect;
    
    /**
     * Collection ID will be null if a Collection does not exist in the session. Package-private: only to be used by
     * unit tests.
     */
    private String collectionId;
    
    private String parentCollectionId;

    private Collection collection;
    
    @ValidateNestedProperties({
            @Validate(field = "name", required = true, on = { "saveAndDoneContactInfo", "saveAndAddMoreContactInfo" }),
            @Validate(field = "emailAddress", required = true, on = { "saveAndDoneContactInfo",
                    "saveAndAddMoreContactInfo" }, converter = EmailTypeConverter.class) })
    private ContactInfo contactInfo;
    
    @ValidateNestedProperties({ @Validate(field = "name", required = true, on = { "saveAndAddMoreMetadataFile",
            "saveAndDoneMetadataFile" }) })
    private MetadataFile metadataFile = new MetadataFile();
    
    public AddCollectionActionBean() {
        super();
        // Ensure desired properties are available.
        try {
            assert (messageKeys.containsKey(MSG_KEY_SESSION_LOGGED_OUT));
            assert (messageKeys.containsKey(MSG_KEY_SYS_OR_PROJECT_ADMIN_REQUIRED_TO_CREATE_COLLECTION));
            assert (messageKeys.containsKey(MSG_KEY_COULD_NOT_DEPOSIT_COLLECTION));
            assert (messageKeys.containsKey(MSG_KEY_METADATA_FILE_UPLOAD_FAIL));
            assert (messageKeys.containsKey(MSG_KEY_POLL_TIMEOUT_WHEN_DEPOSITING_COLLECTION));
        }
        catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of " + MSG_KEY_SESSION_LOGGED_OUT + ", "
                    + MSG_KEY_SYS_OR_PROJECT_ADMIN_REQUIRED_TO_CREATE_COLLECTION + ", "
                    + MSG_KEY_METADATA_FILE_UPLOAD_FAIL + ", " + MSG_KEY_COULD_NOT_DEPOSIT_COLLECTION + " is missing.");
        }
        
    }
    
    // ///////////////////////
    //
    // Accessors
    //
    // ///////////////////////
    
    public void setUploadedFile(FileBean uploadedFile) {
        this.uploadedFile = uploadedFile;
    }
    
    public FileBean getUploadedFile() {
        return uploadedFile;
    }
    
    // The id of the selected
    private String discipline;
    
    private String projectName;
    
    public String getDiscipline() {
        return discipline;
    }
    
    public void setDiscipline(String discipline) {
        this.discipline = discipline;
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
    
    public String getProjectName() {
        Set<Project> projects = relationshipService.getProjectsForAdministrator(getAuthenticatedUser());
        for (Project p : projects) {
            if (p.getId().equals(projectId)) {
                projectName = p.getName();
            }
        }
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    /**
     * Obtain a list of all Disciplines supported by this instance of the DC User Interface.
     * 
     * @return the Discipline List
     */
    public List<Discipline> getDisciplines() {
        return disciplineDAO.list();
    }
    
    /**
     * This is the collection that we are adding/viewing. If the Collection exists on the HTTP session, it is returned.
     * If no Collection exists on the session, a new Collection is created, a new ID is generated, and the Collection is
     * placed onto the session. In all cases, the {@link #collectionId} field of this bean is set.
     */
    @ValidateNestedProperties({ @Validate(field = "title", required = true, on = { "addCollection" }),
            @Validate(field = "summary", required = true, on = { "addCollection" }),
            @Validate(field = "publicationDate", converter = JodaDateTimeTypeConverter.class),
            @Validate(field = "creators[0].familyNames", required = true, on = { "addCollection" }) })
    public Collection getCollection() {
        HttpSession ses = getContext().getRequest().getSession();

        //TODO: Should this code check with the Authorization service about whether the user has permission to retrieve
        //TODO: the collection before performing retrieval?
        Collection collection = (Collection) ses.getAttribute(COLLECTION_SESSION_KEY);
        
        if (collection == null) {
            collection = new Collection();
            collection.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
            ses.setAttribute(COLLECTION_SESSION_KEY, collection);
        }
        
        collectionId = collection.getId();
        
        return collection;
    }
    
    /**
     * Package-private for unit testing
     * 
     * @return
     */
    String getCollectionId() {
        return collectionId;
    }
    
    /**
     * @return the parentCollectionId
     */
    public String getParentCollectionId() {
        HttpSession ses = getContext().getRequest().getSession();
        parentCollectionId = (String) ses.getAttribute(PARENT_COLLECTION_ID_SESSION_KEY);
        return parentCollectionId;
    }
    
    /**
     * @param parentCollectionId
     *            the parentCollectionId to set
     */
    public void setParentCollectionId(String parentCollectionId) {
        this.parentCollectionId = parentCollectionId;
        HttpSession ses = getContext().getRequest().getSession();
        ses.setAttribute(PARENT_COLLECTION_ID_SESSION_KEY, this.parentCollectionId);
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }
    
    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }
    
    public MetadataFile getMetadataFile() {
        return metadataFile;
    }
    
    public void setMetadataFile(MetadataFile metadataFile) {
        this.metadataFile = metadataFile;
    }
    
    public int getMetadataFileIndex() {
        return metadata_file_index;
    }
    
    public void setMetadataFileIndex(int index) {
        this.metadata_file_index = index;
    }
    
    public int getContactInfoIndex() {
        return contactInfoIndex;
    }
    
    public void setContactInfoIndex(int contactInfoIndex) {
        this.contactInfoIndex = contactInfoIndex;
    }
    
    /**
     * Obtain a List of MetadataFormat objects for the supplied Discipline identifier.
     * 
     * @param disciplineId
     *            the identifier for the Discipline
     * @return a List of MetadataFormats for that Discipline
     */
    public List<DcsMetadataFormat> getMetadataFormatsForDiscipline(String disciplineId) {
        Set<String> metadataURIs = relationshipService.getMetadataFormatsForDiscipline(disciplineId);
        List<DcsMetadataFormat> metadataFormats = new ArrayList<DcsMetadataFormat>
                (metadataFormatService.getMetadataFormatsForDiscipline(disciplineId));
        
        return metadataFormats;
    }
    
    // ///////////////////////
    //
    // Stripes Resolutions
    //
    // ///////////////////////
    
    /**
     * Displays the form used to add a Collection
     * 
     * @return the Resolution
     */
    @DefaultHandler
    @DontValidate
    public Resolution displayCollectionForm() {
        if (project != null) {
            project.setId(projectId);
            project.setName(projectName);
        }
        
        metadataFileAddRedirect = new RedirectResolution(getClass(), "displayCollectionForm");
        
        return new ForwardResolution(COLLECTION_ADD_PATH);
    }
    
    /**
     * Deposit Collection into the archive via Archive Service
     * 
     * @return res
     * @throws CollectionException
     * @throws BizPolicyException
     * @throws BizInternalException
     */
    public Resolution addCollection() throws CollectionException, BizPolicyException, BizInternalException {
        try {
            /**
             * Not handling subcollection in the UI yet.
            if (getParentCollectionId() != null) {
                collection = getCollection();
                collection.setParentId(parentCollectionId);
                collectionBizService.createSubCollection(collection, getAuthenticatedUser());
                clearCollection();
                RedirectResolution res = new RedirectResolution(UserCollectionsActionBean.class,
                        "render");
                return res;
            }
            else {
             **/
            getCollection().setParentProjectId(getProjectId());
            collectionBizService.createCollection(getCollection(), getAuthenticatedUser());
            clearCollection();
            RedirectResolution res = new RedirectResolution(ProjectActionBean.class, "viewUserProject");
            res.addParameter("selectedProjectId", projectId);
            return res;
            //}
        }
        catch (BizPolicyException bpe) {
            throw bpe;
        }
        catch (BizInternalException bie) {
            if (bie.getMessage().contains("timeout")) {
                RedirectResolution rr = new RedirectResolution(DepositStatusActionBean.class);
                final StringBuilder depositStatusUrl = new StringBuilder();
                
                rr.addParameter("objectId", collection.getId());
                try {
                    rr.execute(getContext().getRequest(), new HttpServletResponseWrapper(getContext().getResponse()) {
                        @Override
                        public String encodeRedirectURL(String url) {
                            depositStatusUrl.append(super.encodeRedirectURL(url));
                            return depositStatusUrl.toString();
                        }
                        
                        @Override
                        public void sendRedirect(String location) throws IOException {
                            // do nothing
                        }
                        
                        @Override
                        public void setStatus(int sc) {
                            // do nothing
                        }
                    });
                }
                catch (Exception e) {
                    // ignore
                }
                
                final String msg = String.format(
                        messageKeys.getProperty(MSG_KEY_POLL_TIMEOUT_WHEN_DEPOSITING_COLLECTION), collection.getId(),
                        depositStatusUrl);
                log.warn(msg);
                CollectionException collE = new CollectionException(msg);
                collE.setUserId(getAuthenticatedUser().getId());
                collE.setCollectionId(collection.getId());
                collE.setHttpStatusCode(500);
                throw collE;
            }
            else {
                throw bie;
            }
        }
    }
    
    /**
     * Clear collection from the session. Abandon changes.
     */
    public Resolution cancel() {
        clearCollection();
        RedirectResolution res = new RedirectResolution(ProjectActionBean.class, "viewUserProject");
        res.addParameter("selectedProjectId", getProjectId());
        return res;
    }
    
    /**
     * Render entry form for the collection's contact info
     */
    public Resolution displayContactInfoForm() {
        return new ForwardResolution(COLLECTION_CONTACT_INFO_PATH);
    }
    
    /**
     * Save newly entered contact info for the collection and return to the main collection add page
     */
    public Resolution saveAndDoneContactInfo() {
        saveContactInfo();
        return new ForwardResolution(COLLECTION_ADD_PATH);
    }
    
    /**
     * Save newly entered contact info for the collection and reload contact info page to add more
     */
    public Resolution saveAndAddMoreContactInfo() {
        saveContactInfo();
        contactInfo = null;
        return new RedirectResolution(this.getClass(), "displayContactInfoForm");
    }
    
    /**
     * Remove current contact info from the collection
     */
    public Resolution deleteContactInfo() {
        List<ContactInfo> cis = getCollection().getContactInfoList();
        
        if (contactInfoIndex >= 0 && contactInfoIndex < cis.size()) {
            cis.remove(contactInfoIndex);
        }
        return new RedirectResolution(this.getClass(), "displayCollectionForm");
    }
    
    /**
     * Render entry form for the collection's metadata file
     */
    public Resolution displayMetadataFileForm() {
        return new ForwardResolution(COLLECTION_METADATA_FILE_PATH);
    }
    
    public List<DcsMetadataFormat> getBiology() {
        return getMetadataFormatsForDiscipline("dc:discipline:Biology");
    }
    
    public List<DcsMetadataFormat> getAstronomy() {
        return getMetadataFormatsForDiscipline("dc:discipline:Astronomy");
    }
    
    public List<DcsMetadataFormat> getEarthScience() {
        return getMetadataFormatsForDiscipline("dc:discipline:EarthScience");
    }
    
    public List<DcsMetadataFormat> getNone() {
        return getMetadataFormatsForDiscipline("dc:discipline:None");
    }
    
    /**
     * delete collection's metadata file
     */
    public Resolution deleteMetadataFile() throws RelationshipException {
        Set<String> mfs = relationshipService.getMetadataFileIdsForBusinessObjectId(getCollectionId());
        if (metadata_file_index >= 0 && metadata_file_index < mfs.size()) {
            mfs.remove(metadata_file_index);
        }
        
        return new RedirectResolution(getClass(), "displayCollectionForm");
    }
    
    public void setMetadataFileAddRedirect(RedirectResolution resolution) {
        metadataFileAddRedirect = resolution;
    }
    
    public RedirectResolution getMetadataFileAddRedirect() {
        return metadataFileAddRedirect;
    }
    
    // ///////////////////////
    //
    // Stripes-injected Services
    //
    // ///////////////////////
    
    /**
     * Stripes-injected ArchiveService
     * 
     * @param archiveService
     */
    @SpringBean("archiveService")
    public void injectArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }
    
    @SpringBean("projectBizService")
    public void injectProjectBizService(ProjectBizService bizService) {
        this.projectBizService = bizService;
    }

    @SpringBean("metadataFileBizService")
    public void injectMetadataFileBizService(MetadataFileBizService bizService) {
        this.metadataFileBizService = bizService;
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
     * Stripes-injected IdService
     * 
     * @param idService
     */
    @SpringBean("uiIdService")
    private void injectIdService(IdService idService) {
        this.idService = idService;
    }
    
    /**
     * Stripes-injected MetadataFormatService
     * 
     * @param metadataFormatService
     */
    @SpringBean("metadataFormatService")
    public void injectMetadataFormatService(MetadataFormatService metadataFormatService) {
        this.metadataFormatService = metadataFormatService;
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
     * Stripes-injected CollectionBizService
     * 
     * @param collectionBizService
     */
    @SpringBean("collectionBizService")
    public void injectCollectionBizService(CollectionBizService collectionBizService) {
        this.collectionBizService = collectionBizService;
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
        ses.removeAttribute(COLLECTION_SESSION_KEY);
    }
    
    /**
     * Adds the contact information to the Collection that is in the HTTP session
     */
    private void saveContactInfo() {
        if (contactInfo != null) {
            getCollection().addContactInfo(contactInfo);
        }
    }
    
    /**
     * Polls the archive, and returns the status of the object identified by {@code businessId}. The returned status may
     * be {@code null}. If the status of the object is equal to {@link ArchiveDepositInfo.Status#DEPOSITED}, this method
     * immediately returns without further polling.
     * <p/>
     * The archive is polled {@code times} number of times, waiting {@code delayFactorInMs * times} between each call to
     * {@link ArchiveService#pollArchive()}. For example, if this method is called with a 1000ms delay factor, and times
     * equal to 3, the first call to {@code ArchiveService#pollArchive()} will happen with no delay. If the status is
     * not equal to DEPOSITED, then the next call is delayed 1000ms. Subsequent calls will be delayed by 2000ms and
     * 3000ms, respectively.
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
    
}
