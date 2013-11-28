/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.ingest.services;

import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.virusscanning.VirusScanManager;
import org.dataconservancy.dcs.virusscanning.VirusScanRequest;
import org.dataconservancy.dcs.virusscanning.VirusScannerFactory;
import org.dataconservancy.dcs.virusscanning.event.ScanCompleteEvent;
import org.dataconservancy.dcs.virusscanning.event.VirusScanEventListener;
import org.dataconservancy.dcs.virusscanning.event.VirusScanningEventManager;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.springframework.beans.factory.annotation.Required;

/** Does a virus scan on a files in a sip */
public class VirusChecker
        extends IngestServiceBase
        implements IngestService, VirusScanEventListener {

    private String sipRef;

    private VirusScanManager manager;

    private VirusScannerFactory[] scannerFactories;
    
    private int numberOfFiles;
    
    private int completedScans;
    
    private ArrayList<String> uniqueIds;

    @Required
    public void setScannerFactories(VirusScannerFactory[] scanners) {
        scannerFactories = scanners;
    }

    @Override
    public void execute(String sipRef) throws IngestServiceException {
        manager = VirusScanManager.getManager();
        manager.setScannerFactories(scannerFactories);

        uniqueIds = new ArrayList<String>();
        VirusScanningEventManager.getManager().registerListener(this);
        this.sipRef = sipRef;
        Dcp dcp = ingest.getSipStager().getSIP(sipRef);
        Collection<DcsFile> files = dcp.getFiles();
        
        numberOfFiles = files.size();
        completedScans = 0;
        
        Iterator<DcsFile> it = files.iterator();
        while (it.hasNext()) {
            DcsFile file = it.next();
            String name = file.getName();

            InputStream fileData = null;

            try {
                URL content = new URL(file.getSource());
                fileData = content.openStream();
            } catch (Exception e) {
                throw new IngestServiceException("Error opening stream to file, "
                        + e);
            }            
            
            uniqueIds.add(file.getId());
            VirusScanRequest request =
                    new VirusScanRequest(name, fileData, file.getId());
            manager.requestScan(request);
        }
    }

    @Override
    public void onScanComplete(ScanCompleteEvent event) {
        if( uniqueIds.contains(event.getRequest().getId()) )
        {
            DcsEvent dcsEvent =
                    ingest.getEventManager().newEvent(Events.VIRUS_SCAN);
    
            String timeDetails =
                    "Scan Start Time: " + event.getStartTimeAsString()
                            + " Finish Time: " + event.getEndTimeAsString();
            dcsEvent.setDate(timeDetails);
    
            if (event.hadError()) {
                dcsEvent.setOutcome("The virus scan by " + event.getScannerName() + " was not completed due to an error.");
                dcsEvent.setDetail(event.getResultDetails());
            } else if (event.containedVirus()) {
                dcsEvent.setOutcome("A virus was found in " + event.getRequest().getFileName() + " by " + event.getScannerName());
                dcsEvent.setDetail(event.getResultDetails());
            } else {
                dcsEvent.setOutcome(event.getRequest().getFileName() + " was scanned by: "
                        + event.getScannerName());
            }
    
            dcsEvent.addTargets(new DcsEntityReference(event.getRequest().getId()));
    
            ingest.getEventManager().addEvent(sipRef, dcsEvent);
            completedScans++;
        }
        //If we've gotten all the scans were expecting we can unregister.
        if(numberOfFiles * event.getNumberOfScans() == completedScans )
        {
            VirusScanningEventManager.getManager().unRegisterListener(this);
        }
    }

}
