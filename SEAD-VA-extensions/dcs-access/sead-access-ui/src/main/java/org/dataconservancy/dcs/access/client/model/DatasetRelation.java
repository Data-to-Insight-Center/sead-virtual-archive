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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.access.client.upload.model.Repository;

public class DatasetRelation   implements java.io.Serializable{

	public DatasetRelation(){
		duAttrMap = new HashMap<String, CollectionNode>();
		fileAttrMap = new HashMap<String, FileNode>();
		parentMap = new HashMap<String, String>();
	}
	
	private Map<String, CollectionNode> duAttrMap ;
	private Map<String, FileNode> fileAttrMap ;
	private Map<String, String> parentMap ;
	
	private String rootDuId;
	
    public String getRootDuId() {
        return rootDuId;
    }

    public void setRootDuId(String rootDuId) {
        this.rootDuId = rootDuId;
    }
	public Map<String, CollectionNode> getDuAttrMap() {
		return duAttrMap;
	}
	public void setDuAttrMap(Map<String, CollectionNode> duAttrMap) {
		this.duAttrMap = duAttrMap;
	}
	public Map<String, FileNode> getFileAttrMap() {
		return fileAttrMap;
	}
	public void setFileAttrMap(Map<String, FileNode> fileAttrMap) {
		this.fileAttrMap = fileAttrMap;
	}
	public Map<String, String> getParentMap() {
		return parentMap;
	}
	public void setParentMap(Map<String, String> parentMap) {
		this.parentMap = parentMap;
	}
	
  }
