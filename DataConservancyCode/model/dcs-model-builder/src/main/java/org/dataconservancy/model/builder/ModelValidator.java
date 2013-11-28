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
package org.dataconservancy.model.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <em>DO NOT USE</em>
 *
 * Validates model XML
 *
 * Class is package-local as it is not working properly.
 */
class ModelValidator {

    private final static String SCHEMA_RESOURCE = "/schema/dcp.xsd";

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    private final Schema dcpSchema;

    public ModelValidator() {
        final ErrorCollector schemaErrorCollector = new ErrorCollector();
        schemaFactory.setErrorHandler(schemaErrorCollector);
        final URL schemaUrl = this.getClass().getResource(SCHEMA_RESOURCE);
        if (schemaUrl == null) {
            final String msg = "Could not load schema classpath resource '" + SCHEMA_RESOURCE + "'";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        
        try {
            dcpSchema = schemaFactory.newSchema(schemaUrl);
        } catch (SAXException e) {
            final String msg = "Error parsing schema '" + schemaUrl.toString() + "': " + e.getMessage();
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }
    }

    /**
     * Validates the supplied inputstream.  Because it exhausts the supplied inputstream, it returns a new
     * inputstream, with the same, byte for byte, content.
     * <p>
     * Any XML can be supplied to this method.  DCP packaged XML will be validated as-is.  DCP entity fragments
     * will be wrapped in DCP packaging prior to validation. 
     *
     * @param xmlIn
     * @return
     * @throws InvalidXmlException
     */
    public InputStream validate(InputStream xmlIn) throws InvalidXmlException {
        //xmlIn = new CopyingInputStream(xmlIn);

        final Validator v = dcpSchema.newValidator();
        final ErrorCollector errors = new ErrorCollector();
        v.setErrorHandler(errors);
        try {
            final XMLInputFactory readerFactory = XMLInputFactory.newInstance();
            final XMLOutputFactory writerFactory = XMLOutputFactory.newInstance();
            final XMLEventReader eventReader = readerFactory.createXMLEventReader(xmlIn);
            final WrappingEventReader wrappedReader = new WrappingEventReader(eventReader);
            final StAXSource source = new StAXSource(wrappedReader);
            final StAXResult result = new StAXResult(writerFactory.createXMLStreamWriter(System.err));
            v.validate(source, result);
        } catch (SAXException e) {
            log.debug("Validation failed: " + e.getMessage(), e);            
            final InvalidXmlException ixe = new InvalidXmlException();
            for (SAXParseException spe : errors) {
                ixe.addErrorMessage(spe.getMessage());
            }
            throw ixe;
        } catch (IOException e) {
            log.debug("Error reading underlying XML stream: " + e.getMessage(), e);
            throw new RuntimeIOException(e);
        } catch (XMLStreamException e) {
            log.debug("Error reading underlying XML stream: " + e.getMessage(), e);
            throw new RuntimeIOException(e);
        }

        if (errors.iterator().hasNext()) {
            log.debug("Validation failed.");
            final InvalidXmlException ixe = new InvalidXmlException();
            for (SAXParseException spe : errors) {
                ixe.addErrorMessage(spe.getMessage());
            }
            throw ixe;
        }


        //return ((CopyingInputStream)xmlIn).getCopy();
        return null;
    }

    private class ErrorCollector implements ErrorHandler, Iterable<SAXParseException> {
        private ArrayList<SAXParseException> errors = new ArrayList<SAXParseException>();

        @Override
        public void warning(SAXParseException e) throws SAXException {
//            errors.add(e);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            errors.add(e);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            errors.add(e);
        }

        @Override
        public Iterator<SAXParseException> iterator() {
            return errors.iterator();
        }
    }
    
    private class CopyingInputStream extends InputStream {
        private final static int BUF_SIZE = 4096;
        private ByteArrayOutputStream copyTo = new ByteArrayOutputStream(BUF_SIZE);
        private InputStream copyFrom;

        private CopyingInputStream(InputStream copyFrom) {
            this.copyFrom = copyFrom;
        }

        private InputStream getCopy() {
            return new ByteArrayInputStream(copyTo.toByteArray());
        }

        @Override
        public int read() throws IOException {
            int result = copyFrom.read();
            copyTo.write(result);
            return result;
        }

        @Override
        public int read(byte[] bytes) throws IOException {
            int count = copyFrom.read(bytes);
            if (count > 0) {
                copyTo.write(bytes, 0, count);
            }
            return count;
        }

        @Override
        public int read(byte[] bytes, int off, int len) throws IOException {
            int count = copyFrom.read(bytes, off, len);
            if (count > 0) {
                copyTo.write(bytes, off, len);
            }
            return count;
        }

        @Override
        public long skip(long l) throws IOException {
            return copyFrom.skip(l);
        }

        @Override
        public int available() throws IOException {
            return copyFrom.available();
        }

        @Override
        public void close() throws IOException {
            copyFrom.close();
            copyTo.close();
        }

        @Override
        public void mark(int i) {
            copyFrom.mark(i);
        }

        @Override
        public void reset() throws IOException {
            copyFrom.reset();
        }

        @Override
        public boolean markSupported() {
            return copyFrom.markSupported();
        }
    }    
}
