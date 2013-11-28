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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 */
public class RegisterUserRequest {

    private final static String STRIPES_EVENT = "register";

    private final UiUrlConfig config;

    private String id;

    private String firstNames;

    private String lastNames;

    private String middleNames;

    private String prefix;

    private String suffix;

    private String preferredPubName;

    private String city;

    private String state;

    private String jobTitle;

    private String department;

    private String website;

    private String bio;

    private String instCompany;
    
    private String instCompanyWebsite;

    private String password;

    private String confirmedPassword;

    private String emailAddress;

    private String phoneNumber;

    public RegisterUserRequest(UiUrlConfig config) {
        this.config = config;
    }

    public RegisterUserRequest(UiUrlConfig config,
                               String id,
                               String emailAddress,
                               String firstNames,
                               String lastNames,
                               String middleNames,
                               String prefix,
                               String suffix,
                               String preferredPubName,
                               String password,
                               String confirmedPassword,
                               String phoneNumber,
                               String city,
                               String state,
                               String jobTitle,
                               String department,
                               String website,
                               String bio,
                               String instCompany,
                               String instCompanyWebsite) {
        this.config = config;
        this.id = id;
        this.emailAddress = emailAddress;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.middleNames = middleNames;
        this.prefix = prefix;
        this.suffix = suffix;
        this.preferredPubName = preferredPubName;
        this.password = password;
        this.confirmedPassword = confirmedPassword;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.state = state;
        this.jobTitle = jobTitle;
        this.department = department;
        this.website = website;
        this.bio = bio;
        this.instCompany = instCompany;
        this.instCompanyWebsite = instCompanyWebsite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmedPassword() {
        return confirmedPassword;
    }

    public void setConfirmedPassword(String confirmedPassword) {
        this.confirmedPassword = confirmedPassword;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public HttpPost asHttpPost() {
        HttpPost form = new HttpPost(config.getRegistrationUrl().toString());
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user.id", id));
        params.add(new BasicNameValuePair("user.firstNames", getFirstNames()));
        params.add(new BasicNameValuePair("user.lastNames", getLastNames()));
        params.add(new BasicNameValuePair("user.middleNames", getMiddleNames()));
        params.add(new BasicNameValuePair("user.prefix", prefix));
        params.add(new BasicNameValuePair("user.suffix", suffix));
        params.add(new BasicNameValuePair("user.preferredPubName",
                                          preferredPubName));
        params.add(new BasicNameValuePair("user.website", website));
        params.add(new BasicNameValuePair("user.jobTitle", jobTitle));
        params.add(new BasicNameValuePair("user.department", department));
        params.add(new BasicNameValuePair("user.city", city));
        params.add(new BasicNameValuePair("user.state", state));
        params.add(new BasicNameValuePair("user.instCompany", instCompany));
        params.add(new BasicNameValuePair("user.instCompanyWebsite", instCompanyWebsite));
        params.add(new BasicNameValuePair("user.password", password));
        params.add(new BasicNameValuePair("confirmedPassword",
                                          confirmedPassword));
        params.add(new BasicNameValuePair("user.emailAddress", emailAddress));
        params.add(new BasicNameValuePair("user.phoneNumber", phoneNumber));
        params.add(new BasicNameValuePair(STRIPES_EVENT, "Submit"));
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        form.setEntity(entity);
        return form;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getPreferredPubName() {
        return preferredPubName;
    }

    public void setPreferredPubName(String preferredPubName) {
        this.preferredPubName = preferredPubName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getInstCompany() {
        return instCompany;
    }

    public void setInstCompany(String instCompany) {
        this.instCompany = instCompany;
    }
    
    public String getInstCompanyWebsite() {
        return instCompanyWebsite;
    }
    
    public void setInstCompanyWebsite(String instCompanyWebsite) {
        this.instCompanyWebsite = instCompanyWebsite;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public void setFirstNames(String firstNames) {
        this.firstNames = firstNames;
    }

    public String getLastNames() {
        return lastNames;
    }

    public void setLastNames(String lastNames) {
        this.lastNames = lastNames;
    }

    public String getMiddleNames() {
        return middleNames;
    }

    public void setMiddleNames(String middleNames) {
        this.middleNames = middleNames;
    }

}
