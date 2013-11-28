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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Instances of {@link Person} class represent users of the system.
 */
public class Person
        extends BusinessObject
        implements Comparable<Person> {

    private String[] firstNames;

    private String[] lastNames;

    private String[] middleNames;

    private String prefix;

    private String suffix;

    private String password;

    private String emailAddress;

    private String phoneNumber;

    private String jobTitle;

    private String city;

    private String state;

    private String department;

    private String instCompany;
    
    private String instCompanyWebsite;

    private String website;

    private String bio;

    private String preferredPubName;

    private RegistrationStatus registrationStatus;

    private boolean readOnly = false;

    private List<Role> roles = new ArrayList<Role>();

    private Comparator<Person> comparator;
    
    private boolean externalStorageLinked;
    
    private String dropboxAppKey;
    
    private String dropboxAppSecret;

    /**
     * Creates a new Person with no state.
     */
    public Person() {

    }

    /**
     * Creates a new Person {@link #equals(Object) equal} to {@code toCopy}.
     * 
     * @param toCopy
     *        the Person to copy, must not be {@code null}
     * @throws IllegalArgumentException
     *         if {@code toCopy} is {@code null}.
     */
    public Person(Person toCopy) {
        this.id = toCopy.getId();
        this.firstNames = toCopy.getFirstNamesAsArray();
        this.lastNames = toCopy.getLastNamesAsArray();
        this.middleNames = toCopy.getMiddleNamesAsArray();
        this.prefix = toCopy.getPrefix();
        this.suffix = toCopy.getSuffix();
        this.preferredPubName = toCopy.getPreferredPubName();
        this.bio = toCopy.getBio();
        this.website = toCopy.getWebsite();
        this.password = toCopy.getPassword();
        this.emailAddress = toCopy.getEmailAddress();
        this.phoneNumber = toCopy.getPhoneNumber();
        this.jobTitle = toCopy.getJobTitle();
        this.city = toCopy.getCity();
        this.state = toCopy.getState();
        this.department = toCopy.getDepartment();
        this.instCompany = toCopy.getInstCompany();
        this.instCompanyWebsite = toCopy.getInstCompanyWebsite();
        this.registrationStatus = toCopy.getRegistrationStatus();
        // Defensively copy the collection
        this.roles = new ArrayList<Role>();
        for (Role r : toCopy.getRoles()) {
            this.roles.add(r);
        }
        this.comparator = toCopy.getComparator();
        this.readOnly = toCopy.getReadOnly();
        this.externalStorageLinked = toCopy.isExternalStorageLinked();
        this.dropboxAppKey = toCopy.getDropboxAppKey();
        this.dropboxAppSecret = toCopy.getDropboxAppSecret();
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

    /**
     * Sets the first names array based on the string provided
     * 
     * @param firstNames
     *        A space delimited string of the first names.
     */
    public void setFirstNames(String firstNames) {
        if (firstNames != null) {
            this.firstNames = firstNames.split("\\s+");
        }
    }

    /**
     * Sets the first names array to the array provided
     * 
     * @param firstNames
     *        An array representing the first names.
     */
    public void setFirstNamesAsArray(String[] firstNames) {
        if (firstNames != null) {
            this.firstNames = firstNames;
        }
    }

    /**
     * Gets the first names in array form.
     * 
     * @return The string array of the first names.
     */
    public String[] getFirstNamesAsArray() {
        return firstNames;
    }

    /**
     * Gets the first names in a space delimited string.
     * 
     * @return names - A space delimited string of the first names.
     */
    public String getFirstNames() {
        String names = "";
        if (firstNames != null) {
            for (int i = 0; i < firstNames.length; i++) {
                names += firstNames[i];
                if (i != firstNames.length - 1) {
                    names += " ";
                }
            }
        }

        return names;
    }

    /**
     * Sets the last names based on the string provided.
     * 
     * @param lastNames
     *        A spaced delimited string of the last names.
     */
    public void setLastNames(String lastNames) {
        if (lastNames != null) {
            this.lastNames = lastNames.split("\\s+");
        }
    }

    /**
     * Sets the last names to the array provided.
     * 
     * @param lastNames
     *        A string array containing the last names.
     */
    public void setLastNamesAsArray(String[] lastNames) {
        this.lastNames = lastNames;
    }

    /**
     * Gets the last names as a string array.
     * 
     * @return A string array holding the last names.
     */
    public String[] getLastNamesAsArray() {
        return lastNames;
    }

    /**
     * Gets the last names as a space delimited string.
     * 
     * @return A space delimited string of all the last names.
     */
    public String getLastNames() {
        String names = "";
        if (lastNames != null) {
            for (int i = 0; i < lastNames.length; i++) {
                names += lastNames[i];
                if (i != lastNames.length - 1) {
                    names += " ";
                }
            }
        }

        return names;
    }

    /**
     * Sets the array of middle names based on the string provided.
     * 
     * @param middleNames
     *        A space delimited string of the middle names.
     */
    public void setMiddleNames(String middleNames) {
        if (middleNames != null) {
            this.middleNames = middleNames.split("\\s+");
        }
    }

    /**
     * Sets the array of middle names to array provided.
     * 
     * @param middleNames
     *        A string array containing the middle names.
     */
    public void setMiddleNamesAsArray(String[] middleNames) {
        this.middleNames = middleNames;
    }

    /**
     * Gets the middle names as a string array.
     * 
     * @return The string array containing the middle names.
     */
    public String[] getMiddleNamesAsArray() {
        return middleNames;
    }

    /**
     * Gets the middle names as a space delimited string.
     * 
     * @return A space delimited string of all the middle names.
     */
    public String getMiddleNames() {
        String names = "";
        if (middleNames != null) {
            for (int i = 0; i < middleNames.length; i++) {
                names += middleNames[i];
                if (i != middleNames.length - 1) {
                    names += " ";
                }
            }
        }

        return names;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean getReadOnly() {
        return this.readOnly;
    }

    public Comparator<Person> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<Person> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compareTo(Person person) {
        return comparator.compare(this, person);
    }

    public List<Role> getRoles() {
        return roles;
    }

    /**
     * Sets the roles for this person. Removes any existing roles.
     * 
     * @param roles
     *        the roles to set for this person
     * @throws IllegalArgumentException
     *         if the list is null.
     */
    public void setRoles(List<Role> roles) throws IllegalArgumentException {
        if (this.roles == null) {
            throw new IllegalArgumentException("Roles must not be null.");
        }
        this.roles = roles;
    }

    /**
     * Add a role to this Person.
     * 
     * @param role
     *        the role to add
     * @return this Person
     * @throws IllegalArgumentException
     *         if the role is null
     */
    public Person addRole(Role role) throws IllegalArgumentException {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
        roles.add(role);
        return this;
    }

    // We can edit this later to output what we want
    @Override
    public String toString() {
        return "Person{" + "id='" + id + '\'' + ", firstNames='"
                + getFirstNames() + '\'' + ", lastNames='" + getLastNames()
                + '\'' + ", middleNames='" + getMiddleNames() + '\''
                + ", prefix='" + prefix + '\'' + ", suffix='" + suffix + '\''
                + ", preferredPubName='" + preferredPubName + '\'' + ", bio='"
                + bio + '\'' + ", website='" + website + '\'' + ", password='"
                + password + '\'' + ", emailAddress='" + emailAddress + '\''
                + ", phoneNumber='" + phoneNumber + '\'' + ", jobTitle='"
                + jobTitle + '\'' + ", city='" + city + '\'' + ", state='"
                + state + '\'' + ", department='" + department + '\''
                + ", instCompany='" + instCompany + '\''
                + ", instCompanyWebsite='" + instCompanyWebsite + '\''
                + ", registrationStatus=" + registrationStatus + ", roles="
                + roles + ", comparator=" + comparator + ", readOnly="
                + readOnly + ", externalStorageLinked=" + externalStorageLinked + ", dropboxAppKey="
                + dropboxAppKey + ", dropboxAppSecret=" + dropboxAppSecret + '}';
    }

    /**
     * Determines if this Person is equal to another object.
     * <p/>
     * Currently equality is determined by comparing the value of the following
     * fields:
     * <ul>
     * <li>Prefix</li>
     * <li>First Names</li>
     * <li>Middle Names</li>
     * <li>Last Names</li>
     * <li>Suffix</li>
     * <li>Preferred Published Name</li>
     * <li>Bio</li>
     * <li>Website</li>
     * <li>Job Title</li>
     * <li>Department</li>
     * <li>Institution/Company</li>
     * <li>Email Address</li>
     * <li>Password</li>
     * <li>Phone Number</li>
     * <li>City</li>
     * <li>State</li>
     * </ul>
     * 
     * @param o
     *        the object to test for equality
     * @return true if the objects are equal
     */

    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || !(o instanceof Person)) {
            return false;
        }

        Person other = (Person) o;

        if (prefix == null) {
            if (other.prefix != null) {
                return false;
            }
        } else if (!prefix.equalsIgnoreCase(other.getPrefix())) {
            return false;
        }
        if (firstNames == null) {
            if (other.firstNames != null) {
                return false;
            }
        } else if (!Arrays.equals(firstNames, other.getFirstNamesAsArray())) {
            return false;
        }
        if (middleNames == null) {
            if (other.middleNames != null) {
                return false;
            }
        } else if (!Arrays.equals(middleNames, other.getMiddleNamesAsArray())) {
            return false;
        }
        if (lastNames == null) {
            if (other.lastNames != null) {
                return false;
            }
        } else if (!Arrays.equals(lastNames, other.getLastNamesAsArray())) {
            return false;
        }
        if (suffix == null) {
            if (suffix != null) {
                return false;
            }
        } else if (!suffix.equalsIgnoreCase(other.getSuffix())) {
            return false;
        }
        if (preferredPubName == null) {
            if (preferredPubName != null) {
                return false;
            }
        } else if (!preferredPubName.equalsIgnoreCase(other
                .getPreferredPubName())) {
            return false;
        }
        if (bio == null) {
            if (bio != null) {
                return false;
            }
        } else if (!bio.equalsIgnoreCase(other.getBio())) {
            return false;
        }
        if (website == null) {
            if (website != null) {
                return false;
            }
        } else if (!website.equalsIgnoreCase(other.getWebsite())) {
            return false;
        }
        if (jobTitle == null) {
            if (jobTitle != null) {
                return false;
            }
        } else if (!jobTitle.equalsIgnoreCase(other.getJobTitle())) {
            return false;
        }
        if (department == null) {
            if (department != null) {
                return false;
            }
        } else if (!department.equalsIgnoreCase(other.getDepartment())) {
            return false;
        }
        if (instCompany == null) {
            if (instCompany != null) {
                return false;
            }
        } else if (!instCompany.equalsIgnoreCase(other.getInstCompany())) {
            return false;
        }
        if (instCompanyWebsite == null) {
            if (instCompanyWebsite != null) {
                return false;
            }
        }
        else if (!instCompanyWebsite.equalsIgnoreCase(other.getInstCompanyWebsite())) {
            return false;
        }
        if (emailAddress == null) {
            if (emailAddress != null) {
                return false;
            }
        } else if (!emailAddress.equalsIgnoreCase(other.getEmailAddress())) {
            return false;
        }
        if (password == null) {
            if (password != null) {
                return false;
            }
        } else if (!password.equalsIgnoreCase(other.getPassword())) {
            return false;
        }
        if (phoneNumber == null) {
            if (phoneNumber != null) {
                return false;
            }
        } else if (!phoneNumber.equalsIgnoreCase(other.getPhoneNumber())) {
            return false;
        }
        if (city == null) {
            if (city != null) {
                return false;
            }
        } else if (!city.equalsIgnoreCase(other.getCity())) {
            return false;
        }
        if (state == null) {
            if (state != null) {
                return false;
            }
        } else if (!state.equalsIgnoreCase(other.getState())) {
            return false;
        }
        if (externalStorageLinked) {
            if (!other.isExternalStorageLinked()) {
                return false;
            }
        }
        else if (!externalStorageLinked) {
            if (other.isExternalStorageLinked()) {
                return false;
            }
        }
        if (dropboxAppKey == null) {
            if (dropboxAppKey != null) {
                return false;
            }
        }
        else if (!dropboxAppKey.equalsIgnoreCase(other.getDropboxAppKey())) {
            return false;
        }
        if (dropboxAppSecret == null) {
            if (dropboxAppSecret != null) {
                return false;
            }
        }
        else if (!dropboxAppSecret.equalsIgnoreCase(other.getDropboxAppSecret())) {
            return false;
        }

        return true;
    }

    /**
     * Calculates a hash code using the fields that are considered for
     * {@link #equals(Object) equality}.
     * 
     * @return the hashcode
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        if (firstNames == null) {
            result = prime * result;
        } else {
            for (String firstName : firstNames) {
                result = prime * result + firstName.hashCode();
            }
        }
        if (middleNames == null) {
            result = prime * result;
        } else {
            for (String middleName : middleNames) {
                result = prime * result + middleName.hashCode();
            }
        }
        if (lastNames == null) {
            result = prime * result;
        } else {
            for (String lastName : lastNames) {
                result = prime * result + lastName.hashCode();
            }
        }

        result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());

        result = prime * result + ((preferredPubName == null) ? 0 : preferredPubName.hashCode());

        result = prime * result + ((bio == null) ? 0 : bio.hashCode());

        result = prime * result + ((website == null) ? 0 : website.hashCode());

        result = prime * result + ((city == null) ? 0 : city.hashCode());

        result = prime * result + ((state == null) ? 0 : state.hashCode());

        result = prime * result + ((emailAddress == null) ? 0 : emailAddress.hashCode());

        result = prime * result + ((jobTitle == null) ? 0 : jobTitle.hashCode());

        result = prime * result + ((department == null) ? 0 : department.hashCode());

        result = prime * result + ((instCompany == null) ? 0 : instCompany.hashCode());
        
        result = prime * result + ((instCompanyWebsite == null) ? 0 : instCompanyWebsite.hashCode());

        result = prime * result + ((password == null) ? 0 : password.hashCode());
        
        result = prime
                * result
                + ((Boolean.toString(externalStorageLinked) == null) ? 0 : Boolean.toString(externalStorageLinked)
                        .hashCode());

        result = prime * result + ((dropboxAppKey == null) ? 0 : dropboxAppKey.hashCode());
        
        result = prime * result + ((dropboxAppSecret == null) ? 0 : dropboxAppSecret.hashCode());
        return result;

    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getInstCompany() {
        return instCompany;
    }

    public void setInstCompany(String instCompany) {
        this.instCompany = instCompany;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPreferredPubName() {
        return preferredPubName;
    }

    public void setPreferredPubName(String preferredPubName) {
        this.preferredPubName = preferredPubName;
    }
    
    public String getInstCompanyWebsite() {
        return instCompanyWebsite;
    }
    
    public void setInstCompanyWebsite(String instCompanyWebsite) {
        this.instCompanyWebsite = instCompanyWebsite;
    }
    
    public boolean isExternalStorageLinked() {
        return externalStorageLinked;
    }
    
    public void setExternalStorageLinked(boolean externalStorageLinked) {
        this.externalStorageLinked = externalStorageLinked;
    }
    
    public String getDropboxAppKey() {
        return dropboxAppKey;
    }
    
    public void setDropboxAppKey(String dropboxAppKey) {
        this.dropboxAppKey = dropboxAppKey;
    }
    
    public String getDropboxAppSecret() {
        return dropboxAppSecret;
    }
    
    public void setDropboxAppSecret(String dropboxAppSecret) {
        this.dropboxAppSecret = dropboxAppSecret;
    }
}
