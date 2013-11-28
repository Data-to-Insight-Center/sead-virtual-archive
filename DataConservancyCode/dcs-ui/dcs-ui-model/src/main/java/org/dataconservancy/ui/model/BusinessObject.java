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

public class BusinessObject {
    
    protected String id;
    
    /**
     * Retrieves the business identifier of the object.
     *
     * @return the identifier
     */
    public String getId() {
        return id;
    }
    
    /**
     * Set the business identifier of the object.
     *
     * @param identifier the identifier
     */
    public void setId(String identifier) {
        this.id = identifier;
    }
}