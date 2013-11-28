package org.seadva.model;

/**
 * Models a SEAD person
 */
public class SeadPerson {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    private String name;
    private String id;
    private String idType;

    /**
     * Constructs a new DcsCollection with no state.
     */
    public SeadPerson() {

    }

    /**
     * Copy constructor for a DcsCollection.  The state of <code>toCopy</code> is copied
     * to this.
     *
     * @param toCopy the collection to copy
     */
    public SeadPerson(SeadPerson toCopy) {
        this.id = toCopy.getId();
        this.name = toCopy.getName();
        this.idType = toCopy.getIdType();
    }



    @Override
    public String toString() {
        String ids = "";

        return "SeadPerson{" +
                "name=" + name +
                ", personId=" + id +
                ", personIdType=" + idType +
                '}';
    }
}
