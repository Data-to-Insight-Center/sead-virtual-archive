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

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ADD_DEPOSITOR_ERROR;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_COLLECTION_DOES_NOT_EXIST;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_NO_COLLECTION_FOR_OBJECT_ID;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_NO_COLLECTION_OR_INFO_OBJECTS_FOR_OBJECT_ID;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_PROJECT_FOR_COLLECTION_NOT_FOUND;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_REJECT_NONREGISTERED_USER_AS_DEPOSITOR;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_REMOVE_ADMIN_AS_DEPOSITOR;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_REMOVE_DEPOSITOR_ERROR;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_USERCOLLECTIONS_ADD_DEPOSITOR_SUCCESS;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_USERCOLLECTIONS_REMOVE_DEPOSITOR_SUCCESS;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_USER_CANNOT_EDIT_DEPOSITORS;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_USER_CANNOT_VIEW_COLLECTIONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.FlashScope;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.apache.http.HttpStatus;

import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.CollectionException;
import org.dataconservancy.ui.exceptions.ViewProjectCollectionsException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.Citation;
import org.dataconservancy.ui.model.CitationFormatter;
import org.dataconservancy.ui.model.CitationFormatter.CitationFormat;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.ESIPCitationFormatterImpl;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.AuthorizationService;
import org.dataconservancy.ui.services.CitationService;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.MetadataFileBizService;
import org.dataconservancy.ui.services.MetadataFormatService;
import org.dataconservancy.ui.services.ProjectBizService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.RelationshipException;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.services.UserService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 /** {@code ProjectCollectionsActionBeans} handles requests to view list of {@link Collection} associated with a
 * system user.
 */

@UrlBinding(value = "/usercollections/{$event}/usercollections.action")
public class UserCollectionsActionBean extends BaseActionBean {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private ArchiveService archiveService;
    
    private RelationshipService relationshipService;
    
    private CitationService citationService;
    
    private MetadataFormatService metadataFormatService;
    
    private AuthorizationService authorizationService;
    
    private CollectionBizService collectionBizService;
    
    private ProjectBizService projectBizService;
    
    private MetadataFileBizService metadataFileBizService;
    
    private UserService userService;
    
    private String selectedCollectionId;
    
    private List<String> userIdsToAdd;
    
    private List<String> userIdsToRemove = null;
    
    private HashMap<String, String> containsUserAsAdministrator;
    
    private HashMap<String, String> containsUserAsDepositor;
    
    private HashMap<String, Long> dataItemsCountForCollections;
    
    private HashMap<String, Person> depositorForCollection;
    
    private int selectedCollectionDepositorListSize;
    
    private Project project;
    
    private String currentProjectId;
    
    private Map<CitationFormat, String> citations;
    
    private String redirectUrl;
    
    private String parentCollectionName;
    
    private String parentCollectionId;

    private static final DateTimeFormatter dateOnlyFormatter = DateTimeFormat.forPattern("MM-dd-yyyy");
    
    /**
     * The starting path
     */
    static final String HOME_COLLECTIONS_PATH = "/pages/usercollections.jsp";
    
    /**
     * The forward destination when adding or removing depositors to collections
     */
    static final String EDIT_DEPOSITORS_PATH = "/pages/usercollections_edit_depositors.jsp";
    
    /**
     * The page for viewing a collections details
     */
    static final String VIEW_COLLECTION_DETAILS_PATH = "/pages/collection_view.jsp";
    
    /**
     * The page for viewing collections in a specific project for which the user has rights
     */
    static final String VIEW_PROJECT_COLLECTIONS_PATH = "/pages/usercollections_for_project.jsp";
    
    public UserCollectionsActionBean() {
        super();
        
        try {
            assert (messageKeys.containsKey(MSG_KEY_NO_COLLECTION_OR_INFO_OBJECTS_FOR_OBJECT_ID));
            assert (messageKeys.containsKey(MSG_KEY_NO_COLLECTION_FOR_OBJECT_ID));
            assert (messageKeys.containsKey(MSG_KEY_COLLECTION_DOES_NOT_EXIST));
            assert (messageKeys.containsKey(MSG_KEY_ADD_DEPOSITOR_ERROR));
            assert (messageKeys.containsKey(MSG_KEY_REMOVE_DEPOSITOR_ERROR));
            assert (messageKeys.containsKey(MSG_KEY_REJECT_NONREGISTERED_USER_AS_DEPOSITOR));
            assert (messageKeys.containsKey(MSG_KEY_REMOVE_ADMIN_AS_DEPOSITOR));
            assert (messageKeys.containsKey(MSG_KEY_USERCOLLECTIONS_ADD_DEPOSITOR_SUCCESS));
            assert (messageKeys.containsKey(MSG_KEY_USERCOLLECTIONS_REMOVE_DEPOSITOR_SUCCESS));
            assert (messageKeys.containsKey(MSG_KEY_USER_CANNOT_EDIT_DEPOSITORS));
            assert (messageKeys.containsKey(MSG_KEY_USER_CANNOT_VIEW_COLLECTIONS));
        }
        catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of "
                    + MSG_KEY_NO_COLLECTION_OR_INFO_OBJECTS_FOR_OBJECT_ID + ", " + MSG_KEY_NO_COLLECTION_FOR_OBJECT_ID
                    + ", " + MSG_KEY_COLLECTION_DOES_NOT_EXIST + ", " + MSG_KEY_ADD_DEPOSITOR_ERROR + ", "
                    + MSG_KEY_REMOVE_DEPOSITOR_ERROR + ", " + MSG_KEY_REJECT_NONREGISTERED_USER_AS_DEPOSITOR + ", "
                    + MSG_KEY_REMOVE_ADMIN_AS_DEPOSITOR + ", " + MSG_KEY_USERCOLLECTIONS_ADD_DEPOSITOR_SUCCESS + ", "
                    + MSG_KEY_USERCOLLECTIONS_REMOVE_DEPOSITOR_SUCCESS + ", " + MSG_KEY_USER_CANNOT_EDIT_DEPOSITORS
                    + ", " + MSG_KEY_USER_CANNOT_VIEW_COLLECTIONS + " is missing.");
        }
        
        redirectUrl = "render";
    }
    
    @DefaultHandler
    public Resolution render() throws BizInternalException, BizPolicyException, ViewProjectCollectionsException {
        Person currentUser = getAuthenticatedUser();
        final String errorMessage = String.format(messageKeys.getProperty(MSG_KEY_USER_CANNOT_VIEW_COLLECTIONS),
                (currentUser != null) ? currentUser.getId() : "Anonymous");
        
        // initialize class fields so that the jsp will be able to render
        containsUserAsAdministrator = new HashMap<String, String>();
        containsUserAsDepositor = new HashMap<String, String>();
        dataItemsCountForCollections = new HashMap<String, Long>();
        depositorForCollection = new HashMap<String, Person>();
        
        if (currentUser != null) {
            if (currentProjectId != null) {
                try {
                    project = projectBizService.getProject(currentProjectId, currentUser);
                }
                catch (BizPolicyException e) {
                    ViewProjectCollectionsException viewEx = new ViewProjectCollectionsException(errorMessage, e);
                    viewEx.setHttpStatusCode(401);
                    viewEx.setProjectId(currentProjectId);
                    viewEx.setUserId(currentUser.getId());
                    throw viewEx;
                }
                catch (Exception e) {
                    ViewProjectCollectionsException viewEx = new ViewProjectCollectionsException(errorMessage, e);
                    viewEx.setProjectId(currentProjectId);
                    viewEx.setUserId((getAuthenticatedUser() != null) ? getAuthenticatedUser().getId() : "Anonymous");
                    viewEx.setHttpStatusCode(500);
                    throw viewEx;
                }
            }
        }
        return new ForwardResolution(HOME_COLLECTIONS_PATH);
    }
    
    public Resolution editCollectionDepositors() {
        return new ForwardResolution(EDIT_DEPOSITORS_PATH);
    }
    
    public Resolution editDepositors() throws CollectionException, BizInternalException, BizPolicyException,
            ArchiveServiceException {
        Project project = null;
        Collection collection = null;
        List<Message> userCollectionMessages = getContext().getMessages("updated");
        List<Message> successMessages = getContext().getMessages("success");
        
        try {
            collection = getCollection(selectedCollectionId);
            if (collection == null) {
                final String msg = "Collection " + selectedCollectionId + " doesn't exist.";
                log.debug(msg);
                return new ErrorResolution(HttpStatus.SC_BAD_REQUEST, msg);
            }
            project = relationshipService.getProjectForCollection(collection);
        }
        catch (RelationshipConstraintException e) {
            log.warn("Error: ", e);
        }
        if (project == null) {
            // throw new RuntimeException("Could not find associated project for this collection.");
            String msg = String.format(messageKeys.getProperty(MSG_KEY_PROJECT_FOR_COLLECTION_NOT_FOUND,
                    collection.getId()));
            CollectionException ce = new CollectionException(msg);
            ce.setUserId(getAuthenticatedUser().getId());
            ce.setCollectionId(collection.getId());
            throw ce;
        }
        
        // Check to make sure the user adding depositors is a project admin, or a system admin
        Person currentUser = getAuthenticatedUser();
        if (!authorizationService.canEditDepositorsForProject(currentUser, project)) {
            // throw new RuntimeException("Only System Admins or Project Admins may edit depositors.");
            final String msg = messageKeys.getProperty(MSG_KEY_USER_CANNOT_EDIT_DEPOSITORS);
            return new ErrorResolution(HttpStatus.SC_UNAUTHORIZED, msg);
        }
        if (userIdsToAdd != null) {
            for (String id : userIdsToAdd) {
                if (id != null && !id.isEmpty()) {
                    try {
                        Person p = userService.get(id);
                        if (p != null) {
                            // add only approved registered users
                            if (p.getRegistrationStatus().equals(RegistrationStatus.APPROVED)) {
                                relationshipService.addDepositorToCollection(p, collection);
                                String s = messageKeys.getProperty(MSG_KEY_USERCOLLECTIONS_ADD_DEPOSITOR_SUCCESS);
                                final String msg = String.format(s, id, selectedCollectionId);
                                successMessages.add(new SimpleMessage(msg));
                            }
                            else {// not an approved user
                                String s = messageKeys.getProperty(MSG_KEY_REJECT_NONREGISTERED_USER_AS_DEPOSITOR);
                                final String msg = String.format(s, id);
                                userCollectionMessages.add(new SimpleMessage(msg));
                            }
                        }
                        else {
                            log.error("Error adding depositor " + id + " user doesn't exist in the system");
                            String s = messageKeys.getProperty(MSG_KEY_ADD_DEPOSITOR_ERROR);
                            final String msg = String.format(s, id, "user doesn't exist in the system");
                            userCollectionMessages.add(new SimpleMessage(msg));
                        }
                    }
                    catch (Exception e) {
                        log.error("Error adding depositor {} to collection: {}", id, e);
                        String s = messageKeys.getProperty(MSG_KEY_ADD_DEPOSITOR_ERROR);
                        final String msg = String.format(s, id, e);
                        userCollectionMessages.add(new SimpleMessage(msg));
                    }
                }
            }
        }
        if (userIdsToRemove != null) {
            for (String id : userIdsToRemove) {
                if (id != null && !id.isEmpty()) {
                    try {
                        Person p = userService.get(id);
                        if (p != null) {
                            List<Person> removableDepositors = getRemovableDepositors();
                            if (removableDepositors.contains(p)) {
                                relationshipService.removeDepositorFromCollection(p, collection);
                                String s = messageKeys.getProperty(MSG_KEY_USERCOLLECTIONS_REMOVE_DEPOSITOR_SUCCESS);
                                final String msg = String.format(s, id, selectedCollectionId);
                                userCollectionMessages.add(new SimpleMessage(msg));
                            }
                            else {
                                userCollectionMessages.add(new SimpleMessage(messageKeys
                                        .getProperty(MSG_KEY_REMOVE_DEPOSITOR_ERROR)));
                            }
                        }
                    }
                    catch (Exception e) {
                        log.error("Error removing depositor {} from collection: {}", id, e);
                        String s = messageKeys.getProperty(MSG_KEY_REMOVE_DEPOSITOR_ERROR);
                        final String msg = String.format(s, id, selectedCollectionId);
                        userCollectionMessages.add(new SimpleMessage(msg));
                    }
                }
            }
        }
        RedirectResolution res = new RedirectResolution(this.getClass(), "editCollectionDepositors");
        res.addParameter("selectedCollectionId", selectedCollectionId);
        return res;
    }
    
    public Resolution viewCollectionDetails() throws BizInternalException, BizPolicyException, CollectionException,
            ArchiveServiceException {
        if (getDepositId(selectedCollectionId) == null) {
            final String msg = "Collection " + selectedCollectionId + " doesn't exist!";
            log.debug(msg);
            return new ErrorResolution(404, msg);
        }
        generateCitations();
        setRedirectUrl("viewCollectionDetails");
        
        return new ForwardResolution(VIEW_COLLECTION_DETAILS_PATH);
    }
    
    public Map<String, String> getSelectedCollectionFormatNames() throws CollectionException, BizInternalException,
            BizPolicyException, ArchiveServiceException, RelationshipException {
        Map<String, String> result = new HashMap<String, String>();
        
        for (String mfId : relationshipService.getMetadataFileIdsForBusinessObjectId(getSelectedCollectionId())){
            MetadataFile mf = metadataFileBizService.retrieveMetadataFile(mfId);
            // FIXME: This should get the metadata format id and then retrieve the metadata format from the registry
            // then get the name of that. -BMB
            if (mf != null) {
                result.put(mf.getFormat(), getFormatName(mf.getFormat()));
            }
        }
        
        return result;
    }
    
    public List<Message> getMessages(String key) {
        FlashScope scope = FlashScope.getCurrent(getContext().getRequest(), true);
        List<Message> messages = (List<Message>) scope.get(key);

        if (messages == null) {
            messages = new ArrayList<Message>();
            scope.put(key, messages);
        }
        
        return messages;
    }
    
    private String getFormatName(String format_uri) {
        if (format_uri == null) {
            return "Unknown";
        }
        
        DcsMetadataFormat fmt = metadataFormatService.getMetadataFormat(format_uri);
        
        if (fmt == null) {
            return format_uri;
        }
        
        return fmt.getName();
    }
    
    public void generateCitations() throws CollectionException, BizInternalException, BizPolicyException,
            ArchiveServiceException {
        // fetch collection
        final Collection collection = getCollection(selectedCollectionId);
        if (collection == null) {
            final String msg = String.format(messageKeys.getProperty(MSG_KEY_COLLECTION_DOES_NOT_EXIST),
                    selectedCollectionId);
            log.debug(msg);
            throw new CollectionException(msg);
        }
        // create citation object
        Citation citation = citationService.createCitation(collection);
        
        citations = new HashMap<CitationFormat, String>();
        
        // format citation with ESIP formater
        ESIPCitationFormatterImpl citationFormatter = new ESIPCitationFormatterImpl();
        String ESIPcitationString = citationFormatter.formatHtml(citation);
        citations.put(CitationFormat.ESIP, ESIPcitationString);
    }
    
    public List<Person> getDepositorsForSelectedCollection() throws CollectionException, BizInternalException,
            BizPolicyException, ArchiveServiceException {
        final Collection collection = getCollection(selectedCollectionId);
        if (collection == null) {
            return new ArrayList<Person>(1);
        }
        List<Person> personList = getDepositorsForCollection(collection);
        setSelectedCollectionDepositorListSize(personList.size());
        return personList;
    }
    
    public List<Person> getDepositorsForCollection(Collection collection) {
        List<Person> collectionDepositors = new ArrayList<Person>();
        for (Person depositor : relationshipService.getDepositorsForCollection(collection)) {
            collectionDepositors.add(depositor);
        }
        return collectionDepositors;
    }
    
    public List<Person> getRemovableDepositors() throws CollectionException, BizInternalException, BizPolicyException,
            ArchiveServiceException, RelationshipConstraintException {
        List<Person> depositors = getDepositorsForSelectedCollection();
        List<Person> removable = new ArrayList<Person>();
        Collection collection = getSelectedCollection();
        for (Person depositor : depositors) {
            if (authorizationService.canRemoveDepositor(getAuthenticatedUser(), depositor, collection)) {
                removable.add(depositor);
            }
        }
        return removable;
    }
    
    /**
     * We use this to retrieve collections with data item counts. this is used in the case of getting all collections
     * for a user, and also in the case of getting all collections for a particular project for a user
     * 
     * @return userCollections List (sorted)
     * @throws BizPolicyException
     * @throws BizInternalException
     * @throws RelationshipException
     * @throws ArchiveServiceException
     */
    public List<Collection> getCollectionsForUser() throws BizInternalException, BizPolicyException,
            RelationshipException, ArchiveServiceException {
        Person currentUser = getAuthenticatedUser();
        List<Collection> userCollections = new ArrayList<Collection>(); // the collection list
        List<String> collectionIds = new ArrayList<String>(); // their ids
        List<Collection> subCollections = new ArrayList<Collection>();
        
        if (null == project) { // we don't have a particular project in mind, so get all collections for our user
            userCollections.addAll(collectionBizService.findByUser(currentUser));
        }
        else { // have a project in mind, get a list of collections just for the project
            userCollections.addAll(relationshipService.getCollectionsForProject(project));

            // Adding any sub-collections if there are any, since they're not directly associated to the
            // project, they'll be ignored otherwise.
            for (Collection collection : userCollections) {
                Set<String> subColIds = relationshipService.getSubCollectionIdsForCollectionId(collection.getId());
                for (String id : subColIds) {
                    subCollections.add(collectionBizService.getCollection(id, currentUser));
                }
            }
            userCollections.addAll(subCollections);
        }
        
        if (userCollections.isEmpty()) { // if there are no collections to display, we're done
            return userCollections;
        }
        else {
            // ok, we have a non-empty list of collections to output
            
            if (currentUser != null) {// we don't do this for anonymous users
                // set permission hashes for adminhood, depositorhood for our collections
                // these are needed by the jsp to decide whether to present links in the Actions column
                Set<Collection> adminCollectionSet = new HashSet<Collection>();
                Set<Collection> depositorCollectionSet = new HashSet<Collection>();
                
                if (currentUser.getRoles().contains(Role.ROLE_ADMIN)) {// instance admins can do anything
                    adminCollectionSet.addAll(userCollections);
                    depositorCollectionSet.addAll(userCollections);
                }
                else {// get the recorded relationships
                    adminCollectionSet.addAll(relationshipService.getCollectionsForAdministrator(currentUser));
                    depositorCollectionSet.addAll(relationshipService.getCollectionsForDepositor(currentUser));
                }
                // populate the admin hash - this will enable the display of the Edit Depositors link for collections
                for (Collection collection : adminCollectionSet) {
                    containsUserAsAdministrator.put(collection.getId(), "true");
                }
                // populate the depositor hash - this will enable the display of the Deposit Data link for collections
                for (Collection collection : depositorCollectionSet) {
                    containsUserAsDepositor.put(collection.getId(), "true");
                }
            }
            
            // but for any user we have to do this:
            // build a list of collection ids, and pass it to the collection biz service method which
            // gives us counts of data items for these collections
            // this method will poll the archive once, so we don't have to do it here
            for (Collection collection : userCollections) {
                collectionIds.add(collection.getId());
                Person depositor = userService.get(collection.getDepositorId());
                if (depositor != null) {
                    depositorForCollection.put(collection.getId(), depositor);
                }
            }
            dataItemsCountForCollections.putAll(collectionBizService.retrieveDataItemCountForList(collectionIds));
            // sort the collection list alphabetically by title, and return it
            
            return sortCollectionListByTitle(userCollections);
        }
    }
    
    public List<Person> getCollectionDepositors() throws CollectionException, BizInternalException, BizPolicyException {
        List<Person> depositors = new ArrayList<Person>();
        // find the deposit_ids for our collection with object_id=selectedCollectionId
        // most recent is first per the interface contract;
        Collection c = collectionBizService.getCollection(selectedCollectionId, getAuthenticatedUser());
        
        if (c != null) {
            for (Person depositor : relationshipService.getDepositorsForCollection(c)) {
                depositors.add(depositor);
            }
        }
        return depositors;
    }
    
    public Collection getCollection(String objectId) throws CollectionException, BizInternalException,
            BizPolicyException, ArchiveServiceException {
        archiveService.pollArchive();
        Collection collection = collectionBizService.getCollection(objectId, getAuthenticatedUser());
        return collection;
    }
    
    /**
     * A convenience method.
     * 
     * @param collectionList
     * @return
     */
    private List<Collection> sortCollectionListByTitle(List<Collection> collectionList) {
        Collections.sort(collectionList, new Comparator<Collection>() {
            
            public int compare(Collection o1, Collection o2) {
                if (o1.getTitle() == null || o2.getTitle() == null)
                    return 0;
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        return collectionList;
    }
    
    /**
     * @param object_id
     * @return the deposit id of the latest version of the object or null if nothing is known
     */
    private String getDepositId(String object_id) {
        if (object_id == null || object_id.isEmpty()) {
            return null;
        }

        // TODO: account for other status other than deposited in the error message
        List<ArchiveDepositInfo> infoList = archiveService.listDepositInfo(object_id,
                ArchiveDepositInfo.Status.DEPOSITED);
        
        if (infoList == null || infoList.isEmpty()) {
            return null;
        }
        
        return infoList.get(0).getDepositId();
    }
    
    public Collection getSelectedCollection() throws CollectionException, BizInternalException, BizPolicyException,
            ArchiveServiceException {
        return getCollection(selectedCollectionId);
    }
    
    public List<MetadataFile> getCollectionMetadataFiles() throws CollectionException, BizInternalException,
            BizPolicyException, ArchiveServiceException, RelationshipException {
        List<MetadataFile> files = new ArrayList<MetadataFile>();
        Collection col = getSelectedCollection();
        for (String id : relationshipService.getMetadataFileIdsForBusinessObjectId(col.getId())){
            MetadataFile file = metadataFileBizService.retrieveMetadataFile(id);
            if (file != null) {
                files.add(file);
            }
        }
        
        return files;
    }
    
    public void setSelectedCollectionId(String id) {
        this.selectedCollectionId = id;
    }
    
    public String getSelectedCollectionId() {
        return this.selectedCollectionId;
    }
    
    public String getSelectedCollectionPublicationDateString() throws BizInternalException, BizPolicyException,
            CollectionException, ArchiveServiceException {
        DateTime publicationDate = getSelectedCollection().getPublicationDate();
        if (publicationDate != null) {
            return dateOnlyFormatter.print(getSelectedCollection().getPublicationDate());
        }
        else {
            return "";
        }
    }
    
    public HashMap<String, String> getContainsUserAsAdministrator() {
        return this.containsUserAsAdministrator;
    }
    
    public HashMap<String, String> getContainsUserAsDepositor() {
        return this.containsUserAsDepositor;
    }
    
    public HashMap<String, Long> getDataItemsCountForCollections() {
        return this.dataItemsCountForCollections;
    }
    
    public HashMap<String, Person> getDepositorForCollection() {
        return this.depositorForCollection;
    }
    
    public List<String> getUserIdsToAdd() {
        return userIdsToAdd;
    }
    
    public void setUserIdsToAdd(List<String> userIds) {
        this.userIdsToAdd = userIds;
    }
    
    public List<String> getUserIdsToRemove() {
        return userIdsToRemove;
    }
    
    public void setUserIdsToRemove(List<String> userIds) {
        this.userIdsToRemove = userIds;
    }
    
    public int getSelectedCollectionDepositorListSize() {
        return selectedCollectionDepositorListSize;
    }
    
    public void setSelectedCollectionDepositorListSize(int selectedCollectionDepositorListSize) {
        this.selectedCollectionDepositorListSize = selectedCollectionDepositorListSize;
    }
    
    /**
     * @return the parentCollectionName
     * @throws RelationshipException
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    public String getParentCollectionName() throws RelationshipException, BizPolicyException, BizInternalException {
        Set<String> ids = relationshipService.getSuperCollectionIdsForCollectionId(selectedCollectionId);
        for (String id : ids) {
            parentCollectionName = collectionBizService.getCollection(id, getAuthenticatedUser()).getTitle();
            parentCollectionId = id;
            return parentCollectionName;
        }
        return null;
    }
    
    /**
     * @param parentCollectionName
     *            the parentCollectionName to set
     */
    public void setParentCollectionName(String parentCollectionName) {
        this.parentCollectionName = parentCollectionName;
    }
    
    public String getParentCollectionId() {
        return this.parentCollectionId;
    }

    public List<Person> getAdmins() {
        List<Person> admins = new ArrayList<Person>();
        Project project = null;
        
        try {
            Collection collection = collectionBizService.getCollection(selectedCollectionId, getAuthenticatedUser());
            if (collection != null) {
                project = relationshipService.getProjectForCollection(collection);
                for (Person depositor : getDepositorsForCollection(collection)) {
                    if (relationshipService.getAdministratorsForProject(project).contains(depositor)
                            || depositor.getRoles().contains(Role.ROLE_ADMIN))
                        admins.add(depositor);
                }
            }
        }
        catch (Exception e) {
            log.error("Error getting relationship information for admins: {}", e);
        }
        
        return admins;
    }
    
    public Project getProjectForCurrentCollection() throws CollectionException, BizInternalException,
            BizPolicyException, ArchiveServiceException {
        Project project = null;
        Collection currentCollection = getSelectedCollection();
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
    
    public boolean getIsAdminForProject() throws CollectionException, BizInternalException, BizPolicyException,
            ArchiveServiceException {
        boolean isAdmin = false;
        Project project = null;
        Collection currentCollection = getSelectedCollection();
        if (currentCollection != null && currentCollection.getId() != null && !currentCollection.getId().isEmpty()) {
            try {
                // TODO: should be on ProjectBizService or the heretofore non-existent CollectionBizService.
                project = relationshipService.getProjectForCollection(currentCollection);
            }
            catch (RelationshipConstraintException e) {
                String msg = String.format(MSG_KEY_PROJECT_FOR_COLLECTION_NOT_FOUND, currentCollection.getId());
                log.error(msg);
                CollectionException ce = new CollectionException(msg);
                throw ce;
            }
        }
        
        if (project != null) {
            Person currentUser = getAuthenticatedUser();
            // TODO: relationshipService.getAdministratorsForProject(project) should be performed using the
            // ProjectBizService
            if (null != currentUser && currentUser.getRoles().contains(Role.ROLE_ADMIN)
                    || relationshipService.getAdministratorsForProject(project).contains(getAuthenticatedUser())) {
                isAdmin = true;
            }
        }
        
        return isAdmin;
    }
    
    public boolean getIsDepositorForCollection() throws CollectionException, BizInternalException, BizPolicyException,
            ArchiveServiceException {
        Collection currentCollection = getSelectedCollection();
        Person currentUser = getAuthenticatedUser();
        return null != currentUser && null != currentCollection
                && relationshipService.getDepositorsForCollection(currentCollection).contains(currentUser);
    }
    
    public boolean getCanUpdateCollection() throws RelationshipConstraintException, BizInternalException,
            BizPolicyException, CollectionException, ArchiveServiceException {
        Collection currentCollection = getSelectedCollection();
        Person currentUser = getAuthenticatedUser();
        return authorizationService.canUpdateCollection(currentUser, currentCollection);
    }
    
    public ArchiveDepositInfo.Status getDepositStatus(String object_id) {
        String deposit_id = getDepositId(object_id);
        
        if (deposit_id == null) {
            return null;
        }
        
        return archiveService.getDepositStatus(deposit_id);
    }
    
    public boolean getSelectedCollectionDeposited() {
        return getDepositStatus(getSelectedCollectionId()) == ArchiveDepositInfo.Status.DEPOSITED;
    }
    
    public String getSelectedCollectionDepositStatusMsg() {
        ArchiveDepositInfo.Status status = getDepositStatus(selectedCollectionId);
        
        if (status == null) {
            return "No such collection";
        }
        
        if (status == Status.DEPOSITED) {
            return "Deposited";
        }
        
        if (status == Status.FAILED) {
            return "Failed";
        }
        
        if (status == Status.PENDING) {
            return "Pending";
        }
        
        throw new IllegalStateException();
    }
    
    public String getSelectedCollectionDepositStatusExplanation() {
        ArchiveDepositInfo.Status status = getDepositStatus(selectedCollectionId);
        
        if (status == null) {
            return "Failed to find selected collection.";
        }
        
        if (status == Status.DEPOSITED) {
            return "The collection has been deposited in the archive.";
        }
        
        if (status == Status.FAILED) {
            return "Please contact instance administrator.";
        }
        
        if (status == Status.PENDING) {
            return "The collection is in the process of being deposited.";
        }
        
        throw new IllegalStateException();
    }
    
    public CitationFormat[] getCitationFormats() {
        return CitationFormatter.CitationFormat.values();
        
    }
    
    public Map<CitationFormat, String> getCitations() {
        return this.citations;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setRedirectUrl(String url) {
        this.redirectUrl = url;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setCurrentProjectId(String currentProjectId) {
        this.currentProjectId = currentProjectId;
    }
    
    public String getCurrentProjectId() {
        return currentProjectId;
    }
    
    @SpringBean("metadataFormatService")
    public void injectMetadataFormatService(MetadataFormatService metadataFormatService) {
        this.metadataFormatService = metadataFormatService;
    }
    
    @SpringBean("archiveService")
    public void injectArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }
    
    @SpringBean("relationshipService")
    public void injectRelationshipService(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }
    
    @SpringBean("citationService")
    public void injectCitationService(CitationService citationService) {
        this.citationService = citationService;
    }
    
    @SpringBean("collectionBizService")
    public void injectCollectionBizService(CollectionBizService collectionBizService) {
        this.collectionBizService = collectionBizService;
    }
    
    @SpringBean("authorizationService")
    public void injectAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }
    
    @SpringBean("projectBizService")
    public void injectProjectBizService(ProjectBizService projectBizService) {
        this.projectBizService = projectBizService;
    }
    
    @SpringBean("metadataFileBizService")
    public void injectMetadataFileBizService(MetadataFileBizService metadataFileBizService) {
        this.metadataFileBizService = metadataFileBizService;
    }
    
    @SpringBean("userService")
    public void injectUserService(UserService userService) {
        this.userService = userService;
    }
    
}
