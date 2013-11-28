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

import noNamespace.MetadataDocument;
import noNamespace.PlaceType;
import noNamespace.ThemeType;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.DateUtil;
import org.apache.xmlbeans.XmlException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.*;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.dcs.*;
import org.seadva.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.dataconservancy.dcs.util.DateUtility.now;
import static org.dataconservancy.dcs.util.DateUtility.toIso8601;

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

public class SeadSolrMapper {

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
       // addStrings(doc, col.getCreators(), CoreMetadataField.CREATOR);
//        addCreatorSet(doc,col.getCreators());

        return doc;
    }

    private static SolrInputDocument toSolr(SeadDeliverableUnit du,
            ArchiveStore store) throws IOException {
        SolrInputDocument doc = new SolrInputDocument();

        add(doc, EntityField.ID, du.getId());
        add(doc, EntityField.TYPE, EntityTypeValue.DELIVERABLE_UNIT.solrValue());
        //add(doc, EntityField.MDUPDATE_DATE, du.getMetadataUpdateDate());

       // if(du.getMetadataUpdateDate()==null)
            add(doc, SeadSolrField.EntityField.MDUPDATE_DATE, toIso8601(now()));
//        else
//            add(doc, EntityField.MDUPDATE_DATE, du.getMetadataUpdateDate());

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

        //addStrings(doc, du.getCreators(), CoreMetadataField.CREATOR);
        if(du.getSubmitter()!=null)
            addSubmitter(doc, du.getSubmitter());
        addCreatorSet(doc, du.getDataContributors());
        addStrings(doc, du.getSubjects(), CoreMetadataField.SUBJECT);

        addMetadataSet(doc, du.getMetadata());
        addRelationSet(doc, du.getRelations());
        addResourceIdentifierSet(doc, du.getAlternateIds());
        SeadDataLocation primaryDataLocation = new SeadDataLocation();
        primaryDataLocation.setLocation(du.getPrimaryLocation().getLocation());
        primaryDataLocation.setName(du.getPrimaryLocation().getName());
        primaryDataLocation.setType(du.getPrimaryLocation().getType());
        addPrimaryDataLocation(doc, primaryDataLocation);
        addSecondaryDataLocationSet(doc,du.getSecondaryDataLocations());
        //addFgdcMetadata(doc,du.getMetadata());

        add(doc, CoreMetadataField.TYPE, du.getType());


       if(du.getAbstrct()!=null) {
            add(doc, SeadSolrField.EntityField.ABSTRACT, du.getAbstrct());
        }

        if(du.getPubdate()!=null) {
            add(doc, SeadSolrField.EntityField.PUBDATE, du.getPubdate());
        }

        if(du.getContact()!=null) {
            add(doc, SeadSolrField.CoreMetadataField.CONTACT, du.getContact());
        }

        if(du.getSites()!=null) {
            add(doc, SeadSolrField.DeliverableUnitField.LOCATION, du.getSites());
        }



        if (du.isDigitalSurrogate() != null) {
            doc.addField(DeliverableUnitField.DIGITAL_SURROGATE.solrName(),
                    du.isDigitalSurrogate());
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
                        rel.getRef());
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

                //Index FGDC
                String metadata = md.getMetadata();

                if(metadata.contains("<metadata>")) //fgdc - change the check
                {
                    FgdcMapping mapping= new FgdcMapping();
                    Map<Enum,String> fgdcElements = mapping.map(metadata);

                    Iterator it = fgdcElements.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        add(doc, (SeadSolrField.FgdcField) pair.getKey(), pair.getValue());
                        it.remove(); // avoids a ConcurrentModificationException
                    }

                }

                in.close();
            } catch (SAXException e) {
                throw new IOException(e);
            }
        }
    }
    private static SolrDocument toSolrDocument( SolrInputDocument d )
    {
        SolrDocument doc = new SolrDocument();
        for( String name : d.getFieldNames() ) {
            doc.addField( name, d.getFieldValue(name) );
        }
        return doc;
    }


    public static SolrInputDocument indexMetadataFile(SolrInputDocument doc,
                                          Collection<DcsMetadataRef> set,
                                          List<SolrInputDocument> docs ) {

        int foundFgdc = 0;
        //Assuming the DCS file is available in the Sip
        for(DcsMetadataRef ref:set){
            DcsFile file = null;
            for(SolrInputDocument document:docs){
                String refStr = ref.getRef();
                String id = (String) document.getFieldValue(EntityField.ID.solrName());
                if(refStr.equals(id))
                    try {
                        file = (DcsFile) fromSolr(toSolrDocument(document));
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
            }
            if(file == null)
                continue;
            //get the file from SIP or from index
          //  DcsFile file = new DcsFile();
            for(DcsFormat format:file.getFormats()){
                if(format.getFormat().contains("fgdc")){
                    MetadataDocument fgdcDoc = null;
                    try
                    {
                        String fileSource = file.getSource().replace("file://","");

                        fgdcDoc = MetadataDocument.Factory.parse(new File(fileSource));
                        ThemeType[] keywords = fgdcDoc.getMetadata().getIdinfo().getKeywords().getThemeArray();
                        PlaceType[] places = fgdcDoc.getMetadata().getIdinfo().getKeywords().getPlaceArray();

                        Set<String> themes = new HashSet<String>();
                        for(ThemeType theme:keywords){
                            for(String themeStr: theme.getThemekeyArray())
                                themes.add(themeStr);
                        }
                        addStrings(doc, themes, CoreMetadataField.SUBJECT);

                        Set<String> placeKeys = new HashSet<String>();
                        for(PlaceType place:places){
                            for(String placeStr: place.getPlacekeyArray())
                                placeKeys.add(placeStr);
                        }
                        addStrings(doc, placeKeys, SeadSolrField.DeliverableUnitField.LOCATION);

                } catch (XmlException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    foundFgdc = 1;

                }
                if(foundFgdc==1)
                    break;
            }
            if(foundFgdc==1)
                break;
        }
        return doc;
    }
    public static void saveUrl(String filename, String urlString) throws MalformedURLException, IOException
    {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try
        {
            in = new BufferedInputStream(new URL(urlString).openStream());
            fout = new FileOutputStream(filename);

            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1)
            {
                fout.write(data, 0, count);
            }
        }
        finally
        {
            if (in != null)
                in.close();
            if (fout != null)
                fout.close();
        }
    }
    // metadata refs use DcsFile ids to reference DcsFile content  //But why is this not getting indexed or is it?
    private static void addMetadataRefSet(SolrInputDocument doc,
            Collection<DcsMetadataRef> set, SolrName field, ArchiveStore store)
            throws IOException {
        if (set == null || set.size() == 0) {
            return;
        }

        addRefSet(doc, set, field);

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

    private static void add(SolrInputDocument doc, SeadSolrField.SolrName field, Object value) {
        if (value != null) {
            doc.addField(field.solrName(), value);
        }
    }

    private static void setadd(SolrInputDocument doc, SeadSolrField.SolrName field,
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

    private static SolrInputDocument toSolr(SeadFile file, ArchiveStore store)
            throws IOException {
        SolrInputDocument doc = new SolrInputDocument();
        add(doc, EntityField.ID, file.getId());
        add(doc, EntityField.TYPE, EntityTypeValue.FILE.solrValue());

        add(doc, FileField.NAME, file.getName());
        add(doc, FileField.SOURCE, file.getSource());
        add(doc, FileField.SIZE, file.getSizeBytes());


        if(file.getMetadataUpdateDate()==null||file.getMetadataUpdateDate().length()==0)
            add(doc, SeadSolrField.EntityField.MDUPDATE_DATE, toIso8601(now()));//file.getMetadataUpdateDate());
        else
           add(doc, SeadSolrField.EntityField.MDUPDATE_DATE, file.getMetadataUpdateDate());

        if (file.getValid() != null) {
            doc.addField(FileField.VALID.solrName(), file.getValid());
        }

        doc.addField(FileField.EXTANT.solrName(), file.isExtant());

        addFixitySet(doc, file.getFixity());
        addFormatSet(doc, file.getFormats());
        addResourceIdentifierSet(doc, file.getAlternateIds());

        SeadDataLocation primaryDataLocation = new SeadDataLocation();
        primaryDataLocation.setLocation(file.getPrimaryLocation().getLocation());
        primaryDataLocation.setName(file.getPrimaryLocation().getName());
        primaryDataLocation.setType(file.getPrimaryLocation().getType());
        addPrimaryDataLocation(doc, primaryDataLocation);
        addSecondaryDataLocationSet(doc, file.getSecondaryDataLocations());
        
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

    private static void addCreatorSet(SolrInputDocument doc, Collection<SeadPerson> set) {
        if( set == null || set.size() == 0){
            return;
        }

        for( SeadPerson creator : set){
            setadd(doc, SeadSolrField.CreatorField.CREATORID, creator.getId());
            setadd(doc, SeadSolrField.CreatorField.CREATORIDTYPE, creator.getIdType());
            setadd(doc, SeadSolrField.CreatorField.NAME, creator.getName());
        }
    }

    private static void addSubmitter(SolrInputDocument doc, SeadPerson submitter) {
        add(doc, SeadSolrField.SubmitterField.NAME, submitter.getName());
        add(doc, SeadSolrField.SubmitterField.SUBMITTERID, submitter.getId());
        add(doc, SeadSolrField.SubmitterField.SUBMITTERIDTYPE, submitter.getIdType());
    }

    private static void addPrimaryDataLocation(SolrInputDocument doc, SeadDataLocation dataLocation) {

        setadd(doc, SeadSolrField.PrimaryDataLocationField.NAME, dataLocation.getName());
        setadd(doc, SeadSolrField.PrimaryDataLocationField.TYPE, dataLocation.getType());
        setadd(doc, SeadSolrField.PrimaryDataLocationField.LOCATION, dataLocation.getLocation());

    }
    private static void addSecondaryDataLocationSet(SolrInputDocument doc, Set<SeadDataLocation> set) {
        if( set == null || set.size() == 0){
            return;
        }

        for( SeadDataLocation dl : set){
            setadd(doc, SeadSolrField.DataLocationField.NAME, dl.getName());
            setadd(doc, SeadSolrField.DataLocationField.TYPE, dl.getType());
            setadd(doc, SeadSolrField.DataLocationField.LOCATION, dl.getLocation());
        }
    }

    private static Set<DcsFixity> getFixitySet(SolrDocument doc) {
        Set<DcsFixity> set = new HashSet<DcsFixity>();

        String[] algs = setgetAll(doc, FixityField.ALGORITHM);
        String[] values = setgetAll(doc, FixityField.VALUE);

        for (int i = 0; i < algs.length; i++) {
            DcsFixity fix = new DcsFixity();

            fix.setAlgorithm(algs[i]);
            fix.setValue(values[i]);

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

            if(formats[i]!=null)
                fmt.setFormat(formats[i]);
            if(names[i]!=null)
                fmt.setName(names[i]);
            if(schemas[i]!=null)
                fmt.setSchemeUri(schemas[i]);
            if(versions[i]!=null)
                fmt.setVersion(versions[i]);

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

    private static Set<SeadDataLocation> getSecondaryDataLocationSet(SolrDocument doc){
        Set<SeadDataLocation> locSet = new HashSet<SeadDataLocation>();

        String[] name = setgetAll(doc, SeadSolrField.DataLocationField.NAME);
        String[] types = setgetAll(doc, SeadSolrField.DataLocationField.TYPE);
        String[] locations = setgetAll(doc, SeadSolrField.DataLocationField.LOCATION);

        for( int i = 0; i < locations.length; i++) {

            SeadDataLocation dataLocation = new SeadDataLocation();

            dataLocation.setName(name[i]);
            dataLocation.setType(types[i]);
            dataLocation.setLocation(locations[i]);

            locSet.add(dataLocation);
        }

        return locSet;

    }



    private static SeadDataLocation getPrimaryDataLocation(SolrDocument doc){
        SeadDataLocation primaryDataLocation = new SeadDataLocation();

        primaryDataLocation.setName(getFirst(doc, SeadSolrField.PrimaryDataLocationField.NAME));
        primaryDataLocation.setType(getFirst(doc, SeadSolrField.PrimaryDataLocationField.TYPE));
        primaryDataLocation.setLocation(getFirst(doc, SeadSolrField.PrimaryDataLocationField.LOCATION));

        return primaryDataLocation;

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

    private static SolrInputDocument toSolr(SeadEvent event) {
        SolrInputDocument doc = new SolrInputDocument();

        add(doc, EntityField.ID, event.getId());
        add(doc, EntityField.TYPE, EntityTypeValue.EVENT.solrValue());
        add(doc, EventField.DATE, event.getDate());
        add(doc, EventField.DETAIL, event.getDetail());
        add(doc, EventField.OUTCOME, event.getOutcome());
        add(doc, EventField.TYPE, event.getEventType());
        addResourceIdentifierSet(doc, event.getAlternateIds());

        addRefSet(doc, event.getTargets(), EventField.TARGET);

        if(event.getLogDetail()!=null){
            SeadLogDetail log = event.getLogDetail();
            add(doc, SeadSolrField.DetailLogField.IPADDRESS, log.getIpAddress());
            add(doc, SeadSolrField.DetailLogField.USERAGENT, log.getUserAgent());
            add(doc, SeadSolrField.DetailLogField.SUBJECT, log.getSubject());
            add(doc, SeadSolrField.DetailLogField.NODEIDENTIFIER, log.getNodeIdentifier());
        }

        return doc;
    }

    protected static DcsEntity fromSolr(SolrDocument doc) throws IOException {
        String type = get(doc, EntityField.TYPE);
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

    private static SeadEvent getEvent(SolrDocument doc) {
        SeadEvent event = new SeadEvent();

        event.setId(get(doc, EntityField.ID));

        Date date = (Date) doc.getFirstValue(EventField.DATE.solrName());

        if (date != null) {
            event.setDate(DateUtil.getThreadLocalDateFormat().format(date));
        }

        event.setDetail(getFirst(doc, EventField.DETAIL));
        event.setOutcome(getFirst(doc, EventField.OUTCOME));
        event.setEventType(getFirst(doc, EventField.TYPE));
        event.setTargets(getEntityRefSet(doc, EventField.TARGET));
        event.setAlternateIds(getResourceIdentifierSet(doc));

        SeadLogDetail logDetail = new SeadLogDetail();

     //   if (has(doc, DetailLogField.IPADDRESS)) {

            logDetail.setIpAddress(
                   (String)doc.getFieldValue(SeadSolrField.DetailLogField.IPADDRESS
                    .solrName())
            );
      //  }
        if (has(doc, SeadSolrField.DetailLogField.USERAGENT)) {

            logDetail.setUserAgent((String)doc.getFieldValue(SeadSolrField.DetailLogField.USERAGENT
                    .solrName()));
        }
        if (has(doc, SeadSolrField.DetailLogField.SUBJECT)) {

            logDetail.setSubject((String)doc.getFieldValue(SeadSolrField.DetailLogField.SUBJECT
                    .solrName()));
        }
        if (has(doc, SeadSolrField.DetailLogField.NODEIDENTIFIER)) {

            logDetail.setNodeIdentifier((String)doc.getFieldValue(SeadSolrField.DetailLogField.NODEIDENTIFIER
                    .solrName()));
        }

        event.setLogDetail(logDetail);

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

        man.setId(get(doc, EntityField.ID));

        if (has(doc, ManifestationField.DELIVERABLE_UNIT)) {
            man.setDeliverableUnit(get(doc, ManifestationField.DELIVERABLE_UNIT));
        }

        man.setMetadata(getMetadataSet(doc));
        man.setMetadataRef(getMetadataRefSet(doc, EntityField.METADATA_REF));
        man.setManifestationFiles(getManifestationFileSet(doc));

        if (has(doc, CoreMetadataField.TYPE)) {
            man.setType(get(doc, CoreMetadataField.TYPE));
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
        SeadFile file = new SeadFile();

        file.setId(get(doc, EntityField.ID));
        file.setFixity(getFixitySet(doc));
        file.setFormats(getFormatSet(doc));
        file.setExtant((Boolean) doc.getFieldValue(FileField.EXTANT.solrName()));

        if (has(doc, FileField.VALID)) {
            file.setValid((Boolean) doc.getFieldValue(FileField.VALID
                    .solrName()));
        }

        if (has(doc, FileField.NAME)) {
            file.setName(getFirst(doc, FileField.NAME));
        }

        if (has(doc, FileField.SOURCE)) {
            file.setSource(getFirst(doc, FileField.SOURCE));
        }

        if (has(doc, SeadSolrField.EntityField.IMMEDIATEANCESTRY)) {
            file.setParent(getFirst(doc, SeadSolrField.EntityField.IMMEDIATEANCESTRY));
        }

        if(has(doc, SeadSolrField.EntityField.MDUPDATE_DATE)) {
            Object object = doc.getFirstValue(SeadSolrField.EntityField.MDUPDATE_DATE.solrName());
           if(object instanceof Date) {
                Date date = (Date)object;

                if (date != null) {
                    file.setMetadataUpdateDate(
                            DateUtil.getThreadLocalDateFormat().format(date));
                }
            }
            else {

                if (object != null) {
                    file.setMetadataUpdateDate(
                            object.toString());
                }
            }
        }
        else{
            file.setMetadataUpdateDate("2012-10-27T22:05:20.809Z");
        }

        if(has(doc, SeadSolrField.FileField.DEPOSITDATE)) {

            Object object = doc.getFirstValue(SeadSolrField.FileField.DEPOSITDATE.solrName());
            if(object instanceof Date) {
                Date date = (Date)object;// doc.getFirstValue(FileField.DEPOSITDATE.solrName());

                if (date != null) {
                    file.setDepositDate(
                            DateUtil.getThreadLocalDateFormat().format(date));
                }
            }
            else{

                if (object != null) {
                    file.setDepositDate(
                            object.toString());
                }
            }
        }
        else{
            file.setDepositDate("2012-10-27T22:05:20.809Z");
        }

        long size = (Long) doc.getFirstValue(FileField.SIZE.solrName());
        if (size > 0) {
            file.setSizeBytes(size);
        }

        file.setMetadata(getMetadataSet(doc));
        file.setMetadataRef(getMetadataRefSet(doc, EntityField.METADATA_REF));
        file.setAlternateIds(getResourceIdentifierSet(doc));
        SeadDataLocation primaryDataLocation = getPrimaryDataLocation(doc);
        file.setPrimaryLocation(primaryDataLocation);

        file.setSecondaryDataLocations(getSecondaryDataLocationSet(doc));

        return file;
    }

    private static DcsEntity getDeliverableUnit(SolrDocument doc) {
        SeadDeliverableUnit du = new SeadDeliverableUnit();

        du.setId(get(doc, EntityField.ID));

        if (has(doc, CoreMetadataField.RIGHTS)) {
            du.setRights(get(doc, CoreMetadataField.RIGHTS));
        }

        du.setCollections(getCollectionRefSet(doc,
                DeliverableUnitField.COLLECTIONS));
        du.setFormerExternalRefs(getStringSet(doc,
                DeliverableUnitField.FORMER_REFS));

        if (has(doc, CoreMetadataField.TITLE)) {
            du.setTitle(getFirst(doc, CoreMetadataField.TITLE));
        }


        if (has(doc, SeadSolrField.EntityField.ABSTRACT)) {
            String abstrct = get(doc, SeadSolrField.EntityField.ABSTRACT);
            du.setAbstrct(abstrct);
        }

        if (has(doc, SeadSolrField.EntityField.PUBDATE)) {
            String pubdate = getFirst(doc, SeadSolrField.EntityField.PUBDATE);
            du.setPubdate(pubdate);
        }

        if(has(doc, SeadSolrField.EntityField.MDUPDATE_DATE)) {
            Date date = (Date) doc.getFirstValue(SeadSolrField.EntityField.MDUPDATE_DATE.solrName());

            if (date != null) {
                du.setMetadataUpdateDate(DateUtil.getThreadLocalDateFormat().format(date));
            }
        }

//        if (has(doc, DeliverableUnitField.LOCATION)) {
//            String location = getFirst(doc, DeliverableUnitField.LOCATION);
//            du.addSite(location);
//        }

        du.setSites(getStringSet(doc, SeadSolrField.DeliverableUnitField.LOCATION));


        if (has(doc, SeadSolrField.DeliverableUnitField.SIZEBYTES)) {
            long size = (Long) doc.getFirstValue(SeadSolrField.DeliverableUnitField.SIZEBYTES.solrName());
            if (size > 0) {
                du.setSizeBytes(size);
            }
        }

        if (has(doc, SeadSolrField.DeliverableUnitField.FILENO)) {
            long fileNo = (Long) doc.getFirstValue(SeadSolrField.DeliverableUnitField.FILENO.solrName());
            if (fileNo > 0) {
                du.setFileNo(fileNo);
            }
        }

        if (has(doc, SeadSolrField.CoreMetadataField.CONTACT)) {
            String contact = getFirst(doc, SeadSolrField.CoreMetadataField.CONTACT);
            du.setContact(contact);
        }




        du.setCollections(getCollectionRefSet(doc,
                DeliverableUnitField.COLLECTIONS));
        du.setFormerExternalRefs(getStringSet(doc,
                DeliverableUnitField.FORMER_REFS));

        if (has(doc, DeliverableUnitField.DIGITAL_SURROGATE)) {
            du.setDigitalSurrogate((Boolean) doc
                    .getFieldValue(DeliverableUnitField.DIGITAL_SURROGATE
                            .solrName()));
        }

        du.setMetadataRef(getMetadataRefSet(doc, EntityField.METADATA_REF));
        du.setMetadata(getMetadataSet(doc));
        du.setRelations(getRelations(doc));
        du.setParents(getDeliverableUnitRefSet(doc, DeliverableUnitField.PARENT));

        du.setSubjects(getStringSet(doc, CoreMetadataField.SUBJECT));
        du.setDataContributors(getCreatorSet(doc));
        du.setSubmitter(getSubmitter(doc));
        du.setAlternateIds(getResourceIdentifierSet(doc));
        SeadDataLocation primaryDataLocation = getPrimaryDataLocation(doc);
        du.setPrimaryLocation(primaryDataLocation);
        du.setSecondaryDataLocations(getSecondaryDataLocationSet(doc));


        if (has(doc, CoreMetadataField.TYPE)) {
            du.setType(getFirst(doc, CoreMetadataField.TYPE));
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
            md.setSchemaUri(schemas[i]);

            set.add(md);
        }

        return set;
    }



    private static Set<SeadPerson> getCreatorSet(SolrDocument doc) {
        Set<SeadPerson> set = new HashSet<SeadPerson>();

        String[] names = setgetAll(doc, SeadSolrField.CreatorField.NAME);
        String[] ids = setgetAll(doc, SeadSolrField.CreatorField.CREATORID);
        String[] types = setgetAll(doc, SeadSolrField.CreatorField.CREATORIDTYPE);

        for (int i = 0; i < names.length; i++) {
            SeadPerson cr = new SeadPerson();

            cr.setName(names[i]);
            cr.setId(ids[i]);
            cr.setIdType(types[i]);


            set.add(cr);
        }

        return set;
    }

    private static SeadPerson getSubmitter(SolrDocument doc) {
        SeadPerson submitter = new SeadPerson();

        String name = getFirst(doc, SeadSolrField.SubmitterField.NAME);
        String id = getFirst(doc, SeadSolrField.SubmitterField.SUBMITTERID);
        String type = getFirst(doc, SeadSolrField.SubmitterField.SUBMITTERIDTYPE);
        submitter.setName(name);
        submitter.setId(id);
        submitter.setIdType(type);

        return submitter;
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

        String s = get(doc, field);

        if (s != null) {
            ref.setRef(s);
        }

        return ref;
    }

    private static String get(SolrDocument doc, SolrName field) {
        Object temp = doc.getFieldValue(field.solrName());
        if(temp instanceof ArrayList)
            return (String)((ArrayList) temp).get(0);
        return (String)temp ;
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

        col.setId(get(doc, EntityField.ID));

        if (has(doc, CollectionField.PARENT)) {
            col.setParent(getCollectionRef(doc, CollectionField.PARENT));
        }

        if (has(doc, CoreMetadataField.TYPE)) {
            col.setType(get(doc, CoreMetadataField.TYPE));
        }

        if (has(doc, CoreMetadataField.TITLE)) {
            col.setTitle(get(doc, CoreMetadataField.TITLE));
        }

        col.setMetadata(getMetadataSet(doc));
        col.setMetadataRef(col.getMetadataRef());

        col.setSubjects(getStringSet(doc, CoreMetadataField.SUBJECT));
        col.setAlternateIds(getResourceIdentifierSet(doc));

        return col;
    }

    protected static SolrInputDocument toSolr(DcsEntity entity,
            ArchiveStore store) throws IOException {
        if (entity instanceof DcsCollection) {
            return SeadSolrMapper.toSolr((DcsCollection) entity, store);
        } else if (entity instanceof DcsDeliverableUnit) {
            return SeadSolrMapper.toSolr((SeadDeliverableUnit) entity, store);
        } else if (entity instanceof DcsFile) {
            return SeadSolrMapper.toSolr((SeadFile) entity, store);
        }
        else if (entity instanceof SeadEvent) {
            return SeadSolrMapper.toSolr(new SeadEvent((SeadEvent)entity));
        }else if (entity instanceof DcsEvent) {
            return SeadSolrMapper.toSolr(new SeadEvent((DcsEvent)entity));
        } else if (entity instanceof DcsManifestation) {
            return SeadSolrMapper.toSolr((DcsManifestation) entity, store);
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
