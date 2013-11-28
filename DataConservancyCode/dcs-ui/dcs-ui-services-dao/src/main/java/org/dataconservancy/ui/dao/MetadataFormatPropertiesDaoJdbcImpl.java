/*
 * Copyright 2013 Johns Hopkins University
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

import org.dataconservancy.ui.model.MetadataFormatProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC implementation of the {@link MetadataFormatPropertiesDao} interface.
 * <p/>
 * Persisting the information contained in a {@code MetadataFormatProperties} instance is split between two classes:
 * this class, and the {@code RelationshipService}.  The {@code RelationshipService} persists the disciplines that
 * a metadata format applies to, while this class persists all the other properties.  Higher level services will insure
 * that when a MetadataFormatProperties object is retrieved, its state will be complete: its boolean business properties
 * and the discipline identifiers it applies to.
 */
public class MetadataFormatPropertiesDaoJdbcImpl implements MetadataFormatPropertiesDao {

    static final String METADATA_PROPERTIES_TABLE = "METADATA_FORMAT_PROPERTIES";

    static final String FORMAT_ID_COLUMN = "METADATA_FORMAT_ID";

    static final String FLAGS_COLUMN = "FLAGS";
    static final String ACTIVE_COLUMN = "ACTIVE";

    static final String INSERT_NEW_PROPERTY_OBJ = "INSERT INTO " + METADATA_PROPERTIES_TABLE + " VALUES (?, ?, ?)";

    static final String UPDATE_EXISTING_PROPERTY_OBJ = "UPDATE " + METADATA_PROPERTIES_TABLE +
            " SET " + FLAGS_COLUMN + " = ? " +
            "   , " + ACTIVE_COLUMN + " = ? WHERE " + FORMAT_ID_COLUMN + " = ?";

    static final String SELECT_PROPERTY_OBJ_BY_ID = "SELECT * FROM " + METADATA_PROPERTIES_TABLE + " WHERE " +
            FORMAT_ID_COLUMN + " = ?";

    static final String SELECT_ALL_PROPERTY_OBJS = "SELECT * FROM " + METADATA_PROPERTIES_TABLE + " ORDER BY " +
            FORMAT_ID_COLUMN + " ASC";

    static final String SELECT_PROPERTY_OBJS_BY_STATUS = "SELECT * FROM " + METADATA_PROPERTIES_TABLE +
            " WHERE ACTIVE = ?";

    static final byte APPLIES_TO_PROJECT_FLAG = 0x01; // 1

    static final byte APPLIES_TO_COLLECTION_FLAG = 0x02; // 2

    static final byte APPLIES_TO_ITEM_FLAG = 0x04; // 4

    static final byte VALIDATES_FLAG = 0x08; // 8 (the next flag would use 0x10, 16)

    static final int[] FLAGS = new int[] {
            APPLIES_TO_PROJECT_FLAG,
            APPLIES_TO_COLLECTION_FLAG,
            APPLIES_TO_ITEM_FLAG,
            VALIDATES_FLAG
    };

    private static final RowMapper<MetadataFormatProperties> ROW_MAPPER = new MetadataPropertiesRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public MetadataFormatPropertiesDaoJdbcImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Constructor for unit testing <em>only</em>
     */
    MetadataFormatPropertiesDaoJdbcImpl() {
        jdbcTemplate = null;
    }

    @Override
    public void add(MetadataFormatProperties properties) {
        jdbcTemplate.update(INSERT_NEW_PROPERTY_OBJ, new Object [] {
                properties.getFormatId(),
                Byte.toString(flags(properties)),
                properties.isActive()});
    }

    @Override
    public void update(MetadataFormatProperties properties) {
        jdbcTemplate.update(UPDATE_EXISTING_PROPERTY_OBJ, new Object [] {
                Byte.toString(flags(properties)),
                properties.isActive(),
                properties.getFormatId()});
    }

    @Override
    public MetadataFormatProperties get(String metadataFormatId) {
        return jdbcTemplate.queryForObject(SELECT_PROPERTY_OBJ_BY_ID, ROW_MAPPER, metadataFormatId);
    }

    @Override
    public List<MetadataFormatProperties> list() {
        return jdbcTemplate.query(SELECT_ALL_PROPERTY_OBJS, ROW_MAPPER);
    }

    @Override
    public List<MetadataFormatProperties> list(boolean isActive) {
        return jdbcTemplate.query(SELECT_PROPERTY_OBJS_BY_STATUS, ROW_MAPPER, isActive);
    }

    /**
     * Maps the boolean fields of the {@code properties} object to a byte, with the most significant bit to the left.
     * When a field is {@code true}, the bit position corresponding to the flag will be set to 1.  When a field
     * is {@code false}, the bit position corresponding to the flag will be set to 0.
     *
     * @param properties the MetadataFormatProperties
     * @return a byte with the flags set, the MSB to the left
     * @throws RuntimeException if a flag is not properly handled internally
     */
    static byte flags(MetadataFormatProperties properties) {
        byte flags = 0x00;

        for (int flag : FLAGS) {
            switch (flag) {
                case APPLIES_TO_COLLECTION_FLAG:
                    if (properties.isAppliesToCollection()) {
                        flags |= APPLIES_TO_COLLECTION_FLAG;
                    }
                    break;

                case APPLIES_TO_ITEM_FLAG:
                    if (properties.isAppliesToItem()) {
                        flags |= APPLIES_TO_ITEM_FLAG;
                    }
                    break;
                case APPLIES_TO_PROJECT_FLAG:
                    if (properties.isAppliesToProject()) {
                        flags |= APPLIES_TO_PROJECT_FLAG;
                    }
                    break;
                case VALIDATES_FLAG:
                    if (properties.isValidates()) {
                        flags |= VALIDATES_FLAG;
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown flag!");
            }
        }

        return flags;
    }

    /**
     * Maps a byte representation of flags to properties on the {@code MetadataFormatProperties}; the properties on
     * {@code MetadataFormatProperties} will be set according to the bit values contained in the byte.  When a bit is 1,
     * the corresponding field in the properties object is set to {@code true}. When a bit is 0, the corresponding field
     * in the properties object is set to {@code false}.
     *
     * @param flags a byte with the flags set, the MSB to the left
     * @param properties the MetadataFormatProperties
     * @throws RuntimeException if a flag is not properly handled internally
     */
    static void flags(byte flags, MetadataFormatProperties properties) {
        for (int flag : FLAGS) {
            switch (flag) {
                case APPLIES_TO_COLLECTION_FLAG:
                    if ((flags & APPLIES_TO_COLLECTION_FLAG) == APPLIES_TO_COLLECTION_FLAG) {
                        properties.setAppliesToCollection(true);
                    } else {
                        properties.setAppliesToCollection(false);
                    }
                    break;
                case APPLIES_TO_ITEM_FLAG:
                    if ((flags & APPLIES_TO_ITEM_FLAG) == APPLIES_TO_ITEM_FLAG) {
                        properties.setAppliesToItem(true);
                    } else {
                        properties.setAppliesToItem(false);
                    }
                    break;
                case APPLIES_TO_PROJECT_FLAG:
                    if ((flags & APPLIES_TO_PROJECT_FLAG) == APPLIES_TO_PROJECT_FLAG) {
                        properties.setAppliesToProject(true);
                    } else {
                        properties.setAppliesToProject(false);
                    }
                    break;
                case VALIDATES_FLAG:
                    if ((flags & VALIDATES_FLAG) == VALIDATES_FLAG) {
                        properties.setValidates(true);
                    } else {
                        properties.setValidates(false);
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown flag!");

            }
        }
    }

    /**
     * Maps a row from the database table to a properties object.
     */
    private static class MetadataPropertiesRowMapper implements RowMapper<MetadataFormatProperties> {
        @Override
        public MetadataFormatProperties mapRow(ResultSet resultSet, int i) throws SQLException {
            MetadataFormatProperties properties = new MetadataFormatProperties();
            properties.setFormatId(resultSet.getString(FORMAT_ID_COLUMN));
            flags(Byte.parseByte(resultSet.getString(FLAGS_COLUMN)), properties);
            properties.setActive(resultSet.getBoolean(ACTIVE_COLUMN));
            return properties;
        }
    }

}
