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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.DcpUtil;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.dcpsolr.AbstractSearchTest;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.transform.dcp.solr.QueryDuReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QueryDuReaderTest extends AbstractSearchTest {

	private QueryDuReader reader;
	private Dcp dcp;

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testHierarchicalDcp() throws IndexServiceException,
			AIPFormatException {
		DcsCollection top = rb.createCollection(null);
		top.setTitle("top");

		DcsCollection col = rb.createCollection(top.getId());
		col.setTitle("col");

		DcsDeliverableUnit dutop1 = rb.createDeliverableUnit(col.getId(), null,
				false);
		dutop1.setTitle("dutop1");

		DcsDeliverableUnit du1 = rb.createDeliverableUnit(top.getId(), null,
				false);
		du1.setTitle("du1");

		du1.addParent(new DcsDeliverableUnitRef(dutop1.getId()));

		DcsDeliverableUnit dutop2 = rb.createDeliverableUnit(col.getId(), null,
				false);
		dutop1.setTitle("dutop2");

		DcsDeliverableUnit du2 = rb.createDeliverableUnit(top.getId(), null,
				false);
		du2.setTitle("du2");

		du2.addParent(new DcsDeliverableUnitRef(dutop2.getId()));

		List<DcsFile> files = new ArrayList<DcsFile>();
		DcsManifestation man = rb.createManifestation(dutop1.getId(), files);

		DcsManifestationFile manfile = new DcsManifestationFile();
		manfile.setPath("moo");
		DcsFile file = rb.createFile();

		manfile.setRef(new DcsFileRef(file.getId()));
		man.addManifestationFile(manfile);

		DcsFile mdfile = rb.createFile();

		DcsEvent ev = rb.createEvent(null);
		ev.setOutcome("outcome");
		ev.setEventType("ingest");
		ev.addTargets(new DcsEntityReference(file.getId()));
		ev.addTargets(new DcsEntityReference(col.getId()));
		ev.addTargets(new DcsEntityReference(dutop1.getId()));
		ev.addTargets(new DcsEntityReference(man.getId()));

		DcsEvent ev2 = rb.createEvent(null);
		ev2.setOutcome("outcome2");
		ev2.setEventType("ingest2");
		ev2.addTargets(new DcsEntityReference(file.getId()));
		ev2.addTargets(new DcsEntityReference(col.getId()));
		ev2.addTargets(new DcsEntityReference(dutop1.getId()));
		ev2.addTargets(new DcsEntityReference(man.getId()));

		Dcp currentDcp = DcpUtil.add(null, dutop1, top, col, du1, mdfile, man,
				ev, ev2, file);
		DcpUtil.add(currentDcp, files);

		index(currentDcp);
		Dcp resultDcp;

		if (query_service != null) {
			reader = new QueryDuReader(query_service,
					SolrQueryUtil.createLiteralQuery(
                            DcsSolrField.EntityField.ID.solrName(),
                            dutop1.getId()));

			// Test number of results
			assertEquals(reader.getNumberOfResults(), 1);

			// Test that the next result can be fetched
			assertTrue(reader.nextKeyValue());

			resultDcp = reader.getCurrentValue();

			// Test that the result contains the correct
			assertEquals(resultDcp.getDeliverableUnits().toArray()[0], dutop1);

			// Test that resulted manifestation was contained in the origincal
			// Dcp
			for (DcsManifestation resultedManifestation : resultDcp
					.getManifestations()) {
				assertTrue(currentDcp.getManifestations().contains(
						resultedManifestation));
			}
			// Test that resulted manifestations list contains expected
			// manifestation
			assertTrue(resultDcp.getManifestations().contains(man));

			for (DcsFile resultedFile : resultDcp.getFiles()) {
				assertTrue(currentDcp.getFiles().contains(resultedFile));
			}

			for (DcsEvent resultedEvent : resultDcp.getEvents()) {
				assertTrue(currentDcp.getEvents().contains(resultedEvent));
			}

			assertTrue(resultDcp.getEvents().contains(ev));
			assertTrue(resultDcp.getEvents().contains(ev2));

			// Test that no other dcp is returned
			assertFalse(reader.nextKeyValue());

			reader.close();
			reader = new QueryDuReader(query_service,
					SolrQueryUtil.createLiteralQuery(
							DcsSolrField.EntityField.ID.solrName(),
							file.getId()));
			assertFalse(reader.nextKeyValue());
			reader.close();
		}

	}

	@Test
	public void testRandomDcp() throws IndexServiceException,
			AIPFormatException {
		dcp = rb.createDcp(5, 5);

		index(dcp);

		reader = new QueryDuReader(query_service, "entityType:DeliverableUnit");

		assertEquals(dcp.getDeliverableUnits().size(),
				reader.getNumberOfResults());

		Dcp currentDcp;
		while (reader.nextKeyValue()) {
			currentDcp = reader.getCurrentValue();
			for (DcsDeliverableUnit du : currentDcp.getDeliverableUnits()) {
				assertTrue(dcp.getDeliverableUnits().contains(du));
			}
			for (DcsManifestation mani : currentDcp.getManifestations()) {
				assertTrue(dcp.getManifestations().contains(mani));
			}
			for (DcsFile file : currentDcp.getFiles()) {
				assertTrue(dcp.getFiles().contains(file));
			}
			for (DcsEvent event : currentDcp.getEvents()) {
				assertTrue(dcp.getEvents().contains(event));
			}
		}
		reader.close();
	}

	@After
	public void tearDown() throws IOException {
		super.tearDown();
	}
}
