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

import org.junit.Test;

import junit.framework.Assert;

public class VirusScannerTest {

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
            // TODO Auto-generated method stub

        }

        @Override
        public String getScannerName() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Test
    public void createScannerTest() {
        TestVirusScannerFactory factory = new TestVirusScannerFactory();
        Assert.assertNotNull(factory);

        VirusScanner scanner = factory.createVirusScanner();

        Assert.assertNotNull(scanner);

    }

    @Test
    public void virusScannerTest() {
        TestVirusScannerFactory factory = new TestVirusScannerFactory();
        Assert.assertNotNull(factory);

        VirusScanner scanner = factory.createVirusScanner();

        Assert.assertNotNull(scanner);

        boolean isConnected = false;
        
        try{
            isConnected = scanner.connect();
        } catch (Exception e){}

        Assert.assertTrue(isConnected);

        boolean canBePinged = false;
        
        try{
            canBePinged = scanner.ping();
        } catch (Exception e) {};

        Assert.assertTrue(canBePinged);
    }
}
