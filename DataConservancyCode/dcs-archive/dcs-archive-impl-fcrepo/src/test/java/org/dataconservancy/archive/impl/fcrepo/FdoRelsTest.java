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

import java.io.InputStream;

import org.junit.Test;

import org.dataconservancy.archive.impl.fcrepo.semantic.FDORels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FdoRelsTest {

    private static final String VALID_RELSEXT_XML = "/validRELS.xml";

    @Test
    public void testGetDCSEntityType() {
        
        InputStream validXML = getStream(VALID_RELSEXT_XML);
        
        FDORels relsChecker = new FDORels();
        String value = relsChecker.getDCSEntityType(validXML);
        assertNotNull("Valid RELS parse succeeded.", value);
        assertEquals("Found DCS File.", "info:fedora/dcs:File", value);

    }
    
    /*
    @Test
    public void testGetRels() {
        
        InputStream is = getStream(VALID_RELSEXT_XML);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try {
          
        //System.out.println(convertStreamToString(is));
        //HierarchicalStreamReader reader = new BinaryStreamReader(is);
        QNameMap qm = new QNameMap();
        StaxDriver sd = new StaxDriver(qm);
        qm.registerMapping(new QName("http://www.w3.org/1999/02/22-rdf-syntax-ns#","RDF","rdf"), "RDF");
        qm.registerMapping(new QName("http://www.w3.org/1999/02/22-rdf-syntax-ns#","resource","rdf"), "resource");
        qm.registerMapping(new QName("http://www.w3.org/1999/02/22-rdf-syntax-ns#","Description","rdf"), "Description");
        qm.registerMapping(new QName("info:fedora/fedora-system:def/model#","hasModel","fedora-model"), "fedora-model");

        String RDF_OBJECT = "RDF";
        x.alias(RDF_OBJECT, FDOEmbeddedRDF.class);
        x.registerConverter(new FDOEmbeddedRDFConverter());
        qnames.registerMapping(new QName(defaultNSURI, DIGITAL_OBJECT, defaultNSPrefix),
                               FedoraDigitalObject.class);
        qnames.registerMapping(new QName(defaultNSURI, DIGITAL_OBJECT, defaultNSPrefix),
                               DIGITAL_OBJECT);

        XMLInputFactory xmlif = sd.getInputFactory();
        xmlif.setProperty(xmlif.IS_NAMESPACE_AWARE, true);
        //XMLStreamReader xmlsr = xmlif.createXMLStreamReader(is);
        //HierarchicalStreamReader reader = new StaxReader(qm, xmlsr);
        HierarchicalStreamReader reader = sd.createReader(is);
        
        //XMLOutputFactory xmlof = XMLOutputFactory.newFactory();
        XMLOutputFactory xmlof = sd.getOutputFactory();
        //xmlof.setProperty(xmlof.IS_REPAIRING_NAMESPACES, true);
        XMLStreamWriter xmlsw = xmlof.createXMLStreamWriter(os);
        HierarchicalStreamWriter writer = new StaxWriter(qm, xmlsw, false, true);
        
        XStream x = new XStream(sd);
        Object testObject = x.fromXML(is);
        System.out.println("Processed Test");
        
        } catch (Exception e) {
            System.out.println("RELS Crapped out." + e);
            e.printStackTrace();
        }
        
    }
    */
    
    private InputStream getStream(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new RuntimeException("no resource found for '" + path + "'");
        }
        return stream;
    }

}
