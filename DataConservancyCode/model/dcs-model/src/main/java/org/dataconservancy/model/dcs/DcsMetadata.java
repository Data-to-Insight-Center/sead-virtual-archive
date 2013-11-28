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

import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.model.dcs.support.Assertion;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Models a Data Conservancy metadata blob
 */
public class DcsMetadata {

    private String schemaUri;
    private String metadata;
    private String metadataUsedByHashcode;

    private static final Pattern whitespace = Pattern.compile("\\s");
    private static final Pattern doublequote = Pattern.compile("\"");
    private static final Pattern singlequote = Pattern.compile("'");

    /**
     * Constructs a new DcsMetadata with no state.
     */
    public DcsMetadata() {

    }

    /**
     * Copy constructor for DcsMetadata.  The state of <code>toCopy</code> is copied
     * to this.  Note if {@code toCopy} is modified while constructing this DcsMetadata,
     * the state of this DcsMetadata is undefined.
     *
     * @param toCopy the metadata to copy
     */
    public DcsMetadata(DcsMetadata toCopy) {
        this.schemaUri = toCopy.getSchemaUri();
        this.metadata = toCopy.getMetadata();
        this.metadataUsedByHashcode = toCopy.metadataUsedByHashcode;
    }

    /**
     * The URI identifying the metadata scheme
     *
     * @return the URI identifying the metadata scheme
     */
    public String getSchemaUri() {
        return schemaUri;
    }

    /**
     * The URI identifying the metadata scheme
     *
     * @param schemaUri the URI identifying the metadata scheme, must not be {@code null}, the empty or zero-length
     *                  string
     * @throws IllegalArgumentException if {@code schemaUri} is {@code null}, the empty or zero-length string
     */
    public void setSchemaUri(String schemaUri) {
        Assertion.notEmptyOrNull(schemaUri);
        this.schemaUri = schemaUri;
    }

    /**
     * The metadata itself
     *
     * @return the metadata itself
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * The metadata itself
     *
     * @param metadata the metadata itself, must not be {@code null} or the empty or zero-length string
     */
    public void setMetadata(String metadata) {
        Assertion.notEmptyOrNull(metadata);
        this.metadata = metadata;
        // See DC-334: we normalize the metadata string which can be used reliably by the hashCode method.
        this.metadataUsedByHashcode = whitespace.matcher(metadata).replaceAll("");
        this.metadataUsedByHashcode = singlequote.matcher(metadataUsedByHashcode).replaceAll("");
        this.metadataUsedByHashcode = doublequote.matcher(metadataUsedByHashcode).replaceAll("");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DcsMetadata that = (DcsMetadata) o;

        if (schemaUri != null ? !schemaUri.equals(that.schemaUri) : that.schemaUri != null) return false;

        if ((metadata != null && that.metadata == null) || (metadata == null && that.metadata != null)) {
            return false;
        }

        if (metadata != null && that.metadata != null) {
            XMLUnit.setIgnoreWhitespace(true);
            XMLUnit.setIgnoreComments(true);
            try {
                if (!XMLUnit.compareXML(metadata, that.metadata).similar()) {
                    return false;
                }
            } catch (SAXException e) {
                e.printStackTrace();
                System.err.println("Comparing:");
                System.err.println(metadata);
                System.err.println("to:");
                System.err.println(that.metadata);
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * The normalized metadata string used for {@link #hashCode()} calculations.  Exposed for unit testing.
     *
     * @return the metadata string that will be used for this hashcode calculation
     */
    String getNormalizedMetadata() {
        return metadataUsedByHashcode;
    }

    @Override
    public int hashCode() {
        int result = schemaUri != null ? schemaUri.hashCode() : 0;
        result = 31 * result + (metadataUsedByHashcode != null ? metadataUsedByHashcode.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "DcsMetadata{" +
                "schemaUri='" + schemaUri + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }

    public void toString(HierarchicalPrettyPrinter sb) {
        sb.appendWithIndentAndNewLine("Metadata:");
        sb.incrementDepth();
        sb.appendWithIndent("schemaUri: ").appendWithNewLine(schemaUri);
        sb.appendWithIndent("metadata: ").appendWithNewLine(metadata);
        sb.decrementDepth();
    }
}
