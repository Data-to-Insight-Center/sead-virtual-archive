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
import java.io.Reader;
import java.io.StringReader;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.DateUtil;

import org.dataconservancy.model.dcs.support.Util;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.CollectionField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.CoreMetadataField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.DeliverableUnitField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.EntityField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.EntityTypeValue;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.EventField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.FileField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.FixityField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.FormatField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.ManifestationField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.ManifestationFileField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.MetadataField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.RelationField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.ResourceIdentifierField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.SolrName;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO There is an assumption that order is maintained when retrieving fields with multiple values. That assumption may be completely unwarranted because interface stupidly exposes
// Collection. But the underlying implementation is ArrayList for now...
// TODO I suspect reconstructing the object from the index is quite inefficient. Strong guarantees on null values would help...

/**
 * Map the DCS data model to Solr and back. Some entities aggregate a set of
 * objects. The object may have multiple attributes. Those attributes may be
 * null. The object set is stored in the document of the entity. Because a set
 * of objects is being stored, using non-existence of a field to represent null
 * does not work. Instead there is a boolean parallel field for each attribute
 * field that indicates whether or not the field is null.
 */

public class DcsSolrMapper {

    public static String IS_NULL_FIELD_SUFFIX = "_isnull";

    private static String RELATION_FIELD_PREFIX = "rel_";

    private static Logger LOG = LoggerFactory.getLogger(DcsSolrMapper.class);

    private static SolrInputDocument toSolr(DcsCollection col,
            ArchiveStore store) throws IOException {
        SolrInputDocument doc = new SolrInputDocument();

        add(doc, EntityField.ID, col.getId());
        add(doc, EntityField.TYPE, EntityTypeValue.COLLECTION.solrValue());

        if (col.getParent() != null) {
            add(doc, CollectionField.PARENT, col.getParent().getRef());
        }

        add(doc, CoreMetadataField.TITLE, col.getTitle());

        addMetadataSet(doc, col.getMetadata());
        addMetadataRefSet(doc, col.getMetadataRef(), EntityField.METADATA_REF,
                store);

        addResourceIdentifierSet(doc, col.getAlternateIds());
        
        add(doc, CoreMetadataField.TYPE, col.getType());

        addStrings(doc, col.getSubjects(), CoreMetadataField.SUBJECT);
        addStrings(doc, col.getCreators(), CoreMetadataField.CREATOR);

        return doc;
    }

    private static Set<URI> getURIs(SolrDocument doc, SolrName field) {
        Set<URI> set = new HashSet<URI>();

        for (String s : setgetAll(doc, field)) {
            if (s != null) {
                try {
                    set.add(new URI(s));
                } catch (URISyntaxException e) {
                    LOG.warn("Illegal URI '" + s + "'", e);
                }
            }
        }

        return set;
    }

    private static SolrInputDocument toSolr(DcsDeliverableUnit du,
            ArchiveStore store) throws IOException {
        SolrInputDocument doc = new SolrInputDocument();

        add(doc, EntityField.ID, du.getId());
        add(doc, EntityField.TYPE, EntityTypeValue.DELIVERABLE_UNIT.solrValue());

        add(doc, CoreMetadataField.TITLE, du.getTitle());

        if( du.getRights() != null ){
            add(doc, CoreMetadataField.RIGHTS, du.getRights());
        }

        addRefSet(doc, du.getCollections(), DeliverableUnitField.COLLECTIONS);
        addRefSet(doc, du.getParents(), DeliverableUnitField.PARENT);
        addMetadataRefSet(doc, du.getMetadataRef(), EntityField.METADATA_REF,
                store);

        addStrings(doc, du.getFormerExternalRefs(),

        DeliverableUnitField.FORMER_REFS);

        addStrings(doc, du.getCreators(), CoreMetadataField.CREATOR);
        addStrings(doc, du.getSubjects(), CoreMetadataField.SUBJECT);

        addMetadataSet(doc, du.getMetadata());
        addRelationSet(doc, du.getRelations());
        addResourceIdentifierSet(doc, du.getAlternateIds());
        add(doc, CoreMetadataField.TYPE, du.getType());

        if (du.isDigitalSurrogate() != null) {
            doc.addField(DeliverableUnitField.DIGITAL_SURROGATE.solrName(),
                    du.isDigitalSurrogate());
        }
        
        if (du.getLineageId() != null && !du.getLineageId().isEmpty()) {
            add(doc, DeliverableUnitField.LINEAGE, du.getLineageId());
        }

        return doc;
    }

    private static void addRelationSet(SolrInputDocument doc,
            Collection<DcsRelation> set) {
        if (set == null || set.size() == 0) {
            return;
        }

        for (DcsRelation rel : set) {
            setadd(doc, RelationField.TARGET, rel.getRef() == null ? null : rel
                    .getRef().getRef());
            setadd(doc, RelationField.RELATION, rel.getRelUri());

            if (rel.getRef() != null && rel.getRelUri() != null) {
                doc.addField(RELATION_FIELD_PREFIX + rel.getRelUri(),
                        rel.getRef().getRef());
            }
        }
    }

    private static void addMetadataSet(SolrInputDocument doc,
            Collection<DcsMetadata> set) throws IOException {
        if (set == null || set.size() == 0) {
            return;
        }

        for (DcsMetadata md : set) {
            setadd(doc, MetadataField.SCHEMA, md.getSchemaUri());
            setadd(doc, MetadataField.TEXT, md.getMetadata());

            if (md.getMetadata() == null) {
                continue;
            }

            try {
                Reader in = new StringReader(md.getMetadata());
                addXml(doc, "ext_", MetadataField.SEARCH_TEXT.solrName(),
                        new InputSource(in));
                in.close();
            } catch (SAXException e) {
                throw new IOException(e);
            }
        }
    }

    // metadata refs use DcsFile ids to reference DcsFile content
    private static void addMetadataRefSet(SolrInputDocument doc,
            Collection<DcsMetadataRef> set, SolrName field, ArchiveStore store)
            throws IOException {
        if (set == null || set.size() == 0) {
            return;
        }

        addRefSet(doc, set, field);

        if (store == null) {
            return;
        }

        for (DcsMetadataRef mdref : set) {
            String ref = mdref.getRef();

            if (ref == null || ref.isEmpty()) {
                continue;
            }

            InputStream is = null;

            try {
                is = store.getContent(ref);

                addXml(doc, "ext_", MetadataField.SEARCH_TEXT.solrName(),
                        new InputSource(is));
            } catch (SAXException e) {
                throw new IOException("Error indexing " + ref, e);
            } catch (EntityNotFoundException e) {
                LOG.warn("Invalid entity ref: " + ref, e);
            } catch (EntityTypeException e) {
                throw new IOException("Error indexing " + ref, e);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    private static void addRefSet(SolrInputDocument doc,
            Collection<? extends DcsEntityReference> set,

            SolrName field) {
        if (set == null || set.size() == 0) {
            return;
        }

        for (DcsEntityReference ref : set) {
            setadd(doc, field, ref.getRef());
        }
    }

    private static void addStrings(SolrInputDocument doc, Collection<String> set,
            SolrName field) {
        if (set == null || set.size() == 0) {
            return;
        }

        for (String s : set) {
            add(doc, field, s);
        }
    }

    private static void addURIs(SolrInputDocument doc, Set<URI> set,
            SolrName field) {
        if (set == null || set.size() == 0) {
            return;
        }

        for (URI u : set) {
            setadd(doc, field, u.toString());
        }
    }

    private static void add(SolrInputDocument doc, SolrName field, Object value) {
        if (value != null) {
            doc.addField(field.solrName(), value);
        }
    }

    private static void setadd(SolrInputDocument doc, SolrName field,
            Object value) {
        setadd(doc, field.solrName(), value);
    }

    private static void setadd(SolrInputDocument doc, String solrField,
            Object value) {
        // System.out.println("solr add " + field.solrName() + " " + value);

        doc.addField(solrField, value == null ? "" : value);
        doc.addField(solrField + IS_NULL_FIELD_SUFFIX, value == null);

        // System.out.println(doc.containsKey(field.solrName() +
        // IS_NULL_FIELD_SUFFIX));

    }

    private static SolrInputDocument toSolr(DcsFile file, ArchiveStore store)
            throws IOException {
        SolrInputDocument doc = new SolrInputDocument();
        add(doc, EntityField.ID, file.getId());
        add(doc, EntityField.TYPE, EntityTypeValue.FILE.solrValue());

        add(doc, FileField.NAME, file.getName());
        add(doc, FileField.SOURCE, file.getSource());
        add(doc, FileField.SIZE, file.getSizeBytes());

        if (file.getValid() != null) {
            doc.addField(FileField.VALID.solrName(), file.getValid());
        }

        doc.addField(FileField.EXTANT.solrName(), file.isExtant());

        addFixitySet(doc, file.getFixity());
        addFormatSet(doc, file.getFormats());
        addResourceIdentifierSet(doc, file.getAlternateIds());
        
        addMetadataSet(doc, file.getMetadata());
        addMetadataRefSet(doc, file.getMetadataRef(), EntityField.METADATA_REF,
                store);

        return doc;
    }

    private static void addFixitySet(SolrInputDocument doc, Collection<DcsFixity> set) {
        if (set == null || set.size() == 0) {
            return;
        }

        for (DcsFixity fix : set) {
            setadd(doc, FixityField.ALGORITHM, fix.getAlgorithm());
            setadd(doc, FixityField.VALUE, fix.getValue());
        }
    }

    private static void addFormatSet(SolrInputDocument doc, Collection<DcsFormat> set) {
        if (set == null || set.size() == 0) {
            return;
        }

        for (DcsFormat fmt : set) {
            setadd(doc, FormatField.NAME, fmt.getName());
            setadd(doc, FormatField.FORMAT, fmt.getFormat());
            setadd(doc, FormatField.SCHEMA, fmt.getSchemeUri());
            setadd(doc, FormatField.VERSION, fmt.getVersion());
        }
    }
    
    private static void addResourceIdentifierSet(SolrInputDocument doc, Collection<DcsResourceIdentifier> set) {
        if( set == null || set.size() == 0){
            return;
        }
        
        for( DcsResourceIdentifier id : set){
            setadd(doc, ResourceIdentifierField.AUTHORITY, id.getAuthorityId());
            setadd(doc, ResourceIdentifierField.TYPE, id.getTypeId());
            setadd(doc, ResourceIdentifierField.VALUE, id.getIdValue());
        }
    }

    private static Set<DcsFixity> getFixitySet(SolrDocument doc) {
        Set<DcsFixity> set = new HashSet<DcsFixity>();

        String[] algs = setgetAll(doc, FixityField.ALGORITHM);
        String[] values = setgetAll(doc, FixityField.VALUE);

        for (int i = 0; i < algs.length; i++) {
            DcsFixity fix = new DcsFixity();

            if (!Util.isEmptyOrNull(algs[i])) {
                fix.setAlgorithm(algs[i]);
            }
            if (!Util.isEmptyOrNull(values[i])) {
                fix.setValue(values[i]);
            }

            set.add(fix);
        }

        return set;
    }

    // does not handle case of some of these not being set...

    private static Set<DcsFormat> getFormatSet(SolrDocument doc) {
        Set<DcsFormat> set = new HashSet<DcsFormat>();

        String[] names = setgetAll(doc, FormatField.NAME);
        String[] schemas = setgetAll(doc, FormatField.SCHEMA);
        String[] versions = setgetAll(doc, FormatField.VERSION);
        String[] formats = setgetAll(doc, FormatField.FORMAT);

        for (int i = 0; i < formats.length; i++) {
            DcsFormat fmt = new DcsFormat();

            if (!Util.isEmptyOrNull(formats[i])) {
                fmt.setFormat(formats[i]);
            }
            if (!Util.isEmptyOrNull(names[i])) {
                fmt.setName(names[i]);
            }
            if (!Util.isEmptyOrNull(schemas[i])) {
                fmt.setSchemeUri(schemas[i]);
            }
            if (!Util.isEmptyOrNull(versions[i])) {
                fmt.setVersion(versions[i]);
            }

            set.add(fmt);
        }

        return set;
    }
    
    private static Set<DcsResourceIdentifier> getResourceIdentifierSet(SolrDocument doc){
        Set<DcsResourceIdentifier> idSet = new HashSet<DcsResourceIdentifier>();
        
        String[] authorities = setgetAll(doc, ResourceIdentifierField.AUTHORITY);
        String[] types = setgetAll(doc, ResourceIdentifierField.TYPE);
        String[] values = setgetAll(doc, ResourceIdentifierField.VALUE);
        
        for( int i = 0; i < values.length; i++) {
            
            DcsResourceIdentifier id = new DcsResourceIdentifier();
            
            id.setAuthorityId(authorities[i]);
            id.setTypeId(types[i]);
            id.setIdValue(values[i]);
            
            idSet.add(id);
        }
        
        return idSet;
        
    }

    private static SolrInputDocument toSolr(DcsManifestation man,
            ArchiveStore store) throws IOException {
        SolrInputDocument doc = new SolrInputDocument();

        add(doc, EntityField.ID, man.getId());
        add(doc, EntityField.TYPE, EntityTypeValue.MANIFESTATION.solrValue());
        add(doc, ManifestationField.DELIVERABLE_UNIT, man.getDeliverableUnit());
        add(doc, CoreMetadataField.TYPE, man.getType());
        add(doc, ManifestationField.DATE_CREATED, man.getDateCreated());

        addMetadataSet(doc, man.getMetadata());
        addManifestationFileSet(doc, man.getManifestationFiles(), store);
        addMetadataRefSet(doc, man.getMetadataRef(), EntityField.METADATA_REF,
                store);

        addStrings(doc, man.getTechnicalEnvironment(), ManifestationField.TECH);
        
        addResourceIdentifierSet(doc, man.getAlternateIds());

        return doc;
    }

    private static void addManifestationFileSet(SolrInputDocument doc,
            Collection<DcsManifestationFile> set, ArchiveStore store)
            throws IOException {
        for (DcsManifestationFile mf : set) {

            final String fileRef = mf.getRef() == null ? null : mf.getRef()
                    .getRef();
            setadd(doc, ManifestationFileField.FILE_REF, fileRef);
            setadd(doc, ManifestationFileField.PATH, mf.getPath());

            final Collection<DcsRelation> rels = mf.getRelSet();

            if (rels != null && !rels.isEmpty() && fileRef != null) {

                addRelationSet(doc, rels);

                for (DcsRelation rel : rels) {

                    // <doc
                    // field="mf_rel_urn:dataconservancy.org:file/4326762_hasRelationship">urn:dataconservancy.org:rel/isMetadataFor</doc>
                    setadd(doc,
                            ManifestationFileField.DYNAMIC_MF_REL_PREFIX
                                    .solrName()
                                    + fileRef
                                    + "_"
                                    + RelationField.RELATION.solrName(),
                            rel.getRelUri());

                    // <doc
                    // field="mf_rel_urn:dataconservancy.org:file/4326762_relatedTo">http://dataconservancy.org/dcs/entity/article_du</doc>
                    setadd(doc,
                            ManifestationFileField.DYNAMIC_MF_REL_PREFIX
                                    .solrName()
                                    + fileRef
                                    + "_"
                                    + RelationField.TARGET.solrName(), rel
                                    .getRef().getRef());
                }
            }
        }
    }

    // TODO how to make sure date field valid wrt to solr?

    private static SolrInputDocument toSolr(DcsEvent event) {
        SolrInputDocument doc = new SolrInputDocument();

        add(doc, EntityField.ID, event.getId());
        add(doc, EntityField.TYPE, EntityTypeValue.EVENT.solrValue());
        add(doc, EventField.DATE, event.getDate());
        add(doc, EventField.DETAIL, event.getDetail());
        add(doc, EventField.OUTCOME, event.getOutcome());
        add(doc, EventField.TYPE, event.getEventType());
        addResourceIdentifierSet(doc, event.getAlternateIds());

        addRefSet(doc, event.getTargets(), EventField.TARGET);

        return doc;
    }

    protected static DcsEntity fromSolr(SolrDocument doc) throws IOException {
        String type = getFirst(doc, EntityField.TYPE);
        if (type == null) {
            throw new IOException("Missing field: "
                    + EntityField.TYPE.solrName());
        }

        if (type.equals(EntityTypeValue.COLLECTION.solrValue())) {
            return getCollection(doc);
        } else if (type.equals(EntityTypeValue.DELIVERABLE_UNIT.solrValue())) {
            return getDeliverableUnit(doc);
        } else if (type.equals(EntityTypeValue.FILE.solrValue())) {
            return getFile(doc);
        } else if (type.equals(EntityTypeValue.MANIFESTATION.solrValue())) {
            return getManifestation(doc);
        } else if (type.equals(EntityTypeValue.EVENT.solrValue())) {
            return getEvent(doc);
        } else {
            throw new IOException("Unknown type: " + type);
        }
    }

    private static DcsEvent getEvent(SolrDocument doc) {
        DcsEvent event = new DcsEvent();

        event.setId(getFirst(doc, EntityField.ID));

        Date date = (Date) doc.getFirstValue(EventField.DATE.solrName());

        if (date != null) {
            event.setDate(DateUtil.getThreadLocalDateFormat().format(date));
        }

        event.setDetail(getFirst(doc, EventField.DETAIL));
        event.setOutcome(getFirst(doc, EventField.OUTCOME));
        event.setEventType(getFirst(doc, EventField.TYPE));
        event.setTargets(getEntityRefSet(doc, EventField.TARGET));
        event.setAlternateIds(getResourceIdentifierSet(doc));
        return event;
    }

    private static Set<DcsEntityReference> getEntityRefSet(SolrDocument doc,
            EventField field) {
        Set<DcsEntityReference> set = new HashSet<DcsEntityReference>();

        for (String s : setgetAll(doc, field)) {
            DcsEntityReference ref = new DcsEntityReference();

            if (s != null) {
                ref.setRef(s);
            }

            set.add(ref);
        }

        return set;
    }

    private static DcsManifestation getManifestation(SolrDocument doc) {
        DcsManifestation man = new DcsManifestation();

        man.setId(getFirst(doc, EntityField.ID));

        if (has(doc, ManifestationField.DELIVERABLE_UNIT)) {
            man.setDeliverableUnit(getFirst(doc, ManifestationField.DELIVERABLE_UNIT));
        }

        man.setMetadata(getMetadataSet(doc));
        man.setMetadataRef(getMetadataRefSet(doc, EntityField.METADATA_REF));
        man.setManifestationFiles(getManifestationFileSet(doc));

        if (has(doc, CoreMetadataField.TYPE)) {
            man.setType(getFirst(doc, CoreMetadataField.TYPE));
        }

        man.setTechnicalEnvironment(getStringSet(doc, ManifestationField.TECH));

        if (has(doc, ManifestationField.DATE_CREATED)) {
            man.setDateCreated(DateUtility.toIso8601(((Date) doc
                    .getFirstValue(ManifestationField.DATE_CREATED.solrName()))
                    .getTime()));
        }

        man.setAlternateIds(getResourceIdentifierSet(doc));
        return man;
    }

    private static Set<DcsManifestationFile> getManifestationFileSet(
            SolrDocument doc) {
        Set<DcsManifestationFile> set = new HashSet<DcsManifestationFile>();

        String[] filerefs = setgetAll(doc, ManifestationFileField.FILE_REF);
        String[] paths = setgetAll(doc, ManifestationFileField.PATH);

        for (int i = 0; i < filerefs.length; i++) {
            DcsManifestationFile mf = new DcsManifestationFile();

            if (filerefs[i] != null) {
                mf.setRef(new DcsFileRef(filerefs[i]));
            }

            if (paths[i] != null && paths[i].length() > 0) {
                mf.setPath(paths[i]);
            }

            // <doc
            // field="mf_rel_urn:dataconservancy.org:file/4326762_hasRelationship">urn:dataconservancy.org:rel/isMetadataFor</doc>
            // <doc
            // field="mf_rel_urn:dataconservancy.org:file/4326762_relatedTo">http://dataconservancy.org/dcs/entity/article_du</doc>

            String[] relations = setgetAll(
                    doc,
                    ManifestationFileField.DYNAMIC_MF_REL_PREFIX.solrName()
                            + filerefs[i] + "_"
                            + RelationField.RELATION.solrName());

            String[] targets = setgetAll(
                    doc,
                    ManifestationFileField.DYNAMIC_MF_REL_PREFIX.solrName()
                            + filerefs[i] + "_"
                            + RelationField.TARGET.solrName());

            for (int j = 0; j < relations.length; j++) {
                if (relations[j] != null && targets[j] != null) {
                    DcsRelation rel = new DcsRelation(relations[j], targets[j]);
                    mf.addRel(rel);
                }
            }

            set.add(mf);
        }

        return set;
    }

    private static DcsFile getFile(SolrDocument doc) {
        DcsFile file = new DcsFile();

        file.setId(getFirst(doc, EntityField.ID));
        file.setFixity(getFixitySet(doc));
        file.setFormats(getFormatSet(doc));
        file.setExtant((Boolean) doc.getFirstValue(FileField.EXTANT.solrName()));

        if (has(doc, FileField.VALID)) {
            file.setValid((Boolean) doc.getFirstValue(FileField.VALID
                    .solrName()));
        }

        if (has(doc, FileField.NAME)) {
            file.setName(getFirst(doc, FileField.NAME));
        }

        if (has(doc, FileField.SOURCE)) {
            file.setSource(getFirst(doc, FileField.SOURCE));
        }

        long size = (Long) doc.getFirstValue(FileField.SIZE.solrName());
        if (size > 0) {
            file.setSizeBytes(size);
        }

        file.setMetadata(getMetadataSet(doc));
        file.setMetadataRef(getMetadataRefSet(doc, EntityField.METADATA_REF));
        file.setAlternateIds(getResourceIdentifierSet(doc));

        return file;
    }

    private static DcsEntity getDeliverableUnit(SolrDocument doc) {
        DcsDeliverableUnit du = new DcsDeliverableUnit();

        du.setId(getFirst(doc, EntityField.ID));

        if (has(doc, CoreMetadataField.RIGHTS)) {
            du.setRights(getFirst(doc, CoreMetadataField.RIGHTS));
        }

        du.setCollections(getCollectionRefSet(doc,
                DeliverableUnitField.COLLECTIONS));
        du.setFormerExternalRefs(getStringSet(doc,
                DeliverableUnitField.FORMER_REFS));

        if (has(doc, CoreMetadataField.TITLE)) {
            du.setTitle(getFirst(doc, CoreMetadataField.TITLE));
        }

        du.setCollections(getCollectionRefSet(doc,
                DeliverableUnitField.COLLECTIONS));
        du.setFormerExternalRefs(getStringSet(doc,
                DeliverableUnitField.FORMER_REFS));

        if (has(doc, DeliverableUnitField.DIGITAL_SURROGATE)) {
            du.setDigitalSurrogate((Boolean) doc
                    .getFirstValue(DeliverableUnitField.DIGITAL_SURROGATE
                            .solrName()));
        }

        du.setMetadataRef(getMetadataRefSet(doc, EntityField.METADATA_REF));
        du.setMetadata(getMetadataSet(doc));
        du.setRelations(getRelations(doc));
        du.setParents(getDeliverableUnitRefSet(doc, DeliverableUnitField.PARENT));

        du.setSubjects(getStringSet(doc, CoreMetadataField.SUBJECT));
        du.setCreators(getStringSet(doc, CoreMetadataField.CREATOR));
        du.setAlternateIds(getResourceIdentifierSet(doc));
        if (has(doc, CoreMetadataField.TYPE)) {
            du.setType(getFirst(doc, CoreMetadataField.TYPE));
        }
        
        if (has(doc, DeliverableUnitField.LINEAGE)) {
            du.setLineageId(getFirst(doc, DeliverableUnitField.LINEAGE));
        }

        return du;
    }

    private static Set<DcsRelation> getRelations(SolrDocument doc) {
        Set<DcsRelation> set = new HashSet<DcsRelation>();

        String[] rels = setgetAll(doc, RelationField.RELATION);
        String[] targets = setgetAll(doc, RelationField.TARGET);

        for (int i = 0; i < rels.length; i++) {
            DcsRelation rel = new DcsRelation();

            if (rels[i] != null) {
                rel.setRelUri(rels[i]);
            }

            if (targets[i] != null) {
                rel.setRef(new DcsEntityReference(targets[i]));
            }

            set.add(rel);
        }

        return set;
    }

    private static boolean has(SolrDocument doc, SolrName field) {
        return doc.containsKey(field.solrName());
    }

    private static Set<DcsMetadata> getMetadataSet(SolrDocument doc) {
        Set<DcsMetadata> set = new HashSet<DcsMetadata>();

        String[] schemas = setgetAll(doc, MetadataField.SCHEMA);
        String[] texts = setgetAll(doc, MetadataField.TEXT);

        for (int i = 0; i < schemas.length; i++) {
            DcsMetadata md = new DcsMetadata();

            md.setMetadata(texts[i]);
            if (schemas[i] != null && schemas[i].trim().length() > 0) {
                md.setSchemaUri(schemas[i]);
            }

            set.add(md);
        }

        return set;
    }

    private static Set<DcsMetadataRef> getMetadataRefSet(SolrDocument doc,
            SolrName field) {
        Set<DcsMetadataRef> set = new HashSet<DcsMetadataRef>();

        for (String s : setgetAll(doc, field)) {
            DcsMetadataRef ref = new DcsMetadataRef();

            if (s != null) {
                ref.setRef(s);
            }

            set.add(ref);
        }

        return set;
    }

    private static Set<DcsDeliverableUnitRef> getDeliverableUnitRefSet(
            SolrDocument doc, SolrName field) {
        Set<DcsDeliverableUnitRef> set = new HashSet<DcsDeliverableUnitRef>();

        for (String s : setgetAll(doc, field)) {
            DcsDeliverableUnitRef ref = new DcsDeliverableUnitRef();

            if (s != null) {
                ref.setRef(s);
            }

            set.add(ref);
        }

        return set;
    }

    private static Set<String> getStringSet(SolrDocument doc, SolrName field) {
        Set<String> set = new HashSet<String>();

        if (doc.containsKey(field.solrName())) {
            for (Object val : doc.getFieldValues(field.solrName())) {
                set.add((String) val);
            }
        }

        return set;
    }

    private static Set<DcsCollectionRef> getCollectionRefSet(SolrDocument doc,
            SolrName field) {
        Set<DcsCollectionRef> set = new HashSet<DcsCollectionRef>();

        for (String s : setgetAll(doc, field)) {
            DcsCollectionRef ref = new DcsCollectionRef();

            if (s != null) {
                ref.setRef(s);
            }

            set.add(ref);
        }

        return set;
    }

    private static DcsCollectionRef getCollectionRef(SolrDocument doc,
            SolrName field) {
        DcsCollectionRef ref = new DcsCollectionRef();

        String s = getFirst(doc, field);

        if (s != null) {
            ref.setRef(s);
        }

        return ref;
    }

    private static String getFirst(SolrDocument doc, SolrName field) {
        return (String) doc.getFirstValue(field.solrName());
    }

    private static String[] setgetAll(SolrDocument doc, SolrName field) {
        return setgetAll(doc, field.solrName());
    }

    private static String[] setgetAll(SolrDocument doc, String solrField) {
        Collection<Object> value_col = doc.getFieldValues(solrField);
        Collection<Object> isnull_col = doc.getFieldValues(solrField
                + IS_NULL_FIELD_SUFFIX);

        if (value_col == null || isnull_col == null) {
            // System.out.println("returing empty " + field.solrName());
            // System.out.println("valuecol " + (value_col == null));
            // System.out.println("isnull " + (isnull_col == null));

            return new String[] {};
        }

        Boolean[] isnull = isnull_col.toArray(new Boolean[] {});
        String[] values = value_col.toArray(new String[] {});

        for (int i = 0; i < values.length; i++) {
            if (isnull[i]) {
                values[i] = null;
            }
        }

        return values;
    }

    private static DcsCollection getCollection(SolrDocument doc) {
        DcsCollection col = new DcsCollection();

        col.setId(getFirst(doc, EntityField.ID));

        if (has(doc, CollectionField.PARENT)) {
            col.setParent(getCollectionRef(doc, CollectionField.PARENT));
        }

        if (has(doc, CoreMetadataField.TYPE)) {
            col.setType(getFirst(doc, CoreMetadataField.TYPE));
        }

        if (has(doc, CoreMetadataField.TITLE)) {
            col.setTitle(getFirst(doc, CoreMetadataField.TITLE));
        }

        col.setMetadata(getMetadataSet(doc));
        col.setMetadataRef(col.getMetadataRef());

        col.setSubjects(getStringSet(doc, CoreMetadataField.SUBJECT));
        col.setCreators(getStringSet(doc, CoreMetadataField.CREATOR));
        
        col.setAlternateIds(getResourceIdentifierSet(doc));

        return col;
    }

    protected static SolrInputDocument toSolr(DcsEntity entity,
            ArchiveStore store) throws IOException {
        if (entity instanceof DcsCollection) {
            return DcsSolrMapper.toSolr((DcsCollection) entity, store);
        } else if (entity instanceof DcsDeliverableUnit) {
            return DcsSolrMapper.toSolr((DcsDeliverableUnit) entity, store);
        } else if (entity instanceof DcsFile) {
            return DcsSolrMapper.toSolr((DcsFile) entity, store);
        } else if (entity instanceof DcsEvent) {
            return DcsSolrMapper.toSolr((DcsEvent) entity);
        } else if (entity instanceof DcsManifestation) {
            return DcsSolrMapper.toSolr((DcsManifestation) entity, store);
        } else {
            throw new IllegalArgumentException("Unhandled entity type: "
                    + entity.getClass().getName());
        }
    }

    private static void addxmltext(SolrInputDocument doc, String field,
            StringBuilder text) {
        if (text != null && text.length() > 0) {
            doc.addField(field, text.toString());
        }
    }

    // Creates a field for the text directly contained by each element,
    // each attribute of an element, and a field for all the text in the
    // document.

    protected static void addXml(final SolrInputDocument doc,
            final String fieldprefix, final String textfield, InputSource input)
            throws SAXException, IOException {
        XMLReader reader = XMLReaderFactory.createXMLReader();

        final StringBuilder text = new StringBuilder();

        reader.setContentHandler(new ContentHandler() {

            final Deque<String> parents = new LinkedList<String>();

            final Deque<StringBuilder> element_texts = new LinkedList<StringBuilder>();

            private String path() {
                StringBuilder sb = new StringBuilder(fieldprefix);

                for (String el : parents) {
                    sb.append('/');
                    sb.append(el);
                }

                return sb.toString();
            }

            public void startPrefixMapping(String prefix, String uri)
                    throws SAXException {

            }

            public void startDocument() throws SAXException {

            }

            public void skippedEntity(String name) throws SAXException {

            }

            public void setDocumentLocator(Locator locator) {
            }

            public void processingInstruction(String target, String data)
                    throws SAXException {
            }

            public void ignorableWhitespace(char[] ch, int start, int length)
                    throws SAXException {
                text.append(ch, start, length);

                element_texts.getLast().append(ch, start, length);
            }

            public void endPrefixMapping(String prefix) throws SAXException {

            }

            public void endElement(String uri, String localName, String qName)
                    throws SAXException {
                addxmltext(doc, path(), element_texts.removeLast());
                parents.removeLast();
            }

            public void endDocument() throws SAXException {
                addxmltext(doc, textfield, text);
            }

            public void characters(char[] ch, int start, int length)
                    throws SAXException {
                text.append(ch, start, length);
                text.append(' ');

                element_texts.getLast().append(ch, start, length);
            }

            public void startElement(String uri, String localName,
                    String qName, Attributes atts) throws SAXException {

                // ignore namespaces when constructing field names
                parents.addLast(localName);
                element_texts.addLast(new StringBuilder());

                for (int i = 0; i < atts.getLength(); i++) {
                    String attr_field = path() + "@" + atts.getLocalName(i);
                    doc.addField(attr_field, atts.getValue(i));
                }
            }
        });

        reader.setErrorHandler(new ErrorHandler() {

            public void warning(SAXParseException exception)
                    throws SAXException {
                // TODO log?
                throw exception;
            }

            public void fatalError(SAXParseException exception)
                    throws SAXException {
                throw exception;
            }

            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }
        });

        reader.parse(input);
    }
}
