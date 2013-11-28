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
package org.dataconservancy.archive.impl.elm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static org.dataconservancy.archive.api.EntityType.COLLECTION;
import static org.dataconservancy.archive.api.EntityType.DELIVERABLE_UNIT;
import static org.dataconservancy.archive.api.EntityType.EVENT;
import static org.dataconservancy.archive.api.EntityType.FILE;
import static org.dataconservancy.archive.api.EntityType.MANIFESTATION;

/**
 * ArchiveStore implementation based around Entity and LinkMetadata stores.
 * <p>
 * Fully implements the contract of ArchiveStore by utilizing two underlying
 * stores of data:
 * <ul>
 * <li>{@link EntityStore} Opaquely stores and retrieves blobs, keyed on entity
 * identifier.
 * <li>{@link MetadataStore} Stores simple entity metadata (id, type, possibly
 * file content source) for a given entity, and those entities that have
 * relationships pointing "in".
 * </ul>
 * Read/write entity and metadata stores can be mixed and matched as will.
 * Read-only MetadataStores may assume a particular implementation of
 * EntityStore. Given these two relatively simple stores, the ELM store is able
 * to apply algorithms ({@link DipLogic}) to produce DIPs of varying content, as
 * well as efficiently iterate through archive content filtered by type.
 * </p>
 * <h2>configuration</h2>
 * <p>
 * <dl>
 * <dt>{@link #setEntityStore(EntityStore)}</dt>
 * <dd>Required. Should contain a fully configured and initialized entity store.
 * </dd>
 * <dt>{@link #setMetadataStore(MetadataStore)}</dt>
 * <dd>Required. Should contain a fully configured and initialized metadata
 * store.</dd>
 * <dt>{@link #setDefaultPackageLogic(DipLogic)}</dt>
 * <dd>Optional. Sets the logic used for {@link #getPackage(String)}. Default is
 * {@link SingleEntityLogic}</dd>
 * <dt>{@link #setFullPackageLogic(DipLogic)}</dt>
 * <dd>Optional. Sets the logic used for {@link #getFullPackage(String)}.
 * Default is {@link SignificantlyRelatedFullDipTreeLogic}</dd>
 * <dt>{@link #setDipAssembler(DipAssembler)}</dt>
 * <dd>Optional. Default is {@link WalkingDipAssembler}</dd>
 * <dt>{@link #setLinkFinder(LinkFinder)}</dt>
 * <dd>Optional. Default is {@link ComprehensiveLinkFinder}</dd>
 * <dt>{@link #setModelBuilder(DcsModelBuilder)}</dt>
 * <dd>Optoinal. Default is {@link DcsXstreamStaxModelBuilder}</dd>
 * </dl>
 * </p>
 */
public class ElmArchiveStore
        implements ArchiveStore {

    private static final Logger log =
            LoggerFactory.getLogger(ElmArchiveStore.class);

    private MetadataStore mStore;

    private EntityStore eStore;

    private DipLogic defaultLogic = new SingleEntityLogic();

    private DipLogic fullPackageLogic =
            new SignificantlyRelatedFullDipTreeLogic();

    private DipAssembler dipAssembler;

    private DcsModelBuilder model = new DcsXstreamStaxModelBuilder();

    private LinkFinder generator = new ComprehensiveLinkFinder();

    @Required
    public void setMetadataStore(MetadataStore store) {
        mStore = store;
    }

    @Required
    public void setEntityStore(EntityStore store) {
        eStore = store;
    }

    public void setDefaultPackageLogic(DipLogic logic) {
        defaultLogic = logic;
    }

    public void setFullPackageLogic(DipLogic logic) {
        defaultLogic = logic;
    }

    public void setDipAssembler(DipAssembler ass) {
        dipAssembler = ass;
    }

    public void setModelBuilder(DcsModelBuilder builder) {
        model = builder;
    }

    public void setLinkFinder(LinkFinder finder) {
        generator = finder;
    }

    @PostConstruct
    public void init() {

        if (dipAssembler == null) {
            dipAssembler = new WalkingDipAssembler(eStore, mStore);
        }
    }

    public InputStream getContent(String entityId)
            throws EntityNotFoundException {
        Metadata md = mStore.get(entityId);
       
        if (md != null) { 
            String filePath = md.getSrc();
            
            if( md.getSrc().startsWith("file://") ) {
                if( md.getSrc().substring(6, md.getSrc().length()).contains(":/") || 
                        md.getSrc().substring(6, md.getSrc().length()).contains(":\\")){
                    filePath = md.getSrc().replace("file://", "file:///");
                }
            }
            try {
                return new URL(filePath).openStream();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new EntityNotFoundException(entityId);
        }
    }

    public InputStream getFullPackage(String entityId)
            throws EntityNotFoundException {

        return new DcpEntityStream(dipAssembler.getEntities(entityId,
                                                            fullPackageLogic),
                                   eStore);

    }

    /**
     * Build a package using the provided logic.
     * 
     * @param entityId
     *        entity from which to start building package.
     * @param logic
     *        Logic for building package;
     * @return InputStream containing a valid Dcp document
     * @throws EntityNotFoundException
     */
    public InputStream getPackage(String entityId, DipLogic logic)
            throws EntityNotFoundException {
        return new DcpEntityStream(dipAssembler.getEntities(entityId, logic),
                                   eStore);
    }

    public InputStream getPackage(String entityId)
            throws EntityNotFoundException {
        return new DcpEntityStream(dipAssembler.getEntities(entityId,
                                                            defaultLogic),
                                   eStore);
    }

    public Iterator<String> listEntities(final EntityType type) {
        return new MdStoreIterator(mStore, type);
    }

    public void putPackage(InputStream dcpStream) throws AIPFormatException {

        Dcp pkg = null;
        try {
            pkg = model.buildSip(dcpStream);
        } catch (InvalidXmlException e) {
            throw new AIPFormatException(e.getMessage());
        }

        /* Add the package entities */
        addEntities(pkg);

        /* Add any link metadata implied by the SIP entities */
        if (!mStore.isReadOnly()) {
            addRels(generateLinkMetadata(pkg));
        }

    }

    private void addEntities(Dcp pkg) {
        for (DcsCollection coll : pkg.getCollections()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            model.buildCollection(coll, out);
            eStore.put(coll.getId(),
                       new ByteArrayInputStream(out.toByteArray()));
            mStore.add(coll.getId(), COLLECTION.toString(), null);
        }

        for (DcsDeliverableUnit du : pkg.getDeliverableUnits()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            model.buildDeliverableUnit(du, out);
            eStore.put(du.getId(), new ByteArrayInputStream(out.toByteArray()));
            mStore.add(du.getId(), DELIVERABLE_UNIT.toString(), null);
        }

        for (DcsEvent ev : pkg.getEvents()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            model.buildEvent(ev, out);
            eStore.put(ev.getId(), new ByteArrayInputStream(out.toByteArray()));
            mStore.add(ev.getId(), EVENT.toString(), null);
        }

        for (DcsFile file : pkg.getFiles()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            model.buildFile(file, out);
            eStore.put(file.getId(),
                       new ByteArrayInputStream(out.toByteArray()));
            mStore.add(file.getId(), FILE.toString(), file.getSource());
        }

        for (DcsManifestation man : pkg.getManifestations()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            model.buildManifestation(man, out);
            eStore
                    .put(man.getId(), new ByteArrayInputStream(out
                            .toByteArray()));
            mStore.add(man.getId(), MANIFESTATION.toString(), null);
        }
    }

    private void addRels(Map<String, Map<String, String>> rels) {
        /*
         * For each "pointed to" object, add the (id, type) of the object from
         * the sip that is pointing to it
         */
        for (Map.Entry<String, Map<String, String>> objMd : rels.entrySet()) {

            if (mStore.get(objMd.getKey()) != null) {
                mStore.get(objMd.getKey()).addLinks(objMd.getValue());
            } else {
                log.warn("Adding links that reference objects "
                        + "not inside archive is not implemented yet.  "
                        + "Skipping link to: " + objMd.getKey());
            }
        }
    }

    private Map<String, Map<String, String>> generateLinkMetadata(Dcp sip) {
        Map<String, Map<String, String>> extRels =
                new HashMap<String, Map<String, String>>();

        for (DcsCollection coll : sip.getCollections()) {
            mergeRels(generator.getOutboundLinks(coll), extRels);
        }
        for (DcsDeliverableUnit du : sip.getDeliverableUnits()) {
            mergeRels(generator.getOutboundLinks(du), extRels);
        }
        for (DcsEvent e : sip.getEvents()) {
            mergeRels(generator.getOutboundLinks(e), extRels);
        }
        for (DcsFile f : sip.getFiles()) {
            mergeRels(generator.getOutboundLinks(f), extRels);
        }
        for (DcsManifestation m : sip.getManifestations()) {
            mergeRels(generator.getOutboundLinks(m), extRels);
        }

        return extRels;
    }

    private void mergeRels(Map<String, Map<String, String>> src,
                           Map<String, Map<String, String>> dest) {
        for (Map.Entry<String, Map<String, String>> rel : src.entrySet()) {
            if (!dest.containsKey(rel.getKey())) {
                dest.put(rel.getKey(), rel.getValue());
            } else {
                dest.get(rel.getKey()).putAll(rel.getValue());
            }
        }
    }

    private class MdStoreIterator implements Iterator<String> {

        private final Iterator<Metadata> md;

        private MdStoreIterator(MetadataStore mdStore, EntityType type) {
            if (type == null) {
                ArrayList<java.lang.String> types = new ArrayList<java.lang.String>(EntityType.values().length);
                for (EntityType t : EntityType.values()) {
                    types.add(t.toString());
                }
                md = mdStore.getAll(types.toArray(new java.lang.String[] {})).iterator();
            } else {
                md = mdStore.getAll(type.toString()).iterator();
            }
        }

        public boolean hasNext() {
            return md.hasNext();
        }

        public java.lang.String next() {
            return md.next().getId();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}
