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
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;

import java.util.regex.Pattern;

/**
 * Models a Data Conservancy format
 */
public class DcsFormat {
    private String format;
    private String schemeUri;
    private String name;
    private String version;

    /**
     * Constructs a new DcsFormat with no state.
     */
    public DcsFormat() {

    }

    /**
     * Copy constructor for a DcsFormat.  The state of <code>toCopy</code> is copied
     * to this.  Note if {@code toCopy} is modified while constructing this DcsFormat, the
     * state of this DcsFormat is undefined.
     *
     * @param toCopy the dcs format to copy
     */
    public DcsFormat(DcsFormat toCopy) {
        this.format = toCopy.getFormat();
        this.version = toCopy.getVersion();
        this.name = toCopy.getName();
        this.schemeUri = toCopy.getSchemeUri();
    }

    /**
     * The identifier for the format of the file
     *
     * @return the identifier for the format of the file
     */
    public String getFormat() {
        return format;
    }

    /**
     * The identifier for the format of the file
     *
     * @param formatId the identifier for the format of the file, must not be {@code null} or the empty or zero-length
     *                  string
     * @throws IllegalArgumentException if {@code formatId} is {@code null} or the empty or zero-length string
     */
    public void setFormat(String formatId) {
        Assertion.notEmptyOrNull(formatId);
        this.format = formatId;
    }

    /**
     * The version of the format of the file
     *
     * @return the version of the format of the file
     */
    public String getVersion() {
        return version;
    }

    /**
     * The version of the format of the file
     *
     * @param version the version of the format of the file, must not be {@code null} or the empty or zero-length
     *                  string
     * @throws IllegalArgumentException if {@code version} is {@code null} or the empty or zero-length string
     */
    public void setVersion(String version) {
        Assertion.notEmptyOrNull(version);
        this.version = version;
    }

    /**
     * The identifier for the scheme the {@link #getFormat() format identifier} belongs to.
     *
     * @return the identifier for the scheme the {@link #getFormat() format identifier} belongs to
     */
    public String getSchemeUri() {
        return schemeUri;
    }

    /**
     * The identifier for the scheme the {@link #getFormat() format identifier} belongs to
     *
     * @param schemeUri the scheme identifier for the format id, must not be {@code null} or the empty or zero-length
     *                  string
     * @throws IllegalArgumentException if {@code schemeUri} is {@code null} or the empty or zero-length string
     */
    public void setSchemeUri(String schemeUri) {
        Assertion.notEmptyOrNull(schemeUri);
        this.schemeUri = schemeUri;
    }

    /**
     * The name of the format
     *
     * @return the name of the format
     */
    public String getName() {
        return name;
    }

    /**
     * The name of the format
     *
     * @param name the name of the format, must not be {@code null} or the empty or zero-length
     *                  string
     * @throws IllegalArgumentException if {@code name} is {@code null} or the empty or zero-length string
     */

    public void setName(String name) {
        Assertion.notEmptyOrNull(name);
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DcsFormat dcsFormat = (DcsFormat) o;

        if (format != null ? !format.equals(dcsFormat.format) : dcsFormat.format != null) return false;
        if (name != null ? !name.equals(dcsFormat.name) : dcsFormat.name != null) return false;
        if (schemeUri != null ? !schemeUri.equals(dcsFormat.schemeUri) : dcsFormat.schemeUri != null) return false;
        if (version != null ? !version.equals(dcsFormat.version) : dcsFormat.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = format != null ? format.hashCode() : 0;
        result = 31 * result + (schemeUri != null ? schemeUri.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DcsFormat{" +
                "format ='" + format + '\'' +
                ", name='" + name + '\'' +
                ", schema uri='" + schemeUri + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    public void toString(HierarchicalPrettyPrinter sb) {
        sb.appendWithIndentAndNewLine("Format: ");
        sb.incrementDepth();
        sb.appendWithIndent("format: ").appendWithNewLine(format);
        sb.appendWithIndent("name: ").appendWithNewLine(name);
        sb.appendWithIndent("schemaUri: ").appendWithNewLine(schemeUri);
        sb.appendWithIndent("version: ").appendWithNewLine(version);
        sb.decrementDepth();
    }

    /**
     * Given the string value of a DcsFormat object (as generated by DcsFormat.toString() method, parse the string to
     * create a DcsFormat object.
     * @param dcsFormatString
     * @return DcsFormat object represented by the dcsFormatString string.
     * @throws IllegalArgumentException when the provided string, dcsFormatString, is not well formed.
     */
    public static DcsFormat parseDcsFormat(String dcsFormatString) {
        DcsFormat dcsFormat = new DcsFormat();
        String expectedRegEx = "DcsFormat\\s*\\{\\s*format\\s*=\\s*'.*'\\s*,\\s*name\\s*=\\s*'.*'\\s*,\\s*schema uri\\s*=\\s*'.*'\\s*,\\s*version\\s*=\\s*'.*'\\s*}";


        boolean isCorrectFormat = Pattern.matches(expectedRegEx, dcsFormatString);
        if (!isCorrectFormat) {
            throw new IllegalArgumentException("Unexpected string to be parsed as DcsFormat. Expected DcsFormat string " +
                    "looks like this \"DcsFormat{format ='<format id>', " +
                                                "name='<format name>', " +
                                                "schema uri='<format schema uri>', " +
                                                "version='<format version>'}\"");
        }

        String dcsFormatValue = dcsFormatString.split("\\{")[1];
        String [] dcsFormatValueElements = dcsFormatValue.split(",");
        String dcsFormatIdElement = dcsFormatValueElements[0];
        String dcsFormatNameElement = dcsFormatValueElements[1];
        String dcsSchemaUriElement = dcsFormatValueElements[2];
        String dcsFormatVersionElement = dcsFormatValueElements[3];

        String dcsFormatIdString = dcsFormatIdElement.split("=")[1].replace('\'', ' ');
        String dcsFormatNameString = dcsFormatNameElement.split("=")[1].replace('\'', ' ');
        String dcsSchemaUriString = dcsSchemaUriElement.split("=")[1].replace('\'', ' ');
        String dcsFormatVersionString = dcsFormatVersionElement.split("=")[1].split("}")[0].replace('\'', ' ');

        dcsFormat.setFormat(dcsFormatIdString.trim());
        dcsFormat.setName(dcsFormatNameString.trim());
        dcsFormat.setSchemeUri(dcsSchemaUriString.trim());
        dcsFormat.setVersion(dcsFormatVersionString.trim());

        return dcsFormat;
    }
}
