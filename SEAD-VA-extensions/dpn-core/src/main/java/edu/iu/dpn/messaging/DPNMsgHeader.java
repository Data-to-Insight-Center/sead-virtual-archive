package edu.iu.dpn.messaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Aravindh
 * Date: 04/06/2014
 * Time: 15:52
 * To change this template use File | Settings | File Templates.
 */
public class DPNMsgHeader {
    private Map<String, Object> header = new HashMap<>();
    public DPNMsgHeader(){}
    public DPNMsgHeader(DPNMsgHeader header){
        this.header.putAll(header.getHeader());
    }
    public void setHeader(Map<String, Object> header){
        this.header = header;
    }

    public Map<String, Object> getHeader(){
        return header;
    }

    public boolean equals(DPNMsgHeader other){
        if (!header.equals(other.header)){
            for (Map.Entry<String, Object> e : header.entrySet()){
                String k = e.getKey();
                Object v = e.getValue();

            }
            return false;
        }
        return true;
    }
}
