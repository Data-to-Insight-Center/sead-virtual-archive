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
package org.dataconservancy.dcs.access.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.dataconservancy.archive.impl.elm.ElmArchiveStore;
import org.dataconservancy.dcs.access.impl.solr.DcsSolrAccessService;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 *
 * @goal seed-index
 * @phase generate-test-resources
 */
public class IndexArchiveMojo extends AbstractSeedMojo {

    /**
     * The solr home directory.
     *
     * @parameter expression="${solr.home}"
     * @required
     */
    private File solrHome;

    /**
     * The solr core configuration file
     *
     * @parameter expression="${solr.configfile}"
     * @required
     */
    private File solrCoreConfig;

    /**
     * The solr container name
     *
     * @parameter expression="${solr.name}"
     * @required
     */
    private String solrContainerName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final ElmArchiveStore archive = prepareArchive(getElmEntityDirectory(), getElmMetadataDirectory());

//  <bean id="solrServer" class="org.apache.solr.client.solrj.embedded.EmbeddedSolrServer">
//    <constructor-arg>
//      <bean class="org.apache.solr.core.CoreContainer">
//        <constructor-arg value="${solr.solr.home}"/>
//        <constructor-arg value="${solr.solr.config}"/>
//      </bean>
//    </constructor-arg>
//    <constructor-arg value="${solr.containername}"/>
//  </bean>

        final EmbeddedSolrServer solrServer;

        try {
            solrServer = new EmbeddedSolrServer(new CoreContainer(solrHome.getAbsolutePath(), solrCoreConfig),
                    solrContainerName);
        } catch (ParserConfigurationException e) {
            throw new MojoExecutionException(e.getMessage());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        } catch (SAXException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        final DcsSolrAccessService accessSvc;

        try {
            accessSvc = new DcsSolrAccessService(solrServer, archive);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        try {
            accessSvc.indexArchive();
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

    }
}
