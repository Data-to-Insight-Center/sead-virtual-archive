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

package org.dataconservancy.mhf.instance.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Contains a list of Metadata Format IDs expected to be handled by Metadata Handling Framework
 */
public class MetadataFormatId {

    // The commented out sections are there just in case we wanted to have type safe MetadataFormatIds instead of
    // just Strings; but we don't need that right now, so they are just commented out.

    /*
      format ids for data files
     */
    public static final String JPG_FORMAT_ID = "dataconservancy.org:formats:file:jpg";
//    public static final MetadataFormatId JPG_FORMAT = new MetadataFormatId(JPG_FORMAT_ID);

    public static final String JPEG_FORMAT_ID = "dataconservancy.org:formats:file:jpeg";
//    public static final MetadataFormatId JPEG_FORMAT = new MetadataFormatId(JPEG_FORMAT_ID);

    public static final String TIF_FORMAT_ID = "dataconservancy.org:formats:file:tif";
//    public static final MetadataFormatId TIF_FORMAT = new MetadataFormatId(TIF_FORMAT_ID);

    public static final String TIFF_FORMAT_ID = "dataconservancy.org:formats:file:tiff";
//    public static final MetadataFormatId TIFF_FORMAT = new MetadataFormatId(TIFF_FORMAT_ID);

    public static final String PNG_FORMAT_ID = "dataconservancy.org:formats:file:png";
//    public static final MetadataFormatId PNG_FORMAT = new MetadataFormatId(PNG_FORMAT_ID);

    /*
     format ids for metadata files
     */
    public static final String FGDC_XML_FORMAT_ID = "dataconservancy.org:formats:file:metadata:fgdc:xml";
//    public static final MetadataFormatId FGDC_XML_FORMAT = new MetadataFormatId(FGDC_XML_FORMAT_ID);

    public static final String FGDC_PLAIN_TEXT_FORMAT_ID = "dataconservancy.org:formats:file:metadata:fgdc:txt";
//    public static final MetadataFormatId FGDC_PLAIN_TEXT_FORMAT = new MetadataFormatId(FGDC_PLAIN_TEXT_FORMAT_ID);

    /*
      format ids for XSD schema files.  that is, the schema used to validate schema instance documents.
     */
    public static final String XSD_XML_FORMAT_ID = "dataconservancy.org:formats:file:xsd:2004";

    /*
     format id(s) for attribute sets
     */
    public static final String ATTRIBUTE_SET_METADATA_FORMAT_ID =
            "dataconservancy.org:formats:attributeset-metadataformat:1.0";
//    public static final MetadataFormatId ATTRIBUTE_SET_METADATA_FORMAT =
//            new MetadataFormatId(ATTRIBUTE_SET_METADATA_FORMAT_ID);

    public static final String ATTRIBUTE_SETS_METADATA_FORMAT_ID =
            "dataconservancy.org:formats:attributeset-metadataformat:multiple:1.0";

    /*
      format ids for BagIt tag files
     */
    public static final String BAGIT_TAG_FORMAT_ID = "dataconservancy.org:formats:files:metadata:bagit:tag";

    public static final String BAGIT_MANIFEST_FORMAT_ID = "dataconservancy.org:formats:file:metadata:bagit:manifest";

    public static final String BAGIT_FETCH_FORMAT_ID = "dataconservancy.org:formats:file:metadata:bagit:fetch";

    public static Collection<String> FORMAT_IDS = new HashSet<String>();

//    public static Collection<MetadataFormatId> FORMATS = new HashSet<MetadataFormatId>();

    static {
        FORMAT_IDS.add(JPG_FORMAT_ID);
        FORMAT_IDS.add(JPEG_FORMAT_ID);
        FORMAT_IDS.add(TIF_FORMAT_ID);
        FORMAT_IDS.add(TIFF_FORMAT_ID);
        FORMAT_IDS.add(PNG_FORMAT_ID);
        FORMAT_IDS.add(FGDC_XML_FORMAT_ID);
        FORMAT_IDS.add(FGDC_PLAIN_TEXT_FORMAT_ID);
        FORMAT_IDS.add(XSD_XML_FORMAT_ID);
        FORMAT_IDS.add(BAGIT_TAG_FORMAT_ID);
        FORMAT_IDS.add(BAGIT_MANIFEST_FORMAT_ID);
        FORMAT_IDS.add(BAGIT_FETCH_FORMAT_ID);

        FORMAT_IDS = Collections.unmodifiableCollection(FORMAT_IDS);

//        FORMATS.add(JPG_FORMAT);
//        FORMATS.add(JPEG_FORMAT);
//        FORMATS.add(TIF_FORMAT);
//        FORMATS.add(TIFF_FORMAT);
//        FORMATS.add(PNG_FORMAT);
//        FORMATS.add(FGDC_XML_FORMAT);
//        FORMATS.add(FGDC_PLAIN_TEXT_FORMAT);


//        FORMATS = Collections.unmodifiableCollection(FORMATS);
    }


//    private String idString;
//
//    private MetadataFormatId(String idString) {
//        this.idString = idString;
//    }
//
//    public String getId() {
//        return idString;
//    }


}
