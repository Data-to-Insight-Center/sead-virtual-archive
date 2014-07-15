package org.seadva.registry.service.util;

/**
 * Query Attribute Type
 */
public enum QueryAttributeType {

    PROPERTY("property"),
    DATA_IDENTIFIER("data-identifier"),
    DATA_LOCATION("data-location");

    private final String name;

    QueryAttributeType(String name) {
        this.name = name;
    }

    public static QueryAttributeType fromString(String name) {
        if (name != null) {
            for (QueryAttributeType b : QueryAttributeType.values()) {
                if (name.equalsIgnoreCase(b.name)) {
                    return b;
                }
            }
        }
        return null;
    }
    public String getName() {
        return this.name;
    }

}
