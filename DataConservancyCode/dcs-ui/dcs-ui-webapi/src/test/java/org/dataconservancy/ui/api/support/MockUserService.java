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

package org.dataconservancy.ui.api.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;
import org.dataconservancy.ui.services.UserService;

public class MockUserService implements UserService {

    private Map<String, Person> personMap;

    public MockUserService() {
        personMap = new HashMap<String, Person>();
    }

    @Override
    public Person get(String id) {
        return personMap.get(id);
    }

    @Override
    public Person create(Person person) {
        personMap.put(person.getId(), person);
        return person;
    }

    @Override
    public List<Person> find(RegistrationStatus status,
                             Comparator<Person> comparator) {
        List<Person> results = new ArrayList<Person>();
        for (Map.Entry<String, Person> e : personMap.entrySet()) {
            if (e.getValue().getRegistrationStatus() == status) {
                results.add(e.getValue());
            }
        }

        if (comparator != null) {
            Collections.sort(results, comparator);
        }
        return results;
    }

    @Override
    public void deletePerson(String id) {
        personMap.remove(id);
    }

    @Override
    public boolean isReadOnly(String personId) {
        // Default method body
        return false;
    }

    @Override
    public Person updateRegistrationStatus(String personId,
                                           RegistrationStatus status) {
        // Default method body
        return null;
    }

    @Override
    public Person updateEmailAddress(String personId, String emailAddress) {
        // Default method body
        return null;
    }

    @Override
    public Person updatePassword(String personId, String password) {
        // Default method body
        return null;
    }

    @Override
    public Person updateFirstNames(String personId, String firstName) {
        // Default method body
        return null;
    }

    @Override
    public Person updateLastNames(String personId, String lastName) {
        // Default method body
        return null;
    }

    @Override
    public Person updatePhoneNumber(String personId, String phoneNumber) {
        // Default method body
        return null;
    }

    @Override
    public Person updateRoles(String personId, List<Role> roles) {
        // Default method body
        return null;
    }

    @Override
    public PersonBizPolicyConsultant getPolicyConsultant() {
        // Default method body
        return null;
    }

    @Override
    public void setPolicyConsultant(PersonBizPolicyConsultant policyConsultant) {
        // Default method body

    }

    @Override
    public Person updateJobTitle(String personId, String jobTitle) {
        // Default method body
        return null;
    }

    @Override
    public Person updateDepartment(String personId, String department) {
        // Default method body
        return null;
    }

    @Override
    public Person updateCity(String personId, String city) {
        // Default method body
        return null;
    }

    @Override
    public Person updateState(String personId, String state) {
        // Default method body
        return null;
    }

    @Override
    public Person updateInstCompany(String personId, String instCompany) {
        // Default method body
        return null;
    }

    @Override
    public Person updateMiddleNames(String personId, String middleNames) {
        // Default method body
        return null;
    }

    @Override
    public Person updatePrefix(String personId, String prefix) {
        // Default method body
        return null;
    }

    @Override
    public Person updateSuffix(String personId, String suffix) {
        // Default method body
        return null;
    }

    @Override
    public Person updatePreferredPubName(String personId,
                                         String preferredPubName) {
        // Default method body
        return null;
    }

    @Override
    public Person updateBio(String personId, String bio) {
        // Default method body
        return null;
    }

    @Override
    public Person updateWebsite(String personId, String website) {
        // Default method body
        return null;
    }
    
    @Override
    public Person updateInstCompanyWebsite(String personId, String instCompanyWebsite) {
        // Default method body
        return null;
    }
    
    @Override
    public Person updateExternalStorageLinked(String personId, boolean externalStorageLinked) {
        // Default method body
        return null;
    }
    
    @Override
    public Person updateDropboxAppKey(String personId, String dropboxAppKey) {
        // Default method body
        return null;
    }
    
    @Override
    public Person updateDropboxAppSecret(String personId, String dropboxAppSecret) {
        // Default method body
        return null;
    }
}