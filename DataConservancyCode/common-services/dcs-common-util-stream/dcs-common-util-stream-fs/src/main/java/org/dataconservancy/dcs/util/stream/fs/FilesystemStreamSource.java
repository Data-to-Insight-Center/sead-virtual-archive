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
package org.dataconservancy.dcs.util.stream.fs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.dataconservancy.dcs.util.stream.api.StreamSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 */
public class FilesystemStreamSource implements StreamSource {

    private final File baseDir;
    private final IOFileFilter streamfilter;

    public FilesystemStreamSource(File baseDir) {
        if (baseDir == null) {
            throw new IllegalArgumentException("Base directory must not be null.");
        }
        this.baseDir = baseDir;
        streamfilter = TrueFileFilter.TRUE;
    }

    public FilesystemStreamSource(File baseDir, IOFileFilter streamfilter) {
        if (baseDir == null) {
            throw new IllegalArgumentException("Base directory must not be null.");
        }
        if (streamfilter == null) {
            throw new IllegalArgumentException("Stream filter must not be null.");
        }
        this.baseDir = baseDir;
        this.streamfilter = streamfilter;
    }

    @Override
    public Iterable<String> streams() {
        final Collection<String> streamIds = new HashSet<String>();
        for( File f : (Iterable<File>) FileUtils.listFiles(baseDir, streamfilter, TrueFileFilter.TRUE) ) {
            streamIds.add(f.getAbsolutePath());
        }
        return streamIds;
    }

    @Override
    public Iterable<String> streams(Calendar modifiedSince) {
        if (modifiedSince == null) {
            throw new IllegalArgumentException("modified since must not be null.");
        }
        final Collection<String> streamIds = new HashSet<String>();
        final ModifiedSinceFileFilter modifiedSinceFileFilter = new ModifiedSinceFileFilter(modifiedSince);
        final AndFileFilter filter = new AndFileFilter(modifiedSinceFileFilter, streamfilter);
        for( File f : (Iterable<File>)FileUtils.listFiles(baseDir, filter, TrueFileFilter.TRUE) ) {
            streamIds.add(f.getAbsolutePath());
        }
        return streamIds;
    }

    @Override
    public InputStream getStream(String id) throws IOException {
        if (!id.startsWith(baseDir.getAbsolutePath())) {
            return new FileInputStream(new File(baseDir, id));
        }
        return new FileInputStream(new File(id));
    }

    protected File getBaseDir() {
        return baseDir;
    }

    protected IOFileFilter getStreamfilter() {
        return streamfilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilesystemStreamSource that = (FilesystemStreamSource) o;

        if (baseDir != null ? !baseDir.equals(that.baseDir) : that.baseDir != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return baseDir != null ? baseDir.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Base directory: " + baseDir.getAbsolutePath();
    }

}
