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

import org.dataconservancy.access.connector.DcsConnectorFault;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Parses a DCS deposit feed (which is a set of DcsEvents serialized as an Atom feed, one Atom &lt;entry> per
 * DcsEvent) using a DOM parser.
 *
 * TODO: uncouple the parser implementation from the serialization format
 */
public class AtomDepositDocumentParser implements DepositDocumentParser {

    private static final String ATOM_URI = "http://www.w3.org/2005/Atom";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DocumentBuilderFactory builderFactory;

    private final ArchiveUtil archiveUtil;

    public AtomDepositDocumentParser(ArchiveUtil archiveUtil) {
        this.builderFactory = DocumentBuilderFactory.newInstance();

        if (archiveUtil == null) {
            throw new IllegalArgumentException("ArchiveUtil must not be null.");
        }
        this.archiveUtil = archiveUtil;
    }

    public AtomDepositDocumentParser(ArchiveUtil archiveUtil, DocumentBuilderFactory builderFactory) {
        if (archiveUtil == null) {
            throw new IllegalArgumentException("ArchiveUtil must not be null.");
        }
        this.archiveUtil = archiveUtil;

        if (builderFactory == null) {
            throw new IllegalArgumentException("DocumentBuilderFactory must not be null.");
        }
        this.builderFactory = builderFactory;
    }

    @Override
    public DepositDocument parse(InputStream in) throws IOException {
        Document atomFeed = null;

        try {
            final DocumentBuilder db = builderFactory.newDocumentBuilder();
            atomFeed = db.parse(in);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Unable to configure DOM parser: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        final AtomDepositDocument depositDoc = new AtomDepositDocument();

        // Get the Atom <feed> ID and use it for the Deposit Document ID
        NodeList entries = atomFeed.getElementsByTagName("id");
        if (entries.getLength() > 0) {
            log.trace("Found {} Atom <id> elements", entries.getLength());
            for (int i = 0; i < entries.getLength(); i++) {
                Element idEntry = (Element) entries.item(i);
                if (idEntry.getParentNode().equals(atomFeed.getDocumentElement())) {
                    depositDoc.id = idEntry.getTextContent().trim();
                    log.trace("Found deposit ID: {}", depositDoc.id);
                    // Found the deposit id, so we can short-circuit
                    break;
                }
            }
        }

        // Get the Atom <entry> elements; each entry represents a DcsEvent that occurred for this Deposit.
        entries = atomFeed.getElementsByTagName("entry");
        if (entries.getLength() > 0) {
            log.trace("Found {} Atom entries", entries.getLength());
        } else {
            log.debug("Found no Atom entries");
            return null;
        }

        // For each Atom <entry>, determine if the DcsEvent is an ingest.complete or ingest.fail event.
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            NodeList titles = entry.getElementsByTagName("title");

            if (titles.getLength() > 0) {
                String title = titles.item(0).getTextContent().trim();

                log.trace("Found Entry title {}", title);

                // If it's complete, find the root DU of the deposit
                if (title.equals("ingest.complete")) {
                    DcsDeliverableUnit root = null;
                    try {
                        root = getRootDuOfDeposit(entry);
                    } catch (DcsConnectorFault dcsConnectorFault) {
                        throw new RuntimeException("Error connecting to the DCS: " + dcsConnectorFault.getMessage(),
                                dcsConnectorFault);
                    } catch (InvalidXmlException e) {
                        throw new RuntimeException("Unable to parse invalid entity XML: " + e.getMessage(), e);
                    }

                    // Deposit is complete
                    depositDoc.isComplete = true;
                    depositDoc.root = root;
                    depositDoc.isSuccessful = true;

                    if (root == null) {
                        log.debug("Found ingest.complete event, but unable to find the root Deliverable Unit");
                    }
                    // found ingest.complete, so we can short-circuit
                    break;
                } else if (title.equals("ingest.fail")) {
                    depositDoc.isComplete = true;
                    depositDoc.isSuccessful = false;
                    // found ingest.fail, so we can short-circuit
                    break;
                }
            }
        }

        // For each Atom <link>, retrieve the associated entity
        entries = atomFeed.getElementsByTagName("link");
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            String href = entry.getAttribute("href");
            String rel = entry.getAttribute("rel");
            if (rel != null && href != null) {
                rel = rel.trim();
                href = href.trim();
                if (rel.equals("related")) {
                    DcsEntity e = archiveUtil.getEntity(href);
                    if (e != null) {
                        depositDoc.entities.add(e);
                    }
                }
            }
        }
        return depositDoc;
    }

    /**
     * Parse ingest.complete atom feed to find root du of deposit.
     * If the entities have not yet been indexed, null is returned.
     *
     * @param entry
     * @return root du or null
     * @throws org.dataconservancy.access.connector.DcsConnectorFault
     * @throws java.io.IOException
     * @throws org.dataconservancy.model.builder.InvalidXmlException
     *
     */
    private DcsDeliverableUnit getRootDuOfDeposit(Element entry) throws DcsConnectorFault, IOException,
            InvalidXmlException {
        NodeList links = entry.getElementsByTagName("link");

        log.trace("Determining root DU of deposit");

        if (links.getLength() < 1) {
            log.trace("No Atom <link> elements found for {}:{}, unable to determine root DU of deposit.",
                    entry.getNamespaceURI(), entry.getTagName());
        } else {
            log.trace("Found {} Atom <link> elements for {}:{}",
                    new Object[]{links.getLength(), entry.getNamespaceURI(), entry.getTagName()});
        }

        Set<DcsDeliverableUnit> dus = new HashSet<DcsDeliverableUnit>();

        for (int i = 0; i < links.getLength(); i++) {
            Element link = (Element) links.item(i);

            String archive_id = link.getAttribute("href");

            DcsEntity entity = archiveUtil.getEntity(archive_id);

            if (entity == null) {
                log.trace("Unable to retrieve candidate root entity: {}", archive_id);
                continue;
            }

            if (entity instanceof DcsDeliverableUnit) {
                dus.add((DcsDeliverableUnit) entity);
                log.trace("Found candidate root DU: {}", entity.getId());
            }
        }

        DcsDeliverableUnit root = archiveUtil.determineDepositRoot(dus);
        return root;
    }

    private class AtomDepositDocument implements DepositDocument {
        private boolean isComplete = false;
        private boolean isSuccessful = false;
        private DcsDeliverableUnit root = null;
        private String id = "";
        private Set<DcsEntity> entities = new HashSet<DcsEntity>();

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean isComplete() {
            return isComplete;
        }

        @Override
        public boolean isSuccessful() {
            return isSuccessful;
        }

        @Override
        public DcsDeliverableUnit getRoot() {
            return root;
        }

        @Override
        public Set<DcsEntity> getEntities() {
            return entities;
        }
    }
}
