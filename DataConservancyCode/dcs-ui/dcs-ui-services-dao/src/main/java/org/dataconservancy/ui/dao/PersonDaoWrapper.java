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
 * Provides additional behaviors to {@link PersonDAO}s that don't belong on the {@code PersonDAO} interface itself;
 * for example, a {@code PersonDAO} can be made {@link #readOnly read-only}.
 *
 * <table>
 *     <tr>
 *         <th>Property Name</th>
 *         <th>Description</th>
 *         <th>Default Value</th>
 *     </tr>
 *     <tr>
 *         <td>readOnly</td>
 *         <td>Throws a {@code RuntimeException} when mutating methods are called</td>
 *         <td>{@code false}</td>
 *     </tr>
 * </table>
 */
public class PersonDaoWrapper implements PersonDAO {

    private boolean readOnly = false;

    private PersonDAO delegate;

    /**
     * Wrap the supplied DAO using default property settings.
     *
     * @param delegate the DAO to wrap
     */
    public PersonDaoWrapper(PersonDAO delegate) {
        this.delegate = delegate;
    }

    /**
     * Wrap the supplied DAO, optionally marking it as read-only.
     *
     * @param delegate the DAO to wrap
     * @param readOnly flag indicating whether the DAO should be considered read-only
     * @deprecated Use {@link PersonDaoWrapper#PersonDaoWrapper(PersonDAO) this} instead, and supply properties with
     *             setters.
     */
    public PersonDaoWrapper(PersonDAO delegate, boolean readOnly) {
        this.delegate = delegate;
        this.readOnly = readOnly;
    }

    /**
     * Returns {@code true} if the DAO should be considered read-only.  If the DAO is read-only, invoking
     * a mutating method will result in a {@code RuntimeException} being thrown.
     *
     * @return the value of the read-only flag
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * If set to {@code true}, the DAO is considered to be read-only, and invoking a mutating method will
     * result in a {@code RuntimeException} being thrown.
     *
     * @param readOnly the value of the read-only flag
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * {@inheritDoc}
     *
     * @param emailAddress {@inheritDoc}
     * @throws RuntimeException if the DAO is read-only
     */
    @Override
    public void deletePersonByEmail(String emailAddress) {
        if (readOnly) {
            throw new RuntimeException();
        }
        delegate.deletePersonByEmail(emailAddress);
    }
    
    /**
     * {@inheritDoc}
     *
     * @param id {@inheritDoc}
     * @throws RuntimeException if the DAO is read-only
     */
    @Override
    public void deletePersonById(String id) {
        if (readOnly) {
            throw new RuntimeException();
        }
        delegate.deletePersonById(id);
    }

    /**
     * {@inheritDoc}
     *
     * @param emailAddress {@inheritDoc}
     * @return {@inheritDoc}
     * @throws RuntimeException if the DAO is read-only
     */
    @Override
    public Person selectPersonByEmailAddress(String emailAddress) {
        return setReadOnlyFlag(delegate.selectPersonByEmailAddress(emailAddress));
    }
    /**
     * {@inheritDoc}
     *
     * @param id {@inheritDoc}
     * @return {@inheritDoc}
     * @throws RuntimeException if the DAO is read-only
     */
    @Override
    public Person selectPersonById(String id) {
        return setReadOnlyFlag(delegate.selectPersonById(id));
    }

    /**
     * {@inheritDoc}
     *
     * @param status {@inheritDoc}
     * @param comparator {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public List<Person> selectPerson(RegistrationStatus status, Comparator<Person> comparator) {
        return setReadOnlyFlag(delegate.selectPerson(status, comparator));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public List<Person> selectPerson() {
        return setReadOnlyFlag(delegate.selectPerson());
    }

    /**
     * {@inheritDoc}
     *
     * @param person {@inheritDoc}
     * @throws RuntimeException if the DAO is read-only
     */
    @Override
    public void insertPerson(Person person) {
        if (readOnly) {
            throw new RuntimeException();
        }
        delegate.insertPerson(person);
    }

    /**
     * {@inheritDoc}
     *
     * @param person {@inheritDoc}
     * @throws RuntimeException if the DAO is read-only
     */
    @Override
    public void updatePerson(Person person) {
        if (readOnly) {
            throw new RuntimeException();
        }
        delegate.updatePerson(person);
    }

    private List<Person> setReadOnlyFlag(List<Person> persons) {
        if (persons != null) {
            for (Person p : persons) {
                setReadOnlyFlag(p);
            }
        }

        return persons;
    }

    private Person setReadOnlyFlag(Person p) {
        if (p != null) {
            p.setReadOnly(readOnly);
        }

        return p;
    }

}
