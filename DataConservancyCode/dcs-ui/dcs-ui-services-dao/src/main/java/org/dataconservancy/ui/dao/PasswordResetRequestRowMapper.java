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

import org.dataconservancy.ui.model.PasswordResetRequest;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class provides method to map a row from PASSWORD_RESET table result set into a
 * {@link org.dataconservancy.ui.model.PasswordResetRequest} object.
 */
class PasswordResetRequestRowMapper implements RowMapper<PasswordResetRequest>{

        public PasswordResetRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            PasswordResetRequest prr = new PasswordResetRequest();

            prr.setId(rs.getString("ID"));
            prr.setRequestDate(new DateTime(rs.getLong("REQUEST_DATE")));
            prr.setUserEmailAddress(rs.getString("EMAIL_ADDRESS"));

            return prr;
        }
}

