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

import org.dataconservancy.mhf.extractor.api.ExtractionException;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;
import org.dataconservancy.mhf.representations.AttributeSetMetadataRepresentation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Extracts {@code AttributeSet} instances from {@code AttributeSetMetadataInstance} objects.
 */
public class AttributeSetMetadataExtractor extends BaseMetadataExtractor {

    private final MetadataObjectBuilder builder;

    public AttributeSetMetadataExtractor(MetadataObjectBuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("Builder must not be null.");
        }

        this.builder = builder;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation note: <br/>
     * Attempts to extract an {@code AttributeSetMetadataRepresentation} from the supplied {@code MetadataInstance}.
     * The format of the instance must be that of an attribute set, otherwise an {@code ExtractionException} will be
     * thrown.
     *
     * @param instance metadata in a specific format
     * @return the MetadataRepresentation, a AttributeSetMetadataRepresentation
     * @throws ExtractionException if the {@code MetadataInstance} is of the incorrect format
     */
    @Override
    public Collection<MetadataRepresentation> extractMetadata(MetadataInstance instance) {
        if (instance == null) {
            IllegalArgumentException iae = new IllegalArgumentException(NULL_INSTANCE);
            throw new ExtractionException(iae.getMessage(), iae);
        }

        if (!MetadataFormatId.ATTRIBUTE_SET_METADATA_FORMAT_ID.equals(instance.getFormatId())) {
            throw new ExtractionException(String.format(INVALID_FORMAT_ERROR, instance.getFormatId()));
        }

        final AttributeSet attributes;
        try {
            attributes = builder.buildAttributeSet(instance.getContent());
        } catch (Exception e) {
            throw new ExtractionException(e.getMessage(), e);
        } finally {
            try {
                instance.getContent().close();
            } catch (IOException e) {
                throw new ExtractionException(FAILED_CLOSE);
            }
        }

        final MetadataRepresentation representation = new AttributeSetMetadataRepresentation(attributes);
        return Arrays.asList(representation);
    }

}
