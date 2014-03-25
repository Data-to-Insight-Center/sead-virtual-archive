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

import org.seadva.registry.dao.EntityContentDao;
import org.seadva.registry.dao.impl.EntityContentDaoImpl;
import org.seadva.registry.dao.jdbc.DatabaseSingleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Read/Write entity content (blob) from database
 */
public class EntityContentJdbcDaoImpl extends EntityContentDaoImpl {

    static String tableName = "entity_content";

    public boolean insertEntityContent(EntityContentDao entityContentDao, String entityId) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        String sql = "INSERT INTO "+tableName+" (entity_content_id, entity_content_data, entity_id) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, entityContentDao.getEntityContentId());
        preparedStatement.setBlob(2, entityContentDao.getEntityContent());
        preparedStatement.setString(3, entityId);
        preparedStatement.execute();
        connection.close();
        return true;
    }

    public EntityContentDao getEntityContentForEntity(String entityId) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        EntityContentDao entityContentDao = new EntityContentDao();

        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT entity_type, entity_name from entityDao where entity_id='"+entityId+"'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            entityContentDao.setEntityContentId(resultSet.getInt("entity_content_id"));
            entityContentDao.setEntityContent(resultSet.getBlob("entity_content_data").getBinaryStream());
        }
        preparedStatement.execute();
        connection.close();
        return entityContentDao;
    }
}
