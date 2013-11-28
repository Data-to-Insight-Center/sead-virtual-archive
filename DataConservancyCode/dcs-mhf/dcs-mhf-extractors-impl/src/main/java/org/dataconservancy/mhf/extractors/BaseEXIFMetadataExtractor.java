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

import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import org.dataconservancy.mhf.extractor.api.ExtractionException;
import org.dataconservancy.mhf.model.builder.api.AttributeValueBuilder;
import org.dataconservancy.mhf.representation.api.*;
import org.dataconservancy.mhf.representations.AttributeSetMetadataRepresentation;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.mhf.representations.SimpleAltitudeImpl;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: hanh
 * Date: 2/15/13
 * Time: 5:29 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseEXIFMetadataExtractor extends BaseMetadataExtractor {

    protected final AttributeValueBuilder avBuilder;

    protected BaseEXIFMetadataExtractor(AttributeValueBuilder avBuilder)
    {
        if (avBuilder == null) {
            throw new IllegalArgumentException("Builders must not be null.");
        }

        this.avBuilder = avBuilder;
    }

    protected Collection<MetadataRepresentation> extractMetadata(Metadata metadata) throws ExtractionException {

        Collection <MetadataRepresentation> extractedMetadataRepresentation = new ArrayList<MetadataRepresentation>();

        if (metadata != null) {
            AttributeSet keywordAttributeSet = extractKeywordMetadata(metadata);
            AttributeSet copyrightAttributeSet = extractCopyrightMetadata(metadata);
            AttributeSet temporalAttributeSet = extractTemporalMetadata(metadata);
            AttributeSet spatialAttributeSet = extractSpatialMetadata(metadata);
            if (keywordAttributeSet != null) {
                extractedMetadataRepresentation.add(new AttributeSetMetadataRepresentation(keywordAttributeSet));
            }
            if (copyrightAttributeSet != null) {
                extractedMetadataRepresentation.add(new AttributeSetMetadataRepresentation(copyrightAttributeSet));
            }
            if (temporalAttributeSet != null) {
                extractedMetadataRepresentation.add(new AttributeSetMetadataRepresentation(temporalAttributeSet));
            }
            if (spatialAttributeSet != null) {
                extractedMetadataRepresentation.add(new AttributeSetMetadataRepresentation(spatialAttributeSet));
            }
        }

        return extractedMetadataRepresentation;
    }

    /**
     * From the extracted metadata object, read {@code UserComment} and {@code ImageDescription} fields and put the
     * information into a Keyword {@link MetadataAttributeSet}.
     * @param fileMetadata
     * @return {@link AttributeSet} containing keyword attribute representation of {@code UserComment}
     *         and {@code ImageDescription}
     * @return {@code null} is no {@code UserComment} or {@code ImageDescription} is found.
     */
    protected AttributeSet extractKeywordMetadata(Metadata fileMetadata) {

        AttributeSet keywordAttributeSet = new MetadataAttributeSet(MetadataAttributeSetName.KEYWORD_METADATA);
        ExifSubIFDDirectory exifSubIFDDirectory = fileMetadata.getDirectory(ExifSubIFDDirectory.class);
        ExifIFD0Directory exifIFD0Directory = fileMetadata.getDirectory(ExifIFD0Directory.class);

        Attribute tempKeywordAttribute;
        //extract user comment for keywords
        if (exifSubIFDDirectory != null) {
            String userComment = exifSubIFDDirectory.getDescription(ExifSubIFDDirectory.TAG_USER_COMMENT);
            if (userComment != null && !userComment.isEmpty()) {
                tempKeywordAttribute = new MetadataAttribute(MetadataAttributeName.KEYWORD,
                        MetadataAttributeType.STRING,
                        userComment);
                keywordAttributeSet.getAttributes().add(tempKeywordAttribute);
            }
        }

        //extract image description for keywords
        String imageDescription = exifIFD0Directory.getString(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION);
        if (imageDescription != null && !imageDescription.isEmpty()) {
            tempKeywordAttribute = new MetadataAttribute(MetadataAttributeName.KEYWORD,
                    MetadataAttributeType.STRING,
                    imageDescription);
            keywordAttributeSet.getAttributes().add(tempKeywordAttribute);
        }

        if (keywordAttributeSet.getAttributes().size() > 0) {
            return keywordAttributeSet;
        } else  {
            return null;
        }
    }

    /**
     * From the extracted metadata object, retrieve information about the file copyright and put it into a Copyright.
     * <p/>
     * Field being read is: {@code Copyright}
     *
     * {@link AttributeSet}
     * @param fileMetadata
     * @return An {@link AttributeSet} containing copyright attribute
     * @return {@code null} when no copyright information was found
     */
    protected AttributeSet extractCopyrightMetadata(Metadata fileMetadata) {
        AttributeSet copyrightAttributeSet = null;

        ExifIFD0Directory exifIFD0Directory = fileMetadata.getDirectory(ExifIFD0Directory.class);
        String copyrightStatement = exifIFD0Directory.getDescription(ExifIFD0Directory.TAG_COPYRIGHT);
        if (copyrightStatement != null) {
            Attribute copyrightAttribute = new MetadataAttribute(MetadataAttributeName.COPY_RIGHT_NOTE,
                    MetadataAttributeType.STRING,
                    copyrightStatement);
            copyrightAttributeSet = new MetadataAttributeSet(MetadataAttributeSetName.COPY_RIGHT_METADATA);
            copyrightAttributeSet.getAttributes().add(copyrightAttribute);
        }

        return copyrightAttributeSet;
    }

    /**
     * From the extracted metadata object, retrieve temporal information and put it in to Temporal {@link AttributeSet}
     * <p/>
     * Temporal fields expected to be retrieved are:
     * <ul>
     *     <li>{@code DateTime}</li>
     *     <li>{@code DateTimeOriginal}</li>
     *     <li>{@code DateTimeDigitized}</li>
     *     <li>{@code GPSDateStamp}</li>
     * </ul>
     * @param fileMetadata
     * @return an {@link AttributeSet} contain temporal information retrieved from the fileMetadata
     * @return {@code null} if no temporal information was found.
     */
    protected AttributeSet extractTemporalMetadata(Metadata fileMetadata) {

        AttributeSet temporalAttributeSet = new MetadataAttributeSet(MetadataAttributeSetName.TEMPORAL_METADATA);
        ExifSubIFDDirectory exifSubIFDDirectory = fileMetadata.getDirectory(ExifSubIFDDirectory.class);
        ExifIFD0Directory exifIFD0Directory = fileMetadata.getDirectory(ExifIFD0Directory.class);
        GpsDirectory gpsDirectory = fileMetadata.getDirectory(GpsDirectory.class);

        Attribute temporalAttribute;
        if (exifSubIFDDirectory != null) {
            if (exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL) != null) {
                DateTime dateTimeOriginal = new DateTime(exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
                if (dateTimeOriginal != null) {
                    temporalAttribute = new MetadataAttribute(MetadataAttributeName.ORIGINAL_DATETIME,
                            MetadataAttributeType.DATE_TIME,
                            String.valueOf(dateTimeOriginal));
                    temporalAttributeSet.getAttributes().add(temporalAttribute);
                }
            }
            if (exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED) != null) {
                DateTime dateTimeDigitized = new DateTime(exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED));
                temporalAttribute = new MetadataAttribute(MetadataAttributeName.DIGITIZED_DATETIME,
                        MetadataAttributeType.DATE_TIME,
                        String.valueOf(dateTimeDigitized));
                temporalAttributeSet.getAttributes().add(temporalAttribute);
            }
        }

        if (exifIFD0Directory != null && exifIFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME) != null) {
            DateTime dateTime = new DateTime(exifIFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME));
            temporalAttribute = new MetadataAttribute(MetadataAttributeName.CHANGE_DATETIME,
                    MetadataAttributeType.DATE_TIME,
                    String.valueOf(dateTime));
            temporalAttributeSet.getAttributes().add(temporalAttribute);
        }

        if (gpsDirectory != null && gpsDirectory.getDate(GpsDirectory.TAG_GPS_DATE_STAMP) != null) {
            DateTime gpsDateTime = new DateTime(gpsDirectory.getDate(GpsDirectory.TAG_GPS_DATE_STAMP));
            temporalAttribute = new MetadataAttribute(MetadataAttributeName.GPS_DATETIME,
                    MetadataAttributeType.DATE_TIME,
                    String.valueOf(gpsDateTime));
            temporalAttributeSet.getAttributes().add(temporalAttribute);
        }

        if (temporalAttributeSet.getAttributes().size() > 0) {
            return temporalAttributeSet;
        } else  {
            return null;
        }
    }

    /**
     * From the extracted metadata object, retrieve spatial information and put it in to Spatial {@link AttributeSet}
     * <p/>
     * Spatial fields expected to be retrieved are:
     * <ul>
     *     <li>{@code GPSLongitude}</li>
     *     <li>{@code GPSLongitudeRef}</li>
     *     <li>{@code GPSLatitude}</li>
     *     <li>{@code GPSLatitudeRef}</li>
     *     <li>{@code GPSAltitude}</li>
     *     <li>{@code GPSAltitudeRef}</li>
     *     <li>{@code GPSMapDatum}</li>
     * </ul>
     * @param fileMetadata
     * @return an {@link AttributeSet} contain temporal information retrieved from the fileMetadata
     * @return {@code null} if no temporal information was found.
     */
    protected AttributeSet extractSpatialMetadata(Metadata fileMetadata) {
        GpsDirectory gpsDirectory = fileMetadata.getDirectory(GpsDirectory.class);
        AttributeSet attributeSet = new MetadataAttributeSet(MetadataAttributeSetName.SPATIAL_METADATA);
        Attribute spatialAttribute;

        if (gpsDirectory != null) {
            Location location = extractLocation(gpsDirectory);
            ByteArrayOutputStream baos;
            if (location != null) {
                 baos = new ByteArrayOutputStream();
                avBuilder.buildLocation(location, baos);
                spatialAttribute = new MetadataAttribute(MetadataAttributeName.GPS_LOCATION,
                        MetadataAttributeType.LOCATION,
                        new String(baos.toByteArray()));
                attributeSet.getAttributes().add(spatialAttribute);
            }
            Rational rat =  gpsDirectory.getRational(GpsDirectory.TAG_GPS_ALTITUDE);
            if (rat != null) {
                double altitudeValue = rat.doubleValue();
                Altitude altitude = new SimpleAltitudeImpl(altitudeValue);
                baos = new ByteArrayOutputStream();
                avBuilder.buildAltitude(altitude, baos);
                spatialAttribute = new MetadataAttribute(MetadataAttributeName.GPS_ALTITUDE,
                        MetadataAttributeType.ALTITUDE,
                        new String(baos.toByteArray()));
                attributeSet.getAttributes().add(spatialAttribute);
            }

            //extract gps datum
            String datum = gpsDirectory.getDescription(GpsDirectory.TAG_GPS_MAP_DATUM);

            if (datum != null) {
                spatialAttribute = new MetadataAttribute(MetadataAttributeName.GPS_MAP_DATUM,
                        MetadataAttributeType.STRING,
                        datum);
                attributeSet.getAttributes().add(spatialAttribute);
            }
        }

        if (attributeSet.getAttributes().size() == 0) {
            return null;
        } else {
            return attributeSet;
        }
    }

    private Location extractLocation(GpsDirectory gpsDirectory) {
        //extract longitude
        Rational[] rats = gpsDirectory.getRationalArray(GpsDirectory.TAG_GPS_LONGITUDE);
        //get longitude and latitude to unreasonable values
        Double longitude = null;
        Double latitude = null;
        Location location = null;
        if (rats != null) {
            longitude = rats[0].doubleValue() + rats[1].doubleValue()/60 + rats[2].doubleValue()/3600;
            String longRef = gpsDirectory.getDescription(GpsDirectory.TAG_GPS_LONGITUDE_REF);
            if (longRef.contains("w") || longRef.contains("W")) {
                longitude = -1*longitude;
            }
        }

        //extract latitude
        rats = gpsDirectory.getRationalArray(GpsDirectory.TAG_GPS_LATITUDE);
        if (rats != null) {
            latitude = rats[0].doubleValue() + rats[1].doubleValue()/60 + rats[2].doubleValue()/3600;
            String latRef = gpsDirectory.getDescription(GpsDirectory.TAG_GPS_LATITUDE_REF);
            if (latRef.contains("s") || latRef.contains("S")) {
                latitude = -1*latitude;
            }
        }

        if (latitude != null && longitude != null) {
            //convert into GQM Location with null SRID
            Point point = new Point(latitude, longitude);
            location = new Location(new Geometry(Geometry.Type.POINT, point), null);
        }
        return location;
    }
}
