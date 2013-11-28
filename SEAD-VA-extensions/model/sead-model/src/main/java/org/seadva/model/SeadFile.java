package org.seadva.model;

import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataconservancy.model.dcs.support.Assertion;

import java.util.HashSet;
import java.util.Set;

/**
 * SEAD additional File metadata
 */
public class SeadFile extends DcsFile {

    private SeadDataLocation primaryLocation = new SeadDataLocation();
    private Set<SeadDataLocation> secondaryDataLocations = new HashSet<SeadDataLocation>();
    private String depositDate;
    private String pubdate;
    private String metadataUpdateDate;
    private String parent;

    public SeadFile() {
        super();
    }
    public SeadFile(DcsFile toCopy) {
        super(toCopy);
    }

    public SeadFile(SeadFile toCopy) {
        super(toCopy);
        this.primaryLocation = toCopy.getPrimaryLocation();
        this.secondaryDataLocations = toCopy.getSecondaryDataLocations();
        this.depositDate = toCopy.getDepositDate();
        this.pubdate = toCopy.getPubdate();
        this.metadataUpdateDate = toCopy.getMetadataUpdateDate();
        this.parent = toCopy.getParent();
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

    public SeadDataLocation getPrimaryLocation() {
        return new SeadDataLocation(primaryLocation);
    }

    public SeadFile setPrimaryLocation(SeadDataLocation primaryLocation) {
        this.primaryLocation = primaryLocation;
        return this;
    }

    public String getDepositDate() {
        return depositDate;
    }

    public void setDepositDate(String depositDate) {
        this.depositDate = depositDate;
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

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        String ids = "";
        for( DcsResourceIdentifier id : this.getAlternateIds()){
            ids += id + ", ";
        }
        return "DcsFile{" +
                "name='" + getName() + '\'' +
                ", source='" + getSource() + '\'' +
                ", extant=" + isExtant() +
                ", valid=" + getValid() +
                ", sizeBytes=" + getSizeBytes() +
                ", fixity=" + getFixity() +
                ", formats=" + getFormats() +
                ", metadata=" + getMetadata() +
                ", metadataRef=" + getMetadataRef() +
                ", alternateIds=" + ids +
                ", depositDate=" + getDepositDate() +
                ", parent=" + getParent() +
                ", primaryDataLocation=" + getPrimaryLocation() +
                ", secondaryDataLocations=" + getSecondaryDataLocations() +
                '}';
    }
}
