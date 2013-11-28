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
package org.dataconservancy.dcs.ingest.client.impl;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Gets around defensive copies.
 * <p>
 * Makes sure that changes to the delegate are propigated to the corresponding
 * entity in the parent dcp.
 * </p>
 */
class LiveFileWrapper
        extends DcsFile {

    private final DcsFile fileDelegate;

    private final Dcp dcp;

    public LiveFileWrapper(DcsFile delegate, Dcp source) {
        fileDelegate = delegate;
        dcp = source;
    }

    @Override
    public void setExtant(boolean extant) {
        fileDelegate.setExtant(extant);
        save();
    }

    @Override
    public void setFixity(Collection<DcsFixity> fixity) {
        fileDelegate.setFixity(fixity);
        save();
    }

    @Override
    public void setFormats(Collection<DcsFormat> formats) {
        fileDelegate.setFormats(formats);
        save();
    }

    @Override
    public void setId(String id) {
        fileDelegate.setId(id);
        save();
    }

    @Override
    public void setMetadata(Collection<DcsMetadata> metadata) {
        fileDelegate.setMetadata(metadata);
        save();
    }

    @Override
    public void setMetadataRef(Collection<DcsMetadataRef> metadataRef) {
        fileDelegate.setMetadataRef(metadataRef);
        save();
    }

    @Override
    public void setName(String name) {
        fileDelegate.setName(name);
        save();
    }

    @Override
    public void setSizeBytes(long sizeBytes) {
        fileDelegate.setSizeBytes(sizeBytes);
        save();
    }

    @Override
    public void setSource(String source) {
        fileDelegate.setSource(source);
        save();
    }

    @Override
    public void setValid(Boolean valid) {
        fileDelegate.setValid(valid);
        save();
    }

    @Override
    public void addFixity(DcsFixity... fixity) {
        fileDelegate.addFixity(fixity);
        save();
    }

    @Override
    public void addFormat(DcsFormat... format) {
        fileDelegate.addFormat(format);
        save();
    }

    @Override
    public void addMetadata(DcsMetadata... metadata) {
        fileDelegate.addMetadata(metadata);
        save();
    }

    @Override
    public void addMetadataRef(DcsMetadataRef... metadataRef) {
        fileDelegate.addMetadataRef(metadataRef);
        save();
    }

    @Override
    public Collection<DcsFixity> getFixity() {
        return fileDelegate.getFixity();
    }

    @Override
    public Collection<DcsFormat> getFormats() {
        return fileDelegate.getFormats();
    }

    @Override
    public String getId() {
        return fileDelegate.getId();
    }

    @Override
    public Collection<DcsMetadata> getMetadata() {
        return fileDelegate.getMetadata();
    }

    @Override
    public Collection<DcsMetadataRef> getMetadataRef() {
        return fileDelegate.getMetadataRef();
    }

    @Override
    public String getName() {
        return fileDelegate.getName();
    }

    @Override
    public long getSizeBytes() {
        return fileDelegate.getSizeBytes();
    }

    @Override
    public String getSource() {
        return fileDelegate.getSource();
    }

    @Override
    public Boolean getValid() {
        return fileDelegate.getValid();
    }

    @Override
    public boolean isExtant() {
        return fileDelegate.isExtant();
    }

    private void save() {
        Collection<DcsFile> files = new HashSet<DcsFile>();
        for (DcsFile f : dcp.getFiles()) {
            if (f.getId().equals(fileDelegate.getId())) {
                files.add(fileDelegate);
            } else {
                files.add(f);
            }
        }

        dcp.setFiles(files);
    }

    public String toString() {
        return fileDelegate.toString();
    }

    public int hashCode() {
        return fileDelegate.hashCode();
    }

    public boolean equals(Object o) {
        return fileDelegate.equals(o);
    }
}
