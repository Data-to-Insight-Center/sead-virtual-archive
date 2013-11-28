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

package org.dataconservancy.dcs.id.impl;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.dataconservancy.dcs.id.api.ExtendedIdService;
import org.dataconservancy.dcs.id.api.IdMetadata;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

/**
 * Service to create and update Datacite DOIs
 */
public class DataciteIdService implements ExtendedIdService {



    private String username;
    private String password;
    private String service;


    public DataciteIdService() {}

    @Override
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password =  password;
    }



    @Override
    public void setService(String service) {
        this.service = service;
    }

    @Override
    public Identifier createwithMd(Map metadata, boolean update) throws IdentifierNotFoundException, IOException {

         String metadataFile = writeMetadata(metadata,update);

        String cmd =
                "curl -u "+ username +":"+ password +" -X POST -H \"Content-Type:text/plain\" --data-binary @"+
                        metadataFile + " "+service;


        ByteArrayOutputStream stdout = executeCommand(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(stdout.toByteArray())));

        String output_line = null;

        String doiUrl = null;
        while((output_line = br.readLine()) != null)
        {

            if(output_line.contains("doi"))
            {

                doiUrl = output_line;
            }


        }

        DOI doi = new DOI();
        doi.setMetadata(metadata);
        doi.setTargetUrl((String)metadata.get(IdMetadata.Metadata.TARGET));
        doi.setUid(doiUrl);

        return doi;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Identifier create(String type) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Identifier fromUid(String uid) throws IdentifierNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Identifier fromUrl(URL url) throws IdentifierNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private String writeMetadata(Map metadata, boolean update) throws IOException
    {
        String guid = UUID.randomUUID().toString();
        String tempFile = System.getProperty("java.io.tmpdir")+"/"+guid+"metadata.txt";

        File file = new File(tempFile);
        if(!file.exists())
            file.createNewFile();

        PrintWriter pw = new PrintWriter(tempFile);

        if(metadata.containsKey(IdMetadata.Metadata.TARGET))
            pw.println("_target: " + metadata.get(IdMetadata.Metadata.TARGET));

        if(metadata.containsKey(IdMetadata.Metadata.TITLE))
            pw.println("datacite.title: " + metadata.get(IdMetadata.Metadata.TITLE));
        else if(!update)
            pw.println("datacite.title: " +"(:unav)");


        if(metadata.containsKey(IdMetadata.Metadata.CREATOR))
            pw.println("datacite.creator: " + metadata.get(IdMetadata.Metadata.CREATOR));
        else if(!update)
            pw.println("datacite.creator: " +"(:unav)");


        if(metadata.containsKey(IdMetadata.Metadata.PUBDATE))
            pw.println("datacite.publicationyear: " + metadata.get(IdMetadata.Metadata.PUBDATE));
        else if(!update)
            pw.println("datacite.publicationyear: " + "(:unav)");

        if(metadata.containsKey(IdMetadata.Metadata.PUBLISHER))
            pw.println("datacite.publisher: " + metadata.get(IdMetadata.Metadata.PUBLISHER));
        else if(!update)
            pw.println("datacite.publisher: " + "(:unav)");

        pw.flush();
        pw.close();

        return tempFile;

    }

    ByteArrayOutputStream executeCommand(String command) throws IOException {
        CommandLine cmdLine = CommandLine.parse(command);
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        PumpStreamHandler psh = new PumpStreamHandler(stdout);

        executor.setStreamHandler(psh);

        int exitValue = executor.execute(cmdLine);
        return  stdout;
    }
}


