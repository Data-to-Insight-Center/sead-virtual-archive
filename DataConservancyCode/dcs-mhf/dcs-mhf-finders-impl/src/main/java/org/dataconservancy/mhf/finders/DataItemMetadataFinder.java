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
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.MetadataAttributeName;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representation.api.MetadataAttributeType;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.DataItem;

/**
 * Discover expected {@link MetadataInstance}s from a {@link DataItem}.
 */
public class DataItemMetadataFinder extends BusinessObjectMetadataFinder {

    public DataItemMetadataFinder(MetadataObjectBuilder metadataObjectBuilder) {
        super(metadataObjectBuilder);
    }

    @Override
    protected MetadataAttributeSet findCoreMetadata(BusinessObject bo) {
        checkObjectType(bo);
        final DataItem dataItem = (DataItem) bo;
        final MetadataAttributeSet attributeSet = super.findCoreMetadata(dataItem);
        attributeSet.setName(MetadataAttributeSetName.DATAITEM_CORE_METADATA);

        if (dataItem.getName() != null) {
            attributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.TITLE, dataItem.getName().getClass().getSimpleName(), dataItem.getName()));
        }

        if (dataItem.getDescription() != null) {
            attributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.DESCRIPTION, dataItem.getDescription().getClass().getSimpleName(), dataItem.getDescription()));
        }

        //TODO: should be removed, as business id is already set by the super class's method.
        if (dataItem.getId() != null) {
            attributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.BUSINESS_ID, MetadataAttributeType.STRING, dataItem.getId()));
        }

        return attributeSet;
    }

    @Override
    protected MetadataAttributeSet findSystemMetadata(BusinessObject bo) {
        checkObjectType(bo);
        final DataItem dataItem = (DataItem) bo;
        final MetadataAttributeSet attributeSet = super.findSystemMetadata(dataItem);

        if (dataItem.getDepositDate() != null) {
            attributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.DEPOSIT_DATE,
                    dataItem.getDepositDate().getClass().getSimpleName(),
                    String.valueOf(dataItem.getDepositDate())));
        }

        //retrieve depositor id
        if (dataItem.getDepositorId() != null) {
            attributeSet.addAttribute(new MetadataAttribute(MetadataAttributeName.DEPOSITOR_ID,
                    dataItem.getDepositorId().getClass().getSimpleName(),
                    dataItem.getDepositorId()));
        }

        return attributeSet;
    }

    @Override
    protected void checkObjectType(BusinessObject o) {
        if (! (o instanceof DataItem)) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    String.format(TYPE_ERROR, o.getClass().getName(), DataItem.class.getName()));
            throw new MetadataFindingException(iae.getMessage(), iae);
        }
    }

}
