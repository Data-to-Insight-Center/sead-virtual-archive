package org.seadva.registry.database.model.obj.vaRegistry;

import com.felees.hbnpojogen.persistence.IPojoGenEntity;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.seadva.registry.database.model.obj.vaRegistry.iface.IEntityType;


/** 
 * Object mapping for hibernate-handled table: entity_type.
 * @author autogenerated
 */

@Entity
@Table(name = "entity_type", catalog = "va_registry")
public class EntityType implements Cloneable, Serializable, IPojoGenEntity, IEntityType {

	/** Serial Version UID. */
	private static final long serialVersionUID = -559002640L;

	

	/** Field mapping. */
	@Column( name = "entity_type_name", nullable = false, length = 256  )
	private String entityTypeName;

	/** Field mapping. */
	@Id 
	private EntityTypePK id;

	/**
	 * Default constructor, mainly for hibernate use.
	 */
	public EntityType() {
		// Default constructor
	} 

	/** Constructor taking a given ID.
	 * @param id to set
	 */
	public EntityType(EntityTypePK id) {
		this.id = id;
	}
	
	/** Constructor taking a given ID.
	 * @param entityTypeName String object;
	 * @param id EntityTypePK object;
	 */
	public EntityType(String entityTypeName, EntityTypePK id) {

		this.entityTypeName = entityTypeName;
		this.id = id;
	}
	
 


 
	/** Return the type of this class. Useful for when dealing with proxies.
	* @return Defining class.
	*/
	@Transient
	public Class<?> getClassType() {
		return EntityType.class;
	}
 

    /**
     * Return the value associated with the column: entityTypeName.
	 * @return A String object (this.entityTypeName)
	 */
	@Basic( optional = false )
	@Column( name = "entity_type_name", nullable = false, length = 256  )
	public String getEntityTypeName() {
		return this.entityTypeName;
		
	}
	

  
    /**  
     * Set the value related to the column: entityTypeName.
	 * @param entityTypeName the entityTypeName value you wish to set
	 */
	public void setEntityTypeName(final String entityTypeName) {
		this.entityTypeName = entityTypeName;
	}

    /**
     * Return the value associated with the column: id.
	 * @return A EntityTypePK object (this.id)
	 */
	public EntityTypePK getId() {
		return this.id;
		
	}
	

  
    /**  
     * Set the value related to the column: id.
	 * @param id the id value you wish to set
	 */
	public void setId(final EntityTypePK id) {
		this.id = id;
	}


   /**
    * Deep copy.
	* @return cloned object
	* @throws CloneNotSupportedException on error
    */
    @Override
    public EntityType clone() throws CloneNotSupportedException {
		
        final EntityType copy = (EntityType)super.clone();

		copy.setEntityTypeName(this.getEntityTypeName());
		copy.setId(this.getId());
		return copy;
	}
	


	/** Provides toString implementation.
	 * @see Object#toString()
	 * @return String representation of this class.
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("entityTypeName: " + this.getEntityTypeName() + ", ");
		sb.append("id: " + this.getId());
		return sb.toString();		
	}


	/** Equals implementation. 
	 * @see Object#equals(Object)
	 * @param aThat Object to compare with
	 * @return true/false
	 */
	@Override
	public boolean equals(final Object aThat) {
		if ( this == aThat ) {
			 return true;
		}

		if ((aThat == null) || ( !(aThat.getClass().equals(this.getClass())))) {
			 return false;
		}
	
		final EntityType that = (EntityType) aThat;
		return this.getId().equals(that.getId());
	}
	
	/** Calculate the hashcode.
	 * @see Object#hashCode()
	 * @return a calculated number
	 */
	@Override
	public int hashCode() {
		return getId().hashCode();
	}
	

	
}