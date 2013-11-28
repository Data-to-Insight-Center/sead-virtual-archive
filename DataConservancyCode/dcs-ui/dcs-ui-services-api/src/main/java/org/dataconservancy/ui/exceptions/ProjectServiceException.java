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
package org.dataconservancy.ui.exceptions;

/**
 * This exception is thrown by {@link org.dataconservancy.ui.services.ProjectService} when an exceptional condition or error
 * is encountered when updating {@link org.dataconservancy.ui.model.Project}s.
 */
public class ProjectServiceException extends Exception {
	private String message;
	

	public ProjectServiceException()
	{
		super();
		message = "unknown";
	}
	
	public ProjectServiceException(String message) {
		super(message);
		this.message = message;
	}

	@Override
	public String toString() {
		return "ProjectServiceException [message=" + message + "]";
	}
}
