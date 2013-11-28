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

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * S3 upload, download, bucket creation methods
 */
public class AmazonS3 {

    private S3Service s3Service;


    private final static Logger logger = Logger.getLogger(AmazonS3.class.getName());

    public AmazonS3(String awsAccessKey, String awsSecretKey) throws Exception {
        AWSCredentials awsCredentials =
                new AWSCredentials(awsAccessKey, awsSecretKey);
        try {
            s3Service = new RestS3Service(awsCredentials);
        } catch (S3ServiceException e) {
            throw new Exception(e.getMessage());
        }
    }

    public void upload(String sourceFile, String targetFile,String targetBucket){

        S3Object fileObject = new S3Object(targetFile);

        FileInputStream stream = null;
        try {
            stream = new FileInputStream(new File(sourceFile));
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }

        fileObject.setDataInputStream(stream);
       //upload
       try {
                S3Object result = s3Service.putObject(targetBucket, fileObject);
            } catch (S3ServiceException e) {
                    logger.log(Level.SEVERE, e.getMessage());
            }

      }

        public void download(String sourceFile, String sourceBucket, String targetFile){
            //download
            S3Object objectComplete = null;
            try {
                objectComplete = s3Service.getObject(sourceBucket, sourceFile);

                logger.log(Level.INFO, "S3Object, complete: " + objectComplete);

                OutputStream out = new FileOutputStream(new File(targetFile));

                int read = 0;
                byte[] bytes = new byte[1024];

                InputStream inputStream = objectComplete.getDataInputStream();
                while ((read = inputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }

                inputStream.close();
                out.flush();
                out.close();
            } catch (S3ServiceException e) {
                logger.log(Level.SEVERE, e.getMessage());
            } catch (ServiceException e) {
                logger.log(Level.SEVERE, e.getMessage());
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }


        public void createBucket(String bucketName){
            try {
                S3Bucket usWestBucket = s3Service.createBucket(bucketName, S3Bucket.LOCATION_US_WEST);

            } catch (S3ServiceException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }
