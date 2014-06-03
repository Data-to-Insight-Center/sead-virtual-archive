package org.seadva.data.lifecycle.support.model;


import javax.xml.bind.annotation.XmlElement;
import java.util.Date;

/**
 * Event metadata - used to track events in RO system
 */

public class Event {

    public Event(){}

    @XmlElement(required = true)
    String eventIdentifier;
    @XmlElement(required = true)
    String workflowId;
    @XmlElement(required = true)
    String targetId;
    @XmlElement(required = true)
    Date eventDateTime;
    @XmlElement(required = true)
    String eventType;
    @XmlElement(required = true)
    String linkingAgentIdentifier;

    public String getLinkingAgentIdentifier() {
        return linkingAgentIdentifier;
    }

    public void setLinkingAgentIdentifier(String linkingAgentIdentifier) {
        this.linkingAgentIdentifier = linkingAgentIdentifier;
    }

    public String getEventIdentifier() {
        return eventIdentifier;
    }

    public void setEventIdentifier(String eventIdentifier) {
        this.eventIdentifier = eventIdentifier;
    }


    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public Date getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(Date eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
