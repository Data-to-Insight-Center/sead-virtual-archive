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
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.virusscanning.event.ScanCompleteEvent;
import org.dataconservancy.dcs.virusscanning.event.VirusScanEventListener;
import org.dataconservancy.dcs.virusscanning.event.VirusScanningEventManager;

import junit.framework.Assert;

public class VirusScanManagerTest
        implements VirusScanEventListener {

    private VirusScanManager manager;

    private VirusScannerFactory[] factory;

    private ScanCompleteEvent returnedEvent = null;

    private static final String UNIQUE_ID = "48230";

    private class TestVirusScannerFactory
            implements VirusScannerFactory {

        public TestVirusScannerFactory() {

        }

        @Override
        public VirusScanner createVirusScanner() {
            return new TestVirusScanner();
        }
    }

    private class TestVirusScanner
            implements VirusScanner {

        public TestVirusScanner() {

        }

        @Override
        public boolean connect() {
            return true;
        }

        @Override
        public boolean ping() {
            return true;
        }

        @Override
        public void setTimeout(int timeout) {
            // TODO Auto-generated method stub

        }

        @Override
        public void scanFile(VirusScanRequest request,
                             InputStream stream,
                             int numberOfScans) {
            ScanCompleteEvent event =
                    new ScanCompleteEvent(request,
                                          System.currentTimeMillis(),
                                          System.currentTimeMillis(),
                                          numberOfScans,
                                          getScannerName());

            VirusScanningEventManager.getManager().sendEvent(event);
        }

        @Override
        public String getScannerName() {

            return "test";
        }

    }

    @Before
    public void setup() {
        //Get an instance of the manager for the test.
        manager = VirusScanManager.getManager();

        //Create a new factory for a new scanner for each test.
        factory = new VirusScannerFactory[] {new TestVirusScannerFactory()};

        //Reset the returned event for each test.
        returnedEvent = null;

        VirusScanningEventManager.getManager().registerListener(this);
    }

    @After
    public void cleanUp() {
        VirusScanningEventManager.getManager().unRegisterListener(this);
    }

    @Test
    public void getManagerTest() {
        VirusScanManager manager = VirusScanManager.getManager();

        Assert.assertNotNull(manager);
    }

    @Test
    public void noScannerTest() {
        VirusScanRequest request = new VirusScanRequest("nullTest", null, "0");
        manager.requestScan(request);

        //Delay while the event returns
        int i = 0;
        while (i < 30) {
            i++;
        }

        //Checked that we received an actual event
        Assert.assertNotNull(returnedEvent);

        Assert.assertTrue(returnedEvent.hadError());

        Assert.assertEquals("There are no scanners available to scan the file.",
                            returnedEvent.getResultDetails());
    }

    @Test
    public void sendScanRequestTest() {
        //Give the manager a test factory to work with. 
        manager.setScannerFactories(factory);

        //Set up "file data" to scan
        byte[] data = {0, 0};
        InputStream stream = (InputStream) new ByteArrayInputStream(data);

        //Make request
        VirusScanRequest request =
                new VirusScanRequest("test", stream, UNIQUE_ID);
        manager.requestScan(request);

        //Delay while the event returns
        int i = 0;
        while (i < 30) {
            i++;
        }

        //Checked that we received an actual event
        Assert.assertNotNull(returnedEvent);

        //Test that the ID is the same of the request we passed in
        Assert.assertEquals(UNIQUE_ID, returnedEvent.getRequest().getId());

        Assert.assertEquals(1, returnedEvent.getNumberOfScans());
    }

    @Test
    public void nullFileTest() {
        VirusScanRequest request = new VirusScanRequest("nullTest", null, "0");
        manager.requestScan(request);

        //Delay while the event returns
        int i = 0;
        while (i < 30) {
            i++;
        }

        //Checked that we received an actual event
        Assert.assertNotNull(returnedEvent);

        Assert.assertTrue(returnedEvent.hadError());

        Assert.assertEquals("File Data was NULL",
                            returnedEvent.getResultDetails());
    }

    @Override
    public void onScanComplete(ScanCompleteEvent event) {

        returnedEvent = event;
    }
}
