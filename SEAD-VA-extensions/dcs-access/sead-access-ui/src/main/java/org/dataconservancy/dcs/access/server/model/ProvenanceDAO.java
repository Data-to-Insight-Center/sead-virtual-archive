/*
 * Copyright 2013 The Trustees of Indiana University
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

package org.dataconservancy.dcs.access.server.model;

import org.dataconservancy.dcs.access.shared.ProvenaceDataset;import org.dataconservancy.dcs.access.shared.ProvenanceRecord;

import java.util.Date;
import java.util.List;

public interface ProvenanceDAO {

	public void insertProvenanceRecord(ProvenanceRecord provenance);

    public List<ProvenaceDataset> getProvenanceForSubmitter(String submitterId);

	List<ProvenaceDataset> getProvForSubmitterWf(String submitterId,
			String wfInstanceId, Date latestDate);
}
