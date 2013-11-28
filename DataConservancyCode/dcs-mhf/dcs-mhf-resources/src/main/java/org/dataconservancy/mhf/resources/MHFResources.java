package org.dataconservancy.mhf.resources;

/**
 * Provides string constants for, and describes, the classpath resources provided by the {@code dcs-mhf-resources}
 * module.  This includes XSD schemas for XML documents, and DTDs and XSDs included by XSD schema documents.
 */
public class MHFResources {

    /**
     * This is the base resource path that all XML schema documents reside under.  Test classes will use schemas
     * located under this resource path to perform validation of XML instance documents.
     */
    public static final String SCHEMA_BASE_RESOURCE_PATH = "/org/dataconservancy/mhf/resources/schemas/";

    /**
     * This is the resource path that all serialized registry entries live under.
     */
    public static final String REGISTRY_RESOURCE_PATH = "/org/dataconservancy/mhf/resources/registry/";


    /**
     * This is the resource path that the Metadata Format registry lives under.
     */
    public static final String METADATA_FORMAT_REGISTRY_RESOURCE_PATH = REGISTRY_RESOURCE_PATH + "metadataformat/";

    /**
     * This is the resource path that the schema for XSD documents reside under.  Schema and DTDs that are referenced
     * by the primary XSD schema document also reside under here.  Test classes will use resources located under this
     * resource path to perform validation of XSD instance documents.  This resource path resides under
     * {@link #SCHEMA_BASE_RESOURCE_PATH}.
     */
    public static final String XSD_SCHEMA_BASE_RESOURCE_PATH =
            SCHEMA_BASE_RESOURCE_PATH + "xsd/";
    /**
     * The resource path to the primary XSD schema document, {@code XMLSchema.xsd}.  It resides under
     * {@link #XSD_SCHEMA_BASE_RESOURCE_PATH}.
     */
    public static final String XSD_SCHEMA_RESOURCE_PATH =
            XSD_SCHEMA_BASE_RESOURCE_PATH + "XMLSchema.xsd";

    /**
     * The resource path to the {@code XMLSchema.dtd} document, which is referenced by
     * {@link #XSD_SCHEMA_RESOURCE_PATH XMLSchema.xsd}. It resides under {@link #XSD_SCHEMA_BASE_RESOURCE_PATH}.
     */
    public static final String XSD_DTD_RESOURCE_PATH =
            XSD_SCHEMA_BASE_RESOURCE_PATH + "XMLSchema.dtd";

    /**
     * The resource path to the {@code datatypes.dtd} document, which is referenced by the
     * {@link #XSD_DTD_RESOURCE_PATH XMLSchema.dtd}.  It resides under {@link #XSD_SCHEMA_BASE_RESOURCE_PATH}.
     */
    public static final String XSD_DATATYPE_DTD_RESOURCE_PATH =
            XSD_SCHEMA_BASE_RESOURCE_PATH + "datatypes.dtd";

    /**
     * The resource path to the {@code xml.xsd} document, which is referenced by the
     * {@link #XSD_SCHEMA_RESOURCE_PATH XMLSchema.xsd}.  It resides under {@link #XSD_SCHEMA_BASE_RESOURCE_PATH}.
     */
    public static final String XSD_XML_DTD_RESOURCE_PATH =
            XSD_SCHEMA_BASE_RESOURCE_PATH + "xml.xsd";

    /**
     * This is the resource path that the schema for FGDC 1998 XML Schema documents reside under.  Schemas that are
     * referenced by the primary FGDC schema document also reside under here.  Test classes will use schemas located
     * under this resource path to perform validation of FGDC XML instance documents.  This resource path resides under
     * {@link #SCHEMA_BASE_RESOURCE_PATH}.
     */
    public static final String FGDC_BASE_RESOURCE_PATH =
            SCHEMA_BASE_RESOURCE_PATH + "fgdc1998/";

    /**
     * The resource path to the primary FGDC 1998 XML schema document, {@code fgdc-std-001-1998.xsd}.  It resides under
     * {@link #FGDC_BASE_RESOURCE_PATH}.
     */
    public static final String FGDC_SCHEMA_RESOURCE_PATH =
            FGDC_BASE_RESOURCE_PATH + "fgdc-std-001-1998.xsd";

    /**
     * This is the base resource path that all sample metadata files reside under, including sample XML instance
     * documents.  Test classes will uses instance documents located under this resource path to test validation
     * scenarios.
     */
    public static final String SAMPLE_BASE_RESOURCE_PATH = "/SampleMetadataFiles/";

    /**
     * The resource path to an invalid FGDC XML instance document, {@code sample2.xml}.  It resides under
     * {@link #SAMPLE_BASE_RESOURCE_PATH}.
     */
    public static final String SAMPLE_INVALID_FGDC_XML_RESOURCE_PATH =
            SAMPLE_BASE_RESOURCE_PATH + "sample2.xml";

    /**
     * The resource path to a valid FGDC XML instance document, {@code sample3-valid.xml}.  It resides under
     * {@link #SAMPLE_BASE_RESOURCE_PATH}.
     */
    public static final String SAMPLE_VALID_FGDC_XML_RESOURCE_PATH =
            SAMPLE_BASE_RESOURCE_PATH + "sample3-valid.xml";

    /**
     * The resource path to a valid FGDC XML instance document, {@code valid-fgdc-with-maven-pom-schema-definition.xml}.
     * It resides under {@link #SAMPLE_BASE_RESOURCE_PATH}. This instance document may be validated against an FGDC
     * schema, even though it defines a {@code schemaLocation} to the Maven model schema in its {@code &lt;metadata&gt;}
     * element.  This file is meant to illustrate that when parsing is decoupled from validation, the
     * {@code schemaLocation} doesn't matter.
     */
    public static final String SAMPLE_VALID_FGDC_XML_DECLARING_MAVEN_SCHEMA_RESOURCE_PATH =
            SAMPLE_BASE_RESOURCE_PATH + "valid-fgdc-with-maven-pom-schema-definition.xml";

    /**
     * The resource path to a valid, but empty, XSD instance document, {@code empty-xsd-schema.xsd}.  It resides under
     * {@link #SAMPLE_BASE_RESOURCE_PATH}.
     */
    public static final String SAMPLE_VALID_BUT_EMPTY_XSD_RESOURCE_PATH =
            SAMPLE_BASE_RESOURCE_PATH + "empty-xsd-schema.xsd";

    /**
     * The resource path to an invalid XSD instance document, {@code invalid-xsd-schema.xsd}.  It resides under
     * {@link #SAMPLE_BASE_RESOURCE_PATH}.
     */
    public static final String SAMPLE_INVALID_XSD_RESOURCE_PATH =
            SAMPLE_BASE_RESOURCE_PATH + "invalid-xsd-schema.xsd";

    /**
     * The resource path to an valid XSD instance document, {@code xsd-schema-with-non-existent-include.xsd}.  It
     * contains an {@code <xsd:include>} with a {@code schemaLocation} to a non-existent resource. It resides under
     * {@link #SAMPLE_BASE_RESOURCE_PATH}.
     */
    public static final String SAMPLE_VALID_XSD_MISSING_IMPORT =
            SAMPLE_BASE_RESOURCE_PATH + "xsd-schema-with-non-existent-include.xsd";

    /**
     * The resource path to an invalid FGDC 1998 XML document that has multiple errors with the file:
     * {@code fgdc_sample_adopted_tmdls_jan12.shp.xml}.  This file can be used to test the fact that all of the errors
     * that are found in this file are emitted as events.  It resides under {@link #SAMPLE_BASE_RESOURCE_PATH}.
     */
    public static final String SAMPLE_INVALID_FGDC_XML_SHAPE_FILE_MULTIPLE_ERRORS =
            SAMPLE_BASE_RESOURCE_PATH + "fgdc_sample_adopted_tmdls_jan12.shp.xml";


}
