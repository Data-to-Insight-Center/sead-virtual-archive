package org.dataconservancy.mhf.representation.api;

import java.util.Collection;

/**
 *
 */
public interface AttributeSet {

    public String getName();

    public Collection<Attribute> getAttributes();

    public Collection<Attribute> getAttributesByName(String attributeName);

}
