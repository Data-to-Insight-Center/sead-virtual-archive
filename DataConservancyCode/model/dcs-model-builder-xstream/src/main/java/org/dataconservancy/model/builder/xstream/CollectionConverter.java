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
package org.dataconservancy.model.builder.xstream;

import java.util.Collection;
import java.util.Set;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.dcs.*;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * XStream converter for {@link DcsCollection}
 */
public class CollectionConverter extends AbstractEntityConverter {
    
    public static final String E_COLLECTION = "collection";
    public static final String E_COLLECTION_CAP = "Collection";
    public static final String E_PARENT = "parent";
    public static final String E_TITLE = "title";
    public static final String E_TYPE = "type";
    public static final String E_SUBJECT = "subject";
    public static final String E_CREATOR = "creator";
    public static final String E_ALTERNATEID = "alternateIdentifier";
    public static final String A_REF = "ref";
    public static final String A_ID = "id";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        if (source instanceof DcsCollection) {

            final DcsCollection c = (DcsCollection) source;
            final Collection<DcsMetadata> md = c.getMetadata();
            final Collection<DcsMetadataRef> mdRef = c.getMetadataRef();
            final String id = c.getId();
            final Collection<DcsResourceIdentifier> altIds = c.getAlternateIds();
            final DcsCollectionRef parentRef = c.getParent();
            final String title = c.getTitle();
            final String type = c.getType();
            final Collection<String> subjects = c.getSubjects();
            final Collection<String> creators = c.getCreators();

            if (!isEmptyOrNull(id)) {
                writer.addAttribute(A_ID, id);
            }

            if (altIds != null) {
                for (DcsResourceIdentifier rid : altIds) {
                    writer.startNode(E_ALTERNATEID);
                    context.convertAnother(rid);
                    writer.endNode();
                }
            }

            if (parentRef != null) {
                writer.startNode(E_PARENT);
                writer.addAttribute(A_REF, parentRef.getRef());
                writer.endNode();
            }

            if (!isEmptyOrNull(type)) {
                writer.startNode(E_TYPE);
                writer.setValue(type);
                writer.endNode();
            }

            if (!isEmptyOrNull(title)) {
                writer.startNode(E_TITLE);
                writer.setValue(title);
                writer.endNode();
            }

            if (creators != null) {
                for (String creator : creators) {
                    writer.startNode(E_CREATOR);
                    writer.setValue(creator);
                    writer.endNode();
                }
            }

            if (subjects != null) {
                for (String subject : subjects) {
                    writer.startNode(E_SUBJECT);
                    writer.setValue(subject);
                    writer.endNode();
                }
            }

            if (md != null) {
                for (DcsMetadata m : md) {
                    writer.startNode(MetadataConverter.E_METADATA);
                    context.convertAnother(m);
                    writer.endNode();
                }
            }

            if (mdRef != null) {
                for (DcsMetadataRef ref : mdRef) {
                    writer.startNode(MetadataConverter.E_METADATA);
                    context.convertAnother(ref);
                    writer.endNode();
                }
            }
        }

        if (source instanceof DcsCollectionRef) {
            final DcsCollectionRef cr = (DcsCollectionRef) source;
            if (isEmptyOrNull(cr.getRef())) {
                return;
            }
            writer.addAttribute(A_REF, cr.getRef());
        }

    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        final String ref = reader.getAttribute(CollectionConverter.A_REF);
        final String id = reader.getAttribute(CollectionConverter.A_ID);

        if (!isEmptyOrNull(ref)) {
            final DcsCollectionRef collectionReference = new DcsCollectionRef();
            collectionReference.setRef(ref);
            return collectionReference;
        }

        final DcsCollection collection = new DcsCollection();
        if (!isEmptyOrNull(id)) {
            collection.setId(id);
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String name = getElementName(reader);  //reader.getNodeName();

            if (name.equals(E_ALTERNATEID)) {
                final DcsResourceIdentifier rid =
                    (DcsResourceIdentifier) context.convertAnother(collection, DcsResourceIdentifier.class);
                if (rid != null) { collection.addAlternateId(rid); }
                reader.moveUp();
                continue;
            }

            if (name.equals(MetadataConverter.E_METADATA)) {
                final String mdRef = reader.getAttribute(MetadataConverter.A_REF);
                if (!isEmptyOrNull(mdRef)) {
                    final DcsMetadataRef md = (DcsMetadataRef) context.convertAnother(collection, DcsMetadataRef.class);
                    collection.addMetadataRef(md);
                    reader.moveUp();
                    continue;
                } else {
                    final DcsMetadata md = (DcsMetadata) context.convertAnother(collection, DcsMetadata.class);
                    collection.addMetadata(md);
                    continue;
                }                
            }
            
            if (name.equals(E_PARENT)) {
                final String parentRef = reader.getAttribute(A_REF);
                if (!isEmptyOrNull(parentRef)) {
                    final DcsCollectionRef parent = new DcsCollectionRef();
                    parent.setRef(parentRef);
                    collection.setParent(parent);
                }
            }

            final String val = reader.getValue();

            if (name.equals(E_TITLE) && !isEmptyOrNull(val)) {
                collection.setTitle(val);
            }

            if (name.equals(E_TYPE) && !isEmptyOrNull(val)) {
                collection.setType(val);
            }

            if (name.equals(E_CREATOR) && !isEmptyOrNull(val)) {
                collection.addCreator(val);
            }

            if (name.equals(E_SUBJECT) && !isEmptyOrNull(val)) {
                collection.addSubject(val);                
            }

            reader.moveUp();
        }

        return collection;
    }

    @Override
    public boolean canConvert(Class type) {
        return DcsCollection.class == type || DcsCollectionRef.class == type;
    }
}
