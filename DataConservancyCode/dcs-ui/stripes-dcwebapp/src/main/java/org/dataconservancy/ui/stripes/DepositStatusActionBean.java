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

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.dao.PackageDAO;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.UnknownIdentifierTypeException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Package;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ARCHIVE_PROBLEM;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_BAD_ID_FOR_DATASETS_IN_COLLECTION;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_BAD_ID_FOR_DATASETS_IN_COLLECTION_IN_PROJECT;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_NONEXISTENT_OBJECT_ID;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_NULL_OBJECT_ID;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_STATUS_EMPTY_PACKAGE;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_STATUS_ERROR_PERMISSION;

/**
 * {@code DepositStatusActionBean} supports rendering of {@link DataItem} object(s)'s deposit status.
 */
@UrlBinding("/deposit/status.action")
public class DepositStatusActionBean extends BaseActionBean  {

    //Constants
    private static final String DEPOSIT_STATUS_PATH = "/pages/deposit_status.jsp";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    //Stripes objects
    private String objectId;
    List<String> fileList = new ArrayList<String>();
    List<String> statusList = new ArrayList<String>();

    //Services
    private RelationshipService relationshipService;
    private ArchiveService archiveService;
    private IdService idService;
    private PackageDAO packageDao;
    private static final String NOT_STARTED_STATUS = "NOT STARTED";

    //Setter for Stripes
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    //Getter for Stripes
    public List<String> getFileList() {
        return fileList;
    }
    public List<String> getStatusList() {
        return statusList;
    }

    //Injectors for Spring
    @SpringBean("relationshipService")
    public void injectRelationshipService(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @SpringBean("archiveService")
    public void injectArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @SpringBean("uiIdService")
    private void injectIdService(IdService idService) {
        this.idService = idService;
    }

    @SpringBean("packageDao")
    private void injectPackageDao(PackageDAO packageDAO) {
        this.packageDao = packageDAO;
    }

    public DepositStatusActionBean(){
        super();

        // Ensure desired properties are available.
        try {
            assert(messageKeys.containsKey(MSG_KEY_NULL_OBJECT_ID));
            assert(messageKeys.containsKey(MSG_KEY_NONEXISTENT_OBJECT_ID));
            assert(messageKeys.containsKey(MSG_KEY_STATUS_EMPTY_PACKAGE));
            assert(messageKeys.containsKey(MSG_KEY_ARCHIVE_PROBLEM));
            assert(messageKeys.containsKey(MSG_KEY_BAD_ID_FOR_DATASETS_IN_COLLECTION));
            assert(messageKeys.containsKey(MSG_KEY_BAD_ID_FOR_DATASETS_IN_COLLECTION_IN_PROJECT));
            assert(messageKeys.containsKey(MSG_KEY_STATUS_ERROR_PERMISSION));
        }
        catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of " +
                    MSG_KEY_NULL_OBJECT_ID   + ", " +
                    MSG_KEY_NONEXISTENT_OBJECT_ID     + ", " +
                    MSG_KEY_STATUS_EMPTY_PACKAGE   + ", " +
                    MSG_KEY_ARCHIVE_PROBLEM   + ", " +
                    MSG_KEY_BAD_ID_FOR_DATASETS_IN_COLLECTION   + ", " +
                    MSG_KEY_BAD_ID_FOR_DATASETS_IN_COLLECTION_IN_PROJECT   + ", " +
                    MSG_KEY_STATUS_ERROR_PERMISSION  + " is missing.");
        }
    }

    //Resolution
    @DefaultHandler
    public Resolution render() throws MalformedURLException, IdentifierNotFoundException, UnknownIdentifierTypeException, ArchiveServiceException {
        //Trivial resolution if the objectId is not specified
        if (null == objectId) {
            log.debug("Attempted to retrieve the archival status of a null object id");
            String s = messageKeys.getProperty(MSG_KEY_NULL_OBJECT_ID);
            return new ErrorResolution(400, s);
        }

        // Figure out the type of the identifier
        Identifier id = null;
        Types idType = null;
        try {
            try {
                id = idService.fromUrl(new URL(objectId));
            } catch (MalformedURLException e) {
                // the objectId may not be in the form of a URL, it may be a UID.
                id = idService.fromUid(objectId);
            }
            idType = Types.valueOf(id.getType());
        } catch (Exception e) {
            UnknownIdentifierTypeException uite = new UnknownIdentifierTypeException("Unknown ID [" + objectId + "] " +
                    "type [" + ((idType == null) ? "Unknown" : idType.name()) + "]", e);
            uite.setHttpStatusCode(400);
            throw uite;
        }

        //Poll the archive to update the deposit info.
        try {
            archiveService.pollArchive();
        } catch (ArchiveServiceException e) {
            log.warn("Error polling the archive: " + e.getMessage(), e);
            String s = messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM);
            final String msg = String.format(s, e.getMessage());
            return new ErrorResolution(500, msg);
        }

        switch (idType) {

            case COLLECTION:
            {
                List<ArchiveDepositInfo> depositInfoList = archiveService.listDepositInfo(objectId, null);
                if (depositInfoList.isEmpty()) {
                    fileList.add("Collection " + objectId);
                    statusList.add(NOT_STARTED_STATUS);
                } else {
                    ArchiveDepositInfo depositInfo = depositInfoList.get(0);
                    statusList.add(depositInfo.getDepositStatus().name());
                    if (depositInfo.getDepositStatus() == ArchiveDepositInfo.Status.DEPOSITED) {
                        ArchiveSearchResult<Collection> searchResult =
                                archiveService.retrieveCollection(depositInfo.getDepositId());
                        if (searchResult.getResultCount() < 1) {
                            throw new RuntimeException("Archive status for Collection " + objectId + " is " +
                                    depositInfo.getDepositStatus().name() + ", but no ArchiveSearchResults were found " +
                                    "for its deposit id " + depositInfo.getDepositId());
                        }
                        Collection depositedCollection = searchResult.getResults().iterator().next();
                        fileList.add("Collection \"" + depositedCollection.getTitle() + "\" (" +
                                depositedCollection.getId() + ")");
                    } else {
                        fileList.add("Collection " + objectId);
                    }
                }

                break;
            }

            case PACKAGE:
            {
                final DataItem dehydratedDataSet = new DataItem();
                final Collection collection;
                final Project project;
                final Person person = getAuthenticatedUser();
                final Package thePackage;

                //Grab the package
                thePackage = packageDao.selectPackage(objectId);
                if (null == thePackage) {
                    log.warn("The package was null.");
                    String s = messageKeys.getProperty(MSG_KEY_NULL_OBJECT_ID);
                    final String msg = String.format(s, objectId);
                    return new ErrorResolution(404, msg);
                }
                else if (thePackage.getFileData().entrySet().isEmpty()) {
                    log.warn("The package was empty.");
                    String s = messageKeys.getProperty(MSG_KEY_STATUS_EMPTY_PACKAGE);
                    return new ErrorResolution(500, s);
                }

                //Get the object Id for one of the datasets in the package so that we
                // can build a dehydrated data set and later check for its collection
                // and project.  This code assumes that all the datasets in the package
                // belong to the same collection, and the first one is just a
                // representative of the package for permissions purposes.
                dehydratedDataSet.setId(thePackage.getFileData().entrySet().iterator().next().getKey());

                //Iterate through the objects in the package and get their deposit
                // status.
                for (Map.Entry<String, String> entry : thePackage.getFileData().entrySet()) {
                    fileList.add(entry.getValue());
                    List<ArchiveDepositInfo> depositInfo = archiveService.listDepositInfo(entry.getKey(), null);
                    if (null == depositInfo || depositInfo.isEmpty()) {
                        statusList.add(NOT_STARTED_STATUS);
                    }
                    else
                        statusList.add(depositInfo.get(0).getDepositStatus().toString());
                }

                //Get the associated collection and project so that we can check for
                // permissions.  In this case, dehydratedDataSet is not a fully
                // hydrated DataItem object, but the relationship service only needs
                // the id.
                try {
                    collection = relationshipService.getCollectionForDataSet(dehydratedDataSet);
                    if (null == collection || null == collection.getId()){
                        String s = messageKeys.getProperty(MSG_KEY_BAD_ID_FOR_DATASETS_IN_COLLECTION);
                        return new ErrorResolution(400, s);
                    }
                    project = relationshipService.getProjectForCollection(collection);
                    if (null == project || null == project.getId()){
                        String s = messageKeys.getProperty(MSG_KEY_BAD_ID_FOR_DATASETS_IN_COLLECTION_IN_PROJECT);
                        return new ErrorResolution(400, s);
                    }
                }
                catch (RelationshipConstraintException e) {
                    log.warn("Constraint exception: " + e.getMessage());
                    return new ErrorResolution(500, e.getMessage());
                }

                //Check for roles
                if (!(person.getRoles().contains(Role.ROLE_ADMIN)
                        || relationshipService.getAdministratorsForProject(project).contains(person)
                        || relationshipService.getDepositorsForCollection(collection).contains(person))) {
                    String s = messageKeys.getProperty(MSG_KEY_STATUS_ERROR_PERMISSION);
                    return new ErrorResolution(401, s);
                }

                break;
            }

            default:
                throw new RuntimeException("Cannot determine deposit status of items of type " + idType);
        }



        //Return the deposit status jsp.
        return new ForwardResolution(DEPOSIT_STATUS_PATH);
    }
}
