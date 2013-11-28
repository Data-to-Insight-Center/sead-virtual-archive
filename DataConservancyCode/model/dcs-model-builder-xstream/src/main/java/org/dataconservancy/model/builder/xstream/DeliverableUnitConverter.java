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
package org.dataconservancy.model.builder.xstream;

import java.util.Collection;
import java.util.Set;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.dcs.*;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * Converts the xml representation of a {@link DcsDeliverableUnit} to the Java object model and back.
 * <p/>
 * Example XML:
 * <pre>
 * &lt;DeliverableUnit id="urn:sdss:12345"&gt;
 * <p/>
 * &lt;!--  The collection exists, and this is its DCS identifier --&gt;
 * &lt;collection ref="http://dataconservancy.org/collections/SDSS_run_5" /&gt;
 * <p/>
 * &lt;title&gt;SDSS file 12345&lt;/title&gt;
 * &lt;creator&gt;Astrophysical Research Consortium (ARC)&lt;/creator&gt;
 * &lt;subject&gt;Astronomy&lt;/subject&gt;
 * &lt;formerExternalRef&gt;http://das.sdss.org/blahblahblah...&lt;/formerExternalRef&gt;
 * <p/>
 * &lt;!-- An example of metadata inline --&gt;
 * &lt;metadata schemaURI="http://sdss.org/metadata/astroSchema.example.xsd"&gt;
 * &lt;md xmlns:astro="http://sdss.org/astro"&gt;
 * &lt;astro:skyCoverage&gt;all of it&lt;/astro:skyCoverage&gt;
 * &lt;astro:enfOfWorldFactor&gt;-1&lt;/astro:enfOfWorldFactor&gt;
 * &lt;/md&gt;
 * &lt;/metadata&gt;
 * <p/>
 * &lt;!--
 * An example of metadata that exists as a file, in this case it is
 * submitted in the SIP
 * --&gt;
 * &lt;metadata ref="urn:sdss:12345/metadata" /&gt;
 * <p/>
 * &lt;/DeliverableUnit&gt;
 * </pre>
 */
public class DeliverableUnitConverter extends AbstractEntityConverter {

    public static final String E_DU = "DeliverableUnit";
    public static final String E_TITLE = "title";
    public static final String E_CREATOR = "creator";
    public static final String E_SUBJECT = "subject";
    public static final String E_FORMER_EXT_REF = "formerExternalRef";
    public static final String E_RELATION = "relation";
    public static final String E_PARENT = "parent";
    public static final String E_TYPE = "type";
    public static final String E_DIGITAL_SURROGATE = "digitalSurrogate";
    public static final String E_RIGHTS = "rights";
    public static final String E_ALTERNATEID = "alternateIdentifier";
    public static final String A_LINEAGE_ID = "lineageId";
    public static final String A_ID = "id";
    public static final String A_REF = "ref";
    public static final String A_REL = "rel";

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);

        // Handle the reference
        if (source instanceof DcsDeliverableUnitRef) {
            final DcsDeliverableUnitRef duRef = (DcsDeliverableUnitRef) source;
            final String ref = duRef.getRef();
            if (!isEmptyOrNull(ref))
                writer.addAttribute(A_REF, duRef.getRef());
            return;
        }

        // Otherwise handle the Deliverable Unit itself
        final DcsDeliverableUnit du = (DcsDeliverableUnit) source;

        final String id = du.getId();
        final Collection<DcsResourceIdentifier> altIds = du.getAlternateIds();
        final String title = du.getTitle();
        final Collection<DcsCollectionRef> cRefs = du.getCollections();
        final Collection<DcsMetadataRef> mdRefs = du.getMetadataRef();
        final Collection<DcsMetadata> mds = du.getMetadata();
        final Collection<String> creators = du.getCreators();
        final Collection<String> extRefs = du.getFormerExternalRefs();  // Being deprecated, use AltIds DWD
        final Collection<DcsRelation> relations = du.getRelations();
        final Collection<String> subjects = du.getSubjects();
        final Collection<DcsDeliverableUnitRef> parents = du.getParents();
        final Boolean isDigitalSurrogate = du.isDigitalSurrogate();
        final String type = du.getType();
        final String rights = du.getRights();
        final String lineageid = du.getLineageId();

        if (!isEmptyOrNull(id)) {
            writer.addAttribute(A_ID, id);
        }
        
        if (!isEmptyOrNull(lineageid)) {
            writer.addAttribute(A_LINEAGE_ID, lineageid);
        }

        if (altIds != null) {
            for (DcsResourceIdentifier rid : altIds) {
                writer.startNode(E_ALTERNATEID);
                context.convertAnother(rid);
                writer.endNode();
            }
        }

        if (cRefs != null) {
            for (DcsCollectionRef cr : cRefs) {
                writer.startNode(CollectionConverter.E_COLLECTION);
                context.convertAnother(cr);
                writer.endNode();
            }
        }

        if (parents != null) {
            for (DcsDeliverableUnitRef r : parents) {
                final String ref = r.getRef();
                if (!isEmptyOrNull(ref)) {
                    writer.startNode(E_PARENT);
                    // ... intead of this: context.convertAnother()
                    // just handle it here.
                    writer.addAttribute(A_REF, ref);
                    writer.endNode();
                }
            }
        }

        if (!isEmptyOrNull(type)) {
            writer.startNode(E_TYPE);
            writer.setValue(type);
            writer.endNode();
        }

        if (!isEmptyOrNull(title)) {
            writer.startNode(E_TITLE);
            writer.setValue(title);
            writer.endNode();
        }

        if (creators != null) {
            for (String c : creators) {
                if (!isEmptyOrNull(c)) {
                    writer.startNode(E_CREATOR);
                    writer.setValue(c);
                    writer.endNode();
                }
            }
        }

        if (subjects != null) {
            for (String s : subjects) {
                if (!isEmptyOrNull(s)) {
                    writer.startNode(E_SUBJECT);
                    writer.setValue(s);
                    writer.endNode();
                }
            }
        }

        if (!isEmptyOrNull(rights)) {
            writer.startNode(E_RIGHTS);
            writer.setValue(rights);
            writer.endNode();
        }

        // Deprecated but included for backwards compatibility. DWD
        if (extRefs != null) {
            for (String r : extRefs) {
                if (!isEmptyOrNull(r)) {
                    writer.startNode(E_FORMER_EXT_REF);
                    writer.setValue(r);
                    writer.endNode();
                }
            }
        }

        if (isDigitalSurrogate != null) {
            writer.startNode(E_DIGITAL_SURROGATE);
            writer.setValue(isDigitalSurrogate.toString());
            writer.endNode();
        }

        if (mds != null) {
            for (DcsMetadata md : mds) {
                writer.startNode(MetadataConverter.E_METADATA);
                context.convertAnother(md);
                writer.endNode();
            }
        }

        if (mdRefs != null) {
            for (DcsMetadataRef mdr : mdRefs) {
                writer.startNode(MetadataConverter.E_METADATA);
                context.convertAnother(mdr);
                writer.endNode();
            }
        }

        if (relations != null) {
            for (DcsRelation r : relations) {
                writer.startNode(RelationConverter.E_RELATION);
                context.convertAnother(r);
                writer.endNode();
            }
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        final String id = reader.getAttribute(A_ID);
        final String ref = reader.getAttribute(A_REF);
        final String lineageid = reader.getAttribute(A_LINEAGE_ID);
        
        // Handle the reference
        if (!isEmptyOrNull(ref)) {
            final DcsDeliverableUnitRef duRef = new DcsDeliverableUnitRef();
            duRef.setRef(ref);
            return duRef;
        }

        // Otherwise handle the deliverable unit
        final DcsDeliverableUnit du = new DcsDeliverableUnit();

        if (!isEmptyOrNull(id)) {
            du.setId(id);
        }
        
        if (!isEmptyOrNull(lineageid)) {
            du.setLineageId(lineageid);
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            final String name = getElementName(reader);//reader.getNodeName();
            if (name.equals(CollectionConverter.E_COLLECTION)) {
                if (!isEmptyOrNull(reader.getAttribute(CollectionConverter.A_REF))) {
                    final DcsCollectionRef collRef = (DcsCollectionRef) context.convertAnother(du, DcsCollectionRef.class);
                    if (collRef != null) {
                        du.addCollection(collRef);
                    }
                } else {
                    final DcsCollection coll = (DcsCollection) context.convertAnother(du, DcsCollection.class);
                    if (coll != null) {
                        //TODO: this is where we would add a collection
                        log.info("Adding inline collections not supported");
                    }
                }
                reader.moveUp();
                continue;
            }

            if (name.equals(MetadataConverter.E_METADATA)) {
                if (!isEmptyOrNull(reader.getAttribute(MetadataConverter.A_REF))) {
                    final DcsMetadataRef mdRef = (DcsMetadataRef) context.convertAnother(du, DcsMetadataRef.class);
                    if (mdRef != null) {
                        du.addMetadataRef(mdRef);
                    }
                    reader.moveUp();
                    continue;
                } else {
                    final DcsMetadata md = (DcsMetadata) context.convertAnother(du, DcsMetadata.class);
                    if (md != null) {
                        du.addMetadata(md);
                    }
                    continue;
                }
            }

            if (name.equals(E_PARENT)) {
                final String parentRef = reader.getAttribute(A_REF);
                if (!isEmptyOrNull(parentRef)) {
                    DcsDeliverableUnitRef parent = new DcsDeliverableUnitRef();
                    parent.setRef(parentRef);
                    du.addParent(parent);
                }
                reader.moveUp();                
                continue;
            }

            if (name.equals(RelationConverter.E_RELATION)) {
                final DcsRelation rel = (DcsRelation) context.convertAnother(du, DcsRelation.class);
                if (rel != null) {
                    du.addRelation(rel);
                }
                reader.moveUp();
                continue;
            }

            final String value = reader.getValue();

            if (name.equals(E_TITLE) && !isEmptyOrNull(value)) {
                du.setTitle(value);
            }

            if (name.equals(E_CREATOR) && !isEmptyOrNull(value)) {
                du.addCreator(value);
            }

            if (name.equals(E_SUBJECT) && !isEmptyOrNull(value)) {
                du.addSubject(value);
            }

            if (name.equals(E_RIGHTS) && !isEmptyOrNull(value)) {
               du.setRights(value);
            }

            if (name.equals(E_ALTERNATEID)) {
                final DcsResourceIdentifier rid =
                    (DcsResourceIdentifier) context.convertAnother(du, DcsResourceIdentifier.class);
                if (rid != null) { du.addAlternateId(rid); }
                reader.moveUp();
                continue;
            }

            if (name.equals(E_FORMER_EXT_REF) && !isEmptyOrNull(value)) {
                du.addFormerExternalRef(value);
                // Deprecated but not removed for backward compatibility. Cannot combine with alt id in the
                // model due to scope of test modifications. Eventually will be handled in the SIP.
            }

            if (name.equals(E_DIGITAL_SURROGATE) && !isEmptyOrNull(value)) {
                try {
                    Boolean isSurrogate = Boolean.valueOf(value);
                    du.setDigitalSurrogate(isSurrogate);
                } catch (Exception e) {
                    final String msg = "Unable to parse boolean value from element " + E_TYPE + ": value was '" + value + "': " + e.getMessage();
                    log.error(msg, e);
                    throw new ConversionException(msg, e);
                }
            }

            if (name.equals(E_TYPE) && !isEmptyOrNull(value)) {
                du.setType(value);
            }

            reader.moveUp();
        }

        return du;
    }

    @Override
    public boolean canConvert(Class type) {
        return DcsDeliverableUnit.class == type || DcsDeliverableUnitRef.class == type;
    }
}
