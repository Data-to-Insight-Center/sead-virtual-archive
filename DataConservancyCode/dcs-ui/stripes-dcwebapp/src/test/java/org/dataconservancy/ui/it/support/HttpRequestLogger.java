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
package org.dataconservancy.ui.it.support;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *
 */
public class HttpRequestLogger {

    private static Logger LOG = LoggerFactory.getLogger(HttpRequestLogger.class);

    public static void logRequest(HttpRequest req) {
        StringBuilder msg = new StringBuilder("HTTP Request:\n");
        msg.append(req.getRequestLine().toString()).append("\n");
        for (Header h : req.getAllHeaders()) {
            msg.append(h.getName()).append(": ").append(h.getValue()).append("\n");
        }

        if (req instanceof HttpPost) {
            HttpEntity entity = ((HttpPost) req).getEntity();

            if (entity instanceof MultipartEntity) {
                msg.append("** Cannot display multipart entity bodies! **");
            } else {
                try {
                    msg.append(IOUtils.toString(entity.getContent(), "UTF-8"));
                    msg.append("\n");
                } catch (IOException e) {
                    msg.append("** Cannot display entity body: ").append(e.getMessage());
                }
            }
        }

        msg.append("\n\n");
        LOG.error(msg.toString());
    }

    public static void logResponse(HttpResponse res) {
        StringBuilder msg = new StringBuilder("HTTP Response:\n");
        msg.append(res.getStatusLine().toString()).append("\n");
        for (Header h : res.getAllHeaders()) {
            msg.append(h.getName()).append(": ").append(h.getValue()).append("\n");
        }
        msg.append("\n");

        HttpEntity entity = res.getEntity();

        try {
            msg.append(IOUtils.toString(entity.getContent(), "UTF-8"));
            msg.append("\n");
        } catch (IOException e) {
            msg.append("** Cannot display entity body: ").append(e.getMessage());
        }


        msg.append("\n\n");
        LOG.error(msg.toString());
    }

}
