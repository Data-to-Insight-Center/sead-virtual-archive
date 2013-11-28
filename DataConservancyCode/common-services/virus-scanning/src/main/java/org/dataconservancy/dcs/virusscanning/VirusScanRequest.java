/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.virusscanning;

import java.io.InputStream;

/**
 * Class to encapsulate a file and create a request to pass to the
 * {@link VirusScanManager}.
 * <p>
 * A VirusScanRequest should be created for each file that is to be scanned by
 * the {@link VirusScanManager}. The file information should be passed as input
 * stream that will be exhausted by the manager.
 * </p>
 */
public class VirusScanRequest {

    private final String fileName;

    private final InputStream fileData;

    private final String id;

    /**
     * Constructor to create a VirusScanRequest.
     * 
     * @param fileName
     *        The name of the file to be scanned.
     * @param fileData
     *        An input stream containing the file information. Note this stream
     *        will be exhausted by the manager when a scan is requested.
     * @param uniqueId
     *        A uniqueId to identify the request.
     */
    public VirusScanRequest(String fileName, InputStream fileData, String uniqueId) {
        this.fileName = fileName;
        this.fileData = fileData;
        id = uniqueId;
    }

    /**
     * Gets the name of the file to be scanned.
     * 
     * @return The name of the file to be scanned.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the file data from the request.
     * 
     * @return An input stream representing the file data. If this file has been
     *         scanned the stream will have been exhausted.
     */
    public InputStream getFileData() {
        return fileData;
    }

    /**
     * Gets the unique id that was set by the client to identify the request.
     * 
     * @return The unique id set by the client.
     */
    public String getId() {
        return id;
    }

}
