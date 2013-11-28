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

package org.dataconservancy.ui.model.builder.xstream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.MetadataFile;
import org.junit.Ignore;
import org.junit.Test;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.test.support.model.BaseModelTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

/**
 * Test class to test the builder implmementation.
 */
public class XstreamBusinessObjectBuilderTest
        extends BaseModelTest {

    @Autowired
    private BusinessObjectBuilder validatingBusinessObjectBuilder;

    private static final String DATA_FILE_XML =
            "<file xmlns=\"http://dataconservancy.org/schemas/bop/1.0\" id=\"id:dataFileOne\">\n" +
                "<source>file:/var/folders/8c/d5953nv94tz71gl0phc8whkc0000gn/T/testFile51703420723354684.tmp</source>\n" +
                "<name>Data file one</name>\n" +
                "<path>/var/folders/8c/d5953nv94tz71gl0phc8whkc0000gn/T</path>\n" +
                "<fileSize>5</fileSize>\n" +
            "</file>";

    private static final String METADATA_FILE_XML =
            "<metadataFile xmlns=\"http://dataconservancy.org/schemas/bop/1.0\" id=\"id:metadataFileOne\">\n" +
                "<parentId>id:collectionWithData</parentId>\n" +
                "<source>file:/var/folders/8c/d5953nv94tz71gl0phc8whkc0000gn/T/testFile51703420723354684.tmp</source>\n" +
                "<format>FORMAT:ONE</format>\n" +
                "<name>MetadataOne</name>\n"  +
                "<path>/var/folders/8c/d5953nv94tz71gl0phc8whkc0000gn/T</path>\n" +
                "<metadataFormat>FORMAT:ONE</metadataFormat>\n" +
            "</metadataFile>";
    /**
     * This test is to document existing behavior. Creating an xstreambusiness
     * object factory instance should throw an IllegalStateException if the
     * converters are not set before hand.
     */
    @Ignore
    @Test(expected = IllegalStateException.class)
    public void testNonSetUpFactory() {
        XstreamBusinessObjectFactory factory =
                new XstreamBusinessObjectFactory();

        XstreamBusinessObjectBuilder builder =
                new XstreamBusinessObjectBuilder(factory.createInstance());
    }

    @Test
    public void testBuildBusinessObjectPackageRoundTrip()
            throws InvalidXmlException {
        Bop bop = new Bop();
        bop.addProject(projectOne);
        bop.addCollection(collectionWithData);
        bop.addPerson(user);
        dataItemOne.setDepositorId(user.getId());
        bop.addDataItem(dataItemOne);
        bop.addMetadataFile(metadataFileOne);

        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        validatingBusinessObjectBuilder.buildBusinessObjectPackage(bop, sink);

        ByteArrayInputStream stream =
                new ByteArrayInputStream(sink.toByteArray());

        Bop returnedBop =
                validatingBusinessObjectBuilder.buildBusinessObjectPackage(stream);

        assertEquals(bop, returnedBop);
    }

    @Test
    public void testBuildProjectRoundTrip() throws InvalidXmlException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        validatingBusinessObjectBuilder.buildProject(projectOne, sink);

        ByteArrayInputStream stream =
                new ByteArrayInputStream(sink.toByteArray());
        Project returnedProject = validatingBusinessObjectBuilder.buildProject(stream);

        assertEquals(projectOne, returnedProject);
    }

    @Test
    public void testBuildCollectionRoundTrip() throws InvalidXmlException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        validatingBusinessObjectBuilder.buildCollection(collectionWithData, sink);

        ByteArrayInputStream stream =
                new ByteArrayInputStream(sink.toByteArray());
        Collection returnedCollection =
                validatingBusinessObjectBuilder.buildCollection(stream);

        assertEquals(collectionWithData, returnedCollection);
    }

    @Test
    public void testBuildPersonRoundTrip() throws InvalidXmlException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        validatingBusinessObjectBuilder.buildPerson(user, sink);

        ByteArrayInputStream stream =
                new ByteArrayInputStream(sink.toByteArray());
        Person returnedUser = validatingBusinessObjectBuilder.buildPerson(stream);

        assertEquals(user, returnedUser);
    }

    @Test
    public void testBuildDataFileRoundTrip() throws Exception {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        validatingBusinessObjectBuilder.buildDataFile(dataFileOne, sink);

        ByteArrayInputStream stream =
                new ByteArrayInputStream(sink.toByteArray());
        DataFile returnedFile = validatingBusinessObjectBuilder.buildDataFile(stream);

        assertEquals(dataFileOne, returnedFile);
    }

    @Test
    public void testBuildDataFileRoundTrip2() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        DataFile df = validatingBusinessObjectBuilder.buildDataFile(IOUtils.toInputStream(DATA_FILE_XML));
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        validatingBusinessObjectBuilder.buildDataFile(df, sink);
        XMLAssert.assertXMLEqual(DATA_FILE_XML, new String(sink.toByteArray()));
    }

    @Test
    public void testBuildMetadataFileRoundTrip() throws Exception {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        validatingBusinessObjectBuilder.buildMetadataFile(metadataFileOne, sink);

        ByteArrayInputStream stream =
                new ByteArrayInputStream(sink.toByteArray());
        MetadataFile returnedFile = validatingBusinessObjectBuilder.buildMetadataFile(stream);

        assertEquals(metadataFileOne, returnedFile);
    }

    @Test
    public void testBuildMetadataFileRoundTrip2() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        MetadataFile mdf = validatingBusinessObjectBuilder.buildMetadataFile(IOUtils.toInputStream(METADATA_FILE_XML));
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        validatingBusinessObjectBuilder.buildMetadataFile(mdf, sink);
        XMLAssert.assertXMLEqual(METADATA_FILE_XML, new String(sink.toByteArray()));
    }

}
