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
package org.dataconservancy.deposit.sword.extension;

import javax.xml.namespace.QName;

import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.ElementWrapper;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * sword:acceptPackaging element.
 * <p>
 * Denotes SWORD accepted packaging in a collection.  Used to identify the
 * content packaging types supported by this collection. SHOULD be a URI from
 * [SWORD-TYPES]. The q attribute MAY be used to indicate relative preferences
 * between packaging formats
 * </p>
 * <p>
 * Found in:
 * <ul>
 * <li>app:collection</li>
 * </ul>
 * </p>
 *
 * @see <a href="http://www.swordapp.org/docs/sword-type-1.0.html">SWORD-TYPES</a>
 */
public class AcceptPackaging extends ElementWrapper {

    /**
     * Preference attribute name
     */
    public static final String PREFERENCE_ATTRIBUTE = "q";

    public AcceptPackaging(Element internal) {
        super(internal);
    }

    public AcceptPackaging(Factory factory, QName qname) {
        super(factory, qname);
    }

    /**
     * Specify an accepted packaging format.
     *
     * @param packaging URI.  SHOULD be a URI from [SWORD-TYPES]
     */
    public void setAcceptedPackaging(String packaging) {
        try {
            setText(new URI(packaging).toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                "Packaging must be a URI: '" + packaging + "'");
        }
    }

    /**
     * Get the acceptted packaging format URI.
     */
    public String getAcceptedPackaging() {
        return getText();
    }

    /**
     * Set the relative preference of this packaging.
     *
     * @param priority String representing relative preference.
     *                 Should have a numeric value between 0 and 1.
     */
    public void setPreference(String priority) {
        setAttributeValue(PREFERENCE_ATTRIBUTE, priority);
    }

    /**
     * Get the relative preference of this packaging.
     *
     * @return String value with a numeric representation between 0 and 1.
     *         May be null if there is no preference.
     */
    public String getPreference() {
        return getAttributeValue(PREFERENCE_ATTRIBUTE);
    }

    /**
     * Determines whether this packaging has a relative preference.
     */
    public boolean hasPreference() {
        return getAttributeValue(PREFERENCE_ATTRIBUTE) != null;
    }

}
