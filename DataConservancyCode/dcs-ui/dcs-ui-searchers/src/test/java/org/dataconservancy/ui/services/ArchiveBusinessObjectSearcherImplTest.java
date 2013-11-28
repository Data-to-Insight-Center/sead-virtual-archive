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

import java.net.MalformedURLException;
import java.net.URL;

import org.dataconservancy.ui.profile.CollectionProfile;
import org.dataconservancy.ui.profile.MetadataFileProfile;
import org.junit.Before;

import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.ui.dcpmap.AbstractVersioningMapper;
import org.dataconservancy.ui.profile.DataItemProfile;
import org.dataconservancy.ui.test.support.model.BaseModelTest;
import org.dataconservancy.ui.util.SolrQueryUtil;

import static org.dataconservancy.ui.dcpmap.AbstractVersioningMapper.ROOT_DELIVERABLE_UNIT_TYPE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public abstract class ArchiveBusinessObjectSearcherImplTest extends BaseModelTest {

    /**
     * Used to build DCS Entities
     */
    DcsXstreamStaxModelBuilder builder = new DcsXstreamStaxModelBuilder();

    /**
     * DU Type for Root DU objects
     */
    static final String ROOT_DU_TYPE = ROOT_DELIVERABLE_UNIT_TYPE;

    /**
     * The Business ID of the Collection that we are searching for
     */
    static final String COLLECTION_BIZ_ID = "http://foo.com/collection/1234";

    /**
     * The Business ID of the DataItem that we are searching for
     */
    static final String DATAITEM_BIZ_ID = "http://foo.com/item/5678";
    
    /**
     * The Business ID of the Metadata File that we are searching for
     */
    static final String METADATA_FILE_BIZ_ID = "http://foo.com/file/9012";

    /**
     * This is the query normally performed by {@link ArchiveBusinessObjectSearcherImpl#findLatestState(String)}
     * when searching the archive for a Collection.
     */
    static final String COLLECTION_SEARCH_QUERY = "(" + SolrQueryUtil.createLiteralQuery("AND", "entityType",
            "DeliverableUnit", "former", COLLECTION_BIZ_ID, "type", ROOT_DU_TYPE) + ") OR (" +
            SolrQueryUtil.createLiteralQuery("AND", "entityType",
            "DeliverableUnit", "former", COLLECTION_BIZ_ID, "type", CollectionProfile.STATE_DU_TYPE) + ")";

    /**
     * This is the query normally performed by {@link ArchiveBusinessObjectSearcherImpl#findLatestState(String)}
     * when searching the archive for a DataItem.
     */
    static final String DATAITEM_SEARCH_QUERY = SolrQueryUtil.createLiteralQuery("AND", "entityType",
            "DeliverableUnit", "former", DATAITEM_BIZ_ID, "type", DataItemProfile.DATASET_TYPE);

    /**
     * This is the query normally performed by {@link ArchiveBusinessObjectSearcherImpl#findLatestState(String)}
     * when searching the archive for a MetadataFile.
     */
    static final String METADATA_FILE_SEARCH_QUERY = "(" + SolrQueryUtil.createLiteralQuery("AND", "entityType",
            "DeliverableUnit", "former", METADATA_FILE_BIZ_ID, "type", ROOT_DU_TYPE) + ") OR (" +
            SolrQueryUtil.createLiteralQuery("AND", "entityType",
            "DeliverableUnit", "former", METADATA_FILE_BIZ_ID, "type", MetadataFileProfile.STATE_DU_TYPE) + ")";

    /**
     * These are query parameters performed by {@link BaseArchiveSearcher#performSearch(String)}
     */
    static final Integer MAX_RESULTS = Integer.MAX_VALUE;

    /**
     * These are query parameters performed by {@link BaseArchiveSearcher#performSearch(String)}
     */
    static final Integer RESULT_OFFSET = 0;

    /**
     * The first (version 1) of our Collection State DU in our object graph.  It has {@link #COLLECTION_ROOT_DU} as a
     * parent.
     * <p/>
     * Our object graph:
     * Root DU <-- State DU (version 1)
     * Root DU <-- State DU (version 2)
     *
     * @see #COLLECTION_BIZ_ID
     * @see #COLLECTION_ROOT_DU
     * @see #COLLECTION_STATE_DU_WITH_PARENT
     * @see #COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR
     */
    static final String COLLECTION_STATE_DU_WITH_PARENT = "<DeliverableUnit id=\"d8001494-ecc8-4761-8ba6-17e309bca70e\">\n" +
            "      <parent ref=\"6cbe84bd-7bb2-4fac-8c39-40bce4cf0c5e\"/>\n" +
            "      <type>" + CollectionProfile.STATE_DU_TYPE + "</type>\n" +
            "      <title>title</title>\n" +
            "      <creator>dcs-ui</creator>\n" +
            "      <formerExternalRef>" + COLLECTION_BIZ_ID + "</formerExternalRef>\n" +
            "    </DeliverableUnit>";

    /**
     * The current (version 2) of our Collection State DU in our object graph.  It has an "isSuccessorOf" relationship
     * with {@link #COLLECTION_STATE_DU_WITH_PARENT}, and {@link #COLLECTION_ROOT_DU} as a parent.
     * <p/>
     * Our object graph:
     * Root DU <-- State DU (version 1)
     * Root DU <-- State DU (version 2)
     *
     * @see #COLLECTION_BIZ_ID
     * @see #COLLECTION_ROOT_DU
     * @see #COLLECTION_STATE_DU_WITH_PARENT
     * @see #COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR
     */
    static final String COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR = "<DeliverableUnit id=\"448629f6-76f1-423c-8c0b-eca53fe50dee\">\n" +
            "      <parent ref=\"6cbe84bd-7bb2-4fac-8c39-40bce4cf0c5e\"/>\n" +
            "      <type>" + CollectionProfile.STATE_DU_TYPE + "</type>\n" +
            "      <title>title</title>\n" +
            "      <creator>dcs-ui</creator>\n" +
            "      <formerExternalRef>" + COLLECTION_BIZ_ID + "</formerExternalRef>\n" +
            "      <relationship ref=\"d8001494-ecc8-4761-8ba6-17e309bca70e\" rel=\"urn:dataconservancy.org:rel/isSuccessorOf\"/>\n" +
            "    </DeliverableUnit>";

    /**
     * The Root DU of our Collection object graph.
     * <p/>
     * Our object graph:
     * Root DU <-- State DU (version 1)
     * Root DU <-- State DU (version 2)
     *
     * @see #COLLECTION_BIZ_ID
     * @see #COLLECTION_STATE_DU_WITH_PARENT
     * @see #COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR
     */
    static final String COLLECTION_ROOT_DU = "<DeliverableUnit id=\"6cbe84bd-7bb2-4fac-8c39-40bce4cf0c5e\">\n" +
            "      <type>" + ROOT_DU_TYPE + "</type>\n" +
            "      <title>title</title>\n" +
            "      <creator>dcs-ui</creator>\n" +
            "      <formerExternalRef>" + COLLECTION_BIZ_ID + "</formerExternalRef>\n" +
            "    </DeliverableUnit>";
    
    /**
     * The Root DU of MetadataFile object graph. It is metadata for the Collection identified by {@link #COLLECTION_ROOT_DU}.
     * <p/>
     * Our object graph:
     * Collection Root DU <-IsMetadataFor- MetadataFile Root DU <-- MetadataFile State DU (version 1)
     * Collection Root DU <-IsMetadataFor- MetadataFile Root DU <-- MetadataFile State DU (version 2)
     */
    static final String METADATAFILE_ROOT_DU = "<DeliverableUnit id=\"7b8a77cb-90e4-4298-a56a-cebcf3d5a722\">\n" +
            "   <type>" + ROOT_DU_TYPE + "</type>\n" +
            "   <title>title</title>\n" +
            "   <formerExternalRef>" + METADATA_FILE_BIZ_ID + "</formerExternalRef>\n" +
            "  </DeliverableUnit>";
    
    /**
     * The first (version 1) of our Metadata File State DU in our object graph.  It has {@link #METADATAFILE_ROOT_DU} as a
     * parent.
     * <p/>
     * Our object graph:
     * Collection State DU <-IsMetadataFor- MetadataFile Root DU <-- MetadataFile State DU (version 1)
     * Collection State DU <-IsMetadataFor- MetadataFile Root DU <-- MetadataFile State DU (version 2)
     *
     * @see #METADATA_FILE_BIZ_ID
     * @see #COLLECTION_ROOT_DU
     * @see #COLLECTION_STATE_DU_WITH_PARENT
     * @see #COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR
     */
    static final String METADATAFILE_STATE_DU_WITH_PARENT = "<DeliverableUnit id=\"950acd09-8458-4058-a8b4-916b1503f53b\">\n" +
            "      <parent ref=\"7b8a77cb-90e4-4298-a56a-cebcf3d5a722\"/>\n" +
            "      <type>" + MetadataFileProfile.STATE_DU_TYPE + "</type>\n" +
            "      <title>title</title>\n" +
            "      <formerExternalRef>" + METADATA_FILE_BIZ_ID + "</formerExternalRef>\n" +
            "      <relationship ref=\"448629f6-76f1-423c-8c0b-eca53fe50dee\" rel=\"urn:dataconservancy.org:rel/isMetadataFor\"/>\n" +
            "    </DeliverableUnit>";
    
    /**
     * The current (version 2) of our Metadata File State DU in our object graph.  It has an "isSuccessorOf" relationship
     * with {@link #METADATAFILE_STATE_DU_WITH_PARENT}, and {@link #METADATAFILE_ROOT_DU} as a parent.
     * <p/>
     * Our object graph:
     * Collection Root DU <-IsMetadataFor- MetadataFile Root DU <-- MetadataFile State DU (version 1)
     * Collection Root DU <-IsMetadataFor- MetadataFile Root DU <-- MetadataFile State DU (version 2)
     *
     * @see #COLLECTION_BIZ_ID
     * @see #COLLECTION_ROOT_DU
     * @see #COLLECTION_STATE_DU_WITH_PARENT
     * @see #COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR
     */
    static final String METADATAFILE_STATE_DU_WITH_PARENT_AND_PREDECESSOR = "<DeliverableUnit id=\"a5b47fee-99b7-4ba2-8dba-936556d0c536\">\n" +
            "      <parent ref=\"7b8a77cb-90e4-4298-a56a-cebcf3d5a722\"/>\n" +
            "      <type>" + MetadataFileProfile.STATE_DU_TYPE + "</type>\n" +
            "      <title>title</title>\n" +
            "      <formerExternalRef>" + METADATA_FILE_BIZ_ID + "</formerExternalRef>\n" +
            "      <relationship ref=\"950acd09-8458-4058-a8b4-916b1503f53b\" rel=\"urn:dataconservancy.org:rel/isSuccessorOf\"/>\n" +
            "      <relationship ref=\"448629f6-76f1-423c-8c0b-eca53fe50dee\" rel=\"urn:dataconservancy.org:rel/isMetadataFor\"/>\n" +
            "    </DeliverableUnit>";
    
    /**
     * The Root DU of our DataItem object graph.  It belongs to the Collection identified by
     * {@link #COLLECTION_ROOT_DU}.
     * <p/>
     * Our object graph:
     * Collection Root DU <-- Data Item Root DU <-- Data Item State DU (version 1)
     * Collection Root DU <-- Data Item Root DU <-- Data Item State DU (version 2)
     */
    static final String DATAITEM_ROOT_DU = "<DeliverableUnit id=\"94d780a8-1d83-4f67-a0f2-2eed1759f492\">\n" +
                    "      <parent ref=\"6cbe84bd-7bb2-4fac-8c39-40bce4cf0c5e\"/>\n" +
                    "      <type>" + DataItemProfile.DATASET_TYPE + "</type>\n" +
                    "      <title>My Data Set</title>\n" +
                    "      <formerExternalRef>" + DATAITEM_BIZ_ID + "</formerExternalRef>\n" +
                    "      <formerExternalRef>file1bizId</formerExternalRef>\n" +
                    "      <formerExternalRef>file2bizId</formerExternalRef>\n" +
                    "    </DeliverableUnit>";

    /**
     * The first version of our Data Item State DU in our object graph.  It has {@link #DATAITEM_ROOT_DU} as a
     * parent.
     * <p/>
     * Our object graph:
     * Collection Root DU <-- Data Item Root DU <-- Data Item State DU (version 1)
     */
    static final String DATAITEM_STATE_DU_VER_1 = "<DeliverableUnit id=\"DataItemStateDu-00bf6b2e-3f24-4232-b8f5-30ba2c83a60a\">\n" +
                    "      <parent ref=\"94d780a8-1d83-4f67-a0f2-2eed1759f492\"/>\n" +
                    "      <type>org.dataconservancy:types:DataItem:DataItemState</type>\n" +
                    "      <title>DataItem Name</title>\n" +
                    "    </DeliverableUnit>";

    /**
     * The second version of our Data Item State DU in our object graph.  It has {@link #DATAITEM_ROOT_DU} as a
     * parent.  It has a predecessor {@link #DATAITEM_STATE_DU_VER_1}.
     * <p/>
     * Our object graph:
     * Collection Root DU <-- Data Item Root DU <-- Data Item State DU Version 2 (succeeds version 1)
     */
    static final String DATAITEM_STATE_DU_VER_2 = "<DeliverableUnit id=\"DataItemStateDu-a233b870-6d9e-4cf4-b8ae-9dfe37ad3825\">\n" +
                    "      <parent ref=\"94d780a8-1d83-4f67-a0f2-2eed1759f492\"/>\n" +
                    "      <type>org.dataconservancy:types:DataItem:DataItemState</type>\n" +
                    "      <title>DataItem Name</title>\n" +
                    "      <relationship ref=\"DataItemStateDu-00bf6b2e-3f24-4232-b8f5-30ba2c83a60a\" rel=\"urn:dataconservancy.org:rel/isSuccessorOf\"/>\n" +
                    "    </DeliverableUnit>";

    /**
     * The second version of our DataItem.  It belongs to the Collection identified by {@link #COLLECTION_ROOT_DU}.
     * Because DataItems do not have a separate state object, this DU is also the same as the state DU.  It has
     * a predecessor {@link #DATAITEM_ROOT_DU}
     * <p/>
     * Our object graph:
     * Collection Root DU <-- Data Item DU (version 1) <-- DataItem DU (version 2)
     */
    static final String DATAITEM_ROOT_DU_VER_2 = "<DeliverableUnit id=\"7777fe8d-38cd-42f3-b559-a37ee1d48a93\">\n" +
                    "      <parent ref=\"6cbe84bd-7bb2-4fac-8c39-40bce4cf0c5e\"/>\n" +
                    "      <type>" + DataItemProfile.DATASET_STATE_TYPE + "</type>\n" +
                    "      <title>My Data Set</title>\n" +
                    "      <formerExternalRef>" + DATAITEM_BIZ_ID + "</formerExternalRef>\n" +
                    "      <formerExternalRef>file1bizId</formerExternalRef>\n" +
                    "      <formerExternalRef>file2bizId</formerExternalRef>\n" +
                    "      <relationship ref=\"94d780a8-1d83-4f67-a0f2-2eed1759f492\" rel=\"urn:dataconservancy.org:rel/isSuccessorOf\"/>\n" +
                    "    </DeliverableUnit>";


    ArchiveBusinessObjectSearcherImpl underTest;

    DcsConnector mockConnector;

    IdService mockIdService;

    @Before
    public void setUp() throws MalformedURLException, IdentifierNotFoundException {
        instantiateMocks();
        configureMockBehaviors();
        underTest = getInstanceUnderTest();
    }

    void instantiateMocks() {
        mockIdService = mock(IdService.class);
        mockConnector = mock(DcsConnector.class);
    }

    void configureMockBehaviors() throws MalformedURLException, IdentifierNotFoundException {
        Identifier dataItemId = mock(Identifier.class);
        Identifier collectionId = mock(Identifier.class);
        Identifier metadataFileId = mock(Identifier.class);
        when(mockIdService.fromUrl(new URL(DATAITEM_BIZ_ID))).thenReturn(dataItemId);
        when(mockIdService.fromUrl(new URL(COLLECTION_BIZ_ID))).thenReturn(collectionId);
        when(mockIdService.fromUrl(new URL(METADATA_FILE_BIZ_ID))).thenReturn(metadataFileId);
        when(dataItemId.getType()).thenReturn(Types.DATA_SET.name());
        when(collectionId.getType()).thenReturn(Types.COLLECTION.name());
        when(metadataFileId.getType()).thenReturn(Types.METADATA_FILE.name());
    }

    abstract ArchiveBusinessObjectSearcherImpl getInstanceUnderTest();

}
