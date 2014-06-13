/**
 * Description: The class is used to generate the ReplicationInitQuery Message.
 * Input: replication_size, dpn_object_id
 * Return: DPNMsg
 */

package edu.iu.dpn.messaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static edu.iu.dpn.messaging.DPNMsgConstants.*;
import static edu.iu.dpn.messaging.DPNMsgConstants.DPN_OBJECT_ID;
import static edu.iu.dpn.messaging.DPNMsgConstants.PROTOCOL;

public class DPNReplicationInitQuery {
    DPNReplicationInitQuery(){}
    public DPNMsg getDPNReplicationInitMsg(){
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        DPNMsg msg = new DPNMsg();
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(FROM.toString(), DPNMsgConstants.FROM_VALUE);
        headerMap.put(REPLY_KEY.toString(), DPNMsgConstants.REPLY_KEY_VALUE);
        headerMap.put(SEQUENCE.toString(),DPNMsgConstants.REP_INT_QUERY_SEQUENCE);
        headerMap.put(CORRELATION_ID.toString(), UUID.randomUUID().toString());
        headerMap.put(DATE.toString(), today.format(new Date()).toString());
        headerMap.put(TTL.toString(),DPNMsgConstants.TTL_DURATION);
        DPNMsgHeader DPNMsgHeader = new DPNMsgHeader();
        DPNMsgHeader.setHeader(headerMap);
        msg.setHeader(DPNMsgHeader);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(MESSAGE_NAME.toString(), DPNMsgTypes.REPLICATION_INIT_QUERY);
        bodyMap.put(REPLICATION_SIZE.toString(), "4096");
        bodyMap.put(PROTOCOL.toString(), DPNMsgConstants.PROTOCOL_TYPE);
        bodyMap.put(DPN_OBJECT_ID.toString(), "45b6-c3869-4869-jgu3");
        DPNMsgBody DPNMsgBody = new DPNMsgBody();
        DPNMsgBody.setBody(bodyMap);
        msg.setBody(DPNMsgBody);
        return msg;
    }
}
