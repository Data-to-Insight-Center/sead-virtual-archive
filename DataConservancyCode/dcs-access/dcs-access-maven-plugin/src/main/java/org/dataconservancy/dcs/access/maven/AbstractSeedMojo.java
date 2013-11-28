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
package org.dataconservancy.dcs.access.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.dataconservancy.archive.impl.elm.ElmArchiveStore;
import org.dataconservancy.archive.impl.elm.fs.FsEntityStore;
import org.dataconservancy.archive.impl.elm.fs.FsMetadataStore;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;

import java.io.File;

/**
 * Provides access to Data Conservancy entities and file streams.  The entities and file streams might be used to seed
 * and index or to seed an archive store.
 */
public abstract class AbstractSeedMojo extends AbstractAccessMavenMojo {

    /**
     * Specifies the DCS entities available for seeding.  Each file in the directory is a DCP package serialized as XML.
     *
     * @parameter
     */
    private AccessFileSet entities;

//    /**
//     * Specifies binary streams available for seeding.
//     *
//     * @parameter
//     */
//    TODO support indexing binary file streams
//    private AccessFileSet streams;

    /**
     * The directory where the Solr index is located, or the directory where the index is to be created.  If the index
     * directory doesn't exist, it will be created.
     *
     * @parameter default-value="${project.build.testOutputDirectory}/elm-archive-index" expression="${archive.elm.indexdir}"
     * @required
     */
    private File indexDirectory;

    /**
     * The directory where the ELM archive store is located, or the directory where the store is to be created.  If the
     * archive directory doesn't exist it will be created.
     *
     * @parameter default-value="${project.build.testOutputDirectory}/elm-archive-store" expression="${archive.elm.basedir}"
     * @required
     */
    private File archiveDirectory;

    /**
     * The directory where the ELM entity store is located.  If the directory doesn't exist it will
     * be created.  By default this directory
     *
     * @parameter default-value="${project.build.testOutputDirectory}/elm-archive-store/entities"
     * @required
     */
    private File elmEntityDirectory;

    /**
     * The directory where the ELM metadata store is located.  If the directory doesn't exist it will
     * be created.
     *
     * @parameter default-value="${project.build.testOutputDirectory}/elm-archive-store/metadata"
     * @required
     */
    private File elmMetadataDirectory;


    public AccessFileSet getEntities() {
        return entities;
    }

    public File getIndexDirectory() {
        return indexDirectory;
    }

    public File getArchiveDirectory() {
        return archiveDirectory;
    }

    public File getElmEntityDirectory() {
        return elmEntityDirectory;
    }

    public File getElmMetadataDirectory() {
        return elmMetadataDirectory;
    }

    /**
     * Prepares an instance of the ElmArchiveStore.
     *
     * @param entityDirectory the ELM entity directory.  Will be created if it doesn't exist.
     * @param metadataDirectory the ELM metadata directory.  Will be created if it doesn't exist.
     * @return an instance of ElmArchiveStore
     * @throws MojoExecutionException if there is an issue preparing the archive.
     */
    protected ElmArchiveStore prepareArchive(File entityDirectory, File metadataDirectory) throws MojoExecutionException {
        final ElmArchiveStore elmArchive = new ElmArchiveStore();

        final KeyDigestPathAlgorithm keyDigestPathAlgorithm = new KeyDigestPathAlgorithm("MD5", 2, 2, null);
        final FsMetadataStore mdStore = new FsMetadataStore();
        final FsEntityStore entityStore = new FsEntityStore();

        prepareDirectory(entityDirectory);
        prepareDirectory(metadataDirectory);

        mdStore.setBaseDir(metadataDirectory.getAbsolutePath());
        mdStore.setFilePathKeyAlgorithm(keyDigestPathAlgorithm);
        entityStore.setBaseDir(entityDirectory.getAbsolutePath());
        entityStore.setFilePathKeyAlgorithm(keyDigestPathAlgorithm);
        elmArchive.setMetadataStore(mdStore);
        elmArchive.setEntityStore(entityStore);
        elmArchive.init();

        return elmArchive;
    }

    /**
     * Ensures that <code>dir</code> exists, is a directory, and is readable.
     *
     * @param dir the directory
     * @throws MojoExecutionException if there is a problem reading or creating the directory
     */
    protected final void prepareDirectory(File dir) throws MojoExecutionException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new MojoExecutionException("File " + dir + " is not a directory.");
            }
            if (!dir.canRead()) {
                throw new MojoExecutionException("Cannot read directory " + dir + ", please check its permissions.");
            }
        } else {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new MojoExecutionException("Unable to create directory " + dir);
            }
        }
    }
}
