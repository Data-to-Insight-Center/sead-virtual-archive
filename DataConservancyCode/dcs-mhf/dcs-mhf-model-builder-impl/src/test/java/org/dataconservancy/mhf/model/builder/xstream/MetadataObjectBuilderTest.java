/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.mhf.model.builder.xstream;

import com.thoughtworks.xstream.XStream;
import org.dataconservancy.mhf.model.builder.api.AttributeValueBuilder;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataAttributeName;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representation.api.MetadataAttributeType;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MetadataObjectBuilderTest {
    Collection collection;

    DataItem dataItemWithSystemCoreM;
    MetadataAttributeSet collectionCoreAttributeSet;
    MetadataAttributeSet collectionSystemAttributeSet;
    MetadataObjectBuilder moBuilder;
    AttributeValueBuilder avBuilder;

    static DateTime publicationDate = new DateTime();
    static DateTime depositDate = new DateTime();


    private static final String EXPECTED_COLLECTION_CORE_METADATA =
           "<AttributeSet>\n" +
                   "  <name>dataconservancy.org:mhf:schema:attribute-set:bo:collection:core:1.0</name>\n" +
                   "  <attributes>\n" +
                   "    <Attribute>\n" +
                   "      <name>alternate.id</name>\n" +
                   "      <type>String</type>\n" +
                   "      <value>ID1</value>\n" +
                   "    </Attribute>\n" +
                   "    <Attribute>\n" +
                   "      <name>creator</name>\n" +
                   "      <type>PersonName</type>\n" +
                   "      <value>&lt;PersonName&gt;\n" +
                   "  &lt;givenNames&gt;\n" +
                   "    &lt;string&gt;Monkey&lt;/string&gt;\n" +
                   "  &lt;/givenNames&gt;\n" +
                   "  &lt;familyNames&gt;\n" +
                   "    &lt;string&gt;Candy&lt;/string&gt;\n" +
                   "  &lt;/familyNames&gt;\n" +
                   "  &lt;middleNames&gt;\n" +
                   "    &lt;string&gt;Bar&lt;/string&gt;\n" +
                   "  &lt;/middleNames&gt;\n" +
                   "&lt;/PersonName&gt;</value>\n" +
                   "    </Attribute>\n" +
                   "    <Attribute>\n" +
                   "      <name>alternate.id</name>\n" +
                   "      <type>String</type>\n" +
                   "      <value>ID2</value>\n" +
                   "    </Attribute>\n" +
                   "    <Attribute>\n" +
                   "      <name>title</name>\n" +
                   "      <type>String</type>\n" +
                   "      <value>Collection Title</value>\n" +
                   "    </Attribute>\n" +
                   "    <Attribute>\n" +
                   "      <name>description</name>\n" +
                   "      <type>String</type>\n" +
                   "      <value>Collection Summary</value>\n" +
                   "    </Attribute>\n" +
                   "    <Attribute>\n" +
                   "      <name>publication.date</name>\n" +
                   "      <type>DateTime</type>\n" +
                   "      <value>" + String.valueOf(publicationDate) + "</value>\n" +
                   "    </Attribute>\n" +
                   "  </attributes>\n" +
                   "</AttributeSet>";

    private static final String EXPECTED_SYSTEM_METADATA=
            "<AttributeSet>\n" +
                    "  <name>dataconservancy.org:mhf:schema:attribute-set:bo:system:1.0</name>\n" +
                    "  <attributes>\n" +
                    "    <Attribute>\n" +
                    "      <name>system.depositor</name>\n" +
                    "      <type>String</type>\n" +
                    "      <value>id:depositor</value>\n" +
                    "    </Attribute>\n" +
                    "    <Attribute>\n" +
                    "      <name>system.deposit.date</name>\n" +
                    "      <type>DateTime</type>\n" +
                    "      <value>" + String.valueOf(depositDate)+ "</value>\n" +
                    "    </Attribute>\n" +
                    "  </attributes>\n" +
                    "</AttributeSet>";

    @Before
    public void setUp(){

        final XStream xstream = new XStream();
        xstream.alias("AttributeSet", MetadataAttributeSet.class);
        xstream.alias("Attribute", MetadataAttribute.class);
        xstream.alias("PersonName", PersonName.class);

        moBuilder = new XstreamMetadataObjectBuilder(xstream);
        avBuilder = new XStreamAttributeValueBuilder(xstream);
        //-------Setup collaborating objects: COLLECTION
        collection = new Collection();

        //Set up core metadata
        collection.setSummary("Collection Summary");
        collection.setTitle("Collection Title");
        collection.setDepositDate(depositDate);

        List<String> altIds = new ArrayList<String>();
        altIds.add("ID1");
        altIds.add("ID2");

        collection.setAlternateIds(altIds);

        PersonName name1 = new PersonName();
        name1.setFamilyNames("Candy");
        name1.setGivenNames("Monkey");
        name1.setMiddleNames("Bar");

        collection.addCreator(name1);

        Person depositor = new Person();
        depositor.setId("id:depositor");
        collection.setDepositorId(depositor.getId());

        collection.setPublicationDate(publicationDate);

        collectionCoreAttributeSet = new MetadataAttributeSet(MetadataAttributeSetName.COLLECTION_CORE_METADATA);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        avBuilder.buildPersonName(collection.getCreators().get(0), baos);

        collectionCoreAttributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.CREATOR, MetadataAttributeType.PERSON_NAME, baos.toString()));
        collectionCoreAttributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.TITLE, collection.getTitle().getClass().getSimpleName(), collection.getTitle()));
        collectionCoreAttributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.DESCRIPTION, collection.getSummary().getClass().getSimpleName(), collection.getSummary()));
        collectionCoreAttributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.DESCRIPTION, collection.getSummary().getClass().getSimpleName(), collection.getSummary()));
        collectionCoreAttributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.PUBLICATION_DATE, MetadataAttributeType.DATE_TIME, String.valueOf(collection.getPublicationDate())));

        for (String id : collection.getAlternateIds()) {
            collectionCoreAttributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.ALTERNATE_ID, id.getClass().getSimpleName(), id));
        }

        collectionSystemAttributeSet = new MetadataAttributeSet(MetadataAttributeSetName.SYSTEM_METADATA);
        collectionSystemAttributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.DEPOSITOR_ID, collection.getDepositorId().getClass().getSimpleName(), collection.getDepositorId()));
        collectionSystemAttributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.DEPOSIT_DATE, MetadataAttributeType.DATE_TIME, String.valueOf(collection.getDepositDate())));





        //----- Set up collaborating objects: data item with core and system metadata
        dataItemWithSystemCoreM = new DataItem();
        dataItemWithSystemCoreM.setName("Data Item with System and Core metadata");
        dataItemWithSystemCoreM.setDescription("Data Item is expected to contain 2 instances of metadata");
        dataItemWithSystemCoreM.setDepositDate(new DateTime());

        //----- Set up collaborating objects: Person as creator
        Person person = new Person();
        person.setId("id:person1");
        person.setLastNames("Sirk");
        person.setFirstNames("Willard-boy");
        dataItemWithSystemCoreM.setDepositorId(person.getId());
    }

    @Test
    public void testBuildCollectionCoreMetadataFromXML(){
        ByteArrayInputStream inputStream = new ByteArrayInputStream(EXPECTED_COLLECTION_CORE_METADATA.getBytes());
        Assert.assertEquals(collectionCoreAttributeSet, moBuilder.buildAttributeSet(inputStream));
    }
    @Test
    public void testBuildMetadataAttributeSetToXML(){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        moBuilder.buildAttributeSet(collectionCoreAttributeSet, out);
        Assert.assertTrue(collectionCoreAttributeSet.equals(moBuilder.buildAttributeSet(new ByteArrayInputStream(out.toByteArray()))));
    }

    @Test
    public void buildMultipleAS() {
        MetadataAttributeSet asA = new MetadataAttributeSet("TestAttributeSetA");
        asA.addAttribute(new MetadataAttribute("name1", "type1", "value1"));
        asA.addAttribute(new MetadataAttribute("name2", "type2", "value1"));
        asA.addAttribute(new MetadataAttribute("name3", "type3", "value1"));

        MetadataAttributeSet asB = new MetadataAttributeSet("TestAttributeSetB");
        asB.addAttribute(new MetadataAttribute("name1B", "type1B","value1B"));
        asB.addAttribute(new MetadataAttribute("name2B", "type2B","value1B"));
        asB.addAttribute(new MetadataAttribute("name3B", "type3B", "value1B"));

        Set<AttributeSet> attributeSets = new HashSet<AttributeSet>();
        attributeSets.add(asA);
        attributeSets.add(asB);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        moBuilder.buildAttributeSets(attributeSets, out);
        Assert.assertTrue(attributeSets.equals(moBuilder.buildAttributeSets(new ByteArrayInputStream(out.toByteArray()))));
    }

    @Test
    public void testBuildSystemMetadataFromXML(){
        ByteArrayInputStream inputStream = new ByteArrayInputStream(EXPECTED_SYSTEM_METADATA.getBytes());
        Assert.assertTrue(collectionSystemAttributeSet.equals(moBuilder.buildAttributeSet(inputStream)));
    }

    @Test
    public void testBuildSystemMetadataToXML(){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        moBuilder.buildAttributeSet(collectionSystemAttributeSet,out);
        Assert.assertTrue(collectionSystemAttributeSet.equals(moBuilder.buildAttributeSet(new ByteArrayInputStream(out.toByteArray()))));
    }

}
