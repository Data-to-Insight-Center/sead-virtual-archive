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

import java.io.PrintWriter;

import java.net.URL;
import java.util.*;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.dao.DisciplineDAO;
import org.dataconservancy.ui.dao.MetadataFormatPropertiesDao;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.model.MetadataFormatProperties;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.dataconservancy.mhf.validation.api.MetadataValidator;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class MetadataFormatServiceRegistryImplTest extends BaseUnitTest {

    @Autowired
    @Qualifier("formatRegistryImpl")
    private TypedRegistry<DcsMetadataFormat> registry;

    @Autowired
    @Qualifier("archiveService")
    private ArchiveService archiveService;
    
    @Autowired
    @Qualifier("metadataValidatorRegistry")
    private TypedRegistry<MetadataValidator> validatorRegistry;

    @Autowired
    private RelationshipService relService;

    @Autowired
    private MetadataFormatPropertiesDao mdfPropDao;

    @Autowired
    private DisciplineDAO disciplineDao;

    private IdService idService = mock(IdService.class);

    private MetadataFormatServiceRegistryImpl underTest;

    @Before
    public void setUp() {
        underTest = new MetadataFormatServiceRegistryImpl(registry, archiveService, validatorRegistry, relService,
                mdfPropDao, disciplineDao, idService);
    }

    @Test
    public void testGetMetadataFormatById() throws Exception {
        assertNotNull(underTest.getMetadataFormat("dataconservancy.org:registry:metadata-format:entry:id:1"));
    }

    @Test
    public void testGetNonExistantFormatById() throws Exception {
        final String nonexistantId = "FOO";
        assertNull(registry.retrieve(nonexistantId));
        assertNull(underTest.getMetadataFormat(nonexistantId));
    }

    @Test
    public void testGetMetadataFormat() throws Exception {
        assertNotNull(underTest.getMetadataFormats());
        assertEquals(2, underTest.getMetadataFormats().size());
    }

    @Test
    public void testIterator() throws Exception {
        Iterator<DcsMetadataFormat> itr = underTest.iterator();
        int count = 0;
        while (itr.hasNext()) {
            itr.next();
            count++;
        }

        assertEquals(2, count);
    }
    
    @Test
    public void testAddMetadataFormat() throws Exception {
        DcsMetadataFormat format = new DcsMetadataFormat();
        format.setName("test format");
        format.setVersion("1.0");
        format.setId("format:test");
        
        java.io.File tmp = java.io.File.createTempFile("testFile", null);
        tmp.deleteOnExit();
        
        PrintWriter out = new PrintWriter(tmp);
        
        out.println("metadata");
        out.close();
        
        DcsMetadataScheme scheme = new DcsMetadataScheme();
        scheme.setName("test scheme");
        scheme.setSchemaUrl("http://dataconservancy.org/schemas/bop/1.0");
        scheme.setSource(tmp.toURI().toURL().toExternalForm());
        scheme.setSchemaVersion("1.0");
        format.addScheme(scheme);
        
        String archiveId = underTest.addMetadataFormat(format);
        
        assertNotNull(archiveId);
        
        Status depositStatus = archiveService.getDepositStatus(archiveId);
        assertTrue(depositStatus == Status.DEPOSITED);      
    }

    /**
     * A simple test which adds minimal business properties for a DcsMetadataFormat, and insures that they can be
     * retrieved.
     *
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    public void testAddMetadataFormatProperties() throws Exception {
        final String formatId = "formatId";
        final DcsMetadataFormat format = new DcsMetadataFormat();
        format.setId(formatId);
        final MetadataFormatProperties props = new MetadataFormatProperties();
        props.setFormatId(formatId);

        underTest.setProperties(format, props);

        assertEquals(props, underTest.getProperties(formatId));
    }

    /**
     * Part of a set of tests that insures that both the DcsMetadataFormat and the MetadataFormatProperties have
     * identifiers that are not null, not empty, and are equal to each other.
     *
     * @throws Exception
     */
    @Test(expected = BizInternalException.class)
    public void testAddMetadataPropertiesWithNoFormatId() throws Exception {
        final String formatId = "formatId";
        final DcsMetadataFormat format = new DcsMetadataFormat();
        format.setId(formatId);
        final MetadataFormatProperties props = new MetadataFormatProperties();

        underTest.setProperties(format, props);
    }

    /**
     * Part of a set of tests that insures that both the DcsMetadataFormat and the MetadataFormatProperties have
     * identifiers that are not null, not empty, and are equal to each other.
     *
     * @throws Exception
     */
    @Test(expected = BizInternalException.class)
    public void testAddMetadataPropertiesWithMismatchedFormatId() throws Exception {
        final String formatId = "formatId";
        final DcsMetadataFormat format = new DcsMetadataFormat();
        format.setId(formatId);
        final MetadataFormatProperties props = new MetadataFormatProperties();
        props.setFormatId(formatId + "foo");

        underTest.setProperties(format, props);
    }

    /**
     * Part of a set of tests that insures that both the DcsMetadataFormat and the MetadataFormatProperties have
     * identifiers that are not null, not empty, and are equal to each other.
     *
     * @throws Exception
     */
    @Test(expected = BizInternalException.class)
    public void testAddMetadataPropertiesWithAFormatWithNoId() throws Exception {
        final String formatId = "formatId";
        final DcsMetadataFormat format = new DcsMetadataFormat();
        final MetadataFormatProperties props = new MetadataFormatProperties();
        props.setFormatId(formatId);

        underTest.setProperties(format, props);
    }

    /**
     * Insures that a MetadataFormatProperties object with flags can be persisted and retrieved by the service.
     *
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    public void testAddMetadataPropertiesWithFlags() throws Exception {
        final String formatId = "formatId";
        final DcsMetadataFormat format = new DcsMetadataFormat();
        final MetadataFormatProperties props = new MetadataFormatProperties();
        format.setId(formatId);
        props.setFormatId(formatId);

        assertFalse(props.isAppliesToCollection());
        props.setAppliesToCollection(true);

        assertFalse(props.isValidates());
        props.setValidates(true);

        underTest.setProperties(format, props);
        assertEquals(props, underTest.getProperties(formatId));
    }

    /**
     * Insures that a MetadataFormatProperties object with flags and disciplines can be persisted and retrieved by the
     * service.
     *
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    public void testAddMetadataPropertiesWithFlagsAndDisciplineIds() throws Exception {
        final String formatId = "formatId";
        final DcsMetadataFormat format = new DcsMetadataFormat();
        final MetadataFormatProperties props = new MetadataFormatProperties();
        format.setId(formatId);
        props.setFormatId(formatId);

        assertFalse(props.isAppliesToCollection());
        props.setAppliesToCollection(true);

        assertFalse(props.isValidates());
        props.setValidates(true);

        assertTrue(props.getDisciplineIds().isEmpty());
        assertFalse(disciplineDao.list().isEmpty());
        for (Discipline d : disciplineDao.list()) {
            props.getDisciplineIds().add(d.getId());
        }

        // Sort the list so the equals assertion passes below.
        Collections.sort(props.getDisciplineIds());

        underTest.setProperties(format, props);
        assertEquals(props, underTest.getProperties(formatId));
    }

    /**
     * Insures that a MetadataFormatProperties object with flags and disciplines can be persisted and retrieved by the
     * service.
     *
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    public void testAddMetadataPropertiesSetsDisciplineIds() throws Exception {
        final String formatId = "formatId";
        final DcsMetadataFormat format = new DcsMetadataFormat();
        final MetadataFormatProperties props = new MetadataFormatProperties();
        final List<Discipline> disciplineList = disciplineDao.list();
        format.setId(formatId);
        props.setFormatId(formatId);

        assertTrue(props.getDisciplineIds().isEmpty());
        assertTrue(disciplineList.size() > 1);
        for (Discipline d : disciplineList) {
            props.getDisciplineIds().add(d.getId());
        }

        underTest.setProperties(format, props);
        Collections.sort(props.getDisciplineIds());
        assertEquals(props.getDisciplineIds(), underTest.getProperties(formatId).getDisciplineIds());

        // Now, set the properties again, but with only a single discipline
        props.getDisciplineIds().clear();
        props.getDisciplineIds().add(disciplineList.get(0).getId());
        underTest.setProperties(format, props);
        assertEquals(props.getDisciplineIds(), underTest.getProperties(formatId).getDisciplineIds());
    }

    /**
     * Insures that when looking up metadata formats for an unresolvable business id that no metadata formats are
     * returned.
     *
     * @throws Exception
     */
    @Test
    public void testGetDisciplinesAndFormatsForUnresolvableId() throws Exception {
        final String id = "foo:bar";

        // The id service doesn't know about the id
        assertNull(idService.fromUid(id));

        // So the getType method should return null, because the type of the identifier is unknown
        assertNull(underTest.getType(id));

        // Because the id couldn't be resolved to a type, the empty map is returned
        assertTrue(underTest.getMetadataFormats(id).isEmpty());
    }

    /**
     * Insures that when a identifier with an unknown type (to the MetadataFormatServiceRegistryImpl#getType method) is
     * resolved, that a RuntimeException is thrown.  This is different from
     * {@link #testGetDisciplinesAndFormatsForUnresolvableId()}: in this test, the identifier is resolvable.  In the
     * other test, the identifier isn't resolvable.
     *
     * @throws Exception
     */
    @Test
    public void testGetDisciplinesAndFormatsForAnUnknownBusinessObjectType() throws Exception {
        final String id = "id:contactInfo";
        final Types type = Types.CONTACT_INFO;

        when(idService.fromUid(id)).thenReturn(new Identifier() {
            @Override
            public URL getUrl() {
                return null; // don't care
            }

            @Override
            public String getUid() {
                return id;
            }

            @Override
            public String getType() {
                // A type that is unknown to the MetadataFormatRegistryServiceImpl#getType method.
                return type.getTypeName();
            }
        });

        try {
            // Throws a runtime exception because the type of the identifier (mocked above) is "unknown" to the
            // service.  Associating metadata formats with a ContactInfo object makes no sense.
            underTest.getType(id);
            fail("Expected a RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }

        try {
            underTest.getMetadataFormats(id);
            fail("Expected a RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }

        verify(idService, atLeastOnce()).fromUid(id);
    }

    /**
     * Insures that when a identifier with a null type (to the MetadataFormatServiceRegistryImpl#getType method) is
     * resolved, that a RuntimeException is thrown.  That is, a null type is treated the same a an unknown type.
     *
     * @throws Exception
     */
    @Test
    public void testGetDisciplinesAndFormatsForNullBusinessObjectType() throws Exception {
        final String id = "id:nullType";

        when(idService.fromUid(id)).thenReturn(new Identifier() {
            @Override
            public URL getUrl() {
                return null; // don't care
            }

            @Override
            public String getUid() {
                return id;
            }

            @Override
            public String getType() {
                // A null type (considered to be unknown to the MetadataFormatRegistryServiceImpl#getType method).
                return null; // on purpose
            }
        });

        try {
            // Throws a runtime exception because a null identifier is "unknown" to the service
            underTest.getType(id);
            fail("Expected a RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }

        try {
            underTest.getMetadataFormats(id);
            fail("Expected a RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }

        verify(idService, atLeastOnce()).fromUid(id);
    }


    /**
     * Insures that when metadata formats for a collection are requested, that the business properties associated with
     * the colection's metadata formats are adhered to (that is, the flags of each DcsMetadataFormat
     * MetadataFormatProperties are obeyed).
     *
     * @throws Exception
     */
    @Test
    public void testGetDisciplinesAndFormatsForCollectionBusinessObjectTypeDoesntApply() throws Exception {
        final RelationshipService relService = mock(RelationshipService.class);
        final MetadataFormatPropertiesDao mdfPropDao = mock(MetadataFormatPropertiesDao.class);

        underTest = new MetadataFormatServiceRegistryImpl(registry, archiveService, validatorRegistry, relService,
                        mdfPropDao, disciplineDao, idService);

        final String id = "id:collection";
        final Types type = Types.COLLECTION;

        when(idService.fromUid(id)).thenReturn(new Identifier() {
            @Override
            public URL getUrl() {
                return null; // don't care
            }

            @Override
            public String getUid() {
                return id;
            }

            @Override
            public String getType() {
                // A Collection
                return type.getTypeName();
            }
        });

        // Each Discipline will be associated with all MetadataFormats
        final Set<String> formatIds = new HashSet<String>();
        for (DcsMetadataFormat format : underTest.getMetadataFormats()) {
            formatIds.add(format.getId());
        }
        assertTrue(formatIds.size() > 0);
        when(relService.getMetadataFormatsForDiscipline(anyString())).thenReturn(formatIds);

        // When the properties object of each format is looked up, return a properties object that doesn't apply to
        // collections
        for (String formatId : formatIds) {
            final MetadataFormatProperties prop = new MetadataFormatProperties();
            prop.setFormatId(formatId);
            prop.setAppliesToCollection(false);
            when(mdfPropDao.get(formatId)).thenReturn(prop);
        }

        // The service "knows" the type of ID
        assertEquals(org.dataconservancy.ui.model.Collection.class, underTest.getType(id));

        // Attempt to get the metadata formats for a Collection.  Since the mocked mdfPropDao is returning properties
        // that don't apply to Collections, this should be an empty Map.
        final Map<Discipline, Set<DcsMetadataFormat>> formatsAndDisciplines = underTest.getMetadataFormats(id);
        assertEquals(0, formatsAndDisciplines.size());

        verify(idService, atLeastOnce()).fromUid(id);
        verify(relService, atLeastOnce()).getMetadataFormatsForDiscipline(anyString());
        for (String formatId : formatIds) {
            verify(mdfPropDao, atLeastOnce()).get(formatId);
        }
    }

    /**
     * Insures that when metadata formats for a collection are requested, that the business properties associated with
     * the colection's metadata formats are adhered to (that is, the flags of each DcsMetadataFormat
     * MetadataFormatProperties are obeyed).
     *
     * @throws Exception
         */
    @Test
    public void testGetDisciplinesAndFormatsForCollectionBusinessObjectType() throws Exception {
        final RelationshipService relService = mock(RelationshipService.class);
        final MetadataFormatPropertiesDao mdfPropDao = mock(MetadataFormatPropertiesDao.class);

        underTest = new MetadataFormatServiceRegistryImpl(registry, archiveService, validatorRegistry, relService,
                        mdfPropDao, disciplineDao, idService);

        final String id = "id:collection";
        final Types type = Types.COLLECTION;

        when(idService.fromUid(id)).thenReturn(new Identifier() {
            @Override
            public URL getUrl() {
                return null; // don't care
            }

            @Override
            public String getUid() {
                return id;
            }

            @Override
            public String getType() {
                // A Collection
                return type.getTypeName();
            }
        });

        // Each Discipline will be associated with all MetadataFormats
        final Set<String> formatIds = new HashSet<String>();
        for (DcsMetadataFormat format : underTest.getMetadataFormats()) {
            formatIds.add(format.getId());
        }
        assertTrue(formatIds.size() > 0);
        when(relService.getMetadataFormatsForDiscipline(anyString())).thenReturn(formatIds);

        // When the properties object of each format is looked up, return a properties object that doesn't apply to
        // collections
        for (String formatId : formatIds) {
            final MetadataFormatProperties prop = new MetadataFormatProperties();
            prop.setFormatId(formatId);
            prop.setAppliesToCollection(true);
            when(mdfPropDao.get(formatId)).thenReturn(prop);
        }

        // The service "knows" the type of ID
        assertEquals(org.dataconservancy.ui.model.Collection.class, underTest.getType(id));

        // We expect to get back a Map of every discipline mapped to every format
        final Map<Discipline, Set<DcsMetadataFormat>> expectedResults = new HashMap<Discipline, Set<DcsMetadataFormat>>();
        for (Discipline d : disciplineDao.list()) {
            expectedResults.put(d, new HashSet<DcsMetadataFormat>());
            for (DcsMetadataFormat f : underTest.getMetadataFormats()) {
                expectedResults.get(d).add(f);
            }
        }

        // Attempt to get the metadata formats for a Collection.  Since the mocked mdfPropDao is returning properties
        // that *do* apply to Collections, and since we've mocked the relService to map all disciplines to all formats,
        // adn since the mdfDao is mocked to apply each format to the collection, we expect all disciplines to be mapped
        // to all collections.
        final Map<Discipline, Set<DcsMetadataFormat>> actualResults = underTest.getMetadataFormats(id);
        assertEquals(expectedResults, actualResults);

        verify(idService, atLeastOnce()).fromUid(id);
        verify(relService, atLeastOnce()).getMetadataFormatsForDiscipline(anyString());
        for (String formatId : formatIds) {
            verify(mdfPropDao, atLeastOnce()).get(formatId);
        }
    }

    /**
     * Insures that when a identifier with a known (to the MetadataFormatServiceRegistryImpl#getType method) type is
     * resolved, the proper formats come back.
     *
     * @throws Exception
     */
    @Test
    public void testGetDisciplinesAndFormatsForKnownBusinessObjectType() throws Exception {
        RelationshipService relService = mock(RelationshipService.class);

        underTest = new MetadataFormatServiceRegistryImpl(registry, archiveService, validatorRegistry, relService,
                mdfPropDao, disciplineDao, idService);

        final String id = "id:knownType";
        final Types type = Types.PERSON;

        when(idService.fromUid(id)).thenReturn(new Identifier() {
            @Override
            public URL getUrl() {
                return null; // don't care
            }

            @Override
            public String getUid() {
                return id;
            }

            @Override
            public String getType() {
                // A Person, considered "known" to the MetadataFormatRegistryServiceImpl#getType method, but it isn't
                // a "special" type like a Project, Collection, or DataItem.
                return type.getTypeName();
            }
        });

        // Each Discipline will be associated with all MetadataFormats
        final Set<String> formatIds = new HashSet<String>();
        for (DcsMetadataFormat format : underTest.getMetadataFormats()) {
            formatIds.add(format.getId());
        }
        assertTrue(formatIds.size() > 0);
        when(relService.getMetadataFormatsForDiscipline(anyString())).thenReturn(formatIds);

        // The service "knows" the type of ID
        assertEquals(Person.class, underTest.getType(id));

        // known types with no restrictions (that is, the type isn't a Collection, Project, or DataItem) should get all
        // metadata formats
        final Map<Discipline, Set<DcsMetadataFormat>> formatsAndDisciplines = underTest.getMetadataFormats(id);

        // Merge all of the retrieved DcsMetadataFormats into a single Set
        final Set<DcsMetadataFormat> formats = new HashSet<DcsMetadataFormat>();
        for (Set<DcsMetadataFormat> s : formatsAndDisciplines.values()) {
            formats.addAll(s);
        }

        // Insure that all the DcsMetadataFormats were returned.  We don't really care what discipline they were
        // associated to.
        assertEquals(formats, underTest.getMetadataFormats());

        verify(idService, atLeastOnce()).fromUid(id);
        verify(relService, atLeastOnce()).getMetadataFormatsForDiscipline(anyString());
    }

    /**
     * Insures that when metadata formats for a collection are requested, that the business properties associated with
     * the colection's metadata formats are adhered to (that is, the flags of each DcsMetadataFormat
     * MetadataFormatProperties are obeyed).
     *
     * @throws Exception
     */
    @Test
    public void testGetDisciplinesAndFormatsForCollectionBusinessObjectTypeSomeFormats() throws Exception {
        final RelationshipService relService = mock(RelationshipService.class);
        final MetadataFormatPropertiesDao mdfPropDao = mock(MetadataFormatPropertiesDao.class);

        underTest = new MetadataFormatServiceRegistryImpl(registry, archiveService, validatorRegistry, relService,
                mdfPropDao, disciplineDao, idService);

        final String id = "id:collection";
        final Types type = Types.COLLECTION;

        when(idService.fromUid(id)).thenReturn(new Identifier() {
            @Override
            public URL getUrl() {
                return null; // don't care
            }

            @Override
            public String getUid() {
                return id;
            }

            @Override
            public String getType() {
                // A Collection
                return type.getTypeName();
            }
        });

        // Each Discipline will be associated with all MetadataFormats
        final Set<String> formatIds = new HashSet<String>();
        for (DcsMetadataFormat format : underTest.getMetadataFormats()) {
            formatIds.add(format.getId());
        }
        assertTrue(formatIds.size() > 0);
        when(relService.getMetadataFormatsForDiscipline(anyString())).thenReturn(formatIds);

        // When the properties object of a certain format is looked up, return a properties object that does apply to
        // collections, and the rest don't.
        String yesFormat = null;
        for (String formatId : formatIds) {
            final MetadataFormatProperties prop = new MetadataFormatProperties();
            prop.setFormatId(formatId);
            when(mdfPropDao.get(formatId)).thenReturn(prop);

            if (yesFormat == null) {
                yesFormat = formatId;
                prop.setAppliesToCollection(true);
            } else {
                prop.setAppliesToCollection(false);
            }
        }

        // The service "knows" the type of ID
        assertEquals(org.dataconservancy.ui.model.Collection.class, underTest.getType(id));

        // We expect to get back a Map of every discipline mapped to to the format identified by 'yesFormat'
        final Map<Discipline, Set<DcsMetadataFormat>> expectedResults = new HashMap<Discipline, Set<DcsMetadataFormat>>();
        for (Discipline d : disciplineDao.list()) {
            for (DcsMetadataFormat f : underTest.getMetadataFormats()) {
                if (f.getId().equals(yesFormat)) {
                    if (!expectedResults.containsKey(d)) {
                        expectedResults.put(d, new HashSet<DcsMetadataFormat>());
                    }
                    expectedResults.get(d).add(f);
                }
            }
        }

        // Attempt to get the metadata formats for a Collection.  Since the mocked mdfPropDao is returning properties
        // that *do* apply to Collections, and since we've mocked the relService to map all disciplines to all formats,
        // adn since the mdfDao is mocked to apply each format to the collection, we expect all disciplines to be mapped
        // to all collections.
        final Map<Discipline, Set<DcsMetadataFormat>> actualResults = underTest.getMetadataFormats(id);
        assertEquals(expectedResults, actualResults);

        verify(idService, atLeastOnce()).fromUid(id);
        verify(relService, atLeastOnce()).getMetadataFormatsForDiscipline(anyString());
        for (String formatId : formatIds) {
            verify(mdfPropDao, atLeastOnce()).get(formatId);
        }
    }

    /**
     * Ensures that attempts to set record for XSD format will cause exceptions
     * @throws BizInternalException
     */
    @Test (expected = BizInternalException.class)
    public void testXSDCannotBeDeactivated() throws BizInternalException {
        MetadataFormatProperties forbiddenFormat = underTest.getProperties(MetadataFormatServiceRegistryImpl.XSD_FORMAT_ID);
        DcsMetadataFormat dcsMetadataFormat = underTest.getMetadataFormat(MetadataFormatServiceRegistryImpl.XSD_FORMAT_ID);
        assertNotNull(forbiddenFormat);
        forbiddenFormat.setActive(false);
        underTest.setProperties(dcsMetadataFormat, forbiddenFormat);
    }


    /**
     * Ensures that once a metadata format is marked as inactive, it would no longer be returned on requests for active
     * metadata formats.
     * @throws Exception
     */
    @Test
    public void testDeactivateFormats() throws Exception{
        String idToDeactivate = "dataconservancy.org:formats:file:metadata:fgdc:xml";
        MetadataFormatProperties properties = underTest.getProperties(idToDeactivate);
        DcsMetadataFormat dcsMetadataFormat = underTest.getMetadataFormat(idToDeactivate);
        assertNotNull(properties);
        assertNotNull(dcsMetadataFormat);

        properties.setActive(false);
        underTest.setProperties(dcsMetadataFormat, properties);

        Set<DcsMetadataFormat> formats = underTest.getMetadataFormats(true);
        assertFalse(formats.contains(dcsMetadataFormat));
    }


    /**
     * Test getting all metadata format for discipline
     */
    @Test
    public void testGetMetadataFormatForDiscipline() throws Exception {
        final RelationshipService relService = mock(RelationshipService.class);
        final MetadataFormatPropertiesDao mdfPropDao = mock(MetadataFormatPropertiesDao.class);

        //Set up a new metadata format service impl
        underTest = new MetadataFormatServiceRegistryImpl(registry, archiveService, validatorRegistry, relService,
                mdfPropDao, disciplineDao, idService);

        final String id = "id:collection";
        final Types type = Types.COLLECTION;

        //mock call to idservice
        when(idService.fromUid(id)).thenReturn(new Identifier() {
            @Override
            public URL getUrl() {
                return null; // don't care
            }

            @Override
            public String getUid() {
                return id;
            }

            @Override
            public String getType() {
                // A Collection
                return type.getTypeName();
            }
        });

        //obtain all format ids from the registry.
        final Set<String> expectedFormatIds = new HashSet<String>();
        for (DcsMetadataFormat format : underTest.getMetadataFormats()) {
            expectedFormatIds.add(format.getId());
        }

        assertTrue(expectedFormatIds.size() > 0);
        when(relService.getMetadataFormatsForDiscipline(anyString())).thenReturn(expectedFormatIds);

        List<String> activeFormatIds = new ArrayList<String>();
        List<String> inActiveFormatIds = new ArrayList<String>();

        int i = 0;
        // When the properties object of each format is looked up, return a properties object that doesn't apply to
        // collections
        for (String formatId : expectedFormatIds) {
            final MetadataFormatProperties prop = new MetadataFormatProperties();
            prop.setFormatId(formatId);
            prop.setAppliesToCollection(true);
            if (i % 2 == 0) {
                prop.setActive(false);
                inActiveFormatIds.add(prop.getFormatId());
            } else {
                activeFormatIds.add(prop.getFormatId());
            }
            when(mdfPropDao.get(formatId)).thenReturn(prop);
            i++;
        }

        //get a discipline from the dao
        Discipline discipline = disciplineDao.list().get(0);
        assertNotNull(discipline);

        Set<DcsMetadataFormat> actualFormats = null;
        actualFormats = underTest.getMetadataFormatsForDiscipline(discipline.getId());
        boolean foundActiveFormatIds = false;
        boolean foundInactiveFormatIds = false;

        for (DcsMetadataFormat format: actualFormats) {
            assertTrue(expectedFormatIds.contains(format.getId()));
            if (activeFormatIds.contains(format.getId())) {
                foundActiveFormatIds = true;
            } else if (inActiveFormatIds.contains(format.getId())) {
                foundInactiveFormatIds = true;
            }
        }

        assertTrue(foundActiveFormatIds);
        assertTrue(foundInactiveFormatIds);
    }

    /**
     * Test getting metadata format for discipline by active status
     */
    @Test
    public void testGetMetadataFormatsForDisciplineByActiveStatus() throws Exception {
        final RelationshipService relService = mock(RelationshipService.class);
        final MetadataFormatPropertiesDao mdfPropDao = mock(MetadataFormatPropertiesDao.class);

        //Set up a new metadata format service impl
        underTest = new MetadataFormatServiceRegistryImpl(registry, archiveService, validatorRegistry, relService,
                mdfPropDao, disciplineDao, idService);

        final String id = "id:collection";
        final Types type = Types.COLLECTION;

        //mock call to idservice
        when(idService.fromUid(id)).thenReturn(new Identifier() {
            @Override
            public URL getUrl() {
                return null; // don't care
            }

            @Override
            public String getUid() {
                return id;
            }

            @Override
            public String getType() {
                // A Collection
                return type.getTypeName();
            }
        });

        //obtain all format ids from the registry.
        final Set<String> allFormatIds = new HashSet<String>();
        for (DcsMetadataFormat format : underTest.getMetadataFormats()) {
            allFormatIds.add(format.getId());
        }

        assertTrue(allFormatIds.size() > 0);
        when(relService.getMetadataFormatsForDiscipline(anyString())).thenReturn(allFormatIds);

        List<String> activeFormatIds = new ArrayList<String>();
        List<String> inActiveFormatIds = new ArrayList<String>();

        int i = 0;
        // When the properties object of each format is looked up, return a properties object that doesn't apply to
        // collections
        for (String formatId : allFormatIds) {
            final MetadataFormatProperties prop = new MetadataFormatProperties();
            prop.setFormatId(formatId);
            prop.setAppliesToCollection(true);
            if (i % 2 == 0) {
                prop.setActive(false);
                inActiveFormatIds.add(prop.getFormatId());
            } else {
                activeFormatIds.add(prop.getFormatId());
            }
            when(mdfPropDao.get(formatId)).thenReturn(prop);
            i++;
        }

        //get a discipline from the dao
        Discipline discipline = disciplineDao.list().get(0);
        assertNotNull(discipline);

        //test getting active formats for discipline only
        Set<DcsMetadataFormat> actualFormats = null;
        actualFormats = underTest.getMetadataFormatsForDiscipline(discipline.getId(), true);
        boolean foundActiveFormatIds = false;
        boolean foundInactiveFormatIds = false;

        for (DcsMetadataFormat format: actualFormats) {
            if (activeFormatIds.contains(format.getId())) {
                foundActiveFormatIds = true;
            } else if (inActiveFormatIds.contains(format.getId())) {
                foundInactiveFormatIds = true;
            }
        }

        assertTrue(foundActiveFormatIds);
        assertFalse(foundInactiveFormatIds);

        //test getting in-active formats for discipline only
        actualFormats = underTest.getMetadataFormatsForDiscipline(discipline.getId(), false);
        foundActiveFormatIds = false;
        foundInactiveFormatIds = false;

        for (DcsMetadataFormat format: actualFormats) {
            if (activeFormatIds.contains(format.getId())) {
                foundActiveFormatIds = true;
            } else if (inActiveFormatIds.contains(format.getId())) {
                foundInactiveFormatIds = true;
            }
        }

        assertFalse(foundActiveFormatIds);
        assertTrue(foundInactiveFormatIds);
    }
}
