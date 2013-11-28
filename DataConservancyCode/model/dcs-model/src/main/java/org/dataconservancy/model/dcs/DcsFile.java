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
package org.dataconservancy.model.dcs;

import java.util.Arrays;
import java.util.Collection;

import org.dataconservancy.model.dcs.support.Assertion;
import org.dataconservancy.model.dcs.support.CollectionFactory;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.model.dcs.support.Util;

import static org.dataconservancy.model.dcs.support.Util.deepCopy;

/**
 * Models a Data Conservancy File
 */
public class DcsFile extends DcsEntity {
    private String name;
    private String source;
    private boolean extant;
    private Boolean valid;
    private long sizeBytes = -1;
    private Collection<DcsFixity> fixity = CollectionFactory.newCollection();
    private Collection<DcsFormat> formats = CollectionFactory.newCollection();
    private Collection<DcsMetadata> metadata = CollectionFactory.newCollection();
    private Collection<DcsMetadataRef> metadataRef = CollectionFactory.newCollection();

    /**
     * Constructs a new DcsFile with no state.
     */
    public DcsFile() {

    }

    /**
     * Copy constructor for a DcsFile.  The state of <code>toCopy</code> is copied
     * to this.  Note that if {@code toCopy} is modified while constructing this DcsFile, then
     * the state of this DcsFile is undefined.
     *
     * @param toCopy the dcs file to copy, must not be {@code null}
     * @throws IllegalArgumentException if {@code toCopy} is {@code null}
     */
    public DcsFile(DcsFile toCopy) {
        super(toCopy);
        this.name = toCopy.name;
        this.extant = toCopy.extant;
        this.sizeBytes = toCopy.sizeBytes;
        deepCopy(toCopy.fixity, this.fixity);
        deepCopy(toCopy.formats, this.formats);
        this.source = toCopy.source;
        this.valid = toCopy.valid;
        deepCopy(toCopy.metadata, this.metadata);
        deepCopy(toCopy.metadataRef, this.metadataRef);
    }

    /**
     * The name of the file.
     *
     * @return the file name, may be {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * The name of the file.
     *
     * @param name the name of the file, must not be {@code null} or the empty or zero-length string
     * @throws IllegalArgumentException if {@code name} is {@code null} or the empty or zero-length string
     */
    public void setName(String name) {
        Assertion.notEmptyOrNull(name);
        this.name = name;
    }

    /**
     * If the file is extant in the DCS archive.
     *
     * @return if the file is extant in the DCS archive.
     */
    public boolean isExtant() {
        return extant;
    }

    /**
     * If the file is extant in the DCS archive.
     *
     * @param extant if the file is extant in the DCS archive.
     */
    public void setExtant(boolean extant) {
        this.extant = extant;
    }

    /**
     * The size of the file, in bytes.
     *
     * @return the size of the file, in bytes.  May be {@code -1}
     */
    public long getSizeBytes() {
        return sizeBytes;
    }

    /**
     * The size of the file, in bytes.
     *
     * @param sizeBytes the size of the file, in bytes.  Must be greater or equal to zero.
     * @throws IllegalArgumentException if {@code sizeBytes} is less than zero.
     */
    public void setSizeBytes(long sizeBytes) {
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("File size must be greater than or equal to 0 bytes");
        }
        this.sizeBytes = sizeBytes;
    }

    /**
     * The file fixity.
     *
     * @return the file fixity, may be empty but never {@code null}
     */
    public Collection<DcsFixity> getFixity() {
        return fixity;
    }

    /**
     * The file fixity.  Note that this nullifies any existing fixity.
     *
     * @param fixity the file fixity, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code fixity} is {@code null} or contains {@code null} references
     */
    public void setFixity(Collection<DcsFixity> fixity) {
        Assertion.notNull(fixity);
        Assertion.doesNotContainNull(fixity);
        this.fixity = fixity;
    }

    /**
     * Add file fixity.
     *
     * @param fixity the file fixity, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code fixity} is {@code null} or contains {@code null} references
     */
    public void addFixity(DcsFixity... fixity) {
        Assertion.notNull(fixity);
        Assertion.doesNotContainNull(fixity);
        this.fixity.addAll(Arrays.asList(fixity));
    }

    /**
     * The formats of the file.
     *
     * @return the formats of the file, may be empty but never {@code null}
     */
    public Collection<DcsFormat> getFormats() {
        return formats;
    }

    /**
     * The formats of the file. Note this nullifies any existing formats.
     *
     * @param formats the formats of the file, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code formats} is {@code null} or contains {@code null} references
     */
    public void setFormats(Collection<DcsFormat> formats) {
        Assertion.notNull(formats);
        Assertion.doesNotContainNull(formats);
        this.formats = formats;
    }

    /**
     * Add formats of the file.
     *
     * @param format the formats of the file, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code format} is {@code null} or contains {@code null} references
     */
    public void addFormat(DcsFormat... format) {
        Assertion.notNull(format);
        Assertion.doesNotContainNull(format);
        this.formats.addAll(Arrays.asList(format));
    }

    /**
     * The source of the file
     *
     * @return the source of the file, may be {@code null}
     */
    public String getSource() {
        return source;
    }

    /**
     * The source of the file
     *
     * @param source the source of the file, must not be {@code null} or the empty or zero-length string
     * @throws IllegalArgumentException if {@code name} is {@code null} or the empty or zero-length string
     */
    public void setSource(String source) {
        Assertion.notEmptyOrNull(source);
        this.source = source;
    }

    /**
     * If the file is valid
     *
     * @return if the file is valid
     */
    public Boolean getValid() {
        return valid;
    }

    /**
     * If the file is valid
     *
     * @param valid if the file is valid
     */
    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    /**
     * The file metadata.
     *
     * @return the file metadata, may be empty but never {@code null}
     */
    public Collection<DcsMetadata> getMetadata() {
        return this.metadata;
    }

    /**
     * The file metadata.  Note this nullifies any existing metadata.
     *
     * @param metadata the file metadata, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code metadata} is {@code null} or contains {@code null} references
     */
    public void setMetadata(Collection<DcsMetadata> metadata) {
        Assertion.notNull(metadata);
        Assertion.doesNotContainNull(metadata);
        this.metadata = metadata;
    }

    /**
     * The file metadata.
     *
     * @param metadata the file metadata, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code metadata} is {@code null} or contains {@code null} references
     */
    public void addMetadata(DcsMetadata... metadata) {
        Assertion.notNull(metadata);
        Assertion.doesNotContainNull(metadata);
        this.metadata.addAll(Arrays.asList(metadata));
    }

    /**
     * The file metadata references.
     *
     * @return the file metadata references, may be empty but never {@code null}
     */
    public Collection<DcsMetadataRef> getMetadataRef() {
        return this.metadataRef;
    }

    /**
     * The file metadata references.  Note this nullifies any existing metadata.
     *
     * @param metadataRef the file metadata, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code metadata} is {@code null} or contains {@code null} references
     */
    public void setMetadataRef(Collection<DcsMetadataRef> metadataRef) {
        Assertion.notNull(metadataRef);
        Assertion.doesNotContainNull(metadataRef);
        this.metadataRef = metadataRef;
    }

    /**
     * The file metadata references.
     *
     * @param metadataRef the file metadata, must not be {@code null} or contain {@code null} references
     * @throws IllegalArgumentException if {@code metadata} is {@code null} or contains {@code null} references
     */
    public void addMetadataRef(DcsMetadataRef... metadataRef) {
        Assertion.notNull(metadataRef);
        Assertion.doesNotContainNull(metadataRef);
        this.metadataRef.addAll(Arrays.asList(metadataRef));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DcsFile dcsFile = (DcsFile) o;

        if (extant != dcsFile.extant) return false;
        if (sizeBytes != dcsFile.sizeBytes) return false;
        if (!Util.isEqual(fixity, dcsFile.fixity)) {
            return false;
        }
        if (!Util.isEqual(formats, dcsFile.formats)) {
            return false;
        }
        if (!Util.isEqual(metadata, dcsFile.metadata)) {
            return false;
        }
        if (!Util.isEqual(metadataRef, dcsFile.metadataRef)) {
            return false;
        }
        if (name != null ? !name.equals(dcsFile.name) : dcsFile.name != null) return false;
        if (source != null ? !source.equals(dcsFile.source) : dcsFile.source != null) return false;
        if (valid != null ? !valid.equals(dcsFile.valid) : dcsFile.valid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (extant ? 1 : 0);
        result = 31 * result + (valid != null ? valid.hashCode() : 0);
        result = 31 * result + (int) (sizeBytes ^ (sizeBytes >>> 32));
        result = 31 * result + (fixity != null ? Util.hashCode(fixity) : 0);
        result = 31 * result + (formats != null ? Util.hashCode(formats) : 0);
        result = 31 * result + (metadata != null ? Util.hashCode(metadata) : 0);
        result = 31 * result + (metadataRef != null ? Util.hashCode(metadataRef) : 0);
        return result;
    }

    @Override
    public String toString() {
        String ids = "";
        for (DcsResourceIdentifier id : this.getAlternateIds()) {
            ids += id + ", ";
        }
        return "{DcsFile{" +
                "name='" + name + '\'' +
                ", source='" + source + '\'' +
                ", extant=" + extant +
                ", valid=" + valid +
                ", sizeBytes=" + sizeBytes +
                ", fixity=" + fixity +
                ", formats=" + formats +
                ", metadata=" + metadata +
                ", metadataRef=" + metadataRef +
                ", alternateIds=" + ids +
                "}" + super.toString() + "}";
    }

    public void toString(HierarchicalPrettyPrinter sb) {
        sb.appendWithIndentAndNewLine("File:");
        sb.incrementDepth();

        super.toString(sb);

        sb.appendWithIndent("name: ").appendWithNewLine(name);
        sb.appendWithIndent("source: ").appendWithNewLine(source);
        sb.appendWithIndent("extant: ").appendWithNewLine(String.valueOf(extant));
        sb.appendWithIndent("valid: ").appendWithNewLine(String.valueOf(valid));
        sb.appendWithIndent("sizeBytes: ").appendWithNewLine(String.valueOf(sizeBytes));

        for (DcsFixity f : fixity) {
            f.toString(sb);
        }

        for (DcsFormat f : formats) {
            f.toString(sb);
        }

        for (DcsMetadata md : metadata) {
            md.toString(sb);
        }

        for (DcsMetadataRef mdRef : metadataRef) {
            mdRef.toString(sb, "");
        }

        sb.decrementDepth();
    }
}
