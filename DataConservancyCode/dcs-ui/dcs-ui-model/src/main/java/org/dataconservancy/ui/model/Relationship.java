/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.ui.model;

/**
 * Encapsulates a relationship between a source object and a target object.
 * <p/>
 * In order to express that a {@code Person} administers a {@code Project}, one can construct the following
 * {@code Relationship}:
 * 
 * <pre>
 * new Relationship(personId, projectId, Relationship.RelType.IS_ADMINISTRATOR);
 * </pre>
 * 
 * Or the inverse:
 * 
 * <pre>
 * new Relationship(projectId, personId, Relationship.RelType.IS_ADMINISTRATED_BY);
 * </pre>
 * <p/>
 * Instances are immutable by design.
 */
public class Relationship {
    
    /**
     * Relationships supported by the {@link org.dataconservancy.ui.dao.RelationshipDAO}. TODO: keep as an inner class?
     */
    public static enum RelType {
        
        /**
         * Indicates that the source object administers the target object. Inverse of {@link #IS_ADMINISTERED_BY}.
         */
        IS_ADMINISTRATOR_FOR,
        
        /**
         * Indicates that the source object is administered by the target object. Inverse of
         * {@link #IS_ADMINISTRATOR_FOR}.
         */
        IS_ADMINISTERED_BY,
        
        /**
         * Indicates that the source object can deposit to the target object. Inverse of {@link #ACCEPTS_DEPOSIT}.
         */
        IS_DEPOSITOR_FOR,
        
        /**
         * Indicates that the source object accepts deposits from the target object. Inverse of
         * {@link #IS_DEPOSITOR_FOR}.
         */
        ACCEPTS_DEPOSIT,
        
        /**
         * Indicates that the source object aggregates the target object. Inverse of {@link #IS_AGGREGATED_BY}.
         */
        AGGREGATES,
        
        /**
         * Indicates that the source object is aggregated by the target object. Inverse of {@link #AGGREGATES}.
         */
        IS_AGGREGATED_BY,
        
        /**
         * Indicates that the source object is a contributor for the target object. Inverse of {@link #HAS_CONTRIBUTOR}.
         */
        IS_CONTRIBUTOR_FOR,
        
        /**
         * Indicates that the source object has the target object as a contributor. Inverse of
         * {@link #IS_CONTRIBUTOR_FOR}.
         */
        HAS_CONTRIBUTOR,
        
        /**
         * Indicates that the source collection object is a subcollection of the target collection object. Inverse of {@link #HAS_SUBCOLLECION}.
         */        
        IS_SUBCOLLECTION_OF,
        
        /**
         * Indicates that the source collection object has the target collection object as a subcollection. Inverse of {@link #IS_SUBCOLLECTION_OF}.
         */  
        HAS_SUBCOLLECTION,

        /**
         * Indicates that the source object has the target metadata file as a metadata file
         */
        HAS_METADATA_FILE
    }
    
    private final String source;
    private final String target;
    private final RelType type;
    
    /**
     * Constructs a new Relationship. None of the arguments may be null or empty.
     * 
     * @param source
     *            the source of the relationship ("subject")
     * @param target
     *            the target of the relationship ("object")
     * @param type
     *            the type of the relationship ("predicate")
     */
    public Relationship(String source, String target, RelType type) {
        if (source == null || source.isEmpty()) {
            throw new IllegalArgumentException("Source relationship identifier must not be empty or null.");
        }
        
        if (target == null || target.isEmpty()) {
            throw new IllegalArgumentException("Target relationship identifier must not be empty or null.");
        }
        
        if (type == null) {
            throw new IllegalArgumentException("Relationship type must not be null.");
        }
        
        this.source = source;
        this.target = target;
        this.type = type;
    }
    
    public String getSource() {
        return source;
    }
    
    public String getTarget() {
        return target;
    }
    
    public RelType getType() {
        return type;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        
        Relationship that = (Relationship) o;
        
        if (source != null ? !source.equals(that.source) : that.source != null)
            return false;
        if (target != null ? !target.equals(that.target) : that.target != null)
            return false;
        if (type != that.type)
            return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "Relationship{" + "source='" + source + '\'' + ", target='" + target + '\'' + ", type=" + type + '}';
    }
}
