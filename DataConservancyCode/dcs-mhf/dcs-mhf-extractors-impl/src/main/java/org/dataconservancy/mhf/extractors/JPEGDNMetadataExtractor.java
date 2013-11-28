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

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Metadata;
import org.dataconservancy.mhf.extractor.api.ExtractionException;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.model.builder.api.AttributeValueBuilder;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;

import java.io.IOException;
import java.util.Collection;

/**
 * Responsible for extracting EXIF metadata embedded in JPEG files.
 * <p/>
 * Can safely handle files of types: {@code jpg}, {@code JPG}, {@code jpeg}, and {@code JPEG}.
 * <p/>
 * This particular implementation uses {@link com.drew.metadata.MetadataReader} to extract the embedded metadata.
 */
public class JPEGDNMetadataExtractor extends BaseEXIFMetadataExtractor {


    private final MetadataObjectBuilder moBuilder;

    public JPEGDNMetadataExtractor(MetadataObjectBuilder moBuilder, AttributeValueBuilder avBuilder) {
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
        if (!instance.getFormatId().equals(MetadataFormatId.JPEG_FORMAT_ID)
                && !instance.getFormatId().equals(MetadataFormatId.JPG_FORMAT_ID)) {
            throw new IllegalArgumentException(INVALID_FORMAT_ERROR);
        }

        Metadata jpegFileMetadata = null;

        try {
            jpegFileMetadata = JpegMetadataReader.readMetadata(instance.getContent());
        } catch (JpegProcessingException e) {
            throw new ExtractionException("Unable to read provided jpeg/jpg file. " + e.getMessage());
        } catch (IOException e) {
            throw new ExtractionException("Unable to read provided jpeg/jpg file. " + e.getMessage());
        } finally {
            try {
                instance.getContent().close();
            } catch (IOException e) {
                throw new ExtractionException(FAILED_CLOSE+ e.getMessage());
            }
        }


        return extractMetadata(jpegFileMetadata);
    }

}
