package org.dataconservancy.mhf.validators.util;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class XsdHttpResourceResolverTest {
    
    /**
     * Tests that a known url will resolve and return a non null result.
     */
    @Test
    public void testFullUrlResolves() {
        XsdHttpResourceResolver resolver = new XsdHttpResourceResolver();
        assertNotNull(resolver.resolve("http://www.google.com", ""));
    }
    
    /**
     * Tests that a base url correctly combines with a resource id, this is a contrived example
     */
    @Test
    public void testCombinedUrlResolves() {
        XsdHttpResourceResolver resolver = new XsdHttpResourceResolver();
        assertNotNull(resolver.resolve("www.google.com", "http://"));
    }
    
    /**
     * Tests that just a partial url returns null, i.e. an included schema name without the full url.
     */
    @Test
    public void testPartialUrlReturnsNull() {
        XsdHttpResourceResolver resolver = new XsdHttpResourceResolver();
        assertNull(resolver.resolve("fgdc-std-001-1998-sect01.xsd", "http://www.foo.com"));
    }
    
    /**
     * Tests that an invalid url will return null.
     */
    @Test
    public void testInvalidUrlReturnsNull() {
        XsdHttpResourceResolver resolver = new XsdHttpResourceResolver();
        assertNull(resolver.resolve("www.foo.baz", ""));
    }
}