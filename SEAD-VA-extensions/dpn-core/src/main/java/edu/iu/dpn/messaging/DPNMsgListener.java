package edu.iu.dpn.messaging;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.seadva.registry.client.RegistryClient;
import org.seadva.registry.database.model.obj.vaRegistry.BaseEntity;
import org.seadva.registry.database.model.obj.vaRegistry.DataIdentifier;
import org.seadva.registry.database.model.obj.vaRegistry.Property;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import edu.iu.dpn.util.RsyncDownload;
import org.seadva.registry.service.util.QueryAttributeType;
import org.springframework.beans.factory.annotation.Required;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.*;
import org.perf4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.rmi.runtime.Log;


public class DPNMsgListener implements ChannelAwareMessageListener{
    RegistryClient client;
    String TarFileLocation;
    String internalTest;

//    @Required
//    public void setInternalTest(String internalTest){
//        this.internalTest = internalTest;
//        //System.out.println("broadCastKey in setBroadCastKey is "+broadCastKey);
//    }
//
//    public String getInternalTest(){
//        return internalTest;
//    }

    public void onMessage(Message msg, Channel channel) throws Exception {
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
        //logService.writeLog(("Header : "+properties.getHeaders().toString()));
        Map<String, Object> headerMap = properties.getHeaders();
        String reply_key = (String) headerMap.get("reply_key");
        String cid = (String) headerMap.get("correlation_id");
        //String sequence = (String) headerMap.get("sequence");
        String sequence = String.valueOf(headerMap.get("sequence"));

        String json = new String(body);

        System.out.println("Message body is: "+json);
        //logService.writeLog("Body: "+json);
        DPNMsgBody msgBody;
        msgBody = getDPNMessageBody(body);
        DPNMsg dpnMsg = new DPNMsg();
        //dpnMsg.setBody(msgBody);
        //System.out.println("Body Values are: "+msgBody.getBody().toString());

        String msg_name = (String) msgBody.getBody().get("message_name");
        System.out.println("From the Listener: " + msg_name);
        Logger log = LoggerFactory.getLogger(DPNMsgListener.class);
        switch (msg_name){
            case "replication-init-query":
                long initStart = System.currentTimeMillis();
                DPNReplicationAvailableReply replicationAvailableReply = new DPNReplicationAvailableReply();
                client = new RegistryClient("http://localhost:8080/registry/rest");
                try{
                    List<BaseEntity> entityList = client.queryByProperty("CorrelationID",cid,QueryAttributeType.PROPERTY);
                    for(BaseEntity entity:entityList){

                        Iterator props = entity.getProperties().iterator();
                        Set<Property> updatesProperties = new HashSet<Property>();
                        while (props.hasNext()){
                            Property property = (Property) props.next();
                            //System.out.println(property.getMetadata().getMetadataElement().toString());
                            //System.out.println(property.getValuestr());
                            if(property.getMetadata().getMetadataElement().contains("MsgStatus")){
                                property.setValuestr("AVAILABLE");
                                //System.out.println("UPDATED - INIT  : "+property.getValuestr());
                            }
                            updatesProperties.add(property);
                            props.remove();
                        }
                        entity.setProperties(updatesProperties);
                        client.postEntity(entity);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                dpnMsg= replicationAvailableReply.getDPNReplicationAvailableMsg(cid);
                //System.out.println("REPLICATION_INIT_QUERY: "+cid);
                try{
                    dpnMsg.send(dpnMsg,null);
                    channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                }catch(Exception e){
                    e.printStackTrace();
                }
                System.out.println("ExecutorBootstrap - Ingest "+String.valueOf(System.currentTimeMillis() - initStart));
                log.debug("ExecutorBootstrap - Ingest execution: {} ms",String.valueOf(System.currentTimeMillis() - initStart));
                break;
            case "replication-available-reply":
                //
                long availReplyStart = System.currentTimeMillis();
                client = new RegistryClient("http://localhost:8080/registry/rest");

                String location;
                try{
                    List<BaseEntity> entityList = client.queryByProperty("CorrelationID",cid,QueryAttributeType.PROPERTY);
                    for(BaseEntity entity:entityList){

                        Iterator props = entity.getProperties().iterator();
                        Set<Property> updatesProperties = new HashSet<Property>();
                        while (props.hasNext()){
                            Property property = (Property) props.next();
                            if(property.getMetadata().getMetadataElement().contains("TarFileLocation"))
                                TarFileLocation = property.getValuestr();
                            }
                            props.remove();
                        }
                }catch (Exception e){
                    e.printStackTrace();
                }

                location = "dpntestuser@dpntest.d2i.indiana.edu:"+TarFileLocation;
                System.out.println("Tar File Location: "+location);

                DPNReplicationLocationReply replicationLocationReply = new DPNReplicationLocationReply();
                dpnMsg = replicationLocationReply.getDPNReplicationLocationMsg(cid, reply_key, location);
                //System.out.println("REPLICATION_LOCATION_QUERY: "+cid);
                try{
                    dpnMsg.send(dpnMsg,null);
                    // Update the Database of the type of Message Sent
                    try{
                        List<BaseEntity> entityList = client.queryByProperty("CorrelationID",cid,QueryAttributeType.PROPERTY);
                        for(BaseEntity entity:entityList){

                            Iterator props = entity.getProperties().iterator();
                            Set<Property> updatesProperties = new HashSet<Property>();
                            while (props.hasNext()){
                                Property property = (Property) props.next();
                                //System.out.println(property.getMetadata().getMetadataElement().toString());
                                //System.out.println(property.getValuestr());
                                if(property.getMetadata().getMetadataElement().contains("TarFileLocation"))
                                    TarFileLocation = property.getValuestr();
                                if(property.getMetadata().getMetadataElement().contains("MsgStatus")){
                                    property.setValuestr("LOCATION");
                                    //System.out.println("UPDATED - AVAILABLE: "+property.getValuestr());
                                }
                                updatesProperties.add(property);
                                props.remove();
                            }
                            entity.setProperties(updatesProperties);
                            client.postEntity(entity);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                }catch(Exception e){
                    e.printStackTrace();
                }
                System.out.println("ExecutorBootstrap - Ingest "+String.valueOf(System.currentTimeMillis() - availReplyStart));
                log.debug("ExecutorBootstrap - Ingest execution: {} ms",String.valueOf(System.currentTimeMillis() - availReplyStart));
                break;
            case "replication-location-reply":
                //
                long locReplyStart = System.currentTimeMillis();
                String tarFileLocation = (String) msgBody.getBody().get("location");
                //System.out.println("tarFileLocation in DPNMsgListener:"+tarFileLocation);
                RsyncDownload rsyncDownload = new RsyncDownload("dpnuser");
                String downloadedFilePath = rsyncDownload.downloadFile(tarFileLocation,"dpntest.d2i.indiana.edu","/tmp");
                //System.out.println("downloadedFilePath: "+downloadedFilePath);
                String checksum = generateCheckSum(downloadedFilePath,"SHA1");
                //System.out.println("RsyncCopyFile checksum:"+checksum);
                try{
                    channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                }catch(Exception e){
                    e.printStackTrace();
                }
//                client = new RegistryClient("http://localhost:8080/registry/rest");
//                try{
//                    List<BaseEntity> entityList = client.queryByProperty("CorrelationID",cid);
//                    for(BaseEntity entity:entityList){
//
//                        Iterator props = entity.getProperties().iterator();
//                        Set<Property> updatesProperties = new HashSet<Property>();
//                        while (props.hasNext()){
//                            Property property = (Property) props.next();
//                            System.out.println(property.getMetadata().getMetadataElement().toString());
//                            System.out.println(property.getValuestr());
//                            if(property.getMetadata().getMetadataElement().contains("MsgStatus")){
//                                property.setValuestr("REPLICATION_TRANSFER_REPLY");
//                                System.out.println("UPDATED - AVAILABLE: "+property.getValuestr());
//                            }
//                            updatesProperties.add(property);
//                            props.remove();
//                        }
//                        entity.setProperties(updatesProperties);
//                        client.postEntity(entity);
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
                System.out.println("ExecutorBootstrap - Ingest "+String.valueOf(System.currentTimeMillis() - locReplyStart));
                log.debug("ExecutorBootstrap - Ingest execution: {} ms",String.valueOf(System.currentTimeMillis() - locReplyStart));
                break;
            case "replication-transfer-reply":
                long transReplyStart = System.currentTimeMillis();
                Object var;
                String repFixityValue = null;
                boolean fixityStatus = false;
                String remoteFixityValue;
                var = msgBody.getBody().get("fixity_value");
                remoteFixityValue = String.valueOf(var);
                //String remoteFixityValue = (String) msgBody.getBody().get("fixity_value");
                client = new RegistryClient("http://localhost:8080/registry/rest");
                try {
                    List<BaseEntity> entityList = client.queryByProperty("CorrelationID", cid, QueryAttributeType.PROPERTY);
                    //View returned Alternate ID of the retrieved collection, whose abstract property was matched
                    Iterator returnedIds = entityList.get(0).getDataIdentifiers().iterator();
                    while (returnedIds.hasNext()) {
                        DataIdentifier returnedId = (DataIdentifier) returnedIds.next();
                        if(returnedId.getId().getDataIdentifierType().getDataIdentifierTypeName().contains("fixity-sha1")){
                            repFixityValue = returnedId.getDataIdentifierValue();
                            System.out.println("Identifier value:" + returnedId.getDataIdentifierValue());
                            System.out.println("Identifier type:" + returnedId.getId().getDataIdentifierType().getDataIdentifierTypeName());
                            break;
                        }else
                        returnedIds.remove();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                if(repFixityValue != null) {
                    if (repFixityValue.equals(remoteFixityValue)) {
                        fixityStatus = true;
                    }
                    DPNReplicationVerificationReply verificationReply = new DPNReplicationVerificationReply();
                    dpnMsg = verificationReply.getDPNReplicationVerificationReplyMsg(cid, reply_key, fixityStatus);
                    //System.out.println("REPLICATION_LOCATION_QUERY: "+cid);
                    try {
                        dpnMsg.send(dpnMsg, null);
                        try {
                            List<BaseEntity> entityList = client.queryByProperty("CorrelationID", cid, QueryAttributeType.PROPERTY);
                            for (BaseEntity entity : entityList) {

                                Iterator props = entity.getProperties().iterator();
                                Set<Property> updatesProperties = new HashSet<Property>();
                                while (props.hasNext()) {
                                    Property property = (Property) props.next();
                                    //System.out.println(property.getMetadata().getMetadataElement().toString());
                                    //System.out.println(property.getValuestr());
                                    if (property.getMetadata().getMetadataElement().contains("MsgStatus")) {
                                        if(fixityStatus)
                                            property.setValuestr("VERIFY-PASS");
                                        else
                                            property.setValuestr("VERIFY-FAIL");
                                        //System.out.println("UPDATED - AVAILABLE: "+property.getValuestr());
                                    }
                                    updatesProperties.add(property);
                                    props.remove();
                                }
                                entity.setProperties(updatesProperties);
                                client.postEntity(entity);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
                System.out.println("ExecutorBootstrap - Ingest "+String.valueOf(System.currentTimeMillis() - transReplyStart));
                log.debug("ExecutorBootstrap - Ingest execution: {} ms",String.valueOf(System.currentTimeMillis() - transReplyStart));
                break;
            case "":
            default:
                //throw new IllegalArgumentException("Invalid message type");
                System.out.println("Un-Supported Message Type: " + msg_name);
                //System.exit(-1);
        }

        //System.out.println(dpnMsg.bodyToJson());
    }

    private DPNMsgBody getDPNMessageBody(byte[] body) {
        if (body == null) {
            throw new IllegalArgumentException("Message Body is null");
        }

        String json = new String(body);
        //System.out.println("getDPNMessageBody: " + json);
        ObjectMapper mapper = new ObjectMapper();
        try{
            Map<String, Object> mapObject = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            DPNMsgBody obj = new DPNMsgBody();
            obj.setBody(mapObject);
            return obj;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private String generateCheckSum(String tarFilePath, String algorithm) throws Exception{
        //System.out.println("generateCheckSum");
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        FileInputStream fileInputStream = new FileInputStream(tarFilePath);
        byte[] dataBytes = new byte[1024];
        int bytesRead = 0;
        while((bytesRead = fileInputStream.read(dataBytes)) != -1){
            messageDigest.update(dataBytes,0,bytesRead);
        }
        byte[] digestBytes = messageDigest.digest();
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < digestBytes.length; i++) {
            sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        fileInputStream.close();
        return sb.toString();

    }
}
