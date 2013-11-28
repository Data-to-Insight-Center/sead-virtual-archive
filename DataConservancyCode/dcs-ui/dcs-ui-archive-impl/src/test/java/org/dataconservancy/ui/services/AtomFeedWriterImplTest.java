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
package org.dataconservancy.ui.services;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.io.InputStream;

/**
 * Tests that the AtomFeedWriterImpl is producing the expected feed contents from DCS Events.
 */
public class AtomFeedWriterImplTest {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String FAILED_FEED = "<feed xmlns=\"http://www.w3.org/2005/Atom\">\n" +
            "  <id>http://dataconservancy.org/ingest/status/foo</id>\n" +
            "  <title type=\"text\">Status event feed for ingest foo</title>\n" +
            "  <updated>2012-02-02T21:41:56.583Z</updated>\n" +
            "  <author>\n" +
            "    <name>DCS ingest service</name>\n" +
            "  </author>\n" +
            "  <entry>\n" +
            "    <updated>2012-02-02T21:41:56.583Z</updated>\n" +
            "    <title type=\"text\">ingest.fail</title>\n" +
            "    <content type=\"text\">&lt;stacktrace></content>\n" +
            "    <summary type=\"text\">Ingest failed: &lt;reason></summary>\n" +
            "    <link href=\"failed-dcs-entity-id\" rel=\"related\"/>\n" +
            "  </entry>\n" +
            "</feed>";

    private static final String SUCCESS_FEED = "<feed xmlns=\"http://www.w3.org/2005/Atom\">\n" +
            "  <id>http://dataconservancy.org/ingest/status/foo</id>\n" +
            "  <title type=\"text\">Status event feed for ingest foo</title>\n" +
            "  <updated>2012-02-02T21:41:56.794Z</updated>\n" +
            "  <author>\n" +
            "    <name>DCS ingest service</name>\n" +
            "  </author>\n" +
            "  <entry>\n" +
            "    <updated>2012-02-02T21:41:56.794Z</updated>\n" +
            "    <title type=\"text\">ingest.complete</title>\n" +
            "    <summary type=\"text\">Ingest success!</summary>\n" +
            "    <link href=\"success-dcs-entity-id\" rel=\"related\"/>\n" +
            "  </entry>\n" +
            "</feed>";

    @BeforeClass
    public static void setUp() {
        XMLUnit.setIgnoreWhitespace(true);
    }


    @Test
    public void testFailureEvent() throws Exception {
        AtomFeedWriterImpl underTest = new AtomFeedWriterImpl();

        DcsEvent fail = new DcsEvent();
        fail.setDate(DateTime.now().toDateTimeISO().toString());
        fail.setDetail("Ingest failed: <reason>");
        fail.setEventType("ingest.fail");
        fail.setOutcome("<stacktrace>");
        fail.addTargets(new DcsEntityReference("failed-dcs-entity-id"));

        InputStream feed = underTest.toAtom("foo", fail);

        final String actualFeed = IOUtils.toString(feed);
        Diff d = new Diff(FAILED_FEED, actualFeed);
        d.overrideDifferenceListener(new IgnoreDateDifferenceListener());
        XMLAssert.assertXMLEqual(d, true);
    }

    @Test
    public void testSuccessEvent() throws Exception {
        AtomFeedWriterImpl underTest = new AtomFeedWriterImpl();

        DcsEvent complete = new DcsEvent();
        complete.setDate(DateTime.now().toDateTimeISO().toString());
        complete.setDetail("Ingest success!");
        complete.setEventType("ingest.complete");
        complete.addTargets(new DcsEntityReference("success-dcs-entity-id"));

        InputStream feed = underTest.toAtom("foo", complete);

        final String actualFeed = IOUtils.toString(feed);
        Diff d = new Diff(SUCCESS_FEED, actualFeed);
        d.overrideDifferenceListener(new IgnoreDateDifferenceListener());
        XMLAssert.assertXMLEqual(d, true);
    }

    private class IgnoreDateDifferenceListener implements DifferenceListener {

        @Override
        public int differenceFound(Difference difference) {
            if (difference.getControlNodeDetail().getNode().getParentNode().getNodeName().equals("updated") &&
                    difference.getTestNodeDetail().getNode().getParentNode().getNodeName().equals("updated")) {
                log.trace("Ignoring date difference in <updated> node. Expected: {} Actual: {}",
                        difference.getControlNodeDetail().getValue(), difference.getTestNodeDetail().getValue());
                return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            }

            return RETURN_ACCEPT_DIFFERENCE;
        }

        @Override
        public void skippedComparison(Node node, Node node1) {
            // Do nothing
        }
    }
}
