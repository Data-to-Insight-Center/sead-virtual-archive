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

package org.dataconservancy.ui.it.support;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.services.UserService;

/**
 * Factory for creating various HTTP request objects. The {@link BaseIT}
 * provides this as a {@code protected} member.
 */
public class RequestFactory {

    private final UiUrlConfig urlConfig;

    private final UserService userService;

    private final LoginForm loginForm;

    private final BusinessObjectBuilder builder;

    public RequestFactory(UiUrlConfig urlConfig,
                          UserService userService,
                          LoginForm loginForm,
                          BusinessObjectBuilder builder) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UrlConfig must not be null.");
        }

        if (userService == null) {
            throw new IllegalArgumentException("UserService must not be null.");
        }

        if (loginForm == null) {
            throw new IllegalArgumentException("Login Form must not be null.");
        }

        if (builder == null) {
            throw new IllegalArgumentException("Business Object Builder must not be null.");
        }

        this.urlConfig = urlConfig;
        this.userService = userService;
        this.loginForm = loginForm;
        this.builder = builder;
    }

    /**
     * Creates a request object that can be used for depositing the supplied
     * DataItem and File. <em>N.B.:</em> this method does not throw any
     * exceptions if it receives a {@code null} parameter. This allows for one
     * to craft requests that would be expected to fail if POSTed to an
     * endpoint.
     * 
     * @param dataItem
     *        the dataset to deposit
     * @param collectionId
     *        the collection to deposit the dataset to
     * @param toDeposit
     *        the file that makes up the dataset being deposited
     * @return a request object that is ready to be executed by a properly
     *         configured HTTP client
     */
    public DepositRequest createSingleFileDataItemDepositRequest(DataItem dataItem,
                                                                 String collectionId,
                                                                 File toDeposit) {
        final DepositRequest depositRequest = new DepositRequest(urlConfig);

        if (dataItem != null) {
            depositRequest.setDataItem(dataItem);
        }

        if (collectionId != null) {
            depositRequest.setCollectionId(collectionId);
        }

        if (toDeposit != null) {
            depositRequest.setFileToDeposit(toDeposit);
        }

        return depositRequest;
    }

    /**
     * Creates a request object that can be used for depositing the supplied
     * DataItem and File. <em>N.B.:</em> this method does not throw any
     * exceptions if it receives a {@code null} parameter. This allows for one
     * to craft requests that would be expected to fail if POSTed to an
     * endpoint.
     * 
     * @param thePackage
     *        the package to use for packageId
     * @param dataItem
     *        the dataset to deposit
     * @param collectionId
     *        the collection to deposit the dataset to
     * @param toDeposit
     *        the file that makes up the dataset being deposited
     * @return a request object that is ready to be executed by a properly
     *         configured HTTP client
     */
    public DepositRequest createSingleFileDataItemDepositRequest(org.dataconservancy.ui.model.Package thePackage,
                                                                 DataItem dataItem,
                                                                 String collectionId,
                                                                 File toDeposit) {
        final DepositRequest depositRequest = new DepositRequest(urlConfig);

        if (thePackage != null) {
            depositRequest.setPackage(thePackage);
        }

        if (dataItem != null) {
            depositRequest.setDataItem(dataItem);
        }

        if (collectionId != null) {
            depositRequest.setCollectionId(collectionId);
        }

        if (toDeposit != null) {
            depositRequest.setFileToDeposit(toDeposit);
        }

        return depositRequest;
    }

    /**
     * Creates a request for removing the supplied user as an administrator of
     * the project.
     * 
     * @param userId
     * @param project
     * @return
     */
    public ProjectAdminRequest createRemoveAdminFromProjectRequest(String userId,
                                                                   Project project) {
        final ProjectAdminRequest adminRequest =
                new ProjectAdminRequest(urlConfig);
        adminRequest.removeAdminFromProject(userId, project);
        return adminRequest;
    }

    /**
     * Creates a request for setting the supplied user as an administrator of
     * the project.
     * 
     * @param userId
     * @param project
     * @return
     */
    public ProjectAdminRequest createSetNewAdminForProjectRequest(String userId,
                                                                  Project project) {
        final ProjectAdminRequest adminRequest =
                new ProjectAdminRequest(urlConfig);
        adminRequest.setNewAdminForProject(userId, project);
        return adminRequest;
    }

    /**
     * Creates a request for setting the supplied user as a depositor for the
     * collection.
     * 
     * @param userId
     * @param collectionId
     * @return
     */
    public AuthorizedDepositorRequest createSetNewDepositorRequest(String userId,
                                                                   String collectionId) {
        final AuthorizedDepositorRequest depositorRequest =
                new AuthorizedDepositorRequest(urlConfig);
        depositorRequest.setAuthorizedUserForCollection(userId, collectionId);
        return depositorRequest;
    }

    /**
     * Creates a request for removing the supplied user as a depositor for the
     * collection.
     * 
     * @param userId
     * @param collectionId
     * @return
     */
    public AuthorizedDepositorRequest createRemoveDepositorRequest(String userId,
                                                                   String collectionId) {
        final AuthorizedDepositorRequest depositorRequest =
                new AuthorizedDepositorRequest(urlConfig);
        depositorRequest.removeAuthorizedUserFromCollection(userId,
                                                            collectionId);
        return depositorRequest;
    }

    /**
     * Creates a request for creating a {@link Project}.
     * 
     * @param project
     * @return
     */
    public CreateProjectRequest createProjectRequest(Project project) {
        final CreateProjectRequest createProjectRequest =
                new CreateProjectRequest(urlConfig, userService);
        createProjectRequest.setProject(project);
        return createProjectRequest;
    }

    /**
     * Creates a request for a {@Link Project} with a list of Pis.
     */
    public CreateProjectRequest createProjectRequest(Project project,
                                                     List<Person> pis) {
        final CreateProjectRequest createProjectRequest =
                new CreateProjectRequest(urlConfig, userService);
        List<String> piIds = new ArrayList<String>();
        for (Person pi : pis) {
        	piIds.add(pi.getId());
        }
        		
        project.setPis(piIds);
        createProjectRequest.setProject(project);
        return createProjectRequest;
    }

    /**
     * Creates a request for a {@Link Collection}.
     */
    public CreateCollectionRequest createCollectionRequest(Collection collection,
                                                           Project project) {
        final CreateCollectionRequest createCollectionRequest =
                new CreateCollectionRequest(urlConfig);
        createCollectionRequest.setCollection(collection);
        createCollectionRequest.setProjectId(project.getId());
        return createCollectionRequest;
    }

    public CreateProjectApiAddRequest createProjectApiAddRequest(Project project) {
        return new CreateProjectApiAddRequest(urlConfig, project, builder);
    }

    /**
     * Creates a request used for submitting a form-based login to the UI.
     * 
     * @param p
     * @return
     */
    public LoginRequest createLoginRequest(Person p) {
        return createLoginRequest(p.getEmailAddress(), p.getPassword());
    }

    /**
     * Creates a request used for submitting a form-based login to the UI.
     * 
     * @param user
     * @param pass
     * @return
     */
    public LoginRequest createLoginRequest(String user, String pass) {
        final LoginRequest req =
                new LoginRequest(urlConfig, loginForm, user, pass);
        return req;
    }

    /**
     * Creates a request which logs out the current user.
     * 
     * @return
     */
    public LogoutRequest createLogoutRequest() {
        LogoutRequest req = new LogoutRequest();
        req.setHref(urlConfig.getLogoutUrl().toString());
        return req;
    }

    public RegisterUserRequest createRegisterRequest(Person p) {
        RegisterUserRequest req = new RegisterUserRequest(urlConfig);

        req.setId(p.getId());
        req.setFirstNames(p.getFirstNames());
        req.setLastNames(p.getLastNames());
        req.setMiddleNames(p.getMiddleNames());
        req.setPrefix(p.getPrefix());
        req.setSuffix(p.getSuffix());
        req.setBio(p.getBio());
        req.setWebsite(p.getWebsite());
        req.setJobTitle(p.getJobTitle());
        req.setDepartment(p.getDepartment());
        req.setInstCompany(p.getInstCompany());
        req.setInstCompanyWebsite(p.getInstCompanyWebsite());
        req.setCity(p.getCity());
        req.setState(p.getState());
        req.setEmailAddress(p.getEmailAddress());
        req.setPreferredPubName(p.getPreferredPubName());
        req.setPassword(p.getPassword());
        req.setConfirmedPassword(p.getPassword());
        req.setPhoneNumber(p.getPhoneNumber());

        return req;
    }

    public ApproveRegistrationRequest createApproveRegistrationRequest(Person p) {
        return new ApproveRegistrationRequest(urlConfig, p.getEmailAddress());
    }

    public ListPendingRegistrationsRequest listPendingRegistrations() {
        return new ListPendingRegistrationsRequest(urlConfig);
    }

    public CollectionSplashPageRequest collectionSplashPageRequest(String collectionId) {
        CollectionSplashPageRequest request =
                new CollectionSplashPageRequest(urlConfig);
        request.setCollectionId(collectionId);
        return request;
    }

    public DepositStatusRequest depositStatusRequest(String objectId) {
        DepositStatusRequest depositStatusRequest =
                new DepositStatusRequest(urlConfig);
        depositStatusRequest.setObjectId(objectId);
        return depositStatusRequest;
    }

    public CreateIdApiRequest createIdApiRequest(Types type) {
        CreateIdApiRequest request = new CreateIdApiRequest(urlConfig);
        request.setType(type);
        return request;
    }

    public MetadataFileRequest createMetadataFileRequest() {
        MetadataFileRequest request = new MetadataFileRequest(urlConfig);
        return request;
    }

    public UpdateCollectionRequest createUpdateCollectionRequest(Collection collection) {
        UpdateCollectionRequest request =
                new UpdateCollectionRequest(urlConfig);
        request.setCollection(collection);
        request.setCollectionId(collection.getId());
        return request;
    }

    /**
     * creates a citable locator request for reserving, confirming or canceling
     * a citable locator for a collection
     * 
     * @param collectionId
     * @param reservedCitableLocator
     * @return
     */
    public CitableLocatorRequest createCitableLocatorRequest(String collectionId,
                                                             String reservedCitableLocator) {
        CitableLocatorRequest request = new CitableLocatorRequest(urlConfig);
        request.setCollectionId(collectionId);
        request.setReservedCitableLocator(reservedCitableLocator);
        return request;
    }
    
    /**
     * creates a ValidatingMetadataFileRequest for validation.
     * 
     * @param sampleMetadataFile
     * @param underTest
     * 
     * @return
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public ValidatingMetadataFileRequest createValidatingMetadataFileRequest(File sampleMetadataFile, String formatId) throws MalformedURLException,
            URISyntaxException {
        ValidatingMetadataFileRequest vmfr = new ValidatingMetadataFileRequest(urlConfig);
        vmfr.setFormatId(formatId);
        vmfr.setFileToTest(sampleMetadataFile);
        vmfr.setUpMetadataFile();
        return vmfr;
    }
}
