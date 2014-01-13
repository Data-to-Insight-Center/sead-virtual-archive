/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seadva.access.security.model;

import org.seadva.access.security.RegistrationStatus;

public class Person {
	private String firstName;
	private String lastName;
	private String password;
	
	private String emailAddress;
	private String phoneNumber;
	
	private RegistrationStatus registrationStatus;
	private boolean readOnly = false;

    private Role role;
   


    /**
     * Creates a new Person with no state.
     */
    public Person() {

    }
    
    /**
     * Creates a new Person {@link #equals(Object) equal} to {@code toCopy}.
     *
     * @param toCopy the Person to copy, must not be {@code null}
     * @throws IllegalArgumentException if {@code toCopy} is {@code null}.
     */
    public Person(Person toCopy) {
        this.firstName = toCopy.getFirstName();
        this.lastName = toCopy.getLastName();
        this.password = toCopy.getPassword();
        this.emailAddress = toCopy.getEmailAddress();
        this.phoneNumber = toCopy.getPhoneNumber();
        this.registrationStatus = toCopy.getRegistrationStatus();
        // Defensively copy the collection
        this.role = toCopy.getRole(); 
        this.readOnly = toCopy.getReadOnly();
    }

    public RegistrationStatus getRegistrationStatus() {
		return registrationStatus;
	}
	public void setRegistrationStatus(RegistrationStatus registrationStatus) {
		this.registrationStatus = registrationStatus;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setReadOnly(boolean readOnly) {
	    this.readOnly = readOnly;
	}
	 
	public boolean getReadOnly(){
	    return this.readOnly;
	}
	
   
    
   

    public Role getRole() {
        return role;
    }

    /**
     * Sets the roles for this person.  Removes any existing roles.
     *
     * @param roles the roles to set for this person
     * @return void
     * @throws IllegalArgumentException if the list is null.
     */
    public void setRole(Role newRole) throws IllegalArgumentException {
        this.role = newRole; 
    }

      


    // We can edit this later to output what we want
    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", registrationStatus=" + registrationStatus +
                ", role=" + role +
                ", readOnly=" + readOnly +
                '}';
    }

    /**
     * Determines if this Person is equal to another object.
     * <p/>
     * Currently equality is determined by comparing the value of the following fields:
     * <ul>
     *     <li>First Name</li>
     *     <li>Last Name</li>
     *     <li>Email Address</li>
     *     <li>Password</li>
     *     <li>Phone Number</li>
     * </ul>
     *
     * @param o the object to test for equality
     * @return true if the objects are equal
     */

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Person)) {
            return false;
        }

        Person p = (Person) o;

        return equals(getFirstName(), p.getFirstName()) && equals(getLastName(), p.getLastName()) &&
            equals(getEmailAddress(), p.getEmailAddress()) && equals(getPassword(), p.getPassword()) &&
            equals(getPhoneNumber(), p.getPhoneNumber()) && equals(getReadOnly(), p.getReadOnly());
    }

    private static boolean equals(Object o1, Object o2) {
        return (o1 == null && o2 == null)
                || (o1 != null && o2 != null && o1.equals(o2));
    }

    
    /**
     * Calculates a hash code using the fields that are considered for {@link #equals(Object) equality}.
     *
     * @return the hashcode
     */
    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (emailAddress != null ? emailAddress.hashCode() : 0);
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        return result;
    }
}
