package org.seadva.model;

import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataconservancy.model.dcs.support.Assertion;

import java.util.HashSet;
import java.util.Set;

/**
 *Object describing a data collection
 */
public class SeadDeliverableUnit extends DcsDeliverableUnit{

    private Set<SeadPerson> dataContributors = new HashSet<SeadPerson>(1);
    private SeadPerson submitter = new SeadPerson();
    private String abstrct;
    private String contact;
    private Set<String> sites = new HashSet<String>(1);
    private String pubdate;
    private String metadataUpdateDate;
    private long sizeBytes;
    private long fileNo;
    private SeadDataLocation primaryLocation = new SeadDataLocation();
    private Set<SeadDataLocation> secondaryDataLocations = new HashSet<SeadDataLocation>();


    public SeadDeliverableUnit(){
        super();
    }

    public SeadDeliverableUnit(DcsDeliverableUnit toCopy) {
        super(toCopy);
    }

    public SeadDeliverableUnit(SeadDeliverableUnit toCopy) {
        super(toCopy);
        this.abstrct = toCopy.getAbstrct();
        this.submitter = toCopy.getSubmitter();
        this.contact = toCopy.getContact();
    }

    public Set<SeadPerson> getDataContributors() {
        return dataContributors;
    }

    public void setDataContributors(Set<SeadPerson> dataContributors) {
        this.dataContributors = dataContributors;
    }

    public void addDataContributor(SeadPerson dataContributor) {
        this.dataContributors.add(dataContributor);
    }

    public SeadPerson getSubmitter() {
        return submitter;
    }

    public void setSubmitter(SeadPerson submitter) {
        this.submitter = submitter;
    }

    public String getContact(){
        return this.contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Set<String> getSites() {
        return sites;
    }

    public void setSites(Set<String> sites) {
        this.sites = sites;
    }

    public void addSite(String site) {
        this.sites.add(site);
    }

    public String getPubdate() {
        return pubdate;
    }

    public void setPubdate(String pubdate) {
        this.pubdate = pubdate;
    }

    public String getMetadataUpdateDate() {
        return metadataUpdateDate;
    }

    public void setMetadataUpdateDate(String metadataUpdateDate) {
        this.metadataUpdateDate = metadataUpdateDate;
    }
    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long size) {
        this.sizeBytes = size;
    }

    public long getFileNo() {
        return fileNo;
    }

    public void setFileNo(long fileNo) {
        this.fileNo = fileNo;
    }

    public String getAbstrct() {
        return abstrct;
    }

    public void setAbstrct(String abstrct) {
        this.abstrct = abstrct;
    }

    public SeadDataLocation getPrimaryLocation() {
        return primaryLocation;
    }

    public void setPrimaryLocation(SeadDataLocation primaryLocation) {
        this.primaryLocation = primaryLocation;
    }

    public Set<SeadDataLocation> getSecondaryDataLocations() {
        return secondaryDataLocations;
    }

    public void setSecondaryDataLocations(Set<SeadDataLocation> secondaryDataLocations) {
        this.secondaryDataLocations = secondaryDataLocations;
    }

    public void addSecondaryDataLocation(SeadDataLocation... dataLocation) {
        Assertion.notNull(dataLocation);
        for (SeadDataLocation datLoc : dataLocation) {
            Assertion.notNull(datLoc);
            this.secondaryDataLocations.add(datLoc);
        }
    }

    String getCoreMetadata(){
    return "CoreMetadata{" +
            "dataContributors=" + getDataContributors() +
            ", submitter=" + getSubmitter()+
            ", contact='" + getContact() + '\'' +
            ", subjects=" + getSubjects() +
            ", sites=" + getSites() +
            ", type='" + getType() + '\'' +
            ", title='" + getTitle() + '\'' +
            ", rights=" + getRights() +
            '}';
    }
    @Override
    public String toString() {
        String ids = "";
        for( DcsResourceIdentifier id : this.getAlternateIds()){
            ids += id + ", ";
        }
        String ret = "DcsDeliverableUnit{" +
                "collections=" + getCollections() +
                ", metadata=" + getMetadata() +
                ", metadataRefs=" + getMetadataRef() +
                ", relations=" + getRelations() +
                ", formerExternalRefs=" + getFormerExternalRefs() +
                ", parents=" + getParents() +
                ", isDigitalSurrogate=" + isDigitalSurrogate() +
                ", coreMd=" + getCoreMetadata() +
                ", pubdate='" + getPubdate() + '\'' +
                ", metadataUpdateDate='" + getMetadataUpdateDate() +
                ", alternateIds=" + ids +
                ", abstract=" + getAbstrct() +
                ", sizeBytes=" + getSizeBytes() +
                ", fileNo="+ getFileNo() +
                ", primaryDataLocation=" + getPrimaryLocation() +
                ", secondaryDataLocations=" + getSecondaryDataLocations() +
                '}';
        return ret;
    }



}
