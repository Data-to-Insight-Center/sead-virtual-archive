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

import org.dataconservancy.ui.model.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation of PackageDAO.
 */
public class PackageDAOJdbcImpl extends DcsUiDaoBaseImpl implements PackageDAO {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
	private static final String PACKAGE_TBL = "PACKAGE";
    private static final String FILE_DATA_TABLE = "PACKAGE_FILE_DATA";
    
    public PackageDAOJdbcImpl(DataSource dataSource){
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
	}

    @Override
	public Package selectPackage(String packageId) {
		log.debug("Selecting package with objectId of {}", packageId);
		String query = "SELECT * from PACKAGE, PACKAGE_FILE_DATA where PACKAGE.OBJECT_ID = ? AND PACKAGE_FILE_DATA.PACKAGE_ID = PACKAGE.OBJECT_ID";
        List<Package> packages = (List<Package>) jdbcTemplate.query(query, 
                new Object[] { packageId },  new PackageResultSetExtractor() );
		if (packages.size() == 0) {
			return null;
		} else {
			return packages.get(0);
		}
	}

    @Override
    public List<Package> selectPackage(){
        log.debug("Selecting all packages from database");
		String query = "SELECT * from PACKAGE, PACKAGE_FILE_DATA where PACKAGE_FILE_DATA.PACKAGE_ID = PACKAGE.OBJECT_ID";
        List<Package> packages = (List<Package>) jdbcTemplate.query(query,
            new PackageResultSetExtractor() );
        return packages;
    }

    @Override
    public void insertPackage(Package dataPackage) throws DuplicateKeyException, DataIntegrityViolationException {
        log.debug("Insert Package {} into DB" , dataPackage);
		String packageQuery = "INSERT INTO  " + PACKAGE_TBL + " VALUES (?,?,?)";
        String fileDataQuery = "INSERT INTO " +  FILE_DATA_TABLE + " VALUES (?,?,?)";

        jdbcTemplate.update(packageQuery,
                new Object[] { dataPackage.getId(), 
                        dataPackage.getPackageType().toString(),
                        dataPackage.getPackageFileName() });
        String packageId = dataPackage.getId();
        for(String dataSetId : dataPackage.getFileData().keySet()) {
            jdbcTemplate.update(fileDataQuery,
                    new Object[] {  packageId, dataSetId, dataPackage.getFileData().get(dataSetId)});
        }
    }

    @Override
    public void deletePackage(String packageId){
       	log.debug("Deleting Package with objectId of {}", packageId);
		String query = "DELETE FROM " + PACKAGE_TBL + " WHERE OBJECT_ID = ?";
		jdbcTemplate.update(query, new Object[] { packageId  });
        query = "DELETE FROM " + FILE_DATA_TABLE + " WHERE PACKAGE_ID = ?";
        jdbcTemplate.update(query, new Object[] { packageId  });
    }

    @Override
    public void updatePackage(Package dataPackage) throws DuplicateKeyException, DataIntegrityViolationException{
        log.debug("Updating pPackage {}", dataPackage);
        String packageId = dataPackage.getId();
		String packageQuery = "UPDATE " + PACKAGE_TBL
				+ " SET PACKAGE_TYPE = ?" 
                + " , PACKAGE_FILE_NAME = ?" 
                + " WHERE OBJECT_ID = ? ";

		    jdbcTemplate.update(
				packageQuery,
				new Object[] { dataPackage.getPackageType().toString(),
						dataPackage.getPackageFileName(),
                        packageId });

        //remove all existing data set information from the FILE_DATA_TABLE for this package
        String removeQuery = "DELETE FROM " + FILE_DATA_TABLE + " WHERE PACKAGE_ID = ?";
        jdbcTemplate.update(removeQuery, new Object[] { packageId  });
        //now repopulate
        String fileDataQuery = "INSERT INTO " + FILE_DATA_TABLE
            + " VALUES (?,?,?)";
        for(String dataSetId : dataPackage.getFileData().keySet()) {
           try{
                jdbcTemplate.update(fileDataQuery,
                    new Object[] {  packageId, dataSetId, dataPackage.getFileData().get(dataSetId)});
            } catch (DuplicateKeyException e) {
                throw new DuplicateKeyException(dataSetId + " is an existing key in " + FILE_DATA_TABLE, e);
            }
        }
    }

	/**
	 * Provides method to check for existence of Package table in current database.
	 *
	 */
	private class CheckForPackageTable  implements DatabaseMetaDataCallback
	{

		@Override
		public Object processMetaData(DatabaseMetaData dbmd)
				throws SQLException, MetaDataAccessException {
			ResultSet rs = dbmd.getTables(null,null,PACKAGE_TBL,null);

	        if (rs.next()){
	        	return Boolean.TRUE;
		    }
	        return Boolean.FALSE;
		}
	}

	/**
	 * Provides method to check for existence of Package File Data table in current database.
	 *
	 */
    private class CheckForFileDataTable  implements DatabaseMetaDataCallback
	{

    @Override
	public Object processMetaData(DatabaseMetaData dbmd)
				throws SQLException, MetaDataAccessException {
			ResultSet rs = dbmd.getTables(null,null,FILE_DATA_TABLE,null);

	        if (rs.next()){
	        	return Boolean.TRUE;
		    }
	        return Boolean.FALSE;
		}
	}

    /**
     * Extracts package objects from result set
     */
    private class PackageResultSetExtractor implements ResultSetExtractor {
        private static final String PACKAGE_ID_COL = "OBJECT_ID";
        private static final String PACKAGE_NAME_COL = "PACKAGE_FILE_NAME";
        private static final String PACKAGE_TYPE_COL = "PACKAGE_TYPE";
        private static final String DATA_SET_ID_COL = "DATA_SET_ID";
        private static final String DATA_FILE_NAME_COL = "DATA_FILE_NAME";

        @Override
        public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            List<Package> results = new ArrayList<Package>();
            Map<String, Integer> packagePosition = new HashMap<String, Integer>();
            while(resultSet.next()){
                String currentId = resultSet.getString(PACKAGE_ID_COL);
                if(!packagePosition.containsKey(currentId)){
                    Package dataPackage = new Package();
                    dataPackage.setId(currentId);
                    dataPackage.setPackageFileName(resultSet.getString(PACKAGE_NAME_COL));
                    dataPackage.setPackageType(Package.PackageType.valueOf(resultSet.getString(PACKAGE_TYPE_COL)));
                    results.add(dataPackage);
                    packagePosition.put(currentId, results.indexOf(dataPackage));
                }
                results.get(packagePosition.get(currentId)).getFileData().put(resultSet.getString(DATA_SET_ID_COL),
                        resultSet.getString(DATA_FILE_NAME_COL));
             }
            return results;
        }
    }
}