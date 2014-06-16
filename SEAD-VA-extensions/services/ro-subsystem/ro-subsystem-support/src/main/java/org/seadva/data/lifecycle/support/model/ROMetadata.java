package org.seadva.data.lifecycle.support.model;

/**
 * Simple RO metadata, used when retrieving lists
 */
public class ROMetadata {
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getObsolete() {
        return isObsolete;
    }

    public void setObsolete(int obsolete) {
        isObsolete = obsolete;
    }

    public int getIsObsolete() {
        return isObsolete;
    }

    public void setIsObsolete(int obsolete) {
        isObsolete = obsolete;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    String identifier;
    String name;
    String type;
    String updatedDate;
    int isObsolete;
    String agentId;

}
