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

package org.dataconservancy.ui.services;

import java.util.Comparator;
import java.util.List;

import org.dataconservancy.ui.exceptions.PersonUpdateException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;

/**
 * Provides CRUD services for {@link Person} objects while enforcing business
 * policy. Implementations are expected to consult
 * {@link PersonBizPolicyConsultant} when making policy decisions.
 * <p>
 * <strong>Thread Safety:</strong> <br/>
 * Implementations are encouraged to be thread-safe, and are expected to
 * document concurrency considerations.
 * </p>
 * <p>
 * <strong>Identifier Resolution:</strong> <br/>
 * The resolution of identifiers supplied to this interface are
 * implementation-dependant. This means that {@link #get(String) retrieving} or
 * updating a {@code Person} may fail if the implementation doesn't understand
 * how to dereference the supplied identifier. However, the implementation that
 * created a Person must be able to retrieve the same Person.
 * </p>
 * 
 * @see PersonBizPolicyConsultant
 */
public interface UserService {

    /**
     * Obtain the {@code Person} identified by {@code id}.
     * 
     * @param id
     *        the identifier
     * @return the {@code Person} or {@code null} if the {@code Person} cannot
     *         be found, if {@code id} does not identify a {@code Person}, or if
     *         {@code id} cannot be resolved
     */
    public Person get(String id);

    /**
     * Create the supplied {@code Person} by persisting the object. Once the
     * {@code person} is created, it can be retrieved by the
     * {@link #get(String)} method, providing the implementation that created
     * the {@code Person} is the same implementation that is retrieving the
     * {@code Person}.
     * <p/>
     * Implementations may modify the {@code person} prior to persisting it.
     * Example modifications include, but are not limited to, initializing field
     * values, normalizing field values, or insuring field values align with
     * policy.
     * 
     * @param person
     *        the {@code Person} to create.
     * @return the created {@code Person}, which may differ from the supplied
     *         {@code person}
     * @throws PersonUpdateException
     *         if there is an error creating the {@code Person}, including the
     *         inability to resolve the identifier
     */
    public Person create(Person person);

    /**
     * Update the identified {@code Person} by setting their registration
     * status.
     * 
     * @param personId
     *        the identifier
     * @param status
     *        the registration status
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateRegistrationStatus(String personId,
                                           RegistrationStatus status);

    /**
     * Update the identified {@code Person} by setting their roles.
     * 
     * @param personId
     *        the identifier
     * @param roles
     *        the roles to assign to the {@code Person}
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateRoles(String personId, List<Role> roles);

    /**
     * Update the identified {@code Person} by setting their email address.
     * 
     * @param personId
     *        the identifier
     * @param emailAddress
     *        the email address
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateEmailAddress(String personId, String emailAddress);

    /**
     * Update the identified {@code Person} by setting their password.
     * 
     * @param personId
     *        the identifier
     * @param password
     *        the password
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updatePassword(String personId, String password);

    /**
     * Update the identified {@code Person} by setting their first name.
     * 
     * @param personId
     *        the identifier
     * @param firstNames
     *        the first name
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateFirstNames(String personId, String firstNames);

    /**
     * Update the identified {@code Person} by setting their last name.
     * 
     * @param personId
     *        the identifier
     * @param lastNames
     *        the last name
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateLastNames(String personId, String lastNames);

    /**
     * Update the identified {@code Person} by setting their phone number.
     * 
     * @param personId
     *        the identifier
     * @param phoneNumber
     *        the phone number
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updatePhoneNumber(String personId, String phoneNumber);

    /**
     * Update the identified {@code Person} by setting their job title.
     * 
     * @param personId
     *        the identifier
     * @param jobTitle
     *        the job title
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateJobTitle(String personId, String jobTitle);

    /**
     * Update the identified {@code Person} by setting their department.
     * 
     * @param personId
     *        the identifier
     * @param department
     *        the department
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateDepartment(String personId, String department);

    /**
     * Update the identified {@code Person} by setting their city.
     * 
     * @param personId
     *        the identifier
     * @param city
     *        the city
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateCity(String personId, String city);

    /**
     * Update the identified {@code Person} by setting their state.
     * 
     * @param personId
     *        the identifier
     * @param state
     *        the state
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateState(String personId, String state);

    /**
     * Update the identified {@code Person} by setting their
     * institution/company.
     * 
     * @param personId
     *        the identifier
     * @param instCompany
     *        the institution/company
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateInstCompany(String personId, String instCompany);

    /**
     * Update the identified {@code Person} by setting their middle name.
     * 
     * @param personId
     *        the identifier
     * @param middleNames
     *        space separated string of middle name(s)
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateMiddleNames(String personId, String middleNames);

    /**
     * Update the identified {@code Person} by setting their prefix.
     * 
     * @param personId
     *        the identifier
     * @param prefix
     *        person's prefix
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updatePrefix(String personId, String prefix);

    /**
     * Update the identified {@code Person} by setting their suffix.
     * 
     * @param personId
     *        the identifier
     * @param suffix
     *        person's suffix
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateSuffix(String personId, String suffix);

    /**
     * Update the identified {@code Person} by setting their preferred published
     * name.
     * 
     * @param personId
     *        the identifier
     * @param preferredPubName
     *        person's preferred published name.
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updatePreferredPubName(String personId,
                                         String preferredPubName);

    /**
     * Update the identified {@code Person} by setting their bio.
     * 
     * @param personId
     *        the identifier
     * @param bio
     *        the person's bio
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateBio(String personId, String bio);

    /**
     * Update the identified {@code Person} by setting their website.
     * 
     * @param personId
     *        the identifier
     * @param website
     *        the person's website
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *         if there is an error updating the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to update a
     *         read-only {@code Person}
     */
    public Person updateWebsite(String personId, String website);

    /**
     * Return true if this implementation is unable to update {@code Person}
     * objects. If a {@code personId} is read only, update methods will be
     * expected to fail with a {@code PersonUpdateException}. This may be true,
     * for example, when an implementation has read permissions for a
     * centralized directory, but doesn't have access to update entries in the
     * directory.
     * 
     * @param personId
     *        the identifier
     * @return true if the identified {@code Person} cannot be updated,
     *         {@code false} otherwise
     */
    public boolean isReadOnly(String personId);

    /**
     * Obtain persons that have a registration status equal to {@code status}.
     * If a {@code comparator} is supplied, it is used to sort the returned
     * List.
     * 
     * @param status
     *        the registration status
     * @param comparator
     *        used to sort the returned list, may be {@code null}
     * @return a {@code List} of {@code Person} objects, may be empty but never
     *         {@code null}
     */
    public List<Person> find(RegistrationStatus status,
                             Comparator<Person> comparator);

    /**
     * Deletes the identified {@code Person}. Once a {@code Person} has been
     * deleted, it cannot be obtained by {@link #get(String)}, nor can it be
     * updated by any methods on this interface; the supplied {@code id} (and
     * any alternate identifiers that may reference the same {@code Person})
     * must not be re-used.
     * 
     * @param id
     *        the identifier of the {@code Person} to delete
     * @throws PersonUpdateException
     *         if there is an error deleting the {@code Person}, including the
     *         inability to resolve the identifier, or attempting to delete a
     *         read-only {@code Person}
     */
    public void deletePerson(String id);

    /**
     * Set the policy consultant used by this interface when enforcing
     * Person-related business policy decisions.
     * 
     * @param policyConsultant
     *        the policy consultant
     */
    public void setPolicyConsultant(PersonBizPolicyConsultant policyConsultant);

    /**
     * Obtain the policy consultant used by this interface when enforcing
     * Person-related business policy decisions.
     * 
     * @return the policy consultant
     */
    public PersonBizPolicyConsultant getPolicyConsultant();
    
    /**
     * Update the identified {@code Person} by setting their institution/company website.
     * 
     * @param personId
     *            the identifier
     * @param instCompanyWebsite
     *            the institution/company website
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *             if there is an error updating the {@code Person}, including the inability to resolve the identifier,
     *             or attempting to update a read-only {@code Person}
     */
    public Person updateInstCompanyWebsite(String personId, String instCompanyWebsite);
    
    /**
     * Update the identified {@code Person} by setting their institution/company website.
     * 
     * @param personId
     *            the identifier
     * @param externalStorageLinked
     *            the flag for whether external storage is linked or not.
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *             if there is an error updating the {@code Person}, including the inability to resolve the identifier,
     *             or attempting to update a read-only {@code Person}
     */
    public Person updateExternalStorageLinked(String personId, boolean externalStorageLinked);
    
    /**
     * Update the identified {@code Person} by setting their institution/company website.
     * 
     * @param personId
     *            the identifier
     * @param dropboxAppKey
     *            the unique dropbox app key
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *             if there is an error updating the {@code Person}, including the inability to resolve the identifier,
     *             or attempting to update a read-only {@code Person}
     */
    public Person updateDropboxAppKey(String personId, String dropboxAppKey);
    
    /**
     * Update the identified {@code Person} by setting their institution/company website.
     * 
     * @param personId
     *            the identifier
     * @param dropboxAppSecret
     *            the unique dropbox app secret
     * @return the updated {@code Person}, never {@code null}
     * @throws PersonUpdateException
     *             if there is an error updating the {@code Person}, including the inability to resolve the identifier,
     *             or attempting to update a read-only {@code Person}
     */
    public Person updateDropboxAppSecret(String personId, String dropboxAppSecret);
}
