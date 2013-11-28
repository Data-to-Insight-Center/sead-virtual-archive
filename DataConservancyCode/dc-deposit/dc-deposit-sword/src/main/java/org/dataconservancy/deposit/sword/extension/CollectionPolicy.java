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

/**
 * sword:collectionPolicy element.
 * <p>
 * MAY be included. Used for a human-readable description of collection policy.
 * Include either a text description or a URI.
 * </p>
 * <p>
 * Found in:
 * <ul>
 * <li>app:collection</li>
 * </ul>
 * </p>
 */

public class CollectionPolicy extends ElementWrapper {

    public CollectionPolicy(Element internal) {
        super(internal);
    }

    public CollectionPolicy(Factory factory, QName qname) {
        super(factory, qname);
    }

    /**
     * Set policy text or URI.
     *
     * @param policy may be human-readable text, or link to such document.
     */
    public void setPolicy(String policy) {
        setText(policy);
    }

    /**
     * Get policy text or URI
     */
    public String getPolicy() {
        return getText();
    }
}
