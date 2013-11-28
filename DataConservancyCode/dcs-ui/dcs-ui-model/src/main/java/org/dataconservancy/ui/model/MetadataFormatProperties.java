/*
 * Copyright 2013 Johns Hopkins University
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container for the business properties of a {@code DcsMetadataFormat}.  The business properties encapsulated in
 * this object apply to the {@code DcsMetadataFormat} identified by {@link #getFormatId()}.
 * <p/>
 * Because the business properties of a {@code DcsMetadataFormat} may differ between instances of a UI, the format and
 * its business properties are kept separate.
 */
public class MetadataFormatProperties {

    private String formatId;
    private boolean appliesToCollection;
    private boolean appliesToProject;
    private boolean appliesToItem;
    private boolean validates;
    private boolean isActive = true;

    private List<String> disciplineIds = new ArrayList<String>();

    /**
     * Whether or not the {@link #getFormatId() identified metadata format} can be applied at the {@link Collection}
     * level.
     *
     * @return true if the metadata format can be applied at the collection level
     */
    public boolean isAppliesToCollection() {
        return appliesToCollection;
    }

    /**
     * Whether or not the {@link #getFormatId() identified metadata format} can be applied at the {@link Collection}
     * level.
     *
     * @param appliesToCollection if the metadata format can be applied at the item level
     */
    public void setAppliesToCollection(boolean appliesToCollection) {
        this.appliesToCollection = appliesToCollection;
    }

    /**
     * Whether or not the {@link #getFormatId() identified metadata format} can be applied at the {@link DataItem} level.
     *
     * @return true if the metadata format can be applied at the item level
     */
    public boolean isAppliesToItem() {
        return appliesToItem;
    }

    /**
     * Whether or not the {@link #getFormatId() identified metadata format} can be applied at the {@link DataItem} level.
     *
     * @param appliesToItem if the metadata format can be applied at the item level
     */
    public void setAppliesToItem(boolean appliesToItem) {
        this.appliesToItem = appliesToItem;
    }

    /**
     * Whether or not the {@link #getFormatId() identified metadata format} can be applied at the {@link Project} level.
     *
     * @return true if the metadata format can be applied at the project level
     */
    public boolean isAppliesToProject() {
        return appliesToProject;
    }

    /**
     * Whether or not the {@link #getFormatId() identified metadata format} can be applied at the {@link Project} level.
     *
     * @param appliesToProject if the metadata format can be applied at the project level
     */
    public void setAppliesToProject(boolean appliesToProject) {
        this.appliesToProject = appliesToProject;
    }

    /**
     * TODO: Javadoc
     *
     * @return
     */
    public boolean isValidates() {
        return validates;
    }

    /**
     * TODO: Javadoc
     *
     * @param validates
     */
    public void setValidates(boolean validates) {
        this.validates = validates;
    }

    /**
     * Whether or not the the MetadataFormat is still in used in the system. MetadataFormat that has been deleted by the
     * users would be marked as NOT ACTIVE (ie. isActive() is false).  By default, this value is {@code true}.
     *
     * @return true if the metadata format is still in used in the system, has not been "deleted"/"removed" by users
     * @return false if the metadata format has been "deleted"/"removed" by users at some point.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     *
     * @param active
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * The business identifiers of the {@code Discipline} objects that the {@link #getFormatId() DcsMetadataFormat} is
     * relevant to.
     *
     * @return a list of business identifiers of {@code Discipline} objects
     */
    public List<String> getDisciplineIds() {
        return disciplineIds;
    }

    /**
     * The business identifiers of the {@code Discipline} objects that the {@link #getFormatId() DcsMetadataFormat} is
     * relevant to.
     *
     * @return a list of business identifiers of {@code Discipline} objects
     */
    public void setDisciplineIds(List<String> disciplineIds) {
        this.disciplineIds = disciplineIds;
    }

    /**
     * The business identifier of the {@code DcsMetadataFormat} that these properties apply to.
     *
     * @return the business identifier of the {@code DcsMetadataFormat}
     */
    public String getFormatId() {
        return formatId;
    }

    /**
     * The business identifier of the {@code DcsMetadataFormat} that these properties apply to.
     *
     * @param formatId the business identifier of the {@code DcsMetadataFormat}
     */
    public void setFormatId(String formatId) {
        this.formatId = formatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetadataFormatProperties that = (MetadataFormatProperties) o;

        if (appliesToCollection != that.appliesToCollection) return false;
        if (appliesToItem != that.appliesToItem) return false;
        if (appliesToProject != that.appliesToProject) return false;
        if (validates != that.validates) return false;
        if (isActive != that.isActive) return false;
        if (disciplineIds != null ? !disciplineIds.equals(that.disciplineIds) : that.disciplineIds != null)
            return false;
        if (formatId != null ? !formatId.equals(that.formatId) : that.formatId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = formatId != null ? formatId.hashCode() : 0;
        result = 31 * result + (appliesToCollection ? 1 : 0);
        result = 31 * result + (appliesToProject ? 1 : 0);
        result = 31 * result + (appliesToItem ? 1 : 0);
        result = 31 * result + (validates ? 1 : 0);
        result = 31 * result + (isActive ? 1 : 0);
        result = 31 * result + (disciplineIds != null ? disciplineIds.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MetadataFormatProperties{" +
                "appliesToCollection=" + appliesToCollection +
                ", formatId='" + formatId + '\'' +
                ", appliesToProject=" + appliesToProject +
                ", appliesToItem=" + appliesToItem +
                ", validates=" + validates +
                ", isActive=" + isActive +
                ", disciplineIds=" + disciplineIds +
                '}';
    }
}
