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
package org.dataconservancy.transform.dcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;

/**
 * Splits a DCP and returns only the terminal DUs and their descendants.
 * <p>
 * Given a Dcp with <code>N</code> more DUs, with <code>M</code> DUs containing
 * only manifestations as children, will write M output DCPs containing a single
 * DU and all its descendants, and associated events. DUs that are hierarchy
 * nodes (i.e. have DU children) will not be passed along.
 * </p>
 */
public class LeafNodeDUFilter
        implements Mapping<String, Dcp, String, Dcp> {

    @Override
    public void map(String key, Dcp val, Output<String, Dcp> output) {
        for (Map.Entry<String, Dcp> result : split(val).entrySet()) {
            output.write(result.getKey(), result.getValue());
        }
    }

    /**
     * Parse the incoming dcp into single-du dcps
     */
    private Map<String, Dcp> split(Dcp sourceDcp) {
        Map<String, Dcp> resultMap =
                new HashMap<String, Dcp>(sourceDcp.getDeliverableUnits().size());

        HashMap<String, List<DcsEvent>> flattenEventsHashMap =
                splitEvents(sourceDcp);
        HashMap<String, DcsFile> flattenFilesHashMap = splitFiles(sourceDcp);

        List<DcsEvent> holderEventsList = null;

        Dcp currentDcp;
        //for each deliverable unit
        for (DcsDeliverableUnit currentDU : sourceDcp.getDeliverableUnits()) {
            //create a new DCP
            currentDcp = new Dcp();
            holderEventsList = null;

            //add current DU to the new dcp
            currentDcp.addDeliverableUnit(currentDU);
            //look in flattenEventsHashMap for events relating to this DU
            holderEventsList = flattenEventsHashMap.get(currentDU.getId());
            //if there is a list of events for this DU
            if (holderEventsList != null) {
                //add the event to the containing dcp
                addEvent(currentDcp, convertToArray(holderEventsList));
            }

            //add the new dcp to the dcps HashMap
            resultMap.put(currentDU.getId(), currentDcp);

        }

        currentDcp = null;
        holderEventsList = null;
        //DcsManifestation currentManifestation = null;
        String currentFileID = null;
        DcsFile currentFile = null;

        //set iterator to iterate through source dcp manifestations list
        //for each manifestation
        for (DcsManifestation currentManifestation : sourceDcp
                .getManifestations()) {
            //currentManifestation = manifestationsIter.next();
            //Locate the correct dcp using the manifestation's DU's id
            currentDcp =
                    resultMap.get(currentManifestation.getDeliverableUnit());

            //Then add the manifestation to the appropriate dcp
            currentDcp.addManifestation(currentManifestation);

            //look in flattenEventsHashMap for events relating to this manifestation
            holderEventsList =
                    flattenEventsHashMap.get(currentManifestation.getId());

            //if there is a list of events for this manifestation
            if (holderEventsList != null) {
                //add the event to the containing dcp
                addEvent(currentDcp, convertToArray(holderEventsList));
            }

            holderEventsList = null;
            //for each manifestation file
            for (DcsManifestationFile currentMFile : currentManifestation
                    .getManifestationFiles()) {
                //extracting FileID from manifestation file
                //DcsManifestationFile.getRef() -> DcsFileRef:DcsEntityReference.getRef()
                currentFileID = currentMFile.getRef().getRef();

                //locate the DcsFile indicated by currentFileID
                currentFile = flattenFilesHashMap.get(currentFileID);
                //if a file is found
                if (currentFile != null) {
                    //add file to current dcp
                    currentDcp.addFile(currentFile);
                }

                //look for associated event in flattenEventsList
                holderEventsList = flattenEventsHashMap.get(currentFileID);
                //if an event exists for this file
                if (holderEventsList != null) {
                    addEvent(currentDcp, convertToArray(holderEventsList));
                }

            }
        }
        return resultMap;
    }

    private HashMap<String, DcsFile> splitFiles(Dcp sourceDcp) {
        HashMap<String, DcsFile> flattenFilesHashMap =
                new HashMap<String, DcsFile>();

        for (DcsFile currentFile : sourceDcp.getFiles()) {
            flattenFilesHashMap.put(currentFile.getId(), currentFile);
        }
        return flattenFilesHashMap;
    }

    private HashMap<String, List<DcsEvent>> splitEvents(Dcp sourceDcp) {
        //create a holder HashMap to store the flatten events list
        HashMap<String, List<DcsEvent>> flattenEventsHashmMap =
                new HashMap<String, List<DcsEvent>>();

        List<DcsEvent> eventsList = null;
        String targetID;

        //for each event
        for (DcsEvent currentEvent : sourceDcp.getEvents()) {
            //iterate through each event's target
            for (DcsEntityReference entityRef : currentEvent.getTargets()) {
                //get target-id
                targetID = entityRef.getRef();

                //check to see if an events list already exists for this particular target
                eventsList = flattenEventsHashmMap.get(targetID);

                //if such events list doesn't exist
                if (eventsList == null) {
                    //create a new event list for this targetID
                    eventsList = new ArrayList<DcsEvent>();
                    //add current event to the events list 
                    eventsList.add(currentEvent);
                    //ad events list to flatten events hash map
                    flattenEventsHashmMap.put(targetID, eventsList);
                }
                //if the events list for the target is found
                else {
                    //retrieve events list using targetID 
                    //then add current event to that list
                    eventsList.add(currentEvent);
                }
            }
        }
        return flattenEventsHashmMap;
    }

    private static DcsEvent[] convertToArray(List<DcsEvent> toConvert) {
        int sizeOfEventsList = toConvert.size();
        DcsEvent[] eventsArray = new DcsEvent[sizeOfEventsList];
        for (int i = 0; i < sizeOfEventsList; i++) {
            eventsArray[i] = toConvert.get(i);
        }
        return eventsArray;
    }

    private static void addEvent(Dcp dcp, DcsEvent... events) {
        // Only add the event if the DCP doesn't already contain it
        for (DcsEvent event : events) {
            if (dcp.getEvents().contains(event)) {
                continue;
            }
            dcp.addEvent(event);
        }
    }

}
