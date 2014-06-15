package org.dataconservancy.dcs.ingest.services.runners.model;

import org.dataconservancy.dcs.ingest.services.IngestService;
import org.dataconservancy.dcs.ingest.services.runners.RulesExecutorBootstrap;

import java.util.Queue;

/**
 * Service Queue handler to avoid concurrent modifications
 */
final public class ServiceQueueModifier {

    Queue<IngestService> ingestServices;

    public ServiceQueueModifier(Queue<IngestService> ingestServices){
        this.ingestServices = ingestServices;
    }

    public void addIngestServicesName(String serviceName) {
        synchronized(ingestServices){
            ingestServices.add(RulesExecutorBootstrap.serviceMap.get(serviceName));
        }
    }

    //Sequential and single retrieval for a specific workflow/SIP
    public IngestService getIngestService() {
        synchronized(ingestServices){

            if(ingestServices!=null&& ingestServices.size()>0){
                IngestService svc = ingestServices.peek();
                ingestServices.remove();
                return svc;
            }
        }
        return null; // No service yet for this SIP
    }

    public boolean isEmpty(){
        if(ingestServices.size()==0)
            return true;
        else
            return false;
    }
}
