package org.dataconservancy.dcs.ingest.services;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;

import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.pack.ResearchObject;
import java.io.Writer;

public class MiscService extends IngestServiceBase implements IngestService {
    String dirPath;
    String manifestFilePath;
    Writer bagInfo;
    Writer manifestInfo;
    Writer manifest;
    File bagDirectory;

    public void execute(String sipRef) {
        long bagSize;
        ResearchObject sip = (ResearchObject)ingest.getSipStager().getSIP(sipRef);
        Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();

        for (DcsDeliverableUnit d : dus) {
            if(d.getParents() ==null ||d.getParents().isEmpty())  {
                SeadDataLocation dataLocation = ((SeadDeliverableUnit) d).getPrimaryLocation();
                dirPath = dataLocation.getLocation();
                bagDirectory = new File(dirPath);

                Collection<DcsFile> dcsFiles = sip.getFiles();
                manifestFilePath = bagDirectory.toString() + "/manifest-sha256.txt";
                System.out.println("MiscServiceFixity - manifestFilePath "+manifestFilePath);
                FileWriter manifestStream = null;
                for (DcsFile dcsFile : dcsFiles) {
                    String fileName = dcsFile.getName();
                    System.out.println("MiscServiceFixity - FileName "+fileName);
                    for (DcsFixity fixity:dcsFile.getFixity()){
                        System.out.println("MiscServiceFixity - getAlgorithm "+fixity.getAlgorithm());
                        if(fixity.getAlgorithm().equals("SHA-1")){
                            String strCheckSum = fixity.getValue().toString();
                            System.out.println("MiscServiceFixity - fixity Algo " + fixity.getAlgorithm());
                            try {
                                manifestStream = new FileWriter(manifestFilePath,true);
                                manifestInfo = new BufferedWriter(manifestStream);
                                manifestInfo.write(strCheckSum+"\tdata/"+fileName+"\n");
                                manifestInfo.close();
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                        }
                    }

                }

                String bagPath = bagDirectory.toString();
                //FileWriter manifestStream = null;
                System.out.println("TagManifestBagPath : "+bagPath);
                String manifestFilePath = bagPath + "/tagmanifest-sha256.txt";
                try {
                    manifestStream = new FileWriter(manifestFilePath);
                    manifest = new BufferedWriter(manifestStream);
                    File dir = new File(bagPath);
                    File[] dirListing = dir.listFiles();
                    if(dirListing != null){
                        for(File child : dirListing){
                            if (!child.isDirectory()) {
                                String file = child.toString();
                                int pos;
                                pos = file.lastIndexOf('/');
                                String fileName = file.substring(pos+1);
                                System.out.println("TagFileName is :"+fileName);

                                if (!fileName.equals("tagmanifest-sha256.txt")) {
                                    try{
                                        String fixity = generateCheckSum(file,"SHA1");
                                        manifest.write(fixity + "  " + fileName+"\n");
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    manifest.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private String generateCheckSum(String filePath, String algorithm) throws Exception{
        System.out.println("ManifestGenerateCheckSum:" + filePath);
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        FileInputStream fileInputStream = new FileInputStream( filePath);
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
        System.out.println(algorithm+" Checksum for the file: " + sb.toString());
        fileInputStream.close();
        return sb.toString();

    }
}
