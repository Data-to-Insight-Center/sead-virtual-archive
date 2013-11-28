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
package org.dataconservancy.ui.services;

import java.util.Set;

import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.dao.RelationshipDAO;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Relationship;

/**
 * Provides a high-level interface for managing relationships between business objects in the DC User Interface. The
 * presence or absence of a relationship between two objects can be used for various purposes. For example, this service
 * may be a collaborator in an authorization system, which determines who in the system is authorized to do what.
 * <em>This</em> service is <em>not</em> an authorization service. It simply manages and reports on relationships. But
 * it may play an important role in an authorization system. <h3>Discussion</h3> It isn't clear to me that the methods
 * on this interface belong here. The methods for managing depositor relationships, for example, could belong on a
 * {@code CollectionService}. Finally, this interface could become large over time, though its implementation would be
 * relatively simple.
 * <p/>
 * The purpose of the interface is to centralize the management of relationships between objects. For example, this
 * interface would insure that both the obverse and inverse relationships are created when a relationship is added.
 * Right now, relationships are simple: one relationship between a source and target object. But it may be in the future
 * that relationships are more complex, or there are constraints that must be honored; implementations of this interface
 * encapsulate that complexity in a single, testable, place.
 */
public interface RelationshipService {

    /**
     * Adds a relationship indicating {@code admin} administers {@code project}. A single project may have multiple
     * administrators.
     *
     * @param project
     *            the {@code Project} to be administered
     * @param admin
     *            the {@code Person} who should be an administrator for the {@code Project}
     */
    public void addAdministratorToProject(Project project, Person admin);

    /**
     * Adds a relationship indicating {@code admin} administers {@code project}. A single project may have multiple
     * administrators.
     *
     * @param project
     *            the {@code Project} to be administered
     * @param admin
     *            the {@code Person}s who should be administrators for the {@code Project}
     */
    public void addAdministratorsToProject(Project project, Set<Person> admin);

    /**
     * Removes the relationship indicating {@code admin} administers {@code project}. A single project may have multiple
     * administrators.
     *
     * @param admin
     *            the {@code Person} who should be removed as an administrator for the {@code Project}
     * @param project
     *            the {@code Project} to currently administered
     */
    public void removeAdministratorFromProject(Person admin, Project project);

    /**
     * Removes the relationship indicating {@code admin} administers {@code project}. A single project may have multiple
     * administrators.
     *
     * @param admin
     *            the {@code Person} who should be removed as an administrator for the {@code Project}
     * @param project
     *            the {@code Project} to currently administered
     */
    public void removeAdministratorsFromProject(Project project, Set<Person> admin);

    /**
     * Obtain all of the {@code Person}s who administer {@code project}.
     *
     * @param project
     *            the {@code Project}
     * @return a {@code Set} of {@code Person}s who administer the {@code project}. May be empty but never {@code null}.
     */
    public Set<Person> getAdministratorsForProject(Project project);

    /**
     * Obtain all of the {@code Person}s who administer the {@code project} identified by {@code projectId}.
     *
     * @param projectId id of the {@code Project}
     * @return a {@code Set} of {@code Person}s who administer the {@code project}. May be empty but never {@code null}.
     */
    public Set<Person> getAdministratorsForProject(String projectId);

    /**
     * Obtain all of the {@code Project}s administered by the {@code  admin}.
     *
     * @param admin
     *            the {@code Person}
     * @return a {@code Set} of {@code Projects}s administered by the {@code admin}. May be empty but never {@code null}
     *         .
     */
    public Set<Project> getProjectsForAdministrator(Person admin);

    /**
     * Obtain the {@code Project} aggregating the {@code  collection}.
     *
     * @param collection
     *            the {@code Collection}
     * @return the {@code Project} aggregating the {@code collection}. May be empty but never {@code null}.
     */
    public Project getProjectForCollection(Collection collection) throws RelationshipConstraintException;

    /**
     * Obtain the {@code Project} aggregating the {@code  collection}.
     *
     * @param collectionId
     *            the {@code Collection}'s id
     * @return the id of the {@code Project} aggregating the {@code collection}. Return {@code null} if such project
     *          not found.
     */
    public String getProjectForCollection(String collectionId) throws RelationshipConstraintException;


    /**
     * Adds a relationship indicating {@code depositor} administers {@code collection}. A single collection may have
     * multiple depositors, and a single depositor may deposit to multiple collections.
     *
     * @param depositor
     *            the {@code Person} who can deposit to {@code collection}
     * @param collection
     *            the {@code Collection} to be deposited to
     */
    public void addDepositorToCollection(Person depositor, Collection collection);

    /**
     * Removes the relationship indicating {@code depositor} can deposit to {@code collection}. A single collection may
     * have multiple depositors, and a single depositor may deposit to multiple collections.
     *
     * @param depositor
     *            the {@code Person} who can no longer deposit to {@code collection}
     * @param collection
     *            the {@code Collection} the {@code depositor} can no longer deposit to
     */
    public void removeDepositorFromCollection(Person depositor, Collection collection);

    /**
     * Obtain all of the {@code Person}s who can deposit to {@code collection}.
     *
     * @param collection
     *            the {@code Collection}
     * @return a {@code Set} of {@code Person}s who can deposit to the {@code collection}. May be empty but never
     *         {@code null}.
     */
    public Set<Person> getDepositorsForCollection(Collection collection);

    /**
     * Obtain all of the {@code Person}s who can deposit to {@code collection} which is identified by collectionId.
     *
     * @param collectionId id of the collection in question
     * @return a {@code Set} of {@code Person}s who can deposit to the {@code collection}. May be empty but never
     *         {@code null}.
     */
    public Set<Person> getDepositorsForCollection(String collectionId);

    /**
     * Obtain all of the {@code Collection}s that {@code depositor} can deposit to.
     *
     * @param depositor
     *            the {@code Person}
     * @return a {@code Set} of {@code Collection}s that {@code depositor} can deposit to. May be empty but never
     *         {@code null}.
     */
    public Set<Collection> getCollectionsForDepositor(Person depositor);

    /**
     * Obtain all of the {@code Collection}s that {@code depositor} administers.
     *
     * @param administrator
     *            the {@code Person}
     * @return a {@code Set} of {@code Collection}s that {@code depositor} administers. May be empty but never
     *         {@code null}.
     */
    public Set<Collection> getCollectionsForAdministrator(Person administrator);

    /**
     * Adds {@code collection} to the {@code project}. A {@code Collection} may only belong to a single {@code Project},
     * so if {@code collection} already belongs do a different {@code Project}, this method will throw
     * {@link RelationshipConstraintException}.
     *
     * @param collection
     *            the collection to add
     * @param project
     *            the project that aggregates the collection
     * @throws RelationshipConstraintException
     *             if the collection is already aggregated by another project
     */
    public void addCollectionToProject(Collection collection, Project project) throws RelationshipConstraintException;

    /**
     * Removes {@code collection} from the {@code project}. A {@code Collection} may only belong to a single
     * {@code Project}.
     *
     * @param collection
     *            the collection to remove
     * @param project
     *            the project to remove the collection from
     */
    public void removeCollectionFromProject(Collection collection, Project project);

    /**
     * Obtain all of the {@code Collection}s aggregated by the {@code project}.
     *
     * @param project
     *            the project containing collections
     * @return a {@code Set} of {@code Collection}s, may be empty but never {@code null}
     */
    public Set<Collection> getCollectionsForProject(Project project);

    /**
     * Obtain all of the {@code MetadataFormat} URIs aggregated by the {@code Discipline}.
     *
     * @param disciplineId
     *            The URI id of the discipline whose metadata formats will be returned
     * @return a {@code Set} of {@code MetadataFormat} URIs, may be empty but not {@code null}
     */
    public Set<String> getMetadataFormatsForDiscipline(String disciplineId);

    /**
     * Obtain all {@code Discipline}s business identifiers that the supplied {@code metadataFormatId} belongs to.
     *
     * @param metadataFormatId
     *            the business id of the MetadataFormat
     * @return the {@code Set} of {@code Discipline} business identifiers
     */
    public Set<String> getDisciplinesForMetadataFormats(String metadataFormatId);

    /**
     * Adds a {@code AGGREGATES} relationship (and the inverse, {@code IS_AGGREGATED_BY}) between the Discipline and the
     * MetadataFormat. Disciplines and MetadataFormats have a many-to-many relationship: a MetadataFormat can be
     * aggregated by multiple Disciplines, and a Discipline may aggregate multiple MetadataFormats.
     *
     * @param discipline
     *            the Discipline
     * @param metadataFormat
     *            the MetadataFormat
     */
    public void addDisciplineToMetadataFormat(Discipline discipline, DcsMetadataFormat metadataFormat);

    /**
     * Adds a {@code AGGREGATES} relationship (and the inverse, {@code IS_AGGREGATED_BY}) between the Discipline and the
     * supplied MetadataFormats. Disciplines and MetadataFormats have a many-to-many relationship: a MetadataFormat can
     * be aggregated by multiple Disciplines, and a Discipline may aggregate multiple MetadataFormats.
     *
     * @param discipline
     *            the Discipline
     * @param metadataFormats
     *            the MetadataFormats
     */
    public void addDisciplineToMetadataFormats(Discipline discipline, Set<DcsMetadataFormat> metadataFormats);

    /**
     * Adds a {@code AGGREGATES} relationship (and the inverse, {@code IS_AGGREGATED_BY}) between the Disciplines and
     * the MetadataFormat. Disciplines and MetadataFormats have a many-to-many relationship: a MetadataFormat can be
     * aggregated by multiple Disciplines, and a Discipline may aggregate multiple MetadataFormats.
     *
     * @param metadataFormat
     *            the MetadataFormat
     * @param disciplines
     *            the Disciplines
     */
    public void addMetadataFormatToDisciplines(DcsMetadataFormat metadataFormat, Set<Discipline> disciplines);

    /**
     * Removes the {@code AGGREGATES} relationship (and the inverse, {@code IS_AGGREGATED_BY}) between the Discipline
     * and the MetadataFormat. Disciplines and MetadataFormats have a many-to-many relationship: a MetadataFormat can be
     * aggregated by multiple Disciplines, and a Discipline may aggregate multiple MetadataFormats.
     *
     * @param discipline
     *            the Discipline
     * @param metadataFormat
     *            the MetadataFormat
     */
    public void removeDisciplineFromMetadataFormat(Discipline discipline, DcsMetadataFormat metadataFormat);

    /**
     * Removes the {@code AGGREGATES} relationship (and the inverse, {@code IS_AGGREGATED_BY}) between the Disciplines
     * and the MetadataFormat. Disciplines and MetadataFormats have a many-to-many relationship: a MetadataFormat can be
     * aggregated by multiple Disciplines, and a Discipline may aggregate multiple MetadataFormats.
     *
     * @param disciplines
     *            the Disciplines
     * @param metadataFormat
     *            the MetadataFormat
     */
    public void removeDisciplinesFromMetadataFormat(Set<Discipline> disciplines, DcsMetadataFormat metadataFormat);

    /**
     * Removes the {@code AGGREGATES} relationship (and the inverse, {@code IS_AGGREGATED_BY}) between the Discipline\
     * and the MetadataFormats. Disciplines and MetadataFormats have a many-to-many relationship: a MetadataFormat can
     * be aggregated by multiple Disciplines, and a Discipline may aggregate multiple MetadataFormats.
     *
     * @param metadataFormats
     *            the MetadataFormats
     * @param discipline
     *            the Discipline
     */
    public void removeMetadataFormatsFromDiscipline(Set<DcsMetadataFormat> metadataFormats, Discipline discipline);

    /**
     * Adds a {@code IS_CONTRIBUTOR_FOR} relationship (and the inverse, {@code HAS_CONTRIBUTOR}) between the Person and
     * the Project. A single collection may have multiple depositors, and a single contributor may contribute to
     * multiple projects.
     *
     * @param contributor
     *            the Person
     * @param project
     *            the Project
     */

    /**
     * Assert the presence or absence of a relationship between two objects.
     *
     * @param id1
     *            the id of one object in the relationship
     * @param id2
     *            the id of the other object in the relationship
     * @param type
     *            the relationship type
     * @return true if the two objects are related by {@code type}, false otherwise.
     */
    public boolean isRelated(String id1, String id2, Relationship.RelType type);

    /**
     * Set the {@code RelationshipDAO} that will be used to execute queries against the persistence layer. This method
     * is here (and package-private) for testing purposes.
     *
     * @param relationshipDao
     *            the relationship DAO, must not be {@code null}.
     */
    void setRelationshipDao(RelationshipDAO relationshipDao);

    /**
     * Adds {@code collection} to the {@code project}. A {@code Collection} may only belong to a single {@code Project},
     * so if {@code collection} already belongs do a different {@code Project}, this method will throw
     * {@link RelationshipConstraintException}.
     *
     * @param dataItem
     *            the dataset to add
     * @param collection
     *            the collection that aggregates the dataset
     * @throws RelationshipConstraintException
     *             if the dataset is already aggregated by another collection
     */
    public void addDataSetToCollection(DataItem dataItem, Collection collection) throws RelationshipConstraintException;

    /**
     * Removes {@code dataset} from the {@code collection}. A {@code DataItem} may only belong to a single
     * {@code Collection}.
     *
     * @param dataItem
     *            the dataset to remove
     * @param collection
     *            the collection to remove the dataset from
     */
    public void removeDataSetFromCollection(DataItem dataItem, Collection collection);

    /**
     * Obtain the {@code Collection} aggregating the {@code  dataset}.
     *
     * @param dataItem
     *            the {@code DataItem}
     * @return the {@code Collection} aggregating the {@code DataItem}. May be empty but never {@code null}.
     */
    public Collection getCollectionForDataSet(DataItem dataItem) throws RelationshipConstraintException;

    /**
     * Add {@code dataFile} to the {@code dataSet}. A {@code DataFile} may only belong to a single {@code DataItem}, so
     * if {@code DataFile} already belongs to a different {@code DataItem}, this method will throw
     * {@link RelationshipConstraintException}.
     *
     * @param dataFile
     * @param dataItem
     */
    public void addDataFileToDataSet(DataFile dataFile, DataItem dataItem) throws RelationshipConstraintException;

    /**
     * Remove {@code dataFile} from the {@code dataSet}
     *
     * @param dataFile
     * @param dataItem
     */
    public void removeDataFileFromDataSet(DataFile dataFile, DataItem dataItem);

    /**
     * Update the {@code DataFile} to {@code DataItem} relationships for a {@code DataItem}
     *
     * @param dataItem
     */
    public void updateDataFileRelationshipForDataSet(DataItem dataItem) throws RelationshipConstraintException;

    /**
     * Obtain the {@code DataItem} aggregating the {@code dataFile}. This will only return a non-empty {@code DataItem}
     * if the {@code DataFile} is contained in the most current version of the {@code DataItem} May be empty but never
     * {@code null}
     *
     * @param dataFile
     *            The data file object
     * @return the {@code DataItem} currently containing the {@code DataFile}.
     */
    public DataItem getDataSetForDataFile(DataFile dataFile) throws RelationshipConstraintException;

    /**
     * Obtain the {@code DataItem} aggregating the {@code dataFile}. This will only return a non-empty {@code DataItem}
     * if the {@code DataFile} is contained in the most current version of the {@code DataItem} May be empty but never
     * {@code null}
     *
     * @param dataFileId
     *            The business id for the data file object to retrieve the data set for.
     * @return the {@code DataItem} currently containing the {@code DataFile}.
     */
    public DataItem getDataSetForDataFileId(String dataFileId) throws RelationshipConstraintException;

    public Set<String> getDataSetIdsForCollectionId(String collectionId) throws RelationshipException;
    
    /**
     * Returns subcollections of a collection.
     * 
     * @param collectionId
     * @return set of collections whose parent is the given collection.
     * @throws RelationshipException
     */
    public Set<String> getSubCollectionIdsForCollectionId(String collectionId) throws RelationshipException;
    
    /**
     * Return parent collections of a collection.
     * 
     * @param collectionId
     * @return set of collections whose parent is the given collection
     * @throws RelationshipException
     */
    public Set<String> getSuperCollectionIdsForCollectionId(String collectionId) throws RelationshipException;

    /**
     * Adds a {@code IS_SUBCOLLECTION_OF} relationship (and the inverse, {@code HAS_SUBCOLLECTION}) between two collections.
     * 
     * @param childId
     * @param parentId
     * @throws RelationshipException
     */
    public void addSubCollectionToCollection(String childId, String parentId) throws RelationshipException;
    
    /**
     * Removes a {@code IS_SUBCOLLECTION_OF} relationship (and the inverse, {@code HAS_SUBCOLLECTION}) between two collections.
     * 
     * @param childId
     * @param parentId
     * @throws RelationshipException
     */
    public void removeSubCollectionFromCollection(String childId, String parentId) throws RelationshipException;

    /**
     * Adds a {@code HAS_METADATA_FILE} relationship between a Business Object and a Metadata File
     *
     * @param metadataFileId
     * @param businessObjectId
     */
    public void addMetadataFileToBusinessObject(String metadataFileId, String businessObjectId) throws RelationshipConstraintException;

    /**
     * Removes a  {@code HAS_METADATA_FILE} relationship between a Business Object and a Metadata File
     * @param metadataFileId
     * @param businessObjectId
     * @throws RelationshipException
     */
    public void removeMetadataFileFromBusinessObject(String metadataFileId, String businessObjectId);

    /**
     * Return  IDs for all MetadataFiles for a BusinessObject.  May be empty but never {@code null}
     *
     * @param businessObjectId
     * @return
     * @throws RelationshipException
     */
    public Set<String> getMetadataFileIdsForBusinessObjectId(String businessObjectId) throws RelationshipException;
}
