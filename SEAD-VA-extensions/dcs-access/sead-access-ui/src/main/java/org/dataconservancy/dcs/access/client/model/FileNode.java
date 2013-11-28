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

package org.dataconservancy.dcs.access.client.model;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.view.client.ProvidesKey;

public class FileNode implements IsSerializable{

	public static final ProvidesKey<FileNode> KEY_PROVIDER = new ProvidesKey<FileNode>() {
      @Override
      public Object getKey(FileNode item) {
        return item == null ? null : item.getId();
      }
    };

    private String title;
    private String id;
    private String format;
  

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
         
    public int compareTo(FileNode o) {
      return (o == null || o.id == null) ? -1 : -o.id.compareTo(id);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof FileNode) {
        return id == ((FileNode) o).getId();
      }
      return false;
    }

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

  
  }
