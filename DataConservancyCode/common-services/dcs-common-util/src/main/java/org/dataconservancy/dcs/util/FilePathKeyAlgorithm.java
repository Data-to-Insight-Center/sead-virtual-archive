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
 * Provides file paths based on an algorithm which must use keys if provided.
 */
public interface FilePathKeyAlgorithm
        extends FilePathAlgorithm {

    /**
     * Produce a file path that will be retrievable using the supplied key.
     * <p>
     * The underlying algorithm will generate the file path in a manner such
     * that it will be retrievable using the provided key. If this cannot be
     * accomplished, a runtime exception will be thrown.
     * </p>
     * 
     * @param stream
     * @param key
     *        mandatory file path key to use.
     * @param hints
     *        Optional map of key-value pairs that may optimize the file path
     *        algorithm.
     * @return FilePathSource that will adopt the provided key.
     */
    public FilePathSource getPath(InputStream stream,
                                  String key,
                                  Map<String, String> hints);
    
    public String getSuffix();
}
