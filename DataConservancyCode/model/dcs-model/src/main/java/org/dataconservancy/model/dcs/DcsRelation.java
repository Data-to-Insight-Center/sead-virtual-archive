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

import org.dataconservancy.model.dcs.support.Assertion;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;

/**
 * Models a relationship between DCS entities.
 *
 * @see DcsRelationship
 */
public class DcsRelation {
    private String relUri;
    private DcsEntityReference ref;

    /**
     * Creates a DcsRelationship with no state.
     */
    public DcsRelation() {
    }

    /**
     * Creates a DcsRelationship with a relationship type of <code>relUri</code> with <code>target</code>.  See
     * {@link DcsRelationship} for supported relationship types.
     *
     * @param relUri the relationship type, must not be empty or <code>null</code>
     * @param target the target entity, must not be empty or <code>null</code>
     */
    public DcsRelation(String relUri, String target) {
        Assertion.notEmptyOrNull(relUri);
        Assertion.notEmptyOrNull(target);
        this.relUri = relUri;
        this.ref = new DcsEntityReference(target);
    }

    /**
     * Creates a DcsRelationship with a relationship type of <code>rel</code> with <code>target</code>.  See
     * {@link DcsRelationship} for supported relationship types.
     *
     * @param rel the relationship type, must not be <code>null</code>
     * @param target the target entity, must not be empty or <code>null</code>
     */
    public DcsRelation(DcsRelationship rel, String target) {
        Assertion.notNull(rel);
        Assertion.notEmptyOrNull(target);
        this.relUri = rel.asString();
        this.ref = new DcsEntityReference(target);
    }

    /**
     * Creates a DcsRelationship with a relationship type of <code>relUri</code> with <code>target</code>.  See
     * {@link DcsRelationship} for supported relationship types.
     *
     * @param relUri the relationship type, must not be empty or <code>null</code>
     * @param target the target entity, must not be <code>null</code>
     */
    public DcsRelation(String relUri, DcsEntityReference target) {
        Assertion.notEmptyOrNull(relUri);
        Assertion.notNull(target);
        this.relUri = relUri;
        this.ref = target;
    }

    /**
     * Creates a DcsRelationship with a relationship type of <code>relUri</code> with <code>target</code>.  See
     * {@link DcsRelationship} for supported relationship types.
     *
     * @param rel the relationship type, must not be <code>null</code>
     * @param target the target entity, must not be <code>null</code>
     */
    public DcsRelation(DcsRelationship rel, DcsEntityReference target) {
        Assertion.notNull(rel);
        Assertion.notNull(target);
        this.relUri = rel.asString();
        this.ref = target;
    }

    /**
     * Creates a DcsRelation initialized with the state of {@code toCopy}.  Note that if {@code toCopy} is modified
     * while constructing this DcsRelation, the state of this DcsRelation will be undefined.
     *
     * @param toCopy the DcsRelation to copy
     */
    public DcsRelation(DcsRelation toCopy) {
        this.relUri = toCopy.getRelUri();
        this.ref = toCopy.getRef();
    }

    /**
     * The URI representing the relationship type.
     *
     * @return the URI in string form
     */
    public String getRelUri() {
        return relUri;
    }

    /**
     * The URI representing the relationship type.
     *
     * @param relUri the URI in string form, must not be {@code null}, or the zero-length or empty string
     * @throws IllegalArgumentException if {@code relUri} is {@code null}, or the zero-length or empty string
     */
    public void setRelUri(String relUri) {
        Assertion.notEmptyOrNull(relUri);
        this.relUri = relUri;
    }

    /**
     * The target, or object, of the relationship.
     *
     * @return a reference to the target of the relationship
     */
    public DcsEntityReference getRef() {
        return this.ref;
    }

    /**
     * The target, or object, of the relationship.
     *
     * @param ref the reference, must not be {@code null}
     * @throws IllegalArgumentException if {@code ref} is {@code null}
     */
    public void setRef(DcsEntityReference ref) {
        Assertion.notNull(ref);
        this.ref = ref;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DcsRelation that = (DcsRelation) o;

        if (ref != null ? !ref.equals(that.ref) : that.ref != null) return false;
        if (relUri != null ? !relUri.equals(that.relUri) : that.relUri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = relUri != null ? relUri.hashCode() : 0;
        result = 31 * result + (ref != null ? ref.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DcsRelation{" +
                "relUri='" + relUri + '\'' +
                ", ref=" + ref +
                '}';
    }

    public void toString(HierarchicalPrettyPrinter sb) {
        sb.appendWithIndentAndNewLine("DcsRelation:");
        sb.incrementDepth();
        sb.appendWithIndent("relUri: ").appendWithNewLine(relUri);
        ref.toString(sb, "");
        sb.decrementDepth();
    }

}
