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

package org.seadva.bagit.model;


import java.util.ArrayList;
import java.util.List;

public class FileNode implements Node{

    private String title;
    private String id;
    private long fileSize;
    private List<String> formats = new ArrayList<String>();
    private String source;


    public FileNode(){

    }

    @Override
    public String getId(){
        return this.id;
    }
    @Override
    public void setId(String id){
        this.id = id;
    }
    @Override
    public String getTitle(){
        return this.title;
    }
    @Override
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

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }
    public void addFormat(String format){
        formats.add(format);
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
