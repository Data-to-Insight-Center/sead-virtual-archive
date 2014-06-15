package org.dataconservancy.dcs.ingest.services.util;

import org.dataconservancy.dcs.ingest.services.rules.impl.Executor;

import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: kavchand
 * Date: 4/30/14
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */
public final  class Output {

    public static String repoName;

    public static void put(String value, int priority){
        Executor.mapPriorities.put(value, priority);
    }

    public static String getDecision(){
        Iterator iterator = Executor.mapPriorities.entrySet().iterator();
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
        repoName = repoValue;
        return repoValue;
    }
}
