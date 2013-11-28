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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.virusscanning.VirusScanRequest;

public class ScanCompleteEventTest
        implements VirusScanEventListener {

    private static ScanCompleteEvent event;

    private final static String FILE_NAME = "ScanCompleteTest";

    private final static String UNIQUE_ID = "4362";

    private static VirusScanRequest request = new VirusScanRequest(FILE_NAME,
                                                                   null,
                                                                   UNIQUE_ID);

    private boolean scanReceived;

    private static ScanCompleteEvent returnedEvent;

    @BeforeClass
    public static void setUp() {
        //Create a sample event
        event = new ScanCompleteEvent(request,
                                      System.currentTimeMillis(),
                                      System.currentTimeMillis() + 1000,
                                      2,
                                      "test");
        event.setVirusInfo("File foo contained a virus.");
    }

    @Test
    public void virusFoundTest() {
        VirusScanningEventManager.getManager().registerListener(this);
        scanReceived = false;
        returnedEvent = null;

        VirusScanningEventManager.getManager().sendEvent(event);
        int i = 0;
        while (!scanReceived && i < 20) {
            i++;
        }

        //Check that we received the event
        Assert.assertTrue(scanReceived);

        //Check that the event is not null
        Assert.assertNotNull(returnedEvent);

        VirusScanRequest tempRequest = returnedEvent.getRequest();

        Assert.assertEquals(tempRequest.getId(), UNIQUE_ID);

        Assert.assertTrue(returnedEvent.containedVirus());

        VirusScanningEventManager.getManager().unRegisterListener(this);
    }

    @Test
    public void eventListenerUnregisterTest() {
        Assert.assertTrue(VirusScanningEventManager.getManager()
                .registerListener(this));
        scanReceived = false;
        Assert.assertTrue(VirusScanningEventManager.getManager()
                .unRegisterListener(this));
        VirusScanningEventManager.getManager().sendEvent(event);
        int i = 0;
        while (!scanReceived && i < 50) {
            i++;
        }

        Assert.assertFalse(scanReceived);
    }

    @Test
    public void setErrorTest() {
        VirusScanningEventManager.getManager().registerListener(this);
        scanReceived = false;

        event.setError("An error has occured.");
        VirusScanningEventManager.getManager().sendEvent(event);

        int i = 0;
        while (!scanReceived && i < 20) {
            i++;
        }

    }

    @Override
    public void onScanComplete(ScanCompleteEvent event) {
        scanReceived = true;
        returnedEvent = event;
    }

}
