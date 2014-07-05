package org.dataconservancy.dcs.ingest.services.runners.model;

import org.seadva.model.SeadDeliverableUnit;

import java.util.Iterator;
import java.util.Map;

/**
 * Service Queue handler to avoid concurrent modifications
 */
final public class RepositoryMatcher {

    Map<String, Integer> matchedRepositories;

    public RepositoryMatcher(Map<String, Integer> matchedRepositories){
        this.matchedRepositories = matchedRepositories;
    }

    public void addEntry(String repository, int priority) {
        synchronized(matchedRepositories){
            matchedRepositories.put(repository, priority);
        }
    }

    //Sequential and single retrieval for a specific workflow/SIP
    public String getFinalMatch(SeadDeliverableUnit du) {
        if(du==null) {
            synchronized(matchedRepositories){

                Iterator iterator = matchedRepositories.entrySet().iterator();
                int max = -1;
                String repoValue = null;
                while (iterator.hasNext()){
                    Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) iterator.next();
                    if(pair.getValue()>max)
                    {
                        max = pair.getValue();
                        repoValue = pair.getKey();
                    }
                    iterator.remove();
                }
                return repoValue;
            }
        }
    else{
            if(du.getPrimaryLocation().getName().contains("IU"))
                return "iu";
            if(du.getPrimaryLocation().getName().contains("Ideals"))
                return "uiuc";
            return "sda";
        }
    }
}
