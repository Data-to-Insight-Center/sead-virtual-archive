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
package org.dataconservancy.ui.dao;

import org.dataconservancy.ui.model.Relationship;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Provides low-level mechanisms for creating, removing, and querying relationships between objects.  The initial intent
 * is to support relationships between business objects in the DC User Interface, but it doesn't preclude the
 * management of relationships between objects in the UI and the DCS.
 * <p/>
 * For example, to indicate that a Project (identified by {@code projectId} is administered by a Person
 * (identified by {@code personId}), one can add that relationship by invoking:
 * <pre>
 *     RelationshipDAO dao = new RelDaoImpl();
 *     dao.addRelation(projectId, personId, Relationship.RelType.IS_ADMINISTERED_BY);
 * </pre>
 * The inverse relationship like so:
 * <pre>
 *     dao.addRelation(personId, projectId, Relationship.RelType.IS_ADMINISTRATOR_FOR);
 * </pre>
 * To determine which Persons can deposit to a Collection, one may invoke:
 * <pre>
 *     dao.getRelations(collectionId, Relationship.RelType.IS_DEPOSITOR_FOR);
 * </pre>
 *
 * @see Relationship
 */
public interface RelationshipDAO {


    /**
     * Enumeration specifying which end of the arrow relationship an object occupies in a search which
     * is not ignoring this aspect.
     */
    public static enum RelEnd{
        SOURCE,
        TARGET
    }

    /**
     * Adds a new relationship.  Adding a relationship that already exists should have no effect; a duplicate
     * relationship should not be added.  Furthermore, no exceptions should be thrown when adding a duplicate
     * relationship is attempted.
     *
     * @param sourceId the identifier of the source object of the relationship
     * @param targetId the identifier of the target object of the relationship
     * @param rel the relationship type
     * @throws IllegalArgumentException if any of the arguments are {@code null}
     */
    public void addRelation(String sourceId, String targetId, Relationship.RelType rel);

    /**
     * Adds a set of new relationships.  The same rules for adding a single relationship apply for adding a
     * {@code Set} of relationships.  Implementations may optimize the addition of a {@code Set} of relationships
     * versus adding a single relationship at a time.
     *
     * @param relationships the {@code Set} of relationships to add
     */
    public void addRelations(Set<Relationship> relationships);

    /**
     * Remove the specified relationship.  No exceptions should be thrown if the relationship doesn't exist.
     *
     * @param sourceId the identifier of the source object of the relationship
     * @param targetId the identifier of the target object of the relationship
     * @param rel the relationship type
     */
    public void removeRelation(String sourceId, String targetId, Relationship.RelType rel);

    /**
     * Removes a set of relationships.  The same rules for removing a single relationship apply for removing a {@code Set}
     * of relationships.  Implementations may optimize the removal of a {@code Set} of relationships versus removing
     * a single relationship at time.
     *
     * @param relationships the {@code Set} of relationships to remove
     */
    public void removeRelations(Set<Relationship> relationships);

    /**
     * Remove all relationships that include the specified identifier.  No exceptions should be thrown if relationships
     * for the specified identifier don't exist.
     *
     * @param id the identifier of the source or target objects to remove
     * @throws IllegalArgumentException if any of the arguments are {@code null}
     */
    public void removeAll(String id);

    /**
     * Get all the relationships that exist for the supplied identifier, ignoring whether or not the
     * identifier is used as the source of a relationship or the target of the relationship.  The returned
     * {@code Map} is keyed by the relationship type.
     *
     * @param id the identifier for an object that is either the source or target of a relationship
     * @return a {@code Map} keyed by relationship types that pertain to the identified object.  Must not be
     *         {@code null}, but may be empty.
     * @throws IllegalArgumentException if any of the arguments are {@code null}
     */
    public Map<Relationship.RelType, Set<Relationship>> getRelations(String id);

    /**
     * Get the relationships of type {@code rel} that exist for the supplied identifier, ignoring whether or not the
     * identifier is used as the source of a relationship or the target of the relationship.
     *
     * @param id the identifier for an object that is either the source or target of a a relationship
     * @param rel the relationship type
     * @return a {@code Set} of relationships that have relationship {@code rel} with {@code id}
     * @throws IllegalArgumentException if any of the arguments are {@code null}
     */
    public Set<Relationship> getRelations(String id, Relationship.RelType rel);

    /**
     * Get the relationships of type {@code rel} that exist for the supplied identifier, selecting whether or not the
     * identifier is used as the source of a relationship or the target of the relationship.
     *
     * @param id the identifier for an object that is either the source or target of a a relationship
     * @param rel the relationship type
     * @param relEnd the end of the arrow (source or target)
     * @return a {@code Set} of relationships that have relationship {@code rel} with {@code id}
     * @throws IllegalArgumentException if any of the arguments are {@code null}
     */
    public Set<Relationship> getRelations(String id, Relationship.RelType rel, RelEnd relEnd);

    /**
     * Returns an iterator over relationships managed by the DAO.
     *
     * @return the iterator
     */
    Iterator<Relationship> iterator();
}
