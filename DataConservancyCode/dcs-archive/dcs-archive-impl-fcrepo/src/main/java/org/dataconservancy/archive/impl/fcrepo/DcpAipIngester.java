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

import java.util.Collection;
import java.util.Set;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.request.Ingest;
import com.yourmediashelf.fedora.client.response.IngestResponse;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.impl.fcrepo.dto.FedoraDigitalObject;
import org.dataconservancy.archive.impl.fcrepo.mapper.CollectionMapper;
import org.dataconservancy.archive.impl.fcrepo.mapper.DUMapper;
import org.dataconservancy.archive.impl.fcrepo.mapper.EventMapper;
import org.dataconservancy.archive.impl.fcrepo.mapper.FileMapper;
import org.dataconservancy.archive.impl.fcrepo.mapper.ManifestationMapper;
import org.dataconservancy.archive.impl.fcrepo.xstream.FDOXStream;
import org.dataconservancy.archive.impl.fcrepo.xstream.FDOXStreamFactory;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ingests DCP XML-formatted AIP into Archive Services. Note: We can likely to
 * add an interface so as to handle other information models.
 */
public class DcpAipIngester {

    public static final String FEDORA_INGEST_FORMAT =
            "info:fedora/fedora-system:FOXML-1.1";

    private FedoraClient fedoraClient;

    // Creates the Java model equivalent of the DCP AIP.
    private DcsModelBuilder builder;

    // Logger
    final Logger log = LoggerFactory.getLogger(this.getClass());

    // Processes the XML for the FDOs that correspond to the DCS entities.
    // This is shareable and thread safe.
    public static FDOXStream xmlConverter = FDOXStreamFactory.newInstance();

    // TODO Likely the additional arguments will be useful when supporting
    //      multiple AIP formats but not for Y1P.

    /**
     * Constructs a object for ingesting AIPs into a Fedora repository.
     * 
     * @param client
     *        a client for connecting to a Fedora repository
     */
    public DcpAipIngester(FedoraClient client) {
        fedoraClient = client;
    }
    
    /**
     * Ingests the AIP.
     *
     * @param pkg
     *        the AIP package
     * @return true if successful
     */
    public void ingestPackage(InputStream pkg) throws AIPFormatException {

        Dcp aip = null;

        try {
            // Use the DCP model builder to process the DCP AIP into Java objects.
            // Assume the AIP is valid.
            if (builder == null) {
                builder = new DcsXstreamStaxModelBuilder();
            }
            aip = builder.buildSip(pkg);

            // TODO Alternative: Can we assume this has not been formally validated already.
            //aip = builder.buildSip(validator.validating(pkg));
        } catch (Exception e) {
            throw new AIPFormatException("Could not parse AIP.", e);
        }

        // TODO Question when are relations handled?
        //        To content model.
        //        To related objects.
        // TODO Record the PIDS of all new objects so we can removed
        //      all of them atomically if any fail. Optional

        // Process Deliverable Unit Entities
        DUMapper duMapper = new DUMapper(aip);
        Collection<DcsDeliverableUnit> duSet = aip.getDeliverableUnits();
        for (DcsDeliverableUnit du : duSet) {
            FedoraDigitalObject fdo = duMapper.map(du);
            ingestFDO(fdo);
        }

        ManifestationMapper manifestationMapper = new ManifestationMapper(aip);
        Collection<DcsManifestation> manifestationSet = aip.getManifestations();
        for (DcsManifestation manifestation : manifestationSet) {
            FedoraDigitalObject fdo = manifestationMapper.map(manifestation);
            ingestFDO(fdo);
        }

        CollectionMapper collectionMapper = new CollectionMapper(aip);
        Collection<DcsCollection> collectionSet = aip.getCollections();
        for (DcsCollection collection : collectionSet) {
            FedoraDigitalObject fdo = collectionMapper.map(collection);
            ingestFDO(fdo);
        }

        FileMapper fileMapper = new FileMapper(aip);
        Collection<DcsFile> fileSet = aip.getFiles();
        for (DcsFile file : fileSet) {
            FedoraDigitalObject fdo = fileMapper.map(file);
            ingestFDO(fdo);
        }
        
        EventMapper eventMapper = new EventMapper(aip);
        Collection<DcsEvent> eventSet = aip.getEvents();
        for (DcsEvent event : eventSet) {
            FedoraDigitalObject fdo = eventMapper.map(event);
            ingestFDO(fdo);
        }

    }

    private void ingestFDO(FedoraDigitalObject fdo) {

        // Convert the FDO to XML and create a client ingest request.
        String fdoAsXML = xmlConverter.toXML(fdo);
        Ingest ingestRequest = fedoraClient.ingest();
        ingestRequest.format(FEDORA_INGEST_FORMAT);
        ingestRequest.content(fdoAsXML);

        try {

            IngestResponse response = ingestRequest.execute(fedoraClient);
            int status = response.getStatus();

            // Log the status of the ingest.  This is likely of archival interest.
            log.info("Successfully archived FDO: " + fdo.getObjectPid());
            
            // TODO Is this a recordable archival event?
            //addIngestEvent(aip, ingestId);  No implementation just pseudo code.

        } catch (FedoraClientException e) {
            
            // Log the status of the ingest.  This information is likely of
            // archival interest so this log needs to be integrated into
            // records which are stored back into the archive.
            // TODO Temporarily defeated until we have a Fedora Bamboo setup.
            String message = "Repository client ingest failed. " +
                             "Failed to archive FDO: " + fdo.getObjectPid();
            log.info(message);
            throw new RuntimeException(message, e);

        }

    }

}
