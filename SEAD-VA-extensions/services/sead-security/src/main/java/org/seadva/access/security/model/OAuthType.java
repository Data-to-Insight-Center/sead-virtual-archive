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

public enum OAuthType {

    GOOGLE(1,"google",
    		"https://www.googleapis.com/oauth2/v1/",
    		"gmail.com");
    
    public final int serialNo;
    private final String name;
    private final String url;
    private final String suffix;

    OAuthType(int num, String name, String url, String suffix) {
    	this.serialNo = num;
        this.name = name;
        this.url = url;
        this.suffix = suffix;
    }
    
    public static OAuthType fromString(String name) {
        if (name != null) {
          for (OAuthType b : OAuthType.values()) {
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
    public String getUrl(){
    	return this.url;
    }
    public String getSuffix(){
    	return this.suffix;
    }
     
    public int getSerialNo(){
    	return this.serialNo;
    }
}