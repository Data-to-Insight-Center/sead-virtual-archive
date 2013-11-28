package org.dataconservancy.ui.services;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.BusinessObjectMap;

/**
 * Generate {@link BusinessObjectMap}s from objects in the archive and serialize
 * them.
 */
public interface BusinessObjectMapService {
    /**
     * Serialize a map as XML.
     * 
     * @param map
     * @param os
     * @throws IOException
     */
    void writeXmlMap(BusinessObjectMap map, OutputStream os) throws IOException;

    /**
     * Serialize a map as HTML.
     * 
     * @param map
     * @param os
     * @throws IOException
     */
    void writeHtmlMap(BusinessObjectMap map, OutputStream os) throws IOException;

    /**
     * Generate a business object map rooted at the given business object. The
     * descendants of the business object must in the archive.
     * 
     * The alternate_id_map argument maps business object identifiers to a list
     * of alternate identifiers. Any business object with a matching identifier
     * will have that list of alternate identifiers added to the resulting map.
     * Note that a business object is not required to have alternate
     * identifiers.
     *
     * The {@code waitForPendingObjects} flag tells the process to either poll
     * the archive multiple times to wait for Objects who are still being ingested
     * or not.
     * 
     * @param object
     * @param alternate_id_map
     * @throws ArchiveServiceException
     * @return map
     */
    BusinessObjectMap generateMap(BusinessObject object,
            Map<String, List<String>> alternate_id_map, boolean waitForPendingObjects)
            throws ArchiveServiceException, InterruptedException;
}
