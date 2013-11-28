package org.dataconservancy.ui.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.dataconservancy.storage.dropbox.model.DropboxToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class DropboxDAOJdbcImpl extends DcsUiDaoBaseImpl implements DropboxDAO {
    
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    
    private static final String DROPBOX_TBL = "DROPBOX";
    
    public DropboxDAOJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
    }

    @Override
    public boolean insertToken(DropboxToken token, String userId) {
        String query = "INSERT INTO " + DROPBOX_TBL + " VALUES (?, ?, ?, ?)";
        
        int i = jdbcTemplate.update(query,
 new Object[] { userId, "true", token.getAppKey(), token.getAppSecret() });
        if (i > 0) {
            return true;
        }
        else {
            LOG.error("Could not insert the records into the database.");
            return false;
        }

    }
    
    @Override
    public DropboxToken getToken(String userId) {
        String query = "SELECT * FROM " + DROPBOX_TBL + " WHERE PERSON_ID = '"
                + userId + "'";
        List<DropboxToken> token = jdbcTemplate.query(query, new DropboxTblRowMapper());
        return token.get(0);
    }
    
    private class DropboxTblRowMapper implements RowMapper<DropboxToken> {
        
        @Override
        public DropboxToken mapRow(ResultSet rs, int rowNum) throws SQLException {
            DropboxToken token = new DropboxToken();
            token.setAppKey(rs.getString("DROPBOX_APP_KEY"));
            token.setAppSecret(rs.getString("DROPBOX_APP_SECRET"));
            return token;
        }
        
    }
    
}
