package org.dataconservancy.mhf.validators.dom.impl;

import org.dataconservancy.mhf.validators.registry.impl.SchemaRegistryImpl;
import org.dataconservancy.mhf.validators.util.DcsMetadataSchemeResourceResolver;
import org.dataconservancy.mhf.validators.util.MetadataFormatLoader;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.registry.impl.metadata.shared.MetadataFormatMapper;
import org.dataconservancy.registry.impl.metadata.shared.MetadataSchemeMapper;
import org.junit.Test;
import org.w3c.dom.ls.LSInput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * This test class is completely out of date.  It doesn't really test much.  It is being left here (for now) to insure
 * that the behavior of LSResourceResolverImpl remains unchanged, but the javadoc comments on the test methods are not
 * really useful or enlightening.
 * <p/>
 * The tests that were here weren't really testing LSResourceResolverImpl, they were testing ClasspathResourceResolver,
 * and that now has its own test.
 */
public class LSResourceResolverImplTest {

    private static final String FGDC_1998_XSD = "fgdc-std-001-1998.xsd";

    private static final String TEST_RESOURCE_EMPTY_BASE_NAME = "XMLSchema.xsd";

    private LSResourceResolverImpl underTest = new LSResourceResolverImpl(new DcsMetadataSchemeResourceResolver(
                    new SchemaRegistryImpl("ignored", "ignored", new MetadataFormatLoader(
                            new MetadataFormatMapper(new MetadataSchemeMapper()), new DcsXstreamStaxModelBuilder()))));

    /**
     * Insures that a resolver with an empty base cannot resolve an empty resource path.
     *
     * @throws Exception
     */
    @Test
    public void testGetEmptyResourceBaseWithEmptyBase() throws Exception {
        assertNull(underTest.resolveResource(null, null, null, "", null));
    }

    /**
     * Insures that a resolver with an empty base can resolve a resource at the base.
     *
     * @throws Exception
     */
    @Test
    public void testResolveResourceWithEmptyBase() throws Exception {
        final LSInput lsInput = underTest.resolveResource(null, null, null, TEST_RESOURCE_EMPTY_BASE_NAME, null);
        assertNotNull(lsInput);
        assertEquals(TEST_RESOURCE_EMPTY_BASE_NAME, lsInput.getSystemId());
    }

    /**
     * Insures that a resolver with an non-empty base cannot resolve an empty resource path.
     *
     * @throws Exception
     */
    @Test
    public void testGetEmptyResourceWithFgdcBase() throws Exception {
        assertNull(underTest.resolveResource(null, null, null, "", null));
    }

    /**
     * Insures that a resolver with a non-empty base can resolve a resource on a non-empty base.  Note that
     * the resource being resolved is relative to the base.
     *
     * @throws Exception
     */
    @Test
    public void testResolveResourceWithFgdcBase() throws Exception {
        assertNotNull(underTest.resolveResource(null, null, null, FGDC_1998_XSD, null));
    }

    /**
     * Insures that a resolver with a non-empty base returns null for an unresolvable resource.
     *
     * @throws Exception
     */
    @Test
    public void testResolveNonExistentResourceWithFgdcBase() throws Exception {
        assertNull(underTest.resolveResource(null, null, null, "foobarbaz", null));
    }

    /**
     * Insures that a resolver with a non-empty base returns null for an unresolvable resource.
     *
     * @throws Exception
     */
    @Test
    public void testResolveNonExistentResourceWithEmptyBase() throws Exception {
        assertNull(underTest.resolveResource(null, null, null, "foobarbaz", null));
    }

}
