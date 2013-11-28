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

package org.dcs.clients.business.impl.dcp;

import java.util.List;

import org.dataconservancy.business.client.ArchiveService.IdentifierType;
import org.dataconservancy.business.client.impl.LookupService;

/**
 * Looks up the archival identifiers of a lineage of Business Objects from the
 * DCS archive and index.
 */
public class DcpLookupService
        implements LookupService {

    @Override
    public List<String> lookup(String id, IdentifierType type, int... limit) {
        // TODO Auto-generated method stub
        return null;
    }

}
