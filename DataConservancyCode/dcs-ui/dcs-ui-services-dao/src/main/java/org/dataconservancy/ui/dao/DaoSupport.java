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

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.sql.Types;

/**
 *
 */
public class DaoSupport {

    public static StringBuilder dumpTable(JdbcTemplate t, String tableName) {
        final StringBuilder sb = new StringBuilder("Contents of table [" + tableName + "]\n");
        final String SELECT_ALL_QUERY = "SELECT * FROM " + tableName;

        final SqlRowSet rowSet = t.queryForRowSet(SELECT_ALL_QUERY);

        if (rowSet == null) {
            sb.append("Error: no rows returned for [" + SELECT_ALL_QUERY + "]\n");
            return sb;
        }

        SqlRowSetMetaData rowMd = rowSet.getMetaData();

        if (rowMd == null) {
            sb.append("Error: couldn't obtain ResultSetMetadata for [" + SELECT_ALL_QUERY + "]\n");
            return sb;
        }

        String[] columnNames = rowMd.getColumnNames();

        if (columnNames == null) {
            sb.append("Error: couldn't obtain column names for [" + SELECT_ALL_QUERY + "]\n");
            return sb;
        }

        int[] columnTypes = new int[columnNames.length];

        for (int i = 0; i < columnTypes.length; i++) {
            columnTypes[i] = rowMd.getColumnType(i+1);
        }


        while (rowSet.next()) {
            for (int i = 0; i < columnNames.length; i++) {
                sb.append("[").append(columnNames[i]).append("]: [");
                int columnIndex = i + 1;
                switch (columnTypes[i]) {
                    case Types.VARCHAR:
                        sb.append(rowSet.getString(columnIndex));
                        break;

                    case Types.LONGVARCHAR:
                        sb.append(rowSet.getLong(columnIndex));
                        break;

                    default:
                        sb.append("Unhandled SQL type [" + columnTypes[i] + "] for [" + SELECT_ALL_QUERY + "]");
                        break;
                }

                sb.append("]");

                if (i != (columnNames.length - 1)) {
                    sb.append(", ");
                }
            }

            sb.append("\n");
        }

        sb.append("\n");

        return sb;
    }
}
