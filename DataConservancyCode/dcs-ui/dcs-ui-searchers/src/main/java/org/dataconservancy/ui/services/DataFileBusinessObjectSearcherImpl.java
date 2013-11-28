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
package org.dataconservancy.ui.services;

import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.ui.profile.DataFileProfile;

/**
 *
 */
public class DataFileBusinessObjectSearcherImpl extends FileBusinessObjectSearcherImpl
        implements DataFileBusinessObjectSearcher {

    public DataFileBusinessObjectSearcherImpl(DcsConnector connector, IdService idService) {
        super(connector, idService);
    }

    @Override
    public DcsFile findDataFile(String business_id) {
        return findFile(business_id, Types.DATA_FILE.name());
    }

    @Override
    protected String getStateDuType() {
        return DataFileProfile.DATAFILE_STATEDU_TYPE;
    }
}
