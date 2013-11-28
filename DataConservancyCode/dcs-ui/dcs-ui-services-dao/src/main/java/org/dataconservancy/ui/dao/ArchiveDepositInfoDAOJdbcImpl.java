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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Type;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

/**
 * JDBC implementation of {@code ArchiveDepositInfoDAO}.
 */
public class ArchiveDepositInfoDAOJdbcImpl extends DcsUiDaoBaseImpl implements
        ArchiveDepositInfoDAO {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final ArchiveDepositInfoRowMapper MAPPER = new ArchiveDepositInfoRowMapper();

    static final String ARCHIVE_DEPOSIT_INFO_TBL = "ARCHIVE_DEPOSIT_INFO";
    static final String SELECT_MAX_DEPOSIT_DATE = "SELECT MAX(DEPOSIT_DATE) " +
            "FROM ARCHIVE_DEPOSIT_INFO " +
            "GROUP BY OBJECT_ID";

    static final String SELECT_MAX_DEPOSIT_DATE_WITH_STATUS = "SELECT MAX(DEPOSIT_DATE) " +
            " FROM ARCHIVE_DEPOSIT_INFO " +
            " WHERE DEPOSIT_STATUS = ? " +
            " GROUP BY OBJECT_ID";

    public ArchiveDepositInfoDAOJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
    }

    @Override
    public void add(ArchiveDepositInfo info) {
        log.trace("Insert ArchiveDepositInfo {} into DB", info);
        String query = "INSERT INTO  " + ARCHIVE_DEPOSIT_INFO_TBL
                + " VALUES (?,?,?,?,?,?,?,?)";
        DateTime depositDate;

        if (info.getDepositDateTime() == null) {
            depositDate = DateTime.now();
        } else {
            depositDate = info.getDepositDateTime();
        }

        jdbcTemplate.update(
                query,
                new Object[]{info.getObjectId(), info.getArchiveId(),
                        info.getStateId(),
                        info.getDepositId(),
                        info.getParentDepositId(),
                        info.getDepositStatus().toString(),
                        info.getObjectType().toString(),
                        depositDate.toDate()});

    }

    @Override
    public void update(ArchiveDepositInfo info) {
        log.trace("Updating ArchiveDepositInfo {}", info);
        String query = "UPDATE " + ARCHIVE_DEPOSIT_INFO_TBL
                + " SET OBJECT_ID = ? "
                + "   , ARCHIVE_ID = ? , STATE_ID = ? , DEPOSIT_STATUS = ?"
                + "   , OBJECT_TYPE = ? , DEPOSIT_DATE = ?, PARENT_DEPOSIT_ID = ? "
                + " WHERE DEPOSIT_ID = ? ";
        jdbcTemplate.update(query,
                new Object[]{info.getObjectId(), info.getArchiveId(),
                        info.getStateId(),
                        info.getDepositStatus().toString(),
                        info.getObjectType().toString(), info.getDepositDateTime().toDate(),
                        info.getParentDepositId(),
                        info.getDepositId()});
    }

    @Override
    public ArchiveDepositInfo lookup(String deposit_id) {
        log.trace("Select archive deposit info with deposit id of {}",
                deposit_id);
        String query = "SELECT * FROM " + ARCHIVE_DEPOSIT_INFO_TBL
                + " WHERE DEPOSIT_ID = ?";
        List<ArchiveDepositInfo> infoList = jdbcTemplate.query(query,
                new Object[]{deposit_id}, MAPPER);

        if (infoList.size() == 0) {
            return null;
        } else {
            return infoList.get(0);
        }
    }

    public List<ArchiveDepositInfo> lookupChildren(String deposit_id) {
        log.trace("Select archive deposit info(s) with a parent deposit id of {}",
                deposit_id);
        String query = "SELECT * FROM " + ARCHIVE_DEPOSIT_INFO_TBL
                + " WHERE PARENT_DEPOSIT_ID = ?";
        List<ArchiveDepositInfo> infoList = jdbcTemplate.query(query,
                new Object[]{deposit_id}, MAPPER);

        if (infoList.size() == 0) {
            return null;
        } else {
            return infoList;
        }
    }

    @Override
    public List<ArchiveDepositInfo> list(Type type, Status status) {
        List<ArchiveDepositInfo> infoList;

        if (type == null && status == null) {
            infoList = jdbcTemplate
                    .query("SELECT * FROM " + ARCHIVE_DEPOSIT_INFO_TBL
                           
                            + " ORDER BY DEPOSIT_DATE DESC", MAPPER);
        } else if (type == null) {
            infoList = list(status);
        } else if (status == null) {
            infoList = list(type);
        } else {
            String query = "SELECT * FROM " + ARCHIVE_DEPOSIT_INFO_TBL +
                    "       WHERE OBJECT_TYPE = ? " +
                    "       AND DEPOSIT_STATUS = ? " +
                   
                    "       ORDER BY DEPOSIT_DATE DESC";
            infoList = jdbcTemplate.query(query,
                    new Object[]{type.toString(), status.toString()},
                    MAPPER);
        }

        return infoList;
    }

    public List<ArchiveDepositInfo> list(Status status) {
        String query = "SELECT * FROM " + ARCHIVE_DEPOSIT_INFO_TBL +
                " WHERE DEPOSIT_STATUS = ? " +
                " ORDER BY DEPOSIT_DATE DESC";
        List<ArchiveDepositInfo> infoList = jdbcTemplate
                .query(query,
                        new Object[]{status.toString()},
                        MAPPER);
        return infoList;
    }

    public List<ArchiveDepositInfo> list(Type type) {
        String query = "SELECT * FROM " + ARCHIVE_DEPOSIT_INFO_TBL +
                "       WHERE OBJECT_TYPE = ? " +
                "       ORDER BY DEPOSIT_DATE DESC";

        List<ArchiveDepositInfo> infoList = jdbcTemplate
                .query(query,
                        new Object[]{type.toString()},
                        MAPPER);
        return infoList;
    }

    /**
     * Provides method to check for existence of Person table in current
     * database.
     */
    private class CheckForArchiveDepositInfoTable implements
            DatabaseMetaDataCallback {
        @Override
        public Object processMetaData(DatabaseMetaData dbmd)
                throws SQLException, MetaDataAccessException {
            ResultSet rs = dbmd.getTables(null, null, ARCHIVE_DEPOSIT_INFO_TBL,
                    null);

            if (rs.next()) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    }

    private static class ArchiveDepositInfoRowMapper implements
            RowMapper<ArchiveDepositInfo> {
        public ArchiveDepositInfo mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            ArchiveDepositInfo info = new ArchiveDepositInfo();
            info.setArchiveId(rs.getString("ARCHIVE_ID"));
            info.setDepositId(rs.getString("DEPOSIT_ID"));
            info.setParentDepositId(rs.getString("PARENT_DEPOSIT_ID"));
            info.setObjectId(rs.getString("OBJECT_ID"));
            info.setStateId(rs.getString("STATE_ID"));
            info.setDepositStatus(Status.valueOf(rs.getString("DEPOSIT_STATUS")
                    .toUpperCase()));
            info.setObjectType(Type.valueOf(rs.getString("OBJECT_TYPE")
                    .toUpperCase()));
            info.setDepositDateTime(new DateTime(rs.getTimestamp("DEPOSIT_DATE")));
            return info;

        }
    }

    public List<ArchiveDepositInfo> listForObject(String object_id,
                                                  Status status) {
        if (status == null) {
            String query = "SELECT * FROM " + ARCHIVE_DEPOSIT_INFO_TBL +
                    "       WHERE OBJECT_ID = ? " +
                    "       ORDER BY DEPOSIT_DATE DESC";
            return jdbcTemplate.query(query,
                    new Object[]{object_id},
                    MAPPER);
        } else {
            String query = "SELECT * FROM " + ARCHIVE_DEPOSIT_INFO_TBL
                    + " WHERE OBJECT_ID = ? "
                    + " AND DEPOSIT_STATUS = ? "
                    + " ORDER BY DEPOSIT_DATE DESC";
            return jdbcTemplate.query(query,
                    new Object[]{object_id, status.toString()},
                    MAPPER);
        }
    }
}
