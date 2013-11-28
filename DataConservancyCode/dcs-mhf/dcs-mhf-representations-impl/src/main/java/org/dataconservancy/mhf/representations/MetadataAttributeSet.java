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

package org.dataconservancy.mhf.representations;

import org.dataconservancy.mhf.representation.api.Attribute;
import org.dataconservancy.mhf.representation.api.AttributeSet;

import java.util.Collection;
import java.util.HashSet;

public class MetadataAttributeSet implements AttributeSet {

    private String name;
    private HashSet<Attribute> attributes;

    public MetadataAttributeSet(String name) {
        this.name = name;
        this.attributes = new HashSet<Attribute>();
    }

    public void setName(String attributeSetName) {
        this.name = attributeSetName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<Attribute> getAttributesByName(String attributeName) {
        Collection<Attribute> matchingAttributes = new HashSet<Attribute>();
        for (Attribute attribute : attributes) {
            if (attribute.getName().trim().equals(attributeName.trim())) {
                matchingAttributes.add(attribute);
            }
        }
        return matchingAttributes;
    }

    public void setAttributes(Collection<Attribute> attributes) {
        this.attributes.addAll(attributes);
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetadataAttributeSet)) return false;

        MetadataAttributeSet that = (MetadataAttributeSet) o;

        if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MetadataAttributeSet{" +
                "name='" + name + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}

