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
package org.dataconservancy.model.dcs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class DcsCollectionTest {

    @Test
    public void testCopyConstructor() throws Exception {
        final String id = "urn:one";
        final String parentId = "urn:zero";
        final String title = "one";
        final DcsCollectionRef parent = new DcsCollectionRef();
        parent.setRef(parentId);
        final DcsMetadata md = new DcsMetadata();
        md.setSchemaUri("urn:foo");

        final DcsCollection one = new DcsCollection();
        one.setId(id);
        one.setTitle(title);
        one.setParent(parent);
        one.addMetadata(md);
        one.setParent(parent);

        final DcsCollection two = new DcsCollection(one);
        assertEquals(one, two);
        assertEquals(one.getId(), two.getId());
        assertEquals(one.getTitle(), two.getTitle());
        assertEquals(one.getParent(), two.getParent());
        assertEquals(one.getMetadata(), two.getMetadata());

        assertFalse(one == two);
    }

    @Test
    public void testEquals() throws Exception {
        final DcsMetadata md = new DcsMetadata();
        md.setSchemaUri("urn:metadata");
        final String parentId = "urn:zero";
        final DcsCollectionRef parent = new DcsCollectionRef();
        parent.setRef(parentId);

        final String id = "urn:one";
        final String title = "one";

        final String idTwo = "urn:two";
        final String titleTwo = "two";


        final DcsCollection one = new DcsCollection();
        one.setId(id);
        one.setTitle(title);
        one.setParent(parent);
        one.addMetadata(md);

        final DcsCollectionRef twoParent = new DcsCollectionRef();
        twoParent.setRef(parentId);

        final DcsCollection two = new DcsCollection();
        two.setId(idTwo);
        two.setTitle(titleTwo);
        two.setParent(parent);
        two.addMetadata(md);

        final DcsCollection onePrime = new DcsCollection(one);
        final DcsCollection oneDoublePrime = new DcsCollection(one);

        // symmetric
        assertTrue(one.equals(one));

        // reflexive
        assertTrue(one.equals(onePrime) && onePrime.equals(one));
        assertFalse(one.equals(two) && two.equals(one));

        // transitive
        assertTrue(one.equals(onePrime));
        assertTrue(one.equals(oneDoublePrime));
        assertTrue(onePrime.equals(oneDoublePrime));
        
        // consistent
        assertTrue(one.equals(onePrime) && one.equals(onePrime));
        assertFalse(two.equals(one) && two.equals(one));
    }

    @Test
    public void testTitleOk() throws Exception {
        final DcsCollection c = new DcsCollection();
        assertNull(c.getTitle());
        c.setTitle("foo");
        assertEquals("foo", c.getTitle());
        c.setTitle("bar");
        assertEquals("bar", c.getTitle());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTitleZeroLength() throws Exception {
        final DcsCollection c = new DcsCollection();
        c.setTitle("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTitleEmpty() throws Exception {
        final DcsCollection c = new DcsCollection();
        c.setTitle(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTitleNull() throws Exception {
        final DcsCollection c = new DcsCollection();
        c.setTitle(null);
    }

    @Test
    public void testParentOk() throws Exception {
        final DcsCollection c = new DcsCollection();
        assertNull(c.getParent());

        final DcsCollectionRef parent = new DcsCollectionRef();
        parent.setRef("parent");
        c.setParent(parent);
        assertEquals(parent, c.getParent());

        final DcsCollectionRef anotherParent = new DcsCollectionRef();
        anotherParent.setRef("another parent");
        c.setParent(anotherParent);
        assertEquals(anotherParent, c.getParent());
        c.setParent(null);
        assertEquals(null, c.getParent());        
    }

    @Test
    public void testMetadataOk() throws Exception {
        final DcsCollection c = new DcsCollection();
        final DcsMetadata md = new DcsMetadata();
        assertNotNull(c.getMetadata());
        assertTrue(c.getMetadata().isEmpty());
        c.addMetadata(md);
        assertEquals(md, c.getMetadata().iterator().next());
        assertTrue(md == c.getMetadata().iterator().next());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMetadataNull() throws Exception {
        final DcsCollection c = new DcsCollection();
        c.setMetadata(null);
    }
}
