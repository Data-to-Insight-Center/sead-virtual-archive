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
package org.dataconservancy.dcs.integration.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Simple utility which interpolates expressions delimited by '${' and '}'.  For example, the expression
 * <code>${dcs.baseurl}</code> could be interpolated to <code>http://localhost:8080/dcs/</code>.
 */
public class Interpolator {

    private static final Logger log = LoggerFactory.getLogger(Interpolator.class);
    
    /**
     * Interpolates expressions starting with '${' and ending with '}'.
     * <p/>
     * Note: this method does support escaped or nested expressions.
     *
     * @param sb  the StringBuilder which may contain expressions
     * @param off the offset in the StringBuilder to start interpolation
     * @param values contains values for the expressions, keyed by the expression (minus '${' and '}')
     * @return the interpolated StringBuilder
     */
    public static StringBuilder interpolate(final StringBuilder sb, final int off, final Properties values) {
        log.debug("Operating on string '{}' offset {}", sb, off);
        if (off < 0) {
            throw new IllegalArgumentException("Offset must be 0 or greater.");
        }

        if (off >= sb.length()) {
            throw new IllegalArgumentException("Offset beyond the end of the string.");
        }

        int index = -1;

        if ((index = sb.indexOf("${", off)) >= off) {
            final StringBuilder expressionSb = new StringBuilder();
            for (int i = index + 2; sb.charAt(i) != '}' && i <= sb.length(); i++) {
                expressionSb.append(sb.charAt(i));
            }

            final String expression = expressionSb.toString();

            log.debug("Extracted expression {}", expression);

            final String interpolatedValue;
            if (values.containsKey(expression)) {
                interpolatedValue = values.getProperty(expression);
            } else {
                interpolatedValue = "";
            }

            final int toIndex = index + expression.length() + "}".length() + 2;
            final String replacedSubstring = sb.substring(index, toIndex);
            log.debug("Replacing the expression substring (from {}, to {}, substring {}) with value: {}",
                    new Object[]{index, toIndex, replacedSubstring, interpolatedValue});
            sb.replace(index, toIndex, interpolatedValue);
            final int nextOff = index + interpolatedValue.length();

            if (nextOff < sb.length()) {
                return interpolate(sb, nextOff, values);
            }
        }

        return sb;
    }
}
