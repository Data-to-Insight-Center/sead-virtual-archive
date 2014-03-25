/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seadva.registry.dao.jdbc;

import com.sun.jersey.test.framework.JerseyTest;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.Assert.assertTrue;

/**
 * Db test cases
 */
public class DatabaseSingletonTest extends JerseyTest {

    public DatabaseSingletonTest() throws Exception {
        super("org.seadva.registry.dao.jdbc");

    }

    @Test
    public void testDatabase() throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        String sql = "show tables;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        assertTrue(preparedStatement.execute());
    }

}
