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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcp.DcpModelVersion;
import org.dataconservancy.model.dcs.*;

import javax.xml.namespace.QName;

/**
 * Encapsulates the XStream runtime configuration, including {@link com.thoughtworks.xstream.converters.Converter registration},
 * Java class to XML element {@link XStream#alias(String, Class) aliases}, {@link com.thoughtworks.xstream.io.xml.QNameMap qname maps},
 * and the XStream driver instantiation.
 *
 * @see <a href="http://xstream.codehaus.org">XStream website</a>
 */
public class XStreamFactory {

    /**
     * Creates and initializes an XStream facade, ready to (de)serialize Data Conservancy model entities.
     *
     * Currently this factory does not allow the XStream {@link com.thoughtworks.xstream.io.HierarchicalStreamDriver driver}
     * to be injected, because this implementation requires a custom, namespace aware StAX driver.
     * 
     * @return a new, initialized XStream instance, ready to (de)serialize DC entities
     */
    public static XStream newInstance() {
        // QName Map
        final QNameMap qnames = new QNameMap();
        final String defaultnsUri = DcpModelVersion.VERSION_1_0.getXmlns();
        qnames.setDefaultNamespace(defaultnsUri);

        final DcsPullDriver driver = new DcsPullDriver(qnames);

        // The XStream Driver
        final XStream x = new XStream(driver);

        // XStream converter, alias, and QName registrations
        x.alias(FormatConverter.E_FORMAT, DcsFormat.class);
        x.registerConverter(new FormatConverter());
        qnames.registerMapping(new QName(defaultnsUri, FormatConverter.E_FORMAT), DcsFormat.class);

        x.alias(ManifestationFileConverter.E_MANFILE, DcsManifestationFile.class);
        x.registerConverter(new ManifestationFileConverter());
        qnames.registerMapping(new QName(defaultnsUri, ManifestationFileConverter.E_MANFILE), DcsManifestationFile.class);

        x.alias(ManifestationConverter.E_MANIFESTATION, DcsManifestation.class);
        x.registerConverter(new ManifestationConverter());
        qnames.registerMapping(new QName(defaultnsUri, ManifestationConverter.E_MANIFESTATION), DcsManifestation.class);

        x.alias(FixityConverter.E_FIXITY, DcsFixity.class);
        x.registerConverter(new FixityConverter());
        qnames.registerMapping(new QName(defaultnsUri, FixityConverter.E_FIXITY), DcsFixity.class);

        x.alias(FileConverter.E_FILE, DcsFile.class);
        x.registerConverter(new FileConverter());
        qnames.registerMapping(new QName(defaultnsUri, FileConverter.E_FILE), DcsFile.class);

        x.alias(MetadataConverter.E_METADATA, DcsMetadataRef.class);
        x.alias(MetadataConverter.E_METADATA, DcsMetadata.class);
        x.registerConverter(new MetadataConverter());
        qnames.registerMapping(new QName(defaultnsUri, MetadataConverter.E_METADATA), DcsMetadataRef.class);
        qnames.registerMapping(new QName(defaultnsUri, MetadataConverter.E_METADATA), DcsMetadata.class);

        x.alias(CollectionConverter.E_COLLECTION, DcsCollectionRef.class);
        x.alias(CollectionConverter.E_COLLECTION_CAP, DcsCollection.class);
        x.registerConverter(new CollectionConverter());
        qnames.registerMapping(new QName(defaultnsUri, CollectionConverter.E_COLLECTION), DcsCollectionRef.class);
        qnames.registerMapping(new QName(defaultnsUri, CollectionConverter.E_COLLECTION_CAP), DcsCollection.class);

        x.alias(DeliverableUnitConverter.E_DU, DcsDeliverableUnit.class);
        x.registerConverter(new DeliverableUnitConverter());
        qnames.registerMapping(new QName(defaultnsUri, DeliverableUnitConverter.E_DU), DcsDeliverableUnit.class);

        x.alias(DcpConverter.E_DCP, Dcp.class);
        x.registerConverter(new DcpConverter());
        qnames.registerMapping(new QName(defaultnsUri, DcpConverter.E_DCP), Dcp.class);

        x.alias(RelationConverter.E_RELATION, DcsRelation.class);
        x.registerConverter(new RelationConverter());
        qnames.registerMapping(new QName(defaultnsUri, RelationConverter.E_RELATION), DcsRelation.class);

        x.alias(EventConverter.E_EVENT, DcsEvent.class);
        x.registerConverter(new EventConverter());
        qnames.registerMapping(new QName(defaultnsUri, EventConverter.E_EVENT), DcsEvent.class);

        x.alias(DeliverableUnitConverter.E_ALTERNATEID, DcsResourceIdentifier.class);
        x.registerConverter(new ResourceIdentifierConverter());
        qnames.registerMapping(new QName(defaultnsUri, DeliverableUnitConverter.E_ALTERNATEID), DcsResourceIdentifier.class);

        return x;
    }

}
