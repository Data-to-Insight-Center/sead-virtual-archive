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
package org.dataconservancy.dcs.ingest;

/**
 * Enumeration of core ingest-relevant event types.
 * <p>
 * These events can be considered common to the ingest process, and ought to be
 * understood by those that need to reason about a general ingest process.
 * </p>
 * <p>
 * In addition to anything specified in individual javadoc, ingest events SHOULD
 * have an <code>eventDetail</code> that provides a human-readable description
 * of the event. These can be arbitrarily mundane.
 * </p>
 */
public interface Events {

    /**
     * Archive has stored the entities within a SIP.
     * <p>
     * Indicates that a SIP has been sent to the archive. May represent an add,
     * or an update.
     * </p>
     * <p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventOutcome</dt>
     * <dd>Number of entities archived</dd>
     * <dt>eventTarget</dt>
     * <dd>every archived entity</dd>
     * </dl>
     * </p>
     */
    public static final String ARCHIVE = "archive";

    /**
     * Signifies that an entity has been identified as a member of a specific
     * batch load process.
     * <p>
     * There may be an arbitrary number of independent events signifying the
     * same batch (same outcome, date, but different sets of targets). A unique
     * combination of date and outcome (batch label) identify a batch.
     * </p>
     * <p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventOutcome</dt>
     * <dd>Batch label/identifier</dd>
     * <dt>eventTarget</dt>
     * <dd>Entities in a batch</dd>
     * </dl>
     * </p>
     */
    public static final String BATCH = "batch";

    /**
     * File format characterization.
     * <p>
     * Indicates that a format has been verifiably characterized. Format
     * characterizations not accompanied by a corresponding characterization
     * event can be considered to be unverified.
     * </p>
     * <p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventOutcome</dt>
     * <dd>format, in the form "scheme formatId" (whitespace separated)</dd>
     * <dt>eventTarget</dt>
     * <dd>id of characterized file</dd>
     * </dl>
     * </p>
     */
    public static final String CHARACTERIZATION_FORMAT =
            "characterization.format";

    /**
     * Advanced file characterization and/or metadata extraction.
     * <p>
     * Indicates that some sort of characterization or extraction has produced a
     * document containing file metadata.
     * </p>
     * *
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventOutcome</dt>
     * <dd>id of File containing metadata</dd>
     * <dt>eventTarget</dt>
     * <dd>id of File the metadata describes</dd>
     * </dl>
     */
    public static final String CHARACTERIZATION_METADATA =
            "characterization.metadata";

    /**
     * Initial deposit/transfer of an item into the DCS, preceding ingest.
     * <p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventOutcome</dt>
     * <dd>SIP identifier uid</dd>
     * <dt>eventTarget</dt>
     * <dd>id of deposited entity</dd>
     * </dl>
     * </p>
     */
    public static final String DEPOSIT = "deposit";

    /**
     * Content retrieved by dcs.
     * <p>
     * Represents the fact that content has been downloaded/retrieved by the
     * dcs.
     * </p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventOutcome</dt>
     * <dd>http header-like key/value pairs representing circumstances
     * surrounding upload</dd>
     * <dt>eventTarget</dt>
     * <dd>id of File whose staged content has been downloaded</dd>
     * </dl>
     */
    public static final String FILE_DOWNLOAD = "file.download";

    /**
     * uploaaded/downloaded file content resolution.
     * <p>
     * Indicates that the reference URI to a unit of uploaded or downloaded file
     * content has been resolved and replaced with the DCS file access URI.
     * </p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventOutcome</dt>
     * <dd><code>reference_URI</code> 'to' <code>dcs_URI</code></dd>
     * <dt>eventTarget</dt>
     * <dd>id of File whose staged content has been resolved</dd>
     * </dl>
     */
    public static final String FILE_RESOLUTION_STAGED = "file.resolution";

    /**
     * Indicates the uploading of file content.
     * <p>
     * Represents the physical receipt of bytes from a client.
     * </p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventOutcome</dt>
     * <dd>http header-like key/value pairs representing circumstanced
     * surrounding upload</dd>
     * <dt>eventTarget</dt>
     * <dd>id of File whose staged content has been uploaded</dd>
     * </dl>
     */
    public static final String FILE_UPLOAD = "file.upload";

    /**
     * Fixity computation/validation for a particular File.
     * <p>
     * Indicates that a particular digest has been computed for given file
     * content. Digest values not accompanied by a corresponding event may be
     * considered to be un-verified.
     * </p>
     * <p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventOutcome</dt>
     * <dd>computed digest value of the form "alorithm value" (whitepsace
     * separated)</dd>
     * <dt>eventTarget</dt>
     * <dd>id of digested file</dd>
     * </dl>
     * </p>
     */
    public static final String FIXITY_DIGEST = "fixity.digest";

    /**
     * Assignment of an identifier to the given entity, replacing an
     * existing/temporary id. *
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventOutcome</dt>
     * <dd><code>old_identifier</code> 'to' <code>new_identifier</code></dd>
     * <dt>eventTarget</dt>
     * <dd>new id of object</dd>
     * </dl>
     */
    public static final String ID_ASSIGNMENT = "identifier.assignment";

    /**
     * Marks the start of an ingest process.
     * <p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventTarget</dt>
     * <dd>id of all entities an ingest SIP</dd>
     * </dl>
     * </p>
     */
    public static final String INGEST_START = "ingest.start";

    /**
     * Signifies a successful ingest outcome.
     * <p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventTarget</dt>
     * <dd>id of all entities an ingest SIP</dd>
     * </dl>
     * </p>
     */
    public static final String INGEST_SUCCESS = "ingest.complete";

    /**
     * Signifies a failed ingest outcome.
     * <p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventTarget</dt>
     * <dd>id of all entities an ingest SIP</dd>
     * </dl>
     * </p>
     */
    public static final String INGEST_FAIL = "ingest.fail";

    /**
     * Signifies that a feature extraction or transform has successfully
     * occurred.
     * <p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventTarget</dt>
     * <dd>id of a DeliverableUnit or Collection</dd>
     * </dl>
     * </p>
     */
    public static final String TRANSFORM = "transform";

    /**
     * Signifies that a feature extraction or transform failed.
     * <p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventTarget</dt>
     * <dd>id of a DeliverableUnit or Collection</dd>
     * </dl>
     * </p>
     */
    public static final String TRANSFORM_FAIL = "transform.fail";

    /**
     * Signifies a file has been scanned by the virus scanner.
     * <p>
     * Indicates that a file has been scanned by a virus scanner. There could be
     * more than one event for a file.
     * </p>
     * <p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventTarget</dt>
     * <dd>id of file whose content was scanned</dd>
     * </dl>
     * </p>
     */
    public static final String VIRUS_SCAN = "virus.scan";

    /**
     * Signifies an new deliverable unit is being ingested as an update to the target deliverable unit.
     * <p>
     * <dl>
     * <dt>eventType</dt>
     * <dd>{@value}</dd>
     * <dt>eventTarget</dt>
     * <dd>id of the deliverable unit being updated</dd>
     * </dl>
     * </p>
     */
    public static final String DU_UPDATE = "du.update";

}
