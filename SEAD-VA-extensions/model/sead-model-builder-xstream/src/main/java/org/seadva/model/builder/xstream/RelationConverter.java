package org.seadva.model.builder.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsRelation;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;
/*Copied this class since it was not public in DC code's jar - Todo: Find a way around this issue or ask DC to make class public*/

class RelationConverter extends AbstractEntityConverter {
    final static String E_RELATION = "relationship";

    final static String A_REF = "ref";
    final static String A_REL = "rel";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        final DcsRelation rel = (DcsRelation) source;
        if (rel.getRef() != null) {
            writer.addAttribute(A_REF, rel.getRef().getRef());
        }

        if (!isEmptyOrNull(rel.getRelUri())) {
            writer.addAttribute(A_REL, rel.getRelUri());
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final DcsRelation rel = new DcsRelation();
        final String relUri = reader.getAttribute(A_REL);
        final String ref = reader.getAttribute(A_REF);
        if (!isEmptyOrNull(relUri)) {
            rel.setRelUri(relUri);
        }
        if (!isEmptyOrNull(ref)) {
            rel.setRef(new DcsEntityReference(ref));
        }
        return rel;
    }

    @Override
    public boolean canConvert(Class type) {
        return DcsRelation.class == type;
    }
}