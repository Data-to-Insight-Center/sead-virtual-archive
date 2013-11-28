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
package org.dataconservancy.archive.api;

/**
 * Signals that the entity is of the wrong type for the request being made.
 */
public class EntityTypeException extends Exception {
    private final String entityId;
    private final EntityType expectedType;
    private final EntityType actualType;

    /**
     * Constructs an <code>EntityTypeException</code>.
     *
     * @param entityId the id of the requested entity.
     * @param expectedType the type it should have been.
     * @param actualType the type it was.
     */
    public EntityTypeException(String entityId,
                               EntityType expectedType,
                               EntityType actualType) {
        super("Entity (id='" + entityId + "') is a " + actualType
                + "; expected a " + expectedType);
        this.entityId = entityId;
        this.expectedType = expectedType;
        this.actualType = actualType;
    }

    /**
     * Gets the id of the entity.
     *
     * @return the entity id.
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Gets the expected type of the entity.
     *
     * @return the type it should have been.
     */
    public EntityType getExpectedType() {
        return expectedType;
    }

    /**
     * Gets the actual type of the entity.
     *
     * @return the type it was.
     */
    public EntityType getActualType() {
        return actualType;
    }

}
