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

import org.dataconservancy.ui.model.Person;

/**
 * This class provides programmatic access to the form elements of the
 * registration jsp page {@code /pages/registration.jsp} and the profile edit
 * jsp page {@code /pages/edit_user_profile}. The
 * {@link org.dataconservancy.ui.stripes.RegistrationActionBean} renders the
 * registration form and the
 * {@link org.dataconservancy.ui.stripes.UserProfileActionBean} renders the
 * profile edit form. Spring Security is the form controller for both forms.
 * <p/>
 * Programmatic access to the form elements provide necessary information for
 * simulating user registration by POSTing to the form. This class is generally
 * provided by Spring, and configured in the Spring Application Context.
 */
public class ProfileForm {

    private String firstNamesFieldName;

    private String middleNamesFieldName;

    private String lastNamesFieldName;

    private String prefixFieldName;

    private String suffixFieldName;

    private String preferredPubNameFieldName;

    private String emailFieldName;

    private String websiteFieldName;

    private String phoneNumberFieldName;

    private String cityFieldName;

    private String stateFieldName;

    private String jobTitleFieldName;

    private String departmentFieldName;

    private String instCompanyFieldName;
    
    private String instCompanyWebsiteFieldName;

    private String bioFieldName;

    private String passwordFieldName;

    private String confirmedPasswordFieldName;

    private String oldPasswordFieldName;

    private String actionPath;

    public String getFirstNamesFieldName() {
        return firstNamesFieldName;
    }

    public void setFirstNamesFieldName(String firstNamesFieldName) {
        this.firstNamesFieldName = firstNamesFieldName;
    }

    public String getLastNamesFieldName() {
        return lastNamesFieldName;
    }

    public void setLastNamesFieldName(String lastNamesFieldName) {
        this.lastNamesFieldName = lastNamesFieldName;
    }

    public String getEmailFieldName() {
        return emailFieldName;
    }

    public void setEmailFieldName(String emailFieldName) {
        this.emailFieldName = emailFieldName;
    }

    public void setPhoneNumberFieldName(String phoneFieldName) {
        this.phoneNumberFieldName = phoneFieldName;
    }

    public String getPhoneNumberFieldName() {
        return phoneNumberFieldName;
    }

    public String getPasswordFieldName() {
        return passwordFieldName;
    }

    public void setPasswordFieldName(String passwordFieldName) {
        this.passwordFieldName = passwordFieldName;
    }

    public String getConfirmedPasswordFieldName() {
        return confirmedPasswordFieldName;
    }

    public void setConfirmedPasswordFieldName(String confirmedPasswordFieldName) {
        this.confirmedPasswordFieldName = confirmedPasswordFieldName;
    }

    public String getOldPasswordFieldName() {
        return oldPasswordFieldName;
    }

    public void setOldPasswordFieldName(String oldPasswordFieldName) {
        this.oldPasswordFieldName = oldPasswordFieldName;
    }

    public String getActionPath() {
        return actionPath;
    }

    public void setActionPath(String actionPath) {
        this.actionPath = actionPath;
    }

    /**
     * Creates a filled out registration form to use in programatic registration
     * tests.
     */
    public HttpPost createRegistrationPost(UiUrlConfig config, Person registerAs, String confirmedPassword) {
        String registrationFormActionUrl = config.getRegistrationUrl().toString();

        HttpPost registrationFormPost = new HttpPost(registrationFormActionUrl);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(firstNamesFieldName, registerAs.getFirstNames()));
        params.add(new BasicNameValuePair(middleNamesFieldName, registerAs.getMiddleNames()));
        params.add(new BasicNameValuePair(lastNamesFieldName, registerAs.getLastNames()));
        params.add(new BasicNameValuePair(prefixFieldName, registerAs.getPrefix()));
        params.add(new BasicNameValuePair(suffixFieldName, registerAs.getSuffix()));
        params.add(new BasicNameValuePair(preferredPubNameFieldName, registerAs.getPreferredPubName()));
        params.add(new BasicNameValuePair(emailFieldName, registerAs.getEmailAddress()));
        params.add(new BasicNameValuePair(websiteFieldName, registerAs.getWebsite()));
        params.add(new BasicNameValuePair(phoneNumberFieldName, registerAs.getPhoneNumber()));
        params.add(new BasicNameValuePair(cityFieldName, registerAs.getCity()));
        params.add(new BasicNameValuePair(stateFieldName, registerAs.getState()));
        params.add(new BasicNameValuePair(jobTitleFieldName, registerAs.getJobTitle()));
        params.add(new BasicNameValuePair(departmentFieldName, registerAs.getDepartment()));
        params.add(new BasicNameValuePair(bioFieldName, registerAs.getBio()));
        params.add(new BasicNameValuePair(instCompanyFieldName, registerAs.getInstCompany()));
        params.add(new BasicNameValuePair(instCompanyWebsiteFieldName, registerAs.getInstCompanyWebsite()));
        params.add(new BasicNameValuePair(passwordFieldName, registerAs.getPassword()));
        params.add(new BasicNameValuePair(confirmedPasswordFieldName, confirmedPassword));
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        registrationFormPost.setEntity(entity);
        return registrationFormPost;
    }

    /**
     * Creates a filled out profile edit form to use in programatic testing.
     */
    public HttpPost editProfilePost(UiUrlConfig config, Person editAs, String oldPassword, String confirmedPassword) {
        String editProfileFormActionUrl = config.getBaseUrl() + actionPath;

        HttpPost editProfileFormPost = new HttpPost(editProfileFormActionUrl);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(firstNamesFieldName, editAs.getFirstNames()));
        params.add(new BasicNameValuePair(middleNamesFieldName, editAs.getMiddleNames()));
        params.add(new BasicNameValuePair(lastNamesFieldName, editAs.getLastNames()));
        params.add(new BasicNameValuePair(prefixFieldName, editAs.getPrefix()));
        params.add(new BasicNameValuePair(suffixFieldName, editAs.getSuffix()));
        params.add(new BasicNameValuePair(preferredPubNameFieldName, editAs.getPreferredPubName()));
        params.add(new BasicNameValuePair(emailFieldName, editAs.getEmailAddress()));
        params.add(new BasicNameValuePair(websiteFieldName, editAs.getWebsite()));
        params.add(new BasicNameValuePair(phoneNumberFieldName, editAs.getPhoneNumber()));
        params.add(new BasicNameValuePair(cityFieldName, editAs.getCity()));
        params.add(new BasicNameValuePair(stateFieldName, editAs.getState()));
        params.add(new BasicNameValuePair(jobTitleFieldName, editAs.getJobTitle()));
        params.add(new BasicNameValuePair(departmentFieldName, editAs.getDepartment()));
        params.add(new BasicNameValuePair(bioFieldName, editAs.getBio()));
        params.add(new BasicNameValuePair(instCompanyFieldName, editAs.getInstCompany()));
        params.add(new BasicNameValuePair(instCompanyWebsiteFieldName, editAs.getInstCompanyWebsite()));
        params.add(new BasicNameValuePair(passwordFieldName, editAs.getPassword()));
        params.add(new BasicNameValuePair(confirmedPasswordFieldName, confirmedPassword));
        params.add(new BasicNameValuePair(oldPasswordFieldName, oldPassword));
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        editProfileFormPost.setEntity(entity);
        return editProfileFormPost;
    }

    public String getMiddleNamesFieldName() {
        return middleNamesFieldName;
    }

    public void setMiddleNamesFieldName(String middleNamesFieldName) {
        this.middleNamesFieldName = middleNamesFieldName;
    }

    public String getPrefixFieldName() {
        return prefixFieldName;
    }

    public void setPrefixFieldName(String prefixFieldName) {
        this.prefixFieldName = prefixFieldName;
    }

    public String getSuffixFieldName() {
        return suffixFieldName;
    }

    public void setSuffixFieldName(String suffixFieldName) {
        this.suffixFieldName = suffixFieldName;
    }

    public String getPreferredPubNameFieldName() {
        return preferredPubNameFieldName;
    }

    public void setPreferredPubNameFieldName(String preferredPubNameFieldName) {
        this.preferredPubNameFieldName = preferredPubNameFieldName;
    }

    public String getWebsiteFieldName() {
        return websiteFieldName;
    }

    public void setWebsiteFieldName(String websiteFieldName) {
        this.websiteFieldName = websiteFieldName;
    }

    public String getCityFieldName() {
        return cityFieldName;
    }

    public void setCityFieldName(String cityFieldName) {
        this.cityFieldName = cityFieldName;
    }

    public String getStateFieldName() {
        return stateFieldName;
    }

    public void setStateFieldName(String stateFieldName) {
        this.stateFieldName = stateFieldName;
    }

    public String getDepartmentFieldName() {
        return departmentFieldName;
    }

    public void setDepartmentFieldName(String departmentFieldName) {
        this.departmentFieldName = departmentFieldName;
    }

    public String getJobTitleFieldName() {
        return jobTitleFieldName;
    }

    public void setJobTitleFieldName(String jobTitleFieldName) {
        this.jobTitleFieldName = jobTitleFieldName;
    }

    public String getInstCompanyFieldName() {
        return instCompanyFieldName;
    }

    public void setInstCompanyFieldName(String instCompanyFieldName) {
        this.instCompanyFieldName = instCompanyFieldName;
    }
    
    public String getInstCompanyWebsiteFieldName() {
        return instCompanyWebsiteFieldName;
    }
    
    public void setInstCompanyWebsiteFieldName(String instCompanyWebsiteFieldName) {
        this.instCompanyWebsiteFieldName = instCompanyWebsiteFieldName;
    }

    public String getBioFieldName() {
        return bioFieldName;
    }

    public void setBioFieldName(String bioFieldName) {
        this.bioFieldName = bioFieldName;
    }
}