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
package org.seadva.model.builder.xstream;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.dataconservancy.model.builder.xstream.*;
import org.dataconservancy.model.dcs.*;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadPerson;

import java.util.Collection;
import java.util.Set;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;


public class SeadDeliverableUnitConverter extends AbstractEntityConverter {

    public static final String E_TITLE = "title";
    public static final String E_CREATOR = "creator";
    public static final String E_SUBMITTER = "submitter";
    public static final String E_CONTACT = "contact";
    public static final String E_SUBJECT = "subject";
    public static final String E_LOCNAME = "primaryDataLocationName";
    public static final String E_LOCVAL = "primaryDataLocationValue";
    public static final String E_LOCTYPE = "primaryDataLocationType";
    public static final String E_FORMER_EXT_REF = "formerExternalRef";
    public static final String E_RELATION = "relation";
    public static final String E_PARENT = "parent";
    public static final String E_TYPE = "type";
    public static final String E_DIGITAL_SURROGATE = "digitalSurrogate";
    public static final String E_RIGHTS = "rights";
    public static final String E_ABSTRACT = "abstract";
    public static final String E_PUBDATE = "pubdate";
    public static final String E_METADATAUPDATEDATE = "metadataUpdateDate";
    public static final String E_SITE = "site";
    public static final String E_SIZEBYTES = "sizeBytes";
    public static final String E_FILENO = "fileNo";
    public static final String E_ALTERNATEID = "alternateIdentifier";
    public static final String A_ID = "id";
    public static final String A_REF = "ref";
    public static final String A_REL = "rel";
    public static final String E_SDATALOCATION = "secondaryDataLocation";
    public static final String E_FGDC = "Fgdc";

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
        final SeadDeliverableUnit du = (SeadDeliverableUnit) source;

        final String id = du.getId();
        final Collection<DcsResourceIdentifier> altIds = du.getAlternateIds();
        final String title = du.getTitle();
        final Collection<DcsCollectionRef> cRefs = du.getCollections();
        final Collection<DcsMetadataRef> mdRefs = du.getMetadataRef();
        final Collection<DcsMetadata> mds = du.getMetadata();
        final Collection<SeadPerson> creators = du.getDataContributors();
        final Collection<String> extRefs = du.getFormerExternalRefs();  // Being deprecated, use AltIds DWD
        final Collection<DcsRelation> relations = du.getRelations();
        final Collection<String> subjects = du.getSubjects();
        final Collection<DcsDeliverableUnitRef> parents = du.getParents();
        final Boolean isDigitalSurrogate = du.isDigitalSurrogate();
        final String type = du.getType();
        final String rights = du.getRights();
        final String abstrct = du.getAbstrct();
        final Set<String> location = du.getSites();
        final long  size = du.getSizeBytes();
        final long fileNo = du.getFileNo();
        final String contact = du.getContact();
        final String pubdate = du.getPubdate();
        final String metadataUpdateDate = du.getMetadataUpdateDate();

        String locationName = null;
        String locationType = null;
        String locationValue = null;
        if(du.getPrimaryLocation()!=null){
            locationName = du.getPrimaryLocation().getName();
            locationType = du.getPrimaryLocation().getType();
            locationValue = du.getPrimaryLocation().getLocation();
        }
        //  final DcsDataLocation primaryDataLocation = du.getPrimaryDataLocation();
        final Collection<SeadDataLocation> secondaryDataLocations = du.getSecondaryDataLocations();
        final SeadPerson submitter = du.getSubmitter();

        if (!isEmptyOrNull(id)) {
            writer.addAttribute(A_ID, id);
        }

        if (altIds != null) {
            for (DcsResourceIdentifier rid : altIds) {
                writer.startNode(E_ALTERNATEID);
                context.convertAnother(rid);
                writer.endNode();
            }
        }

        if (secondaryDataLocations != null) {
            for (SeadDataLocation dataLocation : secondaryDataLocations) {
                writer.startNode(E_SDATALOCATION);
                context.convertAnother(dataLocation);
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
            for (SeadPerson c : creators) {
                if (c!=null) {
                    writer.startNode(E_CREATOR);
                    context.convertAnother(c);
                    writer.endNode();
                }
            }
        }

        if (submitter != null&submitter.getId()!=null) {
            writer.startNode(E_SUBMITTER);
            context.convertAnother(submitter);
            writer.endNode();
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

        if (location != null) {
            for (String s : location) {
                if (!isEmptyOrNull(s)) {
                    writer.startNode(E_SITE);
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

        if (!isEmptyOrNull(abstrct)) {
            writer.startNode(E_ABSTRACT);
            writer.setValue(abstrct);
            writer.endNode();
        }

        if (!isEmptyOrNull(pubdate)) {
            writer.startNode(E_PUBDATE);
            writer.setValue(pubdate);
            writer.endNode();
        }

        if (!isEmptyOrNull(metadataUpdateDate)) {
            writer.startNode(E_METADATAUPDATEDATE);
            writer.setValue(metadataUpdateDate);
            writer.endNode();
        }


        if (size>-1) {
            writer.startNode(E_SIZEBYTES);
            writer.setValue(String.valueOf(size));
            writer.endNode();
        }
        if (fileNo>-1) {
            writer.startNode(E_FILENO);
            writer.setValue(String.valueOf(fileNo));
            writer.endNode();
        }

        if (!isEmptyOrNull(contact)) {
            writer.startNode(E_CONTACT);
            writer.setValue(contact);
            writer.endNode();
        }

        if (!isEmptyOrNull(locationName)) {
            writer.startNode(E_LOCNAME);
            writer.setValue(locationName);
            writer.endNode();
        }

        if (!isEmptyOrNull(locationType)) {
            writer.startNode(E_LOCTYPE);
            writer.setValue(locationType);
            writer.endNode();
        }

        if (!isEmptyOrNull(locationValue)) {
            writer.startNode(E_LOCVAL);
            writer.setValue(locationValue);
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

        // Handle the reference
        if (!isEmptyOrNull(ref)) {
            final DcsDeliverableUnitRef duRef = new DcsDeliverableUnitRef();
            duRef.setRef(ref);
            return duRef;
        }

        // Otherwise handle the deliverable unit
        final SeadDeliverableUnit du = new SeadDeliverableUnit();

        if (!isEmptyOrNull(id)) {
            du.setId(id);
        }
        boolean locationNameExists = false;
        boolean locationTypeExists = false;
        boolean locationValueExists = false;
        SeadDataLocation dataLocation = null;


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


            if (name.equals(E_CREATOR)) {
                final SeadPerson creator = (SeadPerson) context.convertAnother(du, SeadPerson.class);
                if (creator != null) {
                    du.addDataContributor(creator);
                }
                reader.moveUp();
                continue;
            }

            if (name.equals(E_SUBMITTER)) {
                final SeadPerson submitter = (SeadPerson) context.convertAnother(du, SeadPerson.class);
                if (submitter != null)
                    du.setSubmitter(submitter);
            }

            if (name.equals(E_SUBJECT) && !isEmptyOrNull(value)) {
                du.addSubject(value);
            }

            if (name.equals(E_RIGHTS) && !isEmptyOrNull(value)) {
                du.setRights(value);
            }

            if (name.equals(E_ABSTRACT) && !isEmptyOrNull(value)) {
                du.setAbstrct(value);
            }

            if (name.equals(E_SITE) && !isEmptyOrNull(value)) {
                du.addSite(value);
            }


            if (name.equals(E_SIZEBYTES) && !isEmptyOrNull(value)) {
                try {
                    du.setSizeBytes(Long.parseLong(value));
                }catch (Exception e) {
                    final String msg = "Unable to parse long value '" + value + "' for element '" + E_SIZEBYTES + "': " + e.getMessage();
                    throw new ConversionException(msg, e);
                }
            }

            if (name.equals(E_FILENO) && !isEmptyOrNull(value)) {
                try {
                    du.setFileNo(Long.parseLong(value));
                }catch (Exception e) {
                    final String msg = "Unable to parse long value '" + value + "' for element '" + E_FILENO + "': " + e.getMessage();
                    throw new ConversionException(msg, e);
                }
            }

            if (name.equals(E_PUBDATE) && !isEmptyOrNull(value)) {
                du.setPubdate(value);
            }

            if (name.equals(E_METADATAUPDATEDATE) && !isEmptyOrNull(value)) {
                du.setMetadataUpdateDate(value);
            }

            if (name.equals(E_CONTACT) && !isEmptyOrNull(value)) {
                du.setContact(value);
            }


            if (name.equals(E_LOCNAME) && !isEmptyOrNull(value)) {
                if(dataLocation==null)
                    dataLocation = new SeadDataLocation();
                dataLocation.setName(value);
                locationNameExists = true;
            }
            if (name.equals(E_LOCTYPE) && !isEmptyOrNull(value)) {
                if(dataLocation==null)
                    dataLocation = new SeadDataLocation();
                dataLocation.setType(value);
                locationTypeExists = true;
            }

            if (name.equals(E_LOCVAL) && !isEmptyOrNull(value)) {
                if(dataLocation==null)
                    dataLocation = new SeadDataLocation();
                dataLocation.setLocation(value);
                locationValueExists = true;
            }

            if(locationNameExists&&locationTypeExists&&locationValueExists){
                du.setPrimaryLocation(dataLocation);
                locationNameExists=false;
                locationTypeExists=false;
                locationValueExists = false;
            }

            if (name.equals(E_ALTERNATEID)) {
                final DcsResourceIdentifier rid =
                        (DcsResourceIdentifier) context.convertAnother(du, DcsResourceIdentifier.class);
                if (rid != null) { du.addAlternateId(rid); }
                reader.moveUp();
                continue;
            }

            if (name.equals(E_SDATALOCATION)) {
                final SeadDataLocation dataLoc =
                        (SeadDataLocation) context.convertAnother(du, SeadDataLocation.class);
                if (dataLocation != null) { du.addSecondaryDataLocation(dataLoc); }
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
        return SeadDeliverableUnit.class == type || DcsDeliverableUnitRef.class == type;
    }
}
