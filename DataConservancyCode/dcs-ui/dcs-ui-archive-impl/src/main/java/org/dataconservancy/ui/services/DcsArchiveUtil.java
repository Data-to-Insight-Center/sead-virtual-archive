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
package org.dataconservancy.ui.services;

import org.dataconservancy.access.connector.CountableIterator;
import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.access.connector.DcsConnectorFault;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.ui.util.SolrQueryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for working with an instance of the DCS archive.  This implementation uses an instance of the
 * {@link DcsConnector} to communicate with the archive.
 */
class DcsArchiveUtil extends BaseArchiveUtil {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DcsConnector connector;

    DcsArchiveUtil(DcsConnector connector) {
        if (connector == null) {
            throw new IllegalArgumentException("DCS Connector must not be null.");
        }

        this.connector = connector;
    }

    @Override
    public DcsEntity getEntity(String id) {
        final String query = SolrQueryUtil.createLiteralQuery("id", id);
        try {
            CountableIterator<DcsEntity> itr = connector.search(query);
            if (itr.hasNext()) {
                return itr.next();
            }
        } catch (DcsConnectorFault dcsConnectorFault) {
            log.info("Error executing search query " + query + ": " + dcsConnectorFault.getMessage(), dcsConnectorFault);
        }
        return null;
    }
}
