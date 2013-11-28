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

package org.dataconservancy.dcs.access.http.dataPackager;

import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.*;
import org.seadva.archive.ArchiveEnum;
import org.seadva.archive.SeadArchiveStore;
import org.seadva.model.SeadFile;
import org.seadva.model.builder.api.SeadModelBuilder;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class that creates zipped packages from data from any archive store in SEAD VA
 */
public class ZipPackageCreator extends PackageCreatorBase
                                implements PackageCreator {

    DcsModelBuilder builder = new SeadXstreamStaxModelBuilder();

    @Override
    public List<String> getPackageLinks(ResearchObject dcp) {//get ancestory
        List<String> splitSips = splitSip(dcp);

        List<String> packageLinks = new ArrayList<String>();

        for(String splitSip:splitSips){
            packageLinks.add(
                    "package/"+ splitSip.substring(splitSip.lastIndexOf("/")+1)+".zip");
            //to split package into multiple ones
        }
        return packageLinks;
    }

    /**
     *
     * @param link
     * @return
     */

    @Override
    public void getPackage(String link, OutputStream stream) throws FileNotFoundException {
        zipOutputStream = new ZipOutputStream(stream);
        zipOutputStream.setLevel(Deflater.NO_COMPRESSION);

        String sipPath = cachePath+link.substring(link.lastIndexOf("/")+1).replace(".zip","");

        if(!new File(sipPath).exists())
            throw new FileNotFoundException("Sorry, there seems to be an error. Package does not exist.");

        ResearchObject dcp = null;
        try {
            dcp = (ResearchObject)builder.buildSip(new FileInputStream(new File(sipPath)));
        } catch (InvalidXmlException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        allEntities = new HashMap<String, DcsEntity>();

        String rootId = "";
        for(DcsEntity entity:dcp.getDeliverableUnits())
        {
            if(((DcsDeliverableUnit)entity).getParents().isEmpty())
                rootId = entity.getId();
            allEntities.put(entity.getId(),entity);
        }

        for(DcsEntity entity:dcp.getFiles())
            allEntities.put(entity.getId(),entity);

        parentChildMap = new HashMap<String, List<String>>();
        for(DcsManifestation manifestation : dcp.getManifestations()){
            List<String> children;
            if( parentChildMap.containsKey(manifestation.getDeliverableUnit()))
                children = parentChildMap.get(manifestation.getDeliverableUnit());
            else
                children = new ArrayList<String>();
            for(DcsManifestationFile file:manifestation.getManifestationFiles())
                children.add(file.getRef().getRef());
            parentChildMap.put(manifestation.getDeliverableUnit(), children);
        }

        for(DcsDeliverableUnit du:dcp.getDeliverableUnits()) {
            for(DcsDeliverableUnitRef parent: du.getParents()){
                List<String> children;
                if( parentChildMap.containsKey(parent.getRef()))
                    children = parentChildMap.get(parent.getRef());
                else
                    children = new ArrayList<String>();

                children.add(du.getId());
                parentChildMap.put(parent.getRef(), children);
            }
        }


        createZip("", rootId);
        try {
            zipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return;
    }


    Map<String, List<String>> parentChildMap;
    Map<String, DcsEntity> allEntities;
    ZipOutputStream zipOutputStream;

    void createZip(String path, String rootId){

        DcsEntity entity = allEntities.get(rootId);


        if(entity instanceof DcsDeliverableUnit){
            if(path.length()>0)
                path += "/" + ((DcsDeliverableUnit) entity).getTitle();
            else
                path += ((DcsDeliverableUnit) entity).getTitle();

            if(parentChildMap.containsKey(rootId)){
                for(String child:parentChildMap.get(rootId))
                    createZip(path, child);
            }
        }
        else if(entity instanceof DcsFile) {
            InputStream fis;
            byte[] buf = new byte[1024];
            if(isFileCached(rootId))
                try {
                    fis = new FileInputStream(this.cachePath+rootId);
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException("File not found" + e.getMessage());
                }
            else
                try {
                    fis = downloadFileStream((SeadFile) entity);
                } catch (EntityNotFoundException e) {
                    throw new IllegalArgumentException("Entity not found" + e.getMessage());
                } catch (EntityTypeException e) {
                    throw new IllegalArgumentException("Entity type not found" + e.getMessage());
                }

            try
            {
                zipOutputStream.putNextEntry(new ZipEntry(path+"/" +((DcsFile) entity).getName()));
                int len;
                while ((len = fis.read(buf)) > 0)
                    zipOutputStream.write(buf, 0, len);
                zipOutputStream.closeEntry();
            }
            catch (FileNotFoundException e){
                log.error(e.getMessage());
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }



    boolean isFileCached(String fileId){
        if(new File(this.cachePath+fileId).exists())
            return  true;
        else
            return false;

    }
    private static final Logger log =
            LoggerFactory.getLogger(ZipPackageCreator.class);
    InputStream downloadFileStream(SeadFile file) throws EntityNotFoundException, EntityTypeException {
        ArchiveEnum.Archive archiveEnum = ArchiveEnum.Archive.fromString(file.getPrimaryLocation().getName());
        log.debug("archive="+archiveEnum.toString());
        SeadArchiveStore store = archiveStores.get(archiveEnum);
        return store.getContent(file.getId());
    }

    long twoGB = 2*1024*1024*1024L;
    List<String> duIds = new ArrayList<String>();

    List<String> splitSip(ResearchObject dcp){

        List<String> splitSips = new ArrayList<String>();


        Map<String, DcsFile> fileHashMap = new HashMap<String, DcsFile>();
        Map<String, DcsDeliverableUnit> duHashMap = new HashMap<String, DcsDeliverableUnit>();

        for(DcsFile file: dcp.getFiles())
            fileHashMap.put(file.getId(),file);

        String rootId = "";
        for(DcsDeliverableUnit du: dcp.getDeliverableUnits())
        {
            duHashMap.put(du.getId(),du);
            if(du.getParents().isEmpty())
                rootId = du.getId();
        }
        String fileName = rootId.substring(rootId.lastIndexOf("/")+1);

        int i = 0;
        long totalSize = 0;

        ResearchObject newSip = new ResearchObject();

        List<DcsManifestation> tempManifestations = new ArrayList<DcsManifestation>();

        DcsManifestation tempManifestation = null;
        for(DcsManifestation manifestation: dcp.getManifestations()){
            for(DcsManifestationFile manifestationFile:manifestation.getManifestationFiles())
                if(fileHashMap.containsKey(manifestationFile.getRef().getRef()))
                {
                    DcsFile file = fileHashMap.get(manifestationFile.getRef().getRef());
                    if(totalSize + file.getSizeBytes()>twoGB) {
                        try {
                            duIds = new ArrayList<String>();
                            if(tempManifestation!=null)   //atleast one manifestation will be present. If it goes through a second loop, then 2 or more manifestations will be present.
                                tempManifestations.add(tempManifestation);

                            for(DcsManifestation manifestation1:tempManifestations){
                                newSip.addManifestation(manifestation1);
                                if(!duIds.contains(manifestation1.getDeliverableUnit()))
                                {
                                    duIds.add(manifestation1.getDeliverableUnit());
                                    newSip = addDUs(newSip, duHashMap, manifestation1.getDeliverableUnit());
                                }
                            }
                            String time = String.valueOf(System.currentTimeMillis());
                            writeSip(newSip, cachePath+fileName+"_"+i+"_"+time);
                            splitSips.add(cachePath + fileName + "_" + i + "_" + time);
                            newSip = new ResearchObject();
                            tempManifestation = null;
                            tempManifestations = new ArrayList<DcsManifestation>();//empty manifestations so that a new one can be added next time
                            totalSize = 0;
                            i++;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    //new file gets added to the existing or newly created SIP from the previous condition
                    if(tempManifestation==null)
                    {
                        tempManifestation = new DcsManifestation();
                        tempManifestation.setDeliverableUnit(manifestation.getDeliverableUnit());
                        tempManifestation.setId(manifestation.getId());
                    }

                    DcsManifestationFile manifestationFile1 = new DcsManifestationFile();
                    DcsFileRef ref = new DcsFileRef();
                    ref.setRef(file.getId());
                    manifestationFile1.setRef(ref);
                    tempManifestation.addManifestationFile(manifestationFile1);

                    totalSize+= file.getSizeBytes();
                    newSip.addFile(file);

                }
            if(tempManifestation!=null){
                tempManifestations.add(tempManifestation);
                tempManifestation = null;
            }
        }

        if(!newSip.getDeliverableUnits().isEmpty()||
                !newSip.getFiles().isEmpty()||
                !newSip.getManifestations().isEmpty())
        {
            duIds = new ArrayList<String>();
            if(tempManifestation!=null)   //atleast one manifestation will be present. If it goes through a second loop, then 2 or more manifestations will be present.
                tempManifestations.add(tempManifestation);

            for(DcsManifestation manifestation1:tempManifestations){
                newSip.addManifestation(manifestation1);
                if(!duIds.contains(manifestation1.getDeliverableUnit()))
                {
                    duIds.add(manifestation1.getDeliverableUnit());
                    newSip = addDUs(newSip, duHashMap, manifestation1.getDeliverableUnit());
                }
            }
            String time = String.valueOf(System.currentTimeMillis());
            try {
                writeSip(newSip, cachePath+fileName+"_"+i+"_"+time);
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            splitSips.add(cachePath+fileName+"_"+i+"_"+time);
        }
        return splitSips;
    }

    private void writeSip(ResearchObject newSip,String cachedSipPath) throws FileNotFoundException {
        DcsModelBuilder modelBuilder = new SeadXstreamStaxModelBuilder();
        OutputStream outputStream = new FileOutputStream(cachedSipPath);
        modelBuilder.buildSip(newSip, outputStream);
    }

    private ResearchObject addDUs(ResearchObject newSip, Map<String, DcsDeliverableUnit> duHashMap, String duId){

        DcsDeliverableUnit du = duHashMap.get(duId);
        newSip.addDeliverableUnit(du);//change to SeadDeliverableUnit

        if(du.getParents()==null||du.getParents().size()==0)
            return newSip;

        newSip = addDUs(newSip, duHashMap, du.getParents().iterator().next().getRef());
        return newSip;
    }

}
