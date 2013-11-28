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

public class Discipline {
    
    private String title;
    private String id;
    
    public Discipline(){
        
    }
    
    public Discipline(String title, String id){
        this.title = title;
        this.id = id;
    }
    
    public Discipline(Discipline toCopy) {
        this.title = toCopy.title;
        this.id = toCopy.id;
    }
    
    /**
     * Sets the title of the discipline
     */
    public void setTitle(String title){
        this.title = title;
    }
    
    /**
     * Returns the title of the discipline
     */
    public String getTitle(){
        return title;
    }
    
    /**
     * Sets the identifier of the discipline
     */
    public void setIdentifier(String id){
        this.id = id;
    }
    
    /**
     * Returns the identifier of the discipline
     */
    public String getId(){
        return id;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
       Discipline other = (Discipline) obj;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
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
        return "Discipline [title=" + title
                + ", id=" + id + "]";
    }
}