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

import java.io.InputStream;

/**
 * Produces a single file path according to some algorithm and input.
 */
public interface FilePathSource {

    /**
     * Get the file path.
     * <p>
     * Depending on algorithm and implementation, this may initially be
     * null. Some path algorithm implementations may need to inspect file
     * content in order to determine the final file name. If implemented in
     * a streaming fashion, the path algorithm will compute the file path on
     * the fly when the input stream produced by {@link #getInputStream()}
     * is consumed. If so, getPath() will only return a non-null during or
     * immediately after the inputStream is consumed.
     * </p>
     * 
     * @return file path.
     */
    public String getPathName();

    /**
     * Get an opaque 'key' that can be used to reference a file path.
     * <p>
     * Some path algorithms are able to produce a shortened or abstracted
     * value which can be algorithmically expanded into a full file path.
     * This can be used to retrieve a particular path value via
     * {@link FilePathAlgorithm#lookupPathName(String)}. In the worst case,
     * this value will just be a String containing the full file path, and
     * the lookup is a noop. In the best case, the key will be a
     * significantly shortened or abstracted representation of path.
     * </p>
     * 
     * @return opaque path key.
     */
    public String getPathKey();

    /**
     * Get file input stream for consumption.
     * <p>
     * This stream should be consumed by the caller, rather then the stream
     * instance used to create this FilePathSource in the first place. If
     * the algorithm wishes to wrap the input stream in order to perform
     * in-line streaming analysis, this might return such a wrapped
     * InputStream.
     * </p>
     * 
     * @return file content stream.
     */
    public InputStream getInputStream();

}
