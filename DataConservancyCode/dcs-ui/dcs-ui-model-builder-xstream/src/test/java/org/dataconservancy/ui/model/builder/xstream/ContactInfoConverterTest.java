package org.dataconservancy.ui.model.builder.xstream;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.ui.model.*;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import sun.font.PhysicalFont;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;

import static org.dataconservancy.ui.model.builder.xstream.ContactInfoConverter.E_CONTACT_ADDRESS;
import static org.dataconservancy.ui.model.builder.xstream.ContactInfoConverter.E_CONTACT_EMAIL;
import static org.dataconservancy.ui.model.builder.xstream.ContactInfoConverter.E_CONTACT_NAME;
import static org.dataconservancy.ui.model.builder.xstream.ContactInfoConverter.E_CONTACT_PHONE;
import static org.dataconservancy.ui.model.builder.xstream.ContactInfoConverter.E_CONTACT_ROLE;
import static org.dataconservancy.ui.model.builder.xstream.ContactInfoConverter.E_COUNTRY;
import static org.dataconservancy.ui.model.builder.xstream.ContactInfoConverter.E_STATE;
import static org.dataconservancy.ui.model.builder.xstream.ContactInfoConverter.E_STREET_ADDRESS;
import static org.dataconservancy.ui.model.builder.xstream.ContactInfoConverter.E_ZIP_CODE;
import static org.dataconservancy.ui.model.builder.xstream.ContactInfoConverter.E_COL_CONTACT_INFO;
import static org.dataconservancy.ui.model.builder.xstream.ContactInfoConverter.E_CITY;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_BOP;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_COLLECTION;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_COLLECTIONS;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_COL_CONTACT_INFOS;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_COL_SUMMARY;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_COL_TITLE;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_ID;
import static org.dataconservancy.ui.model.builder.xstream.ConverterTestConstants.COLLECTIONS_WRAPPER;
import static org.junit.Assert.assertEquals;

/**
 * Tests the marshalling and unmarshalling function of ContactInfoConverter.
 */
public class ContactInfoConverterTest extends BaseConverterTest {

    static final String XMLNS = "http://dataconservancy.org/schemas/bop/1.0";

    private String XML;

    private String COLLECTION_XML;

    private Collection testCitationInfoCollection;
    private ContactInfo testContactInfoOne = new ContactInfo();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setupTestCollection();
        setupXMLStrings();

    }

    private void setupXMLStrings() {
        XML =
                "            <" + E_COL_CONTACT_INFO + "  xmlns=\"http://dataconservancy.org/schemas/bop/1.0\">\n" +
                "                <" + E_CONTACT_NAME + ">" + contactInfoOne.getName() + "</" + E_CONTACT_NAME + ">\n" +
                "                <" + E_CONTACT_ROLE + ">" + contactInfoOne.getRole() + "</" + E_CONTACT_ROLE + ">\n" +
                "                <" + E_CONTACT_EMAIL + ">" + contactInfoOne.getEmailAddress() + "</" + E_CONTACT_EMAIL + ">\n" +
                "                <" + E_CONTACT_PHONE + ">" + contactInfoOne.getPhoneNumber() + "</" + E_CONTACT_PHONE + ">\n" +
                "                <" + E_CONTACT_ADDRESS + ">\n" +
                "                    <" + E_STREET_ADDRESS + ">" + contactInfoOne.getPhysicalAddress().getStreetAddress() + "</" + E_STREET_ADDRESS + ">\n" +
                "                    <" + E_CITY + ">" + contactInfoOne.getPhysicalAddress().getCity() + "</" + E_CITY + ">\n" +
                "                    <" + E_COUNTRY + ">" + contactInfoOne.getPhysicalAddress().getCountry() + "</" + E_COUNTRY + ">\n" +
                "                </" + E_CONTACT_ADDRESS + ">\n" +
                "            </" + E_COL_CONTACT_INFO + ">";

        COLLECTION_XML =
                        "    <" + E_COLLECTION + " " + E_ID + "=\"" + testCitationInfoCollection.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                        "        <" + E_COL_TITLE + ">" + testCitationInfoCollection.getTitle() + "</" + E_COL_TITLE + ">\n" +
                        "        <" + E_COL_SUMMARY + ">" + testCitationInfoCollection.getSummary() + "</" + E_COL_SUMMARY + ">\n" +
                        "        <" + E_COL_CONTACT_INFOS + ">\n" +
                        "            <" + E_COL_CONTACT_INFO + ">\n" +
                        "                <" + E_CONTACT_NAME + ">" + testContactInfoOne.getName() + "</" + E_CONTACT_NAME + ">\n" +
                        "                <" + E_CONTACT_ROLE + ">" + testContactInfoOne.getRole() + "</" + E_CONTACT_ROLE + ">\n" +
                        "                <" + E_CONTACT_EMAIL + ">" + testContactInfoOne.getEmailAddress() + "</" + E_CONTACT_EMAIL + ">\n" +
                        "                <" + E_CONTACT_PHONE + ">" + testContactInfoOne.getPhoneNumber() + "</" + E_CONTACT_PHONE + ">\n" +
                        "                <" + E_CONTACT_ADDRESS + ">\n" +
                        "                    <" + E_STREET_ADDRESS + ">" + testContactInfoOne.getPhysicalAddress().getStreetAddress() + "</" + E_STREET_ADDRESS + ">\n" +
                        "                    <" + E_CITY + ">" + testContactInfoOne.getPhysicalAddress().getCity() + "</" + E_CITY + ">\n" +
                        "                    <" + E_STATE + ">" + testContactInfoOne.getPhysicalAddress().getState() + "</" + E_STATE + ">\n" +

                        "                    <" + E_COUNTRY + ">" + testContactInfoOne.getPhysicalAddress().getCountry() + "</" + E_COUNTRY + ">\n" +
                        "                </" + E_CONTACT_ADDRESS + ">\n" +
                        "            </" + E_COL_CONTACT_INFO + ">" +
                        "        </" + E_COL_CONTACT_INFOS + ">\n" +
                        "    </" + E_COLLECTION + ">\n" ;
    }

    private void setupTestCollection(){
        testCitationInfoCollection = new Collection();
        testCitationInfoCollection.setId("id:testCitationInfoCollection");
        testCitationInfoCollection.setTitle("test collection");
        testCitationInfoCollection.setSummary("collection for testing citation info validation");
        testContactInfoOne.setName("NAME");
        testContactInfoOne.setRole("ROLE");
        testContactInfoOne.setEmailAddress("foo@bar.baz");
        testContactInfoOne.setPhoneNumber("8675-309");
        testContactInfoOne.setPhysicalAddress(new Address("1234 Lane Lane", "Yourtown", "State of Confusion", "00000", "Neverland"));
        testCitationInfoCollection.addContactInfo(testContactInfoOne);
    }

    @Test
    public void testUnmarshal() {
        ContactInfo actual = (ContactInfo) x.fromXML(XML);
        assertEquals(contactInfoOne, actual);
        assertEquals(contactInfoOne, x.fromXML(x.toXML(contactInfoOne)));
    }


    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(contactInfoOne));
    }
    /**
     * Test which insures that the expected XML is valid, marshaled XML is valid, and round-tripped XML is valid.
     *
     * @throws Exception
     */
    @Test
    public void testMarshalIsValid() throws Exception {
        // Verify assumptions: test that our expected XML is valid.
        final String expectedXml = String.format(BOP_WRAPPER, String.format(COLLECTIONS_WRAPPER, COLLECTION_XML));

        final Document expectedDom = parser.parse(IOUtils.toInputStream(expectedXml));
        validator.validate(new DOMSource(expectedDom));

        // Verify that our serialized XML is valid.
        final String actualXml = String.format(BOP_WRAPPER, String.format(COLLECTIONS_WRAPPER, x.toXML(testCitationInfoCollection)));
        final Document actualDom = parser.parse(IOUtils.toInputStream(actualXml));
        validator.validate(new DOMSource(actualDom));

        // Verify that round-tripped XML is valid (XML -> Object -> XML)
        final String roundTrippedXml = String.format(BOP_WRAPPER, String.format(COLLECTIONS_WRAPPER, x.toXML(x.fromXML(COLLECTION_XML))));
        final Document roundTrippedDom = parser.parse(IOUtils.toInputStream(roundTrippedXml));
        validator.validate(new DOMSource(roundTrippedDom));
    }

    @Test
    public void testEmptyFields() {
        //Test we are starting with good conversion
        assertEquals(contactInfoOne, x.fromXML(x.toXML(contactInfoOne)));

        //Remove each param and make sure serialization still works correctly.
        contactInfoOne.setEmailAddress(null);
        assertEquals(contactInfoOne, x.fromXML(x.toXML(contactInfoOne)));

        contactInfoOne.setName(null);
        assertEquals(contactInfoOne, x.fromXML(x.toXML(contactInfoOne)));

        contactInfoOne.setPhoneNumber(null);
        assertEquals(contactInfoOne, x.fromXML(x.toXML(contactInfoOne)));

        contactInfoOne.setPhysicalAddress(null);
        assertEquals(contactInfoOne, x.fromXML(x.toXML(contactInfoOne)));

        contactInfoOne.setRole(null);
        assertEquals(contactInfoOne, x.fromXML(x.toXML(contactInfoOne)));
    }

}
