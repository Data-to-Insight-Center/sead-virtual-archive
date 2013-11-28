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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.AbstractIterator;
import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.request.GetDatastreamDissemination;
import com.yourmediashelf.fedora.client.request.GetObjectProfile;
import com.yourmediashelf.fedora.client.request.PurgeObject;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import com.yourmediashelf.fedora.client.response.GetObjectProfileResponse;
import com.yourmediashelf.fedora.generated.access.ObjectProfile;
import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.archive.impl.fcrepo.ri.RIClient;
import org.dataconservancy.archive.impl.fcrepo.ri.RIQueryResult;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 * Provides storage and access to DCS entities in the <em>DCP Serialization
 * Format</em> for a Fedora Commons Repository backend.
 * <p>
 * <h2>Iteration</h2>
 * This interface supports iterating the ids of all entities stored in the
 * archive (in no particular order) via the <code>Iterable&lt;String&gt;</code>
 * interface.  For example:
 * <pre>
 *    ArchiveStore archive = new SomeArchiveStoreImpl(..);
 *    long count = 0;
 *    for (String entityId: archive) {
 *        System.out.println(entityId);
 *        count++;
 *    }
 *    System.out.println("Total entities in archive: " + count);
 * </pre>
 * <p>
 * <h2>Thread Safety</h2>
 * Implementations are expected to ensure that a single instance can be used
 * safely by multiple concurrent threads.  It is therefore not necessary for
 * clients of this interface to construct multiple instances unless they
 * intend to work with multiple back-end archives.
 *
 * @see <a href="https://wiki.library.jhu.edu/x/DoKx">DCP Serialization Format</a>
 */
public class FcrepoArchiveStore implements ArchiveStore {

    private static final String FILE_CONTENT_DSID = "FILE";
    private static final String DCP_DSID = "DCPXML";

    private static final Map<String, EntityType> typeMap
            = new HashMap<String, EntityType>();

    private final PIDMapper pidMapper = new PIDMapper("dcs");

    private final FedoraClient fedoraClient;

    private final RIClient riClient;

    private final DcpAipIngester ingester;

    static {
        typeMap.put("info:fedora/dcs:Collection",
                EntityType.COLLECTION);
        typeMap.put("info:fedora/dcs:DeliverableUnit",
                EntityType.DELIVERABLE_UNIT);
        typeMap.put("info:fedora/dcs:Event",
                EntityType.EVENT);
        typeMap.put("info:fedora/dcs:File",
                EntityType.FILE);
        typeMap.put("info:fedora/dcs:Manifestation",
                EntityType.MANIFESTATION);
    }

    public FcrepoArchiveStore(FedoraClient fedoraClient,
                              RIClient riClient) {
        this.fedoraClient = fedoraClient;
        this.riClient = riClient;
        ingester = new DcpAipIngester(fedoraClient);
    }

    @Override
    public InputStream getContent(String entityId)
            throws EntityNotFoundException, EntityTypeException {
        return getContent(entityId, FILE_CONTENT_DSID, EntityType.FILE);
    }

    private InputStream getContent(String entityId,
                                   String datastreamId,
                                   EntityType expectedType)
            throws EntityNotFoundException,
                   EntityTypeException {
        String pid = pidMapper.getPID(entityId);
        GetDatastreamDissemination method =
                new GetDatastreamDissemination(pid, datastreamId);
        FedoraResponse response = null;
        try {
            response = method.execute(fedoraClient);
            int responseCode = response.getStatus();
            if (responseCode != 200) { // unexpected 2xx response
                throwFedoraFault(responseCode);
            }
        } catch (FedoraClientException e) { // if response if not 2xx...
            int responseCode = e.getStatus();
            if (responseCode < 100) { // non-http error
                throwFedoraFault(e);
            } else if (responseCode == 404) {
                EntityType assertedType = getAssertedType(pid);
                if (assertedType == null) {
                    throw new EntityNotFoundException(entityId);
                } else if (assertedType == expectedType) {
                    throw new RuntimeException("Entity " + entityId + " is a "
                            + expectedType + ", but the corresponding Fedora "
                            + "object (pid=" + pid + ") does not have a "
                            + "datastream with id=" + datastreamId + ")");
                } else {
                    throw new EntityTypeException(entityId, expectedType,
                            assertedType);
                }
            } else {
                throwFedoraFault(responseCode);
            }
        }
        return response.getEntityInputStream();
    }

    @Override
    public InputStream getPackage(String entityId)
            throws EntityNotFoundException {
        try {
            return getContent(entityId, DCP_DSID, null);
        } catch (EntityTypeException wontHappen) {
            throw new RuntimeException(wontHappen);
        }
    }

    @Override
    public InputStream getFullPackage(String entityId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("Not implemented for Y1P");
    }

    @Override
    public void putPackage(InputStream dcpStream) throws AIPFormatException {
        ingester.ingestPackage(dcpStream);
    }

    @Override
    public Iterator<String> listEntities(EntityType type) {
        final RIQueryResult result = riClient.query(getListQuery(type), true);
        return new AbstractIterator<String>() {
            @Override
            protected String computeNext() {
                if (result.hasNext()) {
                    Literal literal = (Literal) result.next().get(0);
                    return literal.getLabel();
                } else {
                    return endOfData();
                }
            }
        };
    }

    // Not part of the contract; only here to support testing
    // Returns true if the entity existed and was successfully deleted.
    // Return false if the entity did not exist.
    // Throws a RuntimeException if an unrecoverable error occurs.
    protected boolean deleteEntity(String entityId) {
        String pid = pidMapper.getPID(entityId);
        PurgeObject method = new PurgeObject(pid);
        FedoraResponse response = null;
        try {
            response = method.execute(fedoraClient);
            int responseCode = response.getStatus();
            if (responseCode != 200) { // unexpected 2xx response
                throwFedoraFault(responseCode);
            }
        } catch (FedoraClientException e) { // if response if not 2xx...
            int responseCode = e.getStatus();
            if (responseCode < 100) { // non-http error
                throwFedoraFault(e);
            } else if (getAssertedType(pid) == null) {
                return false;
            } else {
                throwFedoraFault(responseCode);
            }
        }
        return true;
    }

    private static String getListQuery(EntityType type) {
        StringBuilder query = new StringBuilder();
        query.append("PREFIX model: <info:fedora/fedora-system:def/model#>\n");
        query.append("PREFIX dcs:   <http://dataconservancy.org/ontologies/dcs/1.0/>\n");
        query.append("SELECT ?id\n");
        query.append("WHERE  {\n");
        if (type != null) {
          query.append("  ?fdo model:hasModel <" + getModel(type) + "> .\n");
        }
        query.append("  ?fdo dcs:id ?id .\n");
        query.append("}");
        return query.toString();
    }

    private static String getModel(EntityType type) {
        for (String key: typeMap.keySet()) {
            EntityType value = typeMap.get(key);
            if (value.equals(type)) {
                return key;
            }
        }
        throw new RuntimeException("Programmer error: No Fedora content model "
                + "has been mapped for EntityType: " + type);
    }

    // Gets the dcs EntityType of the given Fedora object, or null if no such
    // object.  Throws RuntimeException if object does not have exactly one
    // dcs entity type asserted.
    private EntityType getAssertedType(String pid) {
        GetObjectProfile method = new GetObjectProfile(pid);
        ObjectProfile profile = null;
        try {
            profile = method.execute(fedoraClient).getObjectProfile();
        } catch (FedoraClientException e) {
            if (e.getStatus() == 404) {
                return null;
            }
            throwFedoraFault(e);
        }
        return getAssertedType(pid, profile.getObjModels().getModel());
    }

    private EntityType getAssertedType(String pid, List<String> models) {
        EntityType assertedType = null;
        for (String model: models) {
            EntityType type = typeMap.get(model);
            if (type != null) { // recognized DCS cmodel
                if (assertedType != null) { // already has a DCS cmodel
                    throw new RuntimeException("Fedora object (pid=" + pid
                            + ") asserts it has multiple DCS content models");
                }
                assertedType = type;
            }
        }
        if (assertedType == null) {
            throw new RuntimeException("Fedora object (pid=" + pid + ") does"
                    + " not assert it has a recognized DCS content model");
        }
        return assertedType;
    }

    private static void throwFedoraFault(int responseCode) {
        throw new RuntimeException("Fedora Server returned an unexpected HTTP"
                + " response code: " + responseCode + ". Consult Fedora Server "
                + "log for details.");
    }

    private static void throwFedoraFault(FedoraClientException e) {
        int responseCode = e.getStatus();
        if (responseCode < 100) { // non-http error
            throw new RuntimeException("Fedora Client encountered an "
                    + "unexpected error", e);
        } else {
            throwFedoraFault(responseCode);
        }
    }

}
