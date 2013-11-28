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

package org.dataconservancy.archive.impl.cloud;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.seadva.archive.SeadArchiveStore;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadFile;
import org.seadva.model.builder.api.SeadModelBuilder;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * S3 implementation for archiving data from SEAD VA
 */
public class AmazonS3ArchiveStore implements SeadArchiveStore {


    SeadXstreamStaxModelBuilder model = new SeadXstreamStaxModelBuilder();
    private final static Logger logger = Logger.getLogger(AmazonS3.class.getName());

    AmazonS3 s3;
    public AmazonS3ArchiveStore(){

    }
    public AmazonS3ArchiveStore(String awsAccessKey, String awsSecretKey) throws Exception {
          s3 = new AmazonS3(awsAccessKey,awsSecretKey);
    }
    @Override
    public Iterator<String> listEntities(EntityType type) {
        return null;  //Todo
    }

    @Override
    public InputStream getContent(String entityId) throws EntityNotFoundException, EntityTypeException {
        return null;  //Todo
    }

    @Override
    public InputStream getPackage(String entityId) throws EntityNotFoundException {
        return null;  //Todo
    }

    @Override
    public InputStream getFullPackage(String entityId) throws EntityNotFoundException {
        return null;  //Todo
    }

    @Override
    public void putPackage(InputStream dcpStream) throws AIPFormatException {
    }

    @Override
    public ResearchObject putResearchPackage(InputStream dcpStream) throws AIPFormatException {

        ResearchObject pkg = null;
        try {
            pkg = model.buildSip(dcpStream);
        } catch (InvalidXmlException e) {
            throw new AIPFormatException(e.getMessage());
        }

        String bucketName=null;
        Collection<DcsDeliverableUnit> dus = pkg.getDeliverableUnits();
        for(DcsDeliverableUnit du:dus){
            if(du.getParents()!=null)
            {
                if(du.getParents().size()==0){
                    bucketName = du.getTitle().toLowerCase().replace("_","").replace(" ","")+"d2i";
                    s3.createBucket(bucketName);
                    SeadDataLocation cloudPath = new SeadDataLocation();
                    cloudPath.setType("cloud");
                    cloudPath.setLocation(
                            bucketName);
                    cloudPath.setName("Amazon S3");
                    ((SeadDeliverableUnit)du).addSecondaryDataLocation(cloudPath);
                }
            }
            else{
                bucketName = du.getTitle().toLowerCase().replace("_","").replace(" ","")+"d2i";
                s3.createBucket(bucketName);
                SeadDataLocation cloudPath = new SeadDataLocation();
                cloudPath.setType("cloud");
                cloudPath.setLocation(
                        bucketName);
                cloudPath.setName("Amazon S3");
                ((SeadDeliverableUnit)du).addSecondaryDataLocation(cloudPath);
            }
        }

        pkg.setDeliverableUnits(dus);
        Collection<DcsFile> files = pkg.getFiles();
        for(DcsFile file: files){

                //upload
            try {

                s3.upload(file.getSource().replace("file://", ""),file.getName(),bucketName);
                SeadDataLocation cloudPath = new SeadDataLocation();
                cloudPath.setType("cloud");
                cloudPath.setLocation(
                        bucketName+"/"+file.getName());
                cloudPath.setName("Amazon S3");
                ((SeadFile)file).addSecondaryDataLocation(cloudPath);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }

        }
        pkg.setFiles(files);
        return pkg;
    }

}
