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

import java.util.List;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;

/**
 * Builds a full Dcp given the identity of a DU.
 * <p>
 * Will produce a Dcp containing the specified DelivarableUnit entity, plus all
 * of its non-DU descendants. Exactly how the builder determines the list of
 * descendants and fetches the corresponding Dcs entities depends on the
 * implementing class.
 * </p>
 */
public class DcpBuilder implements Mapping<Object, String, String, Dcp> {

	private DcsDataModelQueryService queryService;

	public DcpBuilder(DcsDataModelQueryService service) {
		super();
		this.queryService = service;
	}

	/**
	 * Creates an output Dcp containing a DU plus all descendants.
	 * <p>
	 * Given an identifier for a DeliverableUnit, will fetch the corresponding
	 * DeliverableUnit entity, plus all of its descendants.
	 * </p>
	 * 
	 * @param key
	 *            Arbitrary. May be ignored.
	 * @param val
	 *            DeliverableUnit identifier
	 * @param output
	 *            The output to the writer will contain the DU identifier as a
	 *            key, and the complete Dcp as the value.
	 */
	public void map(Object key, String val, Output<String, Dcp> output) {
		// Coded this way to provide an easy switch to another mapping
		// implementation
		if (queryService != null) {
			mapByQueryService(key, val, output);
		}
	}

	private void mapByQueryService(Object key, String val,
			Output<String, Dcp> output) {
		// rename val for easier to understand code
		String duID = val;

		QueryResult<DcsEntity> queryResult = null;
		Dcp finishedDcp = new Dcp();
		List<QueryMatch<DcsEntity>> resultList;
		DcsEntity resultEntity;

		String query = SolrQueryUtil.createLiteralQuery(
				DcsSolrField.EntityField.ID.solrName(), duID);
		try {
			queryResult = queryService.query(query, 0, -1);
			finishedDcp.addDeliverableUnit((DcsDeliverableUnit) queryResult
					.getMatches().get(0).getObject());
		} catch (QueryServiceException e) {
			throw new RuntimeException("Exception occurred when execute query",
					e);
		}

		long totalMatches = 0;
		long fetchedMatches = 0;

		query = SolrQueryUtil.createLiteralQuery("OR",
                DcsSolrField.EntityField.ANCESTRY.solrName(), duID,
                DcsSolrField.EventField.TARGET.solrName(), duID);
		try {
			do {
				queryResult = queryService.query(query, fetchedMatches, -1);
				totalMatches = queryResult.getTotal();
				fetchedMatches = fetchedMatches
						+ queryResult.getMatches().size();
				resultList = queryResult.getMatches();

				for (QueryMatch<DcsEntity> queryMatch : resultList) {
					resultEntity = queryMatch.getObject();
					if (resultEntity instanceof DcsManifestation) {
						finishedDcp
								.addManifestation((DcsManifestation) resultEntity);
					} else if (resultEntity instanceof DcsFile) {
						finishedDcp.addFile((DcsFile) resultEntity);
					}

					else if (resultEntity instanceof DcsEvent) {
						finishedDcp.addEvent((DcsEvent) resultEntity);
					}
				}
			} while (fetchedMatches < totalMatches);
		} catch (QueryServiceException e) {
			throw new RuntimeException(e);
		}

		output.write("", finishedDcp);
	}

}
