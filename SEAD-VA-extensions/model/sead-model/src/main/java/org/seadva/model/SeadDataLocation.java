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
package org.seadva.model;

import org.dataconservancy.model.dcs.support.Assertion;

/**
 * A designation used to identify the resource within the preservation repository system in which it is stored.
 * Identifiers may be unique or not depending on policies applied to their use.
 */
public class SeadDataLocation {

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private String type;
    private String location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    /**
     * Constructs a new DcsResourceIdentifier with no state.
     */
    public SeadDataLocation() {

    }

   public SeadDataLocation(String location, String type, String name) {
        Assertion.notEmptyOrNull(location);
        this.location = location;
        this.type = type;
        this.name = name;
    }

    /**
     * Copy constructor for a DcsResourceIdentifier.  The state of <code>toCopy</code> is copied
     * to this.
     *
     * @param toCopy the dcs resource identifier to copy, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>toCopy</code> is <code>null</code>
     */
    public SeadDataLocation(SeadDataLocation toCopy) {
        Assertion.notNull(toCopy);
        this.location = toCopy.getLocation();
        this.type = toCopy.getType();
        this.name = toCopy.getName();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SeadDataLocation that = (SeadDataLocation) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SeadDataLocation{" +
                "location=" + location +
                ", name=" + name +
                ", type=" + type +
                '}';
    }

}
