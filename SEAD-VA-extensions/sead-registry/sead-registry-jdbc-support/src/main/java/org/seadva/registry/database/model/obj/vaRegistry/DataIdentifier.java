package org.seadva.registry.database.model.obj.vaRegistry;

import com.felees.hbnpojogen.persistence.IPojoGenEntity;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.gson.annotations.Expose;
import org.seadva.registry.database.model.obj.vaRegistry.iface.IDataIdentifier;


/** 
 * Object mapping for hibernate-handled table: data_identifier.
 * @author autogenerated
 */

@Entity
@Table(name = "data_identifier", catalog = "va_registry")
public class DataIdentifier implements Cloneable, Serializable, IPojoGenEntity, IDataIdentifier {

	/** Serial Version UID. */
	private static final long serialVersionUID = -559002648L;

	

	/** Field mapping. */
    @Expose
	@Column( name = "data_identifier_value", nullable = false, length = 256  )
	private String dataIdentifierValue;

	/** Field mapping. */
    @Expose
	@Id 
	private DataIdentifierPK id;

	/**
	 * Default constructor, mainly for hibernate use.
	 */
	public DataIdentifier() {
		// Default constructor
	} 

	/** Constructor taking a given ID.
	 * @param id to set
	 */
	public DataIdentifier(DataIdentifierPK id) {
		this.id = id;
	}
	
	/** Constructor taking a given ID.
	 * @param dataIdentifierValue String object;
	 * @param id DataIdentifierPK object;
	 */
	public DataIdentifier(String dataIdentifierValue, DataIdentifierPK id) {

		this.dataIdentifierValue = dataIdentifierValue;
		this.id = id;
	}
	
 


 
	/** Return the type of this class. Useful for when dealing with proxies.
	* @return Defining class.
	*/
	@Transient
	public Class<?> getClassType() {
		return DataIdentifier.class;
	}
 

    /**
     * Return the value associated with the column: dataIdentifierValue.
	 * @return A String object (this.dataIdentifierValue)
	 */
	@Basic( optional = false )
	@Column( name = "data_identifier_value", nullable = false, length = 256  )
	public String getDataIdentifierValue() {
		return this.dataIdentifierValue;
		
	}
	

  
    /**  
     * Set the value related to the column: dataIdentifierValue.
	 * @param dataIdentifierValue the dataIdentifierValue value you wish to set
	 */
	public void setDataIdentifierValue(final String dataIdentifierValue) {
		this.dataIdentifierValue = dataIdentifierValue;
	}

    /**
     * Return the value associated with the column: id.
	 * @return A DataIdentifierPK object (this.id)
	 */
	public DataIdentifierPK getId() {
		return this.id;
		
	}
	

  
    /**  
     * Set the value related to the column: id.
	 * @param id the id value you wish to set
	 */
	public void setId(final DataIdentifierPK id) {
		this.id = id;
	}


   /**
    * Deep copy.
	* @return cloned object
	* @throws CloneNotSupportedException on error
    */
    @Override
    public DataIdentifier clone() throws CloneNotSupportedException {
		
        final DataIdentifier copy = (DataIdentifier)super.clone();

		copy.setDataIdentifierValue(this.getDataIdentifierValue());
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
		
		sb.append("dataIdentifierValue: " + this.getDataIdentifierValue() + ", ");
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
	
		final DataIdentifier that = (DataIdentifier) aThat;
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
