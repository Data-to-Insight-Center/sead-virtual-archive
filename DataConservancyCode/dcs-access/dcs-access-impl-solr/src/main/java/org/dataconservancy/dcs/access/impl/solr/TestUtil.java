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
package org.dataconservancy.dcs.access.impl.solr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * See {@link org.dataconservancy.dcs.index.dcpsolr}
 */

@Deprecated
public class TestUtil {

    /**
     * @return temp directory holding solr install
     * @throws IOException
     */
    public static File createSolrTestInstall() throws IOException {
        File solrhome = FileUtil.createTempDir("solrhome");

        // Cannot list resources so have keep names here
        String[] filenames =
                new String[] {"elevate.xml", "mapping-ISOLatin1Accent.txt",
                        "protwords.txt", "solrconfig.xml", "spellings.txt",
                        "stopwords.txt", "synonyms.txt", "schema.xml"};

        File confdir = new File(solrhome, "conf");
        confdir.mkdir();

        for (String name : filenames) {
            OutputStream os = new FileOutputStream(new File(confdir, name));
            InputStream is = TestUtil.class.getResourceAsStream("conf/" + name);
            FileUtil.copy(is, os);
            is.close();
            os.close();
        }

        return solrhome;
    }
}
