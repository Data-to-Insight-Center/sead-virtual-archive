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
package org.dataconservancy.ui.util;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;

import java.io.File;
import java.io.IOException;

/**
 * This is a short utility class to help business services guess at a file's mime type
 * Thanks to Rodrigo Garcia who suggested this simple method as an answer to a question on
 * stackoverflow.com Permalink:  http://stackoverflow.com/a/9248119
 * Original license for stackoverflow content is cc-wiki
 */
public class ContentTypeUtil {

    private static final Detector DETECTOR = new DefaultDetector(
            MimeTypes.getDefaultMimeTypes());

    /**
     *  This method returns the mime type for the supplied file. We do not look at any part of the file
     *  name, just at the innards of the file.
     * @param file the file whose mime type we are seeking
     * @return the mime type for the file
     * @throws IOException
     */
    public static String detectMimeType(final File file) throws IOException {
        TikaInputStream tikaInputStream = null;
        try {
            tikaInputStream = TikaInputStream.get(file);

            final Metadata metadata = new Metadata();
            // we don't want to include a check of mime type based on the
            // filename, but if we did we would uncomment the next line
            // metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());

            return DETECTOR.detect(tikaInputStream, metadata).toString();
        } finally {
            if (tikaInputStream != null) {
                tikaInputStream.close();
            }
        }
    }

}
