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

import java.io.IOException;
import java.io.InputStream;

import java.net.SocketException;

/**
 * Interface used by the {@link VirusScanManager} to send files to be scanned by
 * 3rd party Virus Scanning products.
 * <p>
 * Each 3rd party program will need to have it's own Virus Scanner
 * implementation.
 * </p>
 */
public interface VirusScanner {

    /**
     * Call to establish connection with the 3rd party virus software located on
     * an external server.
     * 
     * @return True if connection was established false otherwise
     * @throws Exception
     */
    public boolean connect() throws SocketException, IOException;

    /**
     * Call to ping the 3rd party virus software to make sure it is still
     * active.
     * 
     * @return True if service is active false otherwise
     * @throws Exception
     */
    public boolean ping() throws SocketException, IOException;

    /**
     * Scans the file supplied to check for viruses.
     * <p>
     * The scanner implementation should take the input stream provided, convert
     * it to the correct file format if necessary and pass it to the 3rd party
     * scanning software. This function has no return but it is required to
     * throw a scan complete event when finished.
     * </p>
     * 
     * @param request
     *        The original request that was submitted for scanning. It should be
     *        packaged in the complete event to be returned. The input stream
     *        included in the request has been exhausted and should not be used.
     * @param stream
     *        The file data that is too be scanned.
     * @param numberOfScans
     *        The total number of scans including this one that will take place
     *        on the file. This should be included in the scan complete event.
     */
    public void scanFile(VirusScanRequest request,
                         InputStream stream,
                         int numberOfScans) throws SocketException, IOException;

    /**
     * Changes the amount of time before a connection is considered to be dead.
     * <p>
     * This may not be applicable for all implementations.
     * </p>
     * 
     * @param timeout
     *        The new timeout value in milliseconds.
     */
    public void setTimeout(int timeout);

    /**
     * Returns the name of the scanning software that this wrapper is calling.
     * 
     * @return The name of the 3rd party virus scanning software this wrapper is
     *         calling.
     */
    public String getScannerName();
}
