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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;
import org.dataconservancy.model.builder.xstream.*;
import org.dataconservancy.model.dcp.DcpModelVersion;
import org.dataconservancy.model.dcs.*;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadEvent;
import org.seadva.model.SeadFile;
import org.seadva.model.pack.ResearchObject;

import javax.xml.namespace.QName;

public class SeadXStreamFactory {

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

        x.alias(FileConverter.E_FILE, SeadFile.class);
        x.registerConverter(new SeadFileConverter());
        qnames.registerMapping(new QName(defaultnsUri, SeadFileConverter.E_FILE), SeadFile.class);

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

        x.alias(DeliverableUnitConverter.E_DU, SeadDeliverableUnit.class);
        x.registerConverter(new SeadDeliverableUnitConverter());
        qnames.registerMapping(new QName(defaultnsUri, DeliverableUnitConverter.E_DU), SeadDeliverableUnit.class);

        x.alias(ROConverter.E_DCP, ResearchObject.class);
        x.registerConverter(new ROConverter());
        qnames.registerMapping(new QName(defaultnsUri, ROConverter.E_DCP), ResearchObject.class);

        x.alias(org.seadva.model.builder.xstream.RelationConverter.E_RELATION, DcsRelation.class);
        x.registerConverter(new org.seadva.model.builder.xstream.RelationConverter());
        qnames.registerMapping(new QName(defaultnsUri, org.seadva.model.builder.xstream.RelationConverter.E_RELATION), DcsRelation.class);

        x.alias(SeadEventConverter.E_EVENT, SeadEvent.class);
        x.registerConverter(new SeadEventConverter());
        qnames.registerMapping(new QName(defaultnsUri, SeadEventConverter.E_EVENT), SeadEvent.class);

        x.alias(DeliverableUnitConverter.E_ALTERNATEID, DcsResourceIdentifier.class);
        x.registerConverter(new ResourceIdentifierConverter());
        qnames.registerMapping(new QName(defaultnsUri, SeadDeliverableUnitConverter.E_ALTERNATEID), DcsResourceIdentifier.class);

        return x;
    }

}
