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
package org.dataconservancy.dcs.ingest.services;

import java.io.IOException;
import java.io.InputStream;

import java.net.SocketException;

import org.junit.Test;

import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.dcs.virusscanning.VirusScanRequest;
import org.dataconservancy.dcs.virusscanning.VirusScanner;
import org.dataconservancy.dcs.virusscanning.VirusScannerFactory;
import org.dataconservancy.dcs.virusscanning.event.ScanCompleteEvent;
import org.dataconservancy.dcs.virusscanning.event.VirusScanningEventManager;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;

import junit.framework.Assert;

public class VirusCheckerTest {

    private static SipStager stager = new MemoryStager();

    private IngestFramework fwk = new IngestFramework();

    private static final String FILE_ONE =
            "/org/dataconservancy/dcs/ingest/services/fileOne.txt";

    private static final String FILE_TWO =
            "/org/dataconservancy/dcs/ingest/services/fileTwo.txt";

    @Test
    public void singleFileTest() throws Exception {
        VirusChecker checker = getVirusChecker();
        Dcp sip = new Dcp();

        DcsFile file = new DcsFile();
        file.setSource(this.getClass().getResource(FILE_ONE).toString());
        file.setExtant(true);
        file.setName("File_One");
        file.setId("one");
        sip.addFile(file);

        String id = stager.addSIP(sip);

        checker.execute(id);

        Dcp newSip = fwk.getSipStager().getSIP(id);

        int virusCount = 0;

        for (DcsEvent event : newSip.getEvents()) {
            if (event.getEventType() == Events.VIRUS_SCAN) {
                virusCount++;
            }
        }

        Assert.assertEquals(1, virusCount);
    }

    @Test
    public void multipleFileTest() throws Exception {
        VirusChecker checker = getVirusChecker();
        Dcp sip = new Dcp();

        DcsFile file = new DcsFile();
        file.setSource(this.getClass().getResource(FILE_ONE).toString());
        file.setExtant(true);
        file.setName("File_One");
        file.setId("one");
        sip.addFile(file);

        DcsFile fileTwo = new DcsFile();
        fileTwo.setSource(this.getClass().getResource(FILE_TWO).toString());
        fileTwo.setExtant(true);
        fileTwo.setName("File_Two");
        fileTwo.setId("two");
        sip.addFile(fileTwo);

        String id = stager.addSIP(sip);

        checker.execute(id);

        Dcp newSip = fwk.getSipStager().getSIP(id);

        int virusCount = 0;
        //Can't guarantee the order results will be returned so just ensure we get both results back.         
        boolean outcomeOneReturned = false;
        boolean outcomeTwoReturned = false;
        for (DcsEvent event : newSip.getEvents()) {
            if (event.getEventType() == Events.VIRUS_SCAN) {
                virusCount++;
                if (event.getOutcome().contains("A virus")) {
                    Assert.assertEquals("A virus was found in File_Two by Fake Scanner",
                                        event.getOutcome());
                    Assert.assertEquals("Foo virus found", event.getDetail());
                    outcomeTwoReturned = true;
                } else {
                    Assert.assertEquals("File_One was scanned by: Fake Scanner",
                                        event.getOutcome());
                    outcomeOneReturned = true;
                }
            }
        }
        Assert.assertTrue(outcomeOneReturned);
        Assert.assertTrue(outcomeTwoReturned);
        Assert.assertEquals(2, virusCount);
    }

    public VirusChecker getVirusChecker() {

        InlineEventManager mgr = new InlineEventManager();
        mgr.setIdService(new MemoryIdServiceImpl());
        mgr.setSipStager(stager);

        fwk.setSipStager(stager);
        fwk.setEventManager(mgr);

        VirusChecker checker = new VirusChecker();
        VirusScannerFactory[] factories = {new FakeScannerFactory()};
        checker.setScannerFactories(factories);
        checker.setIngestFramework(fwk);
        return checker;
    }

    private static class FakeScannerFactory
            implements VirusScannerFactory {

        @Override
        public VirusScanner createVirusScanner() {
            return new FakeScanner();
        }

        private static class FakeScanner
                implements VirusScanner {

            public FakeScanner() {

            }

            @Override
            public boolean connect() throws SocketException, IOException {
                return true;
            }

            @Override
            public boolean ping() throws SocketException, IOException {
                return true;
            }

            @Override
            public void scanFile(VirusScanRequest request,
                                 InputStream stream,
                                 int numberOfScans) throws SocketException,
                    IOException {

                ScanCompleteEvent event =
                        new ScanCompleteEvent(request,
                                              System.currentTimeMillis(),
                                              System.currentTimeMillis() + 5500,
                                              numberOfScans,
                                              getScannerName());

                if (request.getFileName().equalsIgnoreCase("File_Two")) {
                    event.setVirusInfo("Foo virus found");
                }

                VirusScanningEventManager.getManager().sendEvent(event);
            }

            @Override
            public void setTimeout(int timeout) {

            }

            @Override
            public String getScannerName() {
                return "Fake Scanner";
            }

        }
    }
}
