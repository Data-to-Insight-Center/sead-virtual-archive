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
package org.dataconservancy.ui.eventing.api;

/**
 * The context surrounding the firing of an event.  Meant to capture what the user-agent was attempting to do when the
 * event was fired (e.g. event metadata).
 * TODO: consider/flesh out the fields and methods in this class.
 */
public class EventContext {

    // TODO: Flesh out the fields and methods in this class.

    private String user;

    private String actionBean;

    private EventClass eventClass;

    private String requestUri;

    private String hostName;

    private String originIp;

    private String buildNumber;

    private String revisionNumber;

    private String buildDate;

    private String eventDate;

    public String getActionBean() {
        return actionBean;
    }

    public void setActionBean(String actionBean) {
        this.actionBean = actionBean;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public EventClass getEventClass() {
        return eventClass;
    }

    public void setEventClass(EventClass eventClass) {
        this.eventClass = eventClass;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getOriginIp() {
        return originIp;
    }

    public void setOriginIp(String originIp) {
        this.originIp = originIp;
    }

    public String getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(String revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    @Override
    public String toString() {
        return "EventContext{" +
                "actionBean='" + actionBean + '\'' +
                ", user='" + user + '\'' +
                ", type=" + eventClass +
                ", requestUri='" + requestUri + '\'' +
                ", hostName='" + hostName + '\'' +
                ", originIp='" + originIp + '\'' +
                ", buildNumber='" + buildNumber + '\'' +
                ", revisionNumber='" + revisionNumber + '\'' +
                ", buildDate='" + buildDate + '\'' +
                ", eventDate='" + eventDate + '\'' +
                '}';
    }
}
