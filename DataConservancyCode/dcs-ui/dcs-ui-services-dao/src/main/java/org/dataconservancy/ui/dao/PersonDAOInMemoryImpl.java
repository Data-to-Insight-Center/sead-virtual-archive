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

package org.dataconservancy.ui.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An in-memory implementation of the PersonDAO interface. Uses an internal Map
 * which keys Person objects by email address.
 */
public class PersonDAOInMemoryImpl
        implements PersonDAO {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<String, Person> people = new HashMap<String, Person>();

    public PersonDAOInMemoryImpl(Map<String, Person> people) {
        this.people = people;
    }

    @Override
    public Person selectPersonByEmailAddress(String emailAddress) {
        for (Map.Entry<String, Person> entry : people.entrySet()) {
            if (emailAddress.equals(entry.getValue().getEmailAddress())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public Person selectPersonById(String id) {
        return people.get(id);
    }

    @Override
    public List<Person> selectPerson(RegistrationStatus status,
                                     Comparator<Person> comparator) {
        List<Person> results = new ArrayList<Person>();
        for (Map.Entry<String, Person> e : people.entrySet()) {
            if (e.getValue().getRegistrationStatus() == status) {
                results.add(e.getValue());
            }
        }

        if (comparator != null) {
            Collections.sort(results, comparator);
        }

        log.debug("Found {} people with registration status {}",
                  results.size(),
                  status);
        return results;
    }

    @Override
    public void insertPerson(Person person) {
        people.put(person.getId(), person);
    }

    @Override
    public void deletePersonByEmail(String email) {
        people.remove(email);
    }

    @Override
    public void deletePersonById(String id) {
        //We weren't able to find the person with email address try to see if ID is the id
        for (Map.Entry<String, Person> entry : people.entrySet()) {
            if (id.equals(entry.getValue().getId())) {
                people.remove(entry.getKey());
                break;
            }
        }
    }

    @Override
    public void updatePerson(Person person) {
        insertPerson(person);
    }

    @Override
    public List<Person> selectPerson() {
        ArrayList<Person> result = new ArrayList<Person>();
        result.addAll(people.values());
        return result;
    }

}
