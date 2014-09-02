package edu.iu.dpn.messaging;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Date;

import static edu.iu.dpn.messaging.DPNMsgConstants.*;

/**
 * Created with IntelliJ IDEA.
 * User: Aravindh
 * Date: 04/06/2014
 * Time: 18:41
 * To change this template use File | Settings | File Templates.
 */
public class DPNSendMessageTest {
    public static void main (String[] args){
//        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
//        DPNMsg msg = new DPNMsg();
//        Map<String, Object> headerMap = new HashMap<>();
//        headerMap.put(FROM.toString(), "IU");
//        headerMap.put(REPLY_KEY.toString(), "dpn.iu");
//        headerMap.put(CORRELATION_ID.toString(), UUID.randomUUID().toString());
//        headerMap.put(DATE.toString(), today.format(new Date()).toString());
//        headerMap.put(TTL.toString(), String.valueOf(566));
//        DPNMsgHeader DPNMsgHeader = new DPNMsgHeader();
//        DPNMsgHeader.setHeader(headerMap);
//        msg.setHeader(DPNMsgHeader);
//
//        Map<String, Object> bodyMap = new HashMap<>();
//        bodyMap.put(MESSAGE_NAME.toString(), DPNMsgTypes.REPLICATION_INIT_QUERY);
//        bodyMap.put(REPLICATION_SIZE.toString(), "4096");
//        bodyMap.put(PROTOCOL.toString(), "rsync");
//        bodyMap.put(DPN_OBJECT_ID.toString(), "45b6-c3869-4869-jgu3");
//        DPNMsgBody DPNMsgBody = new DPNMsgBody();
//        DPNMsgBody.setBody(bodyMap);
//        msg.setBody(DPNMsgBody);

        DPNReplicationInitQuery initQuery = new DPNReplicationInitQuery();
        initQuery.setReplication_size("4096");
        initQuery.setDpn_object_id("45b6-c3869-4869-jgu3");
        DPNMsg msg = initQuery.getDPNReplicationInitMsg();

        try{
            msg.send(msg,null);
        }catch(Exception e){
            e.printStackTrace();
        }

//        DPNReplicationLocationReply replicationLocationReply = new DPNReplicationLocationReply();
//        msg = replicationLocationReply.getDPNReplicationLocationMsg();
//
//        try{
//            msg.send(msg,null);
//        }catch(Exception e){
//            e.printStackTrace();
//        }

    }
}
