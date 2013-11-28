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
package org.dataconservancy.dcs.ingest.file.impl;

import java.util.UUID;

import org.apache.commons.io.IOUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.ingest.FileContentStager;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.StagedFile;
import org.dataconservancy.model.dcp.Dcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class FileContentStagerTest {

    private final String CONTENT = this.getClass().getName();

    private FileContentStager stager;

    @Before
    public void setStager() {
        stager = getStager();
    }

    @Test
    public void directContentRetrievalTest() throws Exception {

        StagedFile file = stager.add(IOUtils.toInputStream(CONTENT), null);
        assertEquals(CONTENT, IOUtils.toString(file.getContent()));

        stager.remove(file.getReferenceURI());
    }

    @Test
    public void retrievalByReferenceUriTest() throws Exception {

        StagedFile file = stager.add(IOUtils.toInputStream(CONTENT), null);
        file = stager.get(file.getReferenceURI());

        Assert.assertNotNull(file);
        assertEquals(CONTENT, IOUtils.toString(file.getContent()));

        stager.remove(file.getReferenceURI());
    }

    @Test
    public void retrievalOfNonExistingTest() throws Exception {
        Assert.assertNull(stager.get(UUID.randomUUID().toString()));
    }

    @Test
    public void sipCreationTest() throws Exception {
        StagedFile file = stager.add(IOUtils.toInputStream(CONTENT), null);
        Dcp sip = getSipStager().getSIP(file.getSipRef());
        Assert.assertNotNull(sip);

        assertEquals(1, sip.getFiles().size());

        assertEquals(file.getReferenceURI(), sip.getFiles().iterator().next()
                .getSource());
    }

    @Test
    public void containmentTest() throws Exception {
        StagedFile file = stager.add(IOUtils.toInputStream(CONTENT), null);
        assertTrue(stager.contains(file.getReferenceURI()));
    }

    @Test
    public void nonContainmentTest() throws Exception {
        Assert.assertFalse(stager.contains(UUID.randomUUID().toString()));
    }

    @Test
    public void removalTest() throws Exception {
        StagedFile file = stager.add(IOUtils.toInputStream(CONTENT), null);
        String sipRef = file.getSipRef();

        stager.remove(file.getReferenceURI());
        Assert.assertFalse(stager.contains(file.getReferenceURI()));
        Assert.assertNull(getSipStager().getSIP(sipRef));
    }

    @Test
    public void removalOfNonexistingTest() {
        stager.remove(UUID.randomUUID().toString());
    }

    protected abstract FileContentStager getStager();

    protected abstract SipStager getSipStager();
}
