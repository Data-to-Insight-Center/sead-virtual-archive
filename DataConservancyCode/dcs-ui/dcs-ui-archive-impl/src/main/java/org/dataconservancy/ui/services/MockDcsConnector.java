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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.dataconservancy.access.connector.CountableIterator;
import org.dataconservancy.access.connector.DcsClientFault;
import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.access.connector.DcsConnectorFault;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.ui.util.DcpUtil;
import org.dataconservancy.ui.util.MockSearchIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mocks the interaction between the InMemoryArchiveServiceImpl and the DCS archive.
 * <p/>
 * An {@code EventManager} is used to provide "normal" DcsEvent objects, or in the case of the
 * {@link InMemoryArchiveServiceImpl#InMemoryArchiveServiceImpl(org.dataconservancy.ui.dcpmap.DcpMapper,
 * org.dataconservancy.ui.dcpmap.DcpMapper, DepositDocumentResolver, MockArchiveUtil, org.dataconservancy.dcs.id.api.IdService, boolean)
 * always failing} flag set to {@code true}, the {@code EventManager} will return "ingest.failed" DcsEvent objects.
 * <p/>
 * An {@code MockArchiveUtil} instance is provided to store and retrieve deposited DCS entities; it attempts to
 * emulate the DCS archive in this way.
 */
class MockDcsConnector implements DcsConnector {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private int nextid = 0;
    private AtomFeedWriter feedWriter;
    private EventManager em;
    private MockArchiveUtil archiveUtil;
    private DcsModelBuilder model_builder;
    private boolean deleteFilesOnExit = true;

    /**
     * Constructs the mock connector
     *
     * @param em          the EventManager used to produce DcsEvent objects
     * @param archiveUtil the MockArchiveUtil used to store and retrieve DcsEntities
     */
    MockDcsConnector(EventManager em, MockArchiveUtil archiveUtil, DcsModelBuilder model_builder) {
        this.em = em;
        this.archiveUtil = archiveUtil;
        this.feedWriter = new AtomFeedWriterImpl(em);
        this.model_builder = model_builder;
    }

    /**
     * The uploaded stream is saved in a temporary file.
     *
     * @param is   the InputStream to store
     * @param size the size of the file being stored
     * @return the url to the file, in string form
     * @throws org.dataconservancy.access.connector.DcsClientFault
     *          if an IOException occurs
     */
    public String uploadFile(InputStream is, long size)
            throws DcsClientFault {
        try {
            File tmp = File.createTempFile("data", null);
            tmp.deleteOnExit();

            FileOutputStream os = new FileOutputStream(tmp);
            IOUtils.copy(is, os);
            os.close();

            return tmp.toURI().toURL().toExternalForm();
        } catch (IOException e) {
            throw new DcsClientFault(e);
        }
    }

    /**
     * Performs an search.  The implementation decides on the number of results to return.  The
     * results will start at offset 0.
     * <p/>
     * Because there is no concrete search implementation backing the mock connector, the query semantics are
     * determined by parsing the query string, and the query itself is emulated. Currently, the following kinds
     * of searches are supported:
     * <ul>
     * <li>ancestry search - find entities that share a common ancestor</li>
     * <li>identity search - find an entity with a specific id</li>
     * <li>parent search - find child Deliverable Units of a parent DU</li>
     * </ul>
     *
     * @param query the query string
     * @return an iterator over the search results, at offset 0.
     * @throws org.dataconservancy.access.connector.DcsConnectorFault
     *
     */
    public CountableIterator<DcsEntity> search(String query)
            throws DcsConnectorFault {
        return search(query, -1, 0);
    }


    /**
     * Performs an search.  The caller indicates the maximum number of results to return, and the offset within
     * the total number of results.
     * <p/>
     * Because there is no concrete search implementation backing the mock connector, the query semantics are
     * determined by parsing the query string, and the query itself is emulated. Currently, the following kinds
     * of searches are supported:
     * <ul>
     * <li>ancestry search - find entities that share a common ancestor</li>
     * <li>identity search - find an entity with a specific id</li>
     * <li>parent search - find child Deliverable Units of a parent DU</li>
     * </ul>
     *
     * @param query the query string
     * @return an iterator over the search results
     * @throws DcsConnectorFault
     */
    public CountableIterator<DcsEntity> search(String query,
                                               int maxResults,
                                               int offset) throws DcsConnectorFault {

        LinkedList<DcsEntity> result = new LinkedList<DcsEntity>();

        // Grab id
        String archive_id = null;
        if (query.contains("parent:")) {
            archive_id = query.substring(query.indexOf("parent:") + "parent:".length() + 1, query.length() - 2);
        } else {
            int i = query.indexOf('\"');
            archive_id = query.substring(i + 1, query.indexOf('\"', i + 1));
        }

        // TODO unescape solr syntax correctly
        archive_id = archive_id.replace("\\", "");

        if (query.contains(" OR ") && !query.contains("former:")) {
            // Assume sip recreation search

            // Find all the ancestors of archive_id
            performAncestrySearch(result, archive_id);

            // Add the common ancestor itself.
            DcsEntity e = archiveUtil.getEntity(archive_id);
            if (!result.contains(e)) {
                result.add(e);
            }

        } else if (query.startsWith("id:")) {
            // Assume id search
            if (archiveUtil.getEntity(archive_id) != null) {
                result.add(archiveUtil.getEntity(archive_id));
            }
        } else if (query.startsWith("ancestry:")) {
            // Assume ancestry search
            performAncestrySearch(result, archive_id);
        } else if (query.contains("parent")) {
            performParentSearch(result, archive_id);
        } else if (query.contains("former:")) {
            // example query we're handling:
            // ((entityType:"DeliverableUnit" AND former:"ed64f0fc\-8201\-47c0\-bdc9\-024078aaefbc" AND type:"root"))
            // OR ((entityType:"DeliverableUnit" AND former:"ed64f0fc\-8201\-47c0\-bdc9\-024078aaefbc" AND type:"state"))
            // another example query:
            // ((entityType:"DeliverableUnit" AND former:"id\://mooo" AND type:"root")) OR ((entityType:"DeliverableUnit" AND former:"id\://mooo" AND type:"state"))
            // another example:
            // (entityType:"DeliverableUnit" AND former:"http\://localhost\:8080/item/8" AND type:"org.dataconservancy\:types\:DataItem")

            Pattern p = Pattern.compile("^.*former:(\\S*)\\s.*$");
            Matcher m = p.matcher(query);
            if (m.find()) {
                String former_ref = m.group(1);
                former_ref = stripQuotes(former_ref);
                performFormerSearch(result, former_ref);
            } else {
                throw new RuntimeException("Unable to parse value for the 'former:' parameter from query string '" +
                        query + "'");
            }

            LinkedList<DcsEntity> culledResults = new LinkedList<DcsEntity>();

            p = Pattern.compile("type:(\\S*)");
            m = p.matcher(query);
            if (!m.find()) {
                culledResults.addAll(result);
            }

            m = p.matcher(query);

            while (m.find()) {
                String type = stripQuotes(m.group(1));

                Iterator<DcsEntity> itr = result.iterator();
                while (itr.hasNext()) {
                    DcsEntity entity = itr.next();
                    if (!(entity instanceof DcsDeliverableUnit)) {
                        culledResults.add(entity);
                    }

                    if (type.equals(((DcsDeliverableUnit) entity).getType())) {
                        culledResults.add(entity);
                    }
                }
            }

            result = culledResults;
        } else {
            throw new UnsupportedOperationException("Search not handled: " + query);
        }

        if (offset > 0 && result.size() > 0) {
            result.subList(0, offset).clear();
        }

        if (maxResults > 0 && result.size() > maxResults) {
            result.subList(maxResults, result.size()).clear();
        }

        return new MockSearchIterator(result);
    }

    private static String stripQuotes(String fieldValue) {
        fieldValue = fieldValue.replaceAll("\"", "");
        fieldValue = fieldValue.replace("\\", "");
        fieldValue = fieldValue.replaceAll("\\)", "");
        fieldValue = fieldValue.replaceAll("\\(", "");
        return fieldValue;
    }

    private void performParentSearch(LinkedList<DcsEntity> result, String archive_id) {
        //This mocks the parent search it ignores the parent id and just returns everything that parent isn't null
        for (Map.Entry<String, Set<DcsEntity>> entry : archiveUtil.getEntities().entrySet()) {
            for (DcsEntity entity : entry.getValue()) {
                if (!(entity instanceof DcsDeliverableUnit)) {
                    continue;
                }

                DcsDeliverableUnit du = (DcsDeliverableUnit) entity;
                for (DcsDeliverableUnitRef parentRef : du.getParents()) {
                    if (parentRef.getRef().equalsIgnoreCase(archive_id)) {
                        if (!result.contains(du)) {
                            result.add(du);
                        }
                    }
                }
            }
        }
    }

    private void performFormerSearch(LinkedList<DcsEntity> results, String formerExternalRef) {
        for (Map.Entry<String, Set<DcsEntity>> entries : archiveUtil.getEntities().entrySet()) {
            for (DcsEntity entity : entries.getValue()) {
                if (!(entity instanceof DcsDeliverableUnit)) {
                    continue;
                }

                DcsDeliverableUnit du = (DcsDeliverableUnit) entity;
                if (du.getFormerExternalRefs().contains(formerExternalRef)) {
                    results.add(du);
                }
            }
        }
    }

    /**
     * Finds all entities that have the Deliverable Unit referenced by {@code archive_id} as an ancestor.
     * In the example below, the manifestation and file (identified by man_id and file_id, respectively) are
     * ancestors of the DU identified by archive_id.
     * <pre>
     *     DU (archive_id) <- Manifestation (man_id) -> File (file_id)
     * </pre>
     * This method searches through the {@code archiveUtil} instance, looking for ancestors of the supplied
     * {@code archive_id} and adds them to the {@code result}.
     * <p/>
     *
     * @param result     the list to add ancestors to
     * @param archive_id the identifier of the Deliverable Unit to find ancestors of
     */
    private void performAncestrySearch(LinkedList<DcsEntity> result, String archive_id) {
        Set<DcsEntity> descendants = new HashSet<DcsEntity>();
        java.util.Collection<DcsEntity> entities = new HashSet<DcsEntity>();
        for (Set<DcsEntity> allEntities : archiveUtil.getEntities().values()) {
            entities.addAll(allEntities);
        }

        DcsDeliverableUnitRef archive_du_ref = new DcsDeliverableUnitRef(archive_id);

        for (DcsEntity entity : entities) {
            if (entity instanceof DcsDeliverableUnit) {
                DcsDeliverableUnit du = (DcsDeliverableUnit) entity;

                if (du.getId().equals(archive_id) || du.getParents().contains(archive_du_ref)) {
                    result.add(entity);
                    add_descendants(entity, entities, descendants);
                }
            }
        }

        result.addAll(descendants);
    }

    private void add_descendants(DcsEntity parent, java.util.Collection<DcsEntity> entities, Set<DcsEntity> result) {
        if (parent instanceof DcsCollection) {
            DcsCollectionRef parent_col_ref = new DcsCollectionRef(parent.getId());

            for (DcsEntity entity : entities) {
                if (entity instanceof DcsCollection) {
                    DcsCollection col = (DcsCollection) entity;

                    if (col.getParent() != null && col.getParent().equals(parent_col_ref)) {
                        result.add(entity);
                        add_descendants(entity, entities, result);
                    }
                } else if (entity instanceof DcsDeliverableUnit) {
                    DcsDeliverableUnit du = (DcsDeliverableUnit) entity;

                    if (du.getCollections().contains(parent_col_ref)) {
                        result.add(entity);
                        add_descendants(entity, entities, result);
                    }
                }
            }
        } else if (parent instanceof DcsDeliverableUnit) {
            DcsDeliverableUnitRef parent_du_ref = new DcsDeliverableUnitRef(parent.getId());

            for (DcsEntity entity : entities) {
                if (entity instanceof DcsDeliverableUnit) {
                    DcsDeliverableUnit du = (DcsDeliverableUnit) entity;

                    if (du.getParents().contains(parent_du_ref)) {
                        result.add(entity);
                        add_descendants(entity, entities, result);
                    }
                } else if (entity instanceof DcsManifestation) {
                    DcsManifestation man = (DcsManifestation) entity;

                    if (man.getDeliverableUnit() != null && man.getDeliverableUnit().equals(parent.getId())) {
                        result.add(entity);
                        add_descendants(entity, entities, result);
                    }
                }
            }
        } else if (parent instanceof DcsManifestation) {
            for (DcsEntity entity : entities) {
                if (entity instanceof DcsFile) {
                    DcsFileRef file_ref = new DcsFileRef(entity.getId());

                    for (DcsManifestationFile mf : ((DcsManifestation) parent).getManifestationFiles()) {
                        if (mf.getRef() != null && mf.getRef().equals(file_ref)) {
                            result.add(entity);
                            add_descendants(entity, entities, result);
                        }
                    }
                }
            }
        }
    }

    public InputStream getStream(String streamId) throws DcsConnectorFault {
        DcsEntity e = archiveUtil.getEntity(streamId);
        Dcp dcp = new Dcp();
        dcp.addEntity(e);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        model_builder.buildSip(dcp, os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    public Iterator<DcsFile> getFiles(String entityId)
            throws DcsConnectorFault {
        throw new UnsupportedOperationException();
    }

    private String nextid() {
        return "" + nextid++;
    }

    // make the deposit id a url to temp file containig atom feed

    public URL depositSIP(final Dcp dcp) throws DcsClientFault {

        // If debugging is enabled, save a copy of the supplied DCP
        if (log.isDebugEnabled()) {
            try {
                File sip = File.createTempFile("MockDcsConnector-deposited-sip-", ".xml");
                model_builder.buildSip(dcp, new FileOutputStream(sip));
                if (deleteFilesOnExit) {
                    sip.deleteOnExit();
                }
                log.debug("Copy of deposited SIP: " + sip.getAbsolutePath());
            } catch (IOException e) {
                log.debug("Unable to save a copy of the deposited sip for debugging purposes: " + e.getMessage(), e);
            }
        }


        // Update the mapping strategy for the archiveUtil instance to map entity ids only.
        // Keep a copy of the original mapping strategy, so we can set it back.
        final MockArchiveUtil.ID_MAPPING_STRATEGY originalStrategy = archiveUtil.getMappingStrategy();
        archiveUtil.setMappingStrategy(MockArchiveUtil.ID_MAPPING_STRATEGY.ENTITY_ID);

        // Records the mapping of business ids to archive ids for this deposit operation.  The map is cleared after.
        // The map is used to generate DcsEvents of identifier assignments, and to re-write the references between
        // archived entities in the archiveUtil.
        final Map<String, String> idMap = new HashMap<String, String>();

        try {
            final List<DcsEvent> events = new ArrayList<DcsEvent>();
            final DcsEvent eventComplete = em.newEvent("ingest.complete");

            // For each entity in the deposited package
            // 1) generate an archive identity for the entity
            // 2) replace the supplied (original) identity with the archive identity
            // 3) record the mapping of the original identity to the archive identity
            // 4) place a copy of the entity into the entity store
            //    - The structure of the entity store is:
            //      archiveId -> Dcp containing the entity with its archive identity (not original identity)
            // 5) place a copy of the submitted Dcp in its original form into the sip store.  In this way each
            //    archived entity can be mapped back to the original Dcp it was submitted in.
            //    - The structure of the sip store is:
            //      archiveId ->  Dcp
            // 6) place a copy of the entity into the archiveUtil
            //    - The structure of the archiveUtil map is:
            //      archiveId -> entity with its archive identity (not original identity)
            // 7) Update the 'ingest.complete' event with the deposited entity's archive id.

            for (DcsEntity entity : DcpUtil.asList(dcp)) {
                String archive_id = nextid();

                Dcp entity_dip = new Dcp();
                String originalId = entity.getId();
                idMap.put(originalId, archive_id);
                entity.setId(archive_id);

                DcpUtil.add(entity_dip, entity);
                archiveUtil.addEntity(entity);
                eventComplete.addTargets(new DcsEntityReference(archive_id));
            }

            // archiveUtil contains a map like
            // - archiveId -> entity
            // However, the references between entities contained in archiveUtil (such as a manifestation to a DU,
            // DU to a parent, or manifestation file to file) are using business ids.  This makes it impossible to
            // traverse a graph, so we need to update these references to use the archive identifiers instead of
            // business identifiers.
            updateArchivedIdReferences(idMap);

            // Generate 'identifier.assignment' events0
            for (Map.Entry<String, String> e : idMap.entrySet()) {
                String originalId = e.getKey();
                String archiveId = e.getValue();
                DcsEvent identifierAssignment = em.newEvent("identifier.assignment");
                identifierAssignment.addTargets(new DcsEntityReference(archiveId));
                identifierAssignment.setOutcome(originalId + " to " + archiveId);
                identifierAssignment.setDetail("Assigned archive id " + archiveId + " to " + originalId);
                events.add(identifierAssignment);
            }

            events.add(eventComplete);

            File ticket = File.createTempFile("ticket", ".xml");
            if (deleteFilesOnExit) {
                ticket.deleteOnExit();
            }
            final FileOutputStream out = new FileOutputStream(ticket);
            IOUtils.copy(feedWriter.toAtom(ticket.toURI().toString(), events.toArray(new DcsEvent[]{})), out);
            out.close();
            archiveUtil.setMappingStrategy(originalStrategy);
            idMap.clear();
            return ticket.toURI().toURL();
        } catch (IOException e) {
            throw new DcsClientFault(e);
        }
    }

    /**
     * Whether or not files generated by this connector (i.e. feeds) are deleted on JVM exit.
     *
     * @return true if files are deleted on exit
     */
    public boolean isDeleteFilesOnExit() {
        return deleteFilesOnExit;
    }

    /**
     * Set whether or not files generated by this connector (i.e. feeds) are deleted on JVM exit.  Setting
     * this to {@code false} will aid in debugging, because the files can be inspected after a test is completed.
     *
     * @param deleteFilesOnExit a flag indicating whether or not files should be deleted on JVM exit
     */
    public void setDeleteFilesOnExit(boolean deleteFilesOnExit) {
        this.deleteFilesOnExit = deleteFilesOnExit;
    }

    private void updateArchivedIdReferences(Map<String, String> idMap) {
        for (Map.Entry<String, String> e : idMap.entrySet()) {
            final String originalId = e.getKey();
            final String archiveId = e.getValue();

            for (Map.Entry<String, Set<DcsEntity>> e2 : archiveUtil.getEntities().entrySet()) {
                for (final DcsEntity archivedEntity : e2.getValue()) {
                    if (!e2.getKey().equals(archiveId)) {
                        continue;
                    }

                    if (archivedEntity instanceof DcsDeliverableUnit) {
                        for (DcsDeliverableUnitRef parentBusinessIdRef : ((DcsDeliverableUnit) archivedEntity).getParents()) {
                            if (idMap.containsKey(parentBusinessIdRef.getRef())) {
                                parentBusinessIdRef.setRef(idMap.get(parentBusinessIdRef.getRef()));
                            }
                        }
                    }

                    if (archivedEntity instanceof DcsManifestation) {
                        final DcsManifestation m = (DcsManifestation) archivedEntity;
                        if (idMap.containsKey(m.getDeliverableUnit())) {
                            m.setDeliverableUnit(idMap.get(m.getDeliverableUnit()));
                        }

                        final Set<DcsManifestationFile> updatedMfs = new HashSet<DcsManifestationFile>();

                        for (DcsManifestationFile mf : m.getManifestationFiles()) {
                            final String originalFileId = mf.getRef().getRef();
                            if (idMap.containsKey(originalFileId)) {
                                String archivedFileId = idMap.get(originalFileId);
                                DcsFileRef fileRef = mf.getRef();
                                fileRef.setRef(archivedFileId);
                                mf.setRef(fileRef);
                                updatedMfs.add(mf);
                            } else {
                                updatedMfs.add(mf);
                            }
                        }

                        m.setManifestationFiles(updatedMfs);
                    }
                }
            }
        }
    }

}

