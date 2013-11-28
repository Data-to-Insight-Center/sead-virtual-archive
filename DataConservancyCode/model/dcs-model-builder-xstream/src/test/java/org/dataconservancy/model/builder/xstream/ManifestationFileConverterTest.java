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

import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ManifestationFileConverterTest extends AbstractXstreamConverterTest {

    private static final String REF = "urn:sdss:12345/FITS_FILE";
    private static final String PATH = "/scans/5/";
    private static final String XML = "<manifestationFile xmlns=\"" + XMLNS + "\" ref=\"" + REF + "\">\n" +
            "        <path>" + PATH + "</path>\n" +
            "      </manifestationFile>";

    @Test
    public void testMarshal() throws Exception {
        final DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(REF));
        mf.setPath(PATH);

        XMLAssert.assertXMLEqual(XML, x.toXML(mf));
        XMLAssert.assertXMLEqual(XML, x.toXML(x.fromXML(XML)));
    }

    @Test
    public void testUnmarshal() throws Exception {
        final DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(REF));
        mf.setPath(PATH);

        assertEquals(mf, x.fromXML(XML));
        assertEquals(mf, x.fromXML(x.toXML(mf)));
    }
}
