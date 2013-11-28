/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.ui.it.support;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the workings of JSoup, which is used to parse HTML responses from the DC UI in integration tests.
 */
public class JsoupTest {

    private static final String HTML_RESOURCE = "/jsoup-test.html";

    private static File HTML;

    @BeforeClass
    public static void setupFile() {
        URL u = JsoupTest.class.getResource(HTML_RESOURCE);
        assertNotNull(u);
        HTML = new File(u.getPath());
        assertTrue(HTML.exists());
    }

    @Test
    public void testJsoupSimple() throws Exception {
        final Document dom = Jsoup.parse(HTML, "UTF-8");
        assertNotNull(dom);

        Elements elements = dom.select("#header");
        assertNotNull(elements);
        assertEquals(1, elements.size());

        elements = dom.select("#dc-flash");
        assertNotNull(elements);
        assertEquals(1, elements.size());
    }
}
