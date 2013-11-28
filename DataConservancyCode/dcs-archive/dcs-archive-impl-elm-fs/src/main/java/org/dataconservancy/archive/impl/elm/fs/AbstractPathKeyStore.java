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

import javax.annotation.PostConstruct;

import org.dataconservancy.dcs.util.FilePathKeyAlgorithm;
import org.springframework.beans.factory.annotation.Required;

/**
 * Base class for configuring FS stores based on path key algorithms. * <h2>
 * configuration</h2>
 * <p>
 * <dl>
 * <dt>{@link #setFilePathKeyAlgorithm(FilePathKeyAlgorithm)}</dt>
 * <dd>Required. Should contain a fully configured key algorithm.</dd>
 * <dt>{@link #setBaseDir(String)}</dt>
 * <dd>Required. Defined the base directory in which files will be stored.</dd>
 * </dl>
 * </p>
 */
public class AbstractPathKeyStore {

    private FilePathKeyAlgorithm algorithm;

    private File baseDir;

    @Required
    public void setFilePathKeyAlgorithm(FilePathKeyAlgorithm algo) {
        algorithm = algo;
    }

    protected FilePathKeyAlgorithm getFilePathKeyAlgorithm() {
        return algorithm;
    }

    @Required
    public void setBaseDir(String dir) {
        baseDir = new File(dir);
    }
    
    @PostConstruct
    public void init() {
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    protected String getBaseDir() {
        return baseDir.toString();
    }

    public void remove(String id) {
        File file = getFile(id);
        if (file.exists()) {
            if (!file.delete()) {
                throw new RuntimeException("Could not delete file "
                        + file.getAbsolutePath());
            }
        }
    }

    protected File getFile(String id) {
        return new File(baseDir, algorithm.lookupPathName(id));
    }
}
