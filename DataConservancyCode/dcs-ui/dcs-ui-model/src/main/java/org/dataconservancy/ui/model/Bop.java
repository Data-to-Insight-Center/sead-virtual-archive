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
package org.dataconservancy.ui.model;

import java.util.HashSet;
import java.util.Set;

import org.dataconservancy.model.dcs.support.Assertion;
import org.dataconservancy.storage.dropbox.model.DropboxModel;

/**
 * A {@code Bop} is a business object package that could contain {@link org.dataconservancy.ui.model.Project} objects,
 * {@link org.dataconservancy.ui.model.Collection} objects, {@link org.dataconservancy.ui.model.DataItem} objects,
 * and/or {@link org.dataconservancy.ui.model.Person} objects.
 */
public class Bop {

    private Set<Project> projects = new HashSet<Project>(1);
    private Set<Collection> collections = new HashSet<Collection>(1);
    private Set<Person> persons = new HashSet<Person>(1);
    private Set<DataItem> dataItems = new HashSet<DataItem>();
    private Set<MetadataFile> metadataFiles = new HashSet<MetadataFile>();
    private Set<DropboxModel> dropboxModels = new HashSet<DropboxModel>();

    public Set<Project> getProjects() {
        final Set<Project> projects = new HashSet<Project>(this.projects.size());
        for (Project du : this.projects) {
            projects.add(new Project(du));
        }
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        Assertion.notNull(projects);
        this.projects = new HashSet<Project>(projects.size());
        for ( Project d : projects ) {
            Assertion.notNull(d);
            this.projects.add(d);
        }
    }

    public void addProject(Project... project) {
        Assertion.notNull(project);
        for (Project du : project) {
            Assertion.notNull(du);
            this.projects.add(new Project(du));
        }
    }
    
    public Set<Collection> getCollections() {
        final Set<Collection> collections = new HashSet<Collection>(this.collections.size());
        for (Collection c : this.collections) {
            collections.add(new Collection(c));
        }
        return collections;
    }

    public void setCollections(Set<Collection> collections) {
        Assertion.notNull(collections);
        this.collections = new HashSet<Collection>(collections.size());
        for (Collection c : collections) {
            Assertion.notNull(c);
            this.collections.add(c);
        }
    }

    public void addCollection(Collection... collection) {
        Assertion.notNull(collection);
        for (Collection c : collection) {
            Assertion.notNull(c);
            this.collections.add(new Collection(c));
        }
    }

    public Set<Person> getPersons() {
        final Set<Person> persons = new HashSet<Person>(this.persons.size());
        for (Person du : this.persons) {
            persons.add(new Person(du));
        }
        return persons;
    }

    public void setPersons(Set<Person> persons) {
        Assertion.notNull(persons);
        this.persons = new HashSet<Person>(persons.size());
        for (Person du : persons) {
            Assertion.notNull(du);
            this.persons.add(du);
        }
    }

    public void addPerson(Person... person) {
        Assertion.notNull(person);
        for (Person du : person) {
            Assertion.notNull(du);
            this.persons.add(new Person(du));
        }
    }

    public Set<DataItem> getDataItems() {
        final Set<DataItem> dataItems = new HashSet<DataItem>(this.dataItems.size());
        for (DataItem du : this.dataItems) {
            dataItems.add(new DataItem(du));
        }
        return dataItems;
    }

    public void setDataItems(Set<DataItem> dataItems) {
        Assertion.notNull(dataItems);
        this.dataItems = new HashSet<DataItem>(dataItems.size());
        for (DataItem du : dataItems) {
            Assertion.notNull(du);
            this.dataItems.add(du);
        }
    }

    public void addDataItem(DataItem... dataItem) {
        Assertion.notNull(dataItem);
        for (DataItem du : dataItem) {
            Assertion.notNull(du);
            this.dataItems.add(new DataItem(du));
        }
    }

    public Set<MetadataFile> getMetadataFiles(){
        final Set<MetadataFile> metadataFiles = new HashSet<MetadataFile>(this.metadataFiles.size());
        for (MetadataFile mdf : this.metadataFiles) {
            metadataFiles.add(new MetadataFile(mdf));
        }
        return metadataFiles;
    }

    public void setMetadataFiles(Set<MetadataFile> metadataFiles) {
        Assertion.notNull(metadataFiles);
        this.metadataFiles = new HashSet<MetadataFile>(metadataFiles.size());
        for (MetadataFile mdf : metadataFiles) {
            Assertion.notNull(mdf);
            this.metadataFiles.add(mdf);
        }
    }

    public void addMetadataFile(MetadataFile... metadataFile) {
        Assertion.notNull(metadataFile);
        for (MetadataFile mdf : metadataFile) {
            Assertion.notNull(mdf);
            this.metadataFiles.add(new MetadataFile(mdf));
        }
    }

    /**
     * @return the dropboxModels
     */
    public Set<DropboxModel> getDropboxModels() {
        return dropboxModels;
    }
    
    /**
     * @param dropboxModels
     *            the dropboxModels to set
     */
    public void setDropboxModels(Set<DropboxModel> dropboxModels) {
        this.dropboxModels = dropboxModels;
    }
    
    public void addDropboxModel(DropboxModel... dropboxModel) {
        if (dropboxModel != null) {
            for (DropboxModel du : dropboxModel) {
                if (du != null) {
                    this.dropboxModels.add(new DropboxModel(du));
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bop)) return false;

        Bop bop = (Bop) o;

        if (collections != null ? !collections.equals(bop.collections) : bop.collections != null) return false;
        if (projects != null ? !projects.equals(bop.projects) : bop.projects != null) return false;
        if (persons != null ? !persons.equals(bop.persons) : bop.persons != null) return false;
        if (dataItems != null ? !dataItems.equals(bop.dataItems) : bop.dataItems != null) return false;
        if (metadataFiles != null ? !metadataFiles.equals(bop.metadataFiles) : bop.metadataFiles != null) return false;
        if (dropboxModels != null ? !dropboxModels.equals(bop.dropboxModels) : bop.dropboxModels != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = projects != null ? projects.hashCode() : 0;
        result = 31 * result + (collections != null ? collections.hashCode() : 0);
        result = 31 * result + (persons != null ? persons.hashCode() : 0);
        result = 31 * result + (dataItems != null ? dataItems.hashCode() : 0);
        result = 31 * result + (metadataFiles != null ? metadataFiles.hashCode() : 0);
        result = 31 * result + (dropboxModels != null ? dropboxModels.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "Bop{" +
                "projects=" + projects +
                ", collections=" + collections +
                ", persons=" + persons +
                ", dataItems=" + dataItems +
                ", metadataFiles" + metadataFiles +
                ", dropboxModels=" + dropboxModels +
                '}';
    }

}
