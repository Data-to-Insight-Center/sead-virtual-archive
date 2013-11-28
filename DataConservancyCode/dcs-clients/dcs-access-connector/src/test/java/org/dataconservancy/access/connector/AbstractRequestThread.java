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

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;

/**
 *
 */
abstract class AbstractRequestThread extends Thread {

    String url;
    boolean consumeResponse = true;
    ExceptionStrategy exeStrategy = ExceptionStrategy.LOG;
    ExceptionHandler<ClientProtocolException> cpeHandler =
            new ExceptionHandler<ClientProtocolException>(null, null, exeStrategy);
    ExceptionHandler<IOException> ioeHandler =
            new ExceptionHandler<IOException>(null, null, exeStrategy);

    protected AbstractRequestThread(String url, boolean consumeResponse) {
        this.url = url;
        this.consumeResponse = consumeResponse;
    }

    @Override
    public void run() {
        if (url == null || url.trim().length() == 0) {
            throw new IllegalStateException("Url must not be null.");
        }
    }

    boolean isConsumeResponse() {
        return consumeResponse;
    }

    void setConsumeResponse(boolean consumeResponse) {
        this.consumeResponse = consumeResponse;
    }

    String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url = url;
    }
}
