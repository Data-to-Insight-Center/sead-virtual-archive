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
package org.dataconservancy.ui.dcpmap;

import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataconservancy.ui.exceptions.DcpMappingException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.profile.CollectionProfile;
import org.dataconservancy.ui.services.ArchiveUtil;
import org.dataconservancy.ui.services.BusinessObjectState;
import org.dataconservancy.ui.services.CollectionBusinessObjectSearcher;
import org.dataconservancy.ui.services.MetadataFileBusinessObjectSearcher;
import org.dataconservancy.ui.services.ParentSearcher;

import com.thoughtworks.xstream.XStream;

/**
 * This class provides methods to transform (or map) {@link org.dataconservancy.ui.model.Collection} objects into valid
 * {@link org.dataconservancy.model.dcp.Dcp} objects and to construct {@link org.dataconservancy.ui.model.Collection} objects
 * from valid {@link org.dataconservancy.model.dcp.Dcp} objects.
 */
public class CollectionMapper extends AbstractVersioningMapper<Collection> {
    // TODO As a hack set these on root and state dus to distinguish them from those of other objects.
    public static final String COLLECTION_TITLE = "collection";
    private final XStream xstream;

    /**
     * Used to handle updates to business objects already deposited in the
     * archive.
     */
    private final CollectionBusinessObjectSearcher boSearcher;
    private final MetadataFileBusinessObjectSearcher mfSearcher;
    private final ParentSearcher parentSearcher;
    private final ArchiveUtil archiveUtil;
    
    public CollectionMapper(CollectionBusinessObjectSearcher boSearcher, 
            MetadataFileBusinessObjectSearcher mfSearcher, ParentSearcher parentSearcher, ArchiveUtil archiveUtil) {
        super();
        this.boSearcher = boSearcher;
        this.mfSearcher = mfSearcher;
        this.parentSearcher = parentSearcher;
        this.xstream = new XStream();
        this.archiveUtil = archiveUtil;
    }

    public Dcp toDcp(String parent_entity_id, Collection collection)
            throws DcpMappingException {
        String root_id = null;
        String state_pred_id = null;    

        // If this is an update, need to find predecessor id and root id
        
        BusinessObjectState current_state = boSearcher.findLatestState(collection.getId());

        if (current_state != null) {
            if (current_state.getRoot() != null) {
                root_id = current_state.getRoot().getId();
            }
            
            if (current_state.getLatestState() != null) {
                state_pred_id  = current_state.getLatestState().getId();
            }
        }
        
        // Clear out information which will be set in fromDcp
        
        Collection to_deposit = new Collection(collection);
        to_deposit.setParentId(null);
        to_deposit.setChildrenIds(new ArrayList<String>());
        
        Dcp dcp = super.toDcp(parent_entity_id, state_pred_id, root_id, to_deposit);

        DcsDeliverableUnit statedu = getCurrentStateDu(dcp);

        // Save dus so we can modify statedu in the Dcp
        java.util.Collection<DcsDeliverableUnit> dus = dcp.getDeliverableUnits();
        dus.remove(statedu);
        dus.add(statedu);

        // Map alternate ids to DcsResourceIdentifier
        if (collection.getAlternateIds() != null) {

            for (String id : collection.getAlternateIds()) {
                if (id == null || id.trim().isEmpty()) {
                    continue;
                }

                // TODO: Eventually parse out other fields

                DcsResourceIdentifier res = new DcsResourceIdentifier();
                res.setIdValue(id);
                statedu.addAlternateId(res);
            }
        }

        // Add the collection id to the former external ref of the root and dus.
        
        statedu.addFormerExternalRef(collection.getId());
        statedu.setTitle(COLLECTION_TITLE);

        DcsDeliverableUnit rootDu = getRootDu(dcp);
        
        if (rootDu != null) {
            dus.remove(rootDu);
            dus.add(rootDu);
            
            rootDu.addFormerExternalRef(collection.getId());
            rootDu.setTitle(COLLECTION_TITLE);
        }
        
        // If there is a parent, add parent root du id to child state du id
        String parent = collection.getParentId();
        
        if (parent != null) {
            BusinessObjectState parent_state = boSearcher.findLatestState(parent);
        
            if (parent_state == null || parent_state.getRoot() == null) {
                throw new DcpMappingException("Unable to find parent business object: " + parent);
            } else {
                statedu.addParent(new DcsDeliverableUnitRef(parent_state.getRoot().getId()));     
            }
        }
        
        // Save any updates to dus
        dcp.setDeliverableUnits(dus);
        
        return dcp;
    }

    private String get_business_id(DcsDeliverableUnit du) throws DcpMappingException {
        java.util.Collection<String> former = du.getFormerExternalRefs();

        if (former.size() == 1) {
            return former.iterator().next();
        }
        
        throw new DcpMappingException("Unable to retrieve collection id from: " + du);
    }
    
    private DcsDeliverableUnit lookup_deliverable_unit(String id) throws DcpMappingException {
        DcsEntity entity = archiveUtil.getEntity(id);

        if (entity == null) {
            throw new DcpMappingException("Unable to lookup du: " + id);
        }
        
        if (!(entity instanceof DcsDeliverableUnit)) {
            throw new DcpMappingException("Entity is not du: " + entity);
        }
        
        return (DcsDeliverableUnit) entity;
    }

    public Collection fromDcp(Dcp dcp) throws DcpMappingException {
        Collection collection = super.fromDcp(dcp);

        // Must check if this collection has a parent.
        
        DcsDeliverableUnit state = getCurrentStateDu(dcp);

        if (state == null) {
            throw new DcpMappingException("Unable find state in dcp");
        }

        // Find root du of this collection and potentially root du of parent collection.
        
        DcsDeliverableUnit root = getRootDu(dcp);
        DcsDeliverableUnit parent_collection_root = null;
        
        for (DcsDeliverableUnitRef ref: state.getParents()) {
            if (root != null && root.getId().equals(ref.getRef())) {
                continue;
            }
            
            DcsDeliverableUnit du = lookup_deliverable_unit(ref.getRef());
            String business_id = get_business_id(du);
            
            if (du.getTitle().equals(COLLECTION_TITLE) && du.getType().equals(ROOT_DELIVERABLE_UNIT_TYPE)) {
                if (business_id.equals(collection.getId())) {
                    root = du;
                } else {
                    parent_collection_root = du;
                }
            }
        }
        
        if (root == null) {
            throw new DcpMappingException("State du does not have root du parent");
        }
        
        // If collection has parent, and set it.
        
        if (parent_collection_root != null) {
            collection.setParentId(get_business_id(parent_collection_root));
        }
        
        // Search archive to find business ids of children
        
        List<String> kid_results = collection.getChildrenIds();
        kid_results.clear();
        
        java.util.Collection<DcsDeliverableUnit> kids = parentSearcher.getParentsOf(root.getId(), DcsDeliverableUnit.class);
        
        for (DcsDeliverableUnit kid: kids) {
            // Ignore non-collections

            if (kid.getId().equals(state.getId()) || !kid.getType().equals(getStateDuType()) || !kid.getTitle().equals(COLLECTION_TITLE)) {
                continue;
            }
            
            // Ignore out our own versions

            String kid_collection_id = get_business_id(kid);

            if (kid_collection_id.equals(collection.getId())) {
                continue;
            }
            
            // Only add collections whose latest version has a relationship.
            
            BusinessObjectState kid_collection_state = boSearcher.findLatestState(kid_collection_id);
            
            if (kid_collection_state == null) {
                throw new DcpMappingException("Could not find collection state: " + kid_collection_id);
            }
            
            DcsDeliverableUnit latest = kid_collection_state.getLatestState();
            
            if (latest == null) {
                throw new DcpMappingException("Could not find latest state: " + kid_collection_id);
            }
            
            if (latest.getId().equals(kid.getId())) {
                kid_results.add(kid_collection_id);
            }
        }
        
        return collection;
    }

    @Override
    protected void serializeObjectState(Collection object, OutputStream os) {
        // TODO Should switch to the collection serialization
        xstream.toXML(object, os);
    }

    @Override
    protected Collection deserializeObjectState(InputStream is) {
        return (Collection) xstream.fromXML(is);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getStateDuType() {
        return CollectionProfile.STATE_DU_TYPE;
    }
}
