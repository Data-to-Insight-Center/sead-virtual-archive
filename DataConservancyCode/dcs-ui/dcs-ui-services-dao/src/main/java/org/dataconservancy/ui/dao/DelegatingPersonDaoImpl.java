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
import java.util.List;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;

/**
 * Implementation of {@link PersonDAO} that delegates method calls to a {@code List} of underlying
 * {@link PersonDaoWrapper wrapped} {@code PersonDAO}s.  The wrapped {@code PersonDAO}s have a
 * {@code readOnly} flag, which this implementation honors: methods that would mutate the underlying
 * persistence layer are not invoked.
 */
public class DelegatingPersonDaoImpl implements PersonDAO {

    /**
     * Ordered {@code List} of delegate {@code PersonDAO}s, wrapped with additional
     * functionality (provided by {@link PersonDaoWrapper}).
     */
    final private List<PersonDaoWrapper> delegates;

    /**
     * Initialize this DAO with an ordered {@link List} of delegate {@link PersonDAO}s.  Delegate
     * DAOs will be invoked in the order that they appear in this {@code List}: DAOs at the head
     * of the list will be tried before DAOs at the tail of the list.
     *
     * @param delegates the ordered {@link List} of wrapped {@code PersonDAO}s, must not be null.
     * @throws IllegalArgumentException if the delegate list is null.
     */
    public DelegatingPersonDaoImpl(List<PersonDaoWrapper> delegates) {

        if (delegates == null) {
            throw new IllegalArgumentException("Delegate DAOs must not be null!");
        }

        this.delegates = delegates;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation does not invoke delegates marked as read-only.  All delegates that have
     * a person for {@code emailAddress} will have the user deleted.  A future design may provide
     * Strategies that influence this behavior.
     *
     * @param emailAddress {@inheritDoc}
     */
    @Override
    public void deletePersonByEmail(String emailAddress) {
        for (PersonDaoWrapper dao : delegates) {
            if (dao.isReadOnly()) {
                continue;
            }

            if (dao.selectPersonByEmailAddress(emailAddress) != null) {
                dao.deletePersonByEmail(emailAddress);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     * <p/>
     * This implementation does not invoke delegates marked as read-only.  All delegates that have
     * a person for {@code id} will have the user deleted.  A future design may provide
     * Strategies that influence this behavior.
     *
     * @param id {@inheritDoc}
     */
    @Override
    public void deletePersonById(String id) {
        for (PersonDaoWrapper dao : delegates) {
            if (dao.isReadOnly()) {
                continue;
            }

            if (dao.selectPersonById(id) != null) {
                dao.deletePersonById(id);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation selects the {@code Person} from the first delegate that contains it.  A future design may
     * provide Strategies that influence this behavior.
     *
     * @param emailAddress {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Person selectPersonByEmailAddress(String emailAddress) {
        Person p = null;
        for (PersonDAO dao : delegates) {
            if ((p = dao.selectPersonByEmailAddress(emailAddress)) != null) {
                return p;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation selects the {@code Person} from the first delegate that contains it.  A future design may
     * provide Strategies that influence this behavior.
     *
     * @param id {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Person selectPersonById(String id) {
        Person p = null;
        for (PersonDAO dao : delegates) {
            if ((p = dao.selectPersonById(id)) != null) {
                return p;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <strong>TODO: Review the behavior of this method.</strong>
     * This implementation does not invoke delegates marked as read-only.  The person will be added to the first delegate
     * that is not read-only, and <em>does not already have a user for {@code Person}</em>.  Remaining delegates are
     * ignored.  A future design may provide Strategies that influence this behavior.
     *
     * @param person {@inheritDoc}
     */
    @Override
    public void insertPerson(Person person) {
        for (PersonDaoWrapper dao : delegates) {
            if (dao.isReadOnly()) {
                continue;
            }
            dao.insertPerson(person);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation does not invoke delegates marked as read-only.  The person will be updated on the first delegate
     * that is not read-only, and <em>already has a user for {@code Person}</em>.  Remaining delegates are
     * ignored.  A future design may provide Strategies that influence this behavior.
     *
     * @param person {@inheritDoc}
     */
    @Override
    public void updatePerson(Person person) {
        for (PersonDaoWrapper dao : delegates) {
            if (dao.isReadOnly()) {
                continue;
            }

            if (dao.selectPersonById(person.getId()) != null) {
                dao.updatePerson(person);
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation considers all delegates in order, and returns a {@code List} containing all {@code Person}s that
     * have the supplied {@code status}.  The {@code List} will be sorted by {@code comparator} if it is not
     * {@code null}.
     * 
     * @param status {@inheritDoc}
     * @param comparator {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public List<Person> selectPerson(RegistrationStatus status, Comparator<Person> comparator) {
        List<Person> results = new ArrayList<Person>();
        for (PersonDaoWrapper dao : delegates) {
            results.addAll(dao.selectPerson(status, comparator));
        }

        if (comparator != null) {
            Collections.sort(results, comparator);
        }

        return results;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation considers all delegates in order, and returns a {@code List} containing all {@code Person}s known to
     * each delegate.
     *
     * @return {@inheritDoc}
     */
    @Override
    public List<Person> selectPerson() {
        List<Person> results = new ArrayList<Person>();
        for (PersonDaoWrapper dao : delegates) {
            results.addAll(dao.selectPerson());
        }

        return results;
    }

}
