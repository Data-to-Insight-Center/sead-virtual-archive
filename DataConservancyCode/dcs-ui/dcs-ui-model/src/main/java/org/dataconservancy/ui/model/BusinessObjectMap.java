package org.dataconservancy.ui.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A business object map represents a hierarchy of business objects with human
 * readable names and types. A business object map may also associate an object
 * with a list of alternate ids.
 */
public class BusinessObjectMap {
    private String id;
    private String name;
    private String type;
    private String depositStatus;
    private List<String> alternateIds;
    private List<BusinessObjectMap> children;
    private Logger log = LoggerFactory.getLogger(BusinessObjectMap.class);

    /**
     * @param object
     * @return human readable name for business object or null on failure to
     *         find a name
     */
    private static String getName(BusinessObject object) {
        if (object instanceof Project) {
            Project p = (Project) object;

            return p.getName();
        }

        if (object instanceof Collection) {
            Collection c = (Collection) object;

            return c.getTitle();
        }

        if (object instanceof DataItem) {
            DataItem di = (DataItem) object;

            return di.getName();
        }

        if (object instanceof DataFile) {
            DataFile df = (DataFile) object;

            return df.getName();
        }

        return null;
    }

    /**
     * @param object
     * @return human readable business object type name or null on failure to
     *         find a name
     */
    private static String getType(BusinessObject object) {
        if (object instanceof Project) {
            return "Project";
        }

        if (object instanceof Collection) {
            return "Collection";
        }

        if (object instanceof DataItem) {
            return "Data Item";
        }

        if (object instanceof MetadataFile) {
            return "Metadata File";
        }

        if (object instanceof DataFile) {
            return "File";
        }

        return null;
    }

    public BusinessObjectMap(String id) {
        this.id = id;
        this.alternateIds = new ArrayList<String>(1);
        this.children = new ArrayList<BusinessObjectMap>(2);
    }

    /**
     * Attempt to infer the name and type of the business object.
     * 
     * @param object
     */
    public BusinessObjectMap(BusinessObject object) {
        this(object.getId());

        this.name = getName(object);
        this.type = getType(object);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDepositStatus() {
        return depositStatus;
    }
    
    public void setDepositStatus(String depositStatus) {
        this.depositStatus = depositStatus;
    }

    /**
     * @return list of alternate ids
     */
    public List<String> getAlternateIds() {
        return alternateIds;
    }

    /**
     * @return list of children
     */
    public List<BusinessObjectMap> getChildren() {
        return children;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((depositStatus == null) ? 0 : depositStatus.hashCode());
        return result;
    }

    @Override
    /**
     * Child in order does not matter for equality.
     */
    public boolean equals(Object obj) {
    	String thisId = (id!=null)?id:"???";
    	String thatId = (obj!=null && obj instanceof BusinessObjectMap)?((BusinessObjectMap)obj).getId():"!!!"; 
    	log.debug("Comparing {} to {}", thisId, thatId);
    	
    	if (this == obj) {
        	log.debug("Object references are the same, they are equal!");
            return true;
        }
        if (obj == null) {
        	log.debug("Other reference is null, maps are not equal!");
            return false;
        }
        if (getClass() != obj.getClass()) {
        	log.debug("Class types differ, maps are not equal!");
            return false;
        }
        BusinessObjectMap other = (BusinessObjectMap) obj;
        if (alternateIds == null) {
            if (other.alternateIds != null) {
            	log.debug("This alternate id is null, the other is not; maps are not equal");
                return false;
            }
        } else if (!alternateIds.equals(other.alternateIds)) {
        	log.debug("Alternate ids differ, so maps are not equal");
            return false;
        }
        if (children == null) {
            if (other.children != null) {
            	log.debug("This children list is empty, the other is not; maps are not equal");
                return false;
            }
        } else if (!new HashSet<BusinessObjectMap>(children).equals(new HashSet<BusinessObjectMap>(other.children))) {
        	log.debug("Children differ, so maps are not equal");
        	return false;
        }
        if (id == null) {
            if (other.id != null) {
            	log.debug("This id is null, the other is not; maps are not equal");
                return false;
            }
        } else if (!id.equals(other.id)) {
        	log.debug("Ids differ, so maps are not equal");
            return false;
        }
        if (name == null) {
            if (other.name != null) {
            	log.debug("This name is null, the other is not; maps are not equal");
                return false;
            }
        } else if (!name.equals(other.name)) {
        	log.debug("Names differ, so maps are not equal");
            return false;
        }
        if (type == null) {
            if (other.type != null) {
            	log.debug("This type is null, the other is not; maps are not equal");
                return false;
            }
        } else if (!type.equals(other.type)) {
        	log.debug("Types differ, so maps are not equal");
            return false;
        }
        if (depositStatus == null) {
            if (other.depositStatus != null) {
            	log.debug("This deposit status is null, the other is not; maps are not equal");
                return false;
            }
        } else if (!depositStatus.equals(other.depositStatus)) {
        	log.debug("Depost statuses differ, so maps are not equal.");
            return false;
        }
        log.debug("Maps are equal!");
        return true;
    }

    @Override
    public String toString() {
        return "BusinessObjectMap [id=" + id + ", name=" + name + ", type=" + type + ", depositStatus=" + depositStatus
                + ", alternateIds=" + alternateIds + ", children=" + children + "]";
    }

}
