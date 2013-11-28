/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seadva.archive.impl.cloud;

import com.jcraft.jsch.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 *Sftp session creator and  data uploader and downloader
 */
public class Sftp {

    JSch jsch;
    String host;
    String username;
    String password;
    String mountPath;
    static Session session;
    static Channel channel;
    static ChannelSftp sftpChannel;
    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(Sftp.class);


    public Sftp(String host, String username, String password, String mountPath) throws JSchException {
        this.jsch = new JSch();
        this.host = host;
        this.username = username;
        this.password = password;
        this.mountPath = mountPath;
        String knownHostsFilename = "~/.ssh/known_hosts";
        jsch.setKnownHosts( knownHostsFilename );

        session = jsch.getSession(this.username, this.host );

        UserInfo ui = new SdaUserInfo();
        ui.promptPassword(this.password);
        session.setUserInfo(ui);
        session.setPassword(this.password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking","no");
        session.setConfig(config);
        session.connect();
        channel = session.openChannel( "sftp" );
        channel.connect();
        sftpChannel = (ChannelSftp) channel;
        log.debug("Sftp session and channel created");
    }

    public void disConnectSession(){
        sftpChannel.exit();
        session.disconnect();
        log.debug("Sftp session and channel ended");
    }
    public void createDirectory(String directoryName){
        log.debug("createDirectory "+directoryName);

        try {
            sftpChannel.stat(directoryName);
        } catch (SftpException dirNonExistant) {
            int tryNum = 1;

            while(true){
                try {
                    sftpChannel.mkdir(directoryName);
                } catch (SftpException e) {
                    if(tryNum>3){
                        e.printStackTrace();
                        break;
                    }
                    else{
                        tryNum++;
                        continue;
                    }
                }
                break;
            }

        }
    }

    public void deleteDirectory(String directoryPath){
        log.debug("delete Directory "+directoryPath);

        int tryNum = 1;

        while(true){
            try {
               sftpChannel.rmdir(directoryPath);
            } catch (SftpException e) {
                if(tryNum>3){
                    log.error("Error Message ="+e.getMessage());
                    break;
                }
                else{
                    tryNum++;
                    continue;
                }
            }
            break;
        }
    }

    public void deleteFile(String filePath){
        log.debug("delete File "+filePath);

        int tryNum = 1;

        while(true){
            try {
                sftpChannel.rm(filePath);
            } catch (SftpException e) {
                if(tryNum>3){
                    log.error("Message="+e.toString());
                    break;
                }
                else{
                    tryNum++;
                    continue;
                }
            }
            break;
        }
    }

    public void uploadFile(String source, String destination,boolean useMount) {

        log.debug("uploadFile "+source+" "+destination);
        int tryNum = 1;

        if(!useMount){
            while(true){
                try {

                    sftpChannel.put(source, destination);

                } catch (SftpException e) {
                    if(tryNum>3){
                        e.printStackTrace();
                        break;
                    }
                    else{
                        tryNum++;
                        continue;
                    }
                }
                break;
            }
        }
        else {
            while(true){
                try {
                    FileUtils.copyFile(new File(source), new File(this.mountPath+destination));
                } catch (IOException e) {
                    if(tryNum>3){
                        e.printStackTrace();
                        break;
                    }
                    else{
                        tryNum++;
                        continue;
                    }
                }
                break;
            }

        }




    }

   public void downloadFile(String source,String file, String destination) throws JSchException, SftpException {

       log.debug("download "+source+" "+destination);
       int tryNum = 1;
       while(true){
           try {
               sftpChannel.cd(source);
               sftpChannel.get(file, destination);
           } catch (SftpException e) {
               if(tryNum>3){
                   e.printStackTrace();
                   break;
               }
               else{
                   tryNum++;
                   continue;
               }
           }
           break;
       }

    }

    public void downloadFile(String source,String file, OutputStream destinationStream) throws JSchException, SftpException {

        log.debug("download "+source+"/"+file);
        if(channel.isConnected())
            log.debug("true");
        else
            log.debug("false");
        int tryNum = 1;
        while(true){
            try {
                sftpChannel.cd(source);
                sftpChannel.get(file, destinationStream);
            } catch (SftpException e) {
                if(tryNum>3){
                    e.printStackTrace();
                    break;
                }
                else{
                    tryNum++;
                    continue;
                }
            }
            break;
        }

    }



    public InputStream downloadFile(String source,String file) throws JSchException, SftpException {

        try {
            return new FileInputStream(new File(this.mountPath+source+file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
      /*

    public void ls(String directory) throws JSchException, SftpException {

//        Session session = getSession();
//        session.connect();

        Channel channel = session.openChannel( "sftp" );
        channel.connect();

        ChannelSftp sftpChannel = (ChannelSftp) channel;

        Vector<ChannelSftp.LsEntry> list = sftpChannel.ls(directory);
        for(ChannelSftp.LsEntry entry:list){
            sftpChannel.get(entry.getFilename());
        }
        System.out.print(list);
        sftpChannel.exit();
//        session.disconnect();
    }     */
}
