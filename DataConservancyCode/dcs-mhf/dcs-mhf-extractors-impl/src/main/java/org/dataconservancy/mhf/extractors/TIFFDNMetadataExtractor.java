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
package org.dataconservancy.mhf.extractors;

import com.drew.imaging.tiff.TiffMetadataReader;
import com.drew.metadata.Metadata;
import org.dataconservancy.mhf.extractor.api.ExtractionException;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.model.builder.api.AttributeValueBuilder;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;

import java.io.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: hanh
 * Date: 2/18/13
 * Time: 9:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class TIFFDNMetadataExtractor extends BaseEXIFMetadataExtractor {

    private final MetadataObjectBuilder moBuilder;

    public TIFFDNMetadataExtractor(MetadataObjectBuilder moBuilder, AttributeValueBuilder avBuilder) {
        super(avBuilder);

        if (avBuilder == null || moBuilder == null) {
            throw new IllegalArgumentException("Builders must not be null.");
        }

        this.moBuilder = moBuilder;
    }

    @Override
    public Collection<MetadataRepresentation> extractMetadata(MetadataInstance instance) throws ExtractionException {

        if (instance == null) {
            throw new IllegalArgumentException(NULL_INSTANCE);
        }
        if (!instance.getFormatId().equals(MetadataFormatId.TIF_FORMAT_ID)
                && !instance.getFormatId().equals(MetadataFormatId.TIFF_FORMAT_ID)) {
            throw new IllegalArgumentException(INVALID_FORMAT_ERROR);
        }

        Metadata tiffFileMetadata = null;
        File tempFile;
        InputStream inputStream = null;
        OutputStream out;
        try {
            System.gc();
            tempFile = File.createTempFile("tempTiffFile-", "");
            out = new FileOutputStream(tempFile);

            int read = 0;
            byte[] bytes = new byte[1024];

            inputStream = instance.getContent();
            while ((read = inputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();

            tiffFileMetadata = TiffMetadataReader.readMetadata(tempFile);
            tempFile.deleteOnExit();
            return extractMetadata(tiffFileMetadata);

        } catch (IOException e) {
            e.printStackTrace();
            throw new ExtractionException("Exception occurred when reading MetadataInstance's content. " + e.getMessage());

        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new ExtractionException(FAILED_CLOSE + e.getMessage());
            }

        }
    }
}
