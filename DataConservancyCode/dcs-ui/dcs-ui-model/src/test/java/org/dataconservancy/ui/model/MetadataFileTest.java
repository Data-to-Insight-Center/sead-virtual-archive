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
package org.dataconservancy.ui.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetadataFileTest {

    @Test
    public void testName() {
        String name = "name";
        
        MetadataFile mf = new MetadataFile();
        mf.setName(name);
        
        assertEquals(name, mf.getName());
    }
    
    @Test
    public void testFormat() {
        String fmt = "format";
        
        MetadataFile mf = new MetadataFile();
        mf.setFormat(fmt);
        
        assertEquals(fmt, mf.getFormat());
    }
    
    @Test
    public void testParent() {
        String parent = "parent";
        
        MetadataFile mf = new MetadataFile();
        mf.setParentId(parent);
        
        assertEquals(parent, mf.getParentId());
    }
    
    @Test
    public void testSource() {
        String src = "source";
        
        MetadataFile mf = new MetadataFile();
        mf.setSource(src);
        
        assertEquals(src, mf.getSource());
    }
    
    public void testEquality() {
        MetadataFile mf1 = new MetadataFile();
        MetadataFile mf2 = new MetadataFile();
        
        mf1.setName("name");
        mf1.setFormat("format");
        mf1.setSource("source");
        mf1.setPath("path");
        mf1.setParentId("parent");
        
        mf2.setName(mf1.getName());
        mf2.setFormat(mf1.getFormat());
        mf2.setSource(mf1.getSource());
        mf2.setPath(mf1.getPath());
        mf2.setParentId("parent");
        
        assertTrue(mf1.equals(mf1));
        assertTrue(mf2.equals(mf2));
        assertTrue(mf1.equals(mf2));
        assertTrue(mf2.equals(mf1));
    }
}
