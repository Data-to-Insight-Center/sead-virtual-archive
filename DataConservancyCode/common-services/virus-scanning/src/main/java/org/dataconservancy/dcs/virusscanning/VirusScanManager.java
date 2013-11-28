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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

import org.dataconservancy.dcs.virusscanning.event.ScanCompleteEvent;
import org.dataconservancy.dcs.virusscanning.event.VirusScanningEventManager;

/**
 * The manager used to send requests to be scanned by all the systems virus
 * scanners.
 * <p>
 * Users will get an instance of this manager to interact with through
 * getManager, and will not create their own instance. Virus Scans are
 * initialized using the request scan call.
 * </p>
 */
public class VirusScanManager {

    private ArrayList<VirusScannerFactory> scannerFactories;

    private ArrayList<VirusScanner> scanners;

    private static VirusScanManager instance;

    private ByteArrayOutputStream copyOfStream;

    private VirusScanManager() {
        scannerFactories = new ArrayList<VirusScannerFactory>();
        scanners = new ArrayList<VirusScanner>();
        copyOfStream = new ByteArrayOutputStream();
        initialize();
    }

    /**
     * Used to get an instance a VirusScanManager to interact with.
     * 
     * @return A VirusScanManager instance.
     */
    public static VirusScanManager getManager() {

        if (instance == null) {
            instance = new VirusScanManager();
        }

        synchronized (instance) {
            return instance;
        }
    }

    private void initialize() {
        //set up scanners from the factories
        //Test to make sure we have a scanner factory
        if (scannerFactories != null) {
            for (int i = 0; i < scannerFactories.size(); i++) {
                VirusScanner scanner =
                        scannerFactories.get(i).createVirusScanner();
                if (scanner != null) {
                    boolean duplicate = false;
                    for (int j = 0; j < scanners.size(); j++) {
                        if (scanner.getScannerName().equalsIgnoreCase(scanners
                                .get(j).getScannerName())) {
                            duplicate = true;
                        }
                    }
                    if (!duplicate) {
                        scanners.add(scanner);
                        try {
                            scanner.connect();
                        } catch (Exception e) {

                        }
                    }
                }
            }
        }
    }

    public void setScannerFactories(VirusScannerFactory[] factories) {
        if (factories != null) {
            for (int i = 0; i < factories.length; i++) {
                scannerFactories.add(factories[i]);
                VirusScanner scanner = factories[i].createVirusScanner();
                if (scanner != null) {
                    boolean duplicate = false;
                    for (int j = 0; j < scanners.size(); j++) {
                        if (scanner.getScannerName().equalsIgnoreCase(scanners
                                .get(j).getScannerName())) {
                            duplicate = true;
                        }
                    }
                    if (!duplicate) scanners.add(scanner);
                    try {
                        scanner.connect();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    private void executeScan(VirusScanRequest request) {
        try {
            for (int i = 0; i < scanners.size(); i++) {
                InputStream data = getCopyOfData();
                if (data != null) {
                    if (scanners.get(i).ping()) {
                        scanners.get(i)
                                .scanFile(request, data, scanners.size());
                    } else if (scanners.get(i).connect()) {
                        scanners.get(i)
                                .scanFile(request, data, scanners.size());
                    } else {
                        ScanCompleteEvent event =
                                new ScanCompleteEvent(request,
                                                      System.currentTimeMillis(),
                                                      System.currentTimeMillis(),
                                                      scanners.size(),
                                                      scanners.get(i)
                                                              .getScannerName());
                        event.setError("Unable to connect to scanner: "
                                + scanners.get(i).getScannerName());
                        VirusScanningEventManager.getManager().sendEvent(event);
                    }
                } else {
                    ScanCompleteEvent event =
                            new ScanCompleteEvent(request,
                                                  System.currentTimeMillis(),
                                                  System.currentTimeMillis(),
                                                  scanners.size(),
                                                  scanners.get(i)
                                                          .getScannerName());
                    event.setError("Unable to Read File Data");
                    VirusScanningEventManager.getManager().sendEvent(event);
                }
            }
        } catch (Exception e) {

        }
    }

    /**
     * Used to request a Virus Scan on the file encapsulated in the
     * VirusScanRequest object.
     * 
     * @param request
     *        The request that a scan contains the file information to be
     *        scanned.
     */
    public void requestScan(VirusScanRequest request) {
        //Make copies of input stream for scanners
        InputStream originalStream = request.getFileData();
        if (scanners.size() > 0) {
            if (originalStream != null) {
                copyInputStream(originalStream);

                executeScan(request);
            } else {
                ScanCompleteEvent event =
                        new ScanCompleteEvent(request,
                                              System.currentTimeMillis(),
                                              System.currentTimeMillis(),
                                              scanners.size(),
                                              "No Scanner");
                event.setError("File Data was NULL");
                VirusScanningEventManager.getManager().sendEvent(event);
            }
        } else {
            ScanCompleteEvent event =
                    new ScanCompleteEvent(request,
                                          System.currentTimeMillis(),
                                          System.currentTimeMillis(),
                                          scanners.size(),
                                          "No Scanner");
            event.setError("There are no scanners available to scan the file.");
            VirusScanningEventManager.getManager().sendEvent(event);
        }
    }

    private void copyInputStream(InputStream input) {
        try {
            copyOfStream = new ByteArrayOutputStream();
            IOUtils.copy(input, copyOfStream);
        } catch (IOException e) {

        }
    }

    private InputStream getCopyOfData() {
        InputStream newStream = null;
        if (copyOfStream != null) {
            newStream =
                    (InputStream) new ByteArrayInputStream(copyOfStream.toByteArray());
        }
        return newStream;
    }

    /**
     * Sets the timeout for the Virus scanner.
     * 
     * @param timeout
     *        The new timeout value in milliseconds.
     */
    public void setTimeout(int timeout) {
        for (int i = 0; i < scanners.size(); i++) {
            scanners.get(i).setTimeout(timeout);
        }
    }
}
