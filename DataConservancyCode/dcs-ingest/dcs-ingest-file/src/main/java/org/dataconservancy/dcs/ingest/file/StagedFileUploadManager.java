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
package org.dataconservancy.dcs.ingest.file;

import java.io.IOException;
import java.io.InputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.FileContentStager;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.StagedFile;
import org.dataconservancy.dcs.ingest.file.impl.FileDepositInfo;
import org.dataconservancy.dcs.util.DigestListener;
import org.dataconservancy.dcs.util.DigestNotificationStream;
import org.dataconservancy.dcs.util.HttpHeaderUtil;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Accepts file deposits, places them into file staging, and creates an initial
 * deposited file SIP.
 * <p>
 * The initial SIP created by the stagedFileUploadmanager contains a file entity
 * populated with any declared metadata from http headers (such as filename and
 * digests), and any events generated during the file upload processing.
 * Specifically:
 * <ul>
 * <li>Every file upload will result a staged SIP containing exactly one File
 * entity</li>
 * <li>Every File entity will have exactly one {@link Events#DEPOSIT} event</li>
 * <li>Every provided/declared digest value will result in a fixity entry in the
 * File entity. If configured to calculate fixiy, these values will be compared
 * with computed values and an exception thrown if they mismatch</li>
 * <li>Provided/declared filename will result in a fileName entry in the File
 * entity</li>
 * <li>If configured to calculate fixity, all computed digest values will result
 * in an associated {@link Events#FIXITY_DIGEST} event, and a fixity entry in
 * the File</li>
 * <li>
 * </ul>
 * </p>
 * <h2>configuration</h2>
 * <p>
 * <dl>
 * <dt>{@link #setFileContentStager(FileContentStager)}</dt>
 * <dd><b>Required</b>. Files will be sent to the specified stager for storage
 * during ingest</dd>
 * <dt>{@link #setIdService(IdService)}</dt>
 * <dd>Required. Used for initially naming uploaded file entities.</dd>
 * <dt>{@link #setSipStager(SipStager)}</dt>
 * <dd>Required. Deposited files will result in a corresponding SIP containing a
 * file entity and associated events generated during deposit.</dd>
 * <dt>{@link #setEventManager(EventManager)}</dt>
 * <dd>Required. Used for creating and associating deposit events with the
 * uploaded file SIP</dd>
 * <dt>{@link #setAlwaysCalculateFixityFor(String...)}</dt>
 * <dd>Optional. All algorithms specified here will be computed upon deposit,
 * and compared with declared values. If provided with a deposit, the metadata
 * map will be updated as each calculated digest value. This may be exploited in
 * order to pass along computed checksums to the file stager</dd>
 * <dt>{@link setCheckFixity(String...)}</dt>
 * <dd>Optional. If not specified in
 * {@link StagedFileUploadManager#setAlwaysCalculateFixityFor(String...)},
 * algorithms listed here will be run ONLY IF the initial deposit request has a
 * value for a matching algorithm. Note: any matching algorithms that are listed
 * here and in 'calculateFixity' will be computed twice, so it is best to use
 * this only for additional "optional" fixity checks.
 * <dt>{@link #setManagerId(String)}</dt>
 * <dd>Optional. String which uniquely names/labels/identifies this manager
 * instance.</dd>
 * </dl>
 * </p>
 */
public class StagedFileUploadManager
        implements DepositManager {

    private static final Logger log =
            LoggerFactory.getLogger(StagedFileUploadManager.class);

    private FileContentStager fileStorage;

    private SipStager sipStager;

    private EventManager eventManager;

    private IdService idService;

    private String managerId = this.getClass().getName();

    private String[] calculateFixity = new String[0];

    private String[] checkFixity = new String[0];

    public void setAlwaysCalculateFixityFor(String... algorithms) {
        calculateFixity = algorithms;
    }

    public void setCheckFixityOnlyIfPresent(String... algorithms) {
        checkFixity = algorithms;
    }

    @Required
    public void setFileContentStager(FileContentStager fcs) {
        fileStorage = fcs;
    }

    FileContentStager getFileContentStater() {
        return fileStorage;
    }

    @Required
    public void setIdService(IdService ids) {
        idService = ids;
    }

    IdService getIdService() {
        return idService;
    }

    @Required
    public void setEventManager(EventManager mgr) {
        eventManager = mgr;
    }

    EventManager getEventManager() {
        return eventManager;
    }

    @Required
    public void setSipStager(SipStager stager) {
        sipStager = stager;
    }

    SipStager getSipStager() {
        return sipStager;
    }

    public void setManagerId(String id) {
        managerId = id;
    }

    public DepositInfo deposit(InputStream pkg,
                               String contentType,
                               String format,
                               Map<String, String> metadata) {

        /*
         * Create initial deposit and upload events so that they are "first". We
         * are not ready to add them yet
         */
        DcsEvent fileDepositEvent = eventManager.newEvent(Events.DEPOSIT);
        DcsEvent fileUploadEvent = eventManager.newEvent(Events.FILE_UPLOAD);

        Map<String, String> origMetadata = new HashMap<String, String>();

        if (metadata != null) {
            origMetadata.putAll(metadata);
        }

        List<DcsEvent> computedFixityEvents = new ArrayList<DcsEvent>();

        StagedFile stagedContent =
                fileStorage.add(fixityFilter(pkg,
                                             metadata,
                                             computedFixityEvents), metadata);

        /* create file package */
        Dcp filePackage = sipStager.getSIP(stagedContent.getSipRef());
        Collection<DcsFile> files = filePackage.getFiles();
        DcsFile fileEntity = files.iterator().next();

        /* populate file with supplied metadata */
        String fileName = setFileMetadata(fileEntity, contentType, metadata);

        /*
         * populate file with calculated checksums, performing any necessary
         * comparisons with declared checksums. Also set extant to true.
         */
        fileEntity.setExtant(true);
        processComputedChecksums(fileEntity, computedFixityEvents, origMetadata);

        /* Work around the Dcp defensive copy */
        filePackage.setFiles(files);
        sipStager.updateSIP(filePackage, stagedContent.getSipRef());

        /* Add file deposit and upload events */
        fileDepositEvent.setOutcome(stagedContent.getSipRef());
        fileDepositEvent.setDetail("Received uploaded file " + fileName);
        fileDepositEvent.addTargets(new DcsEntityReference(fileEntity.getId()));
        eventManager.addEvent(stagedContent.getSipRef(), fileDepositEvent);

        /* Add the file upload event */
        fileUploadEvent.setDetail("Content uploaded " + contentType + " "
                + fileName);
        fileUploadEvent.addTargets(new DcsEntityReference(fileEntity.getId()));
        eventManager.addEvent(stagedContent.getSipRef(), fileUploadEvent);

        /* Add all computed fixity events */
        for (DcsEvent fixityEvent : computedFixityEvents) {
            fixityEvent.addTargets(new DcsEntityReference(fileEntity.getId()));
            eventManager.addEvent(stagedContent.getSipRef(), fixityEvent);
        }

        FileDepositInfo fdi =
                new FileDepositInfo(filePackage,
                                    stagedContent.getSipRef(),
                                    this,
                                    eventManager);

        fdi.getMetadata().put("X-dcs-src", stagedContent.getReferenceURI());

        return fdi;
    }

    public DepositInfo getDepositInfo(String id) {

        Dcp sip = sipStager.getSIP(id);

        return new FileDepositInfo(sip, id, this, eventManager);
    }

    public String getManagerID() {
        return managerId;
    }

    /*
     * For each specified algorithm, calculate the digest and create a fixity
     * event
     */
    private InputStream fixityFilter(InputStream src,
                                     final Map<String, String> metadata,
                                     final List<DcsEvent> events) {
        /* If there are no specific fixity checks, don't do anything */

        InputStream filtered = src;
        if (calculateFixity != null) {
            for (final String algorithm : calculateFixity) {
                filtered =
                        getFilterStream(algorithm, events, filtered, metadata);
            }
        }

        if (checkFixity != null) {
            for (final String algorithm : checkFixity) {
                if (HttpHeaderUtil.getDigests(metadata).containsKey(algorithm)) {
                    filtered =
                            getFilterStream(algorithm,
                                            events,
                                            filtered,
                                            metadata);
                }
            }
        }
        return filtered;
    }

    private InputStream getFilterStream(final String algorithm,
                                        final List<DcsEvent> events,
                                        InputStream filtered,
                                        final Map<String, String> metadata) {
        try {
            return new DigestNotificationStream(filtered, MessageDigest
                    .getInstance(algorithm), new DigestListener() {

                public void notify(byte[] digestValue) throws IOException {
                    events.add(newFixityEvent(algorithm, digestValue));
                    HttpHeaderUtil.addDigest(algorithm, digestValue, metadata);
                }
            });
        } catch (NoSuchAlgorithmException e) {
            log.warn("Provider does not support algorithm %s, skipping check",
                     algorithm);
            return filtered;
        }
    }

    /* Add DcsFile mime format */
    private void setMimeType(DcsFile file, String mime) {
        if (mime != null) {
            DcsFormat fmt = new DcsFormat();
            fmt.setSchemeUri("http://www.iana.org/assignments/media-types/");
            fmt.setFormat(mime);
            file.addFormat(fmt);
        }
    }

    /*
     * Populate DcsFile core metadata with given header and content type
     * metadata
     */
    private String setFileMetadata(DcsFile fileEntity,
                                   String mime,
                                   Map<String, String> metadata) {

        String fileName = HttpHeaderUtil.getFileName(metadata);
        if (fileName != null) {
            fileEntity.setName(fileName);
        } else {
            fileName = "";
        }

        /* Prefer the explicit mime type, if given */
        setMimeType(fileEntity, mime);

        if (metadata != null) {

            /*
             * If no explicit mime type, then use the metadata value (if
             * present)
             */
            if (mime == null
                    && metadata.containsKey(HttpHeaderUtil.CONTENT_TYPE)) {
                setMimeType(fileEntity, metadata
                        .get(HttpHeaderUtil.CONTENT_TYPE));

            }

            /* Write down all checksums */
            for (Map.Entry<String, byte[]> digest : HttpHeaderUtil
                    .getDigests(metadata).entrySet()) {
                DcsFixity cksm = new DcsFixity();
                cksm.setAlgorithm(digest.getKey());
                cksm.setValue(new String(Hex.encodeHex(digest.getValue())));
                fileEntity.addFixity(cksm);

            }
        }
        return fileName;
    }

    /*
     * If a checksum has already been declared in metadata, compare it with
     * computed value. Otherwise, add its value to file and metadata.
     */
    private void processComputedChecksums(DcsFile fileEntity,
                                          Collection<DcsEvent> fixityEvents,
                                          Map<String, String> metadata) {
        Map<String, byte[]> declaredChecksums =
                HttpHeaderUtil.getDigests(metadata);
        for (DcsEvent fixityEvent : fixityEvents) {
            String[] outcome = fixityEvent.getOutcome().split(" ");

            if (!declaredChecksums.containsKey(outcome[0])) {
                /*
                 * If not declared, simply add this checksum to file and
                 * metadata, unless it has been computed previously
                 */
                DcsFixity fixity = new DcsFixity();
                fixity.setAlgorithm(outcome[0]);
                fixity.setValue(outcome[1]);
                if (!fileEntity.getFixity().contains(fixity)) {
                    fileEntity.addFixity(fixity);
                }
            } else {
                /* Compare with existing/declared value */
                String hex =
                        new String(Hex.encodeHex(declaredChecksums
                                .get(outcome[0])));
                if (!hex.equals(outcome[1])) {
                    /* TODO: Decide if we need to throw a specific esception */
                    throw new RuntimeException(String
                            .format("%s checksums don't match! calculated:%s vs declared:%s",
                                    outcome[0],
                                    outcome[1],
                                    hex));
                }
            }
        }
    }

    private DcsEvent newFixityEvent(String algorithm, byte[] value) {
        DcsEvent fixity = eventManager.newEvent(Events.FIXITY_DIGEST);
        fixity.setOutcome(algorithm + " " + new String(Hex.encodeHex(value)));
        fixity.setDetail("Calculated " + algorithm + " upon upload");
        return fixity;
    }
}
