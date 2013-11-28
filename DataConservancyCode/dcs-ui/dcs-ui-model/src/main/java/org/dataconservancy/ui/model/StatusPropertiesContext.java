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

/**
 * Provides a bean within which to record the build context. This is SCM specific (in this case Subversion).
 */
public class StatusPropertiesContext {

    private String buildNumber;
    private String buildTimeStamp;
    private String buildRevision;

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getBuildTimeStamp() {
        return buildTimeStamp;
    }

    public void setBuildTimeStamp(String buildTimeStamp) {
        this.buildTimeStamp = buildTimeStamp;
    }

    public String getBuildRevision() {
        return buildRevision;
    }

    public void setBuildRevision(String buildRevision) {
        this.buildRevision = buildRevision;
    }

    @Override
    public String toString() {
        return "StatusPropertiesContext{" +
            "buildNumber='" + buildNumber + '\'' +
            ", buildTimeStamp='" + buildTimeStamp + '\'' +
            ", buildRevision='" + buildRevision + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatusPropertiesContext that = (StatusPropertiesContext) o;

        if (buildNumber != null ? !buildNumber.equals(that.buildNumber) : that.buildNumber != null) return false;
        if (buildRevision != null ? !buildRevision.equals(that.buildRevision) : that.buildRevision != null)
            return false;
        if (buildTimeStamp != null ? !buildTimeStamp.equals(that.buildTimeStamp) : that.buildTimeStamp != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = buildNumber != null ? buildNumber.hashCode() : 0;
        result = 31 * result + (buildTimeStamp != null ? buildTimeStamp.hashCode() : 0);
        result = 31 * result + (buildRevision != null ? buildRevision.hashCode() : 0);
        return result;
    }

}
