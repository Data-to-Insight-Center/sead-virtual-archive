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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.FileBean;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import org.dataconservancy.ui.exceptions.UnpackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePackageExtractor implements PackageExtractor {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected String extractDirectory;

    @Override 
    public void setExtractDirectory(String dir) {
        extractDirectory = dir;
    }
    
    @Override
    public List<File> getFilesFromPackageFileBean(String baseDir, FileBean fileBean)
            throws UnpackException {
        String fileName = fileBean.getFileName();
        
        String packageDir = createParentDir(baseDir, extractDirectory);

        List<File> fileList = new ArrayList<File>();

        try {
            if (fileBean.getInputStream() != null) {
                fileList = unpackFilesFromStream(fileBean.getInputStream(), packageDir, fileBean.getFileName());            
            }
        } catch (Exception e) {
            final String message = "Error unpacking package file " + fileBean.getFileName() + " was either empty, or a not " +
                    "a known package file type";
            UnpackException ue = new UnpackException(message, e);
            ue.setError(message);
            ue.setFilename(fileBean.getFileName());
            throw ue;
        }
        
        return fileList;
    }

    @Override
    public List<File> getFilesFromPackageFile(String baseDir, File file)
            throws UnpackException {

        String packageDir = createParentDir(baseDir, extractDirectory);
        
        List<File> fileList = new ArrayList<File>();
        try {
            fileList = unpackFilesFromArchive(file, packageDir);
        } catch (Exception e) {
            final String message = "Error unpacking provided file [" + file.getName() + "]. It was " +
                    "either empty, or a not a known package file type";
            UnpackException ue = new UnpackException(message, e);
            ue.setError(message);
            ue.setFilename(file.getName());
            throw ue;
        }
        return fileList;
    }

    @Override
    public List<File> getFilesFromPackageStream(String baseDir, String fileName, InputStream packageStream) throws UnpackException {
        String packageDir = createParentDir(baseDir, extractDirectory);
        
        List<File> fileList = new ArrayList<File>();

        if (packageStream != null) {

            try {
                fileList = unpackFilesFromStream(packageStream, packageDir, fileName);
            } catch (Exception e) {
                final String message = "Error unpacking package file " + fileName + " was either empty, or a not " +
                        "a known package file type";
                UnpackException ue = new UnpackException(message, e);
                ue.setError(message);
                ue.setFilename(fileName);
                throw ue;
            }
        } else {
            final String message = "Error unpacking archive file " + fileName + " was null";
            UnpackException ue = new UnpackException(message);
            ue.setError(message);
            ue.setFilename(fileName);
            throw ue;
        }

        try {
            packageStream.close();
        } catch (IOException e) {
            final String message = "Error unpacking package file " + fileName + " was either empty, or a not " +
                    "a a known package file type";
            UnpackException ue = new UnpackException(message, e);
            ue.setError(message);
            ue.setFilename(fileName);
            throw ue;
        }

        return fileList;      
    }
    
    protected String createParentDir(String baseDir, String dir) {        
        String packageDir = dir;
        if (dir != null && !dir.isEmpty()) {
            if (!dir.endsWith("/") && !dir.endsWith("\\")) {
                dir += '/';
            }
            
            if (baseDir != null && !baseDir.isEmpty()) {
                packageDir = dir + "/" + baseDir;
            }
            
            File tempDir = new File(dir);
            tempDir.mkdirs();
        } else {
            if (baseDir != null && !baseDir.isEmpty()) {
                packageDir = "./" + baseDir;
            } else {
                packageDir = "./";
            }
        }
        
        File extractDir = new File(packageDir);
        extractDir.mkdirs();
        
        return packageDir;
    }
    
    /**
     * Extracts a list of files from the archive file
     * @param archive
     * @return
     * @throws Exception
     */
    protected abstract List<File> unpackFilesFromArchive(File archive, String packageDir) throws Exception;
    
    protected abstract List<File> unpackFilesFromStream(InputStream packageStream, String packageDir, String fileName) throws Exception;

    protected abstract String truncatePackageExtension(String fileName);
    
    protected List<File> saveExtractedFile(File extractedFile, InputStream extractedContent) throws IOException {
        List<File> files = new ArrayList<File>();
        if (extractedFile.getParent() != null && !extractedFile.getParent().isEmpty()) {
            //Create a temp directory of the parent for copying files
            File tempDir = new File(extractedFile.getParent());
            if (!tempDir.exists()) {
                createParentDirectory(tempDir, files);
            }          
        }

        FileOutputStream fileOut = new FileOutputStream(extractedFile);

        IOUtils.copy(extractedContent, fileOut);
        fileOut.close();

        files.add(extractedFile);
        
        return files;
    }
    
    private void createParentDirectory(File dir, List<File> files) {
        if (!dir.getParentFile().exists()) {
            createParentDirectory(dir.getParentFile(), files);
        } 
        
        dir.mkdirs();
        files.add(dir);
        
    }
    
    @Override
    public void cleanUpExtractedPackage(File dir) {
        if (dir != null && dir.exists()) {
            for (File file : dir.listFiles() ) {
                if (file.isDirectory()) {
                    cleanUpExtractedPackage(file);
                } else {
                    file.delete();
                }
            }
           
            dir.delete();
        }
    }
    
    @Override
    public String getExtractDirectory() {
        return extractDirectory;
    }
}