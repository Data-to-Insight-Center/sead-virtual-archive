package org.dataconservancy.dcs.ingest.services;

import java.io.*;
import java.util.*;

import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.pack.ResearchObject;
import java.io.Writer;

public class MiscService extends IngestServiceBase implements IngestService {
    String dirPath;
    String manifestFilePath;
    Writer bagInfo;
    Writer manifestInfo;
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
                FileWriter bagInfoStream = null;
                bagSize = ((SeadDeliverableUnit)d).getSizeBytes();
                try {
                    String bagInfoTxtFilePath = bagDirectory.toString() + "/bag-info.txt";
                    bagInfoStream = new FileWriter(bagInfoTxtFilePath,true);
                    bagInfo = new BufferedWriter(bagInfoStream);
                    bagInfo.write("Bag-Size: "+bagSize+" Bytes");
                    bagInfo.close();

                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                System.out.println("Misc Service - bagSize is "+bagSize);
            }
        }

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

    }
}
