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

import org.dataconservancy.model.dcs.DcsFile;

/**
 * An abstraction to support searching operations fo r{@link org.dataconservancy.ui.model.DataItem} business objects.
 */
public interface DataItemBusinessObjectSearcher extends BusinessObjectSearcher {

    /**
     * Return the {@link org.dataconservancy.model.dcs.DcsFile} for the {@link org.dataconservancy.ui.model.File} with the given business
     * id which was added to the archive as part of an {@link org.dataconservancy.ui.model.DataItem}.
     *
     * @param business_id
     * @return Corresponding {@link org.dataconservancy.model.dcs.DcsFile} or null if not found.
     */
    DcsFile findDataSetFile(String business_id);

}
