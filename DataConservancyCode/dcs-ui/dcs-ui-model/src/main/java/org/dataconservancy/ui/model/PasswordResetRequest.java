/*
 * Copyright 2013 Johns Hopkins University
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

import org.joda.time.DateTime;

/**
 * {@code PasswordResetRequest} represents a request by a user to reset a forgotten password.
 *
 */
public class PasswordResetRequest {

    private String id;
    private DateTime requestDate;
    private String userEmailAddress;
    
    public PasswordResetRequest(){
        
    }
    
    public PasswordResetRequest(String id, DateTime requestDate, String userEmailAddress) {
        this.id = id;
        this.requestDate = requestDate;
        this.userEmailAddress = userEmailAddress;
    }

    /**
     * Sets the email address for the PasswordResetRequest
     * @param emailAddress
     */
    public void setUserEmailAddress(String emailAddress){
        this.userEmailAddress = emailAddress;
    }

    /**
     * Returns the email address for the PasswordResetRequest
     * @return
     */
    public String getUserEmailAddress(){
        return userEmailAddress;
    }

    /**
     * Sets the identifier for the PasswordResetRequest
     * @param id
     */
    public void setId(String id){
        this.id = id;
    }

    /**
     * Returns the identifier for the PasswordResetRequest
     * @return
     */
    public String getId(){
        return id;
    }

    /**
     * Sets the request date and time for the PasswordResetRequest
     * @param requestDate
     */
    public void setRequestDate(DateTime requestDate){
        this.requestDate = requestDate;
    }

    /**
     * Returns the request date and time for the PasswordResetRequest
     * @return
     */
    public DateTime getRequestDate(){
        return requestDate;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((requestDate == null) ? 0 : requestDate.hashCode());
        result = prime * result + ((userEmailAddress == null) ? 0 : userEmailAddress.hashCode());
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
        PasswordResetRequest other = (PasswordResetRequest) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!requestDate.equals(other.requestDate)){
            return false;
        } else if (!userEmailAddress.equals(other.userEmailAddress))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PasswordResetRequest [id=" + id
                + ", requestDate=" + requestDate
                + ", userEmailAddress=" + userEmailAddress + "]";
    }
}
