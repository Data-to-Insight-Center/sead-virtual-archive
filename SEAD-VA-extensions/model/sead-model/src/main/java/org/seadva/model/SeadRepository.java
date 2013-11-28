package org.seadva.model;

import org.dataconservancy.model.dcs.support.Assertion;

/**
 *SEAD Repository details
 */
public class SeadRepository {

    public String getIrId() {
        return irId;
    }

    public void setIrId(String irId) {
        this.irId = irId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String irId;
    private String url;
    private String type;
    private String name;

    /**
     * Constructs a new DcsResourceIdentifier with no state.
     */
    public SeadRepository() {

    }

    public SeadRepository(String irId, String name, String url, String type) {
        Assertion.notEmptyOrNull(irId);
        this.irId = irId;
        this.name = name;
        this.url =url;
        this.type = type;
    }

    /**
     * Copy constructor for a DcsResourceIdentifier.  The state of <code>toCopy</code> is copied
     * to this.
     *
     * @param toCopy the dcs resource identifier to copy, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>toCopy</code> is <code>null</code>
     */
    public SeadRepository(SeadRepository toCopy) {
        Assertion.notNull(toCopy);
        this.irId = toCopy.irId;
        this.name = toCopy.name;
        this.url = toCopy.url;
        this.type = toCopy.type;
    }

}
