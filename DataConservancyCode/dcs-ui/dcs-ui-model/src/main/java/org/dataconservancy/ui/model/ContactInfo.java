/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.ui.model;

/**
 * Instances of this class encapsulate contact information
 */
public class ContactInfo {
	private String name;
	private String role;
	private String emailAddress;
	private String phoneNumber;
	private Address physicalAddress;
	
	
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
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Address getPhysicalAddress() {
		return physicalAddress;
	}
	public void setPhysicalAddress(Address physicalAddress) {
		this.physicalAddress = physicalAddress;
	}
	public ContactInfo() {
		super();
	}
	public ContactInfo(String role, String name, String emailAddress,
			String phoneNumber, Address physicalAddress) {
		super();
		this.role = role;
		this.name = name;
		this.emailAddress = emailAddress;
		this.phoneNumber = phoneNumber;
		this.physicalAddress = physicalAddress;
	}
	public ContactInfo(ContactInfo contactInfo) {
		super();
		this.role = contactInfo.role;
		this.name = contactInfo.name;
		this.emailAddress = contactInfo.emailAddress;
		this.phoneNumber = contactInfo.phoneNumber;
		this.physicalAddress = contactInfo.physicalAddress;
	}
	@Override
	public String toString() {
		return "ContactInfo [role=" + role + ", name=" + name
				+ ", emailAddress=" + emailAddress + ", phoneNumber="
				+ phoneNumber + ", physicalAddress=" + physicalAddress + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((emailAddress == null) ? 0 : emailAddress.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		result = prime * result
				+ ((physicalAddress == null) ? 0 : physicalAddress.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContactInfo other = (ContactInfo) obj;
		if (emailAddress == null) {
			if (other.emailAddress != null)
				return false;
		} else if (!emailAddress.equals(other.emailAddress))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (phoneNumber == null) {
			if (other.phoneNumber != null)
				return false;
		} else if (!phoneNumber.equals(other.phoneNumber))
			return false;
		if (physicalAddress == null) {
			if (other.physicalAddress != null)
				return false;
		} else if (!physicalAddress.equals(other.physicalAddress))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		return true;
	}
	
	
	
}
