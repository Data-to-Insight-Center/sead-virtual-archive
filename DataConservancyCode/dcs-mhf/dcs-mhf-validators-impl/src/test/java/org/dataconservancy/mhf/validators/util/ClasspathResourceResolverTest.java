package org.dataconservancy.mhf.validators.util;

import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.MetadataSerializer;
import org.dataconservancy.registry.impl.metadata.shared.XstreamMetadataFormatSerializer;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Insures that the ClasspathResourceResolverImpl correctly resolves resources.  The initial suite of tests were copied
 * from {@code LSResourceResolverImpl}, and modified to place the ClasspathResourceResolver under test, instead
 * of {@code LSResourceResolverImpl}.
 * <p/>
 * Obviously, any future tests against {@code ClasspathResourceResolver} and {@code LSResourceResolverImpl}
 * should go in their respective test classes.
 */
public class ClasspathResourceResolverTest {

    private static final String FGDC_RESOURCE_BASE = "org/dataconservancy/mhf/resources/schemas/fgdc1998/";

    private static final String REGISTRY_RESOURCE_BASE = "org/dataconservancy/mhf/resources/registry/metadataformat/";

    private static final String FGDC_1998_XSD = "fgdc-std-001-1998.xsd";

    private static final String TEST_RESOURCE_EMPTY_BASE_NAME = "testResource.xml";

    private static final String TEST_RESOURCE_EMPTY_BASE = "";

    private MetadataSerializer<DcsMetadataFormat> serializer = new XstreamMetadataFormatSerializer();

    /**
     * Insures that a resolver with an empty base can resolve the base.
     *
     * @throws Exception
     */
    @Test
    public void testGetResourceBaseWithEmptyBase() throws Exception {
        ClasspathResourceResolver underTest = new ClasspathResourceResolver(TEST_RESOURCE_EMPTY_BASE, serializer);
        assertNotNull(underTest.getResourceBase());
    }

    /**
     * Insures that a resolver with an empty base cannot resolve an empty resource name.
     *
     * @throws Exception
     */
    @Test
    public void testResolveResourceWithEmptyBase() throws Exception {
        ClasspathResourceResolver underTest = new ClasspathResourceResolver(TEST_RESOURCE_EMPTY_BASE, serializer);
        assertNull(underTest.resolve(TEST_RESOURCE_EMPTY_BASE_NAME, ""));
    }

    /**
     * Insures that a resolver with an non-empty base can resolve the base.
     *
     * @throws Exception
     */
    @Test
    public void testGetResourceWithFgdcBase() throws Exception {
        ClasspathResourceResolver underTest = new ClasspathResourceResolver(FGDC_RESOURCE_BASE, serializer);
        assertNotNull(underTest.getResourceBase());
    }

    /**
     * Insures that a resolver with a non-empty base can resolve a resource on a non-empty base.  Note that
     * the resource being resolved is relative to the base.
     *
     * @throws Exception
     */
    @Test
    public void testResolveResourceWithFgdcBase() throws Exception {
        ClasspathResourceResolver underTest = new ClasspathResourceResolver(REGISTRY_RESOURCE_BASE, serializer);
        assertNotNull(underTest.resolve(FGDC_1998_XSD, ""));
    }

    /**
     * Insures that a resolver with a non-empty base returns null for an unresolvable resource.
     *
     * @throws Exception
     */
    @Test
    public void testResolveNonExistentResourceWithFgdcBase() throws Exception {
        ClasspathResourceResolver underTest = new ClasspathResourceResolver(FGDC_RESOURCE_BASE, serializer);
        assertNull(underTest.resolve("foobarbaz", ""));
    }

    /**
     * Insures that a resolver with a non-empty base returns null for an unresolvable resource.
     *
     * @throws Exception
     */
    @Test
    public void testResolveNonExistentResourceWithEmptyBase() throws Exception {
        ClasspathResourceResolver underTest = new ClasspathResourceResolver(TEST_RESOURCE_EMPTY_BASE, serializer);
        assertNull(underTest.resolve("foobarbaz", ""));
    }

}
