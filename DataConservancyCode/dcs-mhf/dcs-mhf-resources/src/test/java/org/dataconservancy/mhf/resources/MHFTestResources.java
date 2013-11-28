package org.dataconservancy.mhf.resources;

/**
 * Provides string constants for, and describes, the <em>test</em> classpath resources provided by the
 * {@code dcs-mhf-resources} module.  This includes XSD schemas for XML documents, and DTDs and XSDs included by XSD
 * schema documents.
 *
 * @see MHFResources
 */
public class MHFTestResources {

    /**
     * This is the base resource path that all sample metadata files reside under, including sample XML instance
     * documents.  Test classes will uses instance documents located under this resource path to test validation
     * scenarios.
     */
    public static final String SAMPLE_BASE_RESOURCE_PATH = MHFResources.SAMPLE_BASE_RESOURCE_PATH;

    /**
     * The resource path to an invalid FGDC XML instance document, {@code sample2.xml}.  It resides under
     * {@link #SAMPLE_BASE_RESOURCE_PATH}.
     */
    public static final String SAMPLE_INVALID_FGDC_XML_RESOURCE_PATH =
            MHFResources.SAMPLE_INVALID_FGDC_XML_RESOURCE_PATH;

    /**
     * The resource path to a valid FGDC XML instance document, {@code sample3-valid.xml}.  It resides under
     * {@link #SAMPLE_BASE_RESOURCE_PATH}.
     */
    public static final String SAMPLE_VALID_FGDC_XML_RESOURCE_PATH =
            MHFResources.SAMPLE_VALID_FGDC_XML_RESOURCE_PATH;

    /**
     * The resource path to a valid FGDC XML instance document, {@code valid-fgdc-with-maven-pom-schema-definition.xml}.
     * It resides under {@link #SAMPLE_BASE_RESOURCE_PATH}. This instance document may be validated against an FGDC
     * schema, even though it defines a {@code schemaLocation} to the Maven model schema in its {@code &lt;metadata&gt;}
     * element.  This file is meant to illustrate that when parsing is decoupled from validation, the
     * {@code schemaLocation} doesn't matter.
     */
    public static final String SAMPLE_VALID_FGDC_XML_DECLARING_MAVEN_SCHEMA_RESOURCE_PATH =
            MHFResources.SAMPLE_VALID_FGDC_XML_DECLARING_MAVEN_SCHEMA_RESOURCE_PATH;

    /**
     * The resource path to a valid, but empty, XSD instance document, {@code empty-xsd-schema.xsd}.  It resides under
     * {@link #SAMPLE_BASE_RESOURCE_PATH}.
     */
    public static final String SAMPLE_VALID_BUT_EMPTY_XSD_RESOURCE_PATH =
            MHFResources.SAMPLE_VALID_BUT_EMPTY_XSD_RESOURCE_PATH;

    /**
     * The resource path to an invalid XSD instance document, {@code invalid-xsd-schema.xsd}.  It resides under
     * {@link #SAMPLE_BASE_RESOURCE_PATH}.
     */
    public static final String SAMPLE_INVALID_XSD_RESOURCE_PATH =
            MHFResources.SAMPLE_INVALID_XSD_RESOURCE_PATH;

    /**
     * The resource path to an valid XSD instance document, {@code xsd-schema-with-non-existent-include.xsd}.  It
     * contains an {@code <xsd:include>} with a {@code schemaLocation} to a non-existent resource. It resides under
     * {@link #SAMPLE_BASE_RESOURCE_PATH}.
     */
    public static final String SAMPLE_VALID_XSD_MISSING_IMPORT =
            MHFResources.SAMPLE_VALID_XSD_MISSING_IMPORT;
    
    
    /**
     * The url pointing to a valid fgdc schema document with includes
     */
    public static final String SAMPLE_VALID_FGDC_SCHEMA_URL = "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd";
    
    /**
     * The base url for the fgdc schema used to resolve included schema files.
     */
    public static final String SAMPLE_FGDC_SCHEMA_BASE_URL = "http://www.fgdc.gov/schemas/metadata/";
}
