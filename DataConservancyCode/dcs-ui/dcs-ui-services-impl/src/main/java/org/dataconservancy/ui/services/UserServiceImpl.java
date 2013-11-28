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

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.dao.PersonDAO;
import org.dataconservancy.ui.exceptions.PersonUpdateException;
import org.dataconservancy.ui.exceptions.RuntimeBizPolicyException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.BusinessIdState;
import org.dataconservancy.ui.policy.support.IdBizPolicyConsultant;
import org.dataconservancy.ui.policy.support.IdPolicy;
import org.dataconservancy.ui.policy.support.IdPolicyAction;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the {@code UserService}. Consults the
 * {@link PersonBizPolicyConsultant} when making and enforcing policy decisions.
 * Persistence is the responsibility of the {@link PersonDAO}.
 * <p/>
 * A single instance of this class may be safely used by concurrent threads.
 * Most methods will synchronize on the {@code Person} identifier, ensuring that
 * all threads will have a consistent view of the {@code Person} object.
 */
public class UserServiceImpl implements UserService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private PersonDAO personDAO;

    private PersonBizPolicyConsultant policyConsultant;

    private IdBizPolicyConsultant idPolicyConsultant;

    private IdService idService;

    // Currently not used, added suppress warning.
    @SuppressWarnings("unused")
    private static String urlPrefix;

    private enum KeyType {
        ID, EMAIL_ADDRESS
    }

    public UserServiceImpl(PersonDAO personDAO,
                           PersonBizPolicyConsultant personPolicyConsultant,
                           IdBizPolicyConsultant idPolicyConsultant,
                           IdService idService) {
        this.personDAO = personDAO;
        this.policyConsultant = personPolicyConsultant;
        this.idPolicyConsultant = idPolicyConsultant;
        this.idService = idService;
        log.trace("Instantiated {} with {}, {}, {}", new Object[] {this,
                personDAO, personPolicyConsultant, idPolicyConsultant});
        /*
         * //TODO: actually call the IdPrefixBootstrap urlPrefix = "http:";
         */
    }

    @Override
    public Person get(String key) {
        log.trace("Obtaining person for id {}", key);

        if (key == null) {
            return null;
        }

        synchronized (key.intern()) {
            if (getKeyType(key).equals(KeyType.EMAIL_ADDRESS)) {
                return personDAO.selectPersonByEmailAddress(key);
            } else {
                return personDAO.selectPersonById(key);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation consults the following when creating new
     * {@code Person} objects:
     * <ul>
     * <li>{@link IdBizPolicyConsultant#getBusinessObjectCreationIdPolicy()}
     * when validating the {@code person}s {@link Person#getId() identifier}</li>
     * <li>{@link PersonBizPolicyConsultant#enforceRegistrationStatusOnCreate()}
     * when validating the {@code person}s {@link RegistrationStatus status}</li>
     * <li>{@link PersonBizPolicyConsultant#allowedRegistrationStatusOnCreate()}
     * when validating the {@code person}s {@link RegistrationStatus status}</li>
     * <li>{@link PersonBizPolicyConsultant#getDefaultRegistrationStatus()} when
     * {@code person} has a {@code null} {@code RegistrationStatus}</li>
     * <li>
     * {@link PersonBizPolicyConsultant#getRolesForRegistrationStatus(RegistrationStatus)}
     * when assigning {@link Role roles} to the {@code Person}</li>
     * </ul>
     * </p>
     * 
     * @param person
     *        the {@code Person} to create.
     * @return {@inheritDoc}
     */
    @Override
    public Person create(Person person) {

        // Determine the state of the business identifier
        final BusinessIdState actualIdState = determineBusinessIdState(person);

        // Obtain the business identifier for the Person.  If the Person doesn't have one,
        // this method creates a candidate (placeholder) identifier.  That way we can reliably
        // lock access to this person even when they don't yet have an ID.
        final String personId = getPersonId(person, actualIdState);

        synchronized (personId.intern()) {

            // We pass in the personId which will be used only if the Person's primary ID needs to be REPLACED or
            // SUBSTITUTEd.
            verifyPersonId(person, actualIdState, personId);
            enforceRegistrationStatus(person);
            setRolesAccordingToRegistrationStatus(person);

            // Validate the person after enforcing policy.
            validateOnCreate(person);

            // Persist the Person
            log.trace("Creating person {}", person);
            personDAO.insertPerson(person);

            // In case the DB updates fields, return a new Person object.
            return personDAO.selectPersonById(person.getId());
        }
    }

    @Override
    public void deletePerson(String id) {
        log.trace("Deleting person {}", id);

        synchronized (id.intern()) {
            Person personToDelete = personDAO.selectPersonById(id);

            if (personToDelete != null) {
                personDAO.deletePersonById(id);
            } else {
                personDAO.deletePersonByEmail(id);
            }
        }
    }

    @Override
    public List<Person> find(RegistrationStatus status,
                             Comparator<Person> comparator) {
        log.trace("Finding persons with registration status {}", status);
        return personDAO.selectPerson(status, comparator);
    }

    @Override
    public boolean isReadOnly(String personId) {
        // Default method body
        return false;
    }

    @Override
    public Person updateRegistrationStatus(String personId,
                                           RegistrationStatus status) {
        synchronized (personId.intern()) {
            final Person p = getPerson(personId);
            p.setRegistrationStatus(status);
            p.setRoles(policyConsultant.getRolesForRegistrationStatus(status));
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updateEmailAddress(String personId, String emailAddress) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setEmailAddress(emailAddress);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updatePassword(String personId, String password) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setPassword(password);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updateFirstNames(String personId, String firstNames) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setFirstNames(firstNames);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updateLastNames(String personId, String lastNames) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setLastNames(lastNames);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updatePhoneNumber(String personId, String phoneNumber) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setPhoneNumber(phoneNumber);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updateRoles(String personId, List<Role> roles) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setRoles(roles);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public synchronized PersonBizPolicyConsultant getPolicyConsultant() {
        return policyConsultant;
    }

    @Override
    public synchronized void setPolicyConsultant(PersonBizPolicyConsultant policyConsultant) {
        this.policyConsultant = policyConsultant;
    }

    private KeyType getKeyType(String key) {
        if (!key.contains("@")) {
            return KeyType.ID;
        } else {
            return KeyType.EMAIL_ADDRESS;
        }
    }

    /**
     * This method insures that the {@code Person} business object has a proper
     * business identifier according to the {@code IdBizPolicyConsultant}.
     * <ol>
     * <li>The policy is looked up based on state of the business identifier
     * (e.g. "what should I do when the Person object [has|does not have] an
     * identifier").</li>
     * <li>The method then enforces the policy.</li>
     * <li>If the policy is to {@code REPLACE} or {@code SUBSTITUTE} the
     * identifier, the supplied {@code candidateId} should be used as the new
     * primary business identifier</li>
     * </ol>
     * 
     * @param person
     *        the person object
     * @param idState
     *        the current state of the {@code person}'s identifier
     * @param candidateId
     *        the primary id for a Person when REPLACEing or SUBSTITUTEing an
     *        identifier
     * @throws PersonUpdateException
     *         if the policy is to reject the identifier (or lack thereof), or
     *         if the policy is equal to {@link IdPolicyAction#SUBSTITUTE},
     *         because it isn't supported.
     */
    private void verifyPersonId(Person person,
                                BusinessIdState idState,
                                String candidateId) {

        // Since we're creating an object, obtain the IdPolicy for object creation.
        final IdPolicy policy =
                idPolicyConsultant.getBusinessObjectCreationIdPolicy();

        // Query the policy for the action to take based on the state of the business object's identifier
        final IdPolicyAction policyAction =
                policy.getIdPolicyActionForState(idState);

        // Enforce the policy
        switch (policyAction) {
            case ACCEPT:
                log.trace("Accepting id '" + person.getId() + "'");
                break;

            case REJECT:
                log.trace("Rejecting id '" + person.getId() + "'");
                throw new PersonUpdateException("Rejecting id '"
                        + person.getId() + "'");

            case REPLACE:
                log.trace("Replacing id '" + person.getId() + "' with '"
                        + candidateId + "'");
                person.setId(candidateId);
                break;

            case SUBSTITUTE:
                throw new PersonUpdateException("ID Policy Action SUBSTITUTE is not currently supported.");

            default:
                throw new PersonUpdateException("Unknown ID Policy Action "
                        + policyAction);
        }

    }

    /**
     * Determines the business ID state of the {@code person}:
     * "Do I have a business ID or not?". This implementation considers empty
     * strings, zero-length strings, and null references to equate to
     * {@link BusinessIdState#DOES_NOT_EXIST}.
     * 
     * @param person
     *        the Person whose business id state needs to be determined.
     * @return the business id state
     */
    private BusinessIdState determineBusinessIdState(Person person) {
        // Determine if the business object has an identifier, and set the appropriate BusinessIdState
        boolean isIdPresent =
                person.getId() != null && person.getId().trim().length() > 0;
        BusinessIdState actualIdState;
        if (isIdPresent) {
            actualIdState = BusinessIdState.EXISTS;
        } else {
            actualIdState = BusinessIdState.DOES_NOT_EXIST;
        }
        return actualIdState;
    }

    /**
     * This method insures that the {@code Person} business object has the
     * proper roles according to the {@code person}'s {@code RegistrationStatus}
     * by consulting the {@code PersonBizPolicyConsultant}.
     * 
     * @param person
     *        the person object
     */
    private void setRolesAccordingToRegistrationStatus(Person person) {
        // Set the Roles the Person should have, based on their RegistrationStatus
        person.setRoles(policyConsultant.getRolesForRegistrationStatus(person
                .getRegistrationStatus()));
    }

    /**
     * This method insures that the {@code Person} business object has a proper
     * {@code RegistrationStatus} by consulting the
     * {@code PersonBizPolicyConsultant}. If
     * {@link PersonBizPolicyConsultant#enforceRegistrationStatusOnCreate()} is
     * {@code true}, this method will verify that the
     * {@link Person#getRegistrationStatus() Person's registration status} is an
     * {@link PersonBizPolicyConsultant#allowedRegistrationStatusOnCreate()
     * allowed status}.
     * <p/>
     * If the Person's registration status is {@code null}, then the
     * {@link PersonBizPolicyConsultant#getDefaultRegistrationStatus() default
     * registration status} is assigned.
     * 
     * @param person
     *        the person object
     * @throws RuntimeBizPolicyException
     *         when the Person does not have an allowed Registration Status
     */
    private void enforceRegistrationStatus(Person person) {
        // Set the default RegistrationStatus if the Person doesn't have one.
        if (person.getRegistrationStatus() == null) {
            person.setRegistrationStatus(policyConsultant
                    .getDefaultRegistrationStatus());
        }

        // Insure that the Person being created has an allowed RegistrationStatus, according to the
        // PersonBizPolicyConsultant
        if (policyConsultant.enforceRegistrationStatusOnCreate()
                && !policyConsultant.allowedRegistrationStatusOnCreate()
                        .isEmpty()) {
            if (!policyConsultant.allowedRegistrationStatusOnCreate()
                    .contains(person.getRegistrationStatus())) {
                RuntimeBizPolicyException e =
                        new RuntimeBizPolicyException("Person "
                                + person.getId()
                                + " ("
                                + person.getEmailAddress()
                                + ") did not have an allowed RegistrationStatus!");
                throw new PersonUpdateException(e.getMessage(), e);
            }
        }
    }

    /**
     * Attempt to retrieve the Person by id, and if that's not possible, throw
     * an exception. If the retrieved Person is {@code null}, or if an Exception
     * is caught, throw a {@code PersonUpdateException}.
     * 
     * @param id
     *        the identifier of the Person
     * @return the Person, never {@code null}
     * @throws PersonUpdateException
     *         if the Person cannot be retrieved for any reason
     */
    private Person getPerson(String id) {
        Person p = null;

        try {
            p = get(id);
        } catch (Exception e) {
            throw new PersonUpdateException(e.getMessage(), e);
        }

        if (p == null) {
            throw new PersonUpdateException("Person '" + id + "' not found");
        }

        return p;
    }

    /**
     * Obtain an identifier for the {@code person} based on the value of the
     * {@code idState}. If the Person's ID state is
     * {@link BusinessIdState#DOES_NOT_EXIST} then this method will mint a new
     * ID for the Person. Otherwise, it will return the existing ID on the
     * {@code person}.
     * 
     * @param person
     *        the person instance, which can be used to generate a new ID or
     *        obtain the existing id
     * @param idState
     *        the state of the Person's
     *        {@link org.dataconservancy.ui.model.Person#getId() business id}.
     * @return the identifier for the Person.
     */
    private String getPersonId(Person person, BusinessIdState idState) {
        if (idState == BusinessIdState.EXISTS) {
            return person.getId();
        }

        String candidateId =
                idService.create(Types.PERSON.name()).getUrl().toString();
        return candidateId;
    }

    /**
     * Validate a person from a create request. Valid person for creation is
     * required to have the following fields non-null or non-empty: firstname,
     * lastname, email address, phone, password.
     * 
     * @param person
     *        the person to validate
     * @throws PersonUpdateException
     *         if validation fails
     */
    protected void validateOnCreate(Person person) throws PersonUpdateException {
        final String personId = person.getId();
        final String personEmail = person.getEmailAddress();
        final String msgStart =
                "Validation failed while creating person (" + personId + ", "
                        + personEmail + "): ";
        if (person.getFirstNames() == null || person.getFirstNames().isEmpty()) {
            throw new PersonUpdateException(new RuntimeBizPolicyException(msgStart
                    + "first name must not be null or empty"));
        } else if (person.getLastNames() == null
                || person.getLastNames().isEmpty()) {
            throw new PersonUpdateException(new RuntimeBizPolicyException(msgStart
                    + "last name must not be null or empty"));
        } else if (person.getPassword() == null
                || person.getPassword().isEmpty()) {
            throw new PersonUpdateException(new RuntimeBizPolicyException(msgStart
                    + "password must not be null or empty"));
        } else if (person.getEmailAddress() == null
                || person.getEmailAddress().isEmpty()) {
            throw new PersonUpdateException(new RuntimeBizPolicyException(msgStart
                    + " email address must not be null or empty"));
        } else if (person.getPhoneNumber() == null
                || person.getPhoneNumber().isEmpty()) {
            throw new PersonUpdateException(new RuntimeBizPolicyException(msgStart
                    + " phone number must not be null or empty"));
        }
    }

    @Override
    public Person updateJobTitle(String personId, String jobTitle) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setJobTitle(jobTitle);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updateDepartment(String personId, String department) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setDepartment(department);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updateCity(String personId, String city) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setCity(city);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updateState(String personId, String state) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setState(state);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updateInstCompany(String personId, String instCompany) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setInstCompany(instCompany);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updateMiddleNames(String personId, String middleNames) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setMiddleNames(middleNames);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updatePrefix(String personId, String prefix) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setPrefix(prefix);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updateSuffix(String personId, String suffix) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setSuffix(suffix);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updatePreferredPubName(String personId,
                                         String preferredPubName) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setPreferredPubName(preferredPubName);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updateBio(String personId, String bio) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setBio(bio);
            personDAO.updatePerson(p);
            return p;
        }
    }

    @Override
    public Person updateWebsite(String personId, String website) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setWebsite(website);
            personDAO.updatePerson(p);
            return p;
        }
    }
    
    @Override
    public Person updateInstCompanyWebsite(String personId, String instCompanyWebsite) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setInstCompanyWebsite(instCompanyWebsite);
            personDAO.updatePerson(p);
            return p;
        }
    }
    
    @Override
    public Person updateExternalStorageLinked(String personId, boolean externalStorageLinked) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setExternalStorageLinked(externalStorageLinked);
            personDAO.updatePerson(p);
            return p;
        }
    }
    
    @Override
    public Person updateDropboxAppKey(String personId, String dropboxAppKey) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setDropboxAppKey(dropboxAppKey);
            personDAO.updatePerson(p);
            return p;
        }
    }
    
    @Override
    public Person updateDropboxAppSecret(String personId, String dropboxAppSecret) {
        synchronized (personId.intern()) {
            Person p = getPerson(personId);
            p.setDropboxAppSecret(dropboxAppSecret);
            personDAO.updatePerson(p);
            return p;
        }
    }
}
