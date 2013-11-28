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

package org.dataconservancy.ui.model;

import java.util.Comparator;
import java.util.List;

/**
 * A Person implementation which delegates all method calls to the Person
 * supplied on construction.
 */
public class PersonWrapper extends Person {

    private Person delegate;

    public PersonWrapper(Person delegate) {
        this.delegate = delegate;
    }

    @Override
    public RegistrationStatus getRegistrationStatus() {
        return delegate.getRegistrationStatus();
    }

    @Override
    public void setRegistrationStatus(RegistrationStatus registrationStatus) {
        delegate.setRegistrationStatus(registrationStatus);
    }

    @Override
    public String getEmailAddress() {
        return delegate.getEmailAddress();
    }

    @Override
    public void setEmailAddress(String emailAddress) {
        delegate.setEmailAddress(emailAddress);
    }

    @Override
    public String getPhoneNumber() {
        return delegate.getPhoneNumber();
    }

    @Override
    public void setPhoneNumber(String phoneNumber) {
        delegate.setPhoneNumber(phoneNumber);
    }

    @Override
    public String getPassword() {
        return delegate.getPassword();
    }

    @Override
    public void setPassword(String password) {
        delegate.setPassword(password);
    }

    @Override
    public String getFirstNames() {
        return delegate.getFirstNames();
    }

    @Override
    public void setFirstNames(String firstName) {
        delegate.setFirstNames(firstName);
    }

    @Override
    public String[] getFirstNamesAsArray() {
        return delegate.getFirstNamesAsArray();
    }

    @Override
    public void setFirstNamesAsArray(String[] firstNames) {
        delegate.setFirstNamesAsArray(firstNames);
    }

    @Override
    public String[] getLastNamesAsArray() {
        return delegate.getLastNamesAsArray();
    }

    @Override
    public void setLastNamesAsArray(String[] lastNames) {
        delegate.setLastNamesAsArray(lastNames);
    }

    @Override
    public String[] getMiddleNamesAsArray() {
        return delegate.getMiddleNamesAsArray();
    }

    @Override
    public void setMiddleNamesAsArray(String[] middleNames) {
        delegate.setMiddleNamesAsArray(middleNames);
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void setId(String id) {
        delegate.setId(id);
    }

    @Override
    public String getLastNames() {
        return delegate.getLastNames();
    }

    @Override
    public void setLastNames(String lastName) {
        delegate.setLastNames(lastName);
    }

    @Override
    public boolean getReadOnly() {
        return delegate.getReadOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        delegate.setReadOnly(readOnly);
    }

    @Override
    public Comparator<Person> getComparator() {
        return delegate.getComparator();
    }

    @Override
    public void setComparator(Comparator<Person> comparator) {
        delegate.setComparator(comparator);
    }

    @Override
    public int compareTo(Person person) {
        return delegate.compareTo(person);
    }

    @Override
    public List<Role> getRoles() {
        return delegate.getRoles();
    }

    @Override
    public void setRoles(List<Role> roles) {
        delegate.setRoles(roles);
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public Person addRole(Role role) {
        return delegate.addRole(role);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public String getJobTitle() {
        return delegate.getJobTitle();
    }

    @Override
    public void setJobTitle(String jobTitle) {
        delegate.setJobTitle(jobTitle);
    }

    @Override
    public String getCity() {
        return delegate.getCity();
    }

    @Override
    public void setCity(String city) {
        delegate.setCity(city);
    }

    @Override
    public String getState() {
        return delegate.getState();
    }

    @Override
    public void setState(String state) {
        delegate.setState(state);
    }

    @Override
    public String getDepartment() {
        return delegate.getDepartment();
    }

    @Override
    public void setDepartment(String department) {
        delegate.setDepartment(department);
    }

    @Override
    public String getInstCompany() {
        return delegate.getInstCompany();
    }

    @Override
    public void setInstCompany(String instCompany) {
        delegate.setInstCompany(instCompany);
    }

    @Override
    public String getPrefix() {
        return delegate.getPrefix();
    }

    @Override
    public void setPrefix(String prefix) {
        delegate.setPrefix(prefix);
    }

    @Override
    public String getSuffix() {
        return delegate.getSuffix();
    }

    @Override
    public void setSuffix(String suffix) {
        delegate.setSuffix(suffix);
    }

    @Override
    public String getPreferredPubName() {
        return delegate.getPreferredPubName();
    }

    @Override
    public void setPreferredPubName(String preferredPubName) {
        delegate.setPreferredPubName(preferredPubName);
    }

    @Override
    public String getBio() {
        return delegate.getBio();
    }

    @Override
    public void setBio(String bio) {
        delegate.setBio(bio);
    }

    @Override
    public String getWebsite() {
        return delegate.getWebsite();
    }

    @Override
    public void setWebsite(String website) {
        delegate.setWebsite(website);
    }

    @Override
    public String getMiddleNames() {
        return delegate.getMiddleNames();
    }

    @Override
    public void setMiddleNames(String middleNames) {
        delegate.setMiddleNames(middleNames);
    }
    
    @Override
    public void setInstCompanyWebsite(String instCompanyWebsite) {
        delegate.setInstCompanyWebsite(instCompanyWebsite);
    }
    
    @Override
    public String getInstCompanyWebsite() {
        return delegate.getInstCompanyWebsite();
    }
    
    @Override
    public void setExternalStorageLinked(boolean externalStorageLinked) {
        delegate.setExternalStorageLinked(externalStorageLinked);
    }
    
    @Override
    public boolean isExternalStorageLinked() {
        return delegate.isExternalStorageLinked();
    }
    
    @Override
    public void setDropboxAppKey(String dropboxAppKey) {
        delegate.setDropboxAppKey(dropboxAppKey);
    }
    
    @Override
    public String getDropboxAppKey() {
        return delegate.getDropboxAppKey();
    }
    
    @Override
    public void setDropboxAppSecret(String dropboxAppSecret) {
        delegate.setDropboxAppSecret(dropboxAppSecret);
    }
    
    @Override
    public String getDropboxAppSecret() {
        return delegate.getDropboxAppSecret();
    }
}
