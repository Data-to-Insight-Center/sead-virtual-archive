package org.dataconservancy.ui.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.impl.SimpleLog;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.BusinessObjectMap;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Generate a {@link BusinessObjectMap} from objects in the archive.
 * <p/>
 * The graph will always contain the provided object as the top level object in
 * the graph. Only if the object itself is in archive, will further archive
 * look-ups be performed on this children. Only children who are in the archive
 * will be reported in the map.
 *
 * For objects (MetadataFile and SubCollection) who are still in the process of
 * being ingest (ie has PENDING deposit status), the service can be told to either
 * wait for the ingest of those objects to complete and continue to look up their
 * children object graphs, OR to ignore those objects and their children from
 * the map. Ideally, this behavior would be applied to all other object types,
 * current current functionally of {@link RelationshipService} and
 * {@link ArchiveService} does not allow that.
 * 
 * Serialize {@link BusinessObjectMap} to a custom xml format.
 * 
 * TODO Write a schema
 * 
 * <pre>
 * {@code
 * <bo>
 *   <id>dcs id</id>
 *   <alternateid>blah</alternateid>
 *   <name>human readable name</name>
 *   <type>human readable bo type</type>
 *   <depositStatus>DEPOSITED or FAILED</depositStatus>
 * 
 *  <!-- Child bo -->
 *    <bo>
 *     ...
 *    </bo>
 * <bo>
 * }
 * </pre>
 */
public class BusinessObjectMapServiceImpl implements BusinessObjectMapService {
    private static final String XSLT_RESOURCE = "/xslt/bomap2html.xsl";

    private RelationshipService rel_service;
    private ArchiveService archive_service;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public BusinessObjectMapServiceImpl(ArchiveService archive_service,
            RelationshipService rel_service) {
        this.archive_service = archive_service;
        this.rel_service = rel_service;
    }

    // For unknown reasons ArchiveService retrieve methods do not simply return
    // the object
    private <T> T unwrap(ArchiveSearchResult<T> sr)
            throws ArchiveServiceException {
        if (sr == null || sr.getResults().size() != 1) {
            throw new ArchiveServiceException(
                    "Expected to find one business object.");
        }

        return sr.getResults().iterator().next();
    }

    // Return the deposit id of a deposited business object
    private String get_deposit_id(String bo_id, boolean waitForPendingObjects) throws ArchiveServiceException, InterruptedException {

        String depositId;
        if (waitForPendingObjects) {
            depositId = getDepositIdWaitForPending(bo_id);
        } else {
            depositId = getDepositIdIgnoreFailedOrPending(bo_id);
        }
        return depositId;
    }

    private String getDepositIdWaitForPending(String boId) throws ArchiveServiceException, InterruptedException {
        String depositId = null;
        ArchiveDepositInfo status;
        long delayTime = 10000;
        int totalPollCount = 0;

        do {
            //TODO: when the time comes, stop calling pollArchive()
            archive_service.pollArchive();
            List<ArchiveDepositInfo> adis = archive_service.listDepositInfo(boId, null);
            if (adis.isEmpty()) {
                //poll for so long
                log.error("Could not find deposit info for business object: " + boId);
                throw new ArchiveServiceException( "Could not find deposit info for business object: " + boId);
                //after a while return null;
            } else {
                status = adis.get(0);
                if (status.getDepositStatus().equals(ArchiveDepositInfo.Status.DEPOSITED)) {
                    depositId = status.getDepositId();
                    break;
                } else if (status.getDepositStatus().equals(ArchiveDepositInfo.Status.FAILED)) {
                    depositId = null;
                    break;
                }
                totalPollCount++;
                Thread.sleep(delayTime);
            }

        } while (status.getDepositStatus().equals(ArchiveDepositInfo.Status.PENDING) && totalPollCount <= 12);
        return depositId;
    }


    private String getDepositIdIgnoreFailedOrPending(String boId) throws ArchiveServiceException {
        String depositId;
        //TODO: when the time comes, stop calling pollArchive()
        archive_service.pollArchive();
        List<ArchiveDepositInfo> adis = archive_service.listDepositInfo(boId, null);
        if (adis.isEmpty()) {
            //poll for so long
            log.error("Could not find deposit info for business object: " + boId);
            throw new ArchiveServiceException( "Could not find deposit info for business object: " + boId);
            //after a while return null;
        } else {
            ArchiveDepositInfo status = adis.get(0);
            if (status.getDepositStatus().equals(ArchiveDepositInfo.Status.DEPOSITED)) {
                depositId = status.getDepositId();
            } else {
                depositId = null;
            }
        }
        return depositId;
    }
    @Override
    public BusinessObjectMap generateMap(BusinessObject object,
            Map<String, List<String>> alternate_id_map, boolean waitForPendingObjects)
            throws ArchiveServiceException, InterruptedException {

        BusinessObjectMap result = new BusinessObjectMap(object);
        result.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        if (alternate_id_map != null) {
            if (alternate_id_map.containsKey(object.getId())) {
                result.getAlternateIds().addAll(
                        alternate_id_map.get(object.getId()));
            }
        }

        //MF support
        try {
            for (String mf_id: rel_service.getMetadataFileIdsForBusinessObjectId(object.getId())) {

                String deposit_id = get_deposit_id(mf_id, waitForPendingObjects);
                if (deposit_id != null) {
                    MetadataFile mf = unwrap(archive_service.retrieveMetadataFile(deposit_id));
                    result.getChildren().add(generateMap(mf, alternate_id_map, waitForPendingObjects));
                } else {
                    MetadataFile failedDepositMF = new MetadataFile();
                    failedDepositMF.setId(mf_id);
                    result.getChildren().add(buildFailedDepositBOMap(failedDepositMF));
                }
            }
        } catch (RelationshipException e) {
            throw new ArchiveServiceException(e);
        }

        if (object instanceof Project) {
            Project proj = (Project) object;

            for (Collection col : rel_service.getCollectionsForProject(proj)) {
                result.getChildren().add(generateMap(col, alternate_id_map, waitForPendingObjects));
            }
        } else if (object instanceof Collection) {
            Collection col = (Collection) object;

            for (String sub_col_id : col.getChildrenIds()) {
                String deposit_id = get_deposit_id(sub_col_id, waitForPendingObjects);
                if (deposit_id != null) {
                    Collection sub_col = unwrap(archive_service
                            .retrieveCollection(deposit_id));
                    result.getChildren().add(generateMap(sub_col, alternate_id_map, waitForPendingObjects));
                } else {
                    Collection failedDepositSubCollection = new Collection();
                    failedDepositSubCollection.setId(sub_col_id);
                    result.getChildren().add(buildFailedDepositBOMap(failedDepositSubCollection));
                }
            }
 
            String deposit_id = get_deposit_id(col.getId(), waitForPendingObjects);
            ArchiveSearchResult<DataItem> sr = archive_service
                    .retrieveDataSetsForCollection(deposit_id, -1, 0);

            if (sr != null) {
                for (DataItem di : sr.getResults()) {
                    result.getChildren().add(generateMap(di, alternate_id_map, waitForPendingObjects));
                }
            }
        } else if (object instanceof DataItem) {
            DataItem di = (DataItem) object;
            for (DataFile df : di.getFiles()) {
                result.getChildren().add(generateMap(df, alternate_id_map, waitForPendingObjects));
            }
        }

        return result;
    }

    @Override
    public void writeXmlMap(BusinessObjectMap map, OutputStream os)
            throws IOException {

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            writeMap(doc, doc, map);

            Transformer trans = TransformerFactory.newInstance()
                    .newTransformer();

            trans.transform(new DOMSource(doc), new StreamResult(os));
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (TransformerConfigurationException e) {
            throw new IOException(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new IOException(e);
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    private BusinessObjectMap buildFailedDepositBOMap (BusinessObject businessObject) {
        BusinessObjectMap failedBoMap = new BusinessObjectMap(businessObject);
        failedBoMap.setDepositStatus(ArchiveDepositInfo.Status.FAILED.toString());
        return failedBoMap;
    }

    @Override
    public void writeHtmlMap(BusinessObjectMap map, OutputStream os)
            throws IOException {
        // Grab the xml

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeXmlMap(map, bos);

        // Transform with xslt to html

        Source xslt = new StreamSource(
                BusinessObjectMapServiceImpl.class
                        .getResourceAsStream(XSLT_RESOURCE));
        try {
            Transformer trans = TransformerFactory.newInstance()
                    .newTransformer(xslt);

            Source xml = new StreamSource(new ByteArrayInputStream(
                    bos.toByteArray()));
            trans.transform(xml, new StreamResult(os));
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    private void writeMap(Document doc, Node parent, BusinessObjectMap bo) {
        Element el = add(doc, parent, "bo", null);

        add(doc, el, "id", bo.getId());
        add(doc, el, "name", bo.getName());
        add(doc, el, "type", bo.getType());
        add(doc, el, "depositStatus", bo.getDepositStatus());

        for (String alt : bo.getAlternateIds()) {
            add(doc, el, "alternateid", alt);
        }

        for (BusinessObjectMap bo_child : bo.getChildren()) {
            writeMap(doc, el, bo_child);
        }
    }

    private Element add(Document doc, Node parent, String name, String text) {
        Element el = doc.createElement(name);

        if (text != null) {
            el.setTextContent(text);
        }

        parent.appendChild(el);
        return el;
    }
}
