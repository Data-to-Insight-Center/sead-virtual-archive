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

package org.dataconservancy.dcs.access.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public enum Role implements IsSerializable, Serializable{

    ROLE_ADMIN("Administrator"),
    ROLE_USER("user"),
    ROLE_NONSEADUSER("non-seaduser"),
    ROLE_CURATOR("Curator"),
    ROLE_RESEARCHER("Researcher"),
    ROLE_REPO_REP("Repository_representative");
    
    private final String name;

    Role(String name) {
        this.name = name;
    }
    
    public static Role fromString(String name) {
        if (name != null) {
          for (Role b : Role.values()) {
            if (name.equalsIgnoreCase(b.name)) {
              return b;
            }
          }
        }
        return null;
      }
    public String getName() {
    	return this.name;
      }
     
}