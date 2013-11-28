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
package org.dataconservancy.archive.impl.fcrepo.xstream;

import javax.xml.namespace.QName;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;

import org.dataconservancy.archive.impl.fcrepo.dto.ContentLocation;
import org.dataconservancy.archive.impl.fcrepo.dto.DCPackage;
import org.dataconservancy.archive.impl.fcrepo.dto.Datastream;
import org.dataconservancy.archive.impl.fcrepo.dto.DatastreamVersion;
import org.dataconservancy.archive.impl.fcrepo.dto.DublinCore;
import org.dataconservancy.archive.impl.fcrepo.dto.EmbeddedRDF;
import org.dataconservancy.archive.impl.fcrepo.dto.EmbeddedRDFDescription;
import org.dataconservancy.archive.impl.fcrepo.dto.FedoraDigitalObject;
import org.dataconservancy.archive.impl.fcrepo.dto.ObjectProperties;
import org.dataconservancy.archive.impl.fcrepo.dto.XMLContent;
import org.dataconservancy.model.builder.xstream.FixityConverter;
import org.dataconservancy.model.builder.xstream.FormatConverter;
import org.dataconservancy.model.builder.xstream.ManifestationFileConverter;
import org.dataconservancy.model.dcp.DcpModelVersion;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadata;

/**
 * Encapsulates the XStream runtime configuration, including
 * {@link com.thoughtworks.xstream.converters.Converter registration}, Java
 * class to XML element {@link XStream#alias(String, Class) aliases},
 * {@link com.thoughtworks.xstream.io.xml.QNameMap qname maps}, and the XStream
 * driver instantiation.
 * 
 * @see <a href="http://xstream.codehaus.org">XStream website</a>
 */
public class FDOXStreamFactory {
    
    /**
     * Creates and initializes an XStream facade, ready to (de)serialize Data
     * Conservancy FDO entities. Currently this factory does not allow the
     * XStream {@link com.thoughtworks.xstream.io.HierarchicalStreamDriver
     * driver} to be injected, because this implementation requires a custom,
     * namespace aware StAX driver.
     * 
     * @return a new, initialized XStream instance, ready to (de)serialize DC
     *         FDO entities
     */
    public static FDOXStream newInstance() {

        // QName Map
        final QNameMap qnameMap = new QNameMap();
        final String defaultNSPrefix = "foxml";
        final String defaultNSURI = "info:fedora/fedora-system:def/foxml#";
        // Don't add these, it will mess up the RDF namespaces.
        //qnameMap.setDefaultPrefix(defaultNSPrefix);
        //qnameMap.setDefaultNamespace(defaultNSURI);

        // There are Fedora attributes with underscores which get doubled
        // by the default replacer.
        XmlFriendlyReplacer replacer = new XmlFriendlyReplacer("_", "_");
        final FDOStaxDriver driver = new FDOStaxDriver(qnameMap, replacer);
        driver.setRepairingNamespace(false);

        // The XStream Driver
        final FDOXStream x = new FDOXStream(driver);

        // XStream converter, alias, and QName registrations

        // Digital Object
        String DIGITAL_OBJECT = "digitalObject";
        x.alias(DIGITAL_OBJECT, FedoraDigitalObject.class);
        x.registerConverter(new FDOConverter());
        QName doQname =
                new QName(defaultNSURI, DIGITAL_OBJECT, defaultNSPrefix);
        qnameMap.registerMapping(doQname, FedoraDigitalObject.class);
        // The automatic start node is by name instead of class requiring this one.
        qnameMap.registerMapping(doQname, DIGITAL_OBJECT);

        // Object Properties
        String OBJECT_PROPERTIES = "objectProperties";
        x.alias(OBJECT_PROPERTIES, ObjectProperties.class);
        x.registerConverter(new ObjectPropertiesConverter());
        x.omitField(ObjectProperties.class, "propertyMap");
        QName opQname =
                new QName(defaultNSURI, OBJECT_PROPERTIES, defaultNSPrefix);
        qnameMap.registerMapping(opQname, ObjectProperties.class);

        // Datastream
        String DATASTREAM = "datastream";
        x.alias(DATASTREAM, Datastream.class);
        x.registerConverter(new DatastreamConverter());
        QName dsQname = new QName(defaultNSURI, DATASTREAM, defaultNSPrefix);
        qnameMap.registerMapping(dsQname, Datastream.class);

        // Datastream Version
        String DATASTREAM_VERSION = "datastreamVersion";
        x.alias(DATASTREAM_VERSION, DatastreamVersion.class);
        x.registerConverter(new DatastreamVersionConverter());
        QName dsvQname =
                new QName(defaultNSURI, DATASTREAM_VERSION, defaultNSPrefix);
        qnameMap.registerMapping(dsvQname, DatastreamVersion.class);

        // Content Location
        String CONTENT_LOCATION = "contentLocation";
        x.alias(CONTENT_LOCATION, ContentLocation.class);
        x.registerConverter(new ContentLocationConverter());
        QName clQname =
                new QName(defaultNSURI, CONTENT_LOCATION, defaultNSPrefix);
        qnameMap.registerMapping(clQname, ContentLocation.class);

        // XML Content
        String XML_CONTENT = "xmlContent";
        x.alias(XML_CONTENT, XMLContent.class);
        x.registerConverter(new XMLContentConverter());
        QName xmlcQname = new QName(defaultNSURI, XML_CONTENT, defaultNSPrefix);
        qnameMap.registerMapping(xmlcQname, XMLContent.class);

        // Embedded RDF
        String EMBEDDED_RDF = "RDF";
        x.alias(EMBEDDED_RDF, EmbeddedRDF.class);
        x.registerConverter(new EmbeddedRDFConverter());
        String rdfNSURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        QName rdfQName = new QName(rdfNSURI, EMBEDDED_RDF, "rdf");
        qnameMap.registerMapping(rdfQName, EmbeddedRDF.class);

        // Embedded RDF Description
        String EMBEDDED_RDF_DESCRIPTION = "Description";
        x.alias(EMBEDDED_RDF_DESCRIPTION, EmbeddedRDFDescription.class);
        x.registerConverter(new EmbeddedRDFDescriptionConverter());
        QName rdfDescriptionQName =
                new QName(rdfNSURI, EMBEDDED_RDF_DESCRIPTION, "rdf");
        qnameMap.registerMapping(rdfDescriptionQName,
                                 EmbeddedRDFDescription.class);
        
        // DCS Deliverable Unit
        String dcpURI = DcpModelVersion.VERSION_1_0.getXmlns();
        //x.alias("DeliverableUnit", DcsDeliverableUnit.class);
        x.registerConverter(new FDODeliverableUnitConverter());
        qnameMap.registerMapping(new QName("DeliverableUnit"), DcsDeliverableUnit.class);

        // DCS Collection
        x.registerConverter(new FDOCollectionConverter());
        qnameMap.registerMapping(new QName("Collection"), DcsCollection.class);
        
        // DCS Metadata
        x.registerConverter(new FDOMetadataConverter());
        qnameMap.registerMapping(new QName("metadata"), DcsMetadata.class);

        // DCS Manifestation
        x.registerConverter(new FDOManifestationConverter());
        qnameMap.registerMapping(new QName("Manifestation"), DcsManifestation.class);

        // DCS Manifestation File
        x.registerConverter(new ManifestationFileConverter());
        qnameMap.registerMapping(new QName("manifestationFile"), DcsManifestationFile.class);

        // DCS Format
        x.registerConverter(new FormatConverter());
        qnameMap.registerMapping(new QName("format"), DcsFormat.class);
        
        // DCS Fixity
        x.registerConverter(new FixityConverter());
        qnameMap.registerMapping(new QName("fixity"), DcsFixity.class);

        // DCS File
        x.registerConverter(new FDOFileConverter());
        qnameMap.registerMapping(new QName("File"), DcsFile.class);

        // DCS Event
        x.registerConverter(new FDOEventConverter());
        qnameMap.registerMapping(new QName("Event"), DcsEvent.class);

        // We will have to add more of these as we add vocabulary.  Post Y1P
        // this can be refactored.
        String dcsURI = "http://dataconservancy.org/ontologies/dcs/1.0/";
        QName dcsQName = new QName(dcsURI, "id", "dcs");
        qnameMap.registerMapping(dcsQName, "id");

        String fedoraModelURI = "info:fedora/fedora-system:def/model#";
        QName fedoraModelQName =
                new QName(fedoraModelURI, "hasModel", "fedora-model");
        qnameMap.registerMapping(fedoraModelQName, "hasModel");

        String relURI = "info:fedora/fedora-system:def/relations-external#";
        QName rel1QName = new QName(relURI, "hasPart", "rel");
        QName rel2QName = new QName(relURI, "isMemberOf", "rel");
        qnameMap.registerMapping(rel1QName, "hasPart");
        qnameMap.registerMapping(rel2QName, "isMemberOf");
        
        //String dcsRelURI = "http://dataconservancy.org/relationships#";
        //QName dcsPAQName = new QName(dcsRelURI, "hasProviderAgreement", "dcsr");
        //qnameMap.registerMapping(dcsPAQName, "hasProviderAgreement");
        
        // Data Conservancy Package
        String DC_PACKAGE = "dcp";
        x.registerConverter(new DCPackageConverter());
        String dcpNSURI = "http://dataconservancy.org/schemas/dcp/1.0";
        QName dcpQName = new QName(dcpNSURI, DC_PACKAGE, "dcp");
        qnameMap.registerMapping(dcpQName, DCPackage.class);

        // Dublin Core
        String DUBLIN_CORE = "dc";
        x.registerConverter(new DublinCoreConverter());
        String oaidcNSURI = "http://www.openarchives.org/OAI/2.0/oai_dc/";
        QName dcQName = new QName(oaidcNSURI, DUBLIN_CORE, "oai_dc");
        qnameMap.registerMapping(dcQName, DublinCore.class);
        
        //QName dcmQName = new QName(oaidcNSURI, DUBLIN_CORE, DUBLIN_CORE);
        //qnameMap.registerMapping(dcmQName, DUBLIN_CORE);

        //String METADATA = "metadata";
        ////QName metadataQName = new QName(dcsURI, METADATA);
        //QName metadataQName = new QName(METADATA);
        //qnameMap.registerMapping(metadataQName, METADATA);

        //String ASTRO = "astro";
        //String astroNSURI = "http://sdss.org/astro";
        //QName astroQName = new QName(astroNSURI, ASTRO, ASTRO);
        //qnameMap.registerMapping(astroQName, ASTRO);

        return x;
    }

}
