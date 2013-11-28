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
package org.dataconservancy.archive.impl.elm.fs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.impl.elm.EntityStore;
import org.dataconservancy.dcs.util.FilePathKeyAlgorithm;

/**
 * Stores entities as individual files on a file system.
 * <p>
 * Uses a {@link FilePathKeyAlgorithm} to name files in a way such that each
 * identifier uniquely and algorithmically maps to a single file name.
 * </p>
 * For configuration, see {@link AbstractPathKeyStore}.
 */
public class FsEntityStore
        extends AbstractPathKeyStore
        implements EntityStore {

    public InputStream get(String entityId) throws EntityNotFoundException {

        try {
            return FileUtils.openInputStream(getFile(entityId));
        } catch (FileNotFoundException e) {
            throw new EntityNotFoundException(entityId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String entityId, InputStream stream) {

        OutputStream out = null;
        try {
            out = FileUtils.openOutputStream(getFile(entityId));
            IOUtils.copy(stream, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {

            }

            try {
                stream.close();
            } catch (IOException e) {

            }
            System.gc();
        }
    }
}
