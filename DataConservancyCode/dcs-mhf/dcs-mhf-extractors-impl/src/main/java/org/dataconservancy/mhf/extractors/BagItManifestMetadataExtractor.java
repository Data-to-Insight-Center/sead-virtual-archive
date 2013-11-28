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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.mhf.extractor.api.ExtractionException;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representation.api.MetadataAttributeType;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;
import org.dataconservancy.mhf.representations.AttributeSetMetadataRepresentation;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.packaging.model.impl.ChecksumImpl;
import org.dataconservancy.packaging.model.impl.Pair;

public class BagItManifestMetadataExtractor extends BaseMetadataExtractor {

    private static final String MANIFEST_ENTRY = "Manifest-Entry";
    private String checksumAlgorithm;

    /**
     * Default constructor - checksumAlgorithm and payload params to be set by
     * the caller.
     * 
     * @param checksumAlgorithm
     * @param payload
     */
    public BagItManifestMetadataExtractor(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<MetadataRepresentation> extractMetadata(
            MetadataInstance instance) throws ExtractionException {
        Collection<MetadataRepresentation> result = new ArrayList<MetadataRepresentation>();

        if (instance == null) {
            throw new IllegalArgumentException(NULL_INSTANCE);
        }

        if (!instance.getFormatId().equals(
                MetadataFormatId.BAGIT_MANIFEST_FORMAT_ID)) {
            throw new IllegalArgumentException(INVALID_FORMAT_ERROR);
        }

        InputStream inputStream = null;

        try {
            inputStream = instance.getContent();
            List<Pair<String, ChecksumImpl>> parsedData = parseData(inputStream);
            Collection<AttributeSet> attributeSets = getAttributeSets(parsedData);

            for (AttributeSet as : attributeSets) {
                result.add(new AttributeSetMetadataRepresentation(as));
            }
        } catch (IOException e) {
            throw new ExtractionException(FAILED_INPUTSTREAM_READING
                    + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new ExtractionException(FAILED_CLOSE + e.getMessage());
                }
            }
        }

        return result;
    }

    /**
     * Creates MetadataAttribute objects which then get added to MetadataAttributeSet and returned as a collection.
     * 
     * @param parsedData
     * @return Collection<AttributeSet>
     */
    protected Collection<AttributeSet> getAttributeSets(List<Pair<String, ChecksumImpl>> parsedData) {
        Collection<AttributeSet> result = new ArrayList<AttributeSet>();

        MetadataAttributeSet manifest = new MetadataAttributeSet(
                MetadataAttributeSetName.BAGIT_METADATA);

        for (Pair<String, ChecksumImpl> pair : parsedData) {
            MetadataAttribute attribute = new MetadataAttribute(MANIFEST_ENTRY, MetadataAttributeType.PAIR,
                    pair.toString());
            manifest.addAttribute(attribute);
        }

        result.add(manifest);
        return result;
    }

    /**
     * Parses the data that is in BagIt manifest format"
     * 
     * @param is
     * @return Map<String, Pair<String, ChecksumImpl>>
     */
    protected List<Pair<String, ChecksumImpl>> parseData(InputStream is) {
        List<Pair<String, ChecksumImpl>> result = new ArrayList<Pair<String, ChecksumImpl>>();

        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(is,
                    "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) {
                int i = findFirstWhiteSpace(line);

                if (i == -1) {
                    throw new ExtractionException("Malformed line contains no whitespace");
                }

                String checksum = line.substring(0, i);

                if (i == line.length() - 1) {
                    throw new ExtractionException(
                            "Malformed line does not contain filename");
                }

                String fullpath = line.substring(i).trim();
                
                result.add(new Pair<String, ChecksumImpl>(fullpath, new ChecksumImpl(getChecksumAlgorithm(),
                        checksum)));
            }

        } catch (IOException e) {
            throw new ExtractionException(FAILED_INPUTSTREAM_READING
                    + e.getMessage());
        }

        return result;
    }

    private int findFirstWhiteSpace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @return the checksumAlgorithm
     */
    public String getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    /**
     * @param checksumAlgorithm
     *            the checksumAlgorithm to set
     */
    public void setChecksumAlgorithm(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }

}
