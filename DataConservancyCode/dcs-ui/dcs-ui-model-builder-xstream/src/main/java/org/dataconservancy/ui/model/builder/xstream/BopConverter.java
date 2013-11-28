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

import java.util.Set;

import javax.xml.XMLConstants;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;

/**
 * BopConverter is used by xstream library to serialize and deserialize
 * {@link org.dataconservancy.ui.model.Bop} objects.
 */

public class BopConverter
        extends AbstractEntityConverter
        implements ConverterConstants {

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);
        final Bop bizObjectPackage = (Bop) source;

        writer.addAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":xsi",
                            XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        writer.addAttribute("xsi:schemaLocation",
                            "http://dataconservancy.org/schemas/bop/1.0");

        final Set<Project> projects = bizObjectPackage.getProjects();
        final Set<Collection> collections = bizObjectPackage.getCollections();
        final Set<Person> persons = bizObjectPackage.getPersons();
        final Set<DataItem> dataItems = bizObjectPackage.getDataItems();
        final Set<MetadataFile> metadataFiles = bizObjectPackage.getMetadataFiles();

        if (!projects.isEmpty()) {
            writer.startNode(E_PROJECTS);
            for (Project project : projects) {
                writer.startNode(ProjectConverter.E_PROJECT);
                context.convertAnother(project);
                writer.endNode();
            }
            writer.endNode();
        }

        if (!collections.isEmpty()) {
            writer.startNode(E_COLLECTIONS);
            for (Collection collection : collections) {
                writer.startNode(CollectionConverter.E_COLLECTION);
                context.convertAnother(collection);
                writer.endNode();
            }
            writer.endNode();

        }

        if (!persons.isEmpty()) {
            writer.startNode(E_PERSONS);
            for (Person person : persons) {
                writer.startNode(PersonConverter.E_PERSON);
                context.convertAnother(person);
                writer.endNode();
            }
            writer.endNode();

        }

        if (!dataItems.isEmpty()) {
            writer.startNode(E_DATA_ITEMS);
            for (DataItem dataItem : dataItems) {
                writer.startNode(DataItemConverter.E_DATA_ITEM);
                context.convertAnother(dataItem);
                writer.endNode();
            }
            writer.endNode();

        }

        if (!metadataFiles.isEmpty()) {
            writer.startNode(E_METADATA_FILES);
            for (MetadataFile metadataFile : metadataFiles) {
                writer.startNode((MetadataFileConverter.E_METADATA_FILE));
                context.convertAnother(metadataFile);
                writer.endNode();
            }
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        final Bop bizObjectPackage = new Bop();
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String containerElementName = getElementName(reader);

            if (containerElementName.equals(E_PROJECTS)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader)
                            .equals(ProjectConverter.E_PROJECT)) {
                        final Project project =
                                (Project) context
                                        .convertAnother(bizObjectPackage,
                                                        Project.class);
                        bizObjectPackage.addProject(project);
                    }
                    reader.moveUp();
                }
            }

            if (containerElementName.equals(E_COLLECTIONS)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader)
                            .equals(CollectionConverter.E_COLLECTION)) {
                        final Collection c =
                                (Collection) context
                                        .convertAnother(bizObjectPackage,
                                                        Collection.class);
                        bizObjectPackage.addCollection(c);
                    }
                    reader.moveUp();
                }
            }

            if (containerElementName.equals(E_PERSONS)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(PersonConverter.E_PERSON)) {
                        final Person p =
                                (Person) context
                                        .convertAnother(bizObjectPackage,
                                                        Person.class);
                        bizObjectPackage.addPerson(p);
                    }
                    reader.moveUp();
                }
            }

            if (containerElementName.equals(E_DATA_ITEMS)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader)
                            .equals(DataItemConverter.E_DATA_ITEM)) {
                        final DataItem di =
                                (DataItem) context
                                        .convertAnother(bizObjectPackage,
                                                        DataItem.class);
                        bizObjectPackage.addDataItem(di);
                    }
                    reader.moveUp();
                }
            }

            if (containerElementName.equals(E_METADATA_FILES)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader)
                            .equals(MetadataFileConverter.E_METADATA_FILE)) {
                        final MetadataFile mdf =
                                (MetadataFile) context
                                        .convertAnother(bizObjectPackage,
                                                MetadataFile.class);
                        bizObjectPackage.addMetadataFile(mdf);
                    }
                    reader.moveUp();
                }
            }

            reader.moveUp();
        }

        return bizObjectPackage;
    }

    @Override
    public boolean canConvert(Class type) {
        return type == Bop.class;
    }
}
