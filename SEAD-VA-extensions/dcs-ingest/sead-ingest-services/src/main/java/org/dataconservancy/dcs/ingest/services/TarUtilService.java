package org.dataconservancy.dcs.ingest.services;

//import org.dataconservancy.dcs.ingest.services.IngestService;
//import org.dataconservancy.dcs.ingest.services.IngestServiceBase;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.codehaus.plexus.util.FileUtils;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.dcs.*;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;

public class TarUtilService extends IngestServiceBase implements IngestService {
    String dirPath;
    //String tarFileName = "dpntest.tar";
    String tarFilePath;
    Writer bagInfo;

    String tarFileLocation;

    @Required
    public void setTarFileLocation(String tarFileLocation)
    {
        this.tarFileLocation = tarFileLocation+"/";
        System.out.println("Tarfile location in setTarFileLocation is"+tarFileLocation);
    }

    public void execute(String sipRef) {

        System.out.println("SIP Ref in TarUtilService: "+sipRef);
        String tarFileName = "";
        long bagSize;
        ResearchObject sip = (ResearchObject)ingest.getSipStager().getSIP(sipRef);


        Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();
        Collection dusc = new ArrayList();

        Map duMap = new HashMap();
        Map fileMap = new HashMap();

        for (DcsDeliverableUnit du : sip.getDeliverableUnits()) {
            duMap.put(du.getId(), du);
        }
        for (DcsFile file : sip.getFiles()) {
            fileMap.put(file.getId(), file);
        }
        for (DcsManifestation manifestation : sip.getManifestations()) {
            if (duMap.containsKey(manifestation.getDeliverableUnit()))
            {
                DcsDeliverableUnit du = (DcsDeliverableUnit)duMap.get(manifestation.getDeliverableUnit());
                int totalSize = 0;
                int n = 0;
                for (DcsManifestationFile manifestationFile : manifestation.getManifestationFiles()) {
                    if (fileMap.containsKey(manifestationFile.getRef().getRef())) {
                        DcsFile file = (DcsFile)fileMap.get(manifestationFile.getRef().getRef());
                        System.out.println("SizeTarUtilService: "+file.getName());
                        System.out.println("SizeTarUtilService-Size:"+file.getSizeBytes());
                        totalSize = (int)(totalSize + file.getSizeBytes());
                        System.out.println("SizeTarUtilService-Size-Total:"+totalSize);
                        n++;
                    }
                }
                ((SeadDeliverableUnit)du).setFileNo(n);
                ((SeadDeliverableUnit)du).setSizeBytes(totalSize);
                duMap.put(du.getId(), du);
            }
        }

        long size = duMap.size();
        Iterator iterator = duMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            dusc.add(pair.getValue());
        }
        if (size > 0) {
            sip.setDeliverableUnits(dusc);
        }


        for (DcsDeliverableUnit d : dus) {
            Collection<DcsResourceIdentifier> alternateIds = null;
            if(d.getParents() ==null ||d.getParents().isEmpty())  {
                alternateIds = d.getAlternateIds();
                if(alternateIds!=null){
                    DcsResourceIdentifier id = null;
                    Iterator<DcsResourceIdentifier> idIt = alternateIds.iterator();
                    while(idIt.hasNext()){
                        id = idIt.next();
                        if(id.getTypeId().equalsIgnoreCase("dpnobjectid")) {
                            tarFileName = id.getIdValue()+".tar";
                            System.out.println("Alternate Object ID for dpnobjectid: "+tarFileName);
                            break;
                        }
                    }

                }
                SeadDataLocation dataLocation = ((SeadDeliverableUnit) d).getPrimaryLocation();
                bagSize = ((SeadDeliverableUnit)d).getSizeBytes();
                System.out.println("bagSize is "+bagSize);
                dirPath = dataLocation.getLocation();


                File bagDirectory = new File(dirPath);
                FileWriter bagInfoStream = null;
                System.out.println(((SeadDeliverableUnit)d).getSizeBytes());
                bagSize = ((SeadDeliverableUnit)d).getSizeBytes()+13414; // Add the size of the extra files
                try {
                    String bagInfoTxtFilePath = bagDirectory.toString() + "/bag-info.txt";
                    bagInfoStream = new FileWriter(bagInfoTxtFilePath,true);
                    bagInfo = new BufferedWriter(bagInfoStream);
                    bagInfo.write("Bag-Size: "+bagSize+" Bytes");
                    bagInfo.close();

                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }


                File tarDirectory = new File(dirPath);
                if(tarFileName.isEmpty()){
                      tarFileName = "dpntest.tar";
                }
                tarFilePath = dirPath.toString()+tarFileName;
                this.tarDirectory(tarDirectory,tarFilePath);

            }
        }
//        tarFileLocation = tarFileLocation +tarFileName;
//        System.out.println("TarFileLocation in TarUtilService"+tarFileLocation);
//        System.out.println("TarFilePath in TarUtilService: "+tarFilePath);

        for(DcsDeliverableUnit du :sip.getDeliverableUnits())
        {
            SeadDataLocation location = new SeadDataLocation();
            location.setLocation(tarFilePath); //for tar requirement
            location.setName("filepath");
            location.setType("filepath");
            ((SeadDeliverableUnit) du).setPrimaryLocation(location);
            try {
                String SHA1Fixity = generateCheckSum(tarFilePath,"SHA-256");
                DcsResourceIdentifier dpnSHA1FixityValue = new DcsResourceIdentifier();
                dpnSHA1FixityValue.setIdValue(SHA1Fixity);
                dpnSHA1FixityValue.setTypeId("fixity-sha1");
                du.addAlternateId(dpnSHA1FixityValue);
                String MD5Fixity = generateCheckSum(tarFilePath,"MD5");
                DcsResourceIdentifier dpnMD5FixityValue = new DcsResourceIdentifier();
                dpnMD5FixityValue.setIdValue(MD5Fixity);
                dpnMD5FixityValue.setTypeId("fixity-md5");
                du.addAlternateId(dpnMD5FixityValue);
                XStream xStream = new XStream(new DomDriver());
                xStream.alias("map",Map.class);
                Map<String,String> map = new HashMap<String, String>();
                String key = "TarFileLocation";
                map.put(key, tarFileLocation +tarFileName); //Here key and value would be your key and value
                DcsMetadata metadata = new DcsMetadata();
                metadata.setSchemaUri(key);
                metadata.setMetadata(xStream.toXML(map));
                du.addMetadata(metadata);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        try {
            CopyTarFile(tarFilePath, tarFileLocation+tarFileName);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        ingest.getSipStager().updateSIP(sip,sipRef);
        addTarEvent(sipRef);
    }

    public void CopyTarFile(String inputFile, String destinationFile) throws IOException{
        System.out.println("inputFile in setTarFileLocation:"+inputFile);
        System.out.println("destinationFile in setTarFileLocation:"+destinationFile);
        File source = new File(inputFile);
        File destination = new File(destinationFile);
        FileUtils.copyFile(source,destination);
    }

    public void unTarFile(String inputTarPath, String destinationDirectory)throws IOException {
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

    /**
     * This method is used to create a tar file from the contents of the
     * directory being passed with the file name being passed.
     * @param dir
     * @param tarFileName
     */
    List<String> filesListInDir;
    public void tarDirectory(File dir, String tarFileName){
        try{
            filesListInDir = new ArrayList<String>();
            getFilesList(dir);
            FileOutputStream fos = new FileOutputStream(tarFileName);
            ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR, fos);
            for(String filePath : filesListInDir){
                File inputFile = new File(filePath);
                TarArchiveEntry te = new TarArchiveEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
                te.setSize(inputFile.length());
                aos.putArchiveEntry(te);
                IOUtils.copy(new FileInputStream(inputFile), aos);
                aos.closeArchiveEntry();
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
    private void getFilesList(File dir) throws IOException {
        File[] files = dir.listFiles();
        for(File file : files){
            if(file.isFile()) filesListInDir.add(file.getAbsolutePath());
            else getFilesList(file);
        }
    }

    private String generateCheckSum(String tarFilePath, String algorithm) throws Exception{
        System.out.println("generateCheckSum");
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        FileInputStream fileInputStream = new FileInputStream(tarFilePath);
        byte[] dataBytes = new byte[1024];
        int bytesRead = 0;
        while((bytesRead = fileInputStream.read(dataBytes)) != -1){
            messageDigest.update(dataBytes,0,bytesRead);
        }
        byte[] digestBytes = messageDigest.digest();
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < digestBytes.length; i++) {
            sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        System.out.println(algorithm+" Checksum for the tar File: " + sb.toString());
        fileInputStream.close();
        return sb.toString();

    }

    private void addTarEvent(String sipRef) {
        DcsEvent tarEvent = ingest.getEventManager().newEvent(Events.TARBAG);
        ResearchObject dcp = (ResearchObject)ingest.getSipStager().getSIP(sipRef);
        ingest.getEventManager().addEvent(sipRef, tarEvent);
    }
}

