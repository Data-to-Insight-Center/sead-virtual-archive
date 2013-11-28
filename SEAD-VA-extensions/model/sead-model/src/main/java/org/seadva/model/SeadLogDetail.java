package org.seadva.model;

/**
 * Models Additional Event details
 */
public class SeadLogDetail {
    private String ipAddress;
    private String userAgent;
    private String subject;
    private String nodeIdentifier;

    /**
     * Constructs a new SeadLogDetail with no state.
     */
    public SeadLogDetail() {

    }

    /**
     * Copy constructor for a SeadLogDetail.  The state of <code>toCopy</code> is copied
     * to this.
     *
     * @param toCopy the dcs event to copy
     * @throws IllegalArgumentException if <code>toCopy</code> is <code>null</code>
     */
    public SeadLogDetail(SeadLogDetail toCopy) {
        this.ipAddress = toCopy.getIpAddress();
        this.userAgent = toCopy.getUserAgent();
        this.subject = toCopy.getSubject();
        this.nodeIdentifier = toCopy.getNodeIdentifier();
    }

     @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

         SeadLogDetail dcsLog = (SeadLogDetail) o;

        if (ipAddress != null ? !ipAddress.equals(dcsLog.ipAddress) : dcsLog.ipAddress != null) return false;
        if (userAgent != null ? !userAgent.equals(dcsLog.userAgent) : dcsLog.userAgent != null) return false;
        if (subject != null ? !subject.equals(dcsLog.subject) : dcsLog.subject != null) return false;
        if (nodeIdentifier != null ? !nodeIdentifier.equals(dcsLog.nodeIdentifier) : dcsLog.nodeIdentifier != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (ipAddress != null ? ipAddress.hashCode() : 0);
        result = 31 * result + (userAgent != null ? userAgent.hashCode() : 0);
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (nodeIdentifier != null ? nodeIdentifier.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SeadLogDetail{" +
                "ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", subject='" + subject + '\'' +
                ", nodeIdentifier=" + nodeIdentifier +
                '}';
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    public void setNodeIdentifier(String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }
}
