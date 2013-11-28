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
package org.dataconservancy.transform.dcp.solr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.transform.dcp.AbstractReader;
import org.dataconservancy.transform.dcp.DcpOutput;

/**
 * Produces DeliverableUnit identifiers from a Dcs Query.
 * <p>
 * Given an query service query string, this reader will execute the query. For
 * for every DeliverableUnit present in a search result, the entity identifier
 * will be readable. That is to say, this will read a list of DU identifiers
 * resulting from a query, discarding any entities that are not a
 * DeliverableUnit.
 * </p>
 */
public class QueryDuReader extends AbstractReader
{
	
	private DcsDataModelQueryService queryService;
	
	private DcpBuilder dcpBuilder;

	private String stagingDirectory;
	
	private final File stagingFile;
	
	private final String queryString;
	
	private static final int stagingFileNamePrefix = new Random().nextInt();
	
	private static final String stagingFileNameSuffix = ".tmp";
	
	/**
	 * Constructor - initialize variables and set up query results to be iterate through.
	 * @param queryService - <code>DcpQueryService</code> - an instance of query service, through which
	 * the query will be executed. 
	 * @param queryString - <code>String</code> - input query to be executed.
	 * @param stagingDirectory- <code>String</code> - full name of the staging directory 
	 * 												to which intermediate output will be written.
	 */
	public QueryDuReader(DcsDataModelQueryService queryService, String queryString, String stagingDirectory)
	{
		this.queryService = queryService;
		this.queryString = queryString;
		this.stagingDirectory = stagingDirectory;
		this.resultMap = new HashMap<String, Dcp>();
		this.stagingFile = createStagingFile();
		this.dcpBuilder = new DcpBuilder(queryService);
		setup();
	}
	
	/**
	 * Constructor 
	 * <p>
	 * When staging directory is not provided, staging file will be written to default location
	 * @param queryService - <code>DcpQueryService</code> - an instance of query service, through which
	 * the query will be executed. 
	 * @param queryString - <code>String</code> - input query to be executed
	 */
	public QueryDuReader(DcsDataModelQueryService queryService, String queryString)
	{
		this.queryService = queryService;
		this.queryString = queryString;
		this.resultMap = new HashMap<String, Dcp>(); 
		this.stagingDirectory = null;
		this.stagingFile = createStagingFile();
		
		this.dcpBuilder = new DcpBuilder(queryService);
		//TODO: set up the query result
		setup();		
	}
	
	/**
	 * Remove staging file.
	 * <p>
	 * To be executed after usage of the instance of QueryDuReader is complete.
	 */
	
	public void close()
	{
		stagingFile.delete();
	}
	
	
	/**
	 * Sets up the QueryDuReader:
	 * <ul>
	 * <li>executes the query  
	 * <li>builds Dcp packages 
	 * <li>sets up iterator
	 * </ul>
	 * @throws QueryServiceException
	 */
	private void setup() 
	{
		executeQuery();
		executeMapping();
		keyIterator = resultMap.keySet().iterator();
	}

	/** 
	 * Create a file to which query result would be temporarily stored.
	 * @return File handle.
	 */
	private File createStagingFile()
	{
		File stagingFile;
		try
		{
			if(this.stagingDirectory != null)
			{
				File stagingDir = new File(this.stagingDirectory);
				stagingFile = File.createTempFile(Integer.toString(stagingFileNamePrefix), stagingFileNameSuffix, stagingDir);
			}
			else
			{
				stagingFile = File.createTempFile(Integer.toString(stagingFileNamePrefix), stagingFileNameSuffix);
			}
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		return stagingFile;
	}

	/**
	 * Execute query. Write resulting IDs of DeliveryableUnits into staging file at 
	 */
	private void executeQuery()
	{
		long totalMatches = 0;
		long fetchedMatches = 0;
		BufferedWriter writer = null;
		List<QueryMatch<DcsEntity>> resultList = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(stagingFile));	
			do
			{
				QueryResult<DcsEntity> queryResult = queryService.query(queryString, fetchedMatches, -1);
				totalMatches = queryResult.getTotal();
				resultList = queryResult.getMatches();
				fetchedMatches = fetchedMatches + resultList.size();
				for(QueryMatch<DcsEntity> match : resultList)
				{
					if( match.getObject() instanceof DcsDeliverableUnit)
					{
						writer.write(match.getObject().getId());
						writer.newLine();
					}
				}
			} while ( fetchedMatches < totalMatches );
		}
		catch(QueryServiceException e)
		{
			throw new RuntimeException("Exception occured when executing the query" , e);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Exception occured when accessing/writing to staged file", e);
		}
		finally
		{
			try
			{
				if (writer != null) 
				{
					writer.flush();
					writer.close();
				}
			}
			catch(IOException e)
			{
				throw new RuntimeException("Exception occurred when closing staged file", e);
			}
		}
	}
	
	/**
	 * Execute the mapping from DU ids to fully formed Dcps containing the DU.
	 */
	private void executeMapping()
	{
		if(stagingFile != null)
		{
			String duID;
			DcpOutput output = new DcpOutput();
		
			try
			{
				BufferedReader reader  = new BufferedReader(new FileReader(stagingFile));
				
				while ((duID = reader.readLine())!=null)
				{
					dcpBuilder.map(duID, duID, output);
					if(!output.isValueEmpty())
					{
						resultMap.put(duID, output.getValue());
					}
				}
			}
			catch(IOException e)
			{
				throw new RuntimeException("Exception occurred when accessing/reading staged file" , e);
			}
		}
				
	}


}
