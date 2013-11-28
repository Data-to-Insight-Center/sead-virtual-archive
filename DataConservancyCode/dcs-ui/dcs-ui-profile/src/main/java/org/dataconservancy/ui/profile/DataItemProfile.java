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
package org.dataconservancy.ui.profile;

import org.dataconservancy.profile.support.MatchOp;
import org.dataconservancy.profile.support.ProfileStatement;
import org.dataconservancy.ui.model.DataItem;

/**
 * Maintains constants and {@link ProfileStatement}s that are used when producing and consuming DCP serializations of
 * {@link DataItem}s.
 */
public class DataItemProfile implements Profile<DataItem> {

    /**

     * This is the value of the &lt;DeliverableUnit/&gt; &lt;type/&gt; element for Deliverable Units representing
     * the root of a {@link DataItem}.
     * <p/>
     * This mapper will produce and consume Deliverable Units with this <type>.  This
     * type, along with the {@link #DATASET_MAPPER_VERSION} will be used to select the
     * appropriate mapping logic.
     */
    public static final String DATASET_TYPE = "org.dataconservancy:types:DataItem";

    /**
     * This is the version of this mapper.  In the future this may migrate from a
     * static field to something more extensible.  It is anticipated that as the
     * UI domain model evolves, mappers will need to evolve as well.  The version
     * number would increment to indicate changes in the mapping logic.
     */
    public static final String DATASET_MAPPER_VERSION = "1.0";

    /**
     * This is the value of the &lt;DeliverableUnit/&gt; &lt;type/&gt; element for Deliverable Units representing
     * the state of a {@link DataItem}.
     */
    public static final String DATASET_STATE_TYPE = DATASET_TYPE + ":" + "DataItemState";

    /**
     * This is the String value of the &lt;Manifestation>/&lt;technicalEnvironment> that contains the actual files
     * in the DataItem.  Use {@link #DATA_SET_FILES_TECH_ENV} {@code ProfileStatement} to evaluate candidate
     * Manifestations' technical environment to determine whether or not the Manifestation contains files for a
     * DataItem.
     */
    public static final String DATASET_FILES_TECHENV =
            DataItemProfile.DATASET_TYPE + ":" + DataItemProfile.DATASET_MAPPER_VERSION + ":DataSetFiles";

    /**
     * The ProfileStatement used to determine whether or not a Manifestation contains files for a DataItem.  Example
     * usage:
     * <pre>
     *     Set&lt;String> techEnv = candidateManifestation.getTechnicalEnvironment();
     *     if (DataItemProfile.DATA_SET_FILES_TECH_ENV.evaluate(techEnv, CollectionMatchStrategy.AT_LEAST_ONE)) {
     *         // then you have a Manifestation that contains DataItem files.
     *     }
     * </pre>
     */
    public static final ProfileStatement DATA_SET_FILES_TECH_ENV =
            new ProfileStatement(MatchOp.EQUAL_TO, DATASET_FILES_TECHENV);

    /**
     * This is the String value of the &lt;Manifestation>/&lt;technicalEnvironment> that contains the metadata
     * describing business properties of the DataItem.  Use {@link #DATA_SET_METADATA_TECH_ENV} {@code ProfileStatement}
     * to evaluate candidate Manifestations' technical environment to determine whether or not the Manifestation
     * contains metadata for a DataItem.
     */
    public static final String DATASET_METADATA_TECHENV =
            DataItemProfile.DATASET_TYPE + ":" + DataItemProfile.DATASET_MAPPER_VERSION + ":DataSetMetadata";

    /**
     * The ProfileStatement used to determine whether or not a Manifestation contains metadata for a DataItem.  Example
     * usage:
     * <pre>
     *     Set&lt;String> techEnv = candidateManifestation.getTechnicalEnvironment();
     *     if (DataItemProfile.DATA_SET_METADATA_TECH_ENV.evaluate(techEnv, CollectionMatchStrategy.AT_LEAST_ONE)) {
     *         // then you have a Manifestation that contains DataItem metadata.
     *     }
     * </pre>
     */
    public static final ProfileStatement DATA_SET_METADATA_TECH_ENV =
            new ProfileStatement(MatchOp.EQUAL_TO, DATASET_METADATA_TECHENV);


    @Override
    public String getType() {
        return DATASET_TYPE;
    }

    @Override
    public String getVersion() {
        return DATASET_MAPPER_VERSION;
    }

    @Override
    public String toString() {
        return "Data Item Profile version " + DATASET_MAPPER_VERSION + ", type " + DATASET_TYPE;
    }
}
