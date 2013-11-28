package org.dataconservancy.mhf.resources.registry;

/**
 * Constants used by the Schema Registry Generator
 */
class SchemaRegistryGeneratorConstants {

    /**
     * The name of the FGDC 1998 master schema document
     */
    static final String FGDC_MASTER = "fgdc-std-001-1998.xsd";

    /**
     * The name of the FGDC DTD file
     */
    static final String FGDC_DTD = "fgdc-std-001-1998.dtd";

    static final String FGDC_DCP_REGISTRY_ENTRY = "fgdc-std-1998-registry-entry.xml";

    static final String XSD_DCP_REGISTRY_ENTRY = "xsd-registry-entry.xml";

    /**
     * A format string used to generate the names of all of the included FGDC 1998 sections included by
     * {@link #FGDC_MASTER}.  Parameters are: the section number (e.g. '1', '2', '3', ...).
     */
    static final String FGDC_SECT_FORMAT = "fgdc-std-001-1998-sect%02d.xsd";

    /**
     * This is the well-known URL that all of the FGDC 1998 schema documents reside under.  Concatenate this prefix with
     * a schema document name, and the result should be a resolvable URL to a schema document.
     */
    static final String FGDC_URL_PREFIX = "http://www.fgdc.gov/schemas/metadata/";

    /**
     * This is the well-known URL to the master FGDC 1998 schema document.
     */
    static final String FGDC_MASTER_URL = FGDC_URL_PREFIX + FGDC_MASTER;

    /**
     * This is the well-known URL to the FGDC 1998 DTD.
     */
    static final String FGDC_DTD_URL = FGDC_URL_PREFIX + FGDC_DTD;

    /**
     * A format string used to generate the well-known URLs included by {@link #FGDC_MASTER}.  Parameters are: the
     * section number (e.g. '1', '2', '3', ...)
     */
    static final String FGDC_SECT_URL_FORMAT = FGDC_URL_PREFIX + FGDC_SECT_FORMAT;

    /**
     * The classpath resource that all included and generated schema and registry resources will be included under.
     */
    static final String BASE_RESOURCE_PATH = "/org/dataconservancy/mhf/resources/";

    /**
     * This is the base resource path that all XML schema documents reside under.  Test classes will use schemas
     * located under this resource path to perform validation of XML instance documents.
     */
    static final String SCHEMA_BASE_RESOURCE_PATH = BASE_RESOURCE_PATH + "schemas/";

    /**
     * This is the base resource path that all registries.  Each directory under this resource path represents
     * a registry.  Under each registry directory, registry entries will serialized as DCP packages, one entry
     * per DCP package.
     */
    static final String REGISTRY_BASE_RESOURCE_PATH = BASE_RESOURCE_PATH + "registry/";

    static final String METADATA_REGISTRY_BASE_RESOURCE_PATH = REGISTRY_BASE_RESOURCE_PATH + "metadataformat/";

    /**
     * This is the resource path that the schema for FGDC 1998 XML Schema documents reside under.  Schemas that are
     * referenced by the primary FGDC schema document also reside under here.  Test classes will use schemas located
     * under this resource path to perform validation of FGDC XML instance documents.  This resource path resides under
     * {@link #SCHEMA_BASE_RESOURCE_PATH}.
     */
    static final String FGDC_BASE_RESOURCE_PATH =
            SCHEMA_BASE_RESOURCE_PATH + "fgdc1998/";
    /**
     * The name of the FGDC 1998 metadata format
     */
    static final String FGDC_FORMAT_NAME = "FGDC 1998";

    /**
     * The version of the FGCD 1998 metadata format
     */
    static final String FGDC_VERSION = "1.0.0 20030801";

    /**
     * The description of the FGDC 1998 schema
     */
    static final String FGDC_1998_SCHEMA_DESCRIPTION = "FGDC 1998 XSD Schema";

    /**
     * The description of the XSD schema
     */
    static final String XSD_SCHEMA_DESCRIPTION = "XSD Schema (for XSD instance documents)";

    /**
     * The description of the metadata format registry
     */
    static final String METADATA_FORMAT_REGISTRY_DESCRIPTION = "Metadata Format Registry (aka Schema Registry)";

    /**
     * This is the classpath resource for the FGDC master schema document
     */
    static final String FGDC_MASTER_RESOURCE = FGDC_BASE_RESOURCE_PATH + FGDC_MASTER;

    /**
     * A format string used to generate the classpath resources included by {@link #FGDC_MASTER_RESOURCE}.  Parameters
     * are: the section number (e.g. '1', '2', '3', ...)
     */
    static final String FGDC_SECT_RESOURCE_FORMAT = FGDC_BASE_RESOURCE_PATH + FGDC_SECT_FORMAT;

    /**
     * The identifier for the Metadata Format Registry
     */
    static final String METADATA_REGISTRY_ID = "dataconservancy.org:registry:metadata-format:";

    /**
     * The identifier of the XSD file format.
     */
    static final String XSD_FORMAT_ID = "dataconservancy.org:formats:file:xsd:2004";

    /**
     * The identifier of the FGDC XML format.
     */
    static final String FGDC_XML_FORMAT_ID = "dataconservancy.org:formats:file:metadata:fgdc:xml";

    static final String XSD_BASE_RESOURCE_PATH = SCHEMA_BASE_RESOURCE_PATH + "xsd/";

    static final String XSD_SECT_RESOURCE_FORMAT = XSD_BASE_RESOURCE_PATH + "%s";




}