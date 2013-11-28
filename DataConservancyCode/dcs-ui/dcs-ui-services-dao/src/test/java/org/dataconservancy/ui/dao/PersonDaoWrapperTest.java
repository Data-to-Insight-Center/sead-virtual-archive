/*
 * Copyright 2013 Johns Hopkins University
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

import org.dataconservancy.ui.model.Person;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests insuring that the PersonDaoWrapper is properly wrapping the Person objects being returned, and that it is
 * honoring the readOnly flag when attempting to mutate Persons.
 */
public class PersonDaoWrapperTest {

    private final PersonDAO mockDao = mock(PersonDAO.class);

    private final PersonDaoWrapper readOnlyWrapper = new PersonDaoWrapper(mockDao);

    @Before
    public void setUp() throws Exception {
        readOnlyWrapper.setReadOnly(true);
        // sanity
        assertTrue(readOnlyWrapper.isReadOnly());
    }

    /**
     * Insures that the readOnly flag of retrieved Persons is being set to the value of the readOnly flag of the
     * PersonDaoWrapper.
     *
     * @throws Exception
     */
    @Test
    public void testSetsReadonlyFlagOnPerson() throws Exception {
        // Set the readOnly flag to 'true' on the Person being returned by the DAO.
        boolean personReadOnly = true;
        Person p = new Person();
        p.setReadOnly(personReadOnly);

        // Configure mockDao behavior to return the Person in a List.
        when(mockDao.selectPerson()).thenReturn(Arrays.asList(p));

        // Construct the PersonDaoWrapper that's under test
        PersonDaoWrapper underTest = new PersonDaoWrapper(mockDao);
        // Configure the PersonDaoWrapper with the opposite read-only behavior (that is, readOnly is 'false')
        boolean wrapperReadOnly = !personReadOnly;
        underTest.setReadOnly(wrapperReadOnly);

        // Sanity check
        assertEquals(1, underTest.selectPerson().size());

        // The returned Person should have the read-only flag set to the value of the Wrapper, not the Person field.
        assertEquals("Expected the value of the read only flag to be that configured on the wrapper, not the " +
                "value configured on the Person.", wrapperReadOnly, underTest.selectPerson().get(0).getReadOnly());
    }

    /**
     * Insures that mutating methods result in the proper exception when called on a readOnly wrapper
     *
     * @throws Exception
     */
    @Test(expected = RuntimeException.class)
    public void testDeleteReadOnlyWrapper() throws Exception {
        readOnlyWrapper.deletePersonById("anyIdShouldWork");
    }

    /**
     * Insures that mutating methods result in the proper exception when called on a readOnly wrapper
     *
     * @throws Exception
     */
    @Test(expected = RuntimeException.class)
    public void testUpdateReadOnlyWrapper() throws Exception {
        readOnlyWrapper.updatePerson(new Person());
    }

    /**
     * Insures that mutating methods result in the proper exception when called on a readOnly wrapper
     *
     * @throws Exception
     */
    @Test(expected = RuntimeException.class)
    public void testInsertReadOnlyWrapper() throws Exception {
        readOnlyWrapper.insertPerson(new Person());
    }
}
