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

import org.dataconservancy.ui.test.support.model.BaseModelTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests that DelegatingPersonDao is producing expected results
 */
@ContextConfiguration(locations = {"classpath*:/org/dataconservancy/ui/config/test-applicationContext.xml", "classpath*:/org/dataconservancy/ui/config/applicationContext.xml"})
@DirtiesDatabase
public class DelegatingPersonDaoTest extends BaseModelTest {

    @Autowired
    private DelegatingPersonDaoImpl underTest;

    /**
     * test to make sure we get a DuplicateKeyException when trying to add the same user twice
     */
    @Ignore("Ignoring because this test doesn't really make sense.  Nowhere does DelegatingPersonDao" +
            "say that two calls to insertPerson will throw a DuplicateKeyException?")
    @Test(expected = DuplicateKeyException.class)
    public void testCantCreateSameUserTwice() {
        underTest.insertPerson(newUser);
        underTest.insertPerson(newUser);
    }
}
