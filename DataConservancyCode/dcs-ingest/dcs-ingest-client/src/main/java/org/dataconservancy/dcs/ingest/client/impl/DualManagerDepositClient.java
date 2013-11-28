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
package org.dataconservancy.dcs.ingest.client.impl;

import java.io.ByteArrayInputStream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.dcs.ingest.client.DepositBuilder;
import org.dataconservancy.dcs.ingest.client.DepositClient;
import org.dataconservancy.dcs.util.HttpHeaderUtil;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.deposit.PackageException;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.springframework.beans.factory.annotation.Required;

/**
 * Creates deposits using separate file and sip upload.
 * <p>
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setDigestAlgorithm(String)}
 * <dd><b>Required</b>.</dd>
 * <dt>{@link #setFileDepositManager(DepositManager)}</dt>
 * <dd>b>Required</b>.</dd>
 * <dt>{@link #setModelBuilder(DcsModelBuilder)}</dt>
 * <dd>b>Required</b>.</dd>
 * <dt>{@link #setSipDepositManager(DepositManager)}</dt>
 * <dd>b>Required</b>.</dd>
 * </dl>
 * </p>
 */
public class DualManagerDepositClient
        implements DepositClient {

    private DepositManager fileDepositManager;

    private DepositManager sipDepositManager;

    private DcsModelBuilder modelBuilder;

    private String algorithm;

    @Required
    public void setFileDepositManager(DepositManager mgr) {
        fileDepositManager = mgr;
    }

    @Required
    public void setSipDepositManager(DepositManager mgr) {
        sipDepositManager = mgr;
    }

    @Required
    public void setModelBuilder(DcsModelBuilder builder) {
        modelBuilder = builder;
    }

    @Required
    public void setDigestAlgorithm(String algo) {
        algorithm = algo;
    }

    public DepositBuilder buildDeposit(Dcp... template) {
        return new DualManagerDeposit(copy(template),
                                      modelBuilder,
                                      sipDepositManager,
                                      fileDepositManager,
                                      algorithm);
    }

    public String doDeposit(byte[] sipContent) {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put(HttpHeaderUtil.CONTENT_TYPE, "application/xml");
        metadata.put(HttpHeaderUtil.CONTENT_LENGTH, Integer
                .toString(sipContent.length));

        try {
            return sipDepositManager
                    .deposit(new ByteArrayInputStream(sipContent),
                             "application/xml",
                             "http://dataconservancy.org/schemas/dcp/1.0",
                             metadata).getDepositID();
        } catch (PackageException e) {
            throw new RuntimeException(e);
        }
    }

    public DepositInfo getDepositInfo(String depositId) {
        return sipDepositManager.getDepositInfo(depositId);
    }

    private Dcp copy(Dcp... templates) {
        Dcp dup = new Dcp();
        Set<DcsCollection> collections = new HashSet<DcsCollection>();
        Set<DcsDeliverableUnit> deliverableUnits =
                new HashSet<DcsDeliverableUnit>();
        Set<DcsFile> files = new HashSet<DcsFile>();
        Set<DcsManifestation> manifestations = new HashSet<DcsManifestation>();
        Set<DcsEvent> events = new HashSet<DcsEvent>();

        for (Dcp dcp : templates) {
            collections.addAll(dcp.getCollections());
            deliverableUnits.addAll(dcp.getDeliverableUnits());
            files.addAll(dcp.getFiles());
            manifestations.addAll(dcp.getManifestations());
            events.addAll(dcp.getEvents());
        }

        dup.setCollections(collections);
        dup.setDeliverableUnits(deliverableUnits);
        dup.setEvents(events);
        dup.setFiles(files);
        dup.setManifestations(manifestations);
        return dup;
    }
}
