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

import java.util.Comparator;
import java.util.List;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;

/**
 * Responsible for CRUD operations on an underlying persistence store for {@link Person} objects.
 */
public interface PersonDAO {

    /**
     * Obtain the {@code Person} identified by the supplied email address.  The behavior of this method is
     * undefined when the {@code Person} is not found, but clients of this interface should be prepared to
     * handle {@code null} responses.
     *
     * @param emailAddress the email address identifying the {@code Person}
     * @return the {@code Person}, if it exists.
     */
	public Person selectPersonByEmailAddress(String emailAddress);

    /**
     * Obtain the {@code Person} identified by the supplied business id.  The behavior of this method is
     * undefined when the {@code Person} is not found, but clients of this interface should be prepared to
     * handle {@code null} responses.
     *
     * @param id the unique identifier that identifies the {@code Person}
     * @return the {@code Person}, if it exists.
     */
	public Person selectPersonById(String id);

    /**
     * Obtain {@code Person}s with the specified {@link RegistrationStatus}.  The returned {@code List} will only contain
     * {@code Person}s with the specified status.   If a {@code comparator} is supplied, implementations should use it
     * to sort the returned {@code List}.  Implementations should be prepared to handle {@code null comparator}s.
     *
     * @param status the registration status
     * @param comparator used to sort the returned list
     * @return the sorted List of {@code Person}s with {@code status}
     */
    public List<Person> selectPerson(RegistrationStatus status, Comparator<Person> comparator);

    /**
     * Obtain all the {@code Person}s known to the underlying persistence store.  Despite the fact the
     * return is a {@code List}, there is no guaranteed ordering of the contained objects.
     *
     * @return a {@code List} of all {@code Person}s, in no guaranteed order.
     */
    public List<Person> selectPerson();

    /**
     * Add a {@code Person} to the underlying persistence store.  Behavior is undefined if the {@code Person} already
     * exists.  The supplied {@code Person} must have a not-null and non-empty id, which will be used as
     * a unique identifier. It also must have a unique email_address field. Adding a person with an email address that
     * is already in the system will cause insertion failure.
     *
     * @param person the {@code Person} to add
     */
    public void insertPerson(Person person);

    /**
     * Deletes the {@code Person} or {@code Person}s from the underlying persistence store.  The
     * behavior of this method is undefined if the user does not exist.
     *
     * @param id the id identifying the user to delete
     */
    public void deletePersonById(String id);

    /**
     * Deletes the {@code Person} or {@code Person}s from the underlying persistence store.  The
     * behavior of this method is undefined if the user does not exist.
     *
     * @param email the email identifying the user to delete
     */
    public void deletePersonByEmail(String email);

    /**
     * Update the persistence store for the supplied {@code Person}.  The behavior of this method is undefined if the
     * user does not exist.
     *
     * @param person the {@code Person} to update
     */
    public void updatePerson(Person person);

}
