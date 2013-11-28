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

/**
 * Message keys used to resolve message strings.
 */
public class MessageKey {

    /**
     * Generic key stating that an administrator has been notified
     */
    static final String MSG_KEY_ADMIN_NOTIFIED = "error.administrator-has-been-notified";

    /**
     * Deposit Action Bean Message Key
     */
    static final String MSG_KEY_COLLECTION_DOES_NOT_EXIST = "error.collection-does-not-exist";

    /**
     * Deposit Action Bean Message Key
     */
    static final String MSG_KEY_ERROR_READING_FILE = "error.error-reading-file";

    /**
     * Deposit Action Bean Message Key
     */
    static final String MSG_KEY_ERROR_LACKING_DEPOSIT_PERMISSION = "error.user-does-not-have-deposit-permission";

    /**
     * Deposit Action Bean Message Key
     */
    static final String MSG_KEY_ERROR_DEPOSITING_FILE = "common.error-depositing-file";

    /**
     * Deposit Action Bean Message Key
     */
    static final String MSG_KEY_ERROR_EMPTY_ZIP_FILE = "common.empty-zip";

    /**
     * Deposit Action Bean Message Key
     */
    static final String MSG_KEY_ERROR_RETRIEVING_DATASET = "error.error-retrieving-dataset";

    /**
     * Deposit Action Bean Message Key
     */
    static final String MSG_KEY_ERROR_CHECKING_PERMISSIONS = "error.error-checking-permissions";

    /**
     * Deposit Action Bean Message Key
     */
    static final String MSG_KEY_DATASET_UPDATE_PENDING = "error.dataset-to-update-still-pending";

    /**
     * Deposit Action Bean Message Key
     */
    static final String MSG_KEY_DATASET_TO_UPDATE_NOT_FOUND = "error.dataset-to-update-not-found";

    /**
     * Deposit Action Bean Message Key
     */
    static final String MSG_KEY_NEW_DATA_AVAILABILITY_MESSAGE = "deposit.new-data-availability-message";
    
    /**
     * Project Activity View Message Key
     */
    static final String MSG_KEY_NOT_AUTHORIZED_TO_VIEW_ACTIVITY = "error.sys-or-project-admin-required-to-create-collection";

    /**
     * Edit Discipline to Metadata Format Mapping Action Bean Key
     */
    static final String MSG_KEY_METADATA_FORMAT_DOESNT_EXIST = "error.metadata-format-doesnt-exist";

    /**
     * Edit Discipline to Metadata Format Mapping Action Bean Key
     */
    static final String MSG_KEY_DISCIPLINE_DOESNT_EXIST = "error.discipline-doesnt-exist";
    
    /**
     * Edit Discipline to Metadata Format Mapping Action Bean Key
     */
    static final String MSG_KEY_DISCIPLINE_RELATIONSHIP_SET_ERROR="error.error-setting-discipline-relationships";

     /**
     * Edit Discipline to Metadata Format Mapping Action Bean Key
     */
    static final String MSG_KEY_DISCIPLINE_RELATIONSHIP_REMOVAL_ERROR="error.error-removing-discipline-relationships";

    /**
     *  Add Collection Action Bean Key
     */
    static final String MSG_KEY_SESSION_LOGGED_OUT =  "error.session-logged-out";

    /**
     *  Add Collection Action Bean Key
     */
    static final String MSG_KEY_SYS_OR_PROJECT_ADMIN_REQUIRED_TO_CREATE_COLLECTION =  "error.sys-or-project-admin-required-to-create-collection";

    /**
     *  Add Collection Action Bean Key
     */
    static final String MSG_KEY_COULD_NOT_DEPOSIT_COLLECTION =  "error.could-not-deposit-collection";

    /**
     * Add Collection Action Bean Key.  Parameters are: depositId, collectionId, url to deposit status web page for the collection
     */
    static final String MSG_KEY_POLL_TIMEOUT_WHEN_DEPOSITING_COLLECTION = "error.poll-timeout-when-depositing-collection";

    /**
     *  Add Collection Action Bean Key
     */
    static final String MSG_KEY_METADATA_FILE_UPLOAD_FAIL= "error.metadata-file-upload-failed";
    
    /**
     *  User Collections Action Bean Key
     */
     static final String MSG_KEY_ADD_DEPOSITOR_ERROR = "error.add-depositor-to-collection";

    /**
     *  User Collections Action Bean Key
     */
     static final String MSG_KEY_REMOVE_DEPOSITOR_ERROR = "error.remove-depositor-from-collection";

    /**
     *  User Collections Action Bean Key
     */
     static final String MSG_KEY_REJECT_NONREGISTERED_USER_AS_DEPOSITOR = "error.reject-nonregistered-user-as-depositor";

    /**
     *  User Collections Action Bean Key
     */
     static final String MSG_KEY_REMOVE_ADMIN_AS_DEPOSITOR = "error.remove-admin-as-depositor";

    /**
     *  User Collections Action Bean Key
     */
     static final String MSG_KEY_USERCOLLECTIONS_ADD_DEPOSITOR_SUCCESS = "usercollections.add-depositor-to-collection";

    /**
     *  User Collections Action Bean Key
     */
     static final String MSG_KEY_USERCOLLECTIONS_REMOVE_DEPOSITOR_SUCCESS = "usercollections.remove-depositor-from-collection";

    /**
     *  User Collections Action Bean Key
     */
    static final String MSG_KEY_USER_CANNOT_EDIT_DEPOSITORS = "error.user-can-not-edit-depositors";

    /**
     * User Collections Action Bean Key
     */
    static final String MSG_KEY_PROJECT_FOR_COLLECTION_NOT_FOUND = "error.project-for-collection-not-found";

    /**
     * User Collections Action Bean Key
     */
    static final String MSG_KEY_NO_COLLECTION_FOR_OBJECT_ID= "error.no-collection-for-object-id";

    /**
     * User Collections Action Bean Key
     */
    static final String MSG_KEY_NO_COLLECTION_OR_INFO_OBJECTS_FOR_OBJECT_ID="error.no-collection-or-info-objects-for-object-id";
    
    /**
     * Collection Data List Action Bean Key
     */
    static final String MSG_KEY_COLLECTION_DATA_LIST_NO_COLLECTION = "collection_data_list.no-collection";

    /**
     * Collection Data List Action Bean Key
     */
    static final String MSG_KEY_COLLECTION_DATA_LIST_NO_PERMISSIONS = "collection_data_list.no-permissions";

     /**
     * Collection Data List Action Bean Key
     */
    static final String MSG_KEY_COLLECTION_DATA_LIST_NO_ITEMS = "collection_data_list.no-data-items";

    /**
     * Collection Data List Action Bean Key
     */
    static final String MSG_KEY_COLLECTION_DATA_LIST_NO_COLLECTION_NAME = "collection_data_list.no-collection-name-ensure-deposited";

    /**
     * Collection Data List Action Bean Key
     */
    static final String MSG_KEY_COLLECTION_DATA_LIST_RETRIEVAL_ERROR = "collection_data_list.collection-retrieval-error-try-again";

    /**
     * Collection Data List Action Bean Key
     */
    static final String MSG_KEY_COLLECTION_DATA_LIST_TOTAL_DATA_ITEMS = "collection_data_list.total-data-items";

    /**
     * Project Collection Action Bean
     */
    static final String MSG_KEY_USER_CANNOT_VIEW_COLLECTIONS = "error.user-may-not-view-collections";

    /**
     * DepositStatusActionBean Key
     */
    static final String MSG_KEY_NULL_OBJECT_ID = "error.status-null-object-id";

    /**
     * DepositStatusActionBean Key
     */
    static final String MSG_KEY_NONEXISTENT_OBJECT_ID = "error.status-nonexistent-object-id";

    /**
     * DepositStatusActionBean Key
     */
    static final String MSG_KEY_STATUS_EMPTY_PACKAGE = "error.status-empty-package";

    /**
     * DepositStatusActionBean Key
     */
    static final String MSG_KEY_ARCHIVE_PROBLEM = "error.archive-problem";

    /**
     * DepositStatusActionBean Key
     */
    static final String MSG_KEY_BAD_ID_FOR_DATASETS_IN_COLLECTION = "error.bad-id-for-datasets-in-collection";

    /**
     * DepositStatusActionBean Key
     */
    static final String MSG_KEY_BAD_ID_FOR_DATASETS_IN_COLLECTION_IN_PROJECT =
            "error.bad-id-for-datasets-in-collection-in-project";

    /**
     * DepositStatusActionBean Key
     */
    static final String MSG_KEY_STATUS_ERROR_PERMISSION = "error.status-permissions";

    /** Admin Update Registrations Action Bean Key
     * 
     */
    static final String MSG_KEY_ADMIN_REGISTRATION_UPDATE_SUCCESS = "adminupdateregistrations.successfully-updated-user";

    /**
     * Admin Update Registrations Action Bean Key
     */
    static final String MSG_KEY_ADMIN_REGISTRATION_UPDATE_ERROR = "error.registration-update";

    /**
     * Admin Registrations Action Bean Key
     */
    static final String MSG_KEY_ADMIN_REGISTRATION_APPROVAL_SUCCESS = "adminregistrations.successfully-approved-user";

    /**
     * Admin Registration Action Bean Key
     */
    static final String MSG_KEY_ADMIN_REGISTRATION_APPROVAL_ERROR = "error.registration-approval";

    /**
     * UiExceptionHandler Deposit Error message key
     */
    static final String MSG_KEY_DEPOSIT_ERROR = "error.error-depositing-file";

    /**
     * UiExceptionHandler Profile Update message key
     */
    static final String MSG_KEY_PROFILE_UPDATE_ERROR = "error.error-updating-profile";

    /**
     * UiExceptionHandler View Project Activity message key
     */
    static final String MSG_KEY_VIEW_PROJECT_ACTIVTY_ERROR = "error.error-viewing-project-activity";

    /**
     * UiExceptionHandler Error Updating Registration message key
     */
    static final String MSG_KEY_ERROR_UPDATING_REGISTRATION = "error.error-updating-registration";

    /**
     * UiExceptionHandler Error Updating Relationship message key
     */
    static final String MSG_KEY_ERROR_UPDATING_RELATIONSHIP = "error.setting-removing-relationship-failure";

    /**
     * UiExceptionHandler Error Depositing Collection.  Format parameter is: reason
     */
    static final String MSG_KEY_ERROR_DEPOSITING_COLLECTION = "error.error-depositing-collection";

    /**
     * UiExceptionHandler Generic error
     */
    static final String MSG_KEY_GENERIC_ERROR = "error.generic-error";

    /**
     * Invalid id usually (null or empty)
     */
    static final String MSG_KEY_EMPTY_OR_INVALID_ID = "error.empty-or-invalid-id";

    /**
     * Error trying to retrieve the project for a collection
     */
    static final String MSG_KEY_ERROR_RETRIEVING_PROJECT_FOR_COLLECTION = "error.error-retrieving-project-for-collection";
    
    /**
     * Error data item not found
     */
    static final String MSG_KEY_ERROR_DATA_ITEM_NOT_FOUND = "error.error-dataitem-not-found";
    
    /**
     * Error not authorized to update metadata file
     */
    static final String MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED = "error.error-metadata-file-not-authorized";
    
    /**
     * Error in the biz service while updating the file
     */
    static final String MSG_KEY_ERROR_UPDATING_COLLECTION = "error.error-updating-collection";

    /**
     * Error in the ezid metadata generator
     */
    static final String MSG_KEY_ERROR_CREATING_EZID_METADATA = "error.ezid-metadata-exception";

    /**
     * Error in the ezid metadata generator
     */
    static final String MSG_KEY_ERROR_CREATING_EZID = "error.ezid-service-exception";
    
    /**
     * Error adding the metadata file
     */
    static final String MSG_KEY_ERROR_ADDING_METADATA_FILE = "error.error-metadata-file-adding";

    /**
     * Error finding a password reset request
     */
    static final String MSG_KEY_ERROR_FINDING_PASSWORD_RESET_REQUEST = "error.password-reset-request-not-found";

    /**
     * Error supplied email address does not match address on the password reset request
     */
    static final String MSG_KEY_ERROR_PASSWORD_RESET_REQUEST_EMAIL_ADDRESS = "error.password-request-wrong-email";

    /**
     * PasswordResetActionBean key
     * Error user for supplied email address is not in the system.
     */
    static final String MSG_KEY_ERROR_FINDING_USER_BY_EMAIL = "error.user-not-found-for-email-address";

    /**
     * PasswordResetActionBean key
     * Error creating a password reset request
     */
    static final String MSG_KEY_PASSWORD_RESET_REQUEST_FAIL = "error.password-reset-request-failed";
    
    /**
     * Terms of Use Message
     */
    static final String MSG_KEY_TERMS_OF_USE = "terms.of.use.message";

   /**
     * PasswordResetActionBean key
     * Success creating a password reset request
     */
    static final String MSG_KEY_PASSWORD_RESET_REQUEST_SUCCESS= "success.password-reset-request";

    /**
     * PasswordResetActionBean key
     * Error creating a password reset request object because one already exists
     */
    static final String MSG_KEY_PASSWORD_RESET_REQUEST_EXISTS="error.password-reset-request-exists";
    
    /**
     * MetadataFileActionBean key
     * Error finding the validator registry entry for the selected format.
     */
    static final String MSG_KEY_VALIDATOR_ENTRY_NOT_FOUND="error.metadata-validator-not-found";
    
    /**
     * MetadataFileActionBean key
     * Updated file has already been validated.
     */
    static final String MSG_KEY_FILE_ALREADY_VALIDATED="metadata.file-already-validated";
    
    /**
     * MetadataFileActionBean key
     * Metadata Format Not Available in the List.
     */
    static final String MSG_KEY_FORMAT_NOT_AVAILABLE="metadata.format-not-available";
    
    /**
     * MetadataFileActionBean key
     * Metadata Format doesn't validate
     */
    static final String MSG_KEY_FORMAT_NO_VALIDATION="metadata.format-no-validation";

    /**
     * MetadataFileActionBean key
     * Error parsing information to be displayed from metadata attribute.
     */
    static final String MSG_KEY_METADATA_ATTRIBUTE_PARSE_FAIL="metadata.metadata-attribute-parse-fail";

    /**
     * MetadataFileActionBean key
     * Error getting relationship between collection and metadata file
     */
    static final String MSG_KEY_METADATA_RELATIONSHIP_FAIL="metadata.metadata-relationship-fail";
    
    /**
     * UserProfileActionBean key
     * Dropbox link test was successful.
     */
    static final String MSG_KEY_UPDATE_USER_PROFILE_DROPBOX_LINK_SUCCESS="update.user.profile.dropbox-link-success";
    
    /**
     * UserProfileActionBean key
     * Dropbox link test was a failure.
     */
    static final String MSG_KEY_UPDATE_USER_PROFILE_DROPBOX_LINK_FAILURE="update.user.profile.dropbox-link-failure";
    
    /**
     * IngestPackageActionBean key Ingest submitted.
     */
    static final String MSG_KEY_INGEST_SUBMITTED = "ingest.submitted";
    
    /**
     * IngestPackageActionBean key How to get status info.
     */
    static final String MSG_KEY_HOW_TO_GET_INGEST_STATUS = "how.to.get.ingest.status";
}                                                                          
