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
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;

/**
 * This class provides support for searching the archive for content who SIPs
 * were constructed by {@link org.dataconservancy.ui.dcpmap.CollectionMapper} or {@link org.dataconservancy.ui.dcpmap.DataSetMapper}.
 * <p/>
 * This abstraction is incomplete, because implementations must accommodate different
 * archival mappings; the way a {@link Collection collection's} state is represented in
 * the archive is different from the way a {@link DataItem data item's} state is
 * represented in the archive.  This leads to implementations which have to accommodate
 * multiple mappings.  In the future this interface should be tied to a profile.
 */
public interface BusinessObjectSearcher {

    /**
     * Return the {@link BusinessObjectState} for the latest version of the
     * {@link DataItem} or {@link Collection} added to the archive.  This
     * may be {@code null} if the object identified by {@code business_id} has
     * not yet been deposited into the archive.
     *
     * @param business_id
     * @return Corresponding {@link DcsDeliverableUnit} or null if not found.
     */
    BusinessObjectState findLatestState(String business_id);

}
