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

import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.dao.DisciplineDAO;
import org.dataconservancy.ui.dao.MetadataFormatPropertiesDao;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.ContactInfo;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.MetadataFormatProperties;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.util.DataSetComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.dataconservancy.mhf.validation.api.MetadataValidator;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;

/**
 * Concrete {@code MetadataFormatService}, backed by the Data Conservancy Registry Framework.
 */
public class MetadataFormatServiceRegistryImpl implements MetadataFormatService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TypedRegistry<DcsMetadataFormat> registry;
    private final ArchiveService archiveService;
    private final TypedRegistry<MetadataValidator> validatorRegistry;
    private final MetadataFormatPropertiesDao propertiesDao;
    private final DisciplineDAO disciplineDao;
    private final RelationshipService relationshipService;
    private final IdService idService;
    private final int POLL_COUNT = 20;
    private final int POLL_DELAY_MS = 500;
    private final SAXParserFactory saxFactory;
    private final SAXParser saxParser;

    public static final String XSD_FORMAT_ID = "dataconservancy.org:formats:file:xsd:2004";

    public MetadataFormatServiceRegistryImpl(TypedRegistry<DcsMetadataFormat> registry, ArchiveService archiveService,
                                             TypedRegistry<MetadataValidator> validatorRegistry,
                                             RelationshipService relService,
                                             MetadataFormatPropertiesDao propertiesDao,
                                             DisciplineDAO disciplineDao, IdService idService) {

        if (registry == null) {
            throw new IllegalArgumentException("DcsMetadataFormat Registry instance must not be null.");
        }

        if (archiveService == null) {
            throw new IllegalArgumentException("Archive Service must not be null.");
        }

        if (validatorRegistry == null) {
            throw new IllegalArgumentException("MetadataValidator Registry instance must not be null.");
        }

        if (relService == null) {
            throw new IllegalArgumentException("Relationship Service must not be null.");
        }

        if (propertiesDao == null) {
            throw new IllegalArgumentException("MetadataFormatPropertiesDao must not be null.");
        }

        if (disciplineDao == null) {
            throw new IllegalArgumentException("DisciplineDAO must not be null.");
        }

        if (idService == null) {
            throw new IllegalArgumentException("ID Service must not be null.");
        }

        this.registry = registry;
        this.archiveService = archiveService;
        this.validatorRegistry = validatorRegistry;
        this.relationshipService = relService;
        this.propertiesDao = propertiesDao;
        this.disciplineDao = disciplineDao;
        this.idService = idService;

        saxFactory = SAXParserFactory.newInstance();

        try {
            saxParser = saxFactory.newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Unable to obtain a SAXParser instance!  " + e.getMessage(), e);
        }
    }

    @Override
    public Set<DcsMetadataFormat> getMetadataFormats() {
        Set<DcsMetadataFormat> results = new HashSet<DcsMetadataFormat>();
        
        Iterator<RegistryEntry<DcsMetadataFormat>> iter = registry.iterator();
        while (iter.hasNext()) {
            final RegistryEntry<DcsMetadataFormat> next = iter.next();
            if (next == null) {
                log.warn("RegistryEntry<DcsMetadataFormat> was null!");
            } else {
                log.warn("RegistryEntry<DcsMetadataFormat>: " + next.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(next)));
            }
            final DcsMetadataFormat format = next.getEntry();
            if (format == null) {
                log.warn("DcsMetadataFormat was null!");
            } else {
                log.warn("DcsMetadataFormat: " + format.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(format)));
            }
            results.add(format);
        }

        return results;
    }

    @Override
    public Set<DcsMetadataFormat> getMetadataFormats(boolean isActive) {
        Set<DcsMetadataFormat> results = new HashSet<DcsMetadataFormat>();
        MetadataFormatProperties properties;
        DcsMetadataFormat format;
        Iterator<RegistryEntry<DcsMetadataFormat>> iter = registry.iterator();
        while (iter.hasNext()) {
            format = iter.next().getEntry();
            properties = propertiesDao.get(format.getId());
            if (isActive) {
                if (properties == null || properties.isActive()) {
                    results.add(format);
                }
            } else {
                if (properties != null && !properties.isActive()) {
                    results.add(format);
                }
            }
        }

        return results;
    }

    @Override
    public DcsMetadataFormat getMetadataFormat(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Id must not be empty or null.");
        }
        
        DcsMetadataFormat format = null;
        RegistryEntry<DcsMetadataFormat> re = registry.retrieve(id);
        if ( re != null) {
            format = re.getEntry();
        } else {
            Set<RegistryEntry<DcsMetadataFormat>> entries = registry.lookup(id);
            if (!entries.isEmpty()) {
                format = entries.iterator().next().getEntry();
            }
        }
                
        return format;
    }

    @Override
    public Iterator<DcsMetadataFormat> iterator() {
        return new MetadataFormatIterator(registry.iterator());
    }
    
    private class MetadataFormatIterator implements Iterator<DcsMetadataFormat> {

        private Iterator<RegistryEntry<DcsMetadataFormat>> delegate;
        
        public MetadataFormatIterator(Iterator<RegistryEntry<DcsMetadataFormat>> iter) {
            delegate = iter;
        }
        
        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public DcsMetadataFormat next() {
            return delegate.next().getEntry();
        }

        @Override
        public void remove() {
            delegate.remove();            
        }
        
    }

    @Override
    public String addMetadataFormat(final DcsMetadataFormat format) throws BizInternalException {
        
        final ArrayList<String> keys = new ArrayList<String>();
       
        if (format.getSchemes().size() > 0) {
            DcsMetadataScheme masterScheme = format.getSchemes().iterator().next();
            
            if (masterScheme.getSchemaUrl() != null && !masterScheme.getSchemaUrl().isEmpty()) {
                final String includePath = masterScheme.getSchemaUrl().toString().substring(0, masterScheme.getSchemaUrl().toString().lastIndexOf("/")+1);

                final DefaultHandler handler = new DefaultHandler() {
                    public void startElement(String uri, String localName,String qName, 
                                             Attributes attributes) throws SAXException {
                        if (qName.equalsIgnoreCase("xsd:include")) {
                            int index = attributes.getIndex("schemaLocation");
                            
                            if (index != -1) {
                                DcsMetadataScheme scheme = new DcsMetadataScheme();
                                scheme.setName(format.getName());
                                scheme.setSchemaVersion(format.getVersion());
                                keys.add(attributes.getValue(index));
                                scheme.setSchemaUrl(includePath + attributes.getValue(index));
                                scheme.setSource(includePath + attributes.getValue(index));
                                format.addScheme(scheme);
                            }
                        }                 
                    }
                };        
        
                InputStream xsdInputStream;
                try {
                    xsdInputStream = new URL(masterScheme.getSchemaUrl()).openStream();
                    saxParser.parse(xsdInputStream, handler);
                } catch (Exception e) {
                    throw new BizInternalException("Error reading master scheme " + masterScheme.getName() + " for format " + format.getName() + ": " + e.getMessage(), e);
                }

                keys.add(format.getName());
                keys.add(format.getId());
                keys.add(masterScheme.getSchemaUrl());
            }
        }
        
        BasicRegistryEntryImpl<DcsMetadataFormat> newFormatRegistryEntry = new BasicRegistryEntryImpl<DcsMetadataFormat>();
        newFormatRegistryEntry.setEntryType("dataconservancy.types:registry-entry:metadataformat");
        newFormatRegistryEntry.setKeys(keys);
        newFormatRegistryEntry.setEntry(format);
        newFormatRegistryEntry.setDescription("DcsMetadataFormat:" + format.getName());
        newFormatRegistryEntry.setId(format.getId());
        
        //TODO: This is hardcoded to modify the new format to the xml metadata validator registry entry. When we support other types of validatable formats this will need to be updated. -BMB
        RegistryEntry<MetadataValidator> xmlEntry = validatorRegistry.retrieve("dc:metadata:validator/Xml");
        if (xmlEntry != null) {
            xmlEntry.getKeys().add(format.getId());
        }
        
        try {
            String depositID = archiveService.deposit(newFormatRegistryEntry);
            archiveService.pollArchive();
            Status entryStatus = archiveService.getDepositStatus(depositID);
            int count = 0;
            
            while (entryStatus == Status.PENDING && count < POLL_COUNT) {
                try{
                    Thread.sleep(POLL_DELAY_MS);
                } catch (InterruptedException e) {
                    // ignore
                }

                archiveService.pollArchive();
                entryStatus = archiveService.getDepositStatus(depositID);
                ++count;
            }
           
            return depositID;
        } catch (ArchiveServiceException e) {
            throw new BizInternalException("Error depositing new metadata format: " + format.getName() + e.getMessage(), e);
        }
    }

    @Override
    public MetadataFormatProperties getProperties(String dcsMetadataFormatId) {
        MetadataFormatProperties properties;
        try {
            properties = propertiesDao.get(dcsMetadataFormatId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }

        // May as well order them consistently
        final List<String> sortedDisciplines = new ArrayList<String>();
        sortedDisciplines.addAll(relationshipService.getDisciplinesForMetadataFormats(dcsMetadataFormatId));
        Collections.sort(sortedDisciplines);
        properties.setDisciplineIds(sortedDisciplines);
        return properties;
    }

    @Override
    public void setProperties(DcsMetadataFormat dcsMetadataFormat, MetadataFormatProperties properties)
            throws BizInternalException {
        if (dcsMetadataFormat.getId() == null || dcsMetadataFormat.getId().trim().length() == 0) {
            throw new BizInternalException("The supplied format id was null or empty.");
        }

        if (properties.getFormatId() == null || properties.getFormatId().trim().length() == 0) {
            throw new BizInternalException("The supplied properties object has a null or empty format id.");
        }

        if (!dcsMetadataFormat.getId().equals(properties.getFormatId())) {
            throw new BizInternalException("The supplied format id " + dcsMetadataFormat.getId() + " did not match " +
                    "the format id in the properties object " + properties.getFormatId());
        }

        if (dcsMetadataFormat.getId().equals(XSD_FORMAT_ID) && !properties.isActive()) {
            throw new BizInternalException("Format with id <" + XSD_FORMAT_ID + "> cannot be removed.");
        }

        try {
            propertiesDao.get(properties.getFormatId());
            propertiesDao.update(properties);
        } catch (EmptyResultDataAccessException e) {
            propertiesDao.add(properties);
        }

        Set<String> currentDisciplines = relationshipService.
                getDisciplinesForMetadataFormats(dcsMetadataFormat.getId());
        Set<String> disciplinesToSet = new HashSet<String>();
        disciplinesToSet.addAll(((properties.getDisciplineIds() == null) ?
                Collections.<String>emptySet() : properties.getDisciplineIds()));
        Set<String> disciplinesToRemove = new HashSet<String>();

        for (String dId : currentDisciplines) {
            if (!disciplinesToSet.contains(dId)) {
                disciplinesToRemove.add(dId);
            } else {
                disciplinesToSet.remove(dId);
            }
        }

        for (String disciplineId : disciplinesToRemove) {
            Discipline d = disciplineDao.get(disciplineId);
            relationshipService.removeDisciplineFromMetadataFormat(d, dcsMetadataFormat);
        }

        for (String disciplineId : disciplinesToSet) {
            Discipline d = disciplineDao.get(disciplineId);
            relationshipService.addDisciplineToMetadataFormat(d, dcsMetadataFormat);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:
     * <p/>
     * <em>N.B.</em>: if the object identified by {@code businessId} <em>doesn't</em> have any associated metadata
     * formats, an <em>empty</em> {@code Map} will be returned.  That is, {@code Discipline}s will only appear in the
     * {@code Map} if a metadata format is associated with the identified business object.  It is possible that a format
     * will be associated with multiple disciplines, and will appear in the map multiple times, one for each discipline.
     * <p/>
     * If the {@code businessId} resolves to a {@code Project}, {@code DataItem}, or {@code Collection}, then the
     * returned {@code Map} of metadata formats is filtered according to the {@code MetadataFormatProperties} associated
     * with each format.  For example, if the {@code businessId} identifies a {@code Collection}, the properties of each
     * metadata format in the system will be interrogated to see if the format
     * {@link MetadataFormatProperties#appliesToCollection applies at the collection level}.  If yes, the format and
     * its disciplines are placed in the returned {@code Map}.  Similarly, if the {@code businessId} identifies a
     * {@code Project}, the properties of each format are interrogated to see if the format
     * {@link MetadataFormatProperties#appliesToProject applies at the project level}.  The same process is applied to
     * {@code DataItem}s.
     * <p/>
     * If the {@code businessId} resolves to a {@code DataFile}, {@code MetadataFile}, or {@code Person}, all
     * metadata formats will be returned.
     * <p/>
     * If the {@code businessId} resolves to any other type, including {@code null}, a {@code RuntimeException} is
     * thrown.
     *
     * @param businessId the identifier of a business object
     * @return {@code DcsMetadataFormat}s keyed by their {@code Discipline}
     * @throws RuntimeException if {@code businessId} cannot be resolved to a known type
     */
    @Override
    public Map<Discipline, Set<DcsMetadataFormat>> getMetadataFormats(String businessId) {
        final Map<Discipline, Set<DcsMetadataFormat>> results = new HashMap<Discipline, Set<DcsMetadataFormat>>();
        final Class<? extends BusinessObject> type = getType(businessId);

        if (type == null) {
            // The business id couldn't be resolved to a type, just return empty results.
            return results;
        }

        for (Discipline d : disciplineDao.list()) {
            final Set<String> formatIds = relationshipService.getMetadataFormatsForDiscipline(d.getId());
            for (String formatId : formatIds) {

                final DcsMetadataFormat metadataFormat = getMetadataFormat(formatId);

                // If the identified object is a Collection, Project, or Item, we treat them specially
                if (type == Collection.class || type == Project.class || type == DataItem.class) {
                    final MetadataFormatProperties mdfProps = getProperties(formatId);
                    // We only add the Metadata Format if the business properties of the identified object allows
                    if (type == Collection.class && mdfProps.isAppliesToCollection() ||
                            type == Project.class && mdfProps.isAppliesToProject() ||
                            type == DataItem.class && mdfProps.isAppliesToItem()) {
                        if (results.containsKey(d)) {
                            results.get(d).add(metadataFormat);
                        } else {
                            Set<DcsMetadataFormat> s = new HashSet<DcsMetadataFormat>();
                            s.add(metadataFormat);
                            results.put(d, s);
                        }
                    }

                // Otherwise, we just add the Metadata Format
                } else {
                    if (results.containsKey(d)) {
                        results.get(d).add(metadataFormat);
                    } else {
                        Set<DcsMetadataFormat> s = new HashSet<DcsMetadataFormat>();
                        s.add(metadataFormat);
                        results.put(d, s);
                    }
                }
            }
        }


        return results;
    }

    @Override
    public Set<DcsMetadataFormat> getMetadataFormatsForDiscipline(String disciplineId) {
        Set<String> metadataURIs = relationshipService.getMetadataFormatsForDiscipline(disciplineId);
        Set<DcsMetadataFormat> metadataFormats = new HashSet<DcsMetadataFormat>();
        for (String uri : metadataURIs) {
            if (uri != null && !uri.isEmpty()) {
                DcsMetadataFormat format = this.getMetadataFormat(uri);
                if (format != null) {
                    metadataFormats.add(format);
                }
            }
        }
        return metadataFormats;
    }

    @Override
    public Set<DcsMetadataFormat> getMetadataFormatsForDiscipline(String disciplineId, boolean isActiveStatus) {
        Set<String> metadataURIs = relationshipService.getMetadataFormatsForDiscipline(disciplineId);
        Set<DcsMetadataFormat> metadataFormats = new HashSet<DcsMetadataFormat>();
        MetadataFormatProperties formatProperties;
        for (String uri : metadataURIs) {
            if (uri != null && !uri.isEmpty()) {
                DcsMetadataFormat format = this.getMetadataFormat(uri);
                if (format != null) {
                    formatProperties = this.getProperties(format.getId());
                    if (isActiveStatus == formatProperties.isActive()) {
                        metadataFormats.add(format);
                    }
                }
            }
        }
        return metadataFormats;
    }

    /**
     * Returns the Java type of the identified business object
     *
     * @param businessId a business identifier that may resolve a business object
     * @return the Java type of the identified object, or null if the identifier cannot be resolved
     * @throws RuntimeException if the resolved identifier's type is null, or the identifier's type is not a business
     *                          object type.
     */
    Class<? extends BusinessObject> getType(String businessId) {
        Identifier id = null;
        try {
            id = idService.fromUrl(new URL(businessId));
        } catch (IdentifierNotFoundException e) {
            try {
                id = idService.fromUid(businessId);
            } catch (IdentifierNotFoundException e1) {
                // ignore
            }
        } catch (MalformedURLException e) {
            try {
                id = idService.fromUid(businessId);
            } catch (IdentifierNotFoundException e1) {
                // ignore
            }
        }

        if (id == null) {
            log.debug("Unable to resolve {} to a business object", businessId);
            return null;
        }

        if (id.getType() == null) {
            throw new RuntimeException("Identifier " + businessId + " has no object type!");
        }

        switch (Types.valueOf(id.getType())) {
            case COLLECTION:
                return Collection.class;
            case DATA_FILE:
                return DataFile.class;
            case DATA_SET:
                return DataItem.class;
            case METADATA_FILE:
                return MetadataFile.class;
            case PERSON:
                return Person.class;
            case PROJECT:
                return Project.class;
            default:
                throw new RuntimeException("Unknown business object type " + id.getType());
        }
    }
}
