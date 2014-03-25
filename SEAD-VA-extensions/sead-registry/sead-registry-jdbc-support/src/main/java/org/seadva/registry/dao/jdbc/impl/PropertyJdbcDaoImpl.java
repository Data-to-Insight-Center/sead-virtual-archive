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

import org.seadva.registry.dao.PropertyDao;
import org.seadva.registry.dao.impl.PropertyDaoImpl;
import org.seadva.registry.dao.jdbc.DatabaseSingleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read/Write property to db
 */
public class PropertyJdbcDaoImpl extends PropertyDaoImpl {

    static String tableName = "property";

    public boolean insertSingleValueProperty(PropertyDao propertyDao) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        if(ifExists(propertyDao.getEntity_id(), propertyDao.getName())){
            String sql = "UPDATE "+tableName+" SET valueStr='"+propertyDao.getValueStr()+"' " +
                    "WHERE entity_id='"+propertyDao.getEntity_id()+"' AND name='"+propertyDao.getName()+"'";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
        }
        else{
            String sql = "INSERT INTO "+tableName+" (property_id, entity_id, name, valueStr) VALUES (?, ?, ?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, propertyDao.getProperty_id());
            preparedStatement.setString(2, propertyDao.getEntity_id());
            preparedStatement.setString(3, propertyDao.getName());
            preparedStatement.setString(4, propertyDao.getValueStr());
            preparedStatement.execute();

        }
        connection.close();
        return true;
    }

    private boolean ifExists(String entityId, String key) throws Exception, SQLException, IllegalAccessException, InstantiationException {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT property_id, name, valueStr from "+tableName+" where entity_id='"+entityId+"' and name='"+key+"'");
        ResultSet resultSet = preparedStatement.executeQuery();

        boolean result =false;
        if(resultSet.next())
            result = true;
        connection.close();
        return result;
    }
    public boolean insertProperty(PropertyDao propertyDao) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        String sql = "INSERT IGNORE INTO "+tableName+" (property_id, entity_id, name, valueStr) VALUES (?, ?, ?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setInt(1, propertyDao.getProperty_id());
        preparedStatement.setString(2, propertyDao.getEntity_id());
        preparedStatement.setString(3, propertyDao.getName());
        preparedStatement.setString(4, propertyDao.getValueStr());
        preparedStatement.execute();

        connection.close();
        return true;
    }


    public List<PropertyDao> getPropertyForEntity(String entityId) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        List<PropertyDao> propertyDaoList = new ArrayList<PropertyDao>();

        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT property_id, name, valueStr from "+tableName+" where entity_id='"+entityId+"'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            PropertyDao propertyDao = new PropertyDao();
            propertyDao.setEntity_id(entityId);
            propertyDao.setProperty_id(resultSet.getInt("property_id"));
            propertyDao.setName(resultSet.getString("name"));
            propertyDao.setValueStr(resultSet.getString("valueStr"));
            propertyDaoList.add(propertyDao);
        }
        preparedStatement.execute();
        connection.close();
        return propertyDaoList;
    }


}
