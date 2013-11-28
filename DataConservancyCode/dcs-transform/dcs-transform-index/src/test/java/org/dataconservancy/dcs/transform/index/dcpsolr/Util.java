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
package org.dataconservancy.dcs.transform.index.dcpsolr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;

public class Util {

    private static final AtomicInteger counter = new AtomicInteger(0);

    public static Dcp newDcp() {
        Dcp dcp = new Dcp();

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(Integer.toString(counter.incrementAndGet()));
        du.setTitle(Integer.toString(counter.incrementAndGet()));

        DcsManifestation man = new DcsManifestation();
        man.setId(Integer.toString(counter.incrementAndGet()));
        man.setDeliverableUnit(du.getId());

        DcsFile file = new DcsFile();
        file.setId(Integer.toString(counter.incrementAndGet()));
        file.setExtant(false);
        file.setSource("http://dataconservancy.org");
        file.setName("index.html");

        DcsManifestationFile mf = new DcsManifestationFile();
        mf.setPath("/");
        mf.setRef(new DcsFileRef(file.getId()));

        man.addManifestationFile(mf);

        dcp.addDeliverableUnit(du);
        dcp.addManifestation(man);
        dcp.addFile(file);

        return dcp;
    }

    public static boolean lookupAll(Dcp dcp, DcpIndexService index) {
        try {
            for (DcsEntity e : dcp.getDeliverableUnits()) {
                if (index.lookupEntity(e.getId()) == null) {
                    return false;
                }
            }
            for (DcsEntity e : dcp.getManifestations()) {
                if (index.lookupEntity(e.getId()) == null) {
                    return false;
                }
            }
            for (DcsEntity e : dcp.getFiles()) {
                if (index.lookupEntity(e.getId()) == null) {
                    return false;
                }
            }
            for (DcsEntity e : dcp.getEvents()) {
                if (index.lookupEntity(e.getId()) == null) {
                    return false;
                }
            }
            for (DcsEntity e : dcp.getCollections()) {
                if (index.lookupEntity(e.getId()) == null) {
                    return false;
                }
            }
        } catch (IndexServiceException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static List<String> getAllIDs(Dcp dcp) {

        List<String> values = new ArrayList<String>();

        for (DcsEntity e : dcp.getDeliverableUnits()) {
            values.add(e.getId());
        }
        for (DcsEntity e : dcp.getManifestations()) {
            values.add(e.getId());
        }
        for (DcsEntity e : dcp.getFiles()) {
            values.add(e.getId());
        }
        for (DcsEntity e : dcp.getEvents()) {
            values.add(e.getId());
        }
        for (DcsEntity e : dcp.getCollections()) {
            values.add(e.getId());
        }

        return values;
    }
}
