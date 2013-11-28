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
package org.dataconservancy.dcs.ingest.sip.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import org.dataconservancy.dcs.ingest.sip.XmlStreamValidator;

/**
 * Creates temp files to pre-validate streams using XSD Schema.
 */
public class TempFileXmlStreamValidator
        implements XmlStreamValidator {

    private static File tempDir;

    private static String prefix = "val.";

    private static String suffix = ".tmp";

    public void setTempDir(String path) {
        tempDir = new File(path);
    }

    private static ThreadLocal<Validator> validators;

    public TempFileXmlStreamValidator(String... schemaSrc) {
        SchemaFactory factory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Source[] sources = new Source[schemaSrc.length];

        for (int i = 0; i < schemaSrc.length; i++) {
            sources[i] = new StreamSource(getSchemaStream(schemaSrc[i]));
        }

        try {
            final Schema schema = factory.newSchema(sources);

            validators = new ThreadLocal<Validator>() {

                protected Validator initialValue() {
                    return schema.newValidator();
                }
            };

        } catch (SAXException e) {
            throw new RuntimeException("Could not create validation schema ", e);
        }
    }

    @SuppressWarnings("finally")
    public InputStream validating(InputStream src) {
        try {
            TempStream temp = TempStream.getInstance(src);
            try {
                validators.get().validate(new StreamSource(temp.readStream()));
            } catch (Exception e) {
                try {
                    temp.close();
                } finally {
                    throw e;
                }
            }
            return temp;
        } catch (Exception e) {
            throw new RuntimeException("Error validating stream", e);
        } finally {
            validators.get().reset();
        }
    }

    private InputStream getSchemaStream(String src) {
        try {
            return new FileInputStream(src);
        } catch (FileNotFoundException e) {
            InputStream stream = this.getClass().getResourceAsStream(src);
            if (stream == null) {
                throw new RuntimeException("Could not find schema " + src);
            }
            return stream;
        }
    }

    protected static class TempStream
            extends FilterInputStream {

        protected final File tempFile;

        private TempStream(File file)
                throws IOException {
            super(new FileInputStream(file));
            tempFile = file;

        }

        private static TempStream getInstance(InputStream in)
                throws IOException {
            File file = File.createTempFile(prefix, suffix, tempDir);
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            out.flush();
            return new TempStream(file);
        }

        public InputStream readStream() throws IOException {
            return new FileInputStream(tempFile);
        }

        @Override
        public int read() throws IOException {
            return detectEnd(super.read());
        }

        @Override
        public int read(byte[] b) throws IOException {
            return detectEnd(super.read(b));
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return detectEnd(super.read(b, off, len));
        }

        private int detectEnd(int bytes) throws IOException {
            if (bytes == -1) {
                close();  
            }

            return bytes;
        }

        public void close() throws IOException {
            try {
                super.close();
            } finally {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }

        @Override
        public void finalize() throws Throwable {
            try {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            } finally {
                super.finalize();
            }
        }
    }
}
