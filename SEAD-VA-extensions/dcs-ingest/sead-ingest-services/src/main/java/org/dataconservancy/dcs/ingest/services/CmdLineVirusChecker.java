package org.dataconservancy.dcs.ingest.services;

import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.seadva.model.pack.ResearchObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

/** Does a virus scan on a files in a sip */
public class CmdLineVirusChecker
        extends IngestServiceBase
        implements IngestService {

    private String sipRef;

    private int numberOfFiles;
    
    private int completedScans;
    



    @Override
    public void execute(String sipRef) throws IngestServiceException {

        this.sipRef = sipRef;
        ResearchObject dcp = (ResearchObject)ingest.getSipStager().getSIP(sipRef);

        completedScans = 0;

        int infectedFiles =0;

        Collection<DcsFile> files = dcp.getFiles();
        for(DcsFile file: files) {
            try {
                String[] filePath = file.getSource().split("file://");
                Boolean copyClean = checkForVirus(filePath[filePath.length-1]);
                if(!copyClean)
                    infectedFiles++;
                file.setValid(
                        copyClean
                );

            } catch (Exception e) {
                throw new IngestServiceException("Error opening stream to file, "
                        + e);
            }

            
        }

        dcp.setFiles(files);
        addVirusScanEvent(sipRef,infectedFiles);


        /* save the SIP containing updated entities */
        ingest.getSipStager().updateSIP(dcp, sipRef);
        if(infectedFiles>0)
            throw new IngestServiceException("Found Virus in the dataset");
    }



    private void addVirusScanEvent(String sipRef, int infectedFiles) {
        DcsEvent archiveEvent =
                ingest.getEventManager().newEvent(Events.VIRUS_SCAN);

        archiveEvent.setOutcome(Integer.toString(infectedFiles));
        archiveEvent.setDetail("Virus Scan Successful");
        //  archiveEvent.setTargets(entities);

        ingest.getEventManager().addEvent(sipRef, archiveEvent);
    }

    private boolean checkForVirus(String filePath){
        String s = null;

        try {

            // run the Unix "ps -ef" command
            // using the Runtime exec method:
            Process p = Runtime.getRuntime().exec(
                    "clamscan " +
                     filePath
                    );

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            int count =0;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
                if(s.contains("Infected files"))   {
                    System.out.print("Count str="+s.split(":")[1].replace(" ","")+"---\n");
                    count = Integer.parseInt(s.split(":")[1].replace(" ",""));
                    System.out.print("Count int="+count+"---\n");
                }
            }

            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                if(s.contains("Infected files"))   {
                    System.out.print("Count str="+s.split(":")[1].replace(" ","")+"---\n");
                    count = Integer.parseInt(s.split(":")[1].replace(" ",""));
                    System.out.print("Count int="+count+"---\n");
                }
                System.out.println(s);
            }

            if(count==0)
                return true;
            else
                return false;
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            //System.exit(-1);
            return false;
        }
    }

}
