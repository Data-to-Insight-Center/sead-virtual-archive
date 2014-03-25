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

import org.seadva.registry.dao.RelationDao;
import org.seadva.registry.dao.impl.RelationDaoImpl;
import org.seadva.registry.dao.jdbc.DatabaseSingleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Read write relation to db
 */
public class RelationJdbcDaoImpl extends RelationDaoImpl {

    static String tableName = "relation";

    public boolean insertRelation(RelationDao relationDao) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        String sql = "INSERT IGNORE INTO "+tableName+" (cause_id, relation, effect_id) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, relationDao.getCause_id());
        preparedStatement.setString(2, relationDao.getRelation());
        preparedStatement.setString(3, relationDao.getEffect_id());
        preparedStatement.execute();
        connection.close();
        return true;
    }

    public List<RelationDao> getRelationForEntity(String entityId) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        List<RelationDao> relationDaoList = new ArrayList<RelationDao>();

        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT relation, effect_id from "+tableName+" where cause_id='"+entityId+"'");
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            RelationDao relationDao = new RelationDao();
            relationDao.setCause_id(entityId);
            relationDao.setEffect_id(resultSet.getString("effect_id"));
            relationDao.setRelation(resultSet.getString("relation"));
            relationDaoList.add(relationDao);
        }
        preparedStatement.execute();
        connection.close();

        return relationDaoList;
    }
}
