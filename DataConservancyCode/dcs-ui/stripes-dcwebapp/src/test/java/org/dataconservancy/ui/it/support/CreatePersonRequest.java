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

import java.io.UnsupportedEncodingException;

import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;

/**
 * This class parallels CreateProjectRequest and CreateCollectionRequest I
 * anticipated using this in HttpPersonIT, but didn't need it. Currently not
 * used, but committed in case a later IT card needs it, in the hope that it
 * will save somebody some work. Please update this javadoc if you use this
 * class.
 */
public class CreatePersonRequest {

    private String id;

    private String firstNames;

    private String lastNames;

    private String middleNames;

    private String prefix;

    private String suffix;

    private String preferredPubName;

    private String bio;

    private String website;

    private String password;

    private String emailAddress;

    private String phoneNumber;

    private String jobTitle;

    private String department;

    private String city;

    private String state;

    private String instCompany;

    private String instCompanyWebsite;

    private RegistrationStatus registrationStatus;
    
    private boolean externalStorageLinked;
    
    private String dropboxAppKey;
    
    private String dropboxAppSecret;

    private static final String STRIPES_EVENT = "addPerson";

    private final UiUrlConfig urlConfig;

    private boolean personSet = false;

    public CreatePersonRequest(UiUrlConfig urlConfig) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UiUrlConfig must not be null.");
        }
        this.urlConfig = urlConfig;
    }

    public Person getPerson() {
        Person person = new Person();
        person.setId(id);
        person.setFirstNames(firstNames);
        person.setLastNames(lastNames);
        person.setMiddleNames(middleNames);
        person.setPrefix(prefix);
        person.setSuffix(suffix);
        person.setPreferredPubName(preferredPubName);
        person.setBio(bio);
        person.setWebsite(website);
        person.setPassword(password);
        person.setEmailAddress(emailAddress);
        person.setPhoneNumber(phoneNumber);
        person.setJobTitle(jobTitle);
        person.setDepartment(department);
        person.setCity(city);
        person.setState(state);
        person.setInstCompany(instCompany);
        person.setInstCompanyWebsite(instCompanyWebsite);
        person.setRegistrationStatus(registrationStatus);
        person.setExternalStorageLinked(false);
        person.setDropboxAppKey("SomeKey");
        person.setDropboxAppSecret("SomeSecret");
        return person;
    }

    public void setPerson(Person toCreate) {
        this.id = toCreate.getId();
        this.firstNames = toCreate.getFirstNames();
        this.lastNames = toCreate.getLastNames();
        this.middleNames = toCreate.getMiddleNames();
        this.prefix = toCreate.getPrefix();
        this.suffix = toCreate.getSuffix();
        this.preferredPubName = toCreate.getPreferredPubName();
        this.bio = toCreate.getBio();
        this.website = toCreate.getWebsite();
        this.password = toCreate.getPassword();
        this.emailAddress = toCreate.getEmailAddress();
        this.phoneNumber = toCreate.getPhoneNumber();
        this.jobTitle = toCreate.getJobTitle();
        this.department = toCreate.getDepartment();
        this.city = toCreate.getCity();
        this.state = toCreate.getState();
        this.instCompany = toCreate.getInstCompany();
        this.instCompanyWebsite = toCreate.getInstCompanyWebsite();
        this.registrationStatus = toCreate.getRegistrationStatus();
        this.externalStorageLinked = toCreate.isExternalStorageLinked();
        this.dropboxAppKey = toCreate.getDropboxAppKey();
        this.dropboxAppSecret = toCreate.getDropboxAppSecret();
        personSet = true;
    }

    public void setPersonId(String id) {
        this.id = id;
    }

    public String getPersonId() {
        return id;
    }

    public HttpPost asHttpPost() {
        if (!personSet) {
            throw new IllegalStateException("Person not set: call setPerson(Person) first.");
        }

        HttpPost post = null;
        try {
            post = new HttpPost(urlConfig.getAddCollectionUrl().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("person.id", id));
        params.add(new BasicNameValuePair("person.firstNames", firstNames));
        params.add(new BasicNameValuePair("person.lastNames", lastNames));
        params.add(new BasicNameValuePair("person.middleNames", middleNames));
        params.add(new BasicNameValuePair("person.prefix", prefix));
        params.add(new BasicNameValuePair("person.suffix", suffix));
        params.add(new BasicNameValuePair("person.preferredPubName",
                                          preferredPubName));
        params.add(new BasicNameValuePair("person.bio", bio));
        params.add(new BasicNameValuePair("person.website", website));
        params.add(new BasicNameValuePair("person.password", password));
        params.add(new BasicNameValuePair("person.emailAddress", emailAddress));
        params.add(new BasicNameValuePair("person.phoneNumber", phoneNumber));
        params.add(new BasicNameValuePair("person.jobTitle", jobTitle));
        params.add(new BasicNameValuePair("person.department", department));
        params.add(new BasicNameValuePair("person.city", city));
        params.add(new BasicNameValuePair("person.state", state));
        params.add(new BasicNameValuePair("person.instCompany", instCompany));
        params.add(new BasicNameValuePair("person.instCompanyWebsite", instCompanyWebsite));
        params.add(new BasicNameValuePair("person.registrationStatus",
                                          registrationStatus.toString()));
        params.add(new BasicNameValuePair("person.externalStorageLinked", Boolean.toString(externalStorageLinked)));
        params.add(new BasicNameValuePair("person.dropboxAppKey", dropboxAppKey));
        params.add(new BasicNameValuePair("person.dropboxAppSecret", dropboxAppSecret));
        params.add(new BasicNameValuePair(STRIPES_EVENT, "Add Person"));

        HttpEntity entity = null;

        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        post.setEntity(entity);

        return post;
    }
}
