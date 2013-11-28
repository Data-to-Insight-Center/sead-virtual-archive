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
package org.dataconservancy.model.dcs;

import java.util.Arrays;
import java.util.Collection;

import org.dataconservancy.model.dcs.support.Assertion;
import org.dataconservancy.model.dcs.support.CollectionFactory;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.model.dcs.support.Util;

import static org.dataconservancy.model.dcs.support.Util.deepCopy;

/**
 * Models a Data Conservancy Deliverable Unit
 */
public class DcsDeliverableUnit extends DcsEntity {

    private Collection<DcsCollectionRef> collections = CollectionFactory.newCollection();
    private Collection<DcsMetadata> metadata = CollectionFactory.newCollection();
    private Collection<DcsMetadataRef> metadataRefs = CollectionFactory.newCollection();
    private Collection<DcsRelation> relations = CollectionFactory.newCollection();
    private Collection<String> formerExternalRefs = CollectionFactory.newCollection();
    private Collection<DcsDeliverableUnitRef> parents = CollectionFactory.newCollection();
    private Boolean isDigitalSurrogate;
    private CoreMetadata coreMd = new CoreMetadata();
    private String lineageId;

    /**
     * Constructs a new DcsDeliverableUnit with no state
     */
    public DcsDeliverableUnit() {

    }

    /**
     * Constructs a DcsDeliverableUnit with state deeply copied from {@code toCopy}.  Note that if {@code toCopy} is
     * concurrently modified during this construction, the resulting state of this DcsDeliverableUnit is undefined.
     *
     * @param toCopy the deliverable unit to copy, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>toCopy</code> is <code>null</code>
     */
    public DcsDeliverableUnit(DcsDeliverableUnit toCopy) {
        super(toCopy);
        this.coreMd = new CoreMetadata(toCopy.coreMd);
        deepCopy(toCopy.collections, this.collections);
        deepCopy(toCopy.metadata, this.metadata);
        deepCopy(toCopy.metadataRefs, this.metadataRefs);
        deepCopy(toCopy.relations, this.relations);
        deepCopy(toCopy.formerExternalRefs, this.formerExternalRefs);
        deepCopy(toCopy.parents, this.parents);
        this.isDigitalSurrogate = toCopy.isDigitalSurrogate();
        this.lineageId = toCopy.lineageId;
    }

    /**
     * The title of the deliverable unit.
     *
     * @return the title, or <code>null</code> if no title has been set
     */
    public String getTitle() {
        return coreMd.getTitle();
    }

    /**
     * Set the title of the deliverable unit.
     *
     * @param title the title, must not be an empty string or <code>null</code>
     * @throws IllegalArgumentException if <code>title</code> is empty or <code>null</code>
     */
    public void setTitle(String title) {
        coreMd.setTitle(title);
    }

    /**
     * The creator(s) of the deliverable unit.
     *
     * @return the creator(s), may be empty, but never <code>null</code>
     */
    public Collection<String> getCreators() {
        return coreMd.getCreators();
    }

    /**
     * Set the creator(s) of the deliverable unit.  Note this nullifies any existing creator(s).
     *
     * @param creators the creators, must not be {@code null}, contain {@code null} references, or contain empty
     *                 or zero-length strings
     * @throws IllegalArgumentException if {@code creators} is {@code null}, contains {@code null} references, or
     *                                  contains empty or zero-length strings.
     */
    public void setCreators(Collection<String> creators) {
        coreMd.setCreators(creators);
    }

    /**
     * The identifier for the lineage of this DcsDeliverableUnit.  Deliverable Units receive a lineage identifier
     * after being deposited to the DCS archive.
     *
     * @return the lineage identifier, may be <code>null</code>
     */
    public String getLineageId() {
        return lineageId;
    }

    /**
     * Set the identifier for the lineage of this DcsDeliverableUnit.  Deliverable Units receive a lineage identifier
     * after being deposited to the DCS archive.
     *
     * @param lineageId the lineage identifier, must not be <code>null</code> or the zero-length or empty string
     * @throws IllegalArgumentException if {@code id} is {@code null}, the empty string, or a zero-length string
     */
    public void setLineageId(String lineageId) {
        Assertion.notEmptyOrNull(lineageId);
        this.lineageId = lineageId;
    }

    /**
     * Add a creator to the deliverable unit.
     *
     * @param creator the creators, must not be {@code null}, contain {@code null} references, zero-length or empty
     *                strings
     * @throws IllegalArgumentException if {@code creator}is {@code null}, or contains {@code null} references, zero-
     *                                  length or empty strings.
     */
    public void addCreator(String... creator) {
        coreMd.addCreator(creator);
    }

    /**
     * The subject(s) of the deliverable unit.
     *
     * @return the subject(s) of the deliverable unit
     */
    public Collection<String> getSubjects() {
        return coreMd.getSubjects();
    }

    /**
     * Set the subject(s) of the deliverable unit.
     *
     * @param subjects the subjects, must not be {@code null}, contain {@code null} references, zero-length or empty
     *                 strings
     * @throws IllegalArgumentException if {@code subjects} is {@code null}, or contains {@code null} references, zero-
     *                                  length or empty strings.
     */
    public void setSubjects(Collection<String> subjects) {
        coreMd.setSubjects(subjects);
    }

    /**
     * Add a subject
     *
     * @param subject the subject, must not be {@code null}, the zero-length or empty string
     * @throws IllegalArgumentException if {@code subject} is {@code null}, the zero-length or empty string
     */
    public void addSubject(String... subject) {
        coreMd.addSubject(subject);
    }

    /**
     * The rights for this deliverable unit.
     *
     * @return the rights, may be {@code null}
     */
    public String getRights() {
        return coreMd.getRights();
    }

    /**
     * Set the rights for this deliverable unit.
     *
     * @param rights the rights, must not be {@code null}, the zero-length or empty string
     * @throws IllegalArgumentException if {@code rights} is {@code null}, the zero-length or empty string
     */
    public void setRights(String rights) {
        coreMd.setRights(rights);
    }

    /**
     * The collections for the deliverable unit
     *
     * @return the collections, may be empty but never {@code null}
     */
    public Collection<DcsCollectionRef> getCollections() {
        return this.collections;
    }

    /**
     * Set the collections for the deliverable unit.  Note this nullifies existing collections.
     *
     * @param collections the collections, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code collections} is {@code null}, or contains {@code null} references
     */
    public void setCollections(Collection<DcsCollectionRef> collections) {
        Assertion.notNull(collections);
        Assertion.doesNotContainNull(collections);
        this.collections = collections;
    }

    /**
     * Add a collection to the deliverable unit
     *
     * @param collection the collection, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code collection} is {@code null} or contains {@code null} references
     */
    public void addCollection(DcsCollectionRef... collection) {
        Assertion.notNull(collection);
        Assertion.doesNotContainNull(collection);
        this.collections.addAll(Arrays.asList(collection));
    }

    /**
     * The metadata for the deliverable unit
     *
     * @return the metadata, may be empty but never {@code null}
     */
    public Collection<DcsMetadata> getMetadata() {
        return this.metadata;
    }

    /**
     * Set the metadata for the deliverable unit.  Note this nullifies any existing metadata.
     *
     * @param metadata the metadata, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code metadata} is {@code null} or contains {@code null} references
     */
    public void setMetadata(Collection<DcsMetadata> metadata) {
        Assertion.notNull(metadata);
        Assertion.doesNotContainNull(metadata);
        this.metadata = metadata;
    }

    /**
     * Add metadata to the deliverable unit
     *
     * @param metadata the metadata, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code metadata} is {@code null} or contains {@code null} references
     */
    public void addMetadata(DcsMetadata... metadata) {
        Assertion.notNull(metadata);
        Assertion.doesNotContainNull(metadata);
        this.metadata.addAll(Arrays.asList(metadata));
    }

    /**
     * The metadata references for this DcsDeliverableUnit
     *
     * @return metadata references, may be empty but never {@code null}
     */
    public Collection<DcsMetadataRef> getMetadataRef() {
        return this.metadataRefs;
    }

    /**
     * Set metadata references.  Note this nullifies existing metadata references.
     *
     * @param metadataRefs metadata refs, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code metadataRefs} is {@code null} or contains {@code null} references
     */
    public void setMetadataRef(Collection<DcsMetadataRef> metadataRefs) {
        Assertion.notNull(metadataRefs);
        Assertion.doesNotContainNull(metadataRefs);
        this.metadataRefs = metadataRefs;
    }

    /**
     * Add metadata references
     *
     * @param metadataRefs metadata refs, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code metadataRefs} is {@code null} or contains {@code null} references
     */
    public void addMetadataRef(DcsMetadataRef... metadataRefs) {
        Assertion.notNull(metadataRefs);
        Assertion.doesNotContainNull(metadataRefs);
        this.metadataRefs.addAll(Arrays.asList(metadataRefs));
    }

    /**
     * The relationships of this DcsDeliverableUnit
     *
     * @return relationships the relationships, may be empty but never {@code null}
     */
    public Collection<DcsRelation> getRelations() {
        return this.relations;
    }

    /**
     * Set relationships of this DcsDeliverableUnit.  Note this nullifies any existing relationships.
     *
     * @param relations the relationships, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code relations} is {@code null} or contains {@code null} references
     */
    public void setRelations(Collection<DcsRelation> relations) {
        Assertion.notNull(relations);
        Assertion.doesNotContainNull(relations);
        this.relations = relations;
    }

    /**
     * Add relationships
     *
     * @param relation the relationships, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code relation} is {@code null} or contains {@code null} references
     */
    public void addRelation(DcsRelation... relation) {
        Assertion.notNull(relation);
        Assertion.doesNotContainNull(relation);
        this.relations.addAll(Arrays.asList(relation));
    }

    /**
     * Former external references
     *
     * @return the former external references, may be empty but never {@code null}
     */
    public Collection<String> getFormerExternalRefs() {
        return this.formerExternalRefs;
    }

    /**
     * Set former external references.  Note this nullifies any existing external references.
     *
     * @param formerExternalRefs references, must not be {@code null}, contain {@code null} references, zero-length or
     *                           empty strings
     * @throws IllegalArgumentException if {@code formerExternalRefs} is {@code null}, or contains {@code null}
     *                                  references, zero- length or empty strings.
     */
    public void setFormerExternalRefs(Collection<String> formerExternalRefs) {
        Assertion.notNull(formerExternalRefs);
        Assertion.doesNotContainNullOrEmptyString(formerExternalRefs);
        this.formerExternalRefs = formerExternalRefs;
    }

    /**
     * Add former external references
     *
     * @param formerExternalRef references, must not be {@code null}, contain {@code null} references, zero-length or
     *                          empty strings
     * @throws IllegalArgumentException if {@code formerExternalRefs} is {@code null}, or contains {@code null}
     *                                  references, zero- length or empty strings.
     */
    public void addFormerExternalRef(String... formerExternalRef) {
        Assertion.notNull(formerExternalRef);
        Assertion.doesNotContainNullOrEmptyString(formerExternalRef);
        formerExternalRefs.addAll(Arrays.asList(formerExternalRef));
    }

    /**
     * Parent deliverable units
     *
     * @return the parent deliverable unit references, may be empty but never {@code null}
     */
    public Collection<DcsDeliverableUnitRef> getParents() {
        return this.parents;
    }

    /**
     * Set parent deliverable units.  Note this nullifies any existing parents.
     *
     * @param parents parent deliverable units, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code parents} is {@code null} or contains {@code null} references
     */
    public void setParents(Collection<DcsDeliverableUnitRef> parents) {
        Assertion.notNull(parents);
        Assertion.doesNotContainNull(parents);
        this.parents = parents;
    }

    /**
     * Add parent deliverable units
     *
     * @param parent parent deliverable units, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code parents} is {@code null} or contains {@code null} references
     */
    public void addParent(DcsDeliverableUnitRef... parent) {
        Assertion.notNull(parent);
        Assertion.doesNotContainNull(parent);
        this.parents.addAll(Arrays.asList(parent));
    }

    public Boolean isDigitalSurrogate() {
        return (isDigitalSurrogate == null) ? null : Boolean.valueOf(isDigitalSurrogate);
    }

    public void setDigitalSurrogate(Boolean digitalSurrogate) {
        Assertion.notNull(digitalSurrogate);
        isDigitalSurrogate = Boolean.valueOf(digitalSurrogate);
    }

    CoreMetadata getCoreMd() {
        return new CoreMetadata(coreMd);
    }

    void setCoreMd(CoreMetadata coreMd) {
        this.coreMd = coreMd;
    }

    public String getType() {
        return coreMd.getType();
    }

    public void setType(String type) {
        coreMd.setType(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DcsDeliverableUnit that = (DcsDeliverableUnit) o;

        if (!Util.isEqual(collections, that.collections)) {
            return false;
        }

        if (coreMd != null ? !coreMd.equals(that.coreMd) : that.coreMd != null) return false;

        if (!Util.isEqual(formerExternalRefs, that.formerExternalRefs)) {
            return false;
        }

        if (isDigitalSurrogate != null ? !isDigitalSurrogate.equals(that.isDigitalSurrogate) : that.isDigitalSurrogate != null)
            return false;

        if (!Util.isEqual(metadata, that.metadata)) {
            return false;
        }

        if (!Util.isEqual(metadataRefs, that.metadataRefs)) {
            return false;
        }

        if (!Util.isEqual(parents, that.parents)) {
            return false;
        }

        if (!Util.isEqual(relations, that.relations)) {
            return false;
        }

        if (lineageId != null ? !lineageId.equals(that.lineageId) : that.lineageId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (collections != null ? Util.hashCode(collections) : 0);
        result = 31 * result + (metadata != null ? Util.hashCode(metadata) : 0);
        result = 31 * result + (metadataRefs != null ? Util.hashCode(metadataRefs) : 0);
        result = 31 * result + (relations != null ? Util.hashCode(relations) : 0);
        result = 31 * result + (formerExternalRefs != null ? Util.hashCode(formerExternalRefs) : 0);
        result = 31 * result + (parents != null ? Util.hashCode(parents) : 0);
        result = 31 * result + (isDigitalSurrogate != null ? isDigitalSurrogate.hashCode() : 0);
        result = 31 * result + (coreMd != null ? coreMd.hashCode() : 0);
        result = 31 * result + (lineageId != null ? lineageId.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        String ids = "";
        for (DcsResourceIdentifier id : this.getAlternateIds()) {
            ids += id + ", ";
        }
        return "{DcsDeliverableUnit{" +
                "collections=" + collections +
                ", metadata=" + metadata +
                ", metadataRefs=" + metadataRefs +
                ", relations=" + relations +
                ", formerExternalRefs=" + formerExternalRefs +
                ", parents=" + parents +
                ", isDigitalSurrogate=" + isDigitalSurrogate +
                ", coreMd=" + coreMd +
                ", alternateIds=" + ids +
                ", lineageId=" + lineageId +
                "}" + super.toString() + "}";
    }

    public void toString(HierarchicalPrettyPrinter sb) {
        sb.appendWithIndentAndNewLine("Deliverable Unit:");

        sb.incrementDepth();

        super.toString(sb);

        for (DcsCollectionRef c : collections) {
            c.toString(sb, "");
        }

        for (DcsMetadata m : metadata) {
            m.toString(sb);
        }

        for (DcsMetadataRef mdRef : metadataRefs) {
            mdRef.toString(sb, "");
        }

        for (DcsRelation rel : relations) {
            rel.toString(sb);
        }

        for (String s : formerExternalRefs) {
            sb.appendWithIndent("formerRef: ").appendWithNewLine(s);
        }

        coreMd.toString(sb);

        for (DcsDeliverableUnitRef ref : parents) {
            sb.appendWithIndent("parent: ");
            ref.toString(sb, "");
        }

        sb.appendWithIndent("isDigitalSurrogate: ").appendWithNewLine(String.valueOf(isDigitalSurrogate));

        sb.appendWithIndent("lineageId: ").appendWithNewLine(lineageId);

        sb.decrementDepth();
    }
}
