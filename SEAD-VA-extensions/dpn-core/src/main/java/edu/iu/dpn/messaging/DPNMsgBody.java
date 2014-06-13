package edu.iu.dpn.messaging;

/**
 * Created with IntelliJ IDEA.
 * User: Aravindh
 * Date: 04/06/2014
 * Time: 13:33
 * To change this template use File | Settings | File Templates.
 */

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DPNMsgBody implements Serializable {
    private Map<String, Object> body = new HashMap<>();
    public DPNMsgBody(){}
    public DPNMsgBody(DPNMsgBody body){
        this.body.putAll(body.getBody());
    }

    public void setBody(Map<String, Object> body){
        this.body = body;
    }

    public Map<String, Object> getBody(){
        return body;
    }

    public boolean equals(DPNMsgBody other){
        if (!body.equals(other.body)){
            for (Map.Entry<String, Object> e : body.entrySet()){
                String k = e.getKey();
                Object v = e.getValue();

            }
            return false;
        }
        return true;
    }

}
