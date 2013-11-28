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

package org.dataconservancy.mhf.finders;


import org.dataconservancy.mhf.finder.api.MetadataFindingException;
import org.dataconservancy.mhf.model.builder.api.AttributeValueBuilder;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.PersonName;

import java.io.ByteArrayOutputStream;

import static org.dataconservancy.mhf.representation.api.MetadataAttributeName.*;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.DATE_TIME;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.PERSON_NAME;
import static org.dataconservancy.mhf.representation.api.MetadataAttributeType.STRING;

/**
 * Responsible for finding Business Object metadata on a {@code org.dataconservancy.ui.model.Collection}.
 * <p/>
 * This implementation looks for properties of the {@code Collection}, such as its title, description, publication date,
 * etc.  It is not responsible for discovering metadata on {@code MetadataFile} objects referenced by the
 * {@code Collection}.
 */
public class CollectionMetadataFinder extends BusinessObjectMetadataFinder {

    private AttributeValueBuilder attributeValueBuilder;

    public CollectionMetadataFinder(MetadataObjectBuilder metadataObjectBuilder, AttributeValueBuilder attributeValueBuilder) {
        super(metadataObjectBuilder);
        this.attributeValueBuilder = attributeValueBuilder;
    }

    @Override
    protected MetadataAttributeSet findCoreMetadata(BusinessObject o) {
        checkObjectType(o);
        Collection collection = (Collection) o;

        final MetadataAttributeSet attributeSet = super.findCoreMetadata(collection);
        attributeSet.setName(MetadataAttributeSetName.COLLECTION_CORE_METADATA);

        if (collection.getTitle() != null) {
            attributeSet.addAttribute(new MetadataAttribute(TITLE, STRING, collection.getTitle()));
        }

        if (collection.getSummary() != null) {
            attributeSet.addAttribute(new MetadataAttribute(DESCRIPTION, STRING, collection.getSummary()));
        }

        if (collection.getPublicationDate() != null) {
            attributeSet.addAttribute(new MetadataAttribute(PUBLICATION_DATE, DATE_TIME,
                    String.valueOf(collection.getPublicationDate())));
        }

        for (String alternateId : collection.getAlternateIds()) {
            attributeSet.addAttribute(new MetadataAttribute(ALTERNATE_ID, STRING, alternateId));
        }

        ByteArrayOutputStream baos;
        for (PersonName creator : collection.getCreators()) {
            baos = new ByteArrayOutputStream();
            attributeValueBuilder.buildPersonName(creator, baos);
            attributeSet.addAttribute(new MetadataAttribute(CREATOR, PERSON_NAME, baos.toString()));
        }

        if (collection.getCitableLocator() != null) {
            attributeSet.addAttribute(new MetadataAttribute(CITABLE_LOCATOR, STRING, collection.getCitableLocator()));
        }

        return attributeSet;
    }

    /**
     * Finds system related metadata
     * @param o the Collection
     * @return A MetadataInstance object contains information found in the bo.
     * @return null if no relevant metadata is found
     * @throws java.io.IOException
     */
    @Override
    protected MetadataAttributeSet findSystemMetadata(BusinessObject o) {
        checkObjectType(o);
        Collection collection = (Collection) o;

        final MetadataAttributeSet attributeSet = super.findSystemMetadata(collection);

        if (collection.getDepositDate() != null) {
            attributeSet.addAttribute(new MetadataAttribute(DEPOSIT_DATE, DATE_TIME, String.valueOf(collection.getDepositDate())));
        }

        //retrieve depositor id
        if (collection.getDepositorId() != null) {
            attributeSet.addAttribute(new MetadataAttribute(DEPOSITOR_ID, STRING, collection.getDepositorId()));
        }

        return attributeSet;
    }

    /**
     * Formats a PersonName object into a String.
     *
     * @param name the PersonName
     * @return a String representing the PersonName, may return {@code null}
     */
    private String formatPersonName(PersonName name) {
//        private String [] givenNames;
//        private String [] familyNames;
//        private String prefixes;
//        private String suffixes;
//        private String [] middleNames;

        final StringBuilder sb = new StringBuilder();

        sb.append((name.getPrefixes() == null ? "" : name.getPrefixes() + " "));
        sb.append((name.getGivenNames() == null ? "" : name.getGivenNames() + " "));
        sb.append((name.getMiddleNames() == null ? "" : name.getMiddleNames() + " "));
        sb.append((name.getFamilyNames() == null ? "" : name.getFamilyNames() + " "));
        sb.append((name.getSuffixes() == null ? "" : name.getSuffixes()));

        String nameStr = sb.toString();
        if (nameStr != null) {
            return nameStr.trim();
        }

        return null;
    }

    @Override
    protected void checkObjectType(BusinessObject o) {
        if (! (o instanceof Collection)) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    String.format(TYPE_ERROR, o.getClass().getName(), Collection.class.getName()));
            throw new MetadataFindingException(iae.getMessage(), iae);
        }
    }

}
