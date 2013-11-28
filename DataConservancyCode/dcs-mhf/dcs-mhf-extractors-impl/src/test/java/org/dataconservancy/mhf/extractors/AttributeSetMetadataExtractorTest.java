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

package org.dataconservancy.mhf.extractors;

import org.dataconservancy.mhf.extractor.api.ExtractionException;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instances.AttributeSetMetadataInstance;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;
import org.dataconservancy.mhf.representations.AttributeSetMetadataRepresentation;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.mhf.test.support.BuilderTestUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collection;

import static org.dataconservancy.mhf.representation.api.MetadataAttributeName.BUSINESS_ID;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeName.DEPOSIT_DATE;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.DATE_TIME;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class AttributeSetMetadataExtractorTest {

    private AttributeSet attributeSet;

    private MetadataObjectBuilder builder;

    private AttributeSetMetadataExtractor underTest;

    @Before
    public void setUp() throws Exception {
        MetadataAttributeSet attributeSet = new MetadataAttributeSet(MetadataAttributeSetName.COLLECTION_CORE_METADATA);
        attributeSet.addAttribute(new MetadataAttribute(BUSINESS_ID, STRING, "http://foo.bar.baz/1234"));
        attributeSet.addAttribute(new MetadataAttribute(DEPOSIT_DATE, DATE_TIME, "2001-04-01"));
        this.attributeSet = attributeSet;

        builder = BuilderTestUtil.newXstreamModelBuilder();

        underTest = new AttributeSetMetadataExtractor(builder);
    }

    /**
     * Insures that the extractor can extract the attributes under proper conditions.
     *
     * @throws Exception
     */
    @Test
    public void testExtraction() throws Exception {
        // Create a MetadataInstance to perform extraction on.  We serialize the {@link #attributeSet} composed in
        // setUp()
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        builder.buildAttributeSet(attributeSet, out);
        final AttributeSetMetadataInstance instance = new AttributeSetMetadataInstance(
                MetadataFormatId.ATTRIBUTE_SET_METADATA_FORMAT_ID, out.toByteArray());

        // Attempt extraction on the MetadataInstance
        final Collection<MetadataRepresentation> representations = underTest.extractMetadata(instance);

        // Sanity check
        assertNotNull(representations);
        assertEquals(1, representations.size());

        // Obtain the MetadataRepresentation
        final MetadataRepresentation<AttributeSet> representation = representations.iterator().next();

        assertEquals(AttributeSetMetadataRepresentation.REPRESENTATION_ID, representation.getRepresentationId());
        assertNotNull(representation.getRepresentation());
        assertEquals(attributeSet, representation.getRepresentation());
    }

    /**
     * Insures that the proper exception is thrown when the extractor receives a MetadataInstance with an unknown
     * format.
     *
     * @throws Exception
     */
    @Test(expected = ExtractionException.class)
    public void testExtractionInvalidMetadataInstanceFormat() throws Exception {
        // Create a MetadataInstance to perform extraction on.  The inputstream doesn't matter. The wonky format
        // id is all that matters
        final AttributeSetMetadataInstance instance = new AttributeSetMetadataInstance(
                "a random format id", new byte[10]);

        // Attempt extraction on the MetadataInstance; we expect a ExtractionException
        underTest.extractMetadata(instance);
    }

    /**
     * Insures that the proper exception is thrown when the extractor receives a MetadataInstance with a zero-length
     * input stream.
     *
     * @throws Exception
     */
    @Test(expected = ExtractionException.class)
    public void testExtractionZeroLengthStream() throws Exception {
        // Create a MetadataInstance to perform extraction on.
        final AttributeSetMetadataInstance instance = new AttributeSetMetadataInstance(
                MetadataFormatId.ATTRIBUTE_SET_METADATA_FORMAT_ID, new byte[10]);

        // Attempt extraction on the MetadataInstance; we expect a ExtractionException
        underTest.extractMetadata(instance);
    }

    /**
     * Insures the proper exception and cause are thrown when the extractor receives a null MetadataInstance.
     *
     * @throws Exception
     */
    @Test
    public void testExtractionNullInstance() throws Exception {
        Throwable caught = null;
        Throwable cause = null;

        try {
            underTest.extractMetadata(null);
        } catch (Exception e) {
            caught = e;
            cause = e.getCause();
        }

        assertNotNull(caught);
        assertEquals(ExtractionException.class, caught.getClass());

        assertNotNull(cause);
        assertEquals(IllegalArgumentException.class, cause.getClass());
    }
}
