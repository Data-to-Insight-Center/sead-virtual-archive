package edu.iu.dpn.messaging;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DPNMsgConsumer {
    public static String readLine(){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException("Exception while waiting for input ....");
        }
    }
    public static void main(String[] args){
        System.out.println("I am DPNMsgConsumer");
        boolean quit = false;
        //new ClassPathXmlApplicationContext("classpath:/application-context.xml",DPNMsgConsumer.class);
        //new GenericXmlApplicationContext("classpath:/application-application-context.xml");
        //new ClassPathXmlApplicationContext("application-context.xml");
        new ClassPathXmlApplicationContext("rabbit-listener-context.xml");
        while(!quit){
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){}
        }
        System.out.println("'q' to exit: ");
        if("q".equalsIgnoreCase(readLine())){
            System.out.println("Stopping the Listener..");
            quit = true;
        }
    }
}
