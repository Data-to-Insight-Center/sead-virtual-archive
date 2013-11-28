/* Copyright 2012 Johns Hopkins University
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

package org.dataconservancy.ui.model.builder.xstream;

/**
 * ConverterConstants holds all the constants for all the Converter classes.
 * 
 * @author Payam Meyer
 * @version $Id$
 */
public interface ConverterConstants {

    /**
     * The bop element name.
     */
    public static final String E_BOP = "bop";

    /**
     * The projects element name.
     */
    public static final String E_PROJECTS = "projects";

    /**
     * The collections element name.
     */
    public static final String E_COLLECTIONS = "collections";

    /**
     * The persons element name.
     */
    public static final String E_PERSONS = "persons";

    /**
     * The dataItems element name.
     */
    public static final String E_DATA_ITEMS = "dataItems";

    /**
     * The collection element name.
     */
    public static final String E_COLLECTION = "collection";

    /**
     * The collection title element name.
     */
    public static final String E_COL_TITLE = "title";

    /**
     * The id element name.
     */
    public static final String E_ID = "id";

    /**
     * The collection summary element name.
     */
    public static final String E_COL_SUMMARY = "summary";

    /**
     * The collection alternate ids element name.
     */
    public static final String E_COL_ALTERNATE_IDS = "alternateIds";

    /**
     * The collection alternate id element name.
     */
    public static final String E_COL_ALTERNATE_ID = "alternateId";

    /**
     * The collection children ids element name.
     */
    public static final String E_COL_CHILDREN_IDS = "childrenIds";
    
    /**
     * The collection children id element name.
     */
    public static final String E_COL_CHILDREN_ID = "childrenId";
    
    /**
     * The collection parent id element name.
     */
    public static final String E_COL_PARENT_ID = "parentId";
    
    /**
     * The collection citable locator element name.
     */
    public static final String E_COL_CITABLE_LOCATOR = "citableLocator";

    /**
     * The collection contact infos element name.
     */
    public static final String E_COL_CONTACT_INFOS = "contactInfos";

    /**
     * The collection contact info element name.
     */
    public static final String E_COL_CONTACT_INFO = "contactInfo";

    /**
     * The collection creators element name.
     */
    public static final String E_COL_CREATORS = "creators";

    /**
     * The collection creator element name.
     */
    public static final String E_COL_CREATOR = "creator";

    /**
     * The collection publication date element name.
     */
    public static final String E_COL_PUBLICATION_DATE = "publicationDate";

    /**
     * The collection deposit date element name.
     */
    public static final String E_COL_DEPOSIT_DATE = "depositDate";

    /**
     * The collection depositor element name.
     */
    public static final String E_COL_DEPOSITOR = "depositor";

    /**
     * The name element name.
     */
    public static final String E_NAME = "name";

    /**
     * The source element name.
     */
    public static final String E_SOURCE = "source";

    /**
     * The format element name.
     */
    public static final String E_FORMAT = "format";

    /**
     * The path element name.
     */
    public static final String E_PATH = "path";

    /**
     * The fileSize element name.
     */
    public static final String E_SIZE = "fileSize";

    /**
     * The dataItem element name.
     */
    public static final String E_DATA_ITEM = "dataItem";

    /**
     * The description element name.
     */
    public static final String E_DESCRIPTION = "description";

    /**
     * The depositor element name.
     */
    public static final String E_DEPOSITOR = "depositor";

    /**
     * The deposit date element name.
     */
    public static final String E_DEPOSIT_DATE = "depositDate";

    /**
     * The files element name.
     */
    public static final String E_FILES = "files";

    /**
     * The ref attribute name.
     */
    public static final String ATTR_REF = "ref";

    /**
     * The date element name.
     */
    public static final String E_DATE = "date";

    /**
     * The person element name.
     */
    public static final String E_PERSON = "person";

    /**
     * The first name element name.
     */
    public static final String E_FIRST_NAME = "firstName";

    /**
     * The last name element name.
     */
    public static final String E_LAST_NAME = "lastName";

    /**
     * The middle name element name.
     */
    public static final String E_MIDDLE_NAME = "middleName";

    /**
     * The prefix element name.
     */
    public static final String E_PREFIX = "prefix";

    /**
     * The suffix element name.
     */
    public static final String E_SUFFIX = "suffix";

    /**
     * The preferred published name element name.
     */
    public static final String E_PREFERRED_PUB_NAME = "preferredPubName";

    /**
     * The bio element name.
     */
    public static final String E_BIO = "bio";

    /**
     * The website element name.
     */
    public static final String E_WEBSITE = "website";

    /**
     * The job title element name.
     */
    public static final String E_JOB_TITLE = "jobTitle";

    /**
     * The department element name.
     */
    public static final String E_DEPARTMENT = "department";

    /**
     * The city element name.
     */
    public static final String E_CITY = "city";

    /**
     * The state element name.
     */
    public static final String E_STATE = "state";

    /**
     * The institution/company element name.
     */
    public static final String E_INST_COMPANY = "instCompany";
    
    /**
     * The institution/company website element name.
     */
    public static final String E_INST_COMPANY_WEBSITE = "instCompanyWebsite";
    
    /**
     * The external storage linked flag element name.
     */
    public static final String E_EXTERNAL_STORAGE_LINKED = "externalStorageLinked";
    
    /**
     * The dropbox app key element name.
     */
    public static final String E_DROPBOX_APP_KEY = "dropboxAppKey";
    
    /**
     * The dropbox app secret element name.
     */
    public static final String E_DROPBOX_APP_SECRET = "dropboxAppSecret";

    /**
     * The password element name.
     */
    public static final String E_PASSWORD = "password";

    /**
     * The email address element name.
     */
    public static final String E_EMAIL_ADDRESS = "emailAddress";

    /**
     * The phone number element name.
     */
    public static final String E_PHONE_NUMBER = "phoneNumber";

    /**
     * The registration status element name.
     */
    public static final String E_REGISTRATION_STATUS = "registrationStatus";

    /**
     * The read only element name.
     */
    public static final String E_READ_ONLY = "readOnly";

    /**
     * The roles element name.
     */
    public static final String E_ROLES = "roles";

    /**
     * The role element name.
     */
    public static final String E_ROLE = "role";

    /**
     * The given names element name.
     */
    public static final String E_GIVEN_NAMES = "givenNames";

    /**
     * The family names element name.
     */
    public static final String E_FAMILY_NAMES = "familyNames";

    /**
     * The middle names element name.
     */
    public static final String E_MIDDLE_NAMES = "middleNames";

    /**
     * The name prefix element name.
     */
    public static final String E_NAME_PREFIX = "prefix";

    /**
     * The name suffix element name.
     */
    public static final String E_NAME_SUFFIX = "suffix";

    /**
     * The Project element name.
     */
    public static final String E_PROJECT = "project";

    /**
     * The project number element name.
     */
    public static final String E_NUMBER = "number";

    /**
     * The publisher element name.
     */
    public static final String E_PUBLISHER = "publisher";

    /**
     * The storage allocation element name.
     */
    public static final String E_STORAGEALLOCATED = "storageAllocated";

    /**
     * The storage used element name.
     */
    public static final String E_STORAGEUSED = "storageUsed";

    /**
     * The start date element name.
     */
    public static final String E_STARTDATE = "startDate";

    /**
     * The end date element name.
     */
    public static final String E_ENDDATE = "endDate";

    /**
     * The funding entity element name.
     */
    public static final String E_FUNDINGENTITY = "fundingEntity";

    /**
     * The PI id entity element name.
     */
    public static final String E_PRINCIPLEINVESTIGATORID = "piID";

    /**
     * The contact name element name.
     */
    public static final String E_CONTACT_NAME = "name";

    /**
     * The contact role element name.
     */
    public static final String E_CONTACT_ROLE = "role";

    /**
     * The contact email address element name.
     */
    public static final String E_CONTACT_EMAIL = "emailAddress";

    /**
     * The contact phone number element name.
     */
    public static final String E_CONTACT_PHONE = "phoneNumber";

    /**
     * The physical address element name.
     */
    public static final String E_CONTACT_ADDRESS = "physicalAddress";

    /**
     * The street address element name.
     */
    public static final String E_STREET_ADDRESS = "streetAddress";

    /**
     * The zipcode element name.
     */
    public static final String E_ZIP_CODE = "zipCode";

    /**
     * The country element name.
     */
    public static final String E_COUNTRY = "country";

    /**
     * The dataItemTransport element name.
     */
    public static final String E_DATA_ITEM_TRANSPORT = "dataItemTransport";

    /**
     * The initial deposit date element name.
     */
    public static final String E_INITIAL_DEPOSIT_DATE = "initialDepositDate";

    /**
     * The deposit status element name.
     */
    public static final String E_DEPOSIT_STATUS = "depositStatus";

    /**
     * The metadata files element name
     */
    public static final String E_METADATA_FILES = "metadataFiles";

    /**
     * The metadata file element name
     */
    public static final String E_METADATA_FILE = "metadataFile";

    /**
     * The medatada format field name
     */
    public static final String E_METADATA_FORMAT = "metadataFormat";
    
    /**
     * Generic reference to an object's parent, e.g. A MetadataFile's reference to the object it describes, or a
     * DataFile's reference to its DataItem, or a DataItem's reference to a parent Collection.
     */
    public static final String E_PARENT_ID = "parentId";
    
    public static final String E_COL_PARENT_PROJECT = "parentProjectId";

}