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

import java.io.File;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.dataconservancy.archive.impl.elm.Metadata;
import org.dataconservancy.archive.impl.elm.MetadataStore;
import org.dataconservancy.dcs.util.FilePathKeyAlgorithm;

/**
 * Stores metadata as individual files on a filesystem.
 * <p>
 * Uses a {@link FilePathKeyAlgorithm} to name files in a way such that each
 * identifier uniquely and algorithmically maps to a single file, which contains
 * metadata.
 * </p>
 * <p>
 * As a practical note, if used in conjunction with {@link FsEntityStore} and
 * under the same base directory, with the same park algorithm, then they each
 * must be configured to use different file suffixes.
 * </p>
 * For configuration, see {@link AbstractPathKeyStore}.
 */
public class FsMetadataStore
        extends AbstractPathKeyStore
        implements MetadataStore {

    public Metadata get(String id) {
        File file = getFile(id);
        if (file.exists()) {
            return new FsMetadata(file);
        } else {
            return null;
        }
    }

    public Iterable<Metadata> getAll(final String... types) {

        final List<String> accepted = Arrays.asList(types);

        return new Iterable<Metadata>() {

            @SuppressWarnings("unchecked")
            public Iterator<Metadata> iterator() {

                final Iterator<File> fileIterator =
                        FileUtils.iterateFiles(new File(getBaseDir()),
                                               getSuffix(),
                                               true);

                return new Iterator<Metadata>() {

                    private Metadata next;

                    {
                        advanceNext();
                    }

                    public boolean hasNext() {
                        return next != null;
                    }

                    public Metadata next() {
                        Metadata toReturn = next;
                        advanceNext();
                        return toReturn;
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                    private void advanceNext() {

                        while (fileIterator.hasNext()) {
                            File candidate = fileIterator.next();
                            Metadata m = new FsMetadata(candidate);
                            if (accepted.isEmpty()
                                    || accepted.contains(m.getType())) {
                                next = m;
                                return;
                            }
                        }
                        next = null;
                    }
                };
            }
        };
    }

    public Metadata add(String id, String type, String src) {
        File file = getFile(id);
        if (file.exists()) {
            FsMetadata existing = new FsMetadata(file);
            if (!id.equals(existing.getId())) {
                throw new RuntimeException("Error saving metadata for " + id
                        + ", there is already a file for " + existing.getId()
                        + " at " + file.getAbsolutePath());
            }
            return existing;
        } else {
            return new FsMetadata(file, id, type, src);
        }
    }

    public boolean isReadOnly() {
        return false;
    }

    private String[] getSuffix() {
        String suffix = getFilePathKeyAlgorithm().getSuffix();
        if (suffix == null || suffix.equals("")) {
            return null;
        } else {
            return new String[] {suffix.replaceFirst("\\.", "")};
        }
    }
}
