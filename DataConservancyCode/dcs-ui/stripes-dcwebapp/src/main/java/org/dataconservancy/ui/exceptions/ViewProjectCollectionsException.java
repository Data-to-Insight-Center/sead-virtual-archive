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
 * Thrown by the ProjectCollectionsActionBean when there is an error viewing the collections for a project.
 */
public class ViewProjectCollectionsException extends BaseUiException {

    private String userId;

    private String projectId;

    public ViewProjectCollectionsException() {
    }

    public ViewProjectCollectionsException(Throwable cause) {
        super(cause);
    }

    public ViewProjectCollectionsException(String message) {
        super(message);
    }

    public ViewProjectCollectionsException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
