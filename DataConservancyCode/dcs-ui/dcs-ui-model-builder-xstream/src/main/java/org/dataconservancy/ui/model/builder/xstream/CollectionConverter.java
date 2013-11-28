/*
 * Copyright 2012 Johns Hopkins University
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

package org.dataconservancy.ui.model.builder.xstream;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.ContactInfo;
import org.dataconservancy.ui.model.PersonName;
import org.joda.time.DateTime;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * CollectionConverter is used by Xstream object builder to serialized and
 * deserialize {@link org.dataconservancy.ui.model.Collection} objects.
 */
public class CollectionConverter
        extends AbstractEntityConverter
        implements ConverterConstants {

    /**
     * The Collection element name
     */

    public CollectionConverter() {
    }


    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        final Collection collection = (Collection) source;
        if (collection != null) {
            if (!isEmptyOrNull(collection.getId())) {
                writer.addAttribute(E_ID, collection.getId());
            }

            List<String> alternateIds = collection.getAlternateIds();
            if (alternateIds != null && !alternateIds.isEmpty()) {
                writer.startNode(E_COL_ALTERNATE_IDS);
                for (String altId : alternateIds) {
                    writer.startNode(E_COL_ALTERNATE_ID);
                    writer.setValue(altId);
                    writer.endNode();
                }
                writer.endNode();
            }

            if (!isEmptyOrNull(collection.getTitle())) {
                writer.startNode(E_COL_TITLE);
                writer.setValue(collection.getTitle());
                writer.endNode();
            }

            if (!isEmptyOrNull(collection.getSummary())) {
                writer.startNode(E_COL_SUMMARY);
                writer.setValue(collection.getSummary());
                writer.endNode();
            }

            if (!isEmptyOrNull(collection.getCitableLocator())) {
                writer.startNode(E_COL_CITABLE_LOCATOR);
                writer.setValue(collection.getCitableLocator());
                writer.endNode();
            }

            List<ContactInfo> contactInfoList = collection.getContactInfoList();
            if (contactInfoList != null && !contactInfoList.isEmpty()) {
                writer.startNode(E_COL_CONTACT_INFOS);
                for (ContactInfo contactInfo : contactInfoList) {
                    if (contactInfo != null) {
                        writer.startNode(E_COL_CONTACT_INFO);
                        context.convertAnother(contactInfo);
                        writer.endNode();
                    }
                }
                writer.endNode();
            }

            List<PersonName> creators = collection.getCreators();
            if (creators != null && !creators.isEmpty()) {
                writer.startNode(E_COL_CREATORS);
                for (PersonName creator : creators) {
                    if (creator != null) {
                        writer.startNode(E_COL_CREATOR);
                        context.convertAnother(creator);
                        writer.endNode();
                    }
                }
                writer.endNode();
            }

            if (collection.getPublicationDate() != null) {
                writer.startNode(E_COL_PUBLICATION_DATE);
                context.convertAnother(collection.getPublicationDate());
                writer.endNode();
            }

            if (collection.getDepositDate() != null) {
                writer.startNode(E_COL_DEPOSIT_DATE);
                context.convertAnother(collection.getDepositDate());
                writer.endNode();
            }

            if (collection.getDepositorId() != null
                    && !isEmptyOrNull(collection.getDepositorId())) {
                writer.startNode(E_COL_DEPOSITOR);
                writer.setValue(collection.getDepositorId());
                writer.endNode();
            }

            List<String> childrenIds = collection.getChildrenIds();
            if (childrenIds != null && !childrenIds.isEmpty()) {
                writer.startNode(E_COL_CHILDREN_IDS);
                for (String childId : childrenIds) {
                    writer.startNode(E_COL_CHILDREN_ID);
                    writer.setValue(childId);
                    writer.endNode();
                }
                writer.endNode();
            }
            
            if (collection.getParentId() != null && !isEmptyOrNull(collection.getParentId())) {
                writer.startNode(E_COL_PARENT_ID);
                writer.setValue(collection.getParentId());
                writer.endNode();
            }
            
            if (collection.getParentProjectId() != null && !isEmptyOrNull(collection.getParentProjectId())) {
                writer.startNode(E_COL_PARENT_PROJECT);
                writer.setValue(collection.getParentProjectId());
                writer.endNode();
            }

        }
    }

    /**
     * @param reader
     * @param context
     * @return collection
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        Collection collection = new Collection();
        DateTime publicationDate = null;
        DateTime depositDate = null;
        collection.setId(reader.getAttribute(E_ID));

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String name = getElementName(reader);

            if (name.equals(E_COL_TITLE)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    collection.setTitle(value.trim());
                }
            }

            else if (name.equals(E_ID)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    collection.setId(value.trim());
                }
            }

            else if (name.equals(E_COL_SUMMARY)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    collection.setSummary(value.trim());
                }
            }

            else if (name.equals(E_COL_CITABLE_LOCATOR)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    collection.setCitableLocator(value.trim());
                }
            }

            else if (name.equals(E_COL_DEPOSITOR)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    collection.setDepositorId(value);
                }
            }

            else if (name.equals(E_COL_PUBLICATION_DATE)) {
                reader.moveDown();
                publicationDate =
                        (DateTime) context.convertAnother(publicationDate,
                                                          DateTime.class);
                collection.setPublicationDate(publicationDate);
                reader.moveUp();
            }

            else if (name.equals(E_COL_DEPOSIT_DATE)) {
                reader.moveDown();
                depositDate =
                        (DateTime) context.convertAnother(depositDate,
                                                          DateTime.class);
                collection.setDepositDate(depositDate);
                reader.moveUp();
            }

            else if (name.equals(E_COL_CONTACT_INFOS)) {
                ContactInfo contactInfo = null;
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_COL_CONTACT_INFO)) {
                        contactInfo =
                                (ContactInfo) context
                                        .convertAnother(contactInfo,
                                                        ContactInfo.class);
                        collection.addContactInfo(contactInfo);
                    }
                    reader.moveUp();
                }
            }

            else  if (name.equals(E_COL_CONTACT_INFOS)) {
                ContactInfo contactInfo = null;
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_COL_CONTACT_INFO)) {
                        contactInfo =
                                (ContactInfo) context
                                        .convertAnother(contactInfo,
                                                        ContactInfo.class);
                        collection.addContactInfo(contactInfo);
                    }
                    reader.moveUp();
                }
            }

            else if (name.equals(E_COL_CREATORS)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_COL_CREATOR)) {
                        PersonName creator = null;
                        creator =
                                (PersonName) context
                                        .convertAnother(creator,
                                                        PersonName.class);
                        collection.addCreator(creator);
                    }
                    reader.moveUp();
                }
            }

            else if (name.equals(E_COL_ALTERNATE_IDS)) {
                List<String> alternateIdList = new ArrayList<String>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_COL_ALTERNATE_ID)) {
                        final String value = reader.getValue();
                        if (!isEmptyOrNull(value)) {
                            alternateIdList.add(value.trim());
                        }
                    }
                    reader.moveUp();
                }
                collection.setAlternateIds(alternateIdList);
            }
            
            else if (name.equals(E_COL_CHILDREN_IDS)) {
                List<String> childrenIds = new ArrayList<String>();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(E_COL_CHILDREN_ID)) {
                        final String value = reader.getValue();
                        if (!isEmptyOrNull(value)) {
                            childrenIds.add(value.trim());
                        }
                    }
                    reader.moveUp();
                }
                collection.setChildrenIds(childrenIds);
            }

            else if (name.equals(E_COL_PARENT_ID)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    collection.setParentId(value.trim());
                }
            }

            else if (name.equals(E_COL_PARENT_PROJECT)) {
                final String value = reader.getValue();
                if (!isEmptyOrNull(value)) {
                    collection.setParentProjectId(value.trim());
                }                
            }
            reader.moveUp();
        }
        return collection;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(Class type) {
        return type == Collection.class;
    }
}
