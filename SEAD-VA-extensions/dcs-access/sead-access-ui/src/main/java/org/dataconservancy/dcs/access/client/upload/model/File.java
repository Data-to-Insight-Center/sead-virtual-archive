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
package org.dataconservancy.dcs.access.client.upload.model;

import java.util.ArrayList;
import java.util.List;


public class File extends Entity {
    private List<Metadata> metadata;
    private String name;
    private String source;
    private String technicalEnviroment;
    
    public File() {
        this.metadata = new ArrayList<Metadata>();
    }

    public List<Metadata> metadata() {
        return metadata;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setTechnicalEnviroment(String technicalEnviroment) {
        this.technicalEnviroment = technicalEnviroment;
    }

    public String getTechnicalEnviroment() {
        return technicalEnviroment;
    }
}
