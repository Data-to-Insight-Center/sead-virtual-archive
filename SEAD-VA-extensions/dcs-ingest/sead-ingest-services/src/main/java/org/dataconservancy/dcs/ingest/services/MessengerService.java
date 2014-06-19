package org.dataconservancy.dcs.ingest.services;

/**
 * Created with IntelliJ IDEA.
 * User: Aravindh
 * Date: 15/06/2014
 * Time: 14:47
 * To change this template use File | Settings | File Templates.
 */

import edu.iu.dpn.messaging.DPNMsg;
import edu.iu.dpn.messaging.DPNReplicationInitQuery;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.pack.ResearchObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class MessengerService extends IngestServiceBase implements IngestService{
    @Override
    public void execute(String sipRef) throws IngestServiceException {
        String fileName=null;
        long bagSize;

        ResearchObject sip = (ResearchObject)ingest.getSipStager().getSIP(sipRef);
        Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();
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
                            fileName = id.getIdValue();
                            System.out.println("Alternate Object ID for dpnobjectid: "+fileName);
                            break;
                        }
                    }
                }
                SeadDataLocation dataLocation = ((SeadDeliverableUnit) d).getPrimaryLocation();
                bagSize = ((SeadDeliverableUnit)d).getSizeBytes();
                DPNReplicationInitQuery initQuery = new DPNReplicationInitQuery();
                initQuery.setDpn_object_id(fileName);
                initQuery.setReplication_size(Objects.toString(bagSize,null));
                DPNMsg msg = initQuery.getDPNReplicationInitMsg();
                try{
                    msg.send(msg,null);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        ingest.getSipStager().updateSIP(sip,sipRef);
        addMessengerEvent(sipRef);
    }
    private void addMessengerEvent(String sipRef) {
        DcsEvent msgnrEvent = ingest.getEventManager().newEvent(Events.MSGNR);
        ResearchObject dcp = (ResearchObject)ingest.getSipStager().getSIP(sipRef);
        ingest.getEventManager().addEvent(sipRef, msgnrEvent);
    }
}
