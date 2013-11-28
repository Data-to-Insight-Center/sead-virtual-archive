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

import java.io.File;
import java.io.InputStream;

import java.util.List;

import net.sourceforge.stripes.action.FileBean;

import org.dataconservancy.ui.exceptions.UnpackException;

/**
 * {@code PackageExtractor} is an abstraction that supports operation on {@link FileBean} that are of packaged files, such as .zip files
 */
public interface PackageExtractor {
    
    /**
     * Returns a dataset from the contents of the file.
     * @param baseDir The base directory to extract the files into created under extract directory, can be empty or null.
     * @param file A FileBean object loaded from the UI. Files will be extracted to the current directory.    
     * @return A list of files that were extracted from the package
     */
    public List<File> getFilesFromPackageFileBean(String baseDir, FileBean file) throws UnpackException;
    
    /**
     * Returns a dataset from the contents of the java file object.
     * @param baseDir The base directory to extract the files into created under extract directory, can be empty or null.
     * @param file A java file object representing the package. Files will be extracted to the current directory.
     * @return A list of files that were extracted from the package
     */
    public List<File> getFilesFromPackageFile(String baseDir, File file) throws UnpackException;
    
    /**
     * Deletes all of the files in the provided directory.
     * @param dir The directory of extracted files to be deleted. 
     */
    public void cleanUpExtractedPackage(File dir);
    
    /**
     * Accept an InputStream, and extract the files.
     * @param baseDir The base directory to extract the files into created under extract directory, can be empty or null.
     * @param packageInputStream the InputStream representing a package
     * @return a List of Files in the package
     * @throws UnpackException if there is an error processing the InputStream
     */
    public List<File> getFilesFromPackageStream(String baseDir, String fileName, InputStream packageInputStream) throws UnpackException;
    
    /**
     * Sets the extract directory that the package will be expanded into. 
     * @param dir A string representing the path to the extract directory can be a full or relative path.
     */
    public void setExtractDirectory(String dir);
    
    /**
     * Get the extract directory the files will be unpacked into.
     */
    public String getExtractDirectory();
}