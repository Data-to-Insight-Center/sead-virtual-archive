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
import java.util.HashSet;
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

/**
 * Produce a BAGIT_ATTRIBUTES and a BAGIT_PROFILE_DATACONS_ATTRIBUTES from a
 * BagIT tag file (bagit.txt or bag-info.txt).
 */
public class BagItTagMetadataExtractor extends BaseMetadataExtractor {
    private static final String BAGIT_VERSION = "BagIt-Version";
    private static final String TAG_FILE_CHARACTER_ENCODING = "Tag-File-Character-Encoding";
    private static final String CONTACT_NAME = "Contact-Name";
    private static final String CONTACT_PHONE = "Contact-Phone";
    private static final String CONTACT_EMAIL = "Contact-Email";
    private static final String BAG_SIZE = "Bag-Size";
    private static final String PAYLOAD_OXUM = "Payload-Oxum";
    private static final String EXTERNAL_IDENTIFIER = "External-Identifier";
    private static final String BAG_COUNT = "Bag-Count";
    private static final String BAG_GROUP_IDENTIFIER = "Bag-Group-Identifier";
    private static final String BAGGING_DATE = "Bagging-Date";
    private static final String BAGIT_PROFILE_IDENTIFIER = "BagIt-Profile-Identifier";
    private static final String PKG_BAG_DIR = "PKG-BAG-DIR";
    private static final String PKG_ORE_REM = "PKG-ORE-REM";

    private static final Collection<String> BAGIT_PROFILE_DATACONS_ATTRIBUTES = new HashSet<String>();
    private static final Collection<String> BAGIT_ATTRIBUTES = new HashSet<String>();

    static {
        BAGIT_ATTRIBUTES.add(BAGGING_DATE);
        BAGIT_ATTRIBUTES.add(BAG_GROUP_IDENTIFIER);
        BAGIT_ATTRIBUTES.add(BAG_COUNT);
        BAGIT_ATTRIBUTES.add(EXTERNAL_IDENTIFIER);
        BAGIT_ATTRIBUTES.add(PAYLOAD_OXUM);
        BAGIT_ATTRIBUTES.add(BAG_SIZE);
        BAGIT_ATTRIBUTES.add(CONTACT_EMAIL);
        BAGIT_ATTRIBUTES.add(CONTACT_PHONE);
        BAGIT_ATTRIBUTES.add(CONTACT_NAME);
        BAGIT_ATTRIBUTES.add(BAGIT_VERSION);
        BAGIT_ATTRIBUTES.add(TAG_FILE_CHARACTER_ENCODING);

        BAGIT_PROFILE_DATACONS_ATTRIBUTES.add(BAGIT_PROFILE_IDENTIFIER);
        BAGIT_PROFILE_DATACONS_ATTRIBUTES.add(PKG_BAG_DIR);
        BAGIT_PROFILE_DATACONS_ATTRIBUTES.add(PKG_ORE_REM);
    }

    /**
     * BagIt tag files are in two formats
     * 
     * @param mi
     *            {@link org.dataconservancy.mhf.instance.api.MetadataInstance}
     *            and BagIt tag metadata instance expressed in the form of
     *            key-value pairs text {@link java.io.InputStream}
     * @return a collection
     *         {@link org.dataconservancy.mhf.representation.api.MetadataRepresentation}
     *         extracted from the
     *         {@link org.dataconservancy.mhf.instance.api.MetadataInstance}
     * @throws org.dataconservancy.mhf.extractor.api.ExtractionException
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Collection<MetadataRepresentation> extractMetadata(
            MetadataInstance mi) throws ExtractionException {

        Collection<MetadataRepresentation> result = new ArrayList<MetadataRepresentation>();

        if (mi == null) {
            throw new IllegalArgumentException(NULL_INSTANCE);
        }

        if (!mi.getFormatId().equals(MetadataFormatId.BAGIT_TAG_FORMAT_ID)) {
            throw new IllegalArgumentException(INVALID_FORMAT_ERROR);
        }

        InputStream content = null;

        try {
            content = mi.getContent();

            Collection<AttributeSet> sets = asAttributeSets(parseTags(content));

            for (AttributeSet set : sets) {
                result.add(new AttributeSetMetadataRepresentation(set));
            }

            return result;
        } catch (IOException e) {
            throw new ExtractionException(FAILED_INPUTSTREAM_READING
                    + e.getMessage());
        } finally {
            try {
                if (content != null) {
                    content.close();
                }
            } catch (IOException e) {
                throw new ExtractionException(FAILED_CLOSE + e.getMessage());
            }
        }
    }

    /**
     * Parse a BagIt tags file into key,(value+) pairs.
     * 
     * @param is
     * @return
     * @throws IOException
     */
    protected Map<String, List<String>> parseTags(InputStream is)
            throws IOException {
        Map<String, List<String>> result = new HashMap<String, List<String>>();

        BufferedReader r = new BufferedReader(
                new InputStreamReader(is, "UTF-8"));
        String last_name = null;
        String line;

        while ((line = r.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }

            if (Character.isWhitespace(line.charAt(0))) {
                // Continuing value from last line.

                if (last_name == null) {
                    throw new ExtractionException(
                            "BagIt tag file trying to continue non-existent value.");
                }

                // Append to last value

                List<String> values = result.get(last_name);

                String value = values.get(values.size() - 1);
                value = value + " " + line.trim();
                value = value.trim();
                
                values.set(values.size() - 1, value);
            } else {
                // New value

                int i = line.indexOf(':');

                if (i == -1) {
                    throw new ExtractionException(
                            "BagIt tag file missing colon.");
                } else {
                    String name = line.substring(0, i).trim();
                    String value = i == line.length() ? "" : line.substring(
                            i + 1).trim();

                    last_name = name;

                    add(result, name, value);
                }
            }
        }

        return result;
    }

    private void add(Map<String, List<String>> data, String name, String value) {
        List<String> values = data.get(name);

        if (values == null) {
            values = new ArrayList<String>();
            data.put(name, values);
        }

        values.add(value);
    }

    /**
     * Convert name,(value+) pairs from a tag file to AttributeSets.
     * 
     * @param data
     * @return
     */
    private Collection<AttributeSet> asAttributeSets(
            Map<String, List<String>> data) {
        Collection<AttributeSet> result = new HashSet<AttributeSet>();

        MetadataAttributeSet bagit = new MetadataAttributeSet(
                MetadataAttributeSetName.BAGIT_METADATA);
        MetadataAttributeSet bagit_dc = new MetadataAttributeSet(
                MetadataAttributeSetName.BAGIT_PROFILE_DATACONS_METADATA);

        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            String name = entry.getKey();
            List<String> values = entry.getValue();

            List<MetadataAttribute> attrs = new ArrayList<MetadataAttribute>();

            for (String value : values) {
                MetadataAttribute attr = new MetadataAttribute(name,
                        MetadataAttributeType.STRING, value);
                attrs.add(attr);
            }

            // TODO Have to actually test profile value?
            
            if (BAGIT_PROFILE_DATACONS_ATTRIBUTES.contains(name)) {
                for (MetadataAttribute attr : attrs) {
                    bagit_dc.addAttribute(attr);
                }
            } else if (BAGIT_ATTRIBUTES.contains(name)) {
                for (MetadataAttribute attr : attrs) {
                    bagit.addAttribute(attr);
                }
            } else {
                // Ignore if not part of a attribute set profile
            }
        }

        result.add(bagit);
        result.add(bagit_dc);

        return result;
    }
}
