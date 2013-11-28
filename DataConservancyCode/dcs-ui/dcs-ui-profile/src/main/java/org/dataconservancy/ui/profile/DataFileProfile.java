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
package org.dataconservancy.ui.profile;

import org.dataconservancy.profile.support.MatchOp;
import org.dataconservancy.profile.support.ProfileStatement;
import org.dataconservancy.ui.model.DataFile;

/**
 *
 */
public class DataFileProfile implements Profile<DataFile> {

    /**
     * <em>Must</em> sync with AbstractVersioningMapper#ROOT_DELIVERABLE_UNIT_TYPE.  Can't reference it due to
     * dependency tree issues.
     */
    public static final String DATAFILE_ROOTDU_TYPE = "root";

    /**
     * <em>Must</em> sync with AbstractVersioningMapper#STATE_DELIVERABLE_UNIT_TYPE.  Can't reference it due to
     * dependency tree issues.
     */
    public static final String DATAFILE_STATEDU_TYPE = "datafile_state";

    public static final String DATAFILE_MANIFESTATION_TYPE = "DataFile";

    public static final String DATAFILE_TECHENV =
            DataItemProfile.DATASET_TYPE + ":" + DataItemProfile.DATASET_MAPPER_VERSION + ":DataSetFiles";

    /**
     * Profile statement which evaluates the technical environment of Manifestations for Data Item Data Files.
     * Can also be injected.
     */
    public static final ProfileStatement DATA_SET_FILES_TECH_ENV = new ProfileStatement(MatchOp.EQUAL_TO, DATAFILE_TECHENV);

    @Override
    public String getType() {
        // Default method body
        return null;
    }

    @Override
    public String getVersion() {
        // Default method body
        return null;
    }
}
