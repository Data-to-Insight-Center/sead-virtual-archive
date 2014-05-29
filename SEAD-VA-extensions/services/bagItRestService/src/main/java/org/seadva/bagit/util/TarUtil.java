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

package org.seadva.bagit.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

public class TarUtil {

    public static void unTarFile(String inputTarPath, String destinationDirectory)throws IOException{
        try{
            File sourceTarFile = new File(inputTarPath);
            File unTarDestinationDirectory = new File(destinationDirectory);
            if(!unTarDestinationDirectory.exists()){
                unTarDestinationDirectory.mkdir();
            }
            InputStream is = new FileInputStream(sourceTarFile);
            TarArchiveInputStream tarInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
            TarArchiveEntry entry = null;
            while((entry = (TarArchiveEntry)tarInputStream.getNextEntry())!=null){
                File outputFile = new File(unTarDestinationDirectory,entry.getName());
                if (entry.isDirectory()){
                    if(!outputFile.exists()){
                        if(!outputFile.mkdirs()){
                            throw new IllegalStateException(String.format("Failed to create directory %s.", outputFile.getAbsolutePath()));
                        }
                    }
                }else{
                    if(outputFile.getParentFile() != null && !outputFile.getParentFile().exists()){
                        outputFile.getParentFile().mkdirs();
                    }
                    OutputStream outputFileStream = new FileOutputStream(outputFile);
                    IOUtils.copy(tarInputStream,outputFileStream);
                    outputFileStream.close();
                }
            }
            tarInputStream.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    static List<String> filesListInDir;
    public static void tarDirectory(File dir, String tarFileName){
        System.out.println("Tar File Dir:"+dir.toString());
        try{
            filesListInDir = new ArrayList<String>();
            getFilesList(dir);
            FileOutputStream fos = new FileOutputStream(tarFileName);
            ArchiveOutputStream myTar = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR, fos);
            for(String filePath : filesListInDir){
                File inputFile = new File(filePath);
                TarArchiveEntry te = new TarArchiveEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
                te.setSize(inputFile.length());
                myTar.putArchiveEntry(te);
                IOUtils.copy(new FileInputStream(inputFile), myTar);
                myTar.closeArchiveEntry();
            }
            fos.close();
        }catch(IOException e){
            e.printStackTrace();
        }catch (Exception e){
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
