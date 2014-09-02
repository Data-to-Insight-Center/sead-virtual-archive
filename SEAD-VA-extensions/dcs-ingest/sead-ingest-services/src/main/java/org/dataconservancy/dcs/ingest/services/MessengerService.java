package org.dataconservancy.dcs.ingest.services;

/**
 * Created with IntelliJ IDEA.
 * User: Aravindh
 * Date: 15/06/2014
 * Time: 14:47
 * To change this template use File | Settings | File Templates.
 */

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import edu.iu.dpn.messaging.DPNMsg;
import edu.iu.dpn.messaging.DPNReplicationInitQuery;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.pack.ResearchObject;
import org.seadva.registry.mapper.DcsDBMapper;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class MessengerService extends IngestServiceBase implements IngestService{


    String registryUrl;
    String correlationID;

    @Required
    public void setRegistryUrl(String registryUrl)
    {
        this.registryUrl = registryUrl;
    }

    @Override
    public void execute(String sipRef) throws IngestServiceException {
        String fileName=null;
        long bagSize;

        correlationID = UUID.randomUUID().toString();

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
                initQuery.setCorrelation_id(correlationID);
                initQuery.setReplication_size(Objects.toString(bagSize,null));
                DPNMsg msg = initQuery.getDPNReplicationInitMsg();
                try{
                    msg.send(msg,null);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            Collection<DcsMetadata> metadatas = null;
            if (d.getParents()==null || d.getParents().isEmpty()){
                metadatas = d.getMetadata();
                if (metadatas!=null){
                    DcsMetadata metadata = null;
                    Iterator<DcsMetadata> metadataIterator = metadatas.iterator();
                    while (metadataIterator.hasNext()){
                        metadata = metadataIterator.next();
                        if (metadata.getMetadata().equalsIgnoreCase("TarFileLocation")){
                            System.out.println("TarFileLocation: "+metadata.getMetadata().toString());
                        }
                    }
                }
            }
        }

        for(DcsDeliverableUnit du :sip.getDeliverableUnits())
        {
            try{
                XStream xStream = new XStream(new DomDriver());
                xStream.alias("map",Map.class);
                Map<String,String> map = new HashMap<String, String>();
                String key = "MsgStatus";
                map.put(key, "INIT_QUERY"); //Here key and value would be your key and value
                DcsMetadata metadata = new DcsMetadata();
                metadata.setSchemaUri(key);
                metadata.setMetadata(xStream.toXML(map));
                du.addMetadata(metadata);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        for(DcsDeliverableUnit du :sip.getDeliverableUnits())
        {
            try{
                XStream xStream = new XStream(new DomDriver());
                xStream.alias("map",Map.class);
                Map<String,String> map = new HashMap<String, String>();
                String key = "CorrelationID";
                map.put(key, correlationID); //Here key and value would be your key and value
                DcsMetadata metadata = new DcsMetadata();
                metadata.setSchemaUri(key);
                metadata.setMetadata(xStream.toXML(map));
                du.addMetadata(metadata);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        ingest.getSipStager().updateSIP(sip,sipRef);
        try {
            new DcsDBMapper(this.registryUrl).mapfromSip(sip);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        addMessengerEvent(sipRef);
    }
    private void addMessengerEvent(String sipRef) {
        DcsEvent msgnrEvent = ingest.getEventManager().newEvent(Events.MSGNR);
        ResearchObject dcp = (ResearchObject)ingest.getSipStager().getSIP(sipRef);
        ingest.getEventManager().addEvent(sipRef, msgnrEvent);
    }
}
