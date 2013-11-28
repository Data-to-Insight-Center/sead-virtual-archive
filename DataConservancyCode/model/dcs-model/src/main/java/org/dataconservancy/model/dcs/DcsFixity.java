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

/**
 * Models Data Conservancy fixity
 */
public class DcsFixity {
    private String algorithm;
    private String value;

    /**
     * Constructs a new DcsFixity with no state.
     */
    public DcsFixity() {

    }

    /**
     * Copy constructor for a DcsFixity.  The state of <code>toCopy</code> is copied
     * to this.  Note if {@code toCopy} is modified while construcing this DcsFixity, the
     * state of this DcsFixity is undefined.
     *
     * @param toCopy the dcs fixity to copy
     */
    public DcsFixity(DcsFixity toCopy) {
        this.algorithm = toCopy.getAlgorithm();
        this.value = toCopy.getValue();
    }

    /**
     * The algorithm used to calculate fixity
     *
     * @return the algorithm, may be {@code null}
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * The algorithm used to calculate fixity
     *
     * @param algorithm the algorithm used to calculate fixity, must not be {@code null} or the empty or zero-length
     *                  string
     * @throws IllegalArgumentException if {@code algorithm} is {@code null} or the empty or zero-length string
     */
    public void setAlgorithm(String algorithm) {
        Assertion.notEmptyOrNull(algorithm);
        this.algorithm = algorithm;
    }

    /**
     * The fixity value
     *
     * @return the fixity value, may be {@code null}
     */
    public String getValue() {
        return value;
    }

    /**
     * The fixity value
     *
     * @param value the fixity value, must not be {@code null} or the empty or zero-length string
     * @throws IllegalArgumentException if {@code value} is {@code null} or the empty or zero-length string
     */
    public void setValue(String value) {
        Assertion.notEmptyOrNull(value);
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DcsFixity dcsFixity = (DcsFixity) o;

        if (algorithm != null ? !algorithm.equals(dcsFixity.algorithm) : dcsFixity.algorithm != null) return false;
        if (value != null ? !value.equals(dcsFixity.value) : dcsFixity.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = algorithm != null ? algorithm.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "DcsFixity{" +
                "value ='" + value + '\'' +
                ", algorithm='" + algorithm + '\'' +
                '}';
    }

    public void toString(HierarchicalPrettyPrinter sb) {
        sb.appendWithIndentAndNewLine("Fixity: ");
        sb.incrementDepth();
        sb.appendWithIndent("algorithm: ").appendWithNewLine(algorithm);
        sb.appendWithIndent("value: ").appendWithNewLine(value);
        sb.decrementDepth();
    }
}
