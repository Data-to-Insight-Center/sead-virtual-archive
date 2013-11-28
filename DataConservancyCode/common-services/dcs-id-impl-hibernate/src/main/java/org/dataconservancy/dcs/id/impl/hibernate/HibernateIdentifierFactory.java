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
package org.dataconservancy.dcs.id.impl.hibernate;

import org.dataconservancy.dcs.id.api.Identifier;

/**
 * Generate identifiers for the HibernateIdService.
 * 
 */
public interface HibernateIdentifierFactory {
    public Identifier createNewIdentifier(String urlPrefix, String typeString);
    public Identifier createIdentifierWithExistingPersistentId(String urlPrefix, TypeInfo existingPersistentId);
    public String getUrlEntityTypePrefix();
    public String getUrlLineageTypePrefix();
}
