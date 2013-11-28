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
package org.dataconservancy.ui.dao;

import java.util.List;

import org.dataconservancy.ui.model.Discipline;

/**
 * Responsible for CRUD operations on an underlying persistence store for {@link Discipline} objects.  Discipline
 * objects are keyed by URI.
 */
public interface DisciplineDAO {

    /**
     * Store a new Discipline.
     * 
     * @param dis the Discipline
     */
    public void add(Discipline dis);
    
    /**
     * List all Disciplines.
     * 
     * @return list of Discipline
     */
    public List<Discipline> list();

    /**
     * Obtain the identified Discipline.
     *
     * @param disciplineId the business identifier of the Discipline
     * @return the Discipline
     */
    public Discipline get(String disciplineId);
}
