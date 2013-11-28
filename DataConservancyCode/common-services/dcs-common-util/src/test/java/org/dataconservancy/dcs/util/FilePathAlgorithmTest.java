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
package org.dataconservancy.dcs.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import org.dataconservancy.dcs.util.FilePathAlgorithm;
import org.dataconservancy.dcs.util.FilePathSource;


public abstract class FilePathAlgorithmTest {

    @Test
    public void validPathTest() throws Exception {

        String name = prepareSource().getPathName();

        File toCreate = new File(System.getProperty("java.io.tmpdir"), name);
        if (toCreate.getParentFile() != null) {
            toCreate.getParentFile().mkdirs();
        }
        Assert.assertTrue(toCreate.createNewFile());
        toCreate.delete();
    }

    @Test
    public void keyRetrievalTest() throws Exception {
        FilePathSource source = prepareSource();

        Assert.assertNotNull(source.getPathKey());
        Assert.assertEquals(source.getPathName(), getAlgorithm()
                .lookupPathName(source.getPathKey()));
    }

    private FilePathSource prepareSource() throws IOException {
        return prepareSource(getAlgorithm(), new ByteArrayInputStream("Test"
                .getBytes()), null);
    }

    protected static FilePathSource prepareSource(FilePathAlgorithm alg,
                                                  InputStream stream,
                                                  Map<String, String> hints)
            throws IOException {
        FilePathSource source = alg.getPath(stream, hints);
        String name = source.getPathName();
        if (name == null) {
            InputStream sourceStream = source.getInputStream();
            while (sourceStream.read() != -1);
            name = source.getPathName();
        }

        Assert.assertNotNull(name);
        return source;
    }

    protected abstract FilePathAlgorithm getAlgorithm();
}
