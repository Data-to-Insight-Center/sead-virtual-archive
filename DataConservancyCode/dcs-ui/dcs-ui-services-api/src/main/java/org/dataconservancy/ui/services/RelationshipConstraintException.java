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
package org.dataconservancy.ui.services;

/**
 * Indicates that the operation would add or remove a relationship that violates a constraint.  For example,
 * adding an AGGREGATES relationship between a Project and a Collection, where the Collection already belongs to
 * a different Project would result in this exception being thrown.
 */
public class RelationshipConstraintException extends RelationshipException {
    public RelationshipConstraintException() {
    }

    public RelationshipConstraintException(String msg) {
        super(msg);
    }

    public RelationshipConstraintException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RelationshipConstraintException(Throwable cause) {
        super(cause);
    }
}
