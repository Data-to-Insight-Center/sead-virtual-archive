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

import java.util.Arrays;


/**
 * {@code PersonName} encapsulates different components of a person's name.
 */
public class PersonName{

    private String [] givenNames;
    private String [] familyNames;
    private String prefixes;
    private String suffixes;
    private String [] middleNames;

    /**
     * Creates a person name object with the supplied information
     * @param prefixes A string containing the person's prefixes.
     * @param givenNames An array of strings representing all of the person's given names.
     * @param middleNames An array of strings representing all of the person's middle names.
     * @param familyNames An array of strings representing all of the person's family names.
     * @param suffixes A string containing the person't suffixes.
     */
    public PersonName(String prefixes, String[] givenNames, String[] middleNames, String[] familyNames, String suffixes){
        this.givenNames = new String[givenNames.length];
        for (int i = 0; i < givenNames.length; i++) {
            this.givenNames[i] = givenNames[i];
        }
        this.middleNames = new String[middleNames.length];
        for (int i = 0; i < middleNames.length; i++) {
            this.middleNames[i] = middleNames[i];
        }
        this.familyNames = new String[familyNames.length];
        for (int i = 0; i < familyNames.length; i++) {
            this.familyNames[i] = familyNames[i];
        }
        this.prefixes = prefixes;
        this.suffixes = suffixes;
    }

    /**
     * Creates a person name object with the given information
     * @param prefix The prefixes of the person's name.
     * @param givenName The space delimited string of given names.
     * @param middleName The space delimited string of middle names.
     * @param familyName The space delimited string of family names.
     * @param suffix The suffix of the person's name.
     */
    public PersonName(String prefix, String givenName, String middleName, String familyName, String suffix){
        this.givenNames = givenName.split("\\s+");
        this.prefixes = prefix;
        this.middleNames = middleName.split("\\s+");
        this.familyNames = familyName.split("\\s+");
        this.suffixes = suffix;

    }

    /**
     * Copy constructor for duplicating a PersonName object.
     * @param other The object to be copied.
     */
    public PersonName(PersonName other){
        this.prefixes = other.prefixes;
        this.givenNames = other.givenNames;
        this.middleNames = other.middleNames;
        this.familyNames = other.familyNames;
        this.suffixes = other.suffixes;
    }

    /**
     * Empty constructor used by the jsp. Creates an empty Person Name object.
     * All fields should be set using getters and setters.
     */
    public PersonName(){
        givenNames = new String[] {};
        middleNames = new String[] {};
        familyNames = new String[] {};
    }

    /**
     * Sets the given names array based on the string provided
     * @param givenNames A space delimited string of the given names.
     */
    public void setGivenNames(String givenNames){
        this.givenNames = givenNames.split("\\s+");
    }

    /**
     * Sets the given names array to the array provided
     * @param givenNames An array representing the given names.
     */
    public void setGivenNames(String[] givenNames){
        this.givenNames = givenNames;
    }

    /**
     * Gets the given names in array form.
     * @return The string array of the given names.
     */
    public String[] getGivenNamesAsArray(){
        return givenNames;
    }

    /**
     * Gets the given names in a space delimited string.
     * @return A space delimited string of the given names.
     */
    public String getGivenNames(){
        String names = "";
        for( int i = 0; i < givenNames.length; i++){
            names += givenNames[i];
            if(i != givenNames.length - 1){
                names += " ";
            }
        }

        return names;
    }

    /**
     * Sets the family names based on the string provided.
     * @param familyNames A spaced delimited string of the family names.
     */
    public void setFamilyNames(String familyNames){
        this.familyNames = familyNames.split("\\s+");
    }

    /**
     * Sets the family names to the array provided.
     * @param familyNames A string array containing the family names.
     */
    public void setFamilyNames(String[] familyNames){
        this.familyNames = familyNames;
    }

    /**
     * Gets the family names as a string array.
     * @return A string array holding the family names.
     */
    public String[] getFamilyNamesAsArray(){
        return familyNames;
    }

    /**
     * Gets the family names as a space delimited string.
     * @return A space delimited string of all the family names.
     */
    public String getFamilyNames(){
        String names = "";
        for( int i = 0; i < familyNames.length; i++){
            names += familyNames[i];
            if(i != familyNames.length - 1){
                names += " ";
            }
        }

        return names;
    }

    /**
     * Sets the array of middle names based on the string provided.
     * @param middleNames A space delimited string of the middle names.
     */
    public void setMiddleNames(String middleNames){
        this.middleNames = middleNames.split("\\s+");
    }

    /**
     * Sets the array of middle names to array provided.
     * @param middleNames A string array containing the middle names.
     */
    public void setMiddleNames(String[] middleNames){
        this.middleNames = middleNames;
    }

    /**
     * Gets the middle names as a string array.
     * @return The string array containing the middle names.
     */
    public String[] getMiddleNamesAsArray(){
        return middleNames;
    }

    /**
     * Gets the middle names as a space delimited string.
     * @return A space delimited string of all the middle names.
     */
    public String getMiddleNames(){
        String names = "";
        for( int i = 0; i < middleNames.length; i++){
            names += middleNames[i];
            if(i != middleNames.length - 1){
                names += " ";
            }
        }

        return names;
    }

    /**
     * Sets the prefixes for the name based on the string provided. Prefixes will not be split.
     * @param prefixes A string representing all the prefixes.
     */
    public void setPrefixes(String prefixes){
        this.prefixes = prefixes;
    }

    /**
     * Gets the prefixes for the name.
     * @return A string representing all the prefixes.
     */
    public String getPrefixes(){
        return prefixes;
    }

    /**
     * Sets the suffixes for the name based on the string provided. Suffixes will not be split.
     * @param suffixes A string representing all the suffixes.
     */
    public void setSuffixes(String suffixes){
        this.suffixes = suffixes;
    }

    /**
     * Gets the suffixes for the name.
     * @return A string representing all the suffixes.
     */
    public String getSuffixes(){
        return suffixes;
    }

    /**
     * Checks if the object contains any information.
     * @return True if the object contains no information, false otherwise.
     */
    public boolean isEmpty(){
        boolean isEmpty = true;

        if( prefixes != null && prefixes.length() > 0){
            isEmpty = false;
        }else if( givenNames != null && givenNames.length > 0){
            isEmpty = false;
        }else if( middleNames != null && middleNames.length > 0){
            isEmpty = false;
        }else if( familyNames != null && familyNames.length > 0 ){
            isEmpty = false;
        }else if( suffixes != null && suffixes.length() > 0 ){
            isEmpty = false;
        }

        return isEmpty;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((prefixes == null) ? 0 : prefixes.hashCode());
        if(givenNames == null) {
            result = prime * result;
        } else {
            for (String givenName : givenNames) {
                result = prime * result + givenName.hashCode();
            }
        }
        if(middleNames == null) {
            result = prime * result;
        } else {
            for (String middleName : middleNames) {
                result = prime * result + middleName.hashCode();
            }
        }
        if(familyNames == null) {
            result = prime * result;
        } else {
            for (String familyName : familyNames) {
                result = prime * result + familyName.hashCode();
            }
        }

        result = prime * result + ((suffixes == null) ? 0 : suffixes.hashCode());
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
        PersonName other = (PersonName) obj;
        if (prefixes == null) {
            if (other.prefixes != null)
                return false;
        } else if (!prefixes.equalsIgnoreCase(other.prefixes))
            return false;
        if (givenNames == null) {
            if (other.givenNames != null)
                return false;
        } else if (!Arrays.equals(givenNames, other.givenNames))
            return false;
        if (middleNames == null) {
            if (other.middleNames != null)
                return false;
        } else if (!Arrays.equals(middleNames, other.middleNames))
            return false;
        if (familyNames == null) {
            if (other.familyNames != null)
                return false;
        } else if (!Arrays.equals(familyNames, other.familyNames))
            return false;
        if (suffixes == null) {
            if (suffixes != null)
                return false;
        } else if (!suffixes.equalsIgnoreCase(other.suffixes))
            return false;

        return true;
    }

    @Override
    public String toString() {
        String givenNamesStr = "";
        if( givenNames != null){
            for( int i = 0; i < givenNames.length; i++){
                givenNamesStr += givenNames[i] + " ";
            }
        }

        String middleNamesStr = "";
        if( middleNames != null){
            for( int i = 0; i < middleNames.length; i++){
                middleNamesStr += middleNames[i] + " ";
            }
        }

        String familyNamesStr = "";
        if( familyNames != null){
            for( int i = 0; i < familyNames.length; i++){
                familyNamesStr += familyNames[i] + " ";
            }
        }

        if( prefixes == null){
            prefixes = "";
        }

        if( suffixes == null){
            suffixes = "";
        }

        return "PersonName [prefixes=" + prefixes + ", \ngiven names=" + givenNamesStr
                + ", \nmiddle names=" + middleNamesStr + ", \nfamily names=" + familyNamesStr
                + ", \nsuffixes=" + suffixes + "]";
    }

}