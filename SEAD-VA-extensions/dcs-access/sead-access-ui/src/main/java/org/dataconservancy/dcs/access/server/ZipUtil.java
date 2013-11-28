/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.dcs.access.server;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kavchand
 * Date: 8/28/13
 * Time: 1:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ZipUtil {

    public static void unzip(String inputZipPath, String destinationDirectory) throws IOException {

        int BUFFER = 2048;
        List zipFiles = new ArrayList();
        File sourceZipFile = new File(inputZipPath);
        File unzipDestinationDirectory = new File(destinationDirectory);
        unzipDestinationDirectory.mkdir();

        ZipFile zipFile;
        // Open Zip file for reading
        zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);

        // Create an enumeration of the entries in the zip file
        Enumeration zipFileEntries = zipFile.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

            String currentEntry = entry.getName();

            File destFile = new File(unzipDestinationDirectory, currentEntry);
            destFile = new File(unzipDestinationDirectory, destFile.getName());

            if (currentEntry.endsWith(".zip")) {
                zipFiles.add(destFile.getAbsolutePath());
            }

            // grab file's parent directory structure
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            try {
                // extract file if not a directory
                if (!entry.isDirectory()) {
                    BufferedInputStream is =
                            new BufferedInputStream(zipFile.getInputStream(entry));
                    int currentByte;
                    // establish buffer for writing file
                    byte data[] = new byte[BUFFER];

                    // write the current file to disk
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest =
                            new BufferedOutputStream(fos, BUFFER);

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        zipFile.close();

        for (Iterator iter = zipFiles.iterator(); iter.hasNext();) {
            String zipName = (String)iter.next();
            unzip(
                    zipName,
                    destinationDirectory +
                            File.separatorChar +
                            zipName.substring(0, zipName.lastIndexOf(".zip"))
            );
        }
    }

    static List<String> filesListInDir;
    public static void zipDirectory(File dir, String zipDirName) {
        try {
            filesListInDir = new ArrayList<String>();
            getFilesList(dir);
             FileOutputStream fos = new FileOutputStream(zipDirName);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for(String filePath : filesListInDir){
                ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
                zos.putNextEntry(ze);

                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method populates all the files in a directory to a List
     * @param dir
     * @throws IOException
     */
    private static void getFilesList(File dir) throws IOException {
        File[] files = dir.listFiles();
        for(File file : files){
            if(file.isFile()) filesListInDir.add(file.getAbsolutePath());
            else getFilesList(file);
        }
    }
}
