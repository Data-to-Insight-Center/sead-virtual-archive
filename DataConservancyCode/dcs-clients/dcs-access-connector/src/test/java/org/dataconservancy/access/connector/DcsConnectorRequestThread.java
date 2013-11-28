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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class DcsConnectorRequestThread extends AbstractRequestThread {

    private DcsConnector connector;
    private ExceptionHandler<DcsConnectorFault> dcsFaultHandler =
            new ExceptionHandler<DcsConnectorFault>(null, null, ExceptionStrategy.FAIL);

    public DcsConnectorRequestThread(DcsConnector connector, String url, boolean consumeResponse) {
        super(url, consumeResponse);
        this.connector = connector;
    }

    @Override
    public void run() {
        super.run();

        if (connector == null) {
            throw new IllegalStateException("HttpClient must not be null.");
        }

        try {
            InputStream in = connector.getStream(url);
            if (consumeResponse) {
                IOUtils.copy(in, new NullOutputStream());
            }
        } catch (ClientProtocolException e) {
            try {
                cpeHandler.handleException(e);
            } catch (ClientProtocolException e1) {
                // don't care
            }
        } catch (IOException e) {
            try {
                ioeHandler.handleException(e);
            } catch (IOException e1) {
                // don't care
            }
        } catch (DcsConnectorFault e) {
            try {
                dcsFaultHandler.handleException(e);
            } catch (Throwable throwable) {
                // don't care
            }
        }
    }

    public DcsConnector getConnector() {
        return connector;
    }

    public void setConnector(DcsConnector connector) {
        this.connector = connector;
    }
}
