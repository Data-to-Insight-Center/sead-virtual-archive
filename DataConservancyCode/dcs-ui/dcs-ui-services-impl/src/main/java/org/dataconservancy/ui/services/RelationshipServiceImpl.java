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

import static org.dataconservancy.ui.model.ArchiveDepositInfo.Status.DEPOSITED;
import static org.dataconservancy.ui.model.Relationship.RelType.ACCEPTS_DEPOSIT;
import static org.dataconservancy.ui.model.Relationship.RelType.AGGREGATES;
import static org.dataconservancy.ui.model.Relationship.RelType.HAS_METADATA_FILE;
import static org.dataconservancy.ui.model.Relationship.RelType.HAS_SUBCOLLECTION;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_ADMINISTERED_BY;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_ADMINISTRATOR_FOR;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_AGGREGATED_BY;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_DEPOSITOR_FOR;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_SUBCOLLECTION_OF;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.dao.PersonDAO;
import org.dataconservancy.ui.dao.ProjectAwardDAO;
import org.dataconservancy.ui.dao.ProjectDAO;
import org.dataconservancy.ui.dao.RelationshipDAO;
import org.dataconservancy.ui.dao.RelationshipDAO.RelEnd;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Relationship;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link RelationshipService}.
 */
public class RelationshipServiceImpl implements RelationshipService {
    
    /**
     * Error message if the Archive Service can't retrieve a collection. Fields: collection id, error message
     */
    private static final String ERR_RETRIEVE_COLLECTION = "Error retrieving collection %s: %s";
    
    /**
     * Error message if the Archive Service can't retrieve a data set. Fields: data set id, error message
     */
    private static final String ERR_RETRIEVE_DATASET = "Error retrieving data set %s: %s";
    
    /**
     * Error message if a Collection already belongs to an existing Project when trying to associate that Collection to
     * another Project. Fields: collection id, new project id, existing project id
     */
    private static final String ERR_CONSTRAINT_COLLECTION = "While adding Collection %s to project %s: "
            + "Collection already belongs to project %s.";
    
    /**
     * Error message if a DataItem already belongs to an existing Collection when trying to associate that DataItem to
     * another Collection. Fields: dataset id, new collection id, existing collection id
     */
    private static final String ERR_CONSTRAINT_DATASET = "While adding DataItem %s to Collection %s: "
            + "DataItem already belongs to Collection %s.";
    
    /**
     * Error message if a DataFilealready belongs to an existing DateItem when trying to associate that DataFile to
     * another DataItem. Fields: datafile id, new dataitem id, dataitem id
     */
    private static final String ERR_CONSTRAINT_DATAFILE = "While adding DataFile %s to DataItem %s: "
            + "DataFile already belongs to DataItem %s.";

    /**
     * Error message if a metadate file already belongs to an existing business object when trying to associate that matedata file to
     * another business object. Fields: metadatafile  id, new business object id, existing business object id
     */
    private static final String ERR_CONSTRAINT_METADATAFILE = "While adding MetadataFile %s to BusinessObject %s: "
            + "MetadataFile already belongs to BusinessObject %s.";


    /**
     * Error message if a Collection belongs to more than one Project when trying to get the Project aggregating the
     * Collection. Field: collection id
     */
    private static final String ERR_CONSTRAINT_PROJECT_COLLECTION = "Collection %s belongs to more than one Project. "
            + "This should never happen.";
    
    /**
     * Error message if a DataItem belongs to more than one Collection when trying to get the Collection aggregating the
     * DataItem. Field: dataSet id
     */
    private static final String ERR_CONSTRAINT_PROJECT_DATASET = "DataItem %s belongs to more than one Collection. "
            + "This should never happen.";
    
    /**
     * Error message if a DataFile belongs to more than one DataItem when trying to get the DataItem aggregating the
     * DataFile. Field: dataSet id
     */
    private static final String ERR_CONSTRAINT_DATASET_DATAFILE = "DataFile %s belongs to more than one DataItem. "
            + "This should never happen.";
    
    /**
     * This is the locking object used when adding or removing Sets of objects from the relationship table. The
     * {@link IdBasedMutexFactory} can create locks based on object ids, but when operating on a Set of objects, it is
     * unreasonably expensive to obtain a lock over each ID pair in the set. In these cases we just use this lock.
     */
    private static final Object DAO_BULK_OPERATION_LOCK = new Object();
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private RelationshipDAO relDao;
    
    private final PersonDAO personDao;
    
    private final ProjectDAO projectDao;
    
    private final ProjectAwardDAO projectAwardDao;
    
    private final ArchiveService archiveService;
    
    /**
     * Constructs a new RelationshipService.
     * 
     * @param relDao
     *            the RelationshipDAO, must not be null.
     * @param personDao
     *            the PersonDAO, must not be null.
     * @param projectDao
     *            the ProjectDAO, must not be null.
     * @param archiveService
     *            the ArchiveService, must not be null.
     * @throws IllegalArgumentException
     *             if any parameters are null.
     */
    public RelationshipServiceImpl(RelationshipDAO relDao, PersonDAO personDao, ProjectDAO projectDao,
            ProjectAwardDAO projectAwardDao, ArchiveService archiveService) {
        if (relDao == null) {
            throw new IllegalArgumentException("RelationshipDAO must not be null.");
        }
        
        if (personDao == null) {
            throw new IllegalArgumentException("PersonDAO must not be null.");
        }
        
        if (projectDao == null) {
            throw new IllegalArgumentException("ProjectDAO must not be null.");
        }
        
        if (projectAwardDao == null) {
            throw new IllegalArgumentException("ProjectAwardDAO must not be null.");
        }
        
        if (archiveService == null) {
            throw new IllegalArgumentException("Archive Service must not be null.");
        }
        
        this.relDao = relDao;
        this.personDao = personDao;
        this.projectDao = projectDao;
        this.projectAwardDao = projectAwardDao;
        this.archiveService = archiveService;
    }
    
    public void setRelationshipDao(RelationshipDAO relationshipDao) {
        if (relationshipDao == null) {
            throw new IllegalArgumentException("RelationshipDAO must not be null.");
        }
        
        this.relDao = relationshipDao;
    }
    
    @Override
    public void addAdministratorToProject(Project project, Person admin) {
        final String lock = IdBasedMutexFactory.create(project, admin);
        synchronized (lock) {
            relDao.addRelation(admin.getId(), project.getId(), IS_ADMINISTRATOR_FOR);
            relDao.addRelation(project.getId(), admin.getId(), IS_ADMINISTERED_BY);
        }
    }
    
    @Override
    public synchronized void addAdministratorsToProject(Project project, Set<Person> admin) {
        final Set<Relationship> relationships = new HashSet<Relationship>(admin.size());
        final String projectId = project.getId();
        for (Person person : admin) {
            relationships.add(new Relationship(person.getId(), projectId, IS_ADMINISTRATOR_FOR));
            relationships.add(new Relationship(projectId, person.getId(), IS_ADMINISTERED_BY));
        }
        relDao.addRelations(relationships);
    }
    
    @Override
    public void removeAdministratorFromProject(Person admin, Project project) {
        final String lock = IdBasedMutexFactory.create(project, admin);
        synchronized (lock) {
            relDao.removeRelation(admin.getId(), project.getId(), IS_ADMINISTRATOR_FOR);
            relDao.removeRelation(project.getId(), admin.getId(), IS_ADMINISTERED_BY);
        }
    }
    
    @Override
    public synchronized void removeAdministratorsFromProject(Project project, Set<Person> admin) {
        final Set<Relationship> relationships = new HashSet<Relationship>(admin.size());
        final String projectId = project.getId();
        for (Person person : admin) {
            relationships.add(new Relationship(person.getId(), projectId, IS_ADMINISTRATOR_FOR));
            relationships.add(new Relationship(projectId, person.getId(), IS_ADMINISTERED_BY));
        }
        relDao.removeRelations(relationships);
    }
    
    @Override
    public Set<Person> getAdministratorsForProject(Project project) {
        return getAdministratorsForProject(project.getId());
    }


    @Override
    public Set<Person> getAdministratorsForProject(String projectId) {
        final Set<Relationship> rels;
        synchronized (IdBasedMutexFactory.create(projectId)) {
            rels = relDao.getRelations(projectId, IS_ADMINISTERED_BY, RelationshipDAO.RelEnd.SOURCE);
        }
        final Set<Person> admins = new HashSet<Person>();
        for (Relationship rel : rels) {
            Person p = personDao.selectPersonById(rel.getTarget());
            if (p != null) {
                admins.add(p);
            }
        }
        return admins;
    }
    
    @Override
    public Set<Project> getProjectsForAdministrator(Person admin) {
        final Set<Relationship> rels;
        synchronized (IdBasedMutexFactory.create(admin)) {
            rels = relDao.getRelations(admin.getId(), IS_ADMINISTRATOR_FOR, RelationshipDAO.RelEnd.SOURCE);
        }
        final Set<Project> projects = new HashSet<Project>();
        
        for (Relationship rel : rels) {
            Project p = projectDao.selectProject(rel.getTarget());
            if (p != null) {
                List<String> numbers = projectAwardDao.getNumbers(p.getId());
                p.setNumbers(numbers);
                projects.add(p);
            }
        }
        return projects;
    }
    
    @Override
    public Project getProjectForCollection(Collection collection) throws RelationshipConstraintException {
        final Set<Relationship> rels;
        synchronized (IdBasedMutexFactory.create(collection)) {
            rels = relDao.getRelations(collection.getId(), IS_AGGREGATED_BY, RelationshipDAO.RelEnd.SOURCE);
        }
        // Make sure the collection belongs to only one project
        if (rels.size() > 1) {
            final String msg = String.format(ERR_CONSTRAINT_PROJECT_COLLECTION, collection.getId());
            throw new RelationshipConstraintException(msg);
        }
        
        Project project = new Project();
        for (Relationship rel : rels) {
            Project p = projectDao.selectProject(rel.getTarget());
            if (p != null) {
                project = p;
            }
        }
        return project;
    }

    @Override
    public String getProjectForCollection(String collectionId) throws RelationshipConstraintException {
        final Set<Relationship> rels;
        synchronized (IdBasedMutexFactory.create(collectionId)) {
            rels = relDao.getRelations(collectionId, IS_AGGREGATED_BY, RelationshipDAO.RelEnd.SOURCE);
        }
        // Make sure the collection belongs to only one project
        if (rels.size() > 1) {
            final String msg = String.format(ERR_CONSTRAINT_PROJECT_COLLECTION, collectionId);
            throw new RelationshipConstraintException(msg);
        }

        for (Relationship rel : rels) {
            return rel.getTarget();
        }
        return null;
    }

    @Override
    public void addDepositorToCollection(Person depositor, Collection collection) {
        synchronized (IdBasedMutexFactory.create(collection, depositor)) {
            relDao.addRelation(depositor.getId(), collection.getId(), IS_DEPOSITOR_FOR);
            relDao.addRelation(collection.getId(), depositor.getId(), ACCEPTS_DEPOSIT);
        }
    }
    
    @Override
    public void removeDepositorFromCollection(Person depositor, Collection collection) {
        synchronized (IdBasedMutexFactory.create(collection, depositor)) {
            relDao.removeRelation(depositor.getId(), collection.getId(), IS_DEPOSITOR_FOR);
            relDao.removeRelation(collection.getId(), depositor.getId(), ACCEPTS_DEPOSIT);
        }
    }
    
    @Override
    public Set<Person> getDepositorsForCollection(Collection collection) {
        final Set<Relationship> rels;
        synchronized (IdBasedMutexFactory.create(collection)) {
            rels = relDao.getRelations(collection.getId(), ACCEPTS_DEPOSIT, RelationshipDAO.RelEnd.SOURCE);
        }
        final Set<Person> depositors = new HashSet<Person>();
        for (Relationship rel : rels) {
            Person p = personDao.selectPersonById(rel.getTarget());
            if (p != null) {
                depositors.add(p);
            }
        }
        return depositors;
    }

    @Override
    public Set<Person> getDepositorsForCollection(String collectionId) {
        final Set<Relationship> rels;
        synchronized (IdBasedMutexFactory.create(collectionId)) {
            rels = relDao.getRelations(collectionId, ACCEPTS_DEPOSIT, RelationshipDAO.RelEnd.SOURCE);
        }
        final Set<Person> depositors = new HashSet<Person>();
        for (Relationship rel : rels) {
            Person p = personDao.selectPersonById(rel.getTarget());
            if (p != null) {
                depositors.add(p);
            }
        }
        return depositors;
    }

    @Override
    public Set<Collection> getCollectionsForDepositor(Person depositor) {
        final Set<Relationship> rels;
        synchronized (IdBasedMutexFactory.create(depositor)) {
            rels = relDao.getRelations(depositor.getId(), IS_DEPOSITOR_FOR, RelationshipDAO.RelEnd.SOURCE);
        }
        
        final Set<Collection> collections = new HashSet<Collection>();
        for (Relationship rel : rels) {
            final String collectionId = rel.getTarget();
            
            try {
                List<ArchiveDepositInfo> info = archiveService.listDepositInfo(collectionId, DEPOSITED);
                if (info != null && info.size() > 0) {
                    ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(info.get(0)
                            .getDepositId());
                    Iterator<Collection> resultIter = results.getResults().iterator();
                    Collection collection = null;
                    if (resultIter.hasNext()) {
                        collection = resultIter.next();
                    }
                    
                    if (collection != null) {
                        collections.add(collection);
                    }
                }
                
            }
            catch (ArchiveServiceException e) {
                final String msg = String.format(ERR_RETRIEVE_COLLECTION, collectionId, e.getMessage());
                log.warn(msg, e);
            }
        }
        return collections;
    }
    
    @Override
    public Set<Collection> getCollectionsForAdministrator(Person administrator) {
        final Set<Collection> adminCollections = new HashSet<Collection>();
        synchronized (IdBasedMutexFactory.create(administrator)) {
            for (Project project : getProjectsForAdministrator(administrator)) {
                adminCollections.addAll(getCollectionsForProject(project));
            }
        }
        return adminCollections;
    }
    
    @Override
    public void addCollectionToProject(Collection collection, Project project) throws RelationshipConstraintException {
        synchronized (IdBasedMutexFactory.create(collection, project)) {
            final Set<Relationship> existingRelations = relDao.getRelations(collection.getId(), IS_AGGREGATED_BY,
                    RelationshipDAO.RelEnd.SOURCE);
            
            // Make sure the collection doesn't already belong to another project
            if (!existingRelations.isEmpty()) {
                Relationship rel = existingRelations.iterator().next();
                if (!rel.getTarget().equals(project.getId())) {
                    final String msg = String.format(ERR_CONSTRAINT_COLLECTION, collection.getId(), project.getId(),
                            rel.getTarget());
                    throw new RelationshipConstraintException(msg);
                }
                
            }
            
            relDao.addRelation(collection.getId(), project.getId(), IS_AGGREGATED_BY);
            relDao.addRelation(project.getId(), collection.getId(), AGGREGATES);
        }
    }
    
    @Override
    public void removeCollectionFromProject(Collection collection, Project project) {
        synchronized (IdBasedMutexFactory.create(collection, project)) {
            relDao.removeRelation(collection.getId(), project.getId(), IS_AGGREGATED_BY);
            relDao.removeRelation(project.getId(), collection.getId(), AGGREGATES);
        }
    }
    
    @Override
    public Set<Collection> getCollectionsForProject(Project project) {
        final Set<Relationship> rels;
        synchronized (IdBasedMutexFactory.create(project)) {
            rels = relDao.getRelations(project.getId(), AGGREGATES, RelationshipDAO.RelEnd.SOURCE);
        }
        final Set<Collection> collections = new HashSet<Collection>();
        for (Relationship rel : rels) {
            final String targetId = rel.getTarget();
            try {
                List<ArchiveDepositInfo> archiveDepositInfo = archiveService.listDepositInfo(rel.getTarget(),
                        ArchiveDepositInfo.Status.DEPOSITED);
                
                Collection c = null;
                
                if (archiveDepositInfo != null && !archiveDepositInfo.isEmpty()) {
                    ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(archiveDepositInfo.get(
                            0).getDepositId());
                    Iterator<Collection> resultIter = results.getResults().iterator();
                    if (resultIter.hasNext()) {
                        c = resultIter.next();
                    }
                }
                
                if (c != null) {
                    collections.add(c);
                }
            }
            catch (ArchiveServiceException e) {
                final String msg = String.format(ERR_RETRIEVE_COLLECTION, targetId, e.getMessage());
                log.warn(msg, e);
            }
        }
        
        return collections;
    }
    
    @Override
    public Set<String> getMetadataFormatsForDiscipline(String disciplineId) {
        final Set<String> metadataFormats = new HashSet<String>();
        final Set<Relationship> rels;
        synchronized (IdBasedMutexFactory.create(disciplineId)) {
            rels = relDao.getRelations(disciplineId, AGGREGATES);
        }
        for (Relationship rel : rels) {
            metadataFormats.add(rel.getTarget());
        }
        return metadataFormats;
    }
    
    public Set<String> getDisciplinesForMetadataFormats(String metadataFormatId) {
        final Set<String> disciplineIds = new HashSet<String>();
        final Set<Relationship> relationships;
        synchronized (IdBasedMutexFactory.create(metadataFormatId)) {
            relationships = relDao.getRelations(metadataFormatId, IS_AGGREGATED_BY, RelationshipDAO.RelEnd.SOURCE);
        }
        for (Relationship r : relationships) {
            disciplineIds.add(r.getTarget());
        }
        return disciplineIds;
    }
    
    public void addDisciplineToMetadataFormat(Discipline discipline, DcsMetadataFormat metadataFormat) {
        synchronized (IdBasedMutexFactory.create(discipline, metadataFormat)) {
            relDao.addRelation(discipline.getId(), metadataFormat.getId(), AGGREGATES);
            relDao.addRelation(metadataFormat.getId(), discipline.getId(), IS_AGGREGATED_BY);
        }
    }
    
    public void addDisciplineToMetadataFormats(Discipline discipline, Set<DcsMetadataFormat> metadataFormats) {
        final Set<Relationship> rels = new HashSet<Relationship>();
        
        for (DcsMetadataFormat mdf : metadataFormats) {
            rels.add(new Relationship(mdf.getId(), discipline.getId(), IS_AGGREGATED_BY));
            rels.add(new Relationship(discipline.getId(), mdf.getId(), AGGREGATES));
        }
        
        synchronized (DAO_BULK_OPERATION_LOCK) {
            relDao.addRelations(rels);
        }
    }
    
    public void addMetadataFormatToDisciplines(DcsMetadataFormat metadataFormat, Set<Discipline> disciplines) {
        final Set<Relationship> rels = new HashSet<Relationship>();
        
        for (Discipline discipline : disciplines) {
            rels.add(new Relationship(metadataFormat.getId(), discipline.getId(), IS_AGGREGATED_BY));
            rels.add(new Relationship(discipline.getId(), metadataFormat.getId(), AGGREGATES));
        }
        
        synchronized (DAO_BULK_OPERATION_LOCK) {
            relDao.addRelations(rels);
        }
    }
    
    public void removeDisciplineFromMetadataFormat(Discipline discipline, DcsMetadataFormat metadataFormat) {
        synchronized (IdBasedMutexFactory.create(discipline, metadataFormat)) {
            relDao.removeRelation(discipline.getId(), metadataFormat.getId(), AGGREGATES);
            relDao.removeRelation(metadataFormat.getId(), discipline.getId(), IS_AGGREGATED_BY);
        }
    }
    
    public void removeDisciplinesFromMetadataFormat(Set<Discipline> disciplines, DcsMetadataFormat metadataFormat) {
        final Set<Relationship> rels = new HashSet<Relationship>();
        
        for (Discipline discipline : disciplines) {
            rels.add(new Relationship(discipline.getId(), metadataFormat.getId(), AGGREGATES));
            rels.add(new Relationship(metadataFormat.getId(), discipline.getId(), IS_AGGREGATED_BY));
        }
        
        synchronized (DAO_BULK_OPERATION_LOCK) {
            relDao.removeRelations(rels);
        }
    }
    
    public void removeMetadataFormatsFromDiscipline(Set<DcsMetadataFormat> metadataFormats, Discipline discipline) {
        final Set<Relationship> rels = new HashSet<Relationship>();
        
        for (DcsMetadataFormat mdf : metadataFormats) {
            rels.add(new Relationship(mdf.getId(), discipline.getId(), IS_AGGREGATED_BY));
            rels.add(new Relationship(discipline.getId(), mdf.getId(), AGGREGATES));
        }
        
        synchronized (DAO_BULK_OPERATION_LOCK) {
            relDao.removeRelations(rels);
        }
    }
    
    @Override
    public void addDataSetToCollection(DataItem dataItem, Collection collection) throws RelationshipConstraintException {
        synchronized (IdBasedMutexFactory.create(dataItem, collection)) {
            final Set<Relationship> existingRelations = relDao.getRelations(dataItem.getId(), IS_AGGREGATED_BY,
                    RelationshipDAO.RelEnd.SOURCE);
            
            // Make sure the DataItem doesn't already belong to another Collection
            if (!existingRelations.isEmpty()) {
                Relationship rel = existingRelations.iterator().next();
                if (!rel.getTarget().equals(collection.getId())) {
                    final String msg = String.format(ERR_CONSTRAINT_DATASET, dataItem.getId(), collection.getId(),
                            rel.getTarget());
                    throw new RelationshipConstraintException(msg);
                }
                
            } else {         
                relDao.addRelation(dataItem.getId(), collection.getId(), IS_AGGREGATED_BY);
                relDao.addRelation(collection.getId(), dataItem.getId(), AGGREGATES);
            }
        }
    }
    
    @Override
    public void removeDataSetFromCollection(DataItem dataItem, Collection collection) {
        synchronized (IdBasedMutexFactory.create(dataItem, collection)) {
            relDao.removeRelation(dataItem.getId(), collection.getId(), IS_AGGREGATED_BY);
            relDao.removeRelation(collection.getId(), dataItem.getId(), AGGREGATES);
        }
    }
    
    @Override
    public Collection getCollectionForDataSet(DataItem dataItem) throws RelationshipConstraintException {
        final Set<Relationship> rels;
        synchronized (IdBasedMutexFactory.create(dataItem)) {
            rels = relDao.getRelations(dataItem.getId(), IS_AGGREGATED_BY, RelationshipDAO.RelEnd.SOURCE);
        }
        // Make sure the collection belongs to only one project
        if (rels.size() > 1) {
            final String msg = String.format(ERR_CONSTRAINT_PROJECT_DATASET, dataItem.getId());
            throw new RelationshipConstraintException(msg);
        }
        
        Collection collection = new Collection();
        for (Relationship rel : rels) {
            Collection c = null;
            try {
                List<ArchiveDepositInfo> archiveDepositInfo = archiveService.listDepositInfo(rel.getTarget(),
                        ArchiveDepositInfo.Status.DEPOSITED);
                if (archiveDepositInfo != null && !archiveDepositInfo.isEmpty()) {
                    ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(archiveDepositInfo.get(
                            0).getDepositId());
                    Iterator<Collection> resultIter = results.getResults().iterator();
                    if (resultIter.hasNext()) {
                        c = resultIter.next();
                    }
                }
            }
            catch (ArchiveServiceException e) {
                final String msg = String.format(ERR_RETRIEVE_COLLECTION, rel.getTarget(), e.getMessage());
                log.warn(msg, e);
            }
            if (c != null) {
                collection = c;
            }
        }
        return collection;
    }
    
    @Override
    public void addDataFileToDataSet(DataFile dataFile, DataItem dataItem) throws RelationshipConstraintException {
        synchronized (IdBasedMutexFactory.create(dataFile, dataItem)) {
            final Set<Relationship> existingRelations = relDao.getRelations(dataFile.getId(), IS_AGGREGATED_BY);
            
            // Make sure the DataFile doesn't already belong to another DataItem
            if (!existingRelations.isEmpty()) {
                Relationship rel = existingRelations.iterator().next();
                if (!rel.getTarget().equals(dataItem.getId())) {
                    final String msg = String.format(ERR_CONSTRAINT_DATAFILE, dataItem.getId(), dataItem.getId(),
                            rel.getTarget());
                    throw new RelationshipConstraintException(msg);
                }
            }
            relDao.addRelation(dataFile.getId(), dataItem.getId(), IS_AGGREGATED_BY);
            relDao.addRelation(dataItem.getId(), dataFile.getId(), AGGREGATES);
        }
    }
    
    @Override
    public void removeDataFileFromDataSet(DataFile dataFile, DataItem dataItem) {
        synchronized (IdBasedMutexFactory.create(dataFile, dataItem)) {
            relDao.removeRelation(dataFile.getId(), dataItem.getId(), IS_AGGREGATED_BY);
            relDao.removeRelation(dataItem.getId(), dataFile.getId(), AGGREGATES);
        }
    }
    
    @Override
    public void updateDataFileRelationshipForDataSet(DataItem dataItem) throws RelationshipConstraintException {
        final Set<Relationship> existingAggreatesRels;
        final Set<Relationship> existingIsAggreatedByRels;
        synchronized (IdBasedMutexFactory.create(dataItem)) {
            existingAggreatesRels = relDao.getRelations(dataItem.getId(), AGGREGATES, RelationshipDAO.RelEnd.SOURCE);
            existingIsAggreatedByRels = relDao.getRelations(dataItem.getId(), IS_AGGREGATED_BY,
                    RelationshipDAO.RelEnd.TARGET);
            
            // Added all relationship anew
            for (DataFile file : dataItem.getFiles()) {
                if (file instanceof DataFile) {
                    addDataFileToDataSet((DataFile) file, dataItem);
                }
            }
            // removing existing relationship
            if (existingAggreatesRels != null) {
                relDao.removeRelations(existingAggreatesRels);
            }
            if (existingIsAggreatedByRels != null) {
                relDao.removeRelations(existingIsAggreatedByRels);
            }
        }
    }
    
    private DataItem getDataSetWithFileId(String id) throws RelationshipConstraintException {
        final Set<Relationship> rels;
        
        rels = relDao.getRelations(id, IS_AGGREGATED_BY, RelationshipDAO.RelEnd.SOURCE);
        
        // Make sure the data file belongs to only one data set
        if (rels.size() > 1) {
            final String msg = String.format(ERR_CONSTRAINT_DATASET_DATAFILE, id);
            throw new RelationshipConstraintException(msg);
        }
        DataItem dataItem = new DataItem();
        for (Relationship rel : rels) {
            DataItem ds = null;
            try {
                List<ArchiveDepositInfo> archiveDepositInfo = archiveService.listDepositInfo(rel.getTarget(),
                        ArchiveDepositInfo.Status.DEPOSITED);
                if (archiveDepositInfo != null && !archiveDepositInfo.isEmpty()) {
                    ArchiveSearchResult<DataItem> results = archiveService.retrieveDataSet(archiveDepositInfo.get(0)
                            .getDepositId());
                    Iterator<DataItem> resultIter = results.getResults().iterator();
                    if (resultIter.hasNext()) {
                        ds = resultIter.next();
                    }
                }
            }
            catch (ArchiveServiceException e) {
                final String msg = String.format(ERR_RETRIEVE_DATASET, rel.getTarget(), e.getMessage());
                log.warn(msg, e);
            }
            if (ds != null) {
                dataItem = ds;
            }
        }
        return dataItem;
    }
    
    @Override
    public DataItem getDataSetForDataFileId(String id) throws RelationshipConstraintException {
        DataItem dataItem = new DataItem();
        synchronized (IdBasedMutexFactory.create(id)) {
            dataItem = getDataSetWithFileId(id);
        }
        
        return dataItem;
    }
    
    @Override
    public Set<String> getDataSetIdsForCollectionId(String collectionId) throws RelationshipException {
        final Set<Relationship> rels;
        synchronized (IdBasedMutexFactory.create(collectionId)) {
            rels = relDao.getRelations(collectionId, AGGREGATES, RelationshipDAO.RelEnd.SOURCE);
        }
        final Set<String> datasetIds = new HashSet<String>();
        for (Relationship rel : rels) {
            String dataSetId = rel.getTarget();
            if (dataSetId != null) {
                datasetIds.add(dataSetId);
            }
        }
        return datasetIds;
    }

    @Override
    public Set<String> getSubCollectionIdsForCollectionId(String collectionId) throws RelationshipException {
        final Set<Relationship> rels;

        synchronized (IdBasedMutexFactory.create(collectionId)) {
            rels = relDao.getRelations(collectionId, HAS_SUBCOLLECTION, RelationshipDAO.RelEnd.SOURCE);
        }
        
        final Set<String> ids = new HashSet<String>();
        
        for (Relationship rel : rels) {
            String id = rel.getTarget();
            
            if (id != null) {
                ids.add(id);
            }
        }
        
        return ids;
    }
    
    @Override
    public Set<String> getSuperCollectionIdsForCollectionId(String collectionId) throws RelationshipException {
        final Set<Relationship> rels;

        synchronized (IdBasedMutexFactory.create(collectionId)) {
            rels = relDao.getRelations(collectionId, IS_SUBCOLLECTION_OF, RelEnd.SOURCE);
        }
        
        final Set<String> ids = new HashSet<String>();
        
        for (Relationship rel : rels) {
            String id = rel.getTarget();
            
            if (id != null) {
                ids.add(id);
            }
        }
        
        return ids;
    }

    @Override
    public void addSubCollectionToCollection(String childId, String parentId) throws RelationshipException {
        synchronized (IdBasedMutexFactory.create(childId, parentId)) {
            relDao.addRelation(childId, parentId, IS_SUBCOLLECTION_OF);
            relDao.addRelation(parentId, childId, HAS_SUBCOLLECTION);
        }
    }

    @Override
    public DataItem getDataSetForDataFile(DataFile dataFile) throws RelationshipConstraintException {
        DataItem dataItem = new DataItem();
        synchronized (IdBasedMutexFactory.create(dataFile)) {
            dataItem = getDataSetWithFileId(dataFile.getId());
        }
        
        return dataItem;
    }
    
    @Override
    public void removeSubCollectionFromCollection(String childId, String parentId) {
        synchronized (IdBasedMutexFactory.create(childId, parentId)) {
            relDao.removeRelation(childId, parentId, IS_SUBCOLLECTION_OF);
            relDao.removeRelation(parentId, childId, HAS_SUBCOLLECTION);
        }
    }
    
    @Override
    public boolean isRelated(String id1, String id2, Relationship.RelType type) {
        final Relationship wanted = new Relationship(id1, id2, type);
        return relDao.getRelations(id1, type).contains(wanted);
    }

    @Override
    public void addMetadataFileToBusinessObject(String metadataFileId, String businessObjectId) throws RelationshipConstraintException{

        synchronized (IdBasedMutexFactory.create(metadataFileId, businessObjectId)) {
            final Set<Relationship> existingRelations = relDao.getRelations(metadataFileId, HAS_METADATA_FILE, RelEnd.TARGET);

            // Make sure the MetadataFile doesn't already belong to another BusinessObject
            if (!existingRelations.isEmpty()) {
                Relationship rel = existingRelations.iterator().next();
                if (!rel.getSource().equals(businessObjectId)) {
                    final String msg = String.format(ERR_CONSTRAINT_METADATAFILE, metadataFileId, businessObjectId,
                            rel.getSource());
                    throw new RelationshipConstraintException(msg);
                }
            }

            relDao.addRelation(businessObjectId, metadataFileId, HAS_METADATA_FILE);

        }
    }

    @Override
    public void removeMetadataFileFromBusinessObject(String metadataFileId, String businessObjectId){
        synchronized (IdBasedMutexFactory.create(metadataFileId, businessObjectId)) {
            relDao.removeRelation(businessObjectId, metadataFileId, HAS_METADATA_FILE);
        }
    }

    @Override
    public Set<String> getMetadataFileIdsForBusinessObjectId(String businessObjectId)  throws RelationshipException {
        final Set<Relationship> rels;
        synchronized (IdBasedMutexFactory.create(businessObjectId)) {
            rels = relDao.getRelations(businessObjectId, HAS_METADATA_FILE, RelationshipDAO.RelEnd.SOURCE);
        }
        final Set<String> metadataFileIds = new HashSet<String>();
        for (Relationship rel : rels) {
            String metadataFileId = rel.getTarget();
            if (metadataFileId != null) {
                metadataFileIds.add(metadataFileId);
            }
        }
        return metadataFileIds;
    }
    
    private static class IdBasedMutexFactory {
        
        private static String create(String s) {
            return s.intern();
        }
        
        private static String create(Project project, Person person) {
            StringBuilder sb = new StringBuilder(project.getId());
            sb.append(person.getId());
            return sb.toString().intern();
        }
        
        private static String create(Project project, String number) {
            StringBuilder sb = new StringBuilder(project.getId());
            sb.append(number);
            return sb.toString().intern();
        }
        
        private static String create(String id1, String id2) {
            StringBuilder sb = new StringBuilder(id1);
            sb.append(id2);
            return sb.toString().intern();
        }
        
        private static String create(Collection collection, Person person) {
            StringBuilder sb = new StringBuilder(collection.getId());
            sb.append(person.getId());
            return sb.toString().intern();
        }
        
        private static String create(Collection collection, Project project) {
            StringBuilder sb = new StringBuilder(collection.getId());
            sb.append(project.getId());
            return sb.toString().intern();
        }
        
        private static String create(DataItem dataItem, Collection collection) {
            StringBuilder sb = new StringBuilder(dataItem.getId());
            sb.append(collection.getId());
            return sb.toString().intern();
        }
        
        private static String create(Project project) {
            return project.getId().intern();
        }
        
        private static String create(Person person) {
            return person.getId().intern();
        }
        
        private static String create(Collection collection) {
            return collection.getId().intern();
        }
        
        private static String create(Discipline discipline) {
            return discipline.getId().intern();
        }
        
        private static String create(DcsMetadataFormat metadataFormat) {
            return metadataFormat.getId().intern();
        }
        
        private static String create(DataItem dataItem) {
            return dataItem.getId().intern();
        }
        
        private static String create(Discipline discipline, DcsMetadataFormat metadataFormat) {
            StringBuilder sb = new StringBuilder(discipline.getId());
            sb.append(metadataFormat.getId());
            return sb.toString().intern();
        }
        
        private static String create(DataFile dataFile) {
            return dataFile.getId().intern();
        }
        
        private static String create(DataFile dataFile, DataItem dataItem) {
            StringBuilder sb = new StringBuilder(dataFile.getId());
            sb.append(dataItem.getId());
            return sb.toString().intern();
        }
    }
}
