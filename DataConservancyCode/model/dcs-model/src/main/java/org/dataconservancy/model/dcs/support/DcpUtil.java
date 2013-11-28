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
package org.dataconservancy.model.dcs.support;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for manipulating DCPs
 */
public class DcpUtil {

    /**
     * Add the supplied entities to the supplied {@code dcp}.  If {@code dcp} is {@code null}, this method will
     * instantiate an empty DCP.
     *
     * @param dcp      the DCP to add the supplied entities to, may be {@code null}
     * @param entities the entities to add, must not be {@code null}
     * @return the DCP with the supplied entities added
     * @throws IllegalArgumentException if {@code entities} is {@code null}
     * @throws IllegalStateException    if {@code entities} contains an unknown entity type
     */
    public static Dcp add(Dcp dcp, DcsEntity... entities) {
        if (dcp == null) {
            dcp = new Dcp();
        }

        Assertion.notNull(entities);

        for (DcsEntity entity : entities) {
            if (entity instanceof DcsCollection) {
                dcp.addCollection((DcsCollection) entity);
            } else if (entity instanceof DcsDeliverableUnit) {
                dcp.addDeliverableUnit((DcsDeliverableUnit) entity);
            } else if (entity instanceof DcsFile) {
                dcp.addFile((DcsFile) entity);
            } else if (entity instanceof DcsEvent) {
                dcp.addEvent((DcsEvent) entity);
            } else if (entity instanceof DcsManifestation) {
                dcp.addManifestation((DcsManifestation) entity);
            } else {
                throw new IllegalStateException("Unhandled entity type: "
                        + entity.getClass().getName());
            }
        }

        return dcp;
    }

    /**
     * Add the supplied entities to the supplied {@code dcp}.  If {@code dcp} is {@code null}, this method will
     * instantiate an empty DCP.
     *
     * @param dcp      the DCP to add the supplied entities to, may be {@code null}
     * @param entities the entities to add, must not be {@code null}
     * @return the DCP with the supplied entities added
     * @throws IllegalArgumentException if {@code entities} is {@code null}
     * @throws IllegalStateException    if {@code entities} contains an unknown entity type
     */
    public static Dcp add(Dcp dcp, Collection<? extends DcsEntity> entities) {
        if (dcp == null) {
            dcp = new Dcp();
        }

        Assertion.notNull(entities);

        for (DcsEntity entity : entities) {
            add(dcp, entity);
        }

        return dcp;
    }

    /**
     * Converts the supplied DCP to a List.  The entities in the returned List are references to the entities in the
     * DCP, so mutating entities in the returned List will mutate the supplied DCP.
     *
     * @param dcp the DCP to convert to a List, must not be null
     * @return the DCP as a list
     * @throws IllegalArgumentException if {@code dcp} is {@code null}
     */
    public static List<DcsEntity> asList(Dcp dcp) {
        Assertion.notNull(dcp);

        List<DcsEntity> result = new ArrayList<DcsEntity>();

        result.addAll(dcp.getCollections());
        result.addAll(dcp.getDeliverableUnits());
        result.addAll(dcp.getEvents());
        result.addAll(dcp.getFiles());
        result.addAll(dcp.getManifestations());

        return result;
    }

    /**
     * Converts the supplied DCP to a Map.  The entities in the returned Map are references to the entities in the
     * DCP, so mutating entities in the returned Map will mutate the supplied DCP.  The returned Map is keyed by the
     * identifiers of the entities in the supplied DCP.  Therefore, entities in the supplied DCP must have an
     * {@link DcsEntity#getId() id} in order to be represented in the returned Map.  If entities in the supplied DCP do
     * not have an id, then they are simply ignored.  If entities in the supplied DCP share the same id, then one of
     * them will not be represented in the returned Map.
     *
     * @param dcp the DCP to convert to a Map, must not be null
     * @return the DCP as a Map
     * @throws IllegalArgumentException if {@code dcp} is {@code null}
     */
    public static Map<String, DcsEntity> asMap(Dcp dcp) {
        Assertion.notNull(dcp);

        Map<String, DcsEntity> entityMap = new HashMap<String, DcsEntity>();

        for (DcsEntity e : dcp) {
            if (!Util.isEmptyOrNull(e.getId())) {
                entityMap.put(e.getId(), e);
            }
        }

        return entityMap;
    }

}