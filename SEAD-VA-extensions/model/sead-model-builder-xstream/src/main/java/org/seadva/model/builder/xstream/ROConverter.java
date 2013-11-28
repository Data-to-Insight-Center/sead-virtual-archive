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
package org.seadva.model.builder.xstream;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.dataconservancy.model.builder.xstream.*;
import org.dataconservancy.model.dcp.DcpModelVersion;
import org.dataconservancy.model.dcs.*;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadEvent;
import org.seadva.model.SeadFile;
import org.seadva.model.SeadRepository;
import org.seadva.model.pack.ResearchObject;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * XStream converter for {@link @Dcp}
 */
class ROConverter extends AbstractEntityConverter {

    final static String E_DCP = "dcp";
    final static String E_DELIVERABLE_UNITS = "DeliverableUnits";
    final static String E_REPOSITORIES = "Repositories";
    final static String E_COLLECTIONS = "Collections";
    final static String E_MANIFESTATIONS = "Manifestations";
    final static String E_FILES = "Files";
    final static String E_EVENTS = "Events";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);
        final ResearchObject sip = (ResearchObject)source;
        final DcpModelVersion version = sip.getModelVersion();
        String xmlns = null;
        if (version != null) {
            xmlns = version.getXmlns();
        }

        if (DcpModelVersion.VERSION_1_0 != version) {
            final String msg = "I can only marshal Data Conservancy Packaging version " +
                    DcpModelVersion.VERSION_1_0.getVersionNumber() + ", xmlns " + DcpModelVersion.VERSION_1_0.getXmlns() +
                    " (was version: " + version + ", xmlns " + xmlns + ")";
            throw new ConversionException(msg);
        }

        final Collection<DcsDeliverableUnit> deliverableUnits = sip.getDeliverableUnits();
        final Collection<DcsCollection> collections = sip.getCollections();
        final Collection<DcsManifestation> manifestations = sip.getManifestations();
        final Collection<DcsFile> files = sip.getFiles();
        final Collection<DcsEvent> events = sip.getEvents();
        final Collection<SeadRepository> repositories = sip.getRepositories();

        writer.addAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        writer.addAttribute("xsi:schemaLocation", DcpModelVersion.VERSION_1_0.getXmlns() + " " + DcpModelVersion.VERSION_1_0.getXmlns());


        if (!deliverableUnits.isEmpty()) {
            writer.startNode(E_DELIVERABLE_UNITS);
            for (DcsDeliverableUnit du : deliverableUnits) {
                writer.startNode(DeliverableUnitConverter.E_DU);
                context.convertAnother((SeadDeliverableUnit)du);
                writer.endNode();
            }
            writer.endNode();
        }

        if (!collections.isEmpty()) {
            writer.startNode(E_COLLECTIONS);
            for (DcsCollection c : collections) {
                writer.startNode(CollectionConverter.E_COLLECTION_CAP);
                context.convertAnother(c);
                writer.endNode();
            }
            writer.endNode();
        }

        if (!manifestations.isEmpty()) {
            writer.startNode(E_MANIFESTATIONS);
            for (DcsManifestation m : manifestations) {
                writer.startNode(ManifestationConverter.E_MANIFESTATION);
                context.convertAnother(m);
                writer.endNode();
            }
            writer.endNode();
        }

        if (!files.isEmpty()) {
            writer.startNode(E_FILES);
            for (DcsFile f : files) {
                writer.startNode(FileConverter.E_FILE);
                context.convertAnother((SeadFile)f);
                writer.endNode();
            }
            writer.endNode();
        }

        if (!events.isEmpty()) {
            writer.startNode(E_EVENTS);
            for (DcsEvent e : events) {
                writer.startNode(EventConverter.E_EVENT);
                context.convertAnother(new SeadEvent(e));
                writer.endNode();
            }
            writer.endNode();
        }

        if (!repositories.isEmpty()) {
            writer.startNode(E_REPOSITORIES);
            for (SeadRepository repo : repositories) {
                writer.startNode(RepositoryConverter.E_REPOSITORY);
                context.convertAnother(repo);
                writer.endNode();
            }
            writer.endNode();
        }
    }

    QName getElementQname(HierarchicalStreamReader reader) {
        final QName name;
        final HierarchicalStreamReader underlyingReader = reader.underlyingReader();
        final Class readerClass = underlyingReader.getClass();
        if (NsAwareStream.class.isAssignableFrom(readerClass)) {
            name = ((NsAwareStream) underlyingReader).getQname();
        } else {
            name = new QName(reader.getNodeName());
        }
        return name;
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final ResearchObject sip = new ResearchObject();

        final QName dcpQname = getElementQname(reader);
        final String xmlns = dcpQname.getNamespaceURI();
        final DcpModelVersion version = DcpModelVersion.fromXmlns(xmlns);
        if (DcpModelVersion.VERSION_1_0 != version) {
            final String msg = "I can only unmarshal Data Conservancy Packaging version " +
                    DcpModelVersion.VERSION_1_0.getVersionNumber() + ", xmlns " + DcpModelVersion.VERSION_1_0.getXmlns() +
                    " (was version: " + version + ", xmlns: " + xmlns + ")";
            throw new ConversionException(msg);
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            final String containerElementName = getElementName(reader);

            if (containerElementName.equals(ROConverter.E_DELIVERABLE_UNITS)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(DeliverableUnitConverter.E_DU)) {
                        final SeadDeliverableUnit du = (SeadDeliverableUnit) context.convertAnother(sip, SeadDeliverableUnit.class);
                        sip.addDeliverableUnit(du);
                    }
                    reader.moveUp();
                }
            }

            if (containerElementName.equals(ROConverter.E_COLLECTIONS)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(CollectionConverter.E_COLLECTION_CAP)) {
                        final DcsCollection c = (DcsCollection) context.convertAnother(sip, DcsCollection.class);
                        sip.addCollection(c);
                    }
                    reader.moveUp();
                }
            }

            if (containerElementName.equals(ROConverter.E_MANIFESTATIONS)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(ManifestationConverter.E_MANIFESTATION)) {
                        final DcsManifestation man = (DcsManifestation) context.convertAnother(sip, DcsManifestation.class);
                        sip.addManifestation(man);
                    }
                    reader.moveUp();
                }
            }

            if (containerElementName.equals(ROConverter.E_FILES)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(FileConverter.E_FILE)) {
                        final SeadFile f = (SeadFile) context.convertAnother(sip, SeadFile.class);
                        sip.addFile(f);
                    }
                    reader.moveUp();
                }
            }

            if (containerElementName.equals(ROConverter.E_EVENTS)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(EventConverter.E_EVENT)) {
                        final SeadEvent e = (SeadEvent) context.convertAnother(sip, SeadEvent.class);
                        sip.addEvent(e);
                    }
                    reader.moveUp();
                }
            }

            if (containerElementName.equals(ROConverter.E_REPOSITORIES)) {
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if (getElementName(reader).equals(RepositoryConverter.E_REPOSITORY)) {
                        final SeadRepository repository = (SeadRepository) context.convertAnother(sip, SeadRepository.class);
                        sip.addRepository(repository);
                    }
                    reader.moveUp();
                }
            }


            reader.moveUp();
        }


        return sip;
    }

    @Override
    public boolean canConvert(Class type) {
        return ResearchObject.class == type;
    }
}
