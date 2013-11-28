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

package org.dataconservancy.ui.model.builder.xstream;

import java.io.IOException;

import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLAssert;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dataconservancy.ui.model.builder.xstream.ConverterTestConstants.COLLECTIONS_WRAPPER;
import static org.dataconservancy.ui.model.builder.xstream.ConverterTestConstants.METADATA_FILES_WRAPPER;
import static org.dataconservancy.ui.model.builder.xstream.ConverterTestConstants.PERSONS_WRAPPER;
import static org.dataconservancy.ui.model.builder.xstream.ConverterTestConstants.PROJECTS_WRAPPER;
import static org.dataconservancy.ui.model.builder.xstream.DataFileConverter.E_FILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: HanhVu Date: 5/17/12 Time: 11:47 PM To change
 * this template use File | Settings | File Templates.
 */
public class BopConverterTest
        extends BaseConverterTest
        implements ConverterConstants {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final DateTimeFormatter fmt = DateTimeFormat
            .forPattern("d M yyyy");

    static final String XMLNS = "http://dataconservancy.org/schemas/bop/1.0";

    private static Bop collectionsBop;

    private static Bop projectsBop;

    private static Bop personBop;

    private static Bop dataItemBop;

    private String COLLECTIONS_BOP_XML;

    private String PROJECTS_BOP_XML;

    private String PERSONS_XML;

    private String FILE_XML1;

    private String FILE_XML2;

    private String DI_XML1;

    private String DI_XML2;

    private String DI_BOP_XML;

    private String MF_XML_1;

    private String MF_XML_2;

    private String MF_BOP_XML;


    private void setupXMLStrings() {
        
       COLLECTIONS_BOP_XML =
                "<" + E_BOP + " xmlns=\"" + XMLNS + "\"" +
                        "      xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI + "\"\n" +
                        "      xsi:schemaLocation=\"" + XMLNS + "\">" +
                        "<" + E_COLLECTIONS + ">\n" +
                        
                    "    <" + E_COLLECTION + " " + E_ID + "=\"" + collectionWithData.getId() + "\">\n" +
                    "        <" + E_COL_ALTERNATE_IDS + ">\n" +
                    "            <" + E_COL_ALTERNATE_ID + ">" + collectionWithData.getAlternateIds().get(0) + "</" + E_COL_ALTERNATE_ID + ">\n" +
                    "        </" + E_COL_ALTERNATE_IDS + ">\n" +
                    "        <" + E_COL_TITLE + ">" + collectionWithData.getTitle() + "</" + E_COL_TITLE + ">\n" +
                    "        <" + E_COL_SUMMARY + ">" + collectionWithData.getSummary() + "</" + E_COL_SUMMARY + ">\n" +
                    "        <" + E_COL_CITABLE_LOCATOR + ">" + collectionWithData.getCitableLocator() + "</" + E_COL_CITABLE_LOCATOR + ">\n" +
                    "        <" + E_COL_CONTACT_INFOS + ">\n" +
                    "            <" + E_COL_CONTACT_INFO + ">\n" +
                    "                <" + E_CONTACT_NAME + ">" + contactInfoOne.getName() + "</" + E_CONTACT_NAME + ">\n" +
                    "                <" + E_CONTACT_ROLE + ">" + contactInfoOne.getRole() + "</" + E_CONTACT_ROLE + ">\n" +
                    "                <" + E_CONTACT_EMAIL + ">" + contactInfoOne.getEmailAddress() + "</" + E_CONTACT_EMAIL + ">\n" +
                    "                <" + E_CONTACT_PHONE + ">" + contactInfoOne.getPhoneNumber() + "</" + E_CONTACT_PHONE + ">\n" +
                    "                <" + E_CONTACT_ADDRESS + ">\n" +
                    "                    <" + E_STREET_ADDRESS + ">" + contactInfoOne.getPhysicalAddress().getStreetAddress() + "</" + E_STREET_ADDRESS + ">\n" +
                    "                    <" + E_CITY + ">" + contactInfoOne.getPhysicalAddress().getCity() + "</" + E_CITY + ">\n" +
                    "                    <" + E_COUNTRY + ">" + contactInfoOne.getPhysicalAddress().getCountry() + "</" + E_COUNTRY + ">\n" +
                    "                </" + E_CONTACT_ADDRESS + ">\n" +
                    "            </" + E_COL_CONTACT_INFO + ">" +
                    "            <" + E_COL_CONTACT_INFO + ">\n" +
                    "                <" + E_CONTACT_NAME + ">" + contactInfoTwo.getName() + "</" + E_CONTACT_NAME + ">\n" +
                    "                <" + E_CONTACT_ROLE + ">" + contactInfoTwo.getRole() + "</" + E_CONTACT_ROLE + ">\n" +
                    "                <" + E_CONTACT_EMAIL + ">" + contactInfoTwo.getEmailAddress() + "</" + E_CONTACT_EMAIL + ">\n" +
                    "                <" + E_CONTACT_PHONE + ">" + contactInfoTwo.getPhoneNumber() + "</" + E_CONTACT_PHONE + ">\n" +
                    "            </" + E_COL_CONTACT_INFO + ">\n" +
                    "        </" + E_COL_CONTACT_INFOS + ">\n" +
                    "        <" + E_COL_CREATORS + ">\n" +
                    "            <" + E_COL_CREATOR + ">\n" +
                    "                <" + E_GIVEN_NAMES + ">" + creatorOne.getGivenNames() + "</" + E_GIVEN_NAMES + ">\n" +
                    "                <" + E_MIDDLE_NAMES + ">" + creatorOne.getMiddleNames() + "</" + E_MIDDLE_NAMES + ">\n" +
                    "                <" + E_FAMILY_NAMES + ">" + creatorOne.getFamilyNames() + "</" + E_FAMILY_NAMES + ">\n" +
                    "                <" + E_NAME_PREFIX + ">" + creatorOne.getPrefixes() + "</" + E_NAME_PREFIX + ">\n" +
                    "                <" + E_NAME_SUFFIX + ">" + creatorOne.getSuffixes() + "</" + E_NAME_SUFFIX + ">\n" +
                    "            </" + E_COL_CREATOR + ">\n" +
                    "            <" + E_COL_CREATOR + ">\n" +
                    "                <" + E_GIVEN_NAMES + ">" + creatorTwo.getGivenNames() + "</" + E_GIVEN_NAMES + ">\n" +
                    "                <" + E_MIDDLE_NAMES + ">" + creatorTwo.getMiddleNames() + "</" + E_MIDDLE_NAMES + ">\n" +
                    "                <" + E_FAMILY_NAMES + ">" + creatorTwo.getFamilyNames() + "</" + E_FAMILY_NAMES + ">\n" +
                    "                <" + E_NAME_PREFIX + ">" + creatorTwo.getPrefixes() + "</" + E_NAME_PREFIX + ">\n" +
                    "                <" + E_NAME_SUFFIX + ">" + creatorTwo.getSuffixes() + "</" + E_NAME_SUFFIX + ">\n" +
                    "            </" + E_COL_CREATOR + ">\n" +
                    "        </" + E_COL_CREATORS + ">\n" +
                    "        <" + E_COL_PUBLICATION_DATE + ">\n" +
                    "            <" + E_DATE + ">" + fmt.print(collectionWithData.getPublicationDate()) + "</" + E_DATE + ">\n" +
                    "        </" + E_COL_PUBLICATION_DATE + ">\n" +
                    "        <" + E_COL_DEPOSIT_DATE + ">\n" +
                    "            <" + E_DATE + ">" + fmt.print(collectionWithData.getDepositDate()) + "</" + E_DATE + ">\n" +
                    "        </" + E_COL_DEPOSIT_DATE + ">\n" +
                    "        <" + E_COL_DEPOSITOR + ">" + admin.getId() + "</" + E_COL_DEPOSITOR + ">\n" +
                    "        <" + E_COL_PARENT_PROJECT + ">" + projectOne.getId() + "</" + E_COL_PARENT_PROJECT + ">\n" +
                    "    </" + E_COLLECTION + ">\n" +
                    "    <" + E_COLLECTION + " " + E_ID + "=\"" + collectionNoData.getId() + "\">\n" +
                    "        <" + E_COL_TITLE + ">" + collectionNoData.getTitle() + "</" + E_COL_TITLE + ">\n" +
                    "        <" + E_COL_SUMMARY + ">" + collectionNoData.getSummary() + "</" + E_COL_SUMMARY + ">\n" +
                    "        <" + E_COL_PUBLICATION_DATE + ">\n" +
                    "            <" + E_DATE + ">" + fmt.print(collectionNoData.getPublicationDate()) + "</" + E_DATE + ">\n" +
                    "        </" + E_COL_PUBLICATION_DATE + ">\n" +
                    "        <" + E_COL_DEPOSIT_DATE + ">\n" +
                    "            <" + E_DATE + ">" + fmt.print(collectionNoData.getDepositDate()) + "</" + E_DATE + ">\n" +
                    "        </" + E_COL_DEPOSIT_DATE + ">\n" +
                    "        <" + E_COL_DEPOSITOR + ">" + admin.getId() + "</" + E_COL_DEPOSITOR + ">\n" +
                    "        <" + E_COL_PARENT_PROJECT + ">" + projectOne.getId() + "</" + E_COL_PARENT_PROJECT + ">\n" +
                    "    </" + E_COLLECTION + ">\n" +
                    "</" + E_COLLECTIONS + ">\n" +
                    "</" + E_BOP + ">";
       
       PROJECTS_BOP_XML =
               "<" + E_BOP + " xmlns=\"" + XMLNS + "\"" +
                       "      xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI + "\"\n" +
                       "      xsi:schemaLocation=\"" + XMLNS + "\">" +
                       "<" + E_PROJECTS + ">\n" +
                       "<" + E_PROJECT + " " + E_ID + "=\"" + projectOne.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                       "       <" + E_NAME + ">" + projectOne.getName() + "</" + E_NAME + ">\n" +
                       "       <" + E_NUMBER + ">" + projectOne.getNumbers().get(0) + "</" + E_NUMBER + ">\n" +
                       "       <" + E_NUMBER + ">" + projectOne.getNumbers().get(1) + "</" + E_NUMBER + ">\n" +
                       "       <" + E_DESCRIPTION + ">" + projectOne.getDescription() + "</" + E_DESCRIPTION + ">\n" +
                       "       <" + E_PUBLISHER + ">" + projectOne.getPublisher() + "</" + E_PUBLISHER + ">\n" +
                       "       <" + E_STORAGEALLOCATED + ">" + projectOne.getStorageAllocated() + "</" + E_STORAGEALLOCATED + ">\n" +
                       "       <" + E_STORAGEUSED + ">" + projectOne.getStorageUsed() + "</" + E_STORAGEUSED + ">\n" +
                       "       <" + E_STARTDATE + ">\n" +
                       "           <" + E_DATE + ">" + fmt.print(projectOne.getStartDate()) + "</" + E_DATE + ">\n" +
                       "       </" + E_STARTDATE + ">\n" +
                       "       <" + E_ENDDATE + ">\n" +
                       "           <" + E_DATE + ">" + fmt.print(projectOne.getEndDate()) + "</" + E_DATE + ">\n" +
                       "       </" + E_ENDDATE + ">\n" +
                       "       <" + E_PRINCIPLEINVESTIGATORID + ">" + projectOne.getPis().get(0) + "</" + E_PRINCIPLEINVESTIGATORID + ">\n" +
                       "       <" + E_FUNDINGENTITY + ">" + projectOne.getFundingEntity() + "</" + E_FUNDINGENTITY + ">\n" +
                       "    </" + E_PROJECT + ">" +
                       "<" + E_PROJECT + " " + E_ID + "=\"" + projectTwo.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                       "       <" + E_NAME + ">" + projectTwo.getName() + "</" + E_NAME + ">\n" +
                       "       <" + E_NUMBER + ">" + projectTwo.getNumbers().get(0) + "</" + E_NUMBER + ">\n" +
                       "       <" + E_NUMBER + ">" + projectTwo.getNumbers().get(1) + "</" + E_NUMBER + ">\n" +
                       "       <" + E_DESCRIPTION + ">" + projectTwo.getDescription() + "</" + E_DESCRIPTION + ">\n" +
                       "       <" + E_PUBLISHER + ">" + projectTwo.getPublisher() + "</" + E_PUBLISHER + ">\n" +
                       "       <" + E_STORAGEALLOCATED + ">" + projectTwo.getStorageAllocated() + "</" + E_STORAGEALLOCATED + ">\n" +
                       "       <" + E_STORAGEUSED + ">" + projectTwo.getStorageUsed() + "</" + E_STORAGEUSED + ">\n" +
                       "       <" + E_STARTDATE + ">\n" +
                       "           <" + E_DATE + ">" + fmt.print(projectTwo.getStartDate()) + "</" + E_DATE + ">\n" +
                       "       </" + E_STARTDATE + ">\n" +
                       "       <" + E_ENDDATE + ">\n" +
                       "           <" + E_DATE + ">" + fmt.print(projectTwo.getEndDate()) + "</" + E_DATE + ">\n" +
                       "       </" + E_ENDDATE + ">\n" +
                       "       <" + E_PRINCIPLEINVESTIGATORID + ">" + projectTwo.getPis().get(0) + "</" + E_PRINCIPLEINVESTIGATORID + ">\n" +
                       "       <" + E_FUNDINGENTITY + ">" + projectTwo.getFundingEntity() + "</" + E_FUNDINGENTITY + ">\n" +
                       "    </" + E_PROJECT + ">" +
                       "</" + E_PROJECTS + ">\n" +
                       "</" + E_BOP + ">";
       
       PERSONS_XML =
               "<" + E_BOP + " xmlns=\"" + XMLNS + "\"" +
                   "      xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI + "\"\n" +
                   "      xsi:schemaLocation=\"" + XMLNS + "\">" +
                   "<" + E_PERSONS + ">\n" +
                   "    <" + E_PERSON + " " + E_ID + "=\"" + user.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                   "        <" + E_PREFIX + ">" + user.getPrefix() + "</" + E_PREFIX + ">\n" +
                   "        <" + E_FIRST_NAME + ">" + user.getFirstNames() + "</" + E_FIRST_NAME + ">\n" +
                   "        <" + E_MIDDLE_NAME + ">" + user.getMiddleNames() +"</" + E_MIDDLE_NAME + ">\n" +
                   "        <" + E_LAST_NAME + ">" + user.getLastNames() + "</" + E_LAST_NAME + ">\n" +
                   "        <" + E_SUFFIX + ">" + user.getSuffix() + "</" + E_SUFFIX + ">\n" +
                   "        <" + E_EMAIL_ADDRESS + ">" + user.getEmailAddress() + "</" + E_EMAIL_ADDRESS + ">\n" +
                   "        <" + E_PREFERRED_PUB_NAME + ">" + user.getPreferredPubName() + "</" + E_PREFERRED_PUB_NAME + ">\n" +
                   "        <" + E_BIO + ">" + user.getBio() + "</" + E_BIO + ">\n" +
                   "        <" + E_WEBSITE + ">" + user.getWebsite() + "</" + E_WEBSITE + ">\n" +
                   "        <" + E_CITY + ">" + user.getCity() + "</" + E_CITY + ">\n" +
                   "        <" + E_STATE + ">" + user.getState() + "</" + E_STATE + ">\n" +
                   "        <" + E_JOB_TITLE + ">" + user.getJobTitle() + "</" + E_JOB_TITLE + ">\n" +
                   "        <" + E_DEPARTMENT + ">" + user.getDepartment() + "</" + E_DEPARTMENT + ">\n" +
                   "        <" + E_INST_COMPANY + ">" + user.getInstCompany() + "</" + E_INST_COMPANY + ">\n" +
                   "        <" + E_INST_COMPANY_WEBSITE + ">" + user.getInstCompanyWebsite() + "</" + E_INST_COMPANY_WEBSITE + ">\n" +
                   "        <" + E_PHONE_NUMBER + ">" + user.getPhoneNumber() + "</" + E_PHONE_NUMBER + ">\n" +
                   "        <" + E_PASSWORD + ">" + user.getPassword() + "</" + E_PASSWORD + ">\n" +
                   "        <" + E_EXTERNAL_STORAGE_LINKED + ">" + Boolean.toString(user.isExternalStorageLinked()) + "</" + E_EXTERNAL_STORAGE_LINKED + ">\n" +
                   "        <" + E_DROPBOX_APP_KEY + ">" + user.getDropboxAppKey() + "</" + E_DROPBOX_APP_KEY + ">\n" +
                   "        <" + E_DROPBOX_APP_SECRET + ">" + user.getDropboxAppSecret() + "</" + E_DROPBOX_APP_SECRET + ">\n" +
                   "        <" + E_REGISTRATION_STATUS + ">" + user.getRegistrationStatus().name() + "</" + E_REGISTRATION_STATUS + ">\n" +
                   "        <" + E_READ_ONLY + ">" + user.getReadOnly() + "</" + E_READ_ONLY + ">\n" +
                   "        <" + E_ROLE + ">" + user.getRoles().get(0) + "</" + E_ROLE + ">\n" +
                   "    </" + E_PERSON + ">\n" +
                   "    <" + E_PERSON + " " + E_ID + "=\"" + admin.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                   "        <" + E_PREFIX + ">" + admin.getPrefix() + "</" + E_PREFIX + ">\n" +
                   "        <" + E_FIRST_NAME + ">" + admin.getFirstNames() + "</" + E_FIRST_NAME + ">\n" +
                   "        <" + E_MIDDLE_NAME + ">" + admin.getMiddleNames() +"</" + E_MIDDLE_NAME + ">\n" +
                   "        <" + E_LAST_NAME + ">" + admin.getLastNames() + "</" + E_LAST_NAME + ">\n" +
                   "        <" + E_SUFFIX + ">" + admin.getSuffix() + "</" + E_SUFFIX + ">\n" +
                   "        <" + E_EMAIL_ADDRESS + ">" + admin.getEmailAddress() + "</" + E_EMAIL_ADDRESS + ">\n" +
                   "        <" + E_PREFERRED_PUB_NAME + ">" + admin.getPreferredPubName() + "</" + E_PREFERRED_PUB_NAME + ">\n" +
                   "        <" + E_BIO + ">" + admin.getBio() + "</" + E_BIO + ">\n" +
                   "        <" + E_WEBSITE + ">" + admin.getWebsite() + "</" + E_WEBSITE + ">\n" +
                   "        <" + E_CITY + ">" + admin.getCity() + "</" + E_CITY + ">\n" +
                   "        <" + E_STATE + ">" + admin.getState() + "</" + E_STATE + ">\n" +
                   "        <" + E_JOB_TITLE + ">" + admin.getJobTitle() + "</" + E_JOB_TITLE + ">\n" +
                   "        <" + E_DEPARTMENT + ">" + admin.getDepartment() + "</" + E_DEPARTMENT + ">\n" +
                   "        <" + E_INST_COMPANY + ">" + admin.getInstCompany() + "</" + E_INST_COMPANY + ">\n" +
                   "        <" + E_INST_COMPANY_WEBSITE + ">" + admin.getInstCompanyWebsite() + "</" + E_INST_COMPANY_WEBSITE + ">\n" +
                   "        <" + E_PHONE_NUMBER + ">" + admin.getPhoneNumber() + "</" + E_PHONE_NUMBER + ">\n" +
                   "        <" + E_PASSWORD + ">" + admin.getPassword() + "</" + E_PASSWORD + ">\n" +
                   "        <" + E_EXTERNAL_STORAGE_LINKED + ">" + Boolean.toString(admin.isExternalStorageLinked()) + "</" + E_EXTERNAL_STORAGE_LINKED + ">\n" +
                   "        <" + E_DROPBOX_APP_KEY + ">" + admin.getDropboxAppKey() + "</" + E_DROPBOX_APP_KEY + ">\n" +
                   "        <" + E_DROPBOX_APP_SECRET + ">" + admin.getDropboxAppSecret() + "</" + E_DROPBOX_APP_SECRET + ">\n" +
                   "        <" + E_REGISTRATION_STATUS + ">" + admin.getRegistrationStatus().name() + "</" + E_REGISTRATION_STATUS + ">\n" +
                   "        <" + E_READ_ONLY + ">" + admin.getReadOnly() + "</" + E_READ_ONLY + ">\n" +
                   "        <" + E_ROLE + ">" + admin.getRoles().get(0) + "</" + E_ROLE + ">\n" +
                   "        <" + E_ROLE + ">" + admin.getRoles().get(1) + "</" + E_ROLE + ">\n" +
                   "    </" + E_PERSON + ">\n" +
                   "</" + E_PERSONS + ">\n" +
               "</" + E_BOP + ">";

       FILE_XML1 =
               "            <" + E_FILE + " " + DataFileConverter.E_ID + "=\"" + dataFileOne.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
               "                <" + E_PARENT_ID + ">" + dataItemOne.getId() + "</" + E_PARENT_ID + ">\n" +
               "                <" + DataFileConverter.E_NAME + ">" + dataFileOne.getName() + "</" + DataFileConverter.E_NAME + ">\n" +
               "                <" + E_SOURCE + ">" + dataFileOne.getSource() + "</" + E_SOURCE + ">\n" +
               "                <" + E_PATH + ">" + dataFileOne.getPath() + "</" + E_PATH + ">\n" +
               "                <" + E_SIZE + ">" + String.valueOf(dataFileOne.getSize()) + "</" + E_SIZE + ">\n" +
               "            </" + E_FILE + ">\n";

       FILE_XML2 =
               "            <" + E_FILE + " " + DataFileConverter.E_ID + "=\"" + dataFileTwo.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
               "                <" + E_PARENT_ID + ">" + dataItemTwo.getId() + "</" + E_PARENT_ID + ">\n" +
               "                <" + DataFileConverter.E_NAME + ">" + dataFileTwo.getName() + "</" + DataFileConverter.E_NAME + ">\n" +
               "                <" + E_SOURCE + ">" + dataFileTwo.getSource() + "</" + E_SOURCE + ">\n" +
               "                <" + E_PATH + ">" + dataFileTwo.getPath() + "</" + E_PATH + ">\n" +
               "                <" + E_SIZE + ">" + String.valueOf(dataFileTwo.getSize()) + "</" + E_SIZE + ">\n" +
               "            </" + E_FILE + ">\n";
       
       DI_XML1 =
               "    <" + E_DATA_ITEM + " " + DataItemConverter.E_ID + "=\"" + dataItemOne.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                   "        <" + DataItemConverter.E_NAME + ">" + dataItemOne.getName() + "</" + DataItemConverter.E_NAME + ">\n" +
                   "        <" + DataItemConverter.E_DESCRIPTION + ">" + dataItemOne.getDescription() + "</" + DataItemConverter.E_DESCRIPTION + ">\n" +
                   "        <" + E_DEPOSITOR + " " + DataItemConverter.ATTR_REF + "=\"" + admin.getId() + "\" " +"/>\n" +
                   "        <" + E_DEPOSIT_DATE + ">\n" +
                   "           <" + E_DATE + ">" + fmt.print(dataItemOne.getDepositDate()) + "</" + E_DATE + ">\n" +
                   "        </" + E_DEPOSIT_DATE + ">\n" +
                   "        <" + E_FILES + ">\n" + FILE_XML1 +
                   "        </" + E_FILES + ">\n" +
                   "        <" + E_PARENT_ID + ">" + collectionWithData.getId() + "</" + E_PARENT_ID + ">\n" +
                   "    </" + E_DATA_ITEM + ">\n";
       
       DI_XML2 =
               "    <" + E_DATA_ITEM + " " + DataItemConverter.E_ID + "=\"" + dataItemTwo.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
               "        <" + DataItemConverter.E_NAME + ">" + dataItemTwo.getName() + "</" + DataItemConverter.E_NAME + ">\n" +
               "        <" + DataItemConverter.E_DESCRIPTION + ">" + dataItemTwo.getDescription() + "</" + DataItemConverter.E_DESCRIPTION + ">\n" +
               "        <" + E_DEPOSITOR + " " + DataItemConverter.ATTR_REF + "=\"" + admin.getId() + "\" " +"/>\n" +
               "        <" + E_DEPOSIT_DATE + ">\n" +
               "           <" + E_DATE + ">" + fmt.print(dataItemTwo.getDepositDate()) + "</" + E_DATE + ">\n" +
               "        </" + E_DEPOSIT_DATE + ">\n" +
               "        <" + E_FILES + ">\n" + FILE_XML2 +
               "        </" + E_FILES + ">\n" +
               "        <" + E_PARENT_ID + ">" + collectionWithData.getId() + "</" + E_PARENT_ID + ">\n" +
               "    </" + E_DATA_ITEM + ">\n";
       
       DI_BOP_XML =
               "<" + E_BOP + " xmlns=\"" + XMLNS + "\"" +
                   "      xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI + "\"\n" +
                   "      xsi:schemaLocation=\"" + XMLNS + "\">\n" +
                   "<" + E_DATA_ITEMS + ">\n" +
                   DI_XML1 +
                   DI_XML2 +
                   "</" + E_DATA_ITEMS + ">\n" +
                   "</" + E_BOP + ">";
       MF_XML_1 =
                "    <" + E_METADATA_FILE + " " + MetadataFileConverter.E_ID + "=\"" + metadataFileOne.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                "        <" + E_PARENT_ID + ">" + metadataFileOne.getParentId() + "</" + E_PARENT_ID + ">\n" +
                "        <" + E_SOURCE + ">" + metadataFileOne.getSource() + "</" + E_SOURCE + ">\n" +
                "        <" + E_FORMAT + ">" + metadataFileOne.getFormat() + "</" + E_FORMAT + ">\n" +
                "        <" + E_NAME + ">" + metadataFileOne.getName() + "</" + E_NAME + ">\n" +
                "        <" + E_PATH + ">" + metadataFileOne.getPath() + "</" + E_PATH + ">\n" +
                "        <" + E_METADATA_FORMAT + ">" + metadataFileOne.getMetadataFormatId() + "</" + E_METADATA_FORMAT + ">\n" +
                "    </" + E_METADATA_FILE + ">\n";

       MF_XML_2 =
                "    <" + E_METADATA_FILE + " " + MetadataFileConverter.E_ID + "=\"" + metadataFileTwo.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                        "        <" + E_PARENT_ID + ">" + metadataFileTwo.getParentId() + "</" + E_PARENT_ID + ">\n" +
                        "        <" + E_SOURCE + ">" + metadataFileTwo.getSource() + "</" + E_SOURCE + ">\n" +
                        "        <" + E_FORMAT + ">" + metadataFileTwo.getFormat() + "</" + E_FORMAT + ">\n" +
                        "        <" + E_NAME + ">" + metadataFileTwo.getName() + "</" + E_NAME + ">\n" +
                        "        <" + E_PATH + ">" + metadataFileTwo.getPath() + "</" + E_PATH + ">\n" +
                        "        <" + E_METADATA_FORMAT + ">" + metadataFileTwo.getMetadataFormatId() + "</" + E_METADATA_FORMAT + ">\n" +
                "    </" + E_METADATA_FILE + ">\n";

       MF_BOP_XML =
                   "<" + E_BOP + " xmlns=\"" + XMLNS + "\"" +
                   "      xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI + "\"\n" +
                   "      xsi:schemaLocation=\"" + XMLNS + "\">\n" +
                   "<" + E_METADATA_FILES+ ">\n" +
                   MF_XML_1 +
                   MF_XML_2 +
                   "</" + E_METADATA_FILES + ">\n" +
                   "</" + E_BOP + ">";

    }

    @Before
    public void setUp() throws Exception {

        super.setUp();
        setupXMLStrings();

        projectsBop = new Bop();
        projectsBop.addProject(projectOne);
        projectsBop.addProject(projectTwo);

        personBop = new Bop();
        personBop.addPerson(admin);
        personBop.addPerson(user);

        dataItemBop = new Bop();
        dataItemOne.setDepositorId(admin.getId());
        dataItemBop.addDataItem(dataItemOne);
        dataItemTwo.setDepositorId(admin.getId());
        dataItemBop.addDataItem(dataItemTwo);

        collectionsBop = new Bop();
        collectionWithData.setDepositorId(admin.getId());
        collectionsBop.addCollection(collectionWithData);
        collectionNoData.setDepositorId(admin.getId());
        collectionsBop.addCollection(collectionNoData);
    }

    @Test
    public void testMarshalPersonBop() throws IOException, SAXException {
        Diff diff = new Diff(PERSONS_XML, x.toXML(personBop));
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual("Expected: " + PERSONS_XML + " Actual: "
                                         + x.toXML(personBop),
                                 diff,
                                 true);
    }

    @Test
    public void testUnmarshalPersonsBop() throws IOException, SAXException {
        assertEquals(2, personBop.getPersons().size());
        assertEquals(2, ((Bop) x.fromXML(x.toXML(personBop))).getPersons()
                .size());
        Bop actual = (Bop) x.fromXML(PERSONS_XML);
        assertEquals(personBop.getPersons().size(), actual.getPersons().size());
        final Set<Person> actualPersons = actual.getPersons();
        final Set<Person> expectedPersons = personBop.getPersons();
        assertTrue(expectedPersons.equals(actualPersons));
        assertEquals(personBop, actual);
        Assert.assertEquals(personBop.getPersons(),
                            ((Bop) x.fromXML(x.toXML(personBop))).getPersons());
    }

    @Test
    public void testMarshalCollectionsBop() throws IOException, SAXException {
        Diff diff = new Diff(COLLECTIONS_BOP_XML, x.toXML(collectionsBop));
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual("Expected: " + COLLECTIONS_BOP_XML
                + " Actual: " + x.toXML(collectionsBop), diff, true);
    }

    /**
     * Test the serialization of bop with projects in it.
     * 
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void testMarshalProjectsBop() throws IOException, SAXException {
        Diff diff = new Diff(PROJECTS_BOP_XML, x.toXML(projectsBop));
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual("Expected: " + PROJECTS_BOP_XML + " Actual: "
                + x.toXML(projectsBop), diff, true);
    }

    @Test
    public void testUnmarshalCollectionsBop() throws IOException, SAXException {
        assertEquals(2, collectionsBop.getCollections().size());
        assertEquals(2, ((Bop) x.fromXML(x.toXML(collectionsBop)))
                .getCollections().size());
        Bop actual = (Bop) x.fromXML(COLLECTIONS_BOP_XML);
        assertEquals(collectionsBop.getProjects(), actual.getProjects());
        final Set<Collection> actualCollections = actual.getCollections();
        final Set<Collection> expectedCollections =
                collectionsBop.getCollections();
        assertEquals(expectedCollections, actualCollections);
        assertEquals(collectionsBop, actual);
        Assert.assertEquals(collectionsBop.getCollections(), ((Bop) x.fromXML(x
                .toXML(collectionsBop))).getCollections());
    }

    @Test
    public void testMarshalDataItemBop() throws IOException, SAXException {
        Diff diff = new Diff(DI_BOP_XML, x.toXML(dataItemBop));
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
        XMLAssert.assertXMLEqual("Expected: " + DI_BOP_XML + " Actual: "
                                         + x.toXML(dataItemBop),
                                 diff,
                                 true);
    }

    @Test
    public void testUnmarshalDataItemsBop() throws IOException, SAXException {
        assertEquals(2, dataItemBop.getDataItems().size());
        assertEquals(2, ((Bop) x.fromXML(x.toXML(dataItemBop))).getDataItems()
                .size());
        Bop actual = (Bop) x.fromXML(DI_BOP_XML);
        assertEquals(dataItemBop.getDataItems(), actual.getDataItems());
        final Set<DataItem> actualDIs = actual.getDataItems();
        final Set<DataItem> expectedDIs = dataItemBop.getDataItems();
        assertTrue(expectedDIs.equals(actualDIs));
        assertEquals(dataItemBop, actual);
        Assert.assertEquals(dataItemBop.getDataItems(), ((Bop) x.fromXML(x
                .toXML(dataItemBop))).getDataItems());
    }

    /**
     * Test which insures that the expected collection XML is valid, marshaled collection  XML is valid, and round-tripped collection XML is valid.
     *
     * @throws Exception
     */
    @Test
    public void testCollectionMarshalIsValid() throws Exception {
        // Verify assumptions: test that our expected XML is valid.
        final String expectedXml = COLLECTIONS_BOP_XML;

        final Document expectedDom = parser.parse(IOUtils.toInputStream(expectedXml));
        validator.validate(new DOMSource(expectedDom));

        // Verify that our serialized XML is valid.
        final String actualXml = String.format(BOP_WRAPPER, String.format(COLLECTIONS_WRAPPER, x.toXML(collectionWithData)));
        final Document actualDom = parser.parse(IOUtils.toInputStream(actualXml));
        validator.validate(new DOMSource(actualDom));

        // Verify that round-tripped XML is valid (XML -> Object -> XML)
        final String roundTrippedXml = x.toXML(x.fromXML(COLLECTIONS_BOP_XML));
        final Document roundTrippedDom = parser.parse(IOUtils.toInputStream(roundTrippedXml));
        validator.validate(new DOMSource(roundTrippedDom));

    }

    /**
     * Test which insures that the expected project XML is valid, marshaled project  XML is valid, and round-tripped project XML is valid.
     *
     * @throws Exception
     */
    @Test
    public void testProjectMarshalIsValid() throws Exception {
        // Verify assumptions: test that our expected XML is valid.
        final String expectedXml = PROJECTS_BOP_XML;

        final Document expectedDom = parser.parse(IOUtils.toInputStream(expectedXml));
        validator.validate(new DOMSource(expectedDom));

        // Verify that our serialized XML is valid.
        final String actualXml = String.format(BOP_WRAPPER, String.format(PROJECTS_WRAPPER, x.toXML(projectOne)));
        final Document actualDom = parser.parse(IOUtils.toInputStream(actualXml));
        validator.validate(new DOMSource(actualDom));

        // Verify that round-tripped XML is valid (XML -> Object -> XML)
        final String roundTrippedXml =  x.toXML(x.fromXML(PROJECTS_BOP_XML));
        final Document roundTrippedDom = parser.parse(IOUtils.toInputStream(roundTrippedXml));
        validator.validate(new DOMSource(roundTrippedDom));

    }

    /**
     * Test which ensures that the expected person XML is valid, marshaled person XML is valid, and round-tripped person
     * XML is valid.
     * 
     * @throws Exception
     */
    @Test
    public void testPersonMarshalIsValid() throws Exception {
        // Verify assumptions: test that our expected XML is valid.
        final String expectedXml = PERSONS_XML;

        final Document expectedDom = parser.parse(IOUtils.toInputStream(expectedXml));
        validator.validate(new DOMSource(expectedDom));

        // Verify that our serialized XML is valid.
        final String actualXml = String.format(BOP_WRAPPER, String.format(PERSONS_WRAPPER, x.toXML(user)));
        final Document actualDom = parser.parse(IOUtils.toInputStream(actualXml));
        validator.validate(new DOMSource(actualDom));

        // Verify that round-tripped XML is valid (XML -> Object -> XML)
        final String roundTrippedXml = x.toXML(x.fromXML(PERSONS_XML));
        final Document roundTrippedDom = parser.parse(IOUtils.toInputStream(roundTrippedXml));
        validator.validate(new DOMSource(roundTrippedDom));

    }

    /**
     * Test which insures that the expected metadata file XML is valid, marshaled metadata file  XML is valid, and round-tripped metadata file XML is valid.
     *
     * @throws Exception
     */
    //TODO: update bop converter to process metadata files
    @Test
    public void testMetadataFilesMarshalIsValid() throws Exception {
        // Verify assumptions: test that our expected XML is valid.
        final String expectedXml = MF_BOP_XML;

        final Document expectedDom = parser.parse(IOUtils.toInputStream(expectedXml));
        validator.validate(new DOMSource(expectedDom));


        //Verify that our serialized XML is valid.
        final String actualXml = String.format(BOP_WRAPPER, String.format(METADATA_FILES_WRAPPER, x.toXML(metadataFileOne)));
        final Document actualDom = parser.parse(IOUtils.toInputStream(actualXml));
        validator.validate(new DOMSource(actualDom));

        // Verify that round-tripped XML is valid (XML -> Object -> XML)
        final String roundTrippedXml = x.toXML(x.fromXML(MF_BOP_XML));
        final Document roundTrippedDom = parser.parse(IOUtils.toInputStream(roundTrippedXml));
        validator.validate(new DOMSource(roundTrippedDom));

    }
}
