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
package org.dataconservancy.dcs.virusscanning.impl;

import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.virusscanning.VirusScanManager;
import org.dataconservancy.dcs.virusscanning.VirusScanRequest;
import org.dataconservancy.dcs.virusscanning.VirusScanner;
import org.dataconservancy.dcs.virusscanning.VirusScannerFactory;
import org.dataconservancy.dcs.virusscanning.event.ScanCompleteEvent;
import org.dataconservancy.dcs.virusscanning.event.VirusScanEventListener;
import org.dataconservancy.dcs.virusscanning.event.VirusScanningEventManager;

import junit.framework.Assert;

public class ClamAVVirusScannerTest
        implements VirusScanEventListener {

    private VirusScanManager manager;

    private VirusScannerFactory factory;

    private static final String CLEAN_FILE = "/cleanTest.txt";

    private static final String VIRUS_FILE = "/eicar.com.txt";

    private static final String CLEAN_ID = "clean";

    private static final String VIRUS_ID = "virus";

    @Before
    public void setup() {
        manager = VirusScanManager.getManager();

        factory = new ClamAVVirusScannerFactory();
        VirusScannerFactory[] factories = {factory};
        manager.setScannerFactories(factories);

        VirusScanningEventManager.getManager().registerListener(this);
    }

    @After
    public void cleanUp() {
        VirusScanningEventManager.getManager().unRegisterListener(this);
    }

    @Test
    public void connectionTest() {
        VirusScanner scanner = factory.createVirusScanner();
        boolean isConnected = false;
        try {
            isConnected = scanner.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(isConnected);
    }

    @Test
    public void pingTest() {
        VirusScanner scanner = factory.createVirusScanner();
        boolean pinged = false;
        try {
            pinged = scanner.ping();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Assert.assertTrue(pinged);
    }

    @Test
    public void cleanFileTest() {
        InputStream cleanFile = this.getClass().getResourceAsStream(CLEAN_FILE);

        Assert.assertNotNull(cleanFile);
        VirusScanRequest request =
                new VirusScanRequest("clean_test", cleanFile, CLEAN_ID);
        manager.requestScan(request);
    }

    @Test
    public void virusFoundTest() {
        InputStream virusFile = this.getClass().getResourceAsStream(VIRUS_FILE);
        VirusScanRequest request =
                new VirusScanRequest("virus_file", virusFile, VIRUS_ID);
        manager.requestScan(request);
    }

    @Override
    public void onScanComplete(ScanCompleteEvent event) {
        if (event.getScannerName().equalsIgnoreCase("ClamAV")) {
            if (event.getRequest().getId() == CLEAN_ID) {
                Assert.assertFalse(event.hadError());
                Assert.assertFalse(event.containedVirus());
            } else if (event.getRequest().getId() == VIRUS_ID) {
                Assert.assertFalse(event.hadError());
                Assert.assertTrue(event.containedVirus());
            }
        }
    }

}
