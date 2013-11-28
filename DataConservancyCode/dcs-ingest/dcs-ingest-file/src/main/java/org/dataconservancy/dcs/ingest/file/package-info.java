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

/** Utilities for handling deposited files in ingest processes.
 * <p>
 * The basic lifeycle of an uploaded file is as follows:
 * <ol>
 *  <li>File content is deposited to 
 *  {@link org.dataconservancy.dcs.ingest.file.StagedFileUploadManager}
 *   <ul>
 *    <li>content and metadata is sent to 
 *    {@link org.dataconservancy.dcs.ingest.FileContentStager}
 *    for storage and accessibility during ingest.</li>
 *    <li>A {@link org.dataconservancy.model.dcp.Dcp} SIP is created for
 *    the file deposit (the "file deposit SIP"), and sent to the 
 *    {@link org.dataconservancy.dcs.ingest.SipStager}
 *     <ul>
 *      <li>Contains a File entity with src = 
 *      {@link org.dataconservancy.dcs.ingest.StagedFile#getReferenceURI()}</li>
 *     </ul>
 *    </li>
 *    <li>A {@link org.dataconservancy.dcs.ingest.Events#DEPOSIT} event is 
 *    associated with the file deposit SIP via the
 *    {@link org.dataconservancy.dcs.ingest.EventManager}
 *    <ul>
 *     <li>The <code>eventOutcome</code> is the identity of the file deposit SIP,
 *     as per {@link org.dataconservancy.dcs.ingest.SipStager#addSIP(org.dataconservancy.model.dcp.Dcp)}</li>
 *    </ul>
 *    </li>
 *   </ul>
 *  </li>
 *  <li>Status of uploaded file is optionally polled</li>
 *  <li>File content is associated with a deposited SIP</li>
 * </ol>
 * </p>
 */
package org.dataconservancy.dcs.ingest.file;