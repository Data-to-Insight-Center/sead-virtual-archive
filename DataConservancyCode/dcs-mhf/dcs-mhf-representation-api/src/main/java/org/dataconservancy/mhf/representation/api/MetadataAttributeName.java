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

package org.dataconservancy.mhf.representation.api;

/**
 * Contains a list of Metadata attribute names anticipated by the system.
 */
public class MetadataAttributeName {

    /****************************************************************************
     * common attributes
     ****************************************************************************/
    public static final String KEYWORD = "keyword";

    /****************************************************************************
     * fgdc attributes
     ****************************************************************************/
    public static final String DATA_SET_GROUND_CONDITION_TIME = "ground.condition";
    public static final String PUBLICATION_DATE = "publication.date";
    public static final String SPATIAL_DOMAIN = "spatial.domain";
    public static final String KEYWORD_THESAURUS = "keyword.thesaurus";


    /****************************************************************************
     * system related attributes
     ****************************************************************************/
    public static final String DEPOSIT_DATE = "system.deposit.date";
    public static final String DEPOSITOR_ID = "system.depositor";

    /****************************************************************************
     * bo attributes
     ****************************************************************************/
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String ALTERNATE_ID = "alternate.id";
    public static final String CREATOR = "creator";
    public static final String CITABLE_LOCATOR = "citable.locator";
    public static final String BUSINESS_ID = "business.id";
    public static final String FILE_FORMAT = "file.format";
    public static final String START_DATE = "start.date";
    public static final String END_DATE = "end.date";
    public static final String AWARD_NUMER = "award.number";


    /****************************************************************************
     * exif attributes
     ****************************************************************************/
    public static final String ORIGINAL_DATETIME = "datetime.original";
    public static final String DIGITIZED_DATETIME = "datetime.digitized";
    public static final String CHANGE_DATETIME = "datetime.changed";
    public static final String COPY_RIGHT_NOTE = "copyright";

    public static final String GPS_DATETIME = "gps.datetime";
    public static final String GPS_ALTITUDE = "gps.altitude";
    public static final String GPS_LOCATION= "gps.location";
    public static final String GPS_MAP_DATUM = "gps.map.datum";


}
