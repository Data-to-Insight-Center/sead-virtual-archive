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

import java.util.Map;

/**
 * Produces relative file paths based on some algorithm.
 * <p>
 * Several different path algorithms are supported, including those that can
 * simply generate a path <em>a priori</em> and those that need to inspect the
 * entire file content before producing a file name <em>a posteriori</em>.
 * </p>
 * <p>
 * In order to cope with the possibility of different requirements and
 * behaviours of different algorithms, usage should generally follow the
 * following pattern:
 * </p>
 * <p>
 * 
 * <pre>
 * FilePathSource path = getPath(in, hints);
 * String pathName = path.getPathName();
 * 
 * if (pathName != null) {
 *      writeFile(in, base + pathName);
 * } else {
 *      writeFile(path.getInputStream(), temporaryPathName);
 *      renameFile(temporaryPathName, base + path.getPathName();
 * }
 * </pre>
 * </p>
 */
public interface FilePathAlgorithm {

    /**
     * Produce a {@linkplain FilePathSource} for generating a single file path
     * location.
     * <p>
     * The file path source will derive a single file path based on the a file
     * path algorithm, and some input.
     * </p>
     * 
     * @param in
     *        InputStream representing the content that will be stored at the
     *        file path location.
     * @param hints
     *        Optional map of key-value pairs that may optimize the file path
     *        algorithm.
     * @return FilePathSource that will generate the file path.
     */
    public FilePathSource getPath(InputStream in, Map<String, String> hints);

    /**
     * Derive a full file path given a path key.
     * <p>
     * Uses path keys produced from {@link FilePathSource#getPathKey()} to
     * lookup/derive the full file path the key represents./?
     * </p>
     * 
     * @param pathKey
     *        String containing path key.
     * @return String containing a relative file path, or null if it cannot be
     *         derived from the key.
     */
    public String lookupPathName(String pathKey);

    /**
     * Determine if a file path is derived from file content.
     * <p>
     * If a file path is derived from file content, then identical file content
     * can and will map to the same file path.
     * </p>
     * 
     * @return true if paths are content addressable.
     */
    public boolean isContentAddressable();
}
