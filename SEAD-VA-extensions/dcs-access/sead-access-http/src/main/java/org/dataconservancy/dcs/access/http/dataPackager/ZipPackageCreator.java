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

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.io.IOUtils;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.dcs.query.dcpsolr.SeadConfig;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.*;
import org.seadva.archive.ArchiveEnum;
import org.seadva.archive.impl.cloud.Sftp;
import org.seadva.bagit.event.api.Event;
import org.seadva.bagit.impl.ConfigBootstrap;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadFile;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;

/**
 * Class that creates zipped packages from data from any archive store in SEAD VA
 */
public class ZipPackageCreator extends PackageCreatorBase
                                implements PackageCreator {

    DcsModelBuilder builder = new SeadXstreamStaxModelBuilder();

    public SeadConfig getConfig() {
        return config;
    }

    public void setConfig(SeadConfig config) {
        this.config = config;
    }

    private SeadConfig config;

    @Override
    public List<String> getPackageLinks(ResearchObject dcp, String prefix) {//get ancestory
        List<String> splitSips = splitSip(dcp);

        List<String> packageLinks = new ArrayList<String>();

        for(String splitSip:splitSips){
            packageLinks.add(
                    prefix+"package/"+ splitSip.substring(splitSip.lastIndexOf("/")+1)+".zip");
            //to split package into multiple ones
        }
        return packageLinks;
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

    /**
     *
     * @param link
     * @return
     */

    @Override
    public void getPackage(String link, OutputStream stream) throws FileNotFoundException {
        zipOutputStream = new ZipOutputStream(stream);
        zipOutputStream.setLevel(Deflater.NO_COMPRESSION);

        String sipPath = cachePath + link.substring(link.lastIndexOf("/") + 1).replace(".zip", "");
        System.out.println("SIP Path: "+sipPath);

        if (!new File(sipPath).exists())
            throw new FileNotFoundException("Sorry, there seems to be an error. Package does not exist.");

        ResearchObject dcp = null;
        try {
            dcp = (ResearchObject) builder.buildSip(new FileInputStream(new File(sipPath)));
            System.out.println("DCP: "+dcp.toString());
        } catch (InvalidXmlException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        allEntities = new HashMap<String, DcsEntity>();

        String rootId = "";
        String collectionTitle = "";

        for (DcsEntity entity : dcp.getDeliverableUnits()) {
            if (((DcsDeliverableUnit) entity).getParents().isEmpty())
                rootId = entity.getId();
            allEntities.put(entity.getId(), entity);
        }

        /************* This part of the code is used to get the tar file from SDA *****************************/
        DcsEntity tmp = allEntities.get(rootId);
        if (tmp instanceof DcsDeliverableUnit) {
            collectionTitle = ((DcsDeliverableUnit) tmp).getTitle();
        }

        String storageFormat = null;
        Collection<DcsDeliverableUnit> dus = dcp.getDeliverableUnits();

        for (DcsDeliverableUnit d : dus) {
            Collection<DcsResourceIdentifier> alternateIds = null;
            if (d.getParents() == null || d.getParents().isEmpty()) {
                alternateIds = d.getAlternateIds();
                if (alternateIds != null) {
                    DcsResourceIdentifier id = null;
                    Iterator<DcsResourceIdentifier> idIt = alternateIds.iterator();
                    while (idIt.hasNext()) {
                        id = idIt.next();
                        if (id.getTypeId().equalsIgnoreCase("storage_format")) {
                            storageFormat = id.getIdValue();
                            System.out.println("Alternate Object ID for storage_format: " + storageFormat);
                            break;
                        }
                    }

                }
            }
        }
        if(storageFormat.equalsIgnoreCase("tar")){
            getTarFromSDA("", collectionTitle);
            try {
                zipOutputStream.putNextEntry(new ZipEntry(collectionTitle + ".tar"));
                IOUtils.copy(new FileInputStream(collectionTitle + ".tar"), zipOutputStream);
                zipOutputStream.closeEntry();

                //Close the whole Zip stream
                zipOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }catch(Exception e){
                System.out.println("Something went wrong...");
            }
            return;
        }
        /*************************************************************************************************/


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
            //Add ORE file inside the folder
            String id =  rootId.split("/")[rootId.split("/").length-1];
            String oreFilePath = oreConversion(sipPath,id);
            String[] fileName = sipPath.split("/");
            zipOutputStream.putNextEntry(new ZipEntry(fileName[fileName.length-1]+"_oaiore.xml"));
            IOUtils.copy(new FileInputStream(oreFilePath), zipOutputStream);
            zipOutputStream.closeEntry();

            zipOutputStream.putNextEntry(new ZipEntry("bagit.txt"));
            zipOutputStream.closeEntry();

            zipOutputStream.putNextEntry(new ZipEntry("manifest.txt"));
            zipOutputStream.closeEntry();

            //Close the whole Zip stream
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
                path += "data";//((DcsDeliverableUnit) entity).getTitle();

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
            try
            {
                zipOutputStream.putNextEntry(new ZipEntry(path+"/" +((DcsFile) entity).getName()));
               /* int len;
                while ((len = fis.read(buf)) > 0)
                    zipOutputStream.write(buf, 0, len);*/
                        //fis =
                    downloadFileStream((SeadFile) entity, zipOutputStream);

                zipOutputStream.closeEntry();
            }
            catch (FileNotFoundException e){
                log.error(e.getMessage());
            } catch (IOException e) {
                log.error(e.getMessage());
            } catch (EntityTypeException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (EntityNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    void getTarFromSDA(String path, String title){
        if(path.length() > 0){
            path += "/" + title;
        }

        String tarFileName = title+".tar";
        FileOutputStream fileOutputStream = null;
        Sftp sftp = null;
        try {
            fileOutputStream = new FileOutputStream(new File(tarFileName));
            sftp = new Sftp(
                    config.getSdahost(),config.getSdauser(),config.getSdapwd(),config.getSdamount()
            );
            sftp.downloadFile(title, tarFileName, fileOutputStream);
            sftp.disConnectSession();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(SftpException e){
            e.printStackTrace();
        }catch (JSchException e) {
            e.printStackTrace();
        }
//        return;
    }


    boolean isFileCached(String fileId){
        if(new File(this.cachePath+fileId).exists())
            return  true;
        else
            return false;

    }
    private static final Logger log =
            LoggerFactory.getLogger(ZipPackageCreator.class);
    void downloadFileStream(SeadFile file, OutputStream destination) throws EntityNotFoundException, EntityTypeException {
            String filePath = null;
            if(file.getPrimaryLocation().getType()!=null&&file.getPrimaryLocation().getType().length()>0
                    &&file.getPrimaryLocation().getLocation()!=null&&file.getPrimaryLocation().getLocation().length()>0
                    &&file.getPrimaryLocation().getName()!=null&&file.getPrimaryLocation().getName().length()>0
                    ){
                if(
                        (file.getPrimaryLocation().getName().equalsIgnoreCase(ArchiveEnum.Archive.IU_SCHOLARWORKS.getArchive()))
                                ||
                                (file.getPrimaryLocation().getName().equalsIgnoreCase(ArchiveEnum.Archive.UIUC_IDEALS.getArchive())
                                )
                        ){
                    URLConnection connection = null;
                    try {
                        String location = file.getPrimaryLocation().getLocation();
                        location = location.replace("http://maple.dlib.indiana.edu:8245/", "https://scholarworks.iu.edu/");
                        connection = new URL(location).openConnection();
                        connection.setDoOutput(true);
                        final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                            @Override
                            public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
                            }
                            @Override
                            public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
                            }
                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }
                        } };
                        if(connection.getURL().getProtocol().equalsIgnoreCase("https")){
                            final SSLContext sslContext = SSLContext.getInstance( "SSL" );
                            sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
                            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                            ((HttpsURLConnection) connection ).setSSLSocketFactory( sslSocketFactory );
                        }
                        IOUtils.copy(connection.getInputStream(), destination);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (KeyManagementException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    return;
                }
                else if( file.getPrimaryLocation().getType().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getType().getText())
                        &&file.getPrimaryLocation().getName().equalsIgnoreCase(
                        ArchiveEnum.Archive.SDA.getArchive())
                        ) {
                    filePath = file.getPrimaryLocation().getLocation();

                    String[] pathArr = filePath.split("/");

                    try {
                        Sftp sftp = new Sftp(
                                config.getSdahost(),config.getSdauser(),config.getSdapwd(),config.getSdamount()
                        );
                        sftp.downloadFile(filePath.substring(0,filePath.lastIndexOf('/')), pathArr[pathArr.length-1], destination);
                        sftp.disConnectSession();
                    } catch (JSchException e) {
                        e.printStackTrace();
                    } catch (SftpException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                if(file.getSecondaryDataLocations()!=null&&file.getSecondaryDataLocations().size()>0){
                    for(SeadDataLocation dataLocation:file.getSecondaryDataLocations()){
                        if( dataLocation.getType().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getType().getText())
                                &&dataLocation.getName().equalsIgnoreCase(
                                ArchiveEnum.Archive.SDA.getArchive())
                                ) {
                            filePath = dataLocation.getLocation();

                            String[] pathArr = filePath.split("/");

                            try {
                                Sftp sftp = new Sftp(
                                        config.getSdahost(),config.getSdauser(),config.getSdapwd(),config.getSdamount()
                                );
                                sftp.downloadFile(filePath.substring(0,filePath.lastIndexOf('/')), pathArr[pathArr.length-1], destination);
                                sftp.disConnectSession();
                            } catch (JSchException e) {
                                e.printStackTrace();
                            } catch (SftpException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            return;
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
        final String fileName = rootId.substring(rootId.lastIndexOf("/")+1);
        File directory = new File(cachePath);
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File directory, String givenFileName) {
                return givenFileName.startsWith(fileName);
            }
        };

        String[] cachedFiles = directory.list(filter);
        if(cachedFiles.length>0)
            return Arrays.asList(cachedFiles);

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
