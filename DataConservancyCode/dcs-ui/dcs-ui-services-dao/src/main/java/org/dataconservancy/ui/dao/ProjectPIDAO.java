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

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;

/**
 * This class is used by {@link org.dataconservancy.ui.dao.ProjectDAO} to mange Project PIs. Current Projects PIs are
 * being given Project Admin rights; therefore Project PI role and Projec Admin right are being managed together here.
 */
public interface ProjectPIDAO {
	public List<String> getProjectIds(Person person);
	public List<String> getPIIds(String projectId);
	/**
	 * Removes project-admin records for a particular adminId
	 * @param adminId to be removed
	 * @return <p>Number of records affected (deleted)</p>
	 * 	       <p>No such record exists and no update was made, returns 0</p>
	 */
    /*
	public int deleteAdminsByAdminId(String adminId);
	*/
	/**
	 * Removes project-admin records for a particular projectId
	 * @param projectId to be removed
	 * @return <p>Number of records affected (deleted)</p>
	 * 	       <p>No such record exists and no update was made, returns 0</p>
	 */
	/*
	public int deleteAdminIdsByProjectId(int projectId);
	*/
	
	/**
	 * Removes a single project-admin record that satisfy the input
	 * @param projectId
	 * @param adminId
	 * @return <p>Number of records affected (deleted)</p>
	 * 	       <p>No such record exists and no update was made, returns 0</p>
	 */
	public int deleteSingleProjectAdmin(String projectId, String adminId);
	
	public void insertAllProjectPIs(Project project);
	public void insertSingleProjectPI(String projectId, String adminId);
}
