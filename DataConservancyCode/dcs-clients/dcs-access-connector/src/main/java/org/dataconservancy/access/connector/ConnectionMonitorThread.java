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
package org.dataconservancy.access.connector;

import org.apache.http.conn.ClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Polls the Apache HttpClient {@link ClientConnectionManager}, closing connections that are expired or
 * idle longer than 30 seconds.
 *
 * TODO: make this more configurable.
 */
class ConnectionMonitorThread extends Thread {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ClientConnectionManager connMgr;
    private volatile boolean shutdown;

    public ConnectionMonitorThread(ClientConnectionManager connMgr) {
        super();
        this.connMgr = connMgr;
    }

    @Override
    public void run() {
        try {
            while (!shutdown) {
                synchronized (this) {
                    wait(5000);
                    // Close expired connections
                    log.trace("Closing expired connections.");
                    connMgr.closeExpiredConnections();
                    // Optionally, close connections
                    // that have been idle longer than 30 sec
                    log.trace("Closing connections idle 30s or longer.");
                    connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                }
            }
        } catch (InterruptedException ex) {
            log.trace("Terminating.");
            // terminate
        }
    }

    public void shutdown() {
        log.trace("Shutting down.");
        shutdown = true;
        synchronized (this) {
            notifyAll();
        }
    }
}
