package org.dataconservancy.dcs.access.http.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.dataconservancy.dcs.access.http.ByteArray;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.model.dcs.DcsRelation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadEvent;
import org.seadva.model.SeadFile;
import org.seadva.model.pack.ResearchObject;

public class SeadUtil
{
public static ResearchObject add(ResearchObject dcp, DcsEntity... entities)
{
    if (dcp == null) {
    dcp = new ResearchObject();
    }
    for (DcsEntity entity : entities) {
        if ((entity instanceof DcsCollection)) {
            dcp.addCollection(new DcsCollection[] { (DcsCollection)entity });
        } else if ((entity instanceof SeadDeliverableUnit)) {
            dcp.addDeliverableUnit(new DcsDeliverableUnit[] { (SeadDeliverableUnit)entity });
        } else if ((entity instanceof DcsDeliverableUnit)) {
            dcp.addDeliverableUnit(new DcsDeliverableUnit[] { new SeadDeliverableUnit((DcsDeliverableUnit)entity) });
        } else if ((entity instanceof SeadFile)) {
            dcp.addFile(new DcsFile[] { (SeadFile)entity });
        } else if ((entity instanceof DcsFile)) {
            dcp.addFile(new DcsFile[] { new SeadFile((DcsFile)entity) });
        } else if ((entity instanceof SeadEvent)) {
            dcp.addEvent(new DcsEvent[] { (SeadEvent)entity });
        } else if ((entity instanceof DcsEvent)) {
            dcp.addEvent(new DcsEvent[] { new SeadEvent((DcsEvent)entity) });
        } else if ((entity instanceof DcsManifestation)) {
            dcp.addManifestation(new DcsManifestation[] { (DcsManifestation)entity });
        } else {
        throw new IllegalStateException("Unhandled entity type: " + entity.getClass().getName());
        }
    }
    return dcp;
}

public static ResearchObject add(ResearchObject dcp, Collection<DcsEntity> entities)
{
    if (dcp == null) {
    dcp = new ResearchObject();
    }
    for (DcsEntity entity : entities) {
    add(dcp, new DcsEntity[] { entity });
    }
    return dcp;
}

public InputStream asInputStream(Dcp dcp)
{
DcsModelBuilder mb = new DcsXstreamStaxModelBuilder();

ByteArray dcp_bytes = new ByteArray(32768);
mb.buildSip(dcp, dcp_bytes.asOutputStream());

return dcp_bytes.asInputStream();
}

public List<DcsEntity> asList(Dcp dcp)
{
    List<DcsEntity> result = new ArrayList();

    result.addAll(dcp.getCollections());
    result.addAll(dcp.getDeliverableUnits());
    result.addAll(dcp.getEvents());
    result.addAll(dcp.getFiles());
    result.addAll(dcp.getManifestations());

    return result;
}

public XStream toJSONConverter()
{
    XStream jsonbuilder = new XStream(new JsonHierarchicalStreamDriver()
    {
    public HierarchicalStreamWriter createWriter(Writer writer)
    {
        return new JsonWriter(writer, 1);
    }
        });
        jsonbuilder.setMode(1001);
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