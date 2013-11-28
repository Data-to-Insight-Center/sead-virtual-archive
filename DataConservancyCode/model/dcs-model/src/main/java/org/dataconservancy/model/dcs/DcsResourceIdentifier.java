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
 * A designation used to identify the resource within the preservation repository system in which it is stored.
 * Identifiers may be unique or not depending on policies applied to their use.
 */
public class DcsResourceIdentifier {

    private String authorityId;
    private String typeId;
    private String idValue;

    /**
     * Constructs a new DcsResourceIdentifier with no state.
     */
    public DcsResourceIdentifier() {

    }

    /**
     * Constructs a new DcsResourceIdentifier with the supplied state.
     *
     *
     * @param authorityId the authority id may be empty or {@code null}
     * @param idValue the value of the id <em>must not</em> be empty or {@code null}
     * @param typeId the type of the id may be empty or {@code null}
     * @throws IllegalArgumentException if {@code idValue} is empty or {@code null}
     */
    public DcsResourceIdentifier(String authorityId, String idValue, String typeId) {
        Assertion.notEmptyOrNull(idValue);
        this.authorityId = authorityId;
        this.idValue = idValue;
        this.typeId = typeId;
    }

    /**
     * Copy constructor for a DcsResourceIdentifier.  The state of <code>toCopy</code> is copied
     * to this.  Note that if the state of {@code toCopy} is modified concurrent to creating this DcsResourceIdentifier,
     * then the state of this is undefined.
     *
     * @param toCopy the dcs resource identifier to copy, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>toCopy</code> is <code>null</code>
     */
    public DcsResourceIdentifier(DcsResourceIdentifier toCopy) {
        Assertion.notNull(toCopy);
        this.authorityId = toCopy.getAuthorityId();
        this.typeId = toCopy.getTypeId();
        this.idValue = toCopy.getIdValue();
    }

    /**
     * The authority ID for the identity
     *
     * @return the authority ID, may be <code>null</code>
     */
    public String getAuthorityId() {
        return authorityId;
    }

    /**
     * Set the authority ID for identity
     *
     * @param authorityId the ID the authority for the identity
     */
    public void setAuthorityId(String authorityId) {
        this.authorityId = authorityId;
    }

    /**
     * The type ID for the identity
     *
     * @return the identity type ID, may be <code>null</code>
     */
    public String getTypeId() {
        return typeId;
    }

    /**
     * Set the value for identity's type
     *
     * @param typeId the ID the identity's type
     */
    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    /**
     * The value for the identity
     *
     * @return the identity's value, may be <code>null</code>
     */
    public String getIdValue() {
        return idValue;
    }

    /**
     * Set the value for identity
     *
     * @param idValue the value of the identity, must not be {@code null}, the empty or zero-length string
     * @throws IllegalArgumentException if {@code id} is {@code null}, the empty or zero-length string
     */
    public void setIdValue(String idValue) {
        Assertion.notEmptyOrNull(idValue);
        this.idValue = idValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DcsResourceIdentifier that = (DcsResourceIdentifier) o;

        if (authorityId != null ? !authorityId.equals(that.authorityId) : that.authorityId != null) return false;
        if (typeId != null ? !typeId.equals(that.typeId) : that.typeId != null) return false;
        if (idValue != null ? !idValue.equals(that.idValue) : that.idValue != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (authorityId != null ? authorityId.hashCode() : 0);
        result = 31 * result + (typeId != null ? typeId.hashCode() : 0);
        result = 31 * result + (idValue != null ? idValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DcsResourceIdentifier{" +
                "authorityId=" + authorityId +
                ", typeId=" + typeId +
                ", idValue=" + idValue +
                '}';
    }

    public void toString(HierarchicalPrettyPrinter stringBuilder) {
        stringBuilder.appendWithIndentAndNewLine("Resource Identifier:");
        stringBuilder.incrementDepth();
        stringBuilder.appendWithIndent("Authority ID: ").appendWithNewLine(authorityId);
        stringBuilder.appendWithIndent("Type ID: ").appendWithNewLine(typeId);
        stringBuilder.appendWithIndent("Identifier Value: ").appendWithNewLine(idValue);
        stringBuilder.decrementDepth();
    }

}
