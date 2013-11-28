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
 * Models a reference to a DCS entity.
 */
public class DcsEntityReference {
    private String ref;

    /**
     * Constructs a DcsEntityRef with no state
     */
    public DcsEntityReference() {
        
    }

    /**
     * Convenience constructor, constructing a reference to <code>ref</code>
     *
     * @param ref the reference
     */
    public DcsEntityReference(String ref) {
        Assertion.notEmptyOrNull(ref);
        this.ref = ref;
    }

    /**
     * Copy constructor for a DcsEntityRef.  The state of <code>toCopy</code> is copied
     * to this.
     *
     * @param toCopy the dcs entity to copy, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>toCopy</code> is <code>null</code>
     */
    public DcsEntityReference(DcsEntityReference toCopy) {
        Assertion.notNull(toCopy);
        this.ref = toCopy.getRef();
    }

    /**
     * The entity reference
     *
     * @return the entity reference, may be <code>null</code>
     */
    public String getRef() {
        return ref;
    }

    /**
     * Set the entity reference
     *
     * @param ref the entity reference, must not be empty or <code>null</code>
     * @throws IllegalArgumentException if <code>ref</code> is empty or <code>null</code>
     */
    public DcsEntityReference setRef(String ref) {
        Assertion.notEmptyOrNull(ref);
        this.ref = ref;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DcsEntityReference that = (DcsEntityReference) o;

        if (ref != null ? !ref.equals(that.ref) : that.ref != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ref != null ? ref.hashCode() : 0;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "{" +
                "ref='" + ref + '\'' +
                '}';
    }

    public void toString(HierarchicalPrettyPrinter sb, String indent) {
        sb.appendWithIndentAndNewLine(
                this.getClass().getName().replace(this.getClass().getPackage().getName() + ".", ""));
        sb.incrementDepth();
        sb.appendWithIndent("ref: ").appendWithNewLine(ref);
        sb.decrementDepth();
    }
}
