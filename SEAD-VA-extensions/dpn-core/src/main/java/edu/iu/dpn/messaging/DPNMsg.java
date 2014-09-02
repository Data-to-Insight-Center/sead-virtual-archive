/**
 * This class is used to build the DPN Message. It used two other classes:
 * DPNMsgHeader used to form the header and DPNMsgBody used to form the body
 * of the message.
 */

package edu.iu.dpn.messaging;

import java.util.Map;
import com.google.gson.*;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DPNMsg {

    protected DPNMsgHeader DPNMsgHeader;
    protected DPNMsgBody DPNMsgBody;

    private String from;
    private String reply_key;
    private String correlation_id;
    private String sequence;
    private String date;
    private String ttl;

    public void setSequence(String sequence){
        this.sequence = sequence;
    }

    public String getSequence(){
        return sequence;
    }

    public void setFrom(String from){
        this.from = from;
    }

    public void setReply_key(String reply_key){
        this.reply_key = reply_key;
    }

    public void setCorrelation_id(String correlation_id){
        this.correlation_id = correlation_id;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getFrom(){
        return from;
    }

    public String getReply_key(){
        return reply_key;
    }

    public String getCorrelation_id(){
        return correlation_id;
    }

    public String getDate(){
        return date;
    }

    public void setTtl(int ttl){
        this.ttl = String.valueOf(ttl);
    }

    public String getTtl(){
        return ttl;
    }

    public void setHeader(DPNMsgHeader header){
        this.DPNMsgHeader = header;
    }

    public DPNMsgHeader getHeader(){
        return DPNMsgHeader;
    }

    public void setBody(DPNMsgBody body){
        this.DPNMsgBody = new DPNMsgBody(body);
    }

    public DPNMsgBody getBody(){
        return DPNMsgBody;
    }

    public void send(DPNMsg msg, String routingKey) throws Exception{
        AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("rabbit-sender-context.xml");
        RabbitTemplate template = ctx.getBean(RabbitTemplate.class);
        //Set-up the message properties required for Rabbit
        MessageProperties properties = new MessageProperties();
        properties.setContentType("application/json");
//        if (routingKey == null){
//            throw new Exception("routingKey is null");
//        }

        DPNMsgHeader header = msg.getHeader();
        for ( Map.Entry<String, Object> entry : header.getHeader().entrySet() ) {
            properties.setHeader(entry.getKey(),entry.getValue());
        }

        //Message dpnMessage = new Message(msg.bodyToJson().getBytes(),properties);
        Message dpnMessage = new Message(msg.bodyToJson().getBytes(),properties);
        template.send(dpnMessage);
        ctx.destroy();
    }

    public String headerToJson(){
        Gson gson = new GsonBuilder().create();
        String bJson = gson.toJson(DPNMsgHeader);
        return bJson;
    }
    public String bodyToJson(){
        Gson gson = new GsonBuilder().create();
        String bJson = gson.toJson(DPNMsgBody.getBody());
        //System.out.println("bodyToJson: "+bJson);
        return bJson;
    }

    public String toString(){
        try{
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            for (Map.Entry<String, Object> entry : DPNMsgHeader.getHeader().entrySet()) {
                sb.append("\t").append(entry.getKey()).append(": ")
                        .append(entry.getValue()).append(",\n");
            }
            sb.append("}");
            sb.append("{\n");
            for (Map.Entry<String, Object> entry : DPNMsgBody.getBody().entrySet()) {
                sb.append("\t").append(entry.getKey()).append(": ")
                        .append(entry.getValue()).append(",\n");
            }
            sb.append("}");
            return sb.toString();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
