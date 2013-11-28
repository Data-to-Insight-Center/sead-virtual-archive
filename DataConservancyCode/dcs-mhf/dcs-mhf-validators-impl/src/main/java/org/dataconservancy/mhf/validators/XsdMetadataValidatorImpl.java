package org.dataconservancy.mhf.validators;

import org.apache.commons.io.input.TeeInputStream;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.validation.api.ValidationException;
import org.dataconservancy.mhf.validators.util.ResourceResolver;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates XSD instance documents.  Our notion of XSD validation extends beyond XML validation: we stipulate that
 * all of the referenced schema components (e.g. from an &lt;xsd:include>) must be resolvable.
 */
public class XsdMetadataValidatorImpl extends XmlMetadataValidatorImpl {

    private ResourceResolver<DcsMetadataScheme> schemeResolver;
    private SchemaFactory xsdSchemaFactory;
    
    public XsdMetadataValidatorImpl(SchemaFactory schemaFactory, SchemaFactory xsdSchemaFactory,
                                    ResourceResolver<DcsMetadataFormat> formatResourceResolver,
                                    ResourceResolver<DcsMetadataScheme> schemeResourceResolver,
                                    MetadataHandlingEventManager eventManager) {
        super(schemaFactory, formatResourceResolver, schemeResourceResolver, eventManager);
        this.xsdSchemaFactory = xsdSchemaFactory;
        this.schemeResolver = schemeResourceResolver;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation notes:<br/>
     * This method invokes the superclass implementation.  If the superclass validate method doesn't throw an exception,
     * then this implementation moves on and attempts to:<br/>
     * <ol>
     *     <li>Resolve each {@code <xsd:include>} in the XSD instance document to a {@code DcsMetadataScheme}</li>
     *     <li>Attempt to instantiate a {@code Schema} object for the XSD instance document</li>
     * </ol>
     * If neither of these steps succeed, a {@code ValidationException} is thrown.
     *
     * @param metadataInstance {@inheritDoc}
     * @throws ValidationException {@inheritDoc}
     */
    @Override
    public void validateInternal(final MetadataInstance metadataInstance, RegistryEntry<DcsMetadataFormat> entry,
                                     URL schemaUrl, Schema schema, URL baseUrl) throws Exception {

        // Keep a copy of the input stream when it's read by the superclass
        final ByteArrayOutputStream copyOut = new ByteArrayOutputStream();
        try {
            final TeeInputStream teeIn = new TeeInputStream(metadataInstance.getContent(), copyOut, true);

            super.validateInternal(new MetadataInstance() {
                @Override
                public String getFormatId() {
                    return metadataInstance.getFormatId();
                }

                @Override
                public InputStream getContent() {
                    return teeIn;
                }
            }, entry, schemaUrl, schema, baseUrl);
        } catch (IOException e) {
            throw new ValidationException("Could not get content from MetadataInstance. " + e.getMessage());
        }
        
        // The superclass thinks the instance is valid; the format Id should not be null, and the registry entry
        // and metadata scheme should be resolved without an exception.

        // Now we perform our additional validation routines

        // We are validating an XSD instance document.  We perform validation of the instance document in the
        // superclass, validating it just like any other XML instance document.  However, we go a step further,
        // attempting to create a Schema from the XSD instance document.  Then we attempt to manually resolve any
        // <xsd:include> elements, and fail the validation if those includes cannot be resolved.
        //
        // see: XsdMetadataValidatorImplTest#testValidateValidXsdWithIncludes
        // see: XsdMetadataValidatorImplTest#testValidateValidXsdWithMissingIncludes
        // see: section 4.2.1 of "XML Schema Part 1: Structures Second Edition" the most recent reference is:
        //      http://www.w3.org/TR/xmlschema-1/#src-include (specifically the paragraph titled "Schema
        //      Representation Constraint: Inclusion Constraints and Semantics)"


        // 1. resolve all imported schema documents
        Set<String> schemaLocations = new HashSet<String>();
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setValidating(false);
        parserFactory.setNamespaceAware(true);
        SAXParser parser = parserFactory.newSAXParser();

        DebuggingDefaultHandler dh = new DebuggingDefaultHandler(new XsdImportCollectorContentHandlerWrapper(
                new DefaultHandler(), schemaLocations));

        parser.parse(new ByteArrayInputStream(copyOut.toByteArray()), dh);

        String baseUrlString = "";
        if (baseUrl != null) {
            baseUrlString = baseUrl.toString();
        }
        
        for (String schemaLoc : schemaLocations) {

            if (schemeResolver.resolve(schemaLoc, baseUrlString) == null) {
                final String message = "Couldn't resolve included schema location " + schemaLoc;
                log.log(message);
                throw new ValidationException(message);
            }
        }

        // 2. Successfully instantiate a Schema from the XSD instance document
//        log.log("Instantiating a Schema from the following instance document:");
//        log.log(IOUtils.toString(new ByteArrayInputStream(copyOut.toByteArray())));
        SAXSource source = new SAXSource(new InputSource(new ByteArrayInputStream(copyOut.toByteArray())));
        if (source != null && baseUrl != null) {
            source.setSystemId(baseUrl.toString());
        }
        xsdSchemaFactory.newSchema(source);

    }

}
