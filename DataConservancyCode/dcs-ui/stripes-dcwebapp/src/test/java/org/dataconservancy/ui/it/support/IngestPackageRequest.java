/*
 * Copyright 2013 Johns Hopkins University
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

import eu.medsea.mimeutil.MimeUtil2;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 *
 */
public class IngestPackageRequest {

    public static final String INGEST_STRIPES_EVENT = "ingest";

    private UiUrlConfig urlConfig;

    private File packageToIngest;

    public IngestPackageRequest(UiUrlConfig urlConfig) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UI URL configuration must not be null.");
        }
        this.urlConfig = urlConfig;
    }

    public File getPackageToIngest() {
        return packageToIngest;
    }

    public void setPackageToIngest(File packageToIngest) {
        this.packageToIngest = packageToIngest;
    }

    public HttpPost asHttpPost(String packageMimeType) throws UnsupportedEncodingException {
        if (packageToIngest == null) {
            throw new IllegalStateException("The package to ingest must not be null: call setPackageToIngest(File) " +
                    "first");
        }

        final HttpPost request = new HttpPost(urlConfig.getIngestPackageUrl().toString());
//
//
//        if (packageMimeType != null) {
//            request.setHeader(HttpHeaders.CONTENT_TYPE, packageMimeType);
//        }
//
//        request.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(packageToIngest.length()));

        final MultipartEntity multiPart = new MultipartEntity();

        final FileBody fileBody;

        if (packageMimeType != null) {
            fileBody = new FileBody(packageToIngest, packageMimeType);
        } else {
            fileBody = new FileBody(packageToIngest);
        }

        multiPart.addPart("uploadedFile", fileBody);
        multiPart.addPart(INGEST_STRIPES_EVENT, new StringBody("Ingest", Charset.forName("UTF-8")));

        request.setEntity(multiPart);

        return request;

    }

    public HttpPost asHttpPost() throws UnsupportedEncodingException {
        return asHttpPost(null);
    }

}
