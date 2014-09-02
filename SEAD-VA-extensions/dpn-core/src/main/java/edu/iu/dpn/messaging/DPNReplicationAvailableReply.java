package edu.iu.dpn.messaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static edu.iu.dpn.messaging.DPNMsgConstants.*;
import static edu.iu.dpn.messaging.DPNMsgConstants.LOCATION;
import static edu.iu.dpn.messaging.DPNMsgConstants.PROTOCOL;

/**
 * Created with IntelliJ IDEA.
 * User: Aravindh
 * Date: 12/06/2014
 * Time: 00:44
 * To change this template use File | Settings | File Templates.
 */
public class DPNReplicationAvailableReply {
    DPNReplicationAvailableReply(){}
    public DPNMsg getDPNReplicationAvailableMsg(String correlation_id){
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        DPNMsg msg = new DPNMsg();
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(FROM.toString(), DPNMsgConstants.FROM_VALUE.toString());
        headerMap.put(REPLY_KEY.toString(), DPNMsgConstants.REPLY_KEY_VALUE.toString());
        headerMap.put(SEQUENCE.toString(),DPNMsgConstants.REP_AVAIL_REPLY_SEQUENCE.toString());
        //headerMap.put(CORRELATION_ID.toString(), UUID.randomUUID().toString());
        //System.out.println("AVAIL_QUERY:CORRELATION_ID ="+correlation_id);
        headerMap.put(CORRELATION_ID.toString(), correlation_id);
        headerMap.put(DATE.toString(), today.format(new Date()).toString());
        headerMap.put(TTL.toString(),DPNMsgConstants.TTL_DURATION);
        DPNMsgHeader DPNMsgHeader = new DPNMsgHeader();
        DPNMsgHeader.setHeader(headerMap);
        msg.setHeader(DPNMsgHeader);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(MESSAGE_NAME.toString(), DPNMsgTypes.REPLICATION_AVAILABLE_REPLY.toString());
        //bodyMap.put(PROTOCOL.toString(), DPNMsgConstants.PROTOCOL_TYPE.toString());
        bodyMap.put(PROTOCOL.toString(), new String[] {DPNMsgConstants.PROTOCOL_TYPE.toString()});
        bodyMap.put(MESSAGE_ATT.toString(),DPNMsgAction.ACK.toString());
        DPNMsgBody DPNMsgBody = new DPNMsgBody();
        DPNMsgBody.setBody(bodyMap);
        msg.setBody(DPNMsgBody);
        return msg;
    }
}
