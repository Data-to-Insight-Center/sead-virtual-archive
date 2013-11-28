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
package org.dataconservancy.dcs.index.dcpsolr.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.io.IOUtils.toInputStream;

/** Used for creating a solr installation */
public class SolrBootstrap {

    public static final String SOLR_PATH =
            "/org/dataconservancy/dcs/index/dcpsolr/";

    public static final String CONF_PATH =
            "/org/dataconservancy/dcs/index/dcpsolr/default/conf/";

    /**
     * Install solr if it is not already installed.
     * 
     * @param solrhome
     *        Solr home directory
     * @return String containing the solr home directory.
     * @throws IOException
     */
    public static String createIfNecessary(String solrhome) throws IOException {

        File confdir = new File(solrhome, "default" + File.separator + "conf");

        if (confdir.exists()) {
            return solrhome;
        }

        // Cannot list resources so have keep names here
        String[] filenames =
                new String[] {"elevate.xml", "mapping-ISOLatin1Accent.txt",
                        "protwords.txt", "solrconfig.xml", "spellings.txt",
                        "stopwords.txt", "synonyms.txt", "schema.xml",
                        "scripts.conf"};

        for (String name : filenames) {

            OutputStream os =
                    FileUtils.openOutputStream(new File(confdir, name));
            InputStream is =
                    SolrBootstrap.class.getResourceAsStream(CONF_PATH + name);
            IOUtils.copy(is, os);
            is.close();
            os.close();
        }

        OutputStream os =
                FileUtils.openOutputStream(new File(solrhome, "solr.xml"));
        InputStream is =
                SolrBootstrap.class.getResourceAsStream(SOLR_PATH + "solr.xml");
        
        copy(toInputStream(IOUtils.toString(is).replace("${solr.solr.home}",
                                                        solrhome)),
             os);
        is.close();
        os.close();

        return solrhome;
    }

}
