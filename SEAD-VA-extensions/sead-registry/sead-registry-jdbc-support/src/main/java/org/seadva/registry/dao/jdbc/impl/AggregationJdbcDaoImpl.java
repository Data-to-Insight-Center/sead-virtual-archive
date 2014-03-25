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

import org.seadva.registry.dao.AggregationDao;
import org.seadva.registry.dao.impl.AggregationDaoImpl;
import org.seadva.registry.dao.jdbc.DatabaseSingleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Read/Write Aggregation From database
 */
public class AggregationJdbcDaoImpl extends AggregationDaoImpl {

    static String tableName = "aggregation";

    public boolean insertAggregation(AggregationDao aggregationDao) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        String sql = "INSERT IGNORE INTO "+tableName+" (parent_id, child_id) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, aggregationDao.getParent_id());
        preparedStatement.setString(2, aggregationDao.getChild_id());
        preparedStatement.execute();
        connection.close();
        return true;
    }

    public List<AggregationDao> getAggregationForParent(String parentId) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        List<AggregationDao> aggregationDaoList = new ArrayList<AggregationDao>();

        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT child_id from "+tableName+" where parent_id='"+parentId+"'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            AggregationDao aggregationDao = new AggregationDao();
            aggregationDao.setParent_id(parentId);
            aggregationDao.setChild_id(resultSet.getString("child_id"));
            aggregationDaoList.add(aggregationDao);
        }
        preparedStatement.execute();
        connection.close();
        return aggregationDaoList;
    }

    public List<AggregationDao> getAggregationForChild(String childId) throws Exception {
        Connection connection = DatabaseSingleton.getInstance().getConnection();

        List<AggregationDao> aggregationDaoList = new ArrayList<AggregationDao>();

        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT parent_id from "+tableName+" where child_id='"+childId+"'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            AggregationDao aggregationDao = new AggregationDao();
            aggregationDao.setChild_id(childId);
            aggregationDao.setParent_id(resultSet.getString("parent_id"));
            aggregationDaoList.add(aggregationDao);
        }
        preparedStatement.execute();
        connection.close();
        return aggregationDaoList;
    }
}
