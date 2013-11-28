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
 * This exception is thrown by {@link org.dataconservancy.ui.stripes.ProjectActivityActionBean} when exception condition
 * or error is encountered when rendering project's activities.
 */
public class ViewProjectActivityException extends BaseUiException {

    private String projectId;

    private String projectName;

    public ViewProjectActivityException() {

    }

    public ViewProjectActivityException(Throwable cause) {
        super(cause);
    }

    public ViewProjectActivityException(String message) {
        super(message);
    }

    public ViewProjectActivityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * The ID of the project that activity is being viewed for
     *
     * @return the project id
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * The ID of the project that activity is being viewed for
     *
     * @param projectId the project id
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * The name of the project that activity is being viewed for
     *
     * @return the project name
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * The name of the project that activity is being viewed for
     *
     * @param projectName the project name
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
