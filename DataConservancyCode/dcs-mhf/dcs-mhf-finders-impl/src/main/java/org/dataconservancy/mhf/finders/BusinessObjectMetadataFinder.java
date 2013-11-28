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
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.instances.AttributeSetMetadataInstance;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;
import org.dataconservancy.mhf.representation.api.MetadataAttributeName;
import org.dataconservancy.mhf.representation.api.MetadataAttributeSetName;
import org.dataconservancy.mhf.representation.api.MetadataAttributeType;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.ui.model.BusinessObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Base class for discovering metadata in {@code BusinessObject}s.  All finder implementations that operate on
 * business objects are expected to extend this class.
 * <p/>
 * Implementation notes:<br/>
 * Subclasses should override the {@code findCoreMetadata} and {@code findSystemMetadata} methods.  They should
 * invoke the superclass's implementation, receiving a {@code MetadataAttributeSet}, and then populate the
 * {@code MetadataAttributeSet} in the subclass implementation.
 */
public abstract class BusinessObjectMetadataFinder extends BaseMetadataFinder {

    protected BusinessObjectMetadataFinder(MetadataObjectBuilder builder) {
        super(builder);
    }

    @Override
    public final Collection<MetadataInstance> findMetadata(Object o) {
        checkObjectTypeInternal(o);
        final BusinessObject bo = (BusinessObject) o;

        final java.util.Collection<MetadataInstance> metadataInstanceList = new ArrayList<MetadataInstance>();

        final MetadataAttributeSet coreAttrs = findCoreMetadata(bo);
        final MetadataAttributeSet sysAttrs = findSystemMetadata(bo);

        // Serialize core metadata into an AttributeSetMetadataInstance
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        metadataObjectBuilder.buildAttributeSet(coreAttrs, out);
        metadataInstanceList.add(new AttributeSetMetadataInstance(MetadataFormatId.ATTRIBUTE_SET_METADATA_FORMAT_ID, out.toByteArray()
        ));

        // Serialize system metadata into an AttributeSetMetadataInstance
        out.reset();
        metadataObjectBuilder.buildAttributeSet(sysAttrs, out);
        metadataInstanceList.add(new AttributeSetMetadataInstance(MetadataFormatId.ATTRIBUTE_SET_METADATA_FORMAT_ID, out.toByteArray()
        ));

        return metadataInstanceList;
    }

    protected MetadataAttributeSet findCoreMetadata(BusinessObject bo) {
        final MetadataAttributeSet attrSet = new MetadataAttributeSet(MetadataAttributeSetName.CORE_METADATA);
        if (bo.getId() != null) {
            attrSet.addAttribute(new MetadataAttribute(
                    MetadataAttributeName.BUSINESS_ID, MetadataAttributeType.STRING, bo.getId()));
        }

        // TODO: have a clearer understanding of what business object "core metadata" are, move that metadata
        // into the BusinessObject class, and create the core metadata attribute set here, in its entirety.
        // For now, subclasses should override this method.

        return attrSet;
    }

    protected MetadataAttributeSet findSystemMetadata(BusinessObject bo) {
        final MetadataAttributeSet attrSet = new MetadataAttributeSet(MetadataAttributeSetName.SYSTEM_METADATA);

        // TODO: have a clearer understanding of what business object "system metadata" are
        // TODO: do we move that metadata into the BusinessObject class?
        // For now, subclasses should override this method.

        // for now, return a new, empty, attribute set
        return attrSet;
    }

    protected abstract void checkObjectType(BusinessObject o);

    private void checkObjectTypeInternal(Object o) {
        if (!(o instanceof BusinessObject)) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    String.format(TYPE_ERROR, o.getClass().getName(), BusinessObject.class.getName()));
            throw new MetadataFindingException(iae.getMessage(), iae);
        }
    }

}
