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

import org.seadva.registry.api.ResourceType;
import org.seadva.registry.dao.EntityDao;
import org.seadva.registry.dao.EntityTypeDao;
import org.seadva.registry.dao.impl.EntityDaoImpl;
import org.seadva.registry.dao.impl.EntityTypeDaoImpl;
import org.seadva.registry.dao.jdbc.DatabaseSingleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read/Write EntityType from db
 */
public class EntityTypeJdbcDaoImpl extends EntityTypeDaoImpl {

    static String tableName = "entity_type";

    public boolean insertEntityType(EntityTypeDao entityTypeDao) throws Exception {
        if(ifExistsType(ResourceType.fromString(entityTypeDao.getEntityTypeName()),entityTypeDao.getEntity_id()))
            return true;
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        String sql = "INSERT INTO "+tableName+" (entity_type_id, entity_type_name, entity_id) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setString(1, entityTypeDao.getEntityTypeId());
        preparedStatement.setString(2, entityTypeDao.getEntityTypeName());
        preparedStatement.setString(3, entityTypeDao.getEntity_id());
        preparedStatement.execute();
        connection.close();

        return true;
    }

    public boolean ifExistsType(ResourceType resourceType, String entityId) throws Exception {

        Connection connection = DatabaseSingleton.getInstance().getConnection();

        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT entity_type_id from "+tableName+
                        " where entity_type_name='"+resourceType.getText()+"'" +
                        " and entity_id='"+entityId+"'");
        ResultSet resultSet = preparedStatement.executeQuery();
        boolean result = false;

        if(resultSet.next())
            result  =true;
        connection.close();

        return result;
    }

    public List<EntityTypeDao> getEntity(String entityTypeId) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        List<EntityTypeDao> entityTypeDaoList = new ArrayList<EntityTypeDao>();


        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT entity_type_name from "+tableName+" where entity_type_id='"+entityTypeId+"'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            EntityTypeDao entityTypeDao = new EntityTypeDao();
            entityTypeDao.setEntityTypeId(entityTypeId);
            entityTypeDao.setEntityTypeName(resultSet.getString("entity_type_name"));
            entityTypeDaoList.add(entityTypeDao);
        }
        preparedStatement.execute();
        connection.close();
        return entityTypeDaoList;
    }

    public List<EntityTypeDao> getEntityType(String entityId) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        List<EntityTypeDao> entityTypeDaoList = new ArrayList<EntityTypeDao>();


        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT entity_type_id, entity_type_name from "+tableName+" where entity_id='"+entityId+"'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            EntityTypeDao entityTypeDao = new EntityTypeDao();
            entityTypeDao.setEntityTypeId(resultSet.getString("entity_type_id"));
            entityTypeDao.setEntityTypeName(resultSet.getString("entity_type_name"));
            entityTypeDaoList.add(entityTypeDao);
        }
        preparedStatement.execute();
        connection.close();
        return entityTypeDaoList;
    }
}
