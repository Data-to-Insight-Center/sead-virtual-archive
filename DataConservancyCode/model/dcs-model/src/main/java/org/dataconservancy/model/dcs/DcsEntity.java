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

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dataconservancy.model.dcs.support.Assertion;
import org.dataconservancy.model.dcs.support.CollectionFactory;
import org.dataconservancy.model.dcs.support.DefaultFieldComparator;
import org.dataconservancy.model.dcs.support.FieldFilter;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.model.dcs.support.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dataconservancy.model.dcs.support.Util.deepCopy;

/**
 * An identifiable entity in the Data Conservancy model
 */
public class DcsEntity {

    /** Unique DCS Assigned Identity for the entity. */
    private String id;

    /**
     * Alternate Identities captured in complete Identifiers. No assertion are made about the persistence or
     * uniqueness of them but they are needed context and provenance information.
     */
    private Collection<DcsResourceIdentifier> alternateIds = CollectionFactory.newCollection();

    /** The common logger to use. */
    private static final Logger LOG = LoggerFactory.getLogger(DcsEntity.class);


    /**
     * Constructs a new DcsEntity with no state.
     */
    public DcsEntity() {

    }

    /**
     * Constructs a new DcsEntity with the supplied entity identifier.  The identifer must not be null or an
     * empty string.
     *
     * @param entityId the entity id, must not be {@code null} or empty.
     */
    public DcsEntity(String entityId) {
        Assertion.notEmptyOrNull(entityId);
        this.id = entityId;
    }

    /**
     * Copy constructor for a DcsEntity.  The state of <code>toCopy</code> is copied
     * to this.  Note that if {@code toCopy} concurrently modified while constructing this
     * DcsEntity, then this state is undefined.
     *
     * @param toCopy the dcs entity to copy, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>toCopy</code> is <code>null</code>
     */
    public DcsEntity(DcsEntity toCopy) {
        Assertion.notNull(toCopy);
        this.id = toCopy.getId();
        deepCopy(toCopy.alternateIds, this.alternateIds);
    }

    /**
     * The identifier for the entity
     *
     * @return the identifier, may be <code>null</code>
     */
    public String getId() {
        return id;
    }

    /**
     * Former alternate ID set.
     *
     * @return the alternate ID set, may be empty but never {@code null}
     */
    public Collection<DcsResourceIdentifier> getAlternateIds() {
        return this.alternateIds;
    }

    /**
     * Set the alternate IDs
     *
     * @param alternateIds other IDs for this entity, must not be {@code null}, or contain {@code null} references
     * @throws IllegalArgumentException if {@code alternateIds} is {@code null}, or contains {@code null} references
     */
    public void setAlternateIds(Collection<DcsResourceIdentifier> alternateIds) {
        Assertion.notNull(alternateIds);
        Assertion.doesNotContainNull(alternateIds);
        this.alternateIds = alternateIds;
    }

    /**
     * Add an alternate ID
     *
     * @param alternateId references, must not be <code>null</code>
     * @throws IllegalArgumentException if {@code alternateIds} is {@code null}, or contains {@code null} references
     */
    public void addAlternateId(DcsResourceIdentifier... alternateId) {
        Assertion.notNull(alternateId);
        Assertion.doesNotContainNull(alternateId);
        this.alternateIds.addAll(Arrays.asList(alternateId));
    }

    /**
     * Set the identifier for the entity.
     *
     * @param id the identifier, must not be {@code null}, the empty or zero-length string
     * @throws IllegalArgumentException if {@code id} is {@code null}, the empty or zero-length string
     */
    public void setId(String id) {
        Assertion.notEmptyOrNull(id);
        this.id = id;
    }

    /**
     * Tests the equality of an entity by comparing member fields by value.  By-value equality testing requires that
     * Object <code>o</code> must be the same class as this.
     *
     * @param o the object to test for equality, must be the same class as this
     * @return true if the fields of the objects are equal in value
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DcsEntity dcsEntity = (DcsEntity) o;

        if (id != null ? !id.equals(dcsEntity.id) : dcsEntity.id != null) {
            return false;
        }

        /*
        if( alternateIds != null ? !alternateIds.equals(dcsEntity.alternateIds) : dcsEntity.alternateIds != null){
            return false;
        }*/

        return true;
    }

    /**
     * Generates a unique hash code based on the value of the member fields.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        String ids = "";
        for (DcsResourceIdentifier id : this.getAlternateIds()) {
            ids += id + ", ";
        }
        return "DcsEntity{" +
                "id='" + id + '\'' +
                "alternateIds=" + ids +
                '}';
    }

    public void toString(HierarchicalPrettyPrinter stringBuilder) {
        stringBuilder.appendWithIndentAndNewLine("DcsEntity:");
        stringBuilder.incrementDepth();
        stringBuilder.appendWithIndent("id: ").appendWithNewLine(id);
        for (DcsResourceIdentifier id : alternateIds) {
            id.toString(stringBuilder);
        }
        stringBuilder.decrementDepth();
    }

    /**
     * Tests the equality of an entity by filtering out un-wanted members, and comparing the remaining member fields by
     * value.  By-value equality testing requires that Object <code>o</code> must be the same class as this.
     * <p/>
     * <em><strong>N.B.</strong></em> This method is extremely sensitive to changes in the DCS object model, and should
     * only be used sparingly.
     *
     * @param o the object to test for equality, must be the same class as this
     * @param filter field filter used to filter out un-wanted members
     * @return true if the fields of the objects are equal in value
     */
    public boolean equals(Object o, FieldFilter filter) {
        return equals(o, filter, new DefaultFieldComparator());
    }

    /**
     * Tests the equality of an entity by filtering out un-wanted members, and comparing the remaining member fields by
     * value.  By-value equality testing requires that Object <code>o</code> must be the same class as this.  The
     * supplied <code>fieldComparator</code> will be used to sort fields prior to comparison.
     *
     * @param o the object to test for equality, must be the same class as this
     * @param filter field filter used to filter out un-wanted members
     * @param fieldComparator the comparator used to sort field members
     * @return true if the fields of the objects are equal in value
     */
    protected boolean equals(Object o, FieldFilter filter, Comparator<Field> fieldComparator) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final List<Field> myFields = new ArrayList<Field>();
        myFields.addAll(Arrays.asList(this.getClass().getDeclaredFields()));
        final List<Field> thoseFields = new ArrayList<Field>();
        thoseFields.addAll(Arrays.asList(o.getClass().getDeclaredFields()));

        // Filter the fields according to the FieldFilter
        filter.filter(myFields);
        filter.filter(thoseFields);

        // Sort the fields according to the comparator
        Collections.sort(myFields, fieldComparator);
        Collections.sort(thoseFields, fieldComparator);

        try {
            for (int i = 0; i < myFields.size(); i++) {
                final Field thisField = myFields.get(i);
                final Field thatField = thoseFields.get(i);

                if (!thisField.isAccessible()) {
                    thisField.setAccessible(true);
                }

                if (!thatField.isAccessible()) {
                    thatField.setAccessible(true);
                }

                if (thisField.equals(thatField)) {
                    final Object thisValue = thisField.get(this);
                    final Object thatValue = thatField.get(o);
                    LOG.trace("Comparing field [{}] value [{}] to field [{}] value [{}]",
                            new Object[]{thisField.getName(), thisValue, thatField.getName(), thatValue});

                    if (thisValue == thatValue) {
                        continue;
                    }

                    if (thisValue == null && thatValue != null) {
                        return false;
                    }

                    if (thisValue != null && thatValue == null) {
                        return false;
                    }

                    if (thisValue instanceof Collection && thatValue instanceof Collection) {
                        if (!Util.isEqual((Collection) thisValue, (Collection) thatValue)) {
                            return false;
                        }
                    } else {
                        if (!thisValue.equals(thatValue)) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return true;
    }
}
