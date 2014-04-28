package org.seadva.registry.database.model.obj.vaRegistry;

import com.felees.hbnpojogen.persistence.IPojoGenEntity;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.gson.annotations.Expose;
import org.hibernate.proxy.HibernateProxy;
import org.seadva.registry.database.enums.subtype.vaRegistry.BaseEntitySubclassType;
import org.seadva.registry.database.model.obj.vaRegistry.Aggregation;
import org.seadva.registry.database.model.obj.vaRegistry.DataIdentifier;
import org.seadva.registry.database.model.obj.vaRegistry.DataLocation;
import org.seadva.registry.database.model.obj.vaRegistry.EntityContent;
import org.seadva.registry.database.model.obj.vaRegistry.MetadataReference;
import org.seadva.registry.database.model.obj.vaRegistry.Property;
import org.seadva.registry.database.model.obj.vaRegistry.Relation;
import org.seadva.registry.database.model.obj.vaRegistry.iface.IBaseEntity;
import org.springframework.test.context.transaction.TransactionConfiguration;


/** 
 * Object mapping for hibernate-handled table: base_entity.
 * @author autogenerated
 */

@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "base_entity", catalog = "va_registry")

public class BaseEntity implements Cloneable, Serializable, IPojoGenEntity, IBaseEntity {

	/** Serial Version UID. */
	private static final long serialVersionUID = -559002650L;

	/** Use a WeakHashMap so entries will be garbage collected once all entities 
		referring to a saved hash are garbage collected themselves. */
	private static final Map<Serializable, String> SAVED_HASHES =
		Collections.synchronizedMap(new WeakHashMap<Serializable, String>());
	
	/** hashCode temporary storage. */
	private volatile String hashCode;
	

	/** Field mapping. */
	//private Set<Aggregation> aggregations = new HashSet<Aggregation>();

	/** Field mapping. */
    @Expose
	private Set<DataIdentifier> dataIdentifiers = new HashSet<DataIdentifier>();

	/** Field mapping. */
    @Expose
	private Set<DataLocation> dataLocations = new HashSet<DataLocation>();

	/** Field mapping. */
	private Set<EntityContent> entityContents = new HashSet<EntityContent>();

	/** Field mapping. */
    @Expose
	private Date entityCreatedTime;
	/** Field mapping. */
    @Expose
	private Date entityLastUpdatedTime;
	/** Field mapping. */
    @Expose
	private String entityName;
	/** Field mapping. */
    @Expose
	private String id;
	/** Field mapping. */
	private Set<MetadataReference> metadataReferences = new HashSet<MetadataReference>();

	/** Field mapping. */
    @Expose
	private Set<Property> properties = new HashSet<Property>();

	/** Field mapping. */
	private Set<Relation> relations = new HashSet<Relation>();

	/**
	 * Default constructor, mainly for hibernate use.
	 */
	public BaseEntity() {
		// Default constructor
	} 

	/** Constructor taking a given ID.
	 * @param id to set
	 */
	public BaseEntity(String id) {
		this.id = id;
	}
	
	/** Constructor taking a given ID.
	 * @param entityCreatedTime Date object;
	 * @param entityLastUpdatedTime Date object;
	 * @param entityName String object;
	 * @param id String object;
	 */
	public BaseEntity(Date entityCreatedTime, Date entityLastUpdatedTime, String entityName, 					
			String id) {

		this.entityCreatedTime = entityCreatedTime;
		this.entityLastUpdatedTime = entityLastUpdatedTime;
		this.entityName = entityName;
		this.id = id;
	}
	
 


 	/**
 	 * Return an enum of the type of this subclass. This is useful to be able to use switch/case in your code.
 	 *
 	 * @return BaseEntitySubclassType enum.
 	 */
 	@Transient
 	public BaseEntitySubclassType getBaseEntitySubclassType() {
		return BaseEntitySubclassType.NOT_A_CHILD;
 	}  
  
	/** Return the type of this class. Useful for when dealing with proxies.
	* @return Defining class.
	*/
	@Transient
	public Class<?> getClassType() {
		return BaseEntity.class;
	}
 

    /**
     * Return the value associated with the column: aggregation.
	 * @return A Set&lt;Aggregation&gt; object (this.aggregation)
	 */
 	/*@OneToMany( fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "id.child"  )
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	@Basic( optional = false )
	@Column( name = "entity_id", nullable = false  )
	public Set<Aggregation> getAggregations() {
		return this.aggregations;
		
	}*/
	
	/**
	 * Adds a bi-directional link of type Aggregation to the aggregations set.
	 * @param aggregation item to add
	 */
	/*public void addAggregation(Aggregation aggregation) {
		this.aggregations.add(aggregation);
	}*/

  
    /**  
     * Set the value related to the column: aggregation.
	 * @param aggregation the aggregation value you wish to set
	 */
/*	public void setAggregations(final Set<Aggregation> aggregation) {
		this.aggregations = aggregation;
	}*/

    /**
     * Return the value associated with the column: dataIdentifier.
	 * @return A Set&lt;DataIdentifier&gt; object (this.dataIdentifier)
	 */
 	@OneToMany( fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "id.entity"  )
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	@Basic( optional = false )
	@Column( name = "entity_id", nullable = false  )
	public Set<DataIdentifier> getDataIdentifiers() {
		return this.dataIdentifiers;
		
	}
	
	/**
	 * Adds a bi-directional link of type DataIdentifier to the dataIdentifiers set.
	 * @param dataIdentifier item to add
	 */
	public void addDataIdentifier(DataIdentifier dataIdentifier) {
		dataIdentifier.getId().setEntity(this);
		this.dataIdentifiers.add(dataIdentifier);
	}

  
    /**  
     * Set the value related to the column: dataIdentifier.
	 * @param dataIdentifier the dataIdentifier value you wish to set
	 */
	public void setDataIdentifiers(final Set<DataIdentifier> dataIdentifier) {
		this.dataIdentifiers = dataIdentifier;
	}

    /**
     * Return the value associated with the column: dataLocation.
	 * @return A Set&lt;DataLocation&gt; object (this.dataLocation)
	 */
 	@OneToMany( fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "id.entity"  )
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	@Basic( optional = false )
	@Column( name = "entity_id", nullable = false  )
	public Set<DataLocation> getDataLocations() {
		return this.dataLocations;
		
	}
	
	/**
	 * Adds a bi-directional link of type DataLocation to the dataLocations set.
	 * @param dataLocation item to add
	 */
	public void addDataLocation(DataLocation dataLocation) {
		dataLocation.getId().setEntity(this);
		this.dataLocations.add(dataLocation);
	}

  
    /**  
     * Set the value related to the column: dataLocation.
	 * @param dataLocation the dataLocation value you wish to set
	 */
	public void setDataLocations(final Set<DataLocation> dataLocation) {
		this.dataLocations = dataLocation;
	}

    /**
     * Return the value associated with the column: entityContent.
	 * @return A Set&lt;EntityContent&gt; object (this.entityContent)
	 */
 	@OneToMany( fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "entity"  )
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	@Basic( optional = false )
	@Column( name = "entity_id", nullable = false  )
	public Set<EntityContent> getEntityContents() {
		return this.entityContents;
		
	}
	
	/**
	 * Adds a bi-directional link of type EntityContent to the entityContents set.
	 * @param entityContent item to add
	 */
	public void addEntityContent(EntityContent entityContent) {
		entityContent.setEntity(this);
		this.entityContents.add(entityContent);
	}

  
    /**  
     * Set the value related to the column: entityContent.
	 * @param entityContent the entityContent value you wish to set
	 */
	public void setEntityContents(final Set<EntityContent> entityContent) {
		this.entityContents = entityContent;
	}

    /**
     * Return the value associated with the column: entityCreatedTime.
	 * @return A Date object (this.entityCreatedTime)
	 */
	@Basic( optional = false )
	@Column( name = "entity_created_time", nullable = false  )
	public Date getEntityCreatedTime() {
		return this.entityCreatedTime;
		
	}
	

  
    /**  
     * Set the value related to the column: entityCreatedTime.
	 * @param entityCreatedTime the entityCreatedTime value you wish to set
	 */
	public void setEntityCreatedTime(final Date entityCreatedTime) {
		this.entityCreatedTime = entityCreatedTime;
	}

    /**
     * Return the value associated with the column: entityLastUpdatedTime.
	 * @return A Date object (this.entityLastUpdatedTime)
	 */
	@Basic( optional = false )
	@Column( name = "entity_last_updated_time", nullable = false  )
	public Date getEntityLastUpdatedTime() {
		return this.entityLastUpdatedTime;
		
	}
	

  
    /**  
     * Set the value related to the column: entityLastUpdatedTime.
	 * @param entityLastUpdatedTime the entityLastUpdatedTime value you wish to set
	 */
	public void setEntityLastUpdatedTime(final Date entityLastUpdatedTime) {
		this.entityLastUpdatedTime = entityLastUpdatedTime;
	}

    /**
     * Return the value associated with the column: entityName.
	 * @return A String object (this.entityName)
	 */
	@Basic( optional = false )
	@Column( name = "entity_name", nullable = false, length = 256  )
	public String getEntityName() {
		return this.entityName;
		
	}
	

  
    /**  
     * Set the value related to the column: entityName.
	 * @param entityName the entityName value you wish to set
	 */
	public void setEntityName(final String entityName) {
		this.entityName = entityName;
	}

    /**
     * Return the value associated with the column: id.
	 * @return A String object (this.id)
	 */
    @Id 
	@Basic( optional = false )
	@Column( name = "entity_id", nullable = false, length = 127  )
	public String getId() {
		return this.id;
		
	}
	

  
    /**  
     * Set the value related to the column: id.
	 * @param id the id value you wish to set
	 */
	public void setId(final String id) {
		// If we've just been persisted and hashCode has been
		// returned then make sure other entities with this
		// ID return the already returned hash code
		if ( (this.id == null ) &&
				(id != null) &&
				(this.hashCode != null) ) {
		SAVED_HASHES.put( id, this.hashCode );
		}
		this.id = id;
	}

    /**
     * Return the value associated with the column: metadataReference.
	 * @return A Set&lt;MetadataReference&gt; object (this.metadataReference)
	 */
 	@OneToMany( fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "objectEntity"  )
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	@Basic( optional = false )
	@Column( name = "entity_id", nullable = false  )
	public Set<MetadataReference> getMetadataReferences() {
		return this.metadataReferences;
		
	}
	
	/**
	 * Adds a bi-directional link of type MetadataReference to the metadataReferences set.
	 * @param metadataReference item to add
	 */
	public void addMetadataReference(MetadataReference metadataReference) {
		metadataReference.setObjectEntity(this);
		this.metadataReferences.add(metadataReference);
	}

  
    /**  
     * Set the value related to the column: metadataReference.
	 * @param metadataReference the metadataReference value you wish to set
	 */
	public void setMetadataReferences(final Set<MetadataReference> metadataReference) {
		this.metadataReferences = metadataReference;
	}

    /**
     * Return the value associated with the column: property.
	 * @return A Set&lt;Property&gt; object (this.property)
	 */
 	@OneToMany( fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "entity"  )
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	@Basic( optional = false )
	@Column( name = "entity_id", nullable = false  )
	public Set<Property> getProperties() {
		return this.properties;
		
	}
	
	/**
	 * Adds a bi-directional link of type Property to the properties set.
	 * @param property item to add
	 */
	public void addProperty(Property property) {
		property.setEntity(this);
		this.properties.add(property);
	}

  
    /**  
     * Set the value related to the column: property.
	 * @param property the property value you wish to set
	 */
	public void setProperties(final Set<Property> property) {
		this.properties = property;
	}

    /**
     * Return the value associated with the column: relation.
	 * @return A Set&lt;Relation&gt; object (this.relation)
	 */
 	@OneToMany( fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "id.cause"  )
 	@org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
	@Basic( optional = false )
	@Column( name = "entity_id", nullable = false  )
	public Set<Relation> getRelations() {
		return this.relations;
		
	}
	
	/**
	 * Adds a bi-directional link of type Relation to the relations set.
	 * @param relation item to add
	 */
	public void addRelation(Relation relation) {
		relation.getId().setCause(this);
		this.relations.add(relation);
	}

  
    /**  
     * Set the value related to the column: relation.
	 * @param relation the relation value you wish to set
	 */
	public void setRelations(final Set<Relation> relation) {
		this.relations = relation;
	}


   /**
    * Deep copy.
	* @return cloned object
	* @throws CloneNotSupportedException on error
    */
    @Override
    public BaseEntity clone() throws CloneNotSupportedException {
		
        final BaseEntity copy = (BaseEntity)super.clone();

		/*if (this.getAggregations() != null) {
			copy.getAggregations().addAll(this.getAggregations());
		}*/
		if (this.getDataIdentifiers() != null) {
			copy.getDataIdentifiers().addAll(this.getDataIdentifiers());
		}
		if (this.getDataLocations() != null) {
			copy.getDataLocations().addAll(this.getDataLocations());
		}
		if (this.getEntityContents() != null) {
			copy.getEntityContents().addAll(this.getEntityContents());
		}
		copy.setEntityCreatedTime(this.getEntityCreatedTime());
		copy.setEntityLastUpdatedTime(this.getEntityLastUpdatedTime());
		copy.setEntityName(this.getEntityName());
		copy.setId(this.getId());
		if (this.getMetadataReferences() != null) {
			copy.getMetadataReferences().addAll(this.getMetadataReferences());
		}
		if (this.getProperties() != null) {
			copy.getProperties().addAll(this.getProperties());
		}
		if (this.getRelations() != null) {
			copy.getRelations().addAll(this.getRelations());
		}
		return copy;
	}
	


	/** Provides toString implementation.
	 * @see Object#toString()
	 * @return String representation of this class.
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("entityCreatedTime: " + this.getEntityCreatedTime() + ", ");
		sb.append("entityLastUpdatedTime: " + this.getEntityLastUpdatedTime() + ", ");
		sb.append("entityName: " + this.getEntityName() + ", ");
		sb.append("id: " + this.getId() + ", ");
		return sb.toString();		
	}


	/** Equals implementation. 
	 * @see Object#equals(Object)
	 * @param aThat Object to compare with
	 * @return true/false
	 */
	@Override
	public boolean equals(final Object aThat) {
		Object proxyThat = aThat;
		
		if ( this == aThat ) {
			 return true;
		}

		
		if (aThat instanceof HibernateProxy) {
 			// narrow down the proxy to the class we are dealing with.
 			try {
				proxyThat = ((HibernateProxy) aThat).getHibernateLazyInitializer().getImplementation(); 
			} catch (org.hibernate.ObjectNotFoundException e) {
				return false;
		   	}
		}
		if (aThat == null)  {
			 return false;
		}
		
		final BaseEntity that; 
		try {
			that = (BaseEntity) proxyThat;
			if ( !(that.getClassType().equals(this.getClassType()))){
				return false;
			}
		} catch (org.hibernate.ObjectNotFoundException e) {
				return false;
		} catch (ClassCastException e) {
				return false;
		}
		
		
		boolean result = true;
		result = result && (((this.getId() == null) && ( that.getId() == null)) || (this.getId() != null  && this.getId().equals(that.getId())));
		result = result && (((getEntityCreatedTime() == null) && (that.getEntityCreatedTime() == null)) || (getEntityCreatedTime() != null && getEntityCreatedTime().equals(that.getEntityCreatedTime())));
		result = result && (((getEntityLastUpdatedTime() == null) && (that.getEntityLastUpdatedTime() == null)) || (getEntityLastUpdatedTime() != null && getEntityLastUpdatedTime().equals(that.getEntityLastUpdatedTime())));
		result = result && (((getEntityName() == null) && (that.getEntityName() == null)) || (getEntityName() != null && getEntityName().equals(that.getEntityName())));
		return result;
	}
	
	/** Calculate the hashcode.
	 * @see Object#hashCode()
	 * @return a calculated number
	 */
	@Override
	public int hashCode() {
		if ( this.hashCode == null ) {
			synchronized ( this ) {
				if ( this.hashCode == null ) {
					String newHashCode = null;

					if ( getId() != null ) {
					newHashCode = SAVED_HASHES.get( getId() );
					}
					
					if ( newHashCode == null ) {
						if ( getId() != null ) {
							newHashCode = getId();
						} else {
						newHashCode = String.valueOf(super.hashCode());

						}
					}
					
					this.hashCode = newHashCode;
				}
			}
		}
	return this.hashCode.hashCode();
	}
	

	
}
