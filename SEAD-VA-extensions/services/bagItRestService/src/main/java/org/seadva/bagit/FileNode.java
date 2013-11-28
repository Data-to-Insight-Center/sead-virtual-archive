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

package org.seadva.bagit;


import java.util.HashSet;
import java.util.Set;

public class FileNode {


    private String title;
    private String id;
    private Set<String> formats = new HashSet<String>();


    public FileNode(){

    }

    public String getId(){
        return this.id;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getTitle(){
        return this.title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public Set<String> getFormats() {
        return formats;
    }

    public void setFormats(Set<String> formats) {
        this.formats = formats;
    }

    public void addFormat(String format){
        this.formats.add(format);
    }

  
  }
