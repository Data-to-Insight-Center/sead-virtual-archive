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
import org.dataconservancy.model.dcs.support.CollectionFactory;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.model.dcs.support.Util;

import java.util.Collection;

import static org.dataconservancy.model.dcs.support.Util.deepCopy;

/**
 * Models a Data Conservancy Manifestation File
 */
public class DcsManifestationFile {
    private String path;
    private DcsFileRef ref;
    private Collection<DcsRelation> relSet = CollectionFactory.newCollection();

    /**
     * Constructs a new DcsManifestationFile with no state.
     */
    public DcsManifestationFile() {

    }

    /**
     * Copy constructor for a DcsManifestationFile.  The state of <code>toCopy</code> is copied
     * to this.  Note if {@code toCopy} is modified while constructing this DcsManifestationFile,
     * the state of this DcsManifestationFile is undefined.
     *
     * @param toCopy the dcs manifestation file to copy
     */
    public DcsManifestationFile(DcsManifestationFile toCopy) {
        this.path = toCopy.path;
        this.ref = toCopy.ref;
        deepCopy(toCopy.relSet, this.relSet);
    }

    /**
     * The path for the manifestation file
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the path for the manifestation file
     *
     * @param path the path, must not be empty, the zero-length string or {@code null}
     * @throws IllegalArgumentException if {@code path} is {@code null}, the zero-length or empty string
     */
    public void setPath(String path) {
        Assertion.notEmptyOrNull(path);
        this.path = path;
    }

    /**
     * The reference to the DcsFile
     *
     * @return the reference to the DcsFile
     */
    public DcsFileRef getRef() {
        return this.ref;
    }

    /**
     * The reference to the DcsFile
     *
     * @param ref the reference to the DcsFile, must not be {@code null}
     * @throws IllegalArgumentException if {@code ref} is {@code null}
     */
    public void setRef(DcsFileRef ref) {
        Assertion.notNull(ref);
        this.ref = ref;
    }

    /**
     * Obtain relationships that this manifestation file asserts.
     *
     * @return a set of DcsRelations, never <code>null</code>.
     */
    public Collection<DcsRelation> getRelSet() {
        return this.relSet;
    }

    /**
     * Relationships that this manifestation file asserts.  This overwrites any existing relationships.
     *
     * @param relSet a set of DcsRelations, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code relSet} is {@code null} or contains {@code null} references
     */
    public void setRelSet(Collection<DcsRelation> relSet) {
        Assertion.notNull(relSet);
        Assertion.doesNotContainNull(relSet);
        this.relSet = relSet;
    }

    /**
     * Add a relationship to this manifestation file.
     *
     * @param rel the relationship, must not be <code>null</code>.
     * @throws IllegalArgumentException if {@code re} is {@code null}
     */
    public void addRel(DcsRelation rel) {
        Assertion.notNull(rel);
        this.relSet.add(rel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DcsManifestationFile that = (DcsManifestationFile) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (ref != null ? !ref.equals(that.ref) : that.ref != null) return false;
        if (!Util.isEqual(relSet, that.relSet)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (ref != null ? ref.hashCode() : 0);
        result = 31 * result + (relSet != null ? Util.hashCode(relSet) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DcsManifestationFile{" +
                "path='" + path + '\'' +
                ", ref=" + ref +
                ", relSet=" + relSet +
                '}';
    }

    public void toString(HierarchicalPrettyPrinter sb, String indent) {
        sb.appendWithIndentAndNewLine("Manifestation File:");
        sb.incrementDepth();
        sb.appendWithIndent("path: ").appendWithNewLine(path);
        ref.toString(sb, "");
        for (DcsRelation rel : relSet) {
            rel.toString(sb);
        }
        sb.decrementDepth();
    }
}
