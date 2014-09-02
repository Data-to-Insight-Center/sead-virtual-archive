package edu.iu.dpn.messaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static edu.iu.dpn.messaging.DPNMsgConstants.*;
import static edu.iu.dpn.messaging.DPNMsgConstants.LOCATION;
import static edu.iu.dpn.messaging.DPNMsgConstants.PROTOCOL;

/**
 * Description: Used to verify the checksum send by the nodes with the
 * checksum held in the registry
 * Input:
 * 1. Checksum method
 * 2. Checksum value
 * 3. DPN Object ID
 */
public class DPNReplicationVerificationReply {
    DPNReplicationVerificationReply(){}
    public DPNMsg getDPNReplicationVerificationReplyMsg(String correlationID, String reply_key, boolean isFine){
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        DPNMsg msg = new DPNMsg();
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(FROM.toString(), DPNMsgConstants.FROM_VALUE.toString());
        headerMap.put(REPLY_KEY.toString(), reply_key);
        headerMap.put(SEQUENCE.toString(),DPNMsgConstants.REP_VERIFY_REPLY_SEQUENCE.toString());
        headerMap.put(CORRELATION_ID.toString(), correlationID);
        headerMap.put(DATE.toString(), today.format(new Date()).toString());
        headerMap.put(TTL.toString(),DPNMsgConstants.TTL_DURATION.toString());
        DPNMsgHeader DPNMsgHeader = new DPNMsgHeader();
        DPNMsgHeader.setHeader(headerMap);
        msg.setHeader(DPNMsgHeader);

        // Get the checksum value for the DPN ObjectID
        // Check if the values match
        // If yes then send an acknowledgement


        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(MESSAGE_NAME.toString(), DPNMsgTypes.REPLICATION_VERIFY_REPLY.toString());
        if(isFine)
            bodyMap.put(MESSAGE_ATT.toString(), DPNMsgAction.ACK.toString());
        else
            bodyMap.put(MESSAGE_ATT.toString(), DPNMsgAction.NACK.toString());
        //bodyMap.put(LOCATION.toString(),"SDA");
        DPNMsgBody DPNMsgBody = new DPNMsgBody();
        DPNMsgBody.setBody(bodyMap);
        msg.setBody(DPNMsgBody);
        return msg;
    }
}
