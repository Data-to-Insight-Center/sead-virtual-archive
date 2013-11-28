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
package org.dataconservancy.archive.impl.fcrepo;

import com.thoughtworks.xstream.XStream;

import org.custommonkey.xmlunit.XMLUnit;

import org.junit.Before;

import org.dataconservancy.archive.impl.fcrepo.xstream.FDOXStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract test class for XStream-based serialization tests.  Encapsulates XMLUnit settings, XStream converters,
 * the XStream driver, and XStream aliases.
 *
 * @see com.thoughtworks.xstream.XStream
 * @see com.thoughtworks.xstream.converters.Converter
 */
public abstract class AbstractXstreamConverterTest {

    //static final String XMLNS = DcpModelVersion.VERSION_1_0.getXmlns();
    final Logger log = LoggerFactory.getLogger(this.getClass());    
    XStream x;

    @Before
    public void setUp() {
        // XMLUnit options shared by all the tests
        XMLUnit.setIgnoreWhitespace(true);        
        XMLUnit.setIgnoreComments(true);

        // XStream instance, shared by all the tests
        x = FDOXStreamFactory.newInstance();
    }

}
