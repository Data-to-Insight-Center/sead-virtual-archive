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

import org.dataconservancy.model.dcs.support.Assertion;
import org.dataconservancy.model.dcs.support.CollectionFactory;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.model.dcs.support.Util;

import java.util.Arrays;
import java.util.Collection;

import static org.dataconservancy.model.dcs.support.Util.deepCopy;

/**
 * Models a Data Conservancy Manifestation
 */
public class DcsManifestation extends DcsEntity {
    private String deliverableUnit;
    private Collection<DcsMetadata> metadata = CollectionFactory.newCollection();
    private Collection<DcsManifestationFile> manifestationFiles = CollectionFactory.newCollection();
    private Collection<String> technicalEnvironment = CollectionFactory.newCollection();
    private Collection<DcsMetadataRef> metadataRef = CollectionFactory.newCollection();
    private String type;
    private String dateCreated;

	/**
     * Constructs a new DcsManifestation with no state.
     */
    public DcsManifestation() {

    }

    /**
     * Copy constructor for a DcsManifestation.  The state of <code>toCopy</code> is copied
     * to this.  Note that if {@code toCopy} is modified while constructing this DcsManifestation,
     * the state of this DcsManifestation is undefined.
     *
     * @param toCopy the dcs manifestation to copy
     */
    public DcsManifestation(DcsManifestation toCopy) {
        super(toCopy);
        this.deliverableUnit = toCopy.deliverableUnit;
        deepCopy(toCopy.metadata, this.metadata);
        deepCopy(toCopy.manifestationFiles, this.manifestationFiles);
        deepCopy(toCopy.metadataRef, this.metadataRef);
        deepCopy(toCopy.technicalEnvironment, this.technicalEnvironment);
        this.type = toCopy.type;
        this.dateCreated = toCopy.dateCreated;
    }

    /**
     * The date the manifestation was created
     *
     * @return the date the manifestation was created
     */
    public String getDateCreated() {
		return dateCreated;
	}

    /**
     * The date the manifestation was created
     *
     * @param dateCreated the date the manifestation was created, must not be {@code null}, the empty or zero-length
     *                    string
     * @throws IllegalArgumentException if {@code dateCreated} is {@code null}, the empty or zero-length string
     */
	public void setDateCreated(String dateCreated) {
		Assertion.notEmptyOrNull(dateCreated);
		this.dateCreated = dateCreated;
	}

    /**
     * The deliverable unit of this manifestation
     *
     * @return the deliverable unit of this manifestation
     */
    public String getDeliverableUnit() {
        return deliverableUnit;
    }

    /**
     * The deliverable unit of this manifestation
     *
     * @param deliverableUnit the deliverable unit of this manifestation, must not be {@code null}, the empty or
     *                        zero-length string
     * @throws IllegalArgumentException if {@code dateCreated} is {@code null}, the empty or zero-length string
     */
    public void setDeliverableUnit(String deliverableUnit) {
        Assertion.notEmptyOrNull(deliverableUnit);
        this.deliverableUnit = deliverableUnit;
    }

    /**
     * The metadata for this manifestation
     *
     * @return the metadata for this manifestation, may be empty but never {@code null}
     */
    public Collection<DcsMetadata> getMetadata() {
        return this.metadata;
    }

    /**
     * The metadata for this manifestation.  Note this nullifies existing metadata.
     *
     * @param metadata the metadata for this manifestation, must not be {@code null}, or contain {@code null} references
     * @throws IllegalArgumentException if {@code metadata} is {@code null}, or contains {@code null} references
     */
    public void setMetadata(Collection<DcsMetadata> metadata) {
        Assertion.notNull(metadata);
        Assertion.doesNotContainNull(metadata);
        this.metadata = metadata;
    }

    /**
     * The metadata for this manifestation.
     *
     * @param metadata the metadata for this manifestation, must not be {@code null}, or contain {@code null} references
     * @throws IllegalArgumentException if {@code metadata} is {@code null}, or contains {@code null} references
     */
    public void addMetadata(DcsMetadata... metadata) {
        Assertion.notNull(metadata);
        Assertion.doesNotContainNull(metadata);
        this.metadata.addAll(Arrays.asList(metadata));
    }

    /**
     * The metadatarefs for this manifestation
     *
     * @return the metadatarefs for this manifestation, may be empty but never {@code null}
     */
    public Collection<DcsMetadataRef> getMetadataRef() {
        return this.metadataRef;
    }

    /**
     * The metadata references for this manifestation.  Note this nullifies existing metadata references.
     *
     * @param metadataRef the metadata references for this manifestation, must not be {@code null}, or contain
     *                    {@code null} references
     * @throws IllegalArgumentException if {@code metadataRef} is {@code null}, or contains {@code null} references
     */
    public void setMetadataRef(Collection<DcsMetadataRef> metadataRef) {
        Assertion.notNull(metadataRef);
        Assertion.doesNotContainNull(metadataRef);
        this.metadataRef = metadataRef;
    }

    /**
     * The metadata references for this manifestation.
     *
     * @param metadataRef the metadata references for this manifestation, must not be {@code null}, or contain
     *                    {@code null} references
     * @throws IllegalArgumentException if {@code metadataRef} is {@code null}, or contains {@code null} references
     */
    public void addMetadataRef(DcsMetadataRef... metadataRef) {
        Assertion.notNull(metadataRef);
        Assertion.doesNotContainNull(metadataRef);
        this.metadataRef.addAll(Arrays.asList(metadataRef));
    }

    /**
     * The manifestation files for this manifestation
     *
     * @return the manifestation files for this manifestation, may be empty but never {@code null}
     */
    public Collection<DcsManifestationFile> getManifestationFiles() {
        return this.manifestationFiles;
    }

    /**
     * The manifestation files for this manifestation.  Note this nullifies existing manifestation files.
     *
     * @param manifestationFiles the manifestation files for this manifestation, must not be {@code null}, or contain
     *                           {@code null} references
     * @throws IllegalArgumentException if {@code manifestationFiles} is {@code null}, or contains {@code null}
     *                                  references
     */
    public void setManifestationFiles(Collection<DcsManifestationFile> manifestationFiles) {
        Assertion.notNull(manifestationFiles);
        Assertion.doesNotContainNull(manifestationFiles);
        this.manifestationFiles = manifestationFiles;
    }

    /**
     * The manifestation files for this manifestation.
     *
     * @param manifestationFiles the manifestation files for this manifestation, must not be {@code null}, or contain
     *                           {@code null} references
     * @throws IllegalArgumentException if {@code manifestationFiles} is {@code null}, or contains {@code null}
     *                                  references
     */
    public void addManifestationFile(DcsManifestationFile... manifestationFiles) {
        Assertion.notNull(manifestationFiles);
        Assertion.doesNotContainNull(manifestationFiles);
        this.manifestationFiles.addAll(Arrays.asList(manifestationFiles));
    }

    /**
     * The technical environment for this manifestation
     *
     * @return the technical environment for this manifestation, may be empty but never {@code null}
     */
    public Collection<String> getTechnicalEnvironment() {
        return this.technicalEnvironment;
    }

    /**
     * The technical environment for this manifestation.  Note this nullifies existing technical environments.
     *
     * @param technicalEnvironment the technical environment for this manifestation, must not be {@code null}, or
     *                             contain {@code null} references
     * @throws IllegalArgumentException if {@code technicalEnvironment} is {@code null}, or contains {@code null}
     *                                  references
     */
    public void setTechnicalEnvironment(Collection<String> technicalEnvironment) {
        Assertion.notNull(technicalEnvironment);
        Assertion.doesNotContainNullOrEmptyString(technicalEnvironment);
        this.technicalEnvironment = technicalEnvironment;
    }

    /**
     * The technical environment for this manifestation.
     *
     * @param technicalEnvironment the technical environment for this manifestation, must not be {@code null}, or
     *                             contain {@code null} references
     * @throws IllegalArgumentException if {@code technicalEnvironment} is {@code null}, or contains {@code null}
     *                                  references
     */
    public void addTechnicalEnvironment(String... technicalEnvironment) {
        Assertion.notNull(technicalEnvironment);
        Assertion.doesNotContainNullOrEmptyString(technicalEnvironment);
        this.technicalEnvironment.addAll(Arrays.asList(technicalEnvironment));
    }

    /**
     * The type of this manifestation
     *
     * @return the type of this manifestation
     */
    public String getType() {
        return type;
    }

    /**
     * The type of this manifestation
     *
     * @param type the type of this manifestation
     */
    public void setType(String type) {
        Assertion.notEmptyOrNull(type);
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DcsManifestation that = (DcsManifestation) o;

        if (dateCreated != null ? !dateCreated.equals(that.dateCreated) : that.dateCreated != null) return false;
        if (deliverableUnit != null ? !deliverableUnit.equals(that.deliverableUnit) : that.deliverableUnit != null)
            return false;

        if (!Util.isEqual(manifestationFiles, that.manifestationFiles)) {
            return false;
        }
        if (!Util.isEqual(metadata, that.metadata)) {
            return false;
        }
        if (!Util.isEqual(metadataRef, that.metadataRef)) {
            return false;
        }
        if (!Util.isEqual(technicalEnvironment, that.technicalEnvironment)) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (deliverableUnit != null ? deliverableUnit.hashCode() : 0);
        result = 31 * result + (metadata != null ? Util.hashCode(metadata) : 0);
        result = 31 * result + (manifestationFiles != null ? Util.hashCode(manifestationFiles) : 0);
        result = 31 * result + (technicalEnvironment != null ? Util.hashCode(technicalEnvironment) : 0);
        result = 31 * result + (metadataRef != null ? Util.hashCode(metadataRef) : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (dateCreated != null ? dateCreated.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{DcsManifestation{" +
                "deliverableUnit='" + deliverableUnit + '\'' +
                ", metadata=" + metadata +
                ", manifestationFiles=" + manifestationFiles +
                ", technicalEnvironment=" + technicalEnvironment +
                ", metadataRef=" + metadataRef +
                ", type='" + type + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                "}" + super.toString() + "}";
    }

    public void toString(HierarchicalPrettyPrinter sb) {
        sb.appendWithIndentAndNewLine("Manifestation: ");
        sb.incrementDepth();

        super.toString(sb);

        sb.appendWithIndent("deliverableUnit: ").appendWithNewLine(deliverableUnit);
        sb.appendWithIndent("type: ").appendWithNewLine(type);
        sb.appendWithIndent("dateCreated: ").appendWithNewLine(dateCreated);

        for (String techEnv : technicalEnvironment) {
            sb.appendWithIndent("technicalEnvironment: ").appendWithNewLine(techEnv);
        }

        for (DcsMetadata md : metadata) {
            md.toString(sb);
        }

        for (DcsMetadataRef mdRef : metadataRef) {
            mdRef.toString(sb, "");
        }

        for (DcsManifestationFile mf : manifestationFiles) {
            mf.toString(sb, "");
        }

        sb.decrementDepth();
    }
}
