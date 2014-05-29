package org.seadva.registry.database.model.obj.vaRegistry.iface;
import javax.persistence.Basic;
import org.seadva.registry.database.model.obj.vaRegistry.Agent;
import org.seadva.registry.database.model.obj.vaRegistry.ProfileType;


/** 
 * Object interface mapping for hibernate-handled table: agent_profile.
 * @author autogenerated
 */

public interface IAgentProfilePK {



    /**
     * Return the value associated with the column: agent.
	 * @return A Agent object (this.agent)
	 */
	Agent getAgent();
	

  
    /**  
     * Set the value related to the column: agent.
	 * @param agent the agent value you wish to set
	 */
	void setAgent(final Agent agent);

    /**
     * Return the value associated with the column: profileType.
	 * @return A ProfileType object (this.profileType)
	 */
	ProfileType getProfileType();
	

  
    /**  
     * Set the value related to the column: profileType.
	 * @param profileType the profileType value you wish to set
	 */
	void setProfileType(final ProfileType profileType);

	// end of interface
}