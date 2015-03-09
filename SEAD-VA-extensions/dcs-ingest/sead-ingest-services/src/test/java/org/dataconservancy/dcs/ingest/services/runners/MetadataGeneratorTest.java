package org.dataconservancy.dcs.ingest.services.runners;

import org.dataconservancy.dcs.ingest.services.MetadataGenerator;
import org.dataconservancy.model.dcp.Dcp;
import org.junit.Test;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class MetadataGeneratorTest {

    @Test
    public void metadataGeneratorTest() throws Exception {
        Dcp dus = (ResearchObject)new SeadXstreamStaxModelBuilder().buildSip(getClass().getResourceAsStream("../rood_d1_sip.xml"));
        MetadataGenerator metaGen = new MetadataGenerator();

        String onlink = "http://seadva.d2i.indiana.edu:8181/dcs-nced/entity/239289";

        String xml = metaGen.toFGDC(dus, onlink);

        assertNotNull(xml);

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));

        Document doc = db.parse(is);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        String expression;
        Node node;
        expression = "/metadata/idinfo/citation/citeinfo/origin";
        node = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
        assertEquals(node.getTextContent(),"Kumar, Praveen");

        expression = "/metadata/idinfo/citation/citeinfo/pubdate";
        node = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
        assertNotNull(node.getTextContent());

        expression = "/metadata/idinfo/citation/citeinfo/title";
        node = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
        assertEquals(node.getTextContent(),"1");

        expression = "/metadata/idinfo/citation/citeinfo/onlink";
        node = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
        assertEquals(node.getTextContent(),"http://seadva.d2i.indiana.edu:8181/dcs-nced/entity/239289");

        expression = "/metadata/idinfo/descript/abstract";
        node = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
        assertEquals(node.getTextContent(),"Data compiled from various sources on 284 streams and rivers: " +
                "river morphology, river process, discharge, hydraulic geometry and grain size.");

        expression = "/metadata/metainfo/metc/cntinfo/cntperp/cntper";
        node = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
        assertEquals(node.getTextContent(),"Nguyen, Charles");
    }

}
