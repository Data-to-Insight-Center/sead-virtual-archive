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
    public DPNMsg getDPNReplicationLocationMsg(String correlation_id){
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        DPNMsg msg = new DPNMsg();
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(FROM.toString(), DPNMsgConstants.FROM_VALUE);
        headerMap.put(REPLY_KEY.toString(), DPNMsgConstants.REPLY_KEY_VALUE);
        headerMap.put(SEQUENCE.toString(),DPNMsgConstants.REP_AVAIL_REPLY_SEQUENCE);
        headerMap.put(CORRELATION_ID.toString(), UUID.randomUUID().toString());
        headerMap.put(DATE.toString(), today.format(new Date()).toString());
        headerMap.put(TTL.toString(),DPNMsgConstants.TTL_DURATION);
        DPNMsgHeader DPNMsgHeader = new DPNMsgHeader();
        DPNMsgHeader.setHeader(headerMap);
        msg.setHeader(DPNMsgHeader);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(MESSAGE_NAME.toString(), DPNMsgTypes.REPLICATION_AVAILABLE_REPLY);
        bodyMap.put(PROTOCOL.toString(), DPNMsgConstants.PROTOCOL_TYPE);
        bodyMap.put(MESSAGE_ATT.toString(),DPNMsgAction.ACK);
        DPNMsgBody DPNMsgBody = new DPNMsgBody();
        DPNMsgBody.setBody(bodyMap);
        msg.setBody(DPNMsgBody);
        return msg;
    }
}
