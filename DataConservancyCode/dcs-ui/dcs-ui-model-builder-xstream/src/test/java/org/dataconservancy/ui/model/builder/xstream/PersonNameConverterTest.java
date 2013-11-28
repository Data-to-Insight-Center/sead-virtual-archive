package org.dataconservancy.ui.model.builder.xstream;

import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.ui.model.ContactInfo;
import org.dataconservancy.ui.model.PersonName;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.dataconservancy.ui.model.builder.xstream.PersonNameConverter.E_FAMILY_NAMES;
import static org.dataconservancy.ui.model.builder.xstream.PersonNameConverter.E_GIVEN_NAMES;
import static org.dataconservancy.ui.model.builder.xstream.PersonNameConverter.E_MIDDLE_NAMES;
import static org.dataconservancy.ui.model.builder.xstream.PersonNameConverter.E_NAME_PREFIX;
import static org.dataconservancy.ui.model.builder.xstream.PersonNameConverter.E_NAME_SUFFIX;
import static org.dataconservancy.ui.model.builder.xstream.CollectionConverter.E_COL_CREATOR;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: hvu
 * Date: 10/11/12
 * Time: 3:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class PersonNameConverterTest extends BaseConverterTest{

    private String XML;

    @Before
    public void setupXML() {
        XML =
                "            <" + E_COL_CREATOR + "  xmlns=\"http://dataconservancy.org/schemas/bop/1.0\">\n" +
                "                <" + E_GIVEN_NAMES + ">" + creatorOne.getGivenNames() + "</" + E_GIVEN_NAMES + ">\n" +
                "                <" + E_MIDDLE_NAMES + ">" + creatorOne.getMiddleNames() + "</" + E_MIDDLE_NAMES + ">\n" +
                "                <" + E_FAMILY_NAMES + ">" + creatorOne.getFamilyNames() + "</" + E_FAMILY_NAMES + ">\n" +
                "                <" + E_NAME_PREFIX + ">" + creatorOne.getPrefixes() + "</" + E_NAME_PREFIX + ">\n" +
                "                <" + E_NAME_SUFFIX + ">" + creatorOne.getSuffixes() + "</" + E_NAME_SUFFIX + ">\n" +
                "            </" + E_COL_CREATOR + ">\n" ;
    }

    @Test
    public void testUnmarshal() {
        PersonName actual = (PersonName) x.fromXML(XML);
        assertEquals(creatorOne, actual);
        assertEquals(creatorOne, x.fromXML(x.toXML(creatorOne)));
    }


    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(creatorOne));
    }
}
