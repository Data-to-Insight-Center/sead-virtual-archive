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
import org.apache.maven.plugin.MojoFailureException;
import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.impl.elm.ElmArchiveStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Generates an ELM archive.
 *
 * @goal seed-archive
 * @phase generate-test-resources
 */
public class SeedElmArchiveMojo extends AbstractSeedMojo {


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Preparing archive in directory " + getArchiveDirectory());
        final ElmArchiveStore store = prepareArchive(getElmEntityDirectory(), getElmMetadataDirectory());
        final List<File> entityFiles;
        try {
            entityFiles = FileSetTransformer.toFileList(getEntities());
        } catch (IOException e) {
            throw new MojoExecutionException("Error listing DCP XML files in " + getEntities().getDirectory(), e);
        }

        long start = System.currentTimeMillis();
        int failureCount = 0;
        int count = 0;

        for (File f : entityFiles) {
            try {
                getLog().debug("Putting DCP XML file " + f + " into the archive store (a " +
                        store.getClass().getName() + ")");
                store.putPackage(new FileInputStream(f));
                count++;
            } catch (AIPFormatException e) {
                getLog().warn("Error ingesting DCP XML file " + f + ": " + e.getMessage());
                getLog().warn("Continuing.");
                failureCount++;
            } catch (FileNotFoundException e) {
                getLog().warn("DCP XML file " + f + " was not found.");
                getLog().warn("Continuing.");
                failureCount++;
            }
        }

        getLog().info("Put " + count + " DCP XML packages in the archive store. " +
                "(duration: " + (System.currentTimeMillis() - start) + " ms, " +
                +failureCount + " package(s) failed)");
    }




}
