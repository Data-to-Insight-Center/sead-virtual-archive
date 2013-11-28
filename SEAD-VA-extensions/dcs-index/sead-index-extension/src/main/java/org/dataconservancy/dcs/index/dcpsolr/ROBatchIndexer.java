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
package org.dataconservancy.dcs.index.dcpsolr;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.EntityField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.EventField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.SolrName;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.*;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadEvent;
import org.seadva.model.pack.ResearchObject;
import org.xml.sax.InputSource;

/**
 * Add packages of related entities to the index and remove individual entities
 * from the index.
 * <p>
 * Each entity is persisted to a Solr document. The entity can be recreated from
 * the document. Collections of related entities are indexed and committed to
 * Solr together. An entity with the same id as an already indexed entity will
 * replace that indexed entity.
 * <p>
 * Consistency guarantee is that each document is only written once. An entity
 * is either fully indexed or not fully indexed. But an indexed entity may
 * reference an entity that has not been indexed. This may happen if an indexed
 * entity is removed and not replaced or when batch jobs are run simultaneously.
 * An exception is the addition of a DcsEvent or DcsFile in a IS_METADATA_FOR
 * relation, which has an already indexed target. That target entity will be
 * modified in the index.
 * <p>
 * Multiple BatchIndexers may be running simultaneously. A commit to the Solr
 * server (on close) by one BatchIndexer will cause documents added by all
 * threads to be made readable.
 */



//Need to add code that     indexes the parent deliverable units for each file
//For every manifestation unit that comes in, for all it's files, get the top most deliverable unit and add it to the files' parent dataset field


public class ROBatchIndexer implements BatchIndexer<ResearchObject> {
    // Controls numbers of documents added at a time.
    private static final int MAX_ADD_SIZE = 1000;

    private final SolrService solr;
    private final ArchiveStore store;

    public ROBatchIndexer(SolrService solr, ArchiveStore store) {
        this.solr = solr;
        this.store = store;
    }

      public void add(ResearchObject dcp) throws IndexServiceException {
        List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

        // entity id -> list of events targeting entity
        Map<String, List<DcsEvent>> event_target_map = new HashMap<String, List<DcsEvent>>();
        updateEventTargets(event_target_map, dcp.getEvents()); //get events for a given deliverable unit id

        // entity id -> list of DcsFile metadata ids targeting entity
        Map<String, List<String>> md_target_map = new HashMap<String, List<String>>();
        updateMetadataTargets(md_target_map, dcp.getManifestations());   //

        // entity id -> list of ancestors
        Map<String, List<String>> ancestrymap = new HashMap<String, List<String>>();

        // entity id -> immediate list of ancestors (only for file, manifestations and Deliverable Units) -Kavitha
        Map<String, List<String>> immediateAncestrymap = new HashMap<String, List<String>>();

        try {
            // Ancestor lists are computed from root to leaves

            add(docs, ancestrymap,immediateAncestrymap, event_target_map, md_target_map,
                    dcp.getEvents(),null);

            modifyIndexedEventTargets(docs, event_target_map);
            //events are added first, if added last it is a bug- and updates to the older version of the document 
            updateCollectionAncestry(ancestrymap, dcp.getCollections());
            add(docs, ancestrymap,immediateAncestrymap, event_target_map, md_target_map,
                    dcp.getCollections(),null);

            //start of parentdu calc

            //all deliverable units
            Map<String, List<String>> allDus = new HashMap<String, List<String>>();
            updateDeliverableUnitAncestry(allDus,
                    dcp.getDeliverableUnits());
            updateDeliverableUnitAncestry(ancestrymap,
                    dcp.getDeliverableUnits());  //this function finds the parent DUs of all DUs in the DCP. The parent DU's which are not in the SIP are dug out from the solr index
            updateImmediateDeliverableUnitAncestry(immediateAncestrymap,
                    dcp.getDeliverableUnits());  //Added by Kavitha

            add(docs, ancestrymap, immediateAncestrymap, event_target_map, md_target_map,
                    dcp.getDeliverableUnits(),
                    //        duFiles
                    null
            );


            //now update the other DUs that are not part of this sip
            //       updateDuParenthood(duFiles);


            updateManifestationAncestry(ancestrymap, dcp.getManifestations());
            updateImmediateManifestationAncestry(immediateAncestrymap,
                    dcp.getManifestations());    //Added by Kavitha - similar to the function above

            add(docs, ancestrymap, immediateAncestrymap, event_target_map, md_target_map,
                    dcp.getManifestations(),null);




            updateFileAncestry(ancestrymap, dcp.getFiles());
            //  updateImmediateFileAncestry(immediateAncestrymap,
            //        dcp.getFiles());    //Added by Kavitha -don't need

            add(docs, ancestrymap, immediateAncestrymap, event_target_map, md_target_map,
                    dcp.getFiles(), null);

            add(docs, ancestrymap, immediateAncestrymap, event_target_map, md_target_map,
                    dcp.getDeliverableUnits(),
                    //        duFiles
                    null
            );

            modifyIndexedMetadataFileTargets(docs, md_target_map);


            solr.server().add(docs);
        } catch (IOException e) {
            throw new IndexServiceException(e);
        } catch (SolrServerException e) {
            throw new IndexServiceException(e);
        }
    }

    private void modifyIndexedEventTargets(List<SolrInputDocument> docs,
                                           Map<String, List<DcsEvent>> targetmap) throws IOException,
            SolrServerException {
        for (String target_id : targetmap.keySet()) {
            SolrDocument target_doc = solr.lookupSolrDocument(target_id);

            if (target_doc != null) {
                SolrInputDocument new_target_doc = dup(target_doc);

                for (DcsEvent event : targetmap.get(target_id)) {
                    addEventToTarget(new_target_doc, event);
                }

                docs.add(new_target_doc);
            }
        }
    }

    // Handle the case of a metadata file having an already indexed target

    private void modifyIndexedMetadataFileTargets(List<SolrInputDocument> docs,
                                                  Map<String, List<String>> targetmap) throws IOException,
            SolrServerException {
        for (String target_id : targetmap.keySet()) {
            SolrDocument target_doc = solr.lookupSolrDocument(target_id);

            if (target_doc != null) {
                SolrInputDocument new_target_doc = dup(target_doc);

                for (String file_id : targetmap.get(target_id)) {
                    addXmlFileToTarget(new_target_doc, file_id);
                }

                docs.add(new_target_doc);
            }
        }
    }

    private void updateEventTargets(Map<String, List<DcsEvent>> targetmap,
                                    Collection<DcsEvent> events) {
        for (DcsEvent event : events) {
            for (DcsEntityReference target : event.getTargets()) {
                String target_id = null;

                if (target != null) {
                    target_id = target.getRef();
                }

                if (target_id == null) {
                    break;
                }

                List<DcsEvent> list = targetmap.get(target_id);

                if (list == null) {
                    list = new ArrayList<DcsEvent>();
                    targetmap.put(target_id, list);
                }

                list.add(event);
            }
        }
    }

    private void updateMetadataTargets(Map<String, List<String>> targetmap,
                                       Collection<DcsManifestation> mans) {
        for (DcsManifestation man : mans) {
            for (DcsManifestationFile mf : man.getManifestationFiles()) {
                DcsFileRef file_ref = mf.getRef();

                if (file_ref == null) {
                    continue;
                }

                String file_id = file_ref.getRef();

                if (file_id == null) {
                    continue;
                }

                for (DcsRelation rel : mf.getRelSet()) {
                    if (DcsRelationship.IS_METADATA_FOR != DcsRelationship
                            .fromString(rel.getRelUri())) {
                        continue;
                    }

                    DcsEntityReference target_ref = rel.getRef();

                    if (target_ref == null) {
                        continue;
                    }

                    String target_id = target_ref.getRef();

                    if (target_id == null) {
                        continue;
                    }

                    List<String> list = targetmap.get(target_id);

                    if (list == null) {
                        list = new ArrayList<String>();
                        targetmap.put(target_id, list);
                    }

                    list.add(file_id);
                }
            }
        }
    }

    private void add(List<SolrInputDocument> docs,
                     Map<String, List<String>> ancestrymap,
                     Map<String, List<String>> immediateAncestrymap,
                     Map<String, List<DcsEvent>> event_target_map,
                     Map<String, List<String>> md_target_map,
                     Collection<? extends DcsEntity> entities,
                     Map<String, List<String>> duFiles
    ) throws IOException,
            SolrServerException {
        for (DcsEntity entity : entities) {
            SolrInputDocument doc = SeadSolrMapper.toSolr(entity, store);

//            if(entity instanceof DcsDeliverableUnit){
//                 List<String> fileIds = duFiles.get(entity.getId());
//                doc.addField(EntityField.PARENTDU.solrName(), fileIds);
//                duFiles.remove(entity.getId());
//            }
            if(entity instanceof DcsDeliverableUnit){
                if(((DcsDeliverableUnit) entity).getMetadataRef()!=null&&((DcsDeliverableUnit) entity).getMetadataRef().size()>0)
                    doc = SeadSolrMapper.indexMetadataFile(doc, ((SeadDeliverableUnit) entity).getMetadataRef(), docs);

            }

            //this doc contains the entity as a solr document with all th inherent fields of that entity populated


            //afterward, it adds the ancestry to the solr document  and finally the doc is added to the rest of the list of documents

            // Add ancestry field

            List<String> ancestors = ancestrymap.get(entity.getId());

            if (ancestors != null && ancestors.size() > 0) {
                doc.addField(EntityField.ANCESTRY.solrName(), ancestors);
            }

            List<String> immediateAncestors = immediateAncestrymap.get(entity.getId());

            if (immediateAncestors != null && immediateAncestors.size() > 0) {
                doc.addField(SeadSolrField.EntityField.IMMEDIATEANCESTRY.solrName(), immediateAncestors);
            }

            // Add fields from events targeting this entity.
            List<DcsEvent> events = event_target_map.get(entity.getId());

            if (events != null) {
                for (DcsEvent event : events) {
                    addEventToTarget(doc, event);
                }
            }



            // Add file contents from metadata files targeting this entity.
            List<String> file_ids = md_target_map.get(entity.getId());

            if (file_ids != null) {
                for (String file_id : file_ids) {
                    addXmlFileToTarget(doc, file_id);
                }
            }

            docs.add(doc);

            if (docs.size() > MAX_ADD_SIZE) {
                solr.server().add(docs);
                docs.clear();
            }
        }
    }

    private void addXmlFileToTarget(SolrInputDocument doc, String file_id)
            throws IOException {
        /*  InputStream is = null;

   try {
       is = store.getContent(file_id);
       DcsSolrMapper.addXml(doc, "ext_",
               MetadataField.SEARCH_TEXT.solrName(), new InputSource(is));
   } catch (Exception e) {
       throw new IOException("Unable to index external file " + file_id
               + ": " + e.getMessage(), e);
   } finally {
       try {
           if (is != null) {
               is.close();
           }
       } catch (IOException e) {
           // ignore
       }
   }     */
    }

    private SolrInputDocument dup(SolrDocument doc) {
        SolrInputDocument newdoc = new SolrInputDocument();
        copy(doc, newdoc);
        return newdoc;
    }

    private void copy(SolrDocument from, SolrInputDocument to) {
        for (String name : from.getFieldNames()) {
            to.addField(name, from.getFieldValue(name));
        }
    }

    private void updateCollectionAncestry(
            Map<String, List<String>> ancestrymap, Collection<DcsCollection> col_set)
            throws IOException, SolrServerException {

        // TODO can use archive store!!
        // id -> collection
        Map<String, DcsCollection> col_map = new HashMap<String, DcsCollection>();

        for (DcsCollection col : col_set) {
            col_map.put(col.getId(), col);
        }

        for (DcsCollection col : col_set) {
            List<String> ancestors = new ArrayList<String>(2);
            ancestrymap.put(col.getId(), ancestors);

            for (;;) {
                DcsCollectionRef ref = col.getParent();

                String parent_id = null;

                if (ref != null) {
                    parent_id = ref.getRef();
                }

                if (parent_id == null) {
                    break;
                }

                if (add_if_not_present(ancestors, parent_id)) {
                    DcsCollection parent = col_map.get(parent_id);

                    if (parent == null) {
                        addAncestorsFromSolr(parent_id, ancestors);
                        break;
                    } else {
                        col = parent;
                    }
                } else {
                    break;
                }
            }

            for (DcsEntityReference ref : col.getMetadataRef()) {
                updateAncestry(ancestrymap, ref, col.getId());
            }
        }
    }

    private void updateDeliverableUnitAncestry(
            Map<String, List<String>> ancestrymap, Collection<DcsDeliverableUnit> set)
            throws IOException, SolrServerException {
        // TODO can use archive store!!
        //(Ancestry map) id -> parent du
        Map<String, DcsDeliverableUnit> du_map = new HashMap<String, DcsDeliverableUnit>();

        for (DcsDeliverableUnit du : set) {
            du_map.put(du.getId(), du);
        }

        for (DcsDeliverableUnit du : set) {
            List<String> ancestors = new ArrayList<String>(2);
            ancestrymap.put(du.getId(), ancestors);

            LinkedList<DcsDeliverableUnit> todo = new LinkedList<DcsDeliverableUnit>();
            todo.add(du);

            // Find all ancestors of dus in todo

            while (!todo.isEmpty()) {
                DcsDeliverableUnit check = todo.pop();

                // Add collections

                for (DcsCollectionRef col_ref : check.getCollections()) {
                    updateAncestry(ancestrymap, du.getId(), col_ref);
                }

                // Add parents

                for (DcsDeliverableUnitRef ref : check.getParents()) {
                    String parent_id = null;

                    if (ref != null) {
                        parent_id = ref.getRef();
                    }

                    DcsDeliverableUnit parent = du_map.get(parent_id);

                    if (add_if_not_present(ancestors, parent_id)) {
                        if (parent == null) {
                            addAncestorsFromSolr(parent_id, ancestors);
                        } else {
                            todo.add(parent);
                        }
                    }
                }
            }

            for (DcsEntityReference ref : du.getMetadataRef()) {
                updateAncestry(ancestrymap, ref, du.getId());
            }
        }
    }


    private void updateImmediateDeliverableUnitAncestry(
            Map<String, List<String>> immaediateAncestrymap, Collection<DcsDeliverableUnit> set)
            throws IOException, SolrServerException {
        // TODO can use archive store!!
        //(Ancestry map) id -> parent du
        Map<String, DcsDeliverableUnit> du_map = new HashMap<String, DcsDeliverableUnit>();

        for (DcsDeliverableUnit du : set) {
            du_map.put(du.getId(), du);
        }

        for (DcsDeliverableUnit du : set) {
            List<String> ancestors = new ArrayList<String>(2);
            immaediateAncestrymap.put(du.getId(), ancestors);

            LinkedList<DcsDeliverableUnit> todo = new LinkedList<DcsDeliverableUnit>();
            todo.add(du);

            // Find all ancestors of dus in todo

            while (!todo.isEmpty()) {
                DcsDeliverableUnit check = todo.pop();

                // Add collections

                for (DcsCollectionRef col_ref : check.getCollections()) {
                    updateAncestry(immaediateAncestrymap, du.getId(), col_ref);
                }

                // Add parents

                for (DcsDeliverableUnitRef ref : check.getParents()) {
                    String parent_id = null;

                    if (ref != null) {
                        parent_id = ref.getRef();
                    }

                    DcsDeliverableUnit parent = du_map.get(parent_id);

                    if (add_if_not_present(ancestors, parent_id)) {
                        if (parent == null) {
                            addAncestorsFromSolr(parent_id, ancestors);
                        }
//                        else {
//                            todo.add(parent);  //Commented by Kavitha to remove indexing all levels of ancestry
//                        }
                    }
                }
            }

            for (DcsEntityReference ref : du.getMetadataRef()) {
                updateAncestry(immaediateAncestrymap, ref, du.getId());
            }
        }
    }



    private void updateManifestationAncestry(
            Map<String, List<String>> ancestrymap, Collection<DcsManifestation> set)
            throws IOException, SolrServerException {
        for (DcsManifestation man : set) {
            List<String> ancestors = new ArrayList<String>(2);
            ancestrymap.put(man.getId(), ancestors);

            updateAncestry(ancestrymap, man.getId(), man.getDeliverableUnit());

            for (DcsManifestationFile mf : man.getManifestationFiles()) {
                updateAncestry(ancestrymap, mf.getRef(), man.getId());
            }

            for (DcsMetadataRef ref : man.getMetadataRef()) {
                updateAncestry(ancestrymap, ref, man.getId());
            }
        }
    }

    private void updateImmediateManifestationAncestry(
            Map<String, List<String>> immediateAncestrymap, Collection<DcsManifestation> set)
            throws IOException, SolrServerException {
        for (DcsManifestation man : set) {
            List<String> ancestors = new ArrayList<String>(2);
            //ancestors.add(man.getDeliverableUnit());

            // immediateAncestrymap.put(man.getId(), ancestors);

            //   ancestors = new ArrayList<String>(2);
            ancestors.add(man.getDeliverableUnit());
            for (DcsManifestationFile mf : man.getManifestationFiles()) {
                immediateAncestrymap.put(mf.getRef().getRef() ,ancestors);
            }

            for (DcsMetadataRef ref : man.getMetadataRef()) {   //Skipping for no
                immediateAncestrymap.put(ref.getRef(), ancestors);
            }
        }
    }

    private void updateFileAncestry(Map<String, List<String>> ancestrymap,
                                    Collection<DcsFile> set) throws IOException, SolrServerException {
        for (DcsFile file : set) {
            for (DcsMetadataRef ref : file.getMetadataRef()) {
                updateAncestry(ancestrymap, ref, file.getId());
            }
        }
    }

    private void updateAncestry(Map<String, List<String>> ancestrymap,
                                DcsEntityReference entity_ref, String ancestor_id)
            throws IOException, SolrServerException {
        if (entity_ref == null) {
            return;
        }

        updateAncestry(ancestrymap, entity_ref.getRef(), ancestor_id);
    }

    private void updateAncestry(Map<String, List<String>> ancestrymap,
                                String entity_id, DcsEntityReference ancestor_ref)
            throws IOException, SolrServerException {
        if (ancestor_ref == null) {
            return;
        }

        updateAncestry(ancestrymap, entity_id, ancestor_ref.getRef());
    }

    private void updateAncestry(Map<String, List<String>> ancestrymap,
                                String entity_id, String ancestor_id) throws IOException,
            SolrServerException {
        if (entity_id == null || ancestor_id == null) {
            return;
        }

        List<String> ancestors = ancestrymap.get(entity_id);

        if (ancestors == null) {
            ancestors = new ArrayList<String>(2);
            ancestrymap.put(entity_id, ancestors);
        }

        ancestors.add(ancestor_id);

        List<String> ancestor_ancestors = ancestrymap.get(ancestor_id);

        if (ancestor_ancestors == null) {
            addAncestorsFromSolr(ancestor_id, ancestors);
        } else {
            ancestors.addAll(ancestor_ancestors);
        }
    }

    /**
     * Remove an entity that is existing in the index.
     */
    public void remove(String id) throws IndexServiceException {
        try {
            solr.server().deleteById(id);
        } catch (SolrServerException e) {
            throw new IndexServiceException(e);
        } catch (IOException e) {
            throw new IndexServiceException(e);
        }
    }

    public void close() throws IndexServiceException {
        try {
            solr.server().commit();
        } catch (SolrServerException e) {
            throw new IndexServiceException(e);
        } catch (IOException e) {
            throw new IndexServiceException(e);
        }
    }

    private boolean add_if_not_present(List<String> list, String item) {
        if (list.contains(item)) {
            return false;
        }

        list.add(item);

        return true;
    }

    private void addAncestorsFromSolr(String id, List<String> result)
            throws IOException, SolrServerException {
        SolrDocument doc = solr.lookupSolrDocument(id);

        if (doc != null) {
            Collection<Object> ancestors = doc
                    .getFieldValues(EntityField.ANCESTRY.solrName());

            if (ancestors != null) {
                for (Object ancestor : ancestors) {
                    result.add((String) ancestor);
                }
            }
        }
    }

    private static void add(SolrInputDocument doc, SolrName field, Object value) {
        if (value != null) {
            doc.addField(field.solrName(), value);
        }
    }

    private void addEventToTarget(SolrInputDocument doc, DcsEvent event)
            throws IOException, SolrServerException {

        add(doc, EventField.DATE, event.getDate());
        add(doc, EventField.DETAIL, event.getDetail());
        add(doc, EventField.OUTCOME, event.getOutcome());
        add(doc, EventField.TYPE, event.getEventType());
//        if(event.getLogDetail()!=null){
//            SeadLogDetail log = event.getLogDetail();
//            add(doc, DetailLogField.IPADDRESS, log.getIpAddress());
//            add(doc, DetailLogField.USERAGENT, log.getUserAgent());
//            add(doc, DetailLogField.SUBJECT, log.getSubject());
//            add(doc, DetailLogField.NODEIDENTIFIER, log.getNodeIdentifier());
//        }

        doc.addField(
                EventField.DYNAMIC_DATE_TYPE_PREFIX.solrName()
                        + event.getEventType(), event.getDate());
    }
}
