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

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.apache.http.HttpStatus;

import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.EZIDMetadataException;
import org.dataconservancy.ui.exceptions.EZIDServiceException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.AuthorizationService;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.EZIDService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.util.EZIDCollectionMetadataGeneratorImpl;
import org.dataconservancy.ui.util.EZIDMetadata;
import org.dataconservancy.ui.util.EZIDMetadataGenerator;

/**
 * {@code CitableLocatorActionBean} handles requests to create, reserve and/or
 * delete {@code CitableLocator} string by contacting {@link EZIDService}.
 */
@UrlBinding("/collection/citable_locator_confirmation.action")
public class CitableLocatorActionBean
        extends BaseActionBean {

    /**
     * The starting path
     */
    static final String HOME_CITABLE_LOCATOR_PATH =
            "/pages/citable_locator_confirmation.jsp";

    private String collectionId;

    private Collection collection;

    private CollectionBizService collectionBizService;

    private AuthorizationService authorizationService;

    private EZIDService ezidService;

    private EZIDMetadataGenerator ezidMetadataGenerator;

    private String reservedCitableLocator;

    //resolution for sending a reservation back to the citable locator view

    //resolution for sending the minted locator to the collection splash view

    //resolution for reserving the DOI

    @DefaultHandler
    public Resolution reserveDOI() throws BizInternalException,
            BizPolicyException, RelationshipConstraintException,
            EZIDMetadataException, EZIDServiceException {

        //Hydrating the collection: used the passed in collection id to get collection from the archive
        collection = getCollection();

        //If collection with collectionId could not be found
        if (collection == null) {
            //throw exception
            return new ErrorResolution(HttpStatus.SC_NOT_FOUND,
                                       "Collection with id " + collectionId
                                               + " could not be found");
        }
        Person currentUser = getAuthenticatedUser();
        //Check for permission, consulting authorization service
        if (!authorizationService.canUpdateCollection(currentUser, collection)) {
            return new ErrorResolution(HttpStatus.SC_FORBIDDEN,
                                       "User "
                                               + currentUser.getFirstNames()
                                               + " "
                                               + currentUser.getLastNames()
                                               + " (id: "
                                               + currentUser.getId()
                                               + " ) does not have permission to request new DOI for this collection.");
        }

        //exception thrown by metadataGenerator.generateMetadata is handled by the UiExceptionHandler
        EZIDMetadata metadata =
                ezidMetadataGenerator.generateMetadata(collection);

        //exception thrown by the ezid service is handled by the UIExceptionHandler, who will put error messages
        //onto the flash scope and trigger notification email.
        reservedCitableLocator = ezidService.createID(metadata);
        //reservedCitableLocator = "tempEZID";

        //if all goes well and no exception was thrown: render the confirmation page
        return new ForwardResolution(HOME_CITABLE_LOCATOR_PATH);
    }

    public Resolution confirmDOI() throws BizInternalException,
            BizPolicyException, RelationshipConstraintException,
            EZIDServiceException {
        //obtain currently logged in user
        Person currentUser = getAuthenticatedUser();

        //obtain the collection identifying by collectionId
        collection = getCollection();
        //If collection with collectionId could not be found
        if (collection == null) {
            //throw exception
            return new ErrorResolution(HttpStatus.SC_NOT_FOUND,
                                       "Collection with id " + collectionId
                                               + " could not be found");
        }

        //Check for permission, consulting authorization service
        if (!authorizationService.canUpdateCollection(currentUser, collection)) {
            return new ErrorResolution(HttpStatus.SC_FORBIDDEN,
                                       "User "
                                               + currentUser.getFirstNames()
                                               + " "
                                               + currentUser.getLastNames()
                                               + " (id: "
                                               + currentUser.getId()
                                               + " ) does not have permission to request new DOI for this collection.");
        }

        //Call to ezid service save the ID
        //exception handling is handled by the UIExceptionHandler
        ezidService.saveID(reservedCitableLocator);

        //Set the collection's citable locator to the new DOI
        collection.setCitableLocator(reservedCitableLocator);

        //call to collection biz service to update the collection in the archive with new citable locator
        //exception thrown by the collectionBizService when updating collection is handled by UIExceptionHandler
        collectionBizService.updateCollection(collection,
                                              getAuthenticatedUser());

        //If no exception has been thrown by this point: re-direct user to collection details page
        RedirectResolution res =
                new RedirectResolution(UserCollectionsActionBean.class,
                                       "viewCollectionDetails");
        res.addParameter("selectedCollectionId", collectionId);
        return res;
    }

    public Resolution cancel() throws EZIDServiceException,
            RelationshipConstraintException, BizInternalException,
            BizPolicyException {
        //obtain currently logged in user
        Person currentUser = getAuthenticatedUser();

        collection = getCollection();

        //If collection with collectionId could not be found
        if (collection == null) {
            //throw exception
            return new ErrorResolution(HttpStatus.SC_NOT_FOUND,
                                       "Collection with id " + collectionId
                                               + " could not be found");
        }
        //Check for permission, consulting authorization service
        if (!authorizationService.canUpdateCollection(currentUser, collection)) {
            return new ErrorResolution(HttpStatus.SC_FORBIDDEN,
                                       "User "
                                               + currentUser.getFirstNames()
                                               + " "
                                               + currentUser.getLastNames()
                                               + " (id: "
                                               + currentUser.getId()
                                               + " ) does not have permission to request new DOI for this collection.");
        }

        //Call ezid service to delete the ID
        //exception thrown by the ezidService when deleting ID is handled by the UiExceptionHandler
        ezidService.deleteID(reservedCitableLocator);

        //If no exception is thrown: re-direct user to collection details page
        RedirectResolution res =
                new RedirectResolution(UserCollectionsActionBean.class,
                                       "viewCollectionDetails");
        res.addParameter("selectedCollectionId", collectionId);
        return res;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public void setReservedCitableLocator(String reservedCitableLocator) {
        this.reservedCitableLocator = reservedCitableLocator;
    }

    public String getReservedCitableLocator() {
        return reservedCitableLocator;
    }

    private Collection getCollection() throws BizInternalException,
            BizPolicyException {
        return collectionBizService.getCollection(collectionId,
                                                  getAuthenticatedUser());
    }

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
     * Stripes-injected authorizationService
     * 
     * @param authorizationService
     */
    @SpringBean("authorizationService")
    public void injectAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * Stripes-injected ezidService
     * 
     * @param ezidService
     */
    @SpringBean("ezidService")
    public void injectEZIDService(EZIDService ezidService) {
        this.ezidService = ezidService;
    }

    /**
     * Stripes-injected ezidMetadataService
     * 
     * @param ezidCollectionMetadataGenerator
     */
    @SpringBean("ezidCollectionMetadataGenerator")
    public void injectEZIDMetadataService(EZIDCollectionMetadataGeneratorImpl ezidCollectionMetadataGenerator) {
        this.ezidMetadataGenerator = ezidCollectionMetadataGenerator;
    }

}
