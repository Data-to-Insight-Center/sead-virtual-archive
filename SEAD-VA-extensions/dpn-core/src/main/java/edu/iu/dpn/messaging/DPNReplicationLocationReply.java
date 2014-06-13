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
    public DPNMsg getDPNReplicationLocationMsg(String correlation_id, String reply_key){
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        DPNMsg msg = new DPNMsg();
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(FROM.toString(), DPNMsgConstants.FROM_VALUE);
        headerMap.put(REPLY_KEY.toString(), reply_key);
        headerMap.put(SEQUENCE.toString(),DPNMsgConstants.REP_LOC_REPLY_SEQUENCE);
        headerMap.put(CORRELATION_ID.toString(), correlation_id);
        headerMap.put(DATE.toString(), today.format(new Date()).toString());
        headerMap.put(TTL.toString(),DPNMsgConstants.TTL_DURATION);
        DPNMsgHeader DPNMsgHeader = new DPNMsgHeader();
        DPNMsgHeader.setHeader(headerMap);
        msg.setHeader(DPNMsgHeader);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(MESSAGE_NAME.toString(), DPNMsgTypes.REPLICATION_LOCATION_REPLY);
        bodyMap.put(PROTOCOL.toString(), DPNMsgConstants.PROTOCOL_TYPE);
        bodyMap.put(LOCATION.toString(),"SDA");
        DPNMsgBody DPNMsgBody = new DPNMsgBody();
        DPNMsgBody.setBody(bodyMap);
        msg.setBody(DPNMsgBody);
        return msg;
    }
}
