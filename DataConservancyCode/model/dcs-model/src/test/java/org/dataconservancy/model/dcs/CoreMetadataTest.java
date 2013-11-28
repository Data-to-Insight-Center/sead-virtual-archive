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

import org.dataconservancy.model.dcs.support.CollectionFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.dataconservancy.model.dcs.support.TestAssertion.assertCollectionsEqual;
import static org.dataconservancy.model.dcs.support.TestAssertion.assertCollectionsIdentical;
import static org.dataconservancy.model.dcs.support.TestAssertion.assertCollectionsNotIdentical;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class CoreMetadataTest {

    private Collection<String> creators = CollectionFactory.newCollection();

    private Collection<String> subjects = CollectionFactory.newCollection();

    private String type = "typeString";

    private String title = "titleString";

    private String rights = "rightsString";

    private CoreMetadata orig = new CoreMetadata();

    @Before
    public void setUp() throws Exception {
        creators.add("creator");
        subjects.add("subject");

        orig.setCreators(creators);
        orig.setRights(rights);
        orig.setSubjects(subjects);
        orig.setTitle(title);
        orig.setType(type);
    }

    @Test
    public void testCopyConstructor() throws Exception {
        CoreMetadata copy = new CoreMetadata(orig);

        assertEquals("Original should equal copy.", orig, copy);

        assertCollectionsEqual(orig.getCreators(), copy.getCreators());
        assertCollectionsEqual(orig.getSubjects(), copy.getSubjects());
        assertCollectionsNotIdentical(orig.getCreators(), copy.getCreators());
        assertCollectionsNotIdentical(orig.getSubjects(), copy.getSubjects());
    }

    @Test
    public void testDefensiveCopy() throws Exception {
        assertCollectionsIdentical(subjects, orig.getSubjects());
        assertCollectionsIdentical(creators, orig.getCreators());
    }
}
