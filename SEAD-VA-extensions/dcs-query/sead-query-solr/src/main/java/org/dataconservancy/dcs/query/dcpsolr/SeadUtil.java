/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.query.dcpsolr;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.*;
import org.dataconservancy.model.dcs.support.ByteArray;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadEvent;
import org.seadva.model.SeadFile;
import org.seadva.model.pack.ResearchObject;

import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SeadUtil {

    public static ResearchObject add(ResearchObject dcp, DcsEntity... entities) {
        if (dcp == null) {
            dcp = new ResearchObject();
        }

        for (DcsEntity entity : entities) {
            if (entity instanceof DcsCollection) {
                dcp.addCollection((DcsCollection) entity);
            }
            else if (entity instanceof SeadDeliverableUnit) {
                dcp.addDeliverableUnit((SeadDeliverableUnit)entity);
            } else if (entity instanceof DcsDeliverableUnit) {
                dcp.addDeliverableUnit(new SeadDeliverableUnit((DcsDeliverableUnit)entity));
            }else if (entity instanceof SeadFile) {
                dcp.addFile((SeadFile)entity);
            } else if (entity instanceof DcsFile) {
                dcp.addFile(new SeadFile((DcsFile)entity));
            } else if (entity instanceof SeadEvent) {
                dcp.addEvent((SeadEvent)entity);
            }else if (entity instanceof DcsEvent) {
                dcp.addEvent(new SeadEvent((DcsEvent)entity));
            } else if (entity instanceof DcsManifestation) {
                dcp.addManifestation((DcsManifestation) entity);
            } else {
                throw new IllegalStateException("Unhandled entity type: "
                        + entity.getClass().getName());
            }
        }

        return dcp;
    }

    public static ResearchObject add(ResearchObject dcp, Collection<DcsEntity> entities) {
        if (dcp == null) {
            dcp = new ResearchObject();
        }

        for (DcsEntity entity : entities) {
            add(dcp, entity);
        }

        return dcp;
    }

    public static InputStream asInputStream(Dcp dcp) {
        DcsModelBuilder mb = new DcsXstreamStaxModelBuilder();

        ByteArray dcp_bytes = new ByteArray(32 * 1024);
        mb.buildSip(dcp, dcp_bytes.asOutputStream());

        return dcp_bytes.asInputStream();
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

    public static XStream toJSONConverter() {
        XStream jsonbuilder = new XStream(new JsonHierarchicalStreamDriver() {
            public HierarchicalStreamWriter createWriter(Writer writer) {
                return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
            }
        });

        jsonbuilder.setMode(XStream.NO_REFERENCES);
        jsonbuilder.alias("dcp", ResearchObject.class);
        jsonbuilder.alias("deliverableUnit", SeadDeliverableUnit.class);
        jsonbuilder.alias("deliverableUnitRef", DcsDeliverableUnitRef.class);
        jsonbuilder.alias("collection", DcsCollection.class);
        jsonbuilder.alias("file", SeadFile.class);
        jsonbuilder.alias("manifestation", DcsManifestation.class);
        jsonbuilder.alias("event", SeadEvent.class);
        jsonbuilder.alias("metadata", DcsMetadata.class);
        jsonbuilder.alias("collectionRef", DcsCollectionRef.class);
        jsonbuilder.alias("relation", DcsRelation.class);
        jsonbuilder.alias("fixity", DcsFixity.class);
        jsonbuilder.alias("fileRef", DcsFileRef.class);
        jsonbuilder.alias("entityRef", DcsEntityReference.class);
        jsonbuilder.alias("metadataRef", DcsMetadataRef.class);
        jsonbuilder.alias("format", DcsFormat.class);

        return jsonbuilder;
    }
}
