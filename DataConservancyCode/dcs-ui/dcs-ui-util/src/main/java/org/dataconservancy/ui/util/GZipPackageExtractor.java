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
package org.dataconservancy.ui.util;

import org.dataconservancy.ui.exceptions.UnpackException;

import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class GZipPackageExtractor extends BasePackageExtractor {

    private File compressedFile;

    /**
     * {@inheritDoc}
     * Note: Due to the nature of GZIP files this method will only return a single file in the list.
     */
    @Override
    protected List<File> unpackFilesFromArchive(File archive, String packageDir) throws Exception {
        FileInputStream fileStream = new FileInputStream(archive);

        //Create a new file from the gzipped file name minus the extension
        this.compressedFile = new File(packageDir,
                archive.getName().substring(0, archive.getName().lastIndexOf('.')));

        //Create a GZIPInputStream to uncompress the file 
        GZIPInputStream gzipStream = new GZIPInputStream(fileStream);

        return unpackFilesFromStream(gzipStream, packageDir, archive.getName());
    }

    @Override
    protected List<File> unpackFilesFromStream(InputStream packageInputStream, String packageDir, String fileName) throws UnpackException {
        final GZIPInputStream gzipStream;

        if (!GZIPInputStream.class.isAssignableFrom(packageInputStream.getClass())) {
            try {
                gzipStream = new GZIPInputStream(packageInputStream);
            } catch (IOException e) {
                final String msg = "Error creating GZIPInputStream: " + e.getMessage();
                log.error(msg, e);
                throw new UnpackException(msg, e);
            }
        } else {
            gzipStream = (GZIPInputStream)packageInputStream;
        }

        List<File> files = new ArrayList<File>();
        try {
            // Create a new file from the gzipped file name minus the extension
            if (this.compressedFile == null) {
                this.compressedFile = new File(packageDir,
                                               fileName.substring(0, fileName.lastIndexOf('.')));
            }

            //Save the extracted file into the new file
            List<File> savedFiles = saveExtractedFile(compressedFile, gzipStream);
            //Add the extracted file to the list, there should be only one file at this point.
            files.addAll(savedFiles);
        } catch (Exception e) {
            this.compressedFile = null;
            final String msg = "Error processing GZIPInputStream: " + e.getMessage();
            log.error(msg, e);
            throw new UnpackException(msg, e);
        }

        // reset internal state
        this.compressedFile = null;

        return files;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String truncatePackageExtension(String fileName) {
        //Remove the gzip extension from the file could be .gz or .gzip
        String gzipRemoved = fileName.substring(0, fileName.lastIndexOf('.'));
        
        //Remove the remaining extension from the file
        return gzipRemoved.substring(0, gzipRemoved.lastIndexOf('.'));
    }
    
}