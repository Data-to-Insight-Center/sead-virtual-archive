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
package org.dataconservancy.dcs.integration.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.dataconservancy.dcs.util.HttpHeaderUtil;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static org.dataconservancy.dcs.integration.support.Interpolator.interpolate;
import static org.dataconservancy.deposit.sword.extension.SWORDExtensionFactory.Headers.PACKAGING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ManualDepositIT {

    private static final Logger log = LoggerFactory.getLogger(ManualDepositIT.class);

    private static File baseDir;

    private static String sampleFilePath;

    private static HttpClient client;

    private final static String ATOM_NS = "http://www.w3.org/2007/app";

    private final static Properties props = new Properties();

    private final String baseUrl = interpolate(new StringBuilder("${dcs.baseurl}/"), 0, props).toString();

    private final String serviceDocUrl = baseUrl + "deposit/";

    private final String sipPostUrl = baseUrl + "sip";

    @BeforeClass
    public static void init() throws IOException {
        initFiles();

        ClassPathXmlApplicationContext appContext =
                new ClassPathXmlApplicationContext(new String[] {
                        "depositClientContext.xml", "classpath*:org/dataconservancy/config/applicationContext.xml"});

        client = (HttpClient) appContext.getBean("httpClient");
    }

    @BeforeClass
    public static void loadProperties() throws IOException {
        final URL defaultProps = ManualDepositIT.class.getResource("/default.properties");
        assertNotNull("Could not resolve /default.properties from the classpath.", defaultProps);
        assertTrue("default.properties does not exist.", new File(defaultProps.getPath()).exists());
        props.load(defaultProps.openStream());
    }

    @Test
    public void testDepositContextUp() throws IOException {
        HttpGet req = new HttpGet(baseUrl);
        HttpResponse resp = client.execute(req);
        assertEquals("Unable to connect to container url " + baseUrl, 200, resp.getStatusLine().getStatusCode());
    }

    /**
     * Ensure that the <code>href</code> attribute values for &lt;collection&gt;s are valid URLs.
     */
    @Test
    public void testServiceDocCollectionUrls() throws IOException, XPathExpressionException {
        final HttpGet req = new HttpGet(serviceDocUrl);
        final HttpResponse resp = client.execute(req);
        assertEquals("Unable to retrieve atompub service document " + serviceDocUrl,
                200, resp.getStatusLine().getStatusCode());

        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if ("app".equals(prefix) || prefix == null || "".equals(prefix)) {
                    return ATOM_NS;
                }
                throw new RuntimeException("Unknown xmlns prefix: '" + prefix + "'");
            }

            @Override
            public String getPrefix(String nsUri) {
                if (ATOM_NS.equals(nsUri)) {
                    return "app";
                }
                throw new RuntimeException("Unknown xmlns uri '" + nsUri + "'");
            }

            @Override
            public Iterator<String> getPrefixes(String s) {
                ArrayList<String> prefixes = new ArrayList<String>();
                prefixes.add("app");
                return prefixes.iterator();
            }
        });


        final String xpathExpression = "//app:collection/@href";
        final NodeList collectionHrefs = (NodeList)
                xpath.evaluate(xpathExpression, new InputSource(resp.getEntity().getContent()), XPathConstants.NODESET);

        assertTrue("No atompub collections found in service document " + serviceDocUrl + " (xpath search '"
                + xpathExpression + "' yielded no results.",                                
                collectionHrefs != null && collectionHrefs.getLength() > 0);

        for (int i = 0; i < collectionHrefs.getLength(); i++) {
            final String collectionUrl = collectionHrefs.item(i).getNodeValue();
            assertNotNull("atompub collection url was null.", collectionUrl);
            assertTrue("atompub collection url was the empty string.", collectionUrl.trim().length() > 0);
            new URL(collectionUrl);
            assertTrue("Expected atompub collection url to start with " + serviceDocUrl +
                    " (collection url was: " + collectionUrl, collectionUrl.startsWith(serviceDocUrl));
        }
    }

    @Ignore
    @Test
    public void manualPerfTestReferenced() throws Exception {
        File file = createReferenceSipFile(false);

        long start = new Date().getTime();
        for (int i = 0; i < 1000; i++) {
            doDeposit(file);
            System.out.println(i);
        }
        long end = new Date().getTime();

        long elapsed = (end - start) / (1000 * 1000);

        log.info(String.format("Upload: %s sips per second", elapsed));

    }

    private void doDeposit(File file) throws Exception {

        HttpPost post = new HttpPost(sipPostUrl);
        post.setHeader(HttpHeaderUtil.CONTENT_TYPE, "application/xml");
        post.setHeader(PACKAGING, "http://dataconservancy.org/schemas/dcp/1.0");

        //FileInputStream fis = new FileInputStream(file);
        //post.setEntity(new InputStreamEntity(fis, file.length()));
        post.setEntity(new FileEntity(file, "application/xml"));

        HttpResponse response = client.execute(post);

        int code = response.getStatusLine().getStatusCode();
        if (code >= 200 && code <= 202) {
            response.getAllHeaders();
            HttpEntity responseEntity = response.getEntity();
            InputStream content = responseEntity.getContent();
            try {
                IOUtils.toByteArray(content);
            } finally {
                content.close();
            }
        } else {
            throw new RuntimeException("Unexpected error code " + code);
        }
    }

    private static File createReferenceSipFile(boolean extant)
            throws IOException {
        Dcp sip = new Dcp();

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("example:/du");
        du.setTitle("title");

        DcsManifestation man = new DcsManifestation();
        man.setId("example:/man");
        man.setDeliverableUnit("example:/du");

        DcsManifestationFile dmf = new DcsManifestationFile();
        dmf.setRef(new DcsFileRef("example:/file"));
        man.addManifestationFile(dmf);

        DcsFile file = new DcsFile();
        file.setId("example:/file");
        file.setSource("file://" + sampleFilePath);
        file.setName("file.png");
        file.setExtant(extant);

        sip.addDeliverableUnit(du);
        sip.addManifestation(man);
        sip.addFile(file);

        String sipFileName = "reference_" + extant + ".xml";
        File sipFile = new File(baseDir, sipFileName);

        OutputStream out = FileUtils.openOutputStream(sipFile);
        new DcsXstreamStaxModelBuilder().buildSip(sip, out);
        new DcsXstreamStaxModelBuilder()
                .buildSip(sip, new FileOutputStream("/tmp/dcp.xml"));

        out.close();

        log.info(String.format("Created example sip file %s", sipFile
                .getAbsoluteFile()));

        return sipFile;

    }

    private static void initFiles() throws IOException {
        final String fileName = "file.1.0.png";
        baseDir =
                new File(System.getProperty("java.io.tmpdir"), UUID
                        .randomUUID().toString());

        File file = new File(baseDir, fileName);
        sampleFilePath = file.getAbsolutePath();

        OutputStream out = FileUtils.openOutputStream(file);
        InputStream in =
                FileRoundTripIT.class.getResourceAsStream("/" + fileName);
        IOUtils.copy(in, out);
        out.close();
    }

    @AfterClass
    public static void cleanUp() {
        FileUtils.deleteQuietly(baseDir);
    }
}
