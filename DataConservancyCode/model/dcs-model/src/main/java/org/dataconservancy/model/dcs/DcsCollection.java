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
 * Models a Data Conservancy Collection
 *
 * @deprecated Consider modeling collections using {@link DcsDeliverableUnit} instead.
 */
public class DcsCollection extends DcsEntity {
    private CoreMetadata coreMd = new CoreMetadata();
    private DcsCollectionRef parent;
    private Collection<DcsMetadata> metadata = CollectionFactory.newCollection();
    private Collection<DcsMetadataRef> metadataRef = CollectionFactory.newCollection();

    /**
     * Constructs a new DcsCollection with no state.
     */
    public DcsCollection() {

    }

    /**
     * Copy constructor for a DcsCollection.  The state of <code>toCopy</code> is copied to this DcsCollection.  Note
     * that if {@code toCopy} is concurrently modified during this construction, the resulting state of this
     * DcsCollection is undefined.
     *
     * @param toCopy the collection to copy
     */
    public DcsCollection(DcsCollection toCopy) {
        super(toCopy);
        this.coreMd = new CoreMetadata(toCopy.coreMd);
        if (toCopy.parent != null) {
            this.parent = new DcsCollectionRef(toCopy.parent);
        }
        deepCopy(toCopy.metadata, this.metadata);
        deepCopy(toCopy.metadataRef, this.metadataRef);
    }

    /**
     * The parent collection.
     *
     * @return the parent collection, or <code>null</code> if no parent exists
     */
    public DcsCollectionRef getParent() {
        return parent;
    }

    /**
     * Set the parent of this DcsCollection
     *
     * @param parent the parent collection, may be <code>null</code>
     */
    public void setParent(DcsCollectionRef parent) {
        this.parent = parent;
    }

    /**
     * The metadata of this DcsCollection
     *
     * @return the collection metadata, may be empty, but never {@code null}
     */
    public Collection<DcsMetadata> getMetadata() {
        return this.metadata;
    }

    /**
     * Set this DcsCollection's metadata.  Calling this method will nullify any existing
     * {@link #getMetadataRef() metadata references}.
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
     * Add additional metadata to this {@code DcsCollection}.
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
     * The metadata of this DcsCollection, by reference.  References will only exist if they are explicitly set by
     * {@link #setMetadataRef(java.util.Set)} (i.e. this method does not return references for metadata set
     * {@link #setMetadata(java.util.Set) by value}).
     *
     * @return the collection metadata reference, may be empty but never {@code null}
     */
    public Collection<DcsMetadataRef> getMetadataRef() {
        return this.metadataRef;
    }

    /**
     * Set this DcsCollection's metadata by reference.
     *
     * @param metadataRef the metadata reference, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code metadataRef} is {@code null} or contains {@code null} references
     */
    public void setMetadataRef(Collection<DcsMetadataRef> metadataRef) {
        Assertion.notNull(metadataRef);
        Assertion.doesNotContainNull(metadataRef);
        this.metadataRef = metadataRef;
    }

    /**
     * Add additional metadata references to this {@code DcsCollection}
     *
     * @param metadataRef metadata refs, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code metadataRef} is {@code null} or contains {@code null} references
     */
    public void addMetadataRef(DcsMetadataRef... metadataRef) {
        Assertion.notNull(metadataRef);
        Assertion.doesNotContainNull(metadataRef);
        this.metadataRef.addAll(Arrays.asList(metadataRef));
    }

    /**
     * The title of this DcsCollection
     *
     * @return the title, or <code>null</code> if no title has been set
     */
    public String getTitle() {
        return coreMd.getTitle();
    }

    /**
     * Set the title of this DcsCollection
     *
     * @param title the title, must not be an empty string or <code>null</code>
     * @throws IllegalArgumentException if <code>title</code> is empty or <code>null</code>
     */
    public void setTitle(String title) {
        Assertion.notEmptyOrNull(title);
        coreMd.setTitle(title);
    }

    /**
     * The creator(s) of this DcsCollection.
     *
     * @return the creator(s), never <code>null</code>
     */
    public Collection<String> getCreators() {
        return coreMd.getCreators();
    }

    /**
     * Set the creator(s) of the collection.
     *
     * @param creators the creators, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if <code>creators</code> is <code>null</code>, contains {@code null} references,
     *                                  or the empty string
     */
    public void setCreators(Collection<String> creators) {
        Assertion.notNull(creators);
        Assertion.doesNotContainNullOrEmptyString(creators);
        coreMd.setCreators(creators);
    }

    /**
     * Add a creator to the collection.
     *
     * @param creator the creators, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>creator<code> is <code>null</code> or the empty string
     */
    public void addCreator(String... creator) {
        Assertion.notNull(creator);
        Assertion.doesNotContainNullOrEmptyString(creator);
        coreMd.addCreator(creator);
    }

    /**
     * The subject(s) of this DcsCollection, may be empty but never {@code null}
     *
     * @return the subject(s) of this DcsCollection, may be empty but never {@code null}
     */
    public Collection<String> getSubjects() {
        return coreMd.getSubjects();
    }

    /**
     * Set the subject(s) of this DcsCollection.  Note that this nullifies any existing subjects.
     *
     * @param subjects the subjects, must not be <code>null</code> or contain {@code null} references or empty
     *                 strings.
     * @throws IllegalArgumentException if <code>subjects</code> is <code>null</code>, contains {@code null}
     *                                  references, or the empty string.
     */
    public void setSubjects(Collection<String> subjects) {
        Assertion.notNull(subjects);
        Assertion.doesNotContainNullOrEmptyString(subjects);
        coreMd.setSubjects(subjects);
    }

    /**
     * Add a subject(s) to this DcsCollection
     *
     * @param subject the subject, must not be <code>null</code>, contain {@code null} references, or the empty string
     * @throws IllegalArgumentException if <code>subjects</code> is <code>null</code>, contains {@code null}
     *                                  references, or the empty string.
     */
    public void addSubject(String... subject) {
        Assertion.notNull(subject);
        Assertion.doesNotContainNullOrEmptyString(subject);
        coreMd.addSubject(subject);
    }

    /**
     * The type of this DcsCollection
     *
     * @return the type of this DcsCollection, may be {@code null}
     */
    public String getType() {
        return coreMd.getType();
    }

    /**
     * Set the type of this DcsCollection
     *
     * @param type the type of this DcsCollection, must not be {@code null} or the empty string
     * @throws IllegalArgumentException if {@code type} is {@code null} or the empty string
     */
    public void setType(String type) {
        Assertion.notEmptyOrNull(type);
        coreMd.setType(type);
    }

    /**
     * Obtain the so-called "core" metadata associated with this entity.
     *
     * @return the core metadata for this entity, never {@code null}
     */
    CoreMetadata getCoreMd() {
        return coreMd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DcsCollection that = (DcsCollection) o;

        if (coreMd != null ? !coreMd.equals(that.coreMd) : that.coreMd != null) return false;

        if (!Util.isEqual(metadata, that.metadata)) {
            return false;
        }

        if (!Util.isEqual(metadataRef, that.metadataRef)) {
            return false;
        }

        if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (coreMd != null ? coreMd.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (metadata != null ? Util.hashCode(metadata) : 0);
        result = 31 * result + (metadataRef != null ? Util.hashCode(metadataRef) : 0);
        return result;
    }

    @Override
    public String toString() {
        String ids = "";
        for (DcsResourceIdentifier id : this.getAlternateIds()) {
            ids += id + ", ";
        }
        return "{DcsCollection{" +
                "coreMd=" + coreMd +
                ", parent=" + parent +
                ", metadata=" + metadata +
                ", metadataRef=" + metadataRef +
                "}" + super.toString() + "}";
    }

    public void toString(HierarchicalPrettyPrinter sb) {
        sb.appendWithIndentAndNewLine("Collection:");
        sb.incrementDepth();

        super.toString(sb);

        if (parent != null) {
            parent.toString(sb, "");
        }

        coreMd.toString(sb);

        for (DcsMetadata md : metadata) {
            md.toString(sb);
        }

        for (DcsMetadataRef mdRef : metadataRef) {
            mdRef.toString(sb, "");
        }

        sb.decrementDepth();
    }
}
