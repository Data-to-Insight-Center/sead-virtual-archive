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
import org.apache.commons.io.IOUtils;
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
import org.seadva.model.builder.api.SeadModelBuilder;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

import java.io.*;
import java.util.*;

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

    public DcsModelBuilder getModelBuilder(){
        return fmodelBuilder;
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

    @Required
    public void setIsTar(Boolean bValue){
        isTar = bValue;
    }


    public SdaArchiveStore() throws JSchException {
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
                sftp.downloadFile(location.substring(0, location.lastIndexOf("/"))+"/", location.split("/")[location.split("/").length - 1],System.getProperty("java.io.tmpdir"));
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

        if(isTar){
            Collection<DcsDeliverableUnit> dus = pkg.getDeliverableUnits();
            for (DcsDeliverableUnit d : dus) {

                if (d.getParents() == null || d.getParents().isEmpty()) {
                    SeadDataLocation dataLocation = ((SeadDeliverableUnit) d).getPrimaryLocation();
                    String dirPath = dataLocation.getLocation();
                    System.out.println("dirPath in SDAArchive: "+dirPath);
                    String fileName = dirPath.substring(dirPath.lastIndexOf('/')+1,dirPath.length());
                    System.out.println("fileName in SDAArchive: "+fileName);
                    sftp.uploadFile(dirPath, fileName, false);
                    SeadDataLocation newdataLocation = new SeadDataLocation();
                    newdataLocation.setType(ArchiveEnum.Archive.SDA.getType().getText());
                    newdataLocation.setLocation(fileName);
                    newdataLocation.setName(ArchiveEnum.Archive.SDA.getArchive());
                    ((SeadDeliverableUnit) d).setPrimaryLocation(newdataLocation);
                    break;
                }
            }
            pkg.setDeliverableUnits(dus);
            return pkg;
        }

        populateRelations(pkg);
        Collection<DcsDeliverableUnit> dus = pkg.getDeliverableUnits();
        Collection<DcsFile> files = pkg.getFiles();

        String sipDirectory =null;
        for(DcsDeliverableUnit rootDu:rootDUs){
            if(((SeadDeliverableUnit)rootDu).getPrimaryLocation().getLocation()==null){
                sipDirectory = rootDu.getTitle();
                sftp.createDirectory(rootDu.getTitle());
            }
            else{
                sipDirectory = ((SeadDeliverableUnit)rootDu).getPrimaryLocation().getLocation();
            }
            //archive metadata files
            for(DcsMetadataRef metadataRef:rootDu.getMetadataRef()){
                for(DcsFile file:files){
                    if(file.getId().equalsIgnoreCase(metadataRef.getRef())){
                        String fileName = file.getName();
                        sftp.uploadFile(file.getSource().replace("file://", ""), sipDirectory + "/" + fileName, useMount);
                        SeadDataLocation dataLocation = new SeadDataLocation();
                        dataLocation.setType(ArchiveEnum.Archive.SDA.getType().getText());
                        dataLocation.setLocation(
                                sipDirectory + "/" + fileName);
                        dataLocation.setName(ArchiveEnum.Archive.SDA.getArchive());
                        ((SeadFile)file).setPrimaryLocation(dataLocation);
                    }
                }

            }

            String id = rootDu.getId();
            if(leftOverParents.contains(id))
                leftOverParents.remove(id);

            for(DcsDeliverableUnit du:dus){
                if(du.getId().equals(rootDu.getId())){
                    SeadDataLocation dataLocation = new SeadDataLocation();
                    dataLocation.setName(ArchiveEnum.Archive.SDA.getArchive());
                    dataLocation.setType(ArchiveEnum.Archive.SDA.getType().getText());
                    dataLocation.setLocation(rootDu.getTitle());
                    ((SeadDeliverableUnit)du).setPrimaryLocation(dataLocation);
                    break;
                }
            }
            uploadtToSDA(sftp, rootDu.getId(), rootDu.getTitle(), dus, pkg.getManifestations(), files);
        }

        //Get missing parents from Solr
        for(String otherParent: leftOverParents){
            DcsEntity entity = null;
            try {
                entity = solrService.lookupEntity(otherParent);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SolrServerException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if (entity != null) {
                if(entity instanceof SeadDeliverableUnit) {
                    if(((SeadDeliverableUnit) entity).getSecondaryDataLocations()!=null
                            &&((SeadDeliverableUnit) entity).getSecondaryDataLocations().size()>0) {
                            for(SeadDataLocation location:((SeadDeliverableUnit) entity).getSecondaryDataLocations()){
                                if(location.getName().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getArchive())
                                        &&location.getType().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getType().getText())){
                                    sipDirectory = location.getLocation().split("/")[0];
                                    uploadtToSDA(sftp, otherParent, location.getLocation(),dus, pkg.getManifestations(), files);
                                    break;
                                }
                            }
                    }
                    else
                    {
                        if(((SeadDeliverableUnit) entity).getPrimaryLocation()!=null)
                        {
                            if(((SeadDeliverableUnit) entity).getPrimaryLocation().getName().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getArchive())
                                    &&((SeadDeliverableUnit) entity).getPrimaryLocation().getType().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getType().getText()))
                                {
                                    sipDirectory = ((SeadDeliverableUnit) entity).getPrimaryLocation().getLocation().split("/")[0];
                                    uploadtToSDA(sftp, otherParent, ((SeadDeliverableUnit) entity).getPrimaryLocation().getLocation(),dus, pkg.getManifestations(), files);
                                }

                        }
                    }
                }
            }

        }
        pkg.setDeliverableUnits(dus);

        pkg.setFiles(files);

        //upload the sip.xml to the top-most possible directory
        String sipSource = sipArchival(pkg);
        sftp.uploadFile(sipSource, sipDirectory + "/"+UUID.randomUUID().toString()+"_sip.xml", useMount);

        sftp.disConnectSession();
        return pkg;
    }
    private void uploadtToSDA(Sftp sftp, String id, String directoryName,
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
                    uploadtToSDA(sftp, child.getId(), tempDirectoryName,dus, manifestations,files);


                }
                else if(child instanceof DcsManifestation){
                    String[] manId = child.getId().split("/");
                    String tempDirectoryName = directoryName + "/man_" + manId[manId.length-1];
                    sftp.createDirectory(tempDirectoryName);

                    uploadtToSDA(sftp, child.getId(), tempDirectoryName, dus, manifestations,files);

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


    public String sipArchival(ResearchObject pkg){
        //SIP Archival
        OutputStream os;
        String sipFileName = "sip";
        File sipFile = new File(sipFileName);

        try {
            if(!sipFile.exists())
                sipFile.createNewFile();
            os = new FileOutputStream(sipFileName);
            DcsModelBuilder modelBuilder = new SeadXstreamStaxModelBuilder();
            modelBuilder.buildSip(pkg, os);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sipFile.getAbsolutePath();
    }




    Map<String,  List<DcsEntity>> duChildren;
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
            Iterator<DcsManifestationFile> mFiles = man.getManifestationFiles().iterator();
            while(mFiles.hasNext())
            {
                DcsManifestationFile mFile = mFiles.next();
                for(DcsFile file:pkg.getFiles())
                {
                    if(file.getId().equals(mFile.getRef().getRef()))
                    {
                        if(duChildren.containsKey(man.getId()))
                        {
                            children = duChildren.get(man.getId());
                            duChildren.remove(man.getId());
                        }
                        else
                            children = new ArrayList<DcsEntity>();

                        children.add(file);
                        duChildren.put(man.getId(),children);
                    }
                }
            }
        }

        Iterator iterator = duChildren.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,List<DcsEntity>> child = (Map.Entry)iterator.next();
            for(DcsEntity entity:child.getValue())
                if(leftOverParents.contains(entity.getId()))
                    leftOverParents.remove(entity.getId());
        }
    }

}


