/**
 * Description: The class is used to generate the ReplicationInitQuery Message.
 * Input: replication_size, dpn_object_id
 * Return: DPNMsg
 */

package edu.iu.dpn.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Required;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static edu.iu.dpn.messaging.DPNMsgConstants.*;
import static edu.iu.dpn.messaging.DPNMsgConstants.DPN_OBJECT_ID;
import static edu.iu.dpn.messaging.DPNMsgConstants.PROTOCOL;

public class DPNReplicationInitQuery {
    String replication_size;
    String dpn_object_id;
    String correlation_id;

    //@Value("${broadCastKey}")
    public String broadCastKey;

    @Required
    public void setBroadCastKey(String broadCastKey){
        this.broadCastKey = broadCastKey;
        //System.out.println("broadCastKey in setBroadCastKey is "+broadCastKey);
    }

    public String getBroadCastKey(){
        return broadCastKey;
    }

    public void setReplication_size(String size){
        this.replication_size = size;
    }

    public void setDpn_object_id(String object_id){
        this.dpn_object_id = object_id;
    }

    public void setCorrelation_id(String id){
        this.correlation_id = id;
    }

    public DPNReplicationInitQuery(){}
    public DPNMsg getDPNReplicationInitMsg(){
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        DPNMsg msg = new DPNMsg();
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(FROM.toString(), DPNMsgConstants.FROM_VALUE.toString());
        //System.out.println("BroadCastKey in InitQuery is: "+this.getBroadCastKey());
        headerMap.put(REPLY_KEY.toString(), DPNMsgConstants.REPLY_KEY_VALUE.toString());
        headerMap.put(SEQUENCE.toString(),DPNMsgConstants.REP_INT_QUERY_SEQUENCE.toString());
        //System.out.println("INIT_QUERY:CORRELATION_ID ="+correlation_id);
        headerMap.put(CORRELATION_ID.toString(), correlation_id);
        headerMap.put(DATE.toString(), today.format(new Date()).toString());
        headerMap.put(TTL.toString(),DPNMsgConstants.TTL_DURATION.toString());
        DPNMsgHeader DPNMsgHeader = new DPNMsgHeader();
        DPNMsgHeader.setHeader(headerMap);
        msg.setHeader(DPNMsgHeader);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(MESSAGE_NAME.toString(), DPNMsgTypes.REPLICATION_INIT_QUERY.toString());
        bodyMap.put(REPLICATION_SIZE.toString(), replication_size);
        //bodyMap.put(PROTOCOL.toString(), DPNMsgConstants.PROTOCOL_TYPE.toString());
        bodyMap.put(PROTOCOL.toString(), new String[] {DPNMsgConstants.PROTOCOL_TYPE.toString()});
        bodyMap.put(DPN_OBJECT_ID.toString(), dpn_object_id);
        DPNMsgBody DPNMsgBody = new DPNMsgBody();
        DPNMsgBody.setBody(bodyMap);
        msg.setBody(DPNMsgBody);
        return msg;
    }
}
