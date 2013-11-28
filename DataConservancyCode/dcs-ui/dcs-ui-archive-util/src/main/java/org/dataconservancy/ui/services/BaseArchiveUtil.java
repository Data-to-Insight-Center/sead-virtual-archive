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

import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Base implementation of {@link ArchiveUtil}.
 */
public abstract class BaseArchiveUtil implements ArchiveUtil {

    final Logger log = LoggerFactory.getLogger(BaseArchiveUtil.class);

    @Override
    public DcsDeliverableUnit determineDepositRoot(Set<DcsDeliverableUnit> candidates) {

        // Root du does not have another du in this list as a parent
        for (DcsDeliverableUnit candidate : candidates) {
            boolean has_local_parent = false;

            for (DcsDeliverableUnitRef parent : candidate.getParents()) {
                String parentId = parent.getRef();

                for (DcsDeliverableUnit parentCandidate : candidates) {
                    if (parentId.equals(parentCandidate.getId())) {
                        has_local_parent = true;
                        break;
                    }
                }
            }

            if (!has_local_parent) {
                log.debug("Root deposit DU is: {}", candidate.getId());
                return candidate;
            }
        }

        log.debug("Unable to find root deposit DU");
        return null;
    }
}
