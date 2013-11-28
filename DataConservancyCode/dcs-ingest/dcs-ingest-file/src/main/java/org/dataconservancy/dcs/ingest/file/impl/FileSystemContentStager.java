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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.dataconservancy.dcs.ingest.FileContentStager;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.StagedFile;
import org.dataconservancy.dcs.util.FilePathAlgorithm;
import org.dataconservancy.dcs.util.FilePathSource;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Manages File content in a traditional File System.
 * <p>
 * Uses a supplied algorithm to generate and dereference files with a directory
 * structure in a traditional filesystem. Access URIs obtained by
 * {@link StagedFile#getAccessURI()} are <code>file://</code> URIs. Currently,
 * this stager is append-only. {@link #remove(String)} does not remove the file,
 * but instead records the request in a file containing paths of files requested
 * to be deleted.
 * </p>
 * <h2>Configuration</h2>
 * <p>
 * <dl>
 * <dt>{@link #setBaseDir(String)}</dt>
 * <dd>Required. Files will be stored (possably in a hierarchy) underneath the
 * specified base directory.</dd>
 * <dt>{@link #setPathAlgorithm(FilePathAlgorithm)}</dt>
 * <dd>Required. Determines the algorithm for naming files and creating
 * sub-directories.</dd>
 * <dt>{@link #setSipStager(SipStager)}</dt>
 * <dd><b>Required</b>. Used for staging sips associated with file staging.</dd>
 * <dt>{@link #setReferenceBaseURI(String)}</dt>
 * <dd>Optional. All referenceURIs will be composed of an opaque string appended
 * to the end of this URI</dd>
 * <dt>{@link #setDeletionFile(String)}</dt>
 * <dd>Optional. File paths of delete-requested files will be written to the
 * specified file, overriding the default value.</dd>
 * </dl>
 * </p>
 */
public class FileSystemContentStager
        implements FileContentStager {

    private static final Logger log =
            LoggerFactory.getLogger(FileSystemContentStager.class);

    private static final String FILE_URI_PREFIX = "file://";

    private File baseDir;

    private File deletionFile;

    private FilePathAlgorithm pathAlgorithm;

    private String referenceBaseURI = "urn:dataconservancy.org:file/";

    private SipStager sipStager;

    @Required
    public void setSipStager(SipStager stager) {
        sipStager = stager;
    }

    @Required
    public void setBaseDir(String dir) {
        baseDir = new File(dir);
        if (deletionFile == null) {
            deletionFile = new File(baseDir, "DELETE.txt");
        }
    }

    @Required
    public void setPathAlgorithm(FilePathAlgorithm algo) {
        pathAlgorithm = algo;
    }

    public void setReferenceBaseURI(String uri) {
        referenceBaseURI = uri;
    }

    public void setDeletionFile(String file) {
        deletionFile = new File(file);
    }

    File getDeletionFile() {
        return deletionFile;
    }

    public StagedFile add(InputStream stream, Map<String, String> metadata) {

        if (baseDir == null) {
            throw new NullPointerException("Base directory not set!");
        }

        FilePathSource path = pathAlgorithm.getPath(stream, metadata);

        String initialPath = null;

        if (path.getPathName() != null) {
            initialPath = path.getPathName();

            File initialFile = new File(baseDir, initialPath);
            if (initialFile.exists() && pathAlgorithm.isContentAddressable()) {
                return new FsStagedFile(path, createSip(path.getPathKey()));
            }
        } else {
            initialPath = getTempPath();
        }

        try {
            File initialFile = new File(baseDir, initialPath);

            FileOutputStream out = FileUtils.openOutputStream(initialFile);
            InputStream source = path.getInputStream();
            IOUtils.copy(source, out);
            out.close();
            source.close();

            if (!path.getPathName().equals(initialPath)) {
                File dest = new File(baseDir, path.getPathName());

                if (dest.exists() && pathAlgorithm.isContentAddressable()) {
                    initialFile.delete();
                    return new FsStagedFile(path, createSip(path.getPathKey()));
                } else {
                    FileUtils.moveFile(initialFile, dest);
                }
            }
        } catch (IOException e) {
            log.error("Error storing file: " + e.getMessage());
            throw new RuntimeException("Could not store content", e);
        }

        return new FsStagedFile(path, createSip(path.getPathKey()));
    }

    public boolean contains(String id) {
        if (id == null) return false;

        if (id.startsWith(referenceBaseURI)) {
            id = id.replace(referenceBaseURI, "");
        }

        return get(id) != null;
    }

    /**
     * Request the removal of a staged file.
     * <p>
     * This does not directly remove a file, but instead appends the full file
     * path of a deleted file to <code>$baseDir/DELETE.txt</code>, or another
     * file specified by {@link #setDeletionFile(String)}.
     * </p> {@inheritDoc}
     */
    public void remove(String id) {

        if (id.startsWith(referenceBaseURI)) {
            id = id.replace(referenceBaseURI, "");
        }

        FsStagedFile file = (FsStagedFile) get(id);

        if (file != null && file.exists()) {
            try {
                sipStager.removeSIP(id);
                OutputStream out = openDeletionFile();
                IOUtils.write(file.getPath() + "\n", out);
                out.close();
            } catch (IOException e) {
                /* We don't make this fatal, just log */
                log
                        .warn(String
                                      .format("Could not apppend deletion request for '%s' to '%s'",
                                              file.getPath(),
                                              deletionFile.getAbsolutePath()),
                              e);
            }
        }
    }

    public void retire(String id) {
        if (id.startsWith(referenceBaseURI)) {
            id = id.replace(referenceBaseURI, "");
        }

        sipStager.retire(id);
    }

    public StagedFile get(String id) {
        String sipRef = id;

        if (id.startsWith(referenceBaseURI)) {
            sipRef = id.replace(referenceBaseURI, "");
        }

        String key = getPathKeyFromSip(sipRef);

        if (key != null) {
            FsStagedFile sf =
                    new FsStagedFile(pathAlgorithm.lookupPathName(key),
                                     key,
                                     sipRef);
            if (sf.exists()) {
                return sf;
            }
        }
        return null;
    }

    private String getTempPath() {
        return baseDir.getName() + UUID.randomUUID().toString();
    }

    private OutputStream openDeletionFile() throws IOException {
        if (deletionFile.exists()) {
            return new FileOutputStream(deletionFile, true);
        } else {
            return FileUtils.openOutputStream(deletionFile);
        }
    }

    private String getPathKeyFromSip(String sipid) {
        Dcp sip = sipStager.getSIP(sipid);

        if (sip != null) {
            for (DcsFile file : sip.getFiles()) {
                return file.getId().replace(referenceBaseURI, "");
            }
        }
        return null;
    }

    private String createSip(String key) {
        Dcp sip = new Dcp();
        String id = sipStager.addSIP(new Dcp());

        DcsFile file = new DcsFile();
        file.setId(referenceBaseURI + key);
        file.setSource(referenceBaseURI + id);
        sip.addFile(file);

        sipStager.updateSIP(sip, id);

        return id;
    }

    class FsStagedFile
            implements StagedFile {

        private final File file;

        private final String sipid;

        public FsStagedFile(FilePathSource src, String sipRef) {
            file = new File(baseDir, src.getPathName());
            sipid = sipRef;
        }

        public FsStagedFile(String pathNane, String key, String sipRef) {
            file = new File(baseDir, pathNane);
            sipid = sipRef;
        }

        public String getAccessURI() {
            return FILE_URI_PREFIX + file.getAbsolutePath();
        }

        public InputStream getContent() {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public String getReferenceURI() {
            return referenceBaseURI + sipid;
        }

        public String getPath() {
            return file.getAbsolutePath();
        }

        public boolean exists() {
            return file.exists();
        }

        public String getSipRef() {
            return sipid;
        }
    }
}
