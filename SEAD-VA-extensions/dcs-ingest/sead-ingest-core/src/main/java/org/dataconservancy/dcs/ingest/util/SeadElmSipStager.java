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
package org.dataconservancy.dcs.ingest.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.impl.elm.EntityStore;
import org.dataconservancy.archive.impl.elm.Metadata;
import org.dataconservancy.archive.impl.elm.MetadataStore;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.seadva.ingest.SeadSipStager;
import org.seadva.model.builder.api.SeadModelBuilder;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class SeadElmSipStager
        implements SipStager {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final String SIP_TYPE = "sip";

    private IdService idService;

    private EntityStore entityStore;

    private MetadataStore metadataStore;

    private SeadXstreamStaxModelBuilder modelBuilder;

    private boolean finishDeletes = true;

    @Required
    public void setIdentifierService(IdService id) {
        idService = id;
    }

    @Required
    public void setModelBuilder(SeadXstreamStaxModelBuilder mb) {
        modelBuilder = mb;
    }

    @Required
    public void setEntityStore(EntityStore es) {
        entityStore = es;
    }

    @Required
    public void setMetadataStore(MetadataStore ms) {
        metadataStore = ms;
    }

    public void setDeleteUponRetire(boolean delete) {
        finishDeletes = delete;
    }

    public String addSIP(ResearchObject sip) {
        String id = idService.create("sip").getUid();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        modelBuilder.buildSip(sip, out);
        entityStore.put(id, new ByteArrayInputStream(out.toByteArray()));
        metadataStore.add(id, SIP_TYPE, null);
        return id;
    }

    public Set<String> getKeys() {
        Set<String> keys = new HashSet<String>();
        for (Metadata md : metadataStore.getAll(SIP_TYPE)) {
            keys.add(md.getId());
        }
        return keys;
    }

    @Override
    public void updateSIP(Dcp sip, String id) {
        synchronized (id.intern()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            modelBuilder.buildSip((ResearchObject)sip, out);
        }
    }

    @Override
    public String addSIP(Dcp sip) {
        String id = idService.create("sip").getUid();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        modelBuilder.buildSip((ResearchObject)sip, out);
        entityStore.put(id, new ByteArrayInputStream(out.toByteArray()));
        metadataStore.add(id, SIP_TYPE, null);
        return id;
    }

    public ResearchObject getSIP(String key) {
        synchronized (key.intern()) {

            InputStream stream = null;
            try {
                stream = entityStore.get(key);
            } catch (EntityNotFoundException e) {
                return null;
            }

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final TeeInputStream tee = new TeeInputStream(stream, out);
            try {
                return (ResearchObject)modelBuilder.buildSip(tee);
            } catch (InvalidXmlException e) {
                try {
                    log.error("Invalid XML in the SIP with key [{}]: {}\n{}\n",
                              new Object[] {
                                      key,
                                      e.getMessage(),
                                      IOUtils.toString(new ByteArrayInputStream(out
                                              .toByteArray()))});
                } catch (IOException ioe) {

                }
                throw new RuntimeException(e);
            } catch (RuntimeException e) {
                try {
                    log.error("Error reading SIP with key [{}]: {}\n{}\n",
                              new Object[] {
                                      key,
                                      e.getMessage(),
                                      IOUtils.toString(new ByteArrayInputStream(out
                                              .toByteArray()))});
                } catch (IOException ioe) {

                }
                throw new RuntimeException(e);
            } finally {
                try {
                    out.close();
                    tee.close();
                    stream.close();
                } catch (IOException e) {

                }
            }
        }
    }

    public void removeSIP(String key) {
        synchronized (key.intern()) {
            entityStore.remove(key);
            metadataStore.remove(key);
        }
    }

    public void retire(String key) {
        if (finishDeletes) {
            removeSIP(key);
        }
    }

    public void updateSIP(ResearchObject sip, String key) {
        synchronized (key.intern()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            modelBuilder.buildSip(sip, out);
            entityStore.put(key, new ByteArrayInputStream(out.toByteArray()));
        }
    }

}
