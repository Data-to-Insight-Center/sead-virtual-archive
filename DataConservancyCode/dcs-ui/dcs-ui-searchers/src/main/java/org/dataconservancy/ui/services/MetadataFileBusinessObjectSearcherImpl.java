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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.ui.dcpmap.AbstractVersioningMapper;
import org.dataconservancy.ui.model.Id;
import org.dataconservancy.ui.profile.DataItemProfile;
import org.dataconservancy.ui.profile.MetadataFileProfile;
import org.dataconservancy.ui.util.SolrQueryUtil;

/**
 * An implementation of {@link MetadataFileBusinessObjectSearcher}.
 */
public class MetadataFileBusinessObjectSearcherImpl extends FileBusinessObjectSearcherImpl
        implements MetadataFileBusinessObjectSearcher {

    public MetadataFileBusinessObjectSearcherImpl(DcsConnector connector, IdService idService) {
        super(connector, idService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DcsFile findMetadataFile(String business_id) {
        return findFile(business_id, Types.METADATA_FILE.name());
    }

    @Override
    protected String getStateDuType() {
        return MetadataFileProfile.STATE_DU_TYPE;
    }
}
