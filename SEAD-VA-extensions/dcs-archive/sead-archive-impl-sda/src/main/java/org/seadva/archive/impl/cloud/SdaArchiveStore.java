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

package org.seadva.archive.impl.cloud;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.solr.client.solrj.SolrServerException;
import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.*;
import org.seadva.archive.ArchiveEnum;
import org.seadva.archive.SeadArchiveStore;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadEvent;
import org.seadva.model.SeadFile;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;

import org.seadva.bagit.impl.ConfigBootstrap;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Event;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * SEAD Virtual Archive SDA repository client
 */
public class SdaArchiveStore implements SeadArchiveStore {

    private String username;

    private String password;

    private String hostname;

    private String mountPath;

    private SolrService solrService;

    private DcsModelBuilder fmodelBuilder;

    private boolean isTar;

    @Required
    public void setModelBuilder(DcsModelBuilder mb) {
        fmodelBuilder = mb;
    }

    @Required
    public void setUsername(String un) {
        username = un;
    }

    @Required
    public void setPassword(String pwd) {
        password = pwd;
    }

    @Required
    public void setHostname(String hn) {
        hostname = hn;
    }

    @Required
    public void setSolrService(SolrService sService) {
        solrService = sService;
    }

    @Required
    public void setMountPath(String mPath) {
        mountPath = mPath;
    }

    public SdaArchiveStore(String hostname, String username, String password, String mountPath) throws JSchException {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.mountPath = mountPath;
    }

    @Override
    public Iterator<String> listEntities(EntityType type) {
        return null;
    }

    @Override
    public InputStream getContent(String entityId) throws EntityNotFoundException, EntityTypeException {
        Sftp sftp = null;
            try {
                sftp = new Sftp(this.hostname, this.username, this.password, this.mountPath);
            } catch (JSchException e) {
                e.printStackTrace();
            }
        DcsEntity entity ;

        try
        {
            entity = solrService.lookupEntity(entityId);

            if (entity instanceof SeadFile) {
                String location = ((SeadFile)entity).getPrimaryLocation().getLocation();
                try {
                    if (sftp != null) {
                        sftp.downloadFile(location.substring(0, location.lastIndexOf("/")) + "/", location.split("/")[location.split("/").length - 1], System.getProperty("java.io.tmpdir"));
                    }
                }catch(NullPointerException npe){
                    System.err.println("SFTP File download failed!");
                }
                return new FileInputStream(new File(System.getProperty("java.io.tmpdir")) +"/"+ location.split("/")[location.split("/").length - 1]);
            }
            else if (entity instanceof SeadDeliverableUnit) {
                throw new IllegalArgumentException("Cannot get content for entity type: "
                        + entity.getClass().getName());
            } else if (entity instanceof SeadEvent) {
                throw new IllegalArgumentException("Cannot get content for entity type: "
                        + entity.getClass().getName());
            } else if (entity instanceof DcsManifestation) {
                throw new IllegalArgumentException("Cannot get content for entity type: "
                        + entity.getClass().getName());
            } else {
                throw new IllegalArgumentException("Unhandled entity type: "
                        + entity.getClass().getName());
            }
        } catch (SftpException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public InputStream getPackage(String entityId) throws EntityNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InputStream getFullPackage(String entityId) throws EntityNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void putPackage(InputStream dcpStream) throws AIPFormatException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    //create exact replica of the directory structure
    boolean useMount;
    public ResearchObject putResearchPackage(InputStream dcpStream) throws AIPFormatException {
        Sftp sftp = null;
        String sipDirectory = null;
        try {
            sftp = new Sftp(this.hostname, this.username, this.password, this.mountPath);
        } catch (JSchException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        DcsModelBuilder modelBuilder = new SeadXstreamStaxModelBuilder();
        useMount = false;
        ResearchObject pkg;
        try {
            pkg = (ResearchObject)modelBuilder.buildSip(dcpStream);
        } catch (InvalidXmlException e) {
            throw new AIPFormatException(e.getMessage());
        }

        populateRelations(pkg);
        Collection<DcsDeliverableUnit> dus = pkg.getDeliverableUnits();
        Collection<DcsFile> files = pkg.getFiles();

        for(DcsDeliverableUnit du: dus){
            Collection<DcsResourceIdentifier> alternateIds = null;
            if(du.getParents().isEmpty()){
                alternateIds = du.getAlternateIds();
                System.out.println("Alternate IDs: "+alternateIds.toString());
                if(alternateIds != null) {
                    DcsResourceIdentifier id = null;
                    Iterator<DcsResourceIdentifier> idIt = alternateIds.iterator();

                    int alreadySet =0;
                    while(idIt.hasNext()){
                        id = idIt.next();

                        if(id.getTypeId().equalsIgnoreCase("storage_format")) {
                            alreadySet=1;
                        }
                    }
                    if(alreadySet==1)
                        break;
                }
                else{
                    alternateIds = new HashSet<DcsResourceIdentifier>();
                }
                try{
                    DcsResourceIdentifier storage_format = new DcsResourceIdentifier();
                    storage_format.setTypeId("storage_format");
                    if (isTar)
                        storage_format.setIdValue("tar");
                    else
                        storage_format.setIdValue("collection");
                    System.out.println("AlternateID: "+storage_format.toString());
                    alternateIds.add(storage_format);
                }catch(Exception e){
                    e.printStackTrace();
                }
                du.setAlternateIds(alternateIds);
            }
        }
        pkg.setDeliverableUnits(dus);
        String sipSource = sipArchival(pkg);

        if(isTar){
            System.out.println("###############################################################");
            Collection<DcsDeliverableUnit> t_dus = pkg.getDeliverableUnits();
            Collection<DcsFile> t_files = pkg.getFiles();
            Map duMap = new HashMap();
            Map fileMap = new HashMap();
            for(DcsDeliverableUnit du:t_dus){
                duMap.put(du.getId(),du);
            }

            for(DcsFile file:pkg.getFiles()){
                fileMap.put(file.getId(), file);
                System.out.println("File name: "+file.getName()+" - "+file.getSource());
            }

            for(DcsManifestation manifestation:pkg.getManifestations()){
                if(duMap.containsKey(manifestation.getDeliverableUnit())){
                    for(DcsManifestationFile manifestationFile : manifestation.getManifestationFiles()){
                        DcsFile file = (DcsFile)fileMap.get(manifestationFile.getRef().getRef());
                        System.out.println("Manifestation file: "+file.getName());
                    }
                }
            }

            String t_sipDirectory =null;
            String tarFilePath = null;
            String tarFileName = null;
            String collectionName = null;

            for(DcsDeliverableUnit rootDu:rootDUs) {
                if (((SeadDeliverableUnit) rootDu).getPrimaryLocation().getLocation() == null) {
                    System.out.println("Root Directory Name: " + rootDu.getTitle()+" - "+rootDu.getId());
                    t_sipDirectory = rootDu.getTitle();
                    try {
                        if (sftp != null) {
                            sftp.createDirectory(rootDu.getTitle());
                        }
                    }catch(NullPointerException npe){
                        System.err.println("SFTP Directory creation failed!");
                    }
                } else {
                    System.out.println("Root Directory Name: " + ((SeadDeliverableUnit) rootDu).getPrimaryLocation().getLocation()+" - "+rootDu.getId());
                    t_sipDirectory = ((SeadDeliverableUnit) rootDu).getPrimaryLocation().getLocation();
                }
                //archive metadata files
                for(DcsMetadataRef metadataRef:rootDu.getMetadataRef()){
                    for(DcsFile file:t_files){
                        if(file.getId().equalsIgnoreCase(metadataRef.getRef())){
                            String fileName = file.getName();
                            System.out.println("Archive metadata filename: " + fileName);
                            System.out.println(file.getSource());
                            System.out.println("DataLocation: "+t_sipDirectory+"/"+fileName);
                        }
                    }

                }
                String tmpDirectory = "/tmp/tarFileLocation/"+rootDu.getTitle()+"/"+rootDu.getTitle();
                System.out.println("Collecting files to create a tar...");
                collectTarFiles(rootDu.getId(), tmpDirectory);
                System.out.println("Creating the tar ...");
                collectionName = rootDu.getTitle();
                tarFileName = rootDu.getTitle()+".tar";
                tarFilePath = "/tmp/tarFileLocation/"+rootDu.getTitle()+"/"+tarFileName;
                long tarStartTime = System.nanoTime();
//                tarDirectory(new File("/tmp/tarFileLocation/"+rootDu.getTitle()), "/tmp/tarFileLocation/"+rootDu.getTitle()+"/"+rootDu.getTitle()+".tar");
                createTar(new File("/tmp/tarFileLocation/" + rootDu.getTitle()), "/tmp/tarFileLocation/" + rootDu.getTitle() + "/" + rootDu.getTitle() + ".tar");
//                generateTar(new File("/tmp/tarFileLocation/"+rootDu.getTitle()), "/tmp/tarFileLocation/"+rootDu.getTitle()+"/"+rootDu.getTitle()+".tar");

                long tarEndTime = System.nanoTime();
                System.out.println("Time taken to tar file: "+(TimeUnit.SECONDS.convert((tarEndTime - tarStartTime), TimeUnit.NANOSECONDS))+" seconds");
                //
                SeadDataLocation dataLocation = new SeadDataLocation();
                dataLocation.setName(ArchiveEnum.Archive.SDA.getArchive());
                dataLocation.setType(ArchiveEnum.Archive.SDA.getType().getText());
                dataLocation.setLocation(t_sipDirectory+"/"+tarFileName);
                dataLocation.setName(ArchiveEnum.Archive.SDA.getArchive());
            }

            String oreFilePath = oreConversion(sipSource,collectionName);
            System.out.println("oreFilePath: "+oreFilePath);

            //Upload the tar file
            System.out.println("Uploading the tar file: "+tarFilePath+" - "+t_sipDirectory+" - "+tarFileName);
            try {
                if (sftp != null) {
                    long sftpStartTime = System.nanoTime();
                    sftp.uploadFile(tarFilePath, t_sipDirectory + "/" + tarFileName, useMount);
                    long sftpEndTime = System.nanoTime();
                    System.out.println("Time taken to transfer file: "+(TimeUnit.SECONDS.convert((sftpEndTime - sftpStartTime), TimeUnit.NANOSECONDS))+" seconds");
                }
            }catch(NullPointerException npe){
                System.err.println("SFTP Tar file upload failed!");
            }
            System.out.println("End of tar file upload");

//            //Upload the OAIORE file
//            System.out.println("Uploading the OAIORE file: "+oreFilePath+" - "+t_sipDirectory+" - "+collectionName+"_oaiore.xml");
//            try {
//                if (sftp != null) {
//                    sftp.uploadFile(oreFilePath, t_sipDirectory + "/" + collectionName + "_oaiore.xml", useMount);
//                }
//            }catch(NullPointerException npe){
//                System.err.println("SFTP OAI ORE file upload failed!");
//            }
            try {
                if (sftp != null) {
                    sftp.uploadFile(sipSource, t_sipDirectory + "/" + UUID.randomUUID().toString() + "_sip.xml", useMount);
                }
            }catch(NullPointerException npe){
                System.err.println("SFTP SIP file upload failed!");
            }
            System.out.println("End of SIP file upload");
            System.out.println("###############################################################");
        }else {
            for (DcsDeliverableUnit rootDu : rootDUs) {
                if (((SeadDeliverableUnit) rootDu).getPrimaryLocation().getLocation() == null) {
                    sipDirectory = rootDu.getTitle();
                    try {
                        if (sftp != null) {
                            sftp.createDirectory(rootDu.getTitle());
                        }
                    }catch(NullPointerException npe){
                        System.err.println("SFTP Create Directory failed!");
                    }

                } else {
                    sipDirectory = ((SeadDeliverableUnit) rootDu).getPrimaryLocation().getLocation();
                }
                //archive metadata files
                for (DcsMetadataRef metadataRef : rootDu.getMetadataRef()) {
                    for (DcsFile file : files) {
                        if (file.getId().equalsIgnoreCase(metadataRef.getRef())) {
                            String fileName = file.getName();
                            try{
                                if (sftp != null) {
                                    sftp.uploadFile(file.getSource().replace("file://", ""), sipDirectory + "/" + fileName, useMount);
                                }
                            }catch (NullPointerException npe){
                                System.err.println("SFTP Metadata file upload failed!");
                            }

                            SeadDataLocation dataLocation = new SeadDataLocation();
                            dataLocation.setType(ArchiveEnum.Archive.SDA.getType().getText());
                            dataLocation.setLocation(
                                    sipDirectory + "/" + fileName);
                            dataLocation.setName(ArchiveEnum.Archive.SDA.getArchive());
                            ((SeadFile) file).setPrimaryLocation(dataLocation);
                        }
                    }

                }

                String id = rootDu.getId();
                if (leftOverParents.contains(id))
                    leftOverParents.remove(id);

                for (DcsDeliverableUnit du : dus) {
                    if (du.getId().equals(rootDu.getId())) {
                        SeadDataLocation dataLocation = new SeadDataLocation();
                        dataLocation.setName(ArchiveEnum.Archive.SDA.getArchive());
                        dataLocation.setType(ArchiveEnum.Archive.SDA.getType().getText());
                        dataLocation.setLocation(rootDu.getTitle());
                        ((SeadDeliverableUnit) du).setPrimaryLocation(dataLocation);
                        break;
                    }
                }
                uploadToSDA(sftp, rootDu.getId(), rootDu.getTitle(), dus, pkg.getManifestations(), files);
            }

            //Get missing parents from Solr
            for (String otherParent : leftOverParents) {
                DcsEntity entity = null;
                try {
                    entity = solrService.lookupEntity(otherParent);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (SolrServerException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                if (entity != null) {
                    if (entity instanceof SeadDeliverableUnit) {
                        if (((SeadDeliverableUnit) entity).getSecondaryDataLocations() != null
                                && ((SeadDeliverableUnit) entity).getSecondaryDataLocations().size() > 0) {
                            for (SeadDataLocation location : ((SeadDeliverableUnit) entity).getSecondaryDataLocations()) {
                                if (location.getName().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getArchive())
                                        && location.getType().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getType().getText())) {
                                    sipDirectory = location.getLocation().split("/")[0];
                                    uploadToSDA(sftp, otherParent, location.getLocation(), dus, pkg.getManifestations(), files);
                                    break;
                                }
                            }
                        } else {
                            if (((SeadDeliverableUnit) entity).getPrimaryLocation() != null) {
                                if (((SeadDeliverableUnit) entity).getPrimaryLocation().getName().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getArchive())
                                        && ((SeadDeliverableUnit) entity).getPrimaryLocation().getType().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getType().getText())) {
                                    sipDirectory = ((SeadDeliverableUnit) entity).getPrimaryLocation().getLocation().split("/")[0];
                                    uploadToSDA(sftp, otherParent, ((SeadDeliverableUnit) entity).getPrimaryLocation().getLocation(), dus, pkg.getManifestations(), files);
                                }

                            }
                        }
                    }
                }

            }

            pkg.setDeliverableUnits(dus);
            pkg.setFiles(files);
            try {
                if (sftp != null) {
                    sftp.uploadFile(sipSource, sipDirectory + "/" + UUID.randomUUID().toString() + "_sip.xml", useMount);
                }
            }catch(NullPointerException npe){
                System.err.println("SFTP Upload failed!");
            }
        }
        if (sftp != null) {
            sftp.disConnectSession();
        }
        return pkg;
    }

    private void uploadToSDA(Sftp sftp, String id, String directoryName,
                             Collection<DcsDeliverableUnit> dus, Collection<DcsManifestation> manifestations, Collection<DcsFile> files){

        List<DcsEntity> children = duChildren.get(id);
        if(children!=null)
            for(DcsEntity child:children){
                if(child instanceof SeadDeliverableUnit){
                    String tempDirectoryName = directoryName+"/"+((SeadDeliverableUnit) child).getTitle();
                    System.out.println(tempDirectoryName);
                    sftp.createDirectory(tempDirectoryName);

                    for(DcsDeliverableUnit du:dus){
                        if(du.getId().equals(child.getId())){
                            SeadDataLocation dataLocation = new SeadDataLocation();
                            dataLocation.setType(ArchiveEnum.Archive.SDA.getType().getText());
                            dataLocation.setLocation(
                                    tempDirectoryName);
                            dataLocation.setName(ArchiveEnum.Archive.SDA.getArchive());

                            ((SeadDeliverableUnit)du).setPrimaryLocation(dataLocation);
                            break;
                        }
                    }
                    //create a directory
                    uploadToSDA(sftp, child.getId(), tempDirectoryName, dus, manifestations, files);


                }
                else if(child instanceof DcsManifestation){
                    String[] manId = child.getId().split("/");
                    String tempDirectoryName = directoryName + "/man_" + manId[manId.length-1];
                    sftp.createDirectory(tempDirectoryName);

                    uploadToSDA(sftp, child.getId(), tempDirectoryName, dus, manifestations, files);

                }
                else if(child instanceof SeadFile){
                    if(((SeadFile) child).getPrimaryLocation()!=null)
                    {
                        if(((SeadFile) child).getPrimaryLocation().getType()!=null)
                            if(((SeadFile) child).getPrimaryLocation().getType().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getType().getText()))
                                continue;
                    }
                    else if(!((SeadFile) child).getSecondaryDataLocations().isEmpty()){
                        int flag =0;
                        for(SeadDataLocation loc: ((SeadFile) child).getSecondaryDataLocations()){
                            if(loc.getType().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getType().getText()))
                            {
                                flag=1;
                                break;
                            }
                        }
                        if(flag==1)
                            continue;
                    }
                    String fileName = ((SeadFile) child).getName();
                    for(DcsFile file:files){
                        if(file.getId().equals(child.getId())){

                            SeadDataLocation dataLocation = new SeadDataLocation();
                            dataLocation.setType(ArchiveEnum.Archive.SDA.getType().getText());
                            dataLocation.setLocation(
                                    directoryName + "/" + fileName);
                            dataLocation.setName(ArchiveEnum.Archive.SDA.getArchive());
                            ((SeadFile)file).setPrimaryLocation(dataLocation);
                            break;
                        }
                    }
                    sftp.uploadFile(((SeadFile)child).getSource().replace("file://", ""), directoryName + "/" + fileName, useMount);
                }
            }

        duChildren.remove(id);

    }

    private void collectTarFiles(String id, String directoryName){

        // Create a directory if a directory for holding
        // the tar files does not exist
        String dirLocation = "/tmp/tarFileLocation";
        File collDir = new File(dirLocation);
        if(!collDir.exists())
            if(!collDir.mkdir())
                System.out.println("Directory Creation failed!");

        // Create a directory
        File rootDir = new File(directoryName);
        if(!rootDir.delete())
            System.out.println("Temporary Directory deletion failed!");
        if(!rootDir.exists())
            if(!rootDir.mkdirs()){
                System.out.println("Temporary Directory creation failed!");
            }
        List<DcsEntity> children = t_duChildren.get(id);
        if(children == null)
            System.out.println("Children is null");
        if(children!=null)
            for(DcsEntity child:children){
                if(child instanceof SeadDeliverableUnit){
                    String tempDirectoryName = directoryName+"/"+((SeadDeliverableUnit) child).getTitle();
                    File file = new File(tempDirectoryName);
                    if(!file.exists())
                        if(!file.mkdirs())
                            System.out.println("Directory Creation failed!");

                    //create a directory
                    collectTarFiles(child.getId(), tempDirectoryName);
                }
                else if(child instanceof DcsManifestation){
                    String[] manId = child.getId().split("/");
                    String tempDirectoryName =directoryName+"/man_" + manId[manId.length-1];
                    collectTarFiles(child.getId(), tempDirectoryName);

                }
                else if(child instanceof SeadFile){
                    if(((SeadFile) child).getPrimaryLocation()!=null)
                    {
                        if(((SeadFile) child).getPrimaryLocation().getType()!=null)
                            if(((SeadFile) child).getPrimaryLocation().getType().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getType().getText()))
                                continue;
                    }
                    else if(!((SeadFile) child).getSecondaryDataLocations().isEmpty()){
                        int flag =0;
                        for(SeadDataLocation loc: ((SeadFile) child).getSecondaryDataLocations()){
                            if(loc.getType().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getType().getText()))
                            {
                                flag=1;
                                break;
                            }
                        }
                        if(flag==1)
                            continue;
                    }
                    String fileName = ((SeadFile) child).getName();
                    String srcFileName =  ((SeadFile)child).getSource().replace("file://", "");
                    String destFilename = directoryName+"/"+fileName;
                    InputStream input = null;
                    OutputStream output = null;
                    try{
                        input = new FileInputStream(new File(srcFileName));
                        output = new FileOutputStream(new File(destFilename));
                        byte[] buf = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = input.read(buf)) > 0) {
                            output.write(buf, 0, bytesRead);
                        }
                    }catch (IOException e){
                        System.out.println("Oops Something went wrong during file copy!!");
                        e.printStackTrace();
                    }
                    finally{
                        try{
                            if(input!=null)
                                input.close();
                            if(output!=null)
                                output.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }catch(NullPointerException e){
                            e.printStackTrace();
                        }

                    }
                }
            }
        t_duChildren.remove(id);

    }

    public List<File> recurseDirectory(final File directory) {
        List<File> files = new ArrayList<File>();
        if (directory != null && directory.isDirectory()) {
            try{
                File[] filesList = directory.listFiles();
                if(filesList != null) {
                    for (File file : filesList) {

                        if (file.isDirectory()) {
                            files.addAll(recurseDirectory(file));
                        } else {
                            files.add(file);
                        }
                    }
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }

        return files;
    }

    public void createTar(final File dir, final String tarFileName){
        try {
            OutputStream tarOutput = new FileOutputStream(new File(tarFileName));
            ArchiveOutputStream tarArchive = new TarArchiveOutputStream(tarOutput);
            List<File> files = new ArrayList<File>();
            File[] filesList = dir.listFiles();
            if(filesList != null) {
                for (File file : filesList) {
                    files.addAll(recurseDirectory(file));
                }
            }
            for(File file:files){
//                tarArchiveEntry = new TarArchiveEntry(file, file.getPath());
                TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(
                        file.toString().substring(dir.getAbsolutePath().length() + 1, file.toString().length()));
                tarArchiveEntry.setSize(file.length());
                tarArchive.putArchiveEntry(tarArchiveEntry);
                FileInputStream fileInputStream = new FileInputStream(file);
                IOUtils.copy(fileInputStream, tarArchive);
                fileInputStream.close();
                tarArchive.closeArchiveEntry();
            }
            tarArchive.finish();
            tarOutput.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    public String oreConversion(String sipFilePath, String rootId) {

        File targetDir = new File(System.getProperty("java.io.tmpdir"));

        PackageDescriptor packageDescriptor = new PackageDescriptor(rootId, null, targetDir.getAbsolutePath());
        packageDescriptor.setSipPath(sipFilePath);
        try
        {
            new ConfigBootstrap().load();
            packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_SIP, packageDescriptor);
            packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_ORE, packageDescriptor);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        return packageDescriptor.getOreFilePath();
    }


    public String sipArchival(ResearchObject pkg){
        //SIP Archival
        OutputStream os;
        String sipFileName = "sip";
        File sipFile = new File(sipFileName);

        try {
            if(!sipFile.exists())
                if(!sipFile.createNewFile()){
                    System.out.println("SIP File creation failed!");
                }
            os = new FileOutputStream(sipFileName);
            DcsModelBuilder modelBuilder = new SeadXstreamStaxModelBuilder();
            modelBuilder.buildSip(pkg, os);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sipFile.getAbsolutePath();
    }




    Map<String,  List<DcsEntity>> duChildren;
    Map<String,  List<DcsEntity>> t_duChildren; // Used to create directories for tar file
    List<DcsDeliverableUnit> rootDUs = new ArrayList<DcsDeliverableUnit>();
    List<String> leftOverParents = new ArrayList<String>();

    private void populateRelations(ResearchObject pkg) {

        duChildren = new HashMap<String, List<DcsEntity>>();



        for(DcsDeliverableUnit du:pkg.getDeliverableUnits())
        {
            if(du.getParents().isEmpty())
            {
                rootDUs.add(du);

                continue;
            }
            String parentId = null;
            for(DcsDeliverableUnitRef duRef:du.getParents())
            {
                parentId = duRef.getRef();
                if(!leftOverParents.contains(parentId))
                    leftOverParents.add(parentId);
                break;
            }

            List<DcsEntity> children;
            if(duChildren.containsKey(parentId))
            {
                children = duChildren.get(parentId);
                duChildren.remove(parentId);
            }
            else
                children = new ArrayList<DcsEntity>();

            children.add(du);
            duChildren.put(parentId,children);

        }



        for(DcsManifestation man:pkg.getManifestations())
        {
            String parentId = man.getDeliverableUnit();
            if(!leftOverParents.contains(parentId))
                leftOverParents.add(parentId);

            List<DcsEntity> children;
            if(duChildren.containsKey(parentId))
            {
                children = duChildren.get(parentId);
                duChildren.remove(parentId);
            }
            else
                children = new ArrayList<DcsEntity>();

            children.add(man);
            duChildren.put(parentId,children);
            //create another folder
            for (DcsManifestationFile mFile : man.getManifestationFiles()) {
                for (DcsFile file : pkg.getFiles()) {
                    if (file.getId().equals(mFile.getRef().getRef())) {
                        if (duChildren.containsKey(man.getId())) {
                            children = duChildren.get(man.getId());
                            duChildren.remove(man.getId());
                        } else
                            children = new ArrayList<DcsEntity>();

                        children.add(file);
                        duChildren.put(man.getId(), children);
                    }
                }
            }
        }
        t_duChildren = duChildren;
        for (Object o : duChildren.entrySet()) {
            Map.Entry<String, List<DcsEntity>> child = (Map.Entry) o;
            for (DcsEntity entity : child.getValue())
                if (leftOverParents.contains(entity.getId()))
                    leftOverParents.remove(entity.getId());
        }

    }

}