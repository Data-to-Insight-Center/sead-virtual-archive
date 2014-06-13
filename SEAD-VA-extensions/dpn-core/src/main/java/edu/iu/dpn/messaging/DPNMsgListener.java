package edu.iu.dpn.messaging;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import java.io.IOException;
import java.util.Map;

public class DPNMsgListener implements MessageListener{
    public void onMessage(Message msg){
        MessageProperties properties = msg.getMessageProperties();
        byte[] body = msg.getBody();
        DPNMsg message = null;

        if(null == properties){
            throw new IllegalArgumentException("Message Properties are null");
        }

        if(null == properties.getHeaders() || properties.getHeaders().isEmpty()){
            throw new IllegalArgumentException("Message Headers are empty");
        }

        System.out.println("Header Values are: "+properties.getHeaders().toString());

        Map<String, Object> headerMap = properties.getHeaders();
//        for (Map.Entry<String, Object> entry : headerMap.entrySet())
//        {
//            System.out.println(entry.getKey() + "/" + entry.getValue());
//        }
        String reply_key = (String) headerMap.get("reply_key");
        String cid = (String) headerMap.get("correlation_id");
        String sequence = (String) headerMap.get("sequence");
        System.out.println("Correlation ID is: "+cid);


        DPNMsgBody msgBody = getDPNMessageBody(body);
        DPNMsg dpnMsg = new DPNMsg();
        //dpnMsg.setBody(msgBody);

        String msg_name = (String) msgBody.getBody().get("message_name");
        System.out.print("From the Listener: "+msg_name);
        switch (msg_name){
            case "REPLICATION_INIT_QUERY":
                DPNReplicationAvailableReply replicationAvailableReply = new DPNReplicationAvailableReply();
                dpnMsg= replicationAvailableReply.getDPNReplicationLocationMsg(cid);
                try{
                    dpnMsg.send(dpnMsg,null);
                }catch(Exception e){
                    e.printStackTrace();
                }
            case "REPLICATION_AVAIL_REPLY":
                DPNReplicationLocationReply replicationLocationReply = new DPNReplicationLocationReply();
                dpnMsg = replicationLocationReply.getDPNReplicationLocationMsg(cid, reply_key);
                try{
                    dpnMsg.send(dpnMsg,null);
                }catch(Exception e){
                    e.printStackTrace();
                }
            default:
                throw new IllegalArgumentException("Invalid message type");
        }

        //System.out.println(dpnMsg.bodyToJson());
    }

    private DPNMsgBody getDPNMessageBody(byte[] body){
        if(body == null){
            throw new IllegalArgumentException("Message Body is null");
        }

        String json = new String(body);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
                DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);

        try {
            return mapper.readValue(json, DPNMsgBody.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
