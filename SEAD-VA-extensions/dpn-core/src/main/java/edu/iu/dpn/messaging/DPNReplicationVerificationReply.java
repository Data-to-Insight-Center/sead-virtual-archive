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
    public DPNMsg getDPNReplicationVerificationReplyMsg(String DPNObjectID, String checkSum, String checkSumValue, String reply_key, String correlation_id){
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

        // Get the checksum value for the DPN ObjectID
        // Check if the values match
        // If yes then send an acknowledgement
        boolean isFine = true;

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(MESSAGE_NAME.toString(), DPNMsgTypes.REPLICATION_VERIFICATION_REPLY);
        if(isFine)
            bodyMap.put(MESSAGE_ATT.toString(), DPNMsgAction.ACK);
        else
            bodyMap.put(MESSAGE_ATT.toString(), DPNMsgAction.NACK);
        bodyMap.put(LOCATION.toString(),"SDA");
        DPNMsgBody DPNMsgBody = new DPNMsgBody();
        DPNMsgBody.setBody(bodyMap);
        msg.setBody(DPNMsgBody);
        return msg;
    }
}
