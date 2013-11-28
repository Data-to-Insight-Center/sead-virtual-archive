
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
package org.dataconservancy.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;

/**
 * Utility class building a Dcp.
 */
public class DcpUtil {

    public static Dcp add(Dcp dcp, DcsEntity... entities) {
        if (dcp == null) {
            dcp = new Dcp();
        }

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
                throw new IllegalArgumentException("Unhandled entity type: "
                        + ((entity == null) ? "'null'" : entity.getClass().getName()));
            }
        }

        return dcp;
    }

    public static Dcp add(Dcp dcp, Collection<DcsEntity> entities) {
        if (dcp == null) {
            dcp = new Dcp();
        }

        for (DcsEntity entity : entities) {
            add(dcp, entity);
        }

        return dcp;
    }

    public static List<DcsEntity> asList(Dcp dcp) {

        List<DcsEntity> result = new ArrayList<DcsEntity>();

        result.addAll(dcp.getCollections());
        result.addAll(dcp.getDeliverableUnits());
        result.addAll(dcp.getEvents());
        result.addAll(dcp.getFiles());
        result.addAll(dcp.getManifestations());

        return result;
    }

    public static Map<String, DcsEntity> asMap(Dcp dcp) {
        Map<String, DcsEntity> entityMap = new HashMap<String, DcsEntity>();

        for (DcsEntity e : dcp) {
            if (e.getId() != null && !e.getId().isEmpty()) {
                entityMap.put(e.getId(), e);
            }
        }

        return entityMap;
    }

}
