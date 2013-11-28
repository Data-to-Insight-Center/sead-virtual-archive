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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import org.dataconservancy.dcs.virusscanning.VirusScanRequest;
import org.dataconservancy.dcs.virusscanning.VirusScanner;
import org.dataconservancy.dcs.virusscanning.VirusScannerFactory;
import org.dataconservancy.dcs.virusscanning.event.ScanCompleteEvent;
import org.dataconservancy.dcs.virusscanning.event.VirusScanningEventManager;

public class ClamAVVirusScannerFactory
        implements VirusScannerFactory {

    private class ClamAVVirusScanner
            implements VirusScanner {

        private String host;

        private static final int PORT = 3310;

        private static final String NAME = "ClamAV";

        private int timeout = 500;

        Socket socket;

        public ClamAVVirusScanner() {
            host = System.getProperty("dcs.clamav.host");
            
            //Fallback to old default
            if( host == null || host.isEmpty() ){
                host = "ben-test.dkc.jhu.edu";
            }
        }

        @Override
        public boolean connect() throws IOException, SocketException {
            socket = new Socket();

            socket.connect(new InetSocketAddress(host, PORT));

            socket.setSoTimeout(timeout);

            //Call ping?
            return socket.isConnected();
        }

        @Override
        public boolean ping() throws SocketException, IOException {
            DataOutputStream dos = null;
            StringBuffer sb = new StringBuffer();

            if (socket == null || !socket.isConnected() || socket.isClosed()) {
                connect();
            }

            dos = new DataOutputStream(socket.getOutputStream());

            dos.write("zPING\0".getBytes());
            dos.flush();

            InputStream is = null;
            is = socket.getInputStream();

            int readSize = 2048;
            byte[] buffer = new byte[readSize];

            while (readSize == 2048) {
                readSize = is.read(buffer);
                sb.append(new String(buffer, 0, readSize));
            }

            if (dos != null) {
                dos.close();
            }

            socket.close();
            return sb.toString().equalsIgnoreCase("PONG\0");
        }

        @Override
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        @Override
        public void scanFile(VirusScanRequest request,
                             InputStream stream,
                             int numberOfScans) throws SocketException,
                IOException {
            long startTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis();

            //TODO Take out nested returns
            if (!ping()) {
                if (!connect()) {
                    ScanCompleteEvent event =
                            new ScanCompleteEvent(request,
                                                  startTime,
                                                  endTime,
                                                  numberOfScans,
                                                  getScannerName());
                    event.setError("Unable to connect to: " + getScannerName());

                    VirusScanningEventManager.getManager().sendEvent(event);
                    return;
                }
            }

            //ClamAV will close the socket after a ping call so we need to reconnect.
            if (socket == null || !socket.isConnected() || socket.isClosed()) {
                connect();
            }

            DataOutputStream dos = null;
            String scanResult = "";
            try {
                try {
                    dos = new DataOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    ScanCompleteEvent event =
                            new ScanCompleteEvent(request,
                                                  startTime,
                                                  endTime,
                                                  numberOfScans,
                                                  getScannerName());
                    event.setError(e.getMessage());
                    VirusScanningEventManager.getManager().sendEvent(event);
                    return;
                }

                try {
                    dos.write("zINSTREAM\0".getBytes());
                } catch (IOException e) {
                    ScanCompleteEvent event =
                            new ScanCompleteEvent(request,
                                                  startTime,
                                                  endTime,
                                                  numberOfScans,
                                                  getScannerName());
                    event.setError(e.getMessage());
                    VirusScanningEventManager.getManager().sendEvent(event);
                    return;
                }

                int readSize = 2048;
                byte[] buffer = new byte[2048];
                while (readSize == 2048) {
                    try {
                        readSize = stream.read(buffer);
                    } catch (IOException e) {
                        ScanCompleteEvent event =
                                new ScanCompleteEvent(request,
                                                      startTime,
                                                      endTime,
                                                      numberOfScans,
                                                      getScannerName());
                        event.setError(e.getMessage());
                        VirusScanningEventManager.getManager().sendEvent(event);
                        return;
                    }
                    try {
                        dos.writeInt(readSize);
                        dos.write(buffer, 0, readSize);
                    } catch (IOException e) {
                    }
                }

                try {
                    dos.writeInt(0);
                    dos.flush();
                } catch (IOException e) {
                }

                try {
                    readSize = socket.getInputStream().read(buffer);
                } catch (IOException e) {
                }

                scanResult = new String(buffer, 0, readSize);
                endTime = System.currentTimeMillis();

            } finally {
                if (dos != null) {
                    try {
                        dos.close();
                    } catch (IOException e) {
                    }
                }
            }

            ScanCompleteEvent event =
                    new ScanCompleteEvent(request,
                                          startTime,
                                          endTime,
                                          numberOfScans,
                                          getScannerName());
            if (scanResult.contains("FOUND")) {
                event.setVirusInfo(scanResult);
            } else if (scanResult.contains("ERROR")) {
                event.setError(scanResult);
            }
            VirusScanningEventManager.getManager().sendEvent(event);
        }

        @Override
        public String getScannerName() {
            return NAME;
        }

    }

    @Override
    public VirusScanner createVirusScanner() {
        return new ClamAVVirusScanner();
    }

}
