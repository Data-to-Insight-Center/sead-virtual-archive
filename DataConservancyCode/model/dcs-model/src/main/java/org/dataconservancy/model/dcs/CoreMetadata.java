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

import java.util.Arrays;
import java.util.Collection;

import org.dataconservancy.model.dcs.support.Assertion;
import org.dataconservancy.model.dcs.support.CollectionFactory;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.model.dcs.support.Util;

import static org.dataconservancy.model.dcs.support.Util.deepCopy;

/**
 * Encapsulates core metadata fields that can be associated with DCS entities.
 */
class CoreMetadata {
    private Collection<String> creators = CollectionFactory.newCollection();
    private Collection<String> subjects = CollectionFactory.newCollection();
    private String type;
    private String title;
    private String rights;

    /**
     * Constructs a CoreMetadata with default state
     */
    CoreMetadata() {

    }

    /**
     * Constructs a CoreMetadata with state deeply copied from {@code toCopy}.  Note that if {@code toCopy} is
     * concurrently modified during this construction, the resulting state of this CoreMetadata is undefined.
     *
     * @param toCopy
     */
    CoreMetadata(CoreMetadata toCopy) {
        deepCopy(toCopy.creators, this.creators);
        deepCopy(toCopy.subjects, this.subjects);
        this.type = toCopy.getType();
        this.title = toCopy.getTitle();
        this.rights = toCopy.getRights();
    }


    /**
     * The title of the entity.
     *
     * @return the title, or <code>null</code> if no title has been set
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title of the entity.
     *
     * @param title the title, must not be an empty string or <code>null</code>
     * @throws IllegalArgumentException if <code>title</code> is empty or <code>null</code>
     */
    public void setTitle(String title) {
        Assertion.notEmptyOrNull(title);
        this.title = title;
    }

    /**
     * The creator(s) of the entity.
     *
     * @return the creator(s), may be empty, but never <code>null</code>
     */
    public Collection<String> getCreators() {
        return this.creators;
    }

    /**
     * Set the creator(s) of the entity.  Note this nullifies existing creators.
     *
     * @param creators the creators, must not be <code>null</code> or contain {@code null} references, empty strings,
     *                 or zero-length strings.
     * @throws IllegalArgumentException if <code>creators</code> is <code>null</code>, contains {@code null} references,
     *                                  zero-length, or empty strings
     */
    public void setCreators(Collection<String> creators) {
        Assertion.notNull(creators);
        Assertion.doesNotContainNullOrEmptyString(creators);
        this.creators = creators;
    }

    /**
     * Add a creator to the entity.
     *
     * @param creator the creators, must not be <code>null</code>, contain {@code null} references, zero-length strings,
     *                or empty strings.
     * @throws IllegalArgumentException if <code>creator</code> is <code>null</code>, contains {@code null} references,
     *                                  zero-length, or empty strings
     */
    public void addCreator(String... creator) {
        Assertion.notNull(creator);
        Assertion.doesNotContainNullOrEmptyString(creator);
        creators.addAll(Arrays.asList(creator));
    }

    /**
     * The subject(s) of the entity.
     *
     * @return the subject(s) of the entity
     */
    public Collection<String> getSubjects() {
        return this.subjects;
    }

    /**
     * Set the subject(s) of the entity.  Note this nullifies existing subjects.
     *
     * @param subjects the subjects, must not be <code>null</code>, contain {@code null} references, empty or zero-
     *                 length strings.
     * @throws IllegalArgumentException if <code>subjects</code> is <code>null</code> or the empty string
     */
    public void setSubjects(Collection<String> subjects) {
        Assertion.notNull(subjects);
        Assertion.doesNotContainNullOrEmptyString(subjects);
        this.subjects = subjects;
    }

    /**
     * Add a subject
     *
     * @param subject the subject, must not be <code>null</code>, the empty string, or a zero-length string.
     * @throws IllegalArgumentException if <code>subject</code> is <code>null</code>, the empty string, or a zero-
     *                                  length string.
     */
    public void addSubject(String... subject) {
        Assertion.notNull(subject);
        Assertion.doesNotContainNullOrEmptyString(subject);
        this.subjects.addAll(Arrays.asList(subject));
    }

    /**
     * The type of the entity
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the type of the entity
     * @param type the type
     * @throws IllegalArgumentException if {@code type} is empty or {@code null}
     */
    public void setType(String type) {
        Assertion.notEmptyOrNull(type);
        this.type = type;
    }

    /**
     * Rights associated with this entity
     *
     * @return the rights description
     */
    public String getRights() {
        return rights;
    }

    /**
     * Rights associated with this entity, must not be {@code null}, an empty or zero-length string.
     *
     * @param rights the rights description
     * @throws IllegalArgumentException if <code>rights</code> is <code>null</code>, the empty string, or a zero-
     *                                  length string.
     */
    public void setRights(String rights) {
        Assertion.notEmptyOrNull(rights);
        this.rights = rights;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoreMetadata that = (CoreMetadata) o;

        if (!Util.isEqual(creators, that.creators)) {
            return false;
        }

        if (rights != null ? !rights.equals(that.rights) : that.rights != null) return false;

        if (!Util.isEqual(subjects, that.subjects)) {
            return false;
        }

        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = creators != null ? Util.hashCode(creators) : 0;
        result = 31 * result + (subjects != null ? Util.hashCode(subjects) : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (rights != null ? rights.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CoreMetadata{" +
                "creators=" + creators +
                ", subjects=" + subjects +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", rights=" + rights +
                '}';
    }

    public void toString(HierarchicalPrettyPrinter sb) {
        sb.appendWithIndentAndNewLine("Core Metadata:");
        sb.incrementDepth();
        sb.appendWithIndent("type: ").appendWithNewLine(type);
        sb.appendWithIndent("title: ").appendWithNewLine(title);
        sb.appendWithIndent("rights: ").appendWithNewLine(rights);
        for (String subject : subjects) {
            sb.appendWithIndent("subject: ").appendWithNewLine(subject);
        }
        sb.decrementDepth();
    }
}

