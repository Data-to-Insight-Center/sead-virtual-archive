package edu.iu.dpn.messaging;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: Aravindh
 * Date: 16/06/2014
 * Time: 23:18
 * To change this template use File | Settings | File Templates.
 */
public class DPNMsgConsumer {
    public static void main(String[] args){
        new ClassPathXmlApplicationContext("classpath:/context.xml",DPNMsgConsumer.class);
    }
}
