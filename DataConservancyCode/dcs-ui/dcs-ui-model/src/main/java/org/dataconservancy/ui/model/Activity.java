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

import org.joda.time.DateTime;

/**
 * Each {@link org.dataconservancy.ui.model.Activity} object contains information about a single event captured in the system.
 * {@link org.dataconservancy.ui.model.Activity} is used by {@link org.dataconservancy.ui.stripes.ProjectActivityActionBean} to
 * render activities of a {@link org.dataconservancy.ui.model.Project}.
 */
public class Activity {
    private String description;
    private Person actor;
    private DateTime dateTimeOfOccurrence;
    private Type type;
    private Integer count;

    /**
     * constructs a new Activity with no state
     */
    public Activity() {

    }
    
    public Person getActor() {
        return actor;
    }

    public void setActor(Person actor) {
        this.actor = actor;
    }

    public DateTime getDateTimeOfOccurrence() {
        return dateTimeOfOccurrence;
    }

    public void setDateTimeOfOccurrence(DateTime dateTimeOfOccurrence) {
        this.dateTimeOfOccurrence = dateTimeOfOccurrence;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * sets the multiplicity of events which this activity represents
     * @return count
     */
   public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateOfOccurrence(){
        if(this.dateTimeOfOccurrence !=null){
            return new String(this.dateTimeOfOccurrence.getMonthOfYear() + "/" + this.dateTimeOfOccurrence.getDayOfMonth() + "/" + this.dateTimeOfOccurrence.getYear());
        }
        else{
            return null;
        }
    }
    @Override
    public String toString() {
        return "Activity{" +
                "description='" + description + '\'' +
                ", actor=" + actor +
                ", dateTimeOfOccurrence=" + dateTimeOfOccurrence +
                ", type=" + type +
                ", count=" + count +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Activity)) return false;

        Activity activity = (Activity) o;

        if (actor != null ? !actor.equals(activity.actor) : activity.actor != null) return false;
        if (dateTimeOfOccurrence != null ? !dateTimeOfOccurrence.equals(activity.dateTimeOfOccurrence) : activity.dateTimeOfOccurrence != null)
            return false;
        if (description != null ? !description.equals(activity.description) : activity.description != null)
            return false;
        if (type != activity.type) return false;
        if (count != activity.count) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = description != null ? description.hashCode() : 0;
        result = 31 * result + (actor != null ? actor.hashCode() : 0);
        result = 31 * result + (dateTimeOfOccurrence != null ? dateTimeOfOccurrence.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (count != null ? count.hashCode() : 0);
        return result;
    }

    public enum Type{
        COLLECTION_DEPOSIT,
        DATASET_DEPOSIT
    }
}
