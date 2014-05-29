package org.dataconservancy.dcs.ingest.services;

//import org.dataconservancy.dcs.ingest.services.IngestService;
//import org.dataconservancy.dcs.ingest.services.IngestServiceBase;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
//import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;

public class TarUtilService extends IngestServiceBase implements IngestService {
    String dirPath;
    String tarFileName = "dpntest.tar";
    String tarFilePath;
    public void execute(String sipRef) {

        System.out.println("SIP Ref in TarUtilService: "+sipRef);
        ResearchObject sip = (ResearchObject)ingest.getSipStager().getSIP(sipRef);


        Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();

        for (DcsDeliverableUnit d : dus) {

            if(d.getParents() ==null ||d.getParents().isEmpty())  {
                SeadDataLocation dataLocation = ((SeadDeliverableUnit) d).getPrimaryLocation();
                dirPath = dataLocation.getLocation();
                File tarDirectory = new File(dirPath);
                tarFilePath = dirPath.toString()+tarFileName;
                this.tarDirectory(tarDirectory,tarFilePath);

            }
        }
        System.out.println("TarFilePath in TarUtilService: "+tarFilePath);

        for(DcsDeliverableUnit du :sip.getDeliverableUnits())
        {
            SeadDataLocation location = new SeadDataLocation();
            location.setLocation(tarFilePath); //for tar requirement
            location.setName("filepath");
            location.setType("filepath");
            ((SeadDeliverableUnit) du).setPrimaryLocation(location);
        }

        ingest.getSipStager().updateSIP(sip,sipRef);
        addTarEvent(sipRef);
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

    private void addTarEvent(String sipRef) {
        DcsEvent tarEvent = ingest.getEventManager().newEvent(Events.TARBAG);
        ResearchObject dcp = (ResearchObject)ingest.getSipStager().getSIP(sipRef);
        ingest.getEventManager().addEvent(sipRef, tarEvent);
    }
}

