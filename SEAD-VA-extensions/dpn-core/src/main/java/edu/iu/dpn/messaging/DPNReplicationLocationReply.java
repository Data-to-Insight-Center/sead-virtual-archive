package edu.iu.dpn.messaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
//import java.util.UUID;

import static edu.iu.dpn.messaging.DPNMsgConstants.*;

/**
 * Created with IntelliJ IDEA.
 * User: Aravindh
 * Date: 12/06/2014
 * Time: 00:16
 * To change this template use File | Settings | File Templates.
 */
public class DPNReplicationLocationReply {
    DPNReplicationLocationReply(){}
    public DPNMsg getDPNReplicationLocationMsg(String correlation_id, String reply_key, String location){
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        DPNMsg msg = new DPNMsg();
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(FROM.toString(), DPNMsgConstants.FROM_VALUE.toString());
        headerMap.put(REPLY_KEY.toString(), DPNMsgConstants.REPLY_KEY_VALUE.toString());
        headerMap.put(SEQUENCE.toString(),DPNMsgConstants.REP_LOC_REPLY_SEQUENCE.toString());
        //System.out.println("LOC_QUERY:CORRELATION_ID ="+correlation_id);
        headerMap.put(CORRELATION_ID.toString(), correlation_id);
        headerMap.put(DATE.toString(), today.format(new Date()).toString());
        headerMap.put(TTL.toString(),DPNMsgConstants.TTL_DURATION.toString());
        DPNMsgHeader DPNMsgHeader = new DPNMsgHeader();
        DPNMsgHeader.setHeader(headerMap);
        msg.setHeader(DPNMsgHeader);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(MESSAGE_NAME.toString(), DPNMsgTypes.REPLICATION_LOCATION_REPLY.toString());
        //bodyMap.put(PROTOCOL.toString(), DPNMsgConstants.PROTOCOL_TYPE.toString());
        bodyMap.put(PROTOCOL.toString(), new String[] {DPNMsgConstants.PROTOCOL_TYPE.toString()});
        bodyMap.put(LOCATION.toString(),location);
        DPNMsgBody DPNMsgBody = new DPNMsgBody();
        DPNMsgBody.setBody(bodyMap);
        msg.setBody(DPNMsgBody);
        return msg;
    }
}
