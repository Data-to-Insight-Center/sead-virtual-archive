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
package org.dataconservancy.dcs.virusscanning.event;

import java.util.Date;

import org.dataconservancy.dcs.virusscanning.VirusScanManager;
import org.dataconservancy.dcs.virusscanning.VirusScanRequest;

/**
 * An event to signify that a virus scan is complete.
 * <p>
 * This event should be thrown be each virus scanner when it is done scanning.
 * Clients can register to listen to this event through the
 * {@link VirusScanningEventManager}. This event should be thrown whenever the
 * scanner completes including due to the occurrence of an error. Clients can
 * use the VirusScanRequest object to match the event to their original request.
 * </p>
 */
public class ScanCompleteEvent {

    private final VirusScanRequest request;

    private boolean hadError;

    private boolean containedVirus;

    private String resultMessage;

    private final int numberOfScans;

    private final long startTime;

    private final long endTime;

    private String logInformation;
    
    private String scannerName;

    /**
     * Constructor used to create a new ScanCompleteEvent.
     * 
     * @param request
     *        The original request that was sent to the {@link VirusScanManager}
     *        .
     * @param startTime
     *        The time and date the scan was started in milliseconds.
     * @param endTime
     *        The time and date the scan finished in milliseconds.
     * @param scans
     *        The number of scans that the file is going to under go. As
     *        specified by the VirusScanManager.
     */
    public ScanCompleteEvent(VirusScanRequest request,
                             long startTime,
                             long endTime,
                             int scans,
                             String name) {
        this.request = request;
        this.startTime = startTime;
        this.endTime = endTime;
        numberOfScans = scans;
        this.scannerName = name;
        
        hadError = false;
        containedVirus = false;
    }

    /**
     * Sets an error message as the result of the scan. Setting this also sets
     * the boolean flag that an error occured during scanning. This should only
     * be used for scans that were unable to finish due to error.
     * 
     * @param message
     *        A human readable description of the error that occured.
     */
    public void setError(String message) {
        hadError = true;
        resultMessage = message;
    }

    /**
     * Sets the virus information about viruses found in the file. This will
     * also set the boolean flag that tells the user a virus was found in the
     * file.
     * 
     * @param message
     *        A human readable message with the names of the viruses found and
     *        any other information that can be provided.
     */
    public void setVirusInfo(String message) {
        containedVirus = true;
        resultMessage = message;
    }

    /**
     * Sets any log information that occurred during scanning. This can be used
     * for any errors or warnings that did not prevent the scan from completing.
     * 
     * @param info
     *        Human readable log information from the scanning software.
     */
    public void setLogInfo(String info) {
        logInformation = info;
    }

    /**
     * Used to determine if an error occured during scanning that prevented the
     * scan from completing succesfully.
     * 
     * @return True if an error occured false otherwise.
     */
    public boolean hadError() {
        return hadError;
    }

    /**
     * Used to get a readable message about the results of the scan. This
     * message will contain either virus information, or error information.
     * Depending on how the scan completed.
     * 
     * @return A human readable message with the errors or viruses that occured
     *         while scanning.
     */
    public String getResultDetails() {
        return resultMessage;
    }

    /**
     * Used to determine if a virus was found during scanning.
     * 
     * @return True if a virus was found false otherwise.
     */
    public boolean containedVirus() {
        return containedVirus;
    }

    /**
     * Used to retrieve any additional log information from the scanner if any
     * was supplied.
     * 
     * @return Any human readable log information provided by the virus scanner.
     */
    public String getLogInfo() {
        return logInformation;
    }

    /**
     * Used to get the start time of the scan.
     * 
     * @return The start time and date of the scan in milliseconds.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * A convenience function for getting the start time in human readable
     * format.
     * 
     * @return The start time of the scan in human readable format. Format is
     *         (Day of week) month day hour:minute:seconds timezone year
     */
    public String getStartTimeAsString() {
        String startTimeString = "";

        Date startDate = new Date(startTime);
        if (startDate != null) {
            startTimeString = startDate.toString();
        }

        return startTimeString;
    }

    /**
     * A call to get the end time and date of the scan in milliseconds.
     * 
     * @return The end time and date of the scan in milliseconds.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * A convienence function for getting the start time in human readable
     * format.
     * 
     * @return The end time of the scan in human readable form. Format is (Day
     *         of week) month day hour:minute:seconds timezone year
     */
    public String getEndTimeAsString() {
        String endTimeString = "";
        Date endDate = new Date(endTime);
        if (endDate != null) {
            endTimeString = endDate.toString();
        }
        return endTimeString;
    }

    /**
     * Call to get the original request that started the scan. This can be used
     * to determine by the client to determine if the event corresponds to their
     * request.
     * 
     * @return The VirusScanRequest object that was passed to the
     *         VirusScanManager for scanning.
     */
    public VirusScanRequest getRequest() {
        return request;
    }

    /**
     * Call to get the number of scans that are being performed on the request.
     * <p>
     * This will be identical for all events on a particular request. This can
     * be used by the client to determine when scanning is finished on their
     * request. Clients will receive the same number of events as is returned by
     * this call.
     * </p>
     * 
     * @return An integer representing the number of scans that are going to be
     *         performed on the request.
     */
    public int getNumberOfScans() {
        return numberOfScans;
    }
    
    /**
     * Call to get the name of the scanner.
     * 
     * @return The name of the virus scanner. 
     */
    public String getScannerName() {
        return scannerName;
    }
}
