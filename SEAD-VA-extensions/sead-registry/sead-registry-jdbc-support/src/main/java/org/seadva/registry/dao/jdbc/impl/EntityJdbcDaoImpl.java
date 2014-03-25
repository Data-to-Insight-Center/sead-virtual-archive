/*
 * Copyright 2014 The Trustees of Indiana University
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

package org.seadva.registry.dao.jdbc.impl;

import org.seadva.registry.dao.EntityDao;
import org.seadva.registry.dao.impl.EntityDaoImpl;
import org.seadva.registry.dao.jdbc.DatabaseSingleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Read/Wrote Entity from db
 */
public class EntityJdbcDaoImpl extends EntityDaoImpl {

    static String tableName = "entity";

    public boolean insertEntity(EntityDao entityDao) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        String sql = "INSERT INTO "+tableName+" (entity_name, entity_id) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE entity_name=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, entityDao.getEntity_name());
        preparedStatement.setString(2, entityDao.getEntity_id());
        preparedStatement.setString(3, entityDao.getEntity_name());
        preparedStatement.execute();
        connection.close();
        return true;
    }

    public EntityDao getEntity(String entityId) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        EntityDao entityDao = new EntityDao();
        entityDao.setEntity_id(entityId);

        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT entity_id, entity_name from "+tableName+" where entity_id='"+entityId+"'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            entityDao.setEntity_name(resultSet.getString("entity_name"));
        }
        preparedStatement.execute();
        connection.close();
        return entityDao;
    }
}
