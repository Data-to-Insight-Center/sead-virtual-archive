package org.dataconservancy.ui.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;

public class MockParentSearcherImpl implements ParentSearcher {
    private final MockArchiveUtil archiveUtil;

    public MockParentSearcherImpl(MockArchiveUtil archiveUtil) {
        this.archiveUtil = archiveUtil;
    }

    @Override
    public Collection<DcsEntity> getParentsOf(String entityId) {
        return getParentsOf(entityId, DcsEntity.class);
    }

    @Override
    public <T extends DcsEntity> Collection<T> getParentsOf(String entityId,
            Class<T> constraint) {
        Collection<DcsEntity> results = new HashSet<DcsEntity>();
        
        for (Set<DcsEntity> entity_set: archiveUtil.getEntities().values()) {
            for (DcsEntity entity: entity_set) {
                if (constraint.isInstance(entity)) {
                    if (constraint == DcsDeliverableUnit.class) {
                        DcsDeliverableUnit du = (DcsDeliverableUnit) entity;
                        
                        if (du.getParents().contains(new DcsDeliverableUnitRef(entityId))) {
                            results.add(du);                            
                        }                        
                    }
                }
            }
        }
        
        Collection<T> constrainedResults = new HashSet<T>(results.size());

        for (DcsEntity e : results) {
            if (e.getClass() == constraint) {
                constrainedResults.add(constraint.cast(e));
            }
        }

        return constrainedResults;
    }     
}
