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

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_PROFILE_UPDATE_ERROR;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_UPDATE_USER_PROFILE_DROPBOX_LINK_FAILURE;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_UPDATE_USER_PROFILE_DROPBOX_LINK_SUCCESS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.storage.dropbox.DropboxAccessor;
import org.dataconservancy.storage.dropbox.model.DropboxToken;
import org.dataconservancy.ui.exceptions.ProfileUpdateException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.DropboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.client2.exception.DropboxException;

/**
 * This is the Action Bean for user profiles. This bean handles viewing user
 * profiles, editing user profiles, adding projects to a profile, and editing
 * profiles associated with a profile.
 */
@UrlBinding("/userprofile/userprofile.action")
public class UserProfileActionBean extends BaseActionBean {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ValidateNestedProperties({
            @Validate(field = "firstNames", on = "updateUserProfile", required = true),
            @Validate(field = "lastNames", on = "updateUserProfile", required = true),
            @Validate(field = "emailAddress", on = "updateUserProfile", required = true, maxlength = 50, converter = EmailTypeConverter.class),
            @Validate(field = "phoneNumber", on = "updateUserProfile", required = true, maxlength = 50),
            @Validate(field = "password", on = "updateUserProfile", required = false, minlength = 5, maxlength = 20),
            @Validate(field = "prefix", on = "updateUserProfile", required = false),
            @Validate(field = "suffix", on = "updateUserProfile", required = false),
            @Validate(field = "preferredPubName", on = "updateUserProfile", required = false),
            @Validate(field = "jobTitle", on = "updateUserProfile", required = true),
            @Validate(field = "department", on = "updateUserProfile", required = true),
            @Validate(field = "city", on = "updateUserProfile", required = true),
            @Validate(field = "state", on = "updateUserProfile", required = true),
            @Validate(field = "instCompany", on = "updateUserProfile", required = true),
            @Validate(field = "instCompanyWebsite", on = "updateUserProfile", required = true),
            @Validate(field = "website", on = "updateUserProfile", required = false),
            @Validate(field = "bio", on = "updateUserProfile", required = false)})
    private Person editedPerson;

    //The oldPassword of the user to be compared when updating the password only.
    @Validate(required = false, on = "updateUserProfile", expression = "this == authenticatedUser.password")
    private String oldPassword;

    //The confirmed new password of to be compared when updating the password only.
    @Validate(required = false, on = "updateUserProfile", expression = "this == editedPerson.password")
    private String confirmedPassword;

    // Currently not used, added suppress warning.
    @SuppressWarnings("unused")
    private IdService idService;
    
    private String dropboxUrl;

    private DropboxService dropboxService;
    
    /**
     * The forward destination when viewing a user's profile
     */
    static final String VIEW_PROFILE_PATH = "/pages/view_user_profile.jsp";

    /**
     * The forward destination when editing a user's profile
     */
    static final String EDIT_PROFILE_PATH = "/pages/edit_user_profile.jsp";
    
    /**
     * The forward destination when editing a user's profile
     */
    static final String LINK_DROPBOX_PATH = "/pages/link_dropbox.jsp";

    public UserProfileActionBean() {
        super();
        try {
            assert (messageKeys.containsKey(MSG_KEY_UPDATE_USER_PROFILE_DROPBOX_LINK_SUCCESS));
            assert (messageKeys.containsKey(MSG_KEY_UPDATE_USER_PROFILE_DROPBOX_LINK_FAILURE));
            assert (messageKeys.containsKey(MSG_KEY_PROFILE_UPDATE_ERROR));
        }
        catch (AssertionError ae) {
            throw new RuntimeException("Missing required message key!  One of "
                    + MSG_KEY_UPDATE_USER_PROFILE_DROPBOX_LINK_SUCCESS + ", "
                    + MSG_KEY_UPDATE_USER_PROFILE_DROPBOX_LINK_FAILURE + ", " + MSG_KEY_PROFILE_UPDATE_ERROR);
        }
    }

    /**
     * Redirects to the uneditable view of the users profile.
     */
    @DefaultHandler
    @DontValidate
    public Resolution viewUserProfile() {
        return new ForwardResolution(VIEW_PROFILE_PATH);
    }

    /**
     * Redirects to the form to allow for editing of the users profile.
     */
    @DontValidate
    public Resolution editUserProfile() {
        return new ForwardResolution(EDIT_PROFILE_PATH);
    }
    
    /**
     * Redirects to the page with further instructions to link user's Dropbox account.
     */
    @DontValidate
    public Resolution linkDropbox() {
        dropboxUrl = getDropboxAccessorInstance().getDropboxOAuthUrl();
        ForwardResolution fr = new ForwardResolution(LINK_DROPBOX_PATH);
        fr.addParameter("dropboxUrl", dropboxUrl);
        return new ForwardResolution(LINK_DROPBOX_PATH);
    }
    
    /**
     * Tests the user's linkage to dropbox.
     */
    @DontValidate
    public Resolution testDropbox() {
        List<Message> successMessages = getContext().getMessages("success");
        List<Message> failureMessages = getContext().getMessages("failure");
        if (dropboxService.testDropboxLink(getAuthenticatedUser().getId())) {
            final String msg = messageKeys.getProperty(MSG_KEY_UPDATE_USER_PROFILE_DROPBOX_LINK_SUCCESS);
            successMessages.add(new SimpleMessage(msg));
            return viewUserProfile();
        }
        else {
            final String msg = messageKeys.getProperty(MSG_KEY_UPDATE_USER_PROFILE_DROPBOX_LINK_FAILURE);
            failureMessages.add(new SimpleMessage(msg));
            return viewUserProfile();
        }
    }
    
    /**
     * redirects back to the view profile page after updating user's account with app key/app secret.
     * 
     * @return resolution
     * @throws DropboxException
     * @throws URISyntaxException
     * @throws IOException
     * @throws MalformedURLException
     */
    @DontValidate
    public Resolution updateProfileWithDropbox() throws MalformedURLException, IOException, URISyntaxException,
            DropboxException {
        DropboxToken token = getDropboxAccessorInstance().getUserAccessTokenPair();
        List<Message> failureMessages = getContext().getMessages("failure");
        if (dropboxService.insertToken(token, getAuthenticatedUser()
                .getId())) {
            userService.updateExternalStorageLinked(getAuthenticatedUser().getId(), true);
            userService.updateDropboxAppKey(getAuthenticatedUser().getId(), token.getAppKey());
            userService.updateDropboxAppSecret(getAuthenticatedUser().getId(), token.getAppSecret());
            return viewUserProfile();
        }
        else {
            final String msg = messageKeys.getProperty(MSG_KEY_PROFILE_UPDATE_ERROR);
            failureMessages.add(new SimpleMessage(msg));
            return viewUserProfile();
        }

    }

    /**
     * Handles the updating of user profiles. The roles and registration status
     * of the user will be set to their current levels. User will be updated in
     * the system when this function finishes.
     */
    public Resolution userProfileUpdated() throws ProfileUpdateException {
        if (editedPerson != null) {
            final Person currentUser = getAuthenticatedUser();
            if (editedPerson.getPassword() == null || editedPerson.getPassword().isEmpty()) {
                editedPerson.setPassword(currentUser.getPassword());
            }

            editedPerson.setRegistrationStatus(currentUser.getRegistrationStatus());

            editedPerson.setRoles(currentUser.getRoles());

            try {
                final String personId = editedPerson.getId();
                userService.updateEmailAddress(personId, editedPerson.getEmailAddress());
                userService.updateFirstNames(personId, editedPerson.getFirstNames());
                userService.updateMiddleNames(personId, editedPerson.getMiddleNames());
                userService.updateLastNames(personId, editedPerson.getLastNames());
                userService.updatePrefix(personId, editedPerson.getPrefix());
                userService.updateSuffix(personId, editedPerson.getSuffix());
                userService.updatePreferredPubName(personId, editedPerson.getPreferredPubName());
                userService.updateBio(personId, editedPerson.getBio());
                userService.updateWebsite(personId, editedPerson.getWebsite());
                userService.updateCity(personId, editedPerson.getCity());
                userService.updateState(personId, editedPerson.getState());
                userService.updateJobTitle(personId, editedPerson.getJobTitle());
                userService.updateDepartment(personId, editedPerson.getDepartment());
                userService.updateInstCompany(personId, editedPerson.getInstCompany());
                userService.updateInstCompanyWebsite(personId, editedPerson.getInstCompanyWebsite());
                userService.updatePassword(personId, editedPerson.getPassword());
                userService.updatePhoneNumber(personId, editedPerson.getPhoneNumber());
                userService.updateExternalStorageLinked(personId, editedPerson.isExternalStorageLinked());
                userService.updateDropboxAppKey(personId, editedPerson.getDropboxAppKey());
                userService.updateDropboxAppSecret(personId, editedPerson.getDropboxAppSecret());
            } catch (RuntimeException e) {
                ProfileUpdateException pue = new ProfileUpdateException(e.getMessage(), e);
                pue.setUserId(currentUser.getId());
                log.warn("Error updating profile: " + pue.getMessage(), pue);
                throw pue;
            }
        }
        return new RedirectResolution(getClass());
    }

    public String getDropboxUrl() {
        return dropboxUrl;
    }

    public String getConfirmedPassword() {
        return confirmedPassword;
    }

    public void setConfirmedPassword(String confirmedPassword) {
        this.confirmedPassword = confirmedPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public Person getEditedPerson() {
        return editedPerson;
    }

    public void setEditedPerson(Person person) {
        this.editedPerson = person;
    }

    @SpringBean("uiIdService")
    public void injectIdService(IdService idService) {
        this.idService = idService;
    }
    
    @SpringBean("dropboxService")
    public void injectFileBizService(DropboxService dropboxService) {
        this.dropboxService = dropboxService;
    }
    
    private DropboxAccessor getDropboxAccessorInstance() {
        return dropboxService.getDropboxInstance();
    }
}
