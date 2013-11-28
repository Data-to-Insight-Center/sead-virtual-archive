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
package org.dataconservancy.dcs.ingest.services.util;

import org.junit.Test;

import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.dcs.ingest.test.SipStagerTest;
import org.dataconservancy.model.dcp.Dcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ReadingSipStagerTest
        extends SipStagerTest {

    protected SipStager getSipStager() {
        ReadingSipStager stager = new ReadingSipStager();
        stager.setReadableStager(new MemoryStager());
        stager.setWritableStager(new MemoryStager());
        return stager;
    }

    @Test
    public void sipFromSingleReaderTest() {
        Dcp dcp = new Dcp();

        MemoryStager writable = new MemoryStager();
        MemoryStager readable = new MemoryStager();

        ReadingSipStager stager = new ReadingSipStager();
        stager.setWritableStager(writable);
        stager.setReadableStager(readable);
        
        String id = readable.addSIP(dcp);
        
        assertNotNull(readable.getSIP(id));
        assertNull(writable.getSIP(id));
        assertEquals(dcp, stager.getSIP(id));
    }

    @Test
    public void sipFromMultipleReadersTest() {
        Dcp dcp = new Dcp();

        MemoryStager writable = new MemoryStager();
        MemoryStager readable1 = new MemoryStager();
        MemoryStager readable2 = new MemoryStager();


        ReadingSipStager stager = new ReadingSipStager();
        stager.setWritableStager(writable);
        stager.setReadableStager(readable1, readable2);
        
        String id = readable2.addSIP(dcp);
        
        assertNotNull(readable2.getSIP(id));
        assertNull(readable1.getSIP(id));
        assertNull(writable.getSIP(id));
        assertEquals(dcp, stager.getSIP(id));      
    }
}
