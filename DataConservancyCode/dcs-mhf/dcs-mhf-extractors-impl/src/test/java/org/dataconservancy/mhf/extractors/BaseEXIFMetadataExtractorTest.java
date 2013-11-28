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

import junit.framework.Assert;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;

import java.util.Collection;

public class BaseEXIFMetadataExtractorTest {

    Collection<MetadataRepresentation> extractedMetadataWithSpatialTemporal;
    Collection<MetadataRepresentation> extractedMetadataWithKeywordCopyright;
    Collection<MetadataRepresentation> extractedMetadataWithAllExtractable;

    protected void testTemporalMetadataExtracted() {
        AttributeSet temporalAttributeSet = null;
        for (MetadataRepresentation representation : extractedMetadataWithSpatialTemporal) {
            AttributeSet attributeSet = (AttributeSet) representation.getRepresentation();
            if (attributeSet.getName().equals(MetadataAttributeSetName.TEMPORAL_METADATA)) {
                temporalAttributeSet = attributeSet;
                break;
            }
        }
        Assert.assertNotNull(temporalAttributeSet);
    }

    protected void testSpatialMetadataExtracted() {
        AttributeSet spatialAttributeSet = null;
        for (MetadataRepresentation representation : extractedMetadataWithSpatialTemporal) {
            AttributeSet attributeSet = (AttributeSet) representation.getRepresentation();
            if (attributeSet.getName().equals(MetadataAttributeSetName.SPATIAL_METADATA)) {
                spatialAttributeSet = attributeSet;
                break;
            }
        }
        Assert.assertNotNull(spatialAttributeSet);
    }

    protected void testNoCopyrightMetadataExtracted() {
        AttributeSet copyrightAttributeSet = null;
        for (MetadataRepresentation representation : extractedMetadataWithSpatialTemporal) {
            AttributeSet attributeSet = (AttributeSet) representation.getRepresentation();
            if (attributeSet.getName().equals(MetadataAttributeSetName.COPY_RIGHT_METADATA)) {
                copyrightAttributeSet = attributeSet;
                break;
            }
        }
        Assert.assertNull(copyrightAttributeSet);
    }

    protected void testNoKeywordMetadataExtracted() {
        AttributeSet keywordAttributeSet = null;
        for (MetadataRepresentation representation : extractedMetadataWithSpatialTemporal) {
            AttributeSet attributeSet = (AttributeSet) representation.getRepresentation();
            if (attributeSet.getName().equals(MetadataAttributeSetName.KEYWORD_METADATA)) {
                keywordAttributeSet = attributeSet;
                break;
            }
        }
        Assert.assertNull(keywordAttributeSet);
    }

    protected void testNoTemporalMetadataExtracted() {
        AttributeSet temporalAttributeSet = null;
        for (MetadataRepresentation representation : extractedMetadataWithKeywordCopyright) {
            AttributeSet attributeSet = (AttributeSet) representation.getRepresentation();
            if (attributeSet.getName().equals(MetadataAttributeSetName.TEMPORAL_METADATA)) {
                temporalAttributeSet = attributeSet;
                break;
            }
        }
        Assert.assertNull(temporalAttributeSet);
    }

    protected void testNoSpatialMetadataExtracted() {
        AttributeSet temporalAttributeSet = null;
        for (MetadataRepresentation representation : extractedMetadataWithKeywordCopyright) {
            AttributeSet attributeSet = (AttributeSet) representation.getRepresentation();
            if (attributeSet.getName().equals(MetadataAttributeSetName.SPATIAL_METADATA)) {
                temporalAttributeSet = attributeSet;
                break;
            }
        }
        Assert.assertNull(temporalAttributeSet);
    }

    protected void testCopyrightMetadataExtracted() {
        AttributeSet copyrightAttributeSet = null;
        for (MetadataRepresentation representation : extractedMetadataWithKeywordCopyright) {
            AttributeSet attributeSet = (AttributeSet) representation.getRepresentation();
            if (attributeSet.getName().equals(MetadataAttributeSetName.COPY_RIGHT_METADATA)) {
                copyrightAttributeSet = attributeSet;
                break;
            }
        }
        Assert.assertNotNull(copyrightAttributeSet);
    }


    protected void testKeywordMetadataExtracted() {
        AttributeSet keywordAttributeSet = null;
        for (MetadataRepresentation representation : extractedMetadataWithKeywordCopyright) {
            AttributeSet attributeSet = (AttributeSet) representation.getRepresentation();
            if (attributeSet.getName().equals(MetadataAttributeSetName.KEYWORD_METADATA)) {
                keywordAttributeSet = attributeSet;
                break;
            }
        }
        Assert.assertNotNull(keywordAttributeSet);
    }
    protected void testAllExtractableExtracted() {
        /*
        for (MetadataRepresentation representation : extractedMetadataWithAllExtractable) {
            AttributeSet attributeSet = (AttributeSet) representation.getRepresentation();
            System.out.println(attributeSet);
        }*/
        AttributeSet keywordAttributeSet = null;
        AttributeSet copyrightAttributeSet = null;
        AttributeSet spatialAttributeSet = null;
        AttributeSet temporalAttributeSet = null;
        for (MetadataRepresentation representation : extractedMetadataWithAllExtractable) {
            AttributeSet attributeSet = (AttributeSet) representation.getRepresentation();
            if (attributeSet.getName().equals(MetadataAttributeSetName.KEYWORD_METADATA)) {
                keywordAttributeSet = attributeSet;
            } else if (attributeSet.getName().equals(MetadataAttributeSetName.COPY_RIGHT_METADATA)) {
                copyrightAttributeSet = attributeSet;
            } else if (attributeSet.getName().equals(MetadataAttributeSetName.SPATIAL_METADATA)) {
                spatialAttributeSet = attributeSet;
            } else if (attributeSet.getName().equals(MetadataAttributeSetName.TEMPORAL_METADATA)) {
                temporalAttributeSet = attributeSet;
            }
        }

        Assert.assertNotNull(keywordAttributeSet);
        Assert.assertNotNull(copyrightAttributeSet);
        Assert.assertNotNull(temporalAttributeSet);
        Assert.assertNotNull(spatialAttributeSet);
    }

}
