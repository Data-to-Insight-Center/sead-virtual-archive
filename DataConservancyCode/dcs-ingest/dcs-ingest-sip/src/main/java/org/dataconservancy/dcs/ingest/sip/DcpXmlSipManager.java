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
package org.dataconservancy.dcs.ingest.sip;

import java.io.InputStream;

import java.util.Map;

import org.dataconservancy.dcs.ingest.Bootstrap;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.sip.impl.TempFileXmlStreamValidator;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.deposit.PackageException;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.springframework.beans.factory.annotation.Required;

/**
 * DepositHandler for dcp xml-formatted SIP. <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setBootstrap(Bootstrap)}</dt>
 * <dd><b>Required</b>. Specifies the ingest pipeline bootstrap class. Used for
 * initiating an ingest process.</dd>
 * <dt>{@link #setSipStager(SipStager)}</dt>
 * <dd><b>Required</b>. Specifies the SipStager for staging deposited sips
 * during the ingest process.</dd>
 * <dt>{@link #setEventManager(EventManager)}</dt>
 * <dd><b>Required</b>. Specifies the ingest event manager.</dd>
 * <dt>{@link #setManagerId(String)}</dt>
 * <dd>Optional. String which uniquely names/labels/identifies this manager
 * instance.</dd>
 * <dt>{@link #setValidating(boolean)}</dt>
 * <dt>Optional. Determines if schema validation is performed. Default is true.
 * </dl>
 */
public class DcpXmlSipManager
        implements DepositManager {

    public static final String PKG_FORMAT =
            "http://dataconservancy.org/schemas/dcp/1.0";

    public static final String DCP_SCHEMA_LOC = "/schema/dcp.xsd";

    private Bootstrap boot;

    private DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    private EventManager eventManager;

    private String managerId = getClass().getName();

    private XmlStreamValidator validator =
            new TempFileXmlStreamValidator(DCP_SCHEMA_LOC);

    private SipStager sipStager;

    private boolean validating = true;

    @Required
    public void setSipStager(SipStager stager) {
        sipStager = stager;
    }

    @Required
    public void setBootstrap(Bootstrap b) {
        boot = b;
    }

    @Required
    public void setEventManager(EventManager mgr) {
        eventManager = mgr;
    }

    public void setManagerId(String id) {
        managerId = id;
    }

    public void setValidating(boolean val) {
        validating = val;
    }

    public DepositInfo deposit(InputStream pkg,
                               String contentType,
                               String packaging,
                               Map<String, String> metadata)
            throws PackageException {
        Dcp sip = null;

        try {
            if (validating) {
                sip = builder.buildSip(validator.validating(pkg));
            } else {
                sip = builder.buildSip(pkg);
            }
        } catch (Exception e) {
            throw new PackageException("Could not parse SIP: " + e.getMessage(),
                                       e);
        }

        String sipId = sipStager.addSIP(sip);

        addIngestEvent(sip, sipId);

        boot.startIngest(sipId);

        return new SipIngestInfo(sip, eventManager, sipId, getManagerID());

    }

    public DepositInfo getDepositInfo(String id) {
        Dcp sip = sipStager.getSIP(id);
        if (sip == null) return null;
        return new SipIngestInfo(sip, eventManager, id, getManagerID());
    }

    public String getManagerID() {
        return managerId;
    }

    private String addIngestEvent(Dcp sip, String ingestId) {
        DcsEvent event = eventManager.newEvent(Events.DEPOSIT);
        event.setDetail(ingestId);

        for (DcsCollection c : sip.getCollections()) {
            event.addTargets(new DcsEntityReference(c.getId()));
        }

        for (DcsEntity du : sip.getDeliverableUnits()) {
            event.addTargets(new DcsEntityReference(du.getId()));
        }

        for (DcsManifestation m : sip.getManifestations()) {
            event.addTargets(new DcsEntityReference(m.getId()));
        }

        for (DcsFile f : sip.getFiles()) {
            event.addTargets(new DcsEntityReference(f.getId()));
        }

        for (DcsEvent e : sip.getEvents()) {
            event.addTargets(new DcsEntityReference(e.getId()));
        }

        eventManager.addEvent(ingestId, event);

        return ingestId;
    }

}
