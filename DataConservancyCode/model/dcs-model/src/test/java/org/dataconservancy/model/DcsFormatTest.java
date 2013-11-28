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
package org.dataconservancy.model;

import org.dataconservancy.model.dcs.DcsFormat;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class DcsFormatTest {
    @Test
    public void testParseDcsFormat() {
        DcsFormat dcsFormat = new DcsFormat();
        dcsFormat.setVersion("unknown");
        dcsFormat.setSchemeUri("http://www.nationalarchives.gov.uk/PRONOM/");
        dcsFormat.setName("Plain Text File");
        dcsFormat.setFormat("x-fmt/111");

        DcsFormat parsedDcsFormat = DcsFormat.parseDcsFormat(dcsFormat.toString());
        Assert.assertEquals(dcsFormat, parsedDcsFormat);

        parsedDcsFormat = DcsFormat.parseDcsFormat("DcsFormat   {   format ='x-fmt/111',    name='Plain Text File', " +
                "schema uri='http://www.nationalarchives.gov.uk/PRONOM/   '     ,    version='unknown   '}");

        Assert.assertEquals(dcsFormat, parsedDcsFormat);

    }
}
