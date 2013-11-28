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
package org.dataconservancy.ui.services;

import org.apache.commons.io.input.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * An implementation of a {@link DepositDocumentResolver} handling {@code depositId} in the form of a {@code URL}.
 */
public class UrlDepositDocumentResolver implements DepositDocumentResolver {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DepositDocumentParser parser;

    public UrlDepositDocumentResolver(DepositDocumentParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("DepositDocumentParser must not be null.");
        }
        this.parser = parser;
    }

    @Override
    public DepositDocument resolve(String depositId) {
        URL depositUrl = null;
        try {
            depositUrl = new URL(depositId);
        } catch (MalformedURLException e) {
            log.info("Deposit ID '" + depositId + "' cannot be converted to a URL.", e);
            return null;
        }

        InputStream in = null;
        try {
            in = depositUrl.openStream();
            if (log.isDebugEnabled() && in != null) {
                File tmp = File.createTempFile("feed-" +
                        Thread.currentThread().getName().replace(" ", "_").replace("/", "_").replace("\\", "_"), ".xml");
                log.debug("Copy of deposit feed: {}", tmp);
                in = new TeeInputStream(in, new FileOutputStream(tmp));
            }
            return parser.parse(in);
        } catch (IOException e) {
            log.info("Could not open URL " + depositId + ": " + e.getMessage(), e);
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // don't care
            }
        }
    }
}
