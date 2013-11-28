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
package org.dataconservancy.dcs.contentdetection.droid;

import junit.framework.Assert;
import org.dataconservancy.dcs.contentdetection.impl.droid.DroidIdentifier;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Test for DroidIdentifier class
 */
public class DroidIdentiferTest extends DroidBaseUnitTest{

    /**
     * Test to ensure that PDF format detection attempt returns expected PRONOM output
     * @throws URISyntaxException
     */
    @Test
    public void testDetectPdfFormat() throws URISyntaxException {
        File file = new File(this.getClass().getResource(PDF_FILE).toURI());
        DroidIdentifier droidIdentifier = new DroidIdentifier();
        IdentificationResultCollection irc = droidIdentifier.detectFormat(file);
        Assert.assertTrue(0 < irc.getResults().size());
        IdentificationResult result = irc.getResults().get(0);

        Assert.assertTrue("application/pdf".equals(result.getMimeType()));
        Assert.assertTrue("fmt/18".equals(result.getPuid()));
        Assert.assertTrue("Acrobat PDF 1.4 - Portable Document Format".equals(result.getName()));
    }
}
