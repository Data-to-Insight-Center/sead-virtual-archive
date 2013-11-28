package org.dataconservancy.mhf.instances;

import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.model.builder.api.AttributeValueBuilder;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.MetadataAttributeName;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.PersonName;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.DATE_TIME;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.PERSON_NAME;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.STRING;
import static org.dataconservancy.mhf.test.support.BuilderTestUtil.newXstreamAttributeValueBuilder;
import static org.dataconservancy.mhf.test.support.BuilderTestUtil.newXstreamModelBuilder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: hanh
 * Date: 3/11/13
 * Time: 1:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class AttributeSetMetadataInstanceTest {

    private final static String SUMMARY = "Collection Summary";
    private final static String TITLE = "Collection Title";
    private final static String DEPOSIT_DATE = "2013-01-13";
    private final static String PUBLICATION_DATE = "2013-01-15";
    private final static String CITABLE_LOCATOR = "http://dx.doi.org/1234";
    private final static String DEPOSITOR_ID = "depositorId";
    private final static DateTime DEPOSIT_DATETIME = DateTime.parse(DEPOSIT_DATE);
    private final static DateTime PUBLICATION_DATETIME = DateTime.parse(PUBLICATION_DATE);
    private final static String COLLECTION_ID = "collectionId";
    private final static String ALT_ID1 = "altId1";
    private final static String ALT_ID2 = "altId2";
    private final static String CREATOR_GN = "Foo";
    private final static String CREATOR_SN = "Bar";
    private final static String CREATOR_MN = "Baz";


    private MetadataObjectBuilder moBuilder;
    private AttributeValueBuilder avBuilder;

    AttributeSetMetadataInstance asMetadataInstance;
    AttributeSetMetadataInstance asMetadataInstancePlus;
    AttributeSetMetadataInstance asMetadataInstancePlusPlus;
    AttributeSetMetadataInstance asMetadataInstance3;



    @Before
    public void setUp() throws MalformedURLException {
        byte [] collectionAttributeSetMetadataAsByteArray = setUpCollectionMetadataAttributeSetAsByteArray();
        byte [] anotherCollectionAttributeSetMetadataAsByteArray = setUpDifferentCollectionMetadataAttributeSetAsByteArray();
        asMetadataInstance =     new AttributeSetMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID, collectionAttributeSetMetadataAsByteArray);
        asMetadataInstancePlus = new AttributeSetMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID, collectionAttributeSetMetadataAsByteArray);
        asMetadataInstancePlusPlus = new AttributeSetMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID, collectionAttributeSetMetadataAsByteArray);
        asMetadataInstance3 = new AttributeSetMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID, anotherCollectionAttributeSetMetadataAsByteArray);
    }

    @Test
    public void testReadInstanceContentMoreThanOnce() throws IOException {
        boolean failedToReadInstanceContentTwice = false;
        InputStream inputStream = null;
        InputStream inputStream2 = null;

        try {
            inputStream = asMetadataInstance.getContent();
            inputStream2 = asMetadataInstance.getContent();
        } catch (Exception e) {
            failedToReadInstanceContentTwice = true;
        } finally {
            inputStream.close();
            inputStream2.close();
        }
        assertFalse(failedToReadInstanceContentTwice);
        assertNotNull(inputStream);
        assertNotNull(inputStream2);
    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertFalse(asMetadataInstance.equals(asMetadataInstance3));
        assertFalse(asMetadataInstance3.equals(asMetadataInstance));
    }


    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(asMetadataInstance.equals(asMetadataInstancePlus));
        assertTrue(asMetadataInstancePlus.equals(asMetadataInstance));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(asMetadataInstance.equals(asMetadataInstancePlus));
        assertTrue(asMetadataInstancePlus.equals(asMetadataInstancePlusPlus));
        assertTrue(asMetadataInstance.equals(asMetadataInstancePlusPlus));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(asMetadataInstance.equals(asMetadataInstancePlusPlus));
        assertTrue(asMetadataInstance.equals(asMetadataInstancePlusPlus));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(asMetadataInstance.equals(null));
    }


    private byte[] setUpCollectionMetadataAttributeSetAsByteArray() {
        Collection collection;

        List<String> altIds = new ArrayList<String>();

        MetadataAttributeSet expectedCoreMd = new MetadataAttributeSet(MetadataAttributeSetName.COLLECTION_CORE_METADATA);
        MetadataAttributeSet expectedSystemMd = new MetadataAttributeSet(MetadataAttributeSetName.SYSTEM_METADATA);

        //----- Set up collaborating objects: moBuilder
        moBuilder = newXstreamModelBuilder();
        avBuilder = newXstreamAttributeValueBuilder();
        //-------Setup collaborating objects: COLLECTION
        collection = new Collection();

        // Set fields on the Collection
        collection.setId(COLLECTION_ID);
        collection.setSummary(SUMMARY);
        collection.setTitle(TITLE);
        collection.setDepositDate(DEPOSIT_DATETIME);
        collection.setPublicationDate(PUBLICATION_DATETIME);
        collection.setCitableLocator(CITABLE_LOCATOR);
        collection.setDepositorId(DEPOSITOR_ID);
        altIds.add(ALT_ID1);
        altIds.add(ALT_ID2);
        collection.setAlternateIds(altIds);
        PersonName name1 = new PersonName(null, CREATOR_GN, CREATOR_MN, CREATOR_SN, null);
        collection.addCreator(name1);


        // Core metadata: the expected core metadata attribute set

        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.BUSINESS_ID, STRING, COLLECTION_ID));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.DESCRIPTION, STRING, SUMMARY));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.TITLE, STRING, TITLE));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.PUBLICATION_DATE, DATE_TIME,
                String.valueOf(PUBLICATION_DATETIME)));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.ALTERNATE_ID, STRING, ALT_ID1));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.ALTERNATE_ID, STRING, ALT_ID2));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        avBuilder.buildPersonName(name1, baos);
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.CREATOR, PERSON_NAME, baos.toString()));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.CITABLE_LOCATOR, STRING,
                CITABLE_LOCATOR));

        // Serialize core metadata into an AttributeSetMetadataInstance
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        moBuilder.buildAttributeSet(expectedCoreMd, out);

        return out.toByteArray();

    }
    private byte[] setUpDifferentCollectionMetadataAttributeSetAsByteArray() {
        Collection collection;

        List<String> altIds = new ArrayList<String>();

        MetadataAttributeSet expectedCoreMd = new MetadataAttributeSet(MetadataAttributeSetName.COLLECTION_CORE_METADATA);
        MetadataAttributeSet expectedSystemMd = new MetadataAttributeSet(MetadataAttributeSetName.SYSTEM_METADATA);

        //----- Set up collaborating objects: moBuilder
        moBuilder = newXstreamModelBuilder();
        avBuilder = newXstreamAttributeValueBuilder();
        //-------Setup collaborating objects: COLLECTION
        collection = new Collection();

        // Set fields on the Collection
        collection.setId(COLLECTION_ID + "2");
        collection.setSummary(SUMMARY);
        collection.setTitle(TITLE);
        collection.setPublicationDate(PUBLICATION_DATETIME);
        collection.setCitableLocator(CITABLE_LOCATOR);
        collection.setDepositorId(DEPOSITOR_ID);
        altIds.add(ALT_ID1);
        altIds.add(ALT_ID2);
        collection.setAlternateIds(altIds);


        // Core metadata: the expected core metadata attribute set

        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.BUSINESS_ID, STRING, COLLECTION_ID));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.DESCRIPTION, STRING, SUMMARY));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.TITLE, STRING, TITLE));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.PUBLICATION_DATE, DATE_TIME,
                String.valueOf(PUBLICATION_DATETIME)));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.ALTERNATE_ID, STRING, ALT_ID1));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.ALTERNATE_ID, STRING, ALT_ID2));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.CREATOR, PERSON_NAME, baos.toString()));
        expectedCoreMd.addAttribute(new MetadataAttribute(MetadataAttributeName.CITABLE_LOCATOR, STRING,
                CITABLE_LOCATOR));

        // Serialize core metadata into an AttributeSetMetadataInstance
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        moBuilder.buildAttributeSet(expectedCoreMd, out);

        return out.toByteArray();

    }
}
