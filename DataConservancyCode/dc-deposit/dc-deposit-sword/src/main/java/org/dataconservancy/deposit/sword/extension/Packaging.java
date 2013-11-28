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
 * sword:packaging element.
 * <p>
 * If the POST request results in the creation of packaged resource,
 * the server MAY use this element to declare the packaging type
 * </p>
 * <p>
 * Found in:
 * <ul>
 * <li>atom:entry</li>
 * </ul>
 * </p>
 */
public class Packaging extends ElementWrapper {

    public Packaging(Element internal) {
        super(internal);
    }

    public Packaging(Factory factory, QName name) {
        super(factory, name);
    }

    /**
     * Set the packaging URI.
     *
     * @param packaging Contains a URI representing the resource packaging.
     * @throws IllegalArgumentException if the packaging is not a valid URI
     */
    public void setPackaging(String packaging) {
        try {
            setText(new URI(packaging).toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                "Packaging must be a URI: '" + packaging + "'");
        }
    }

    /**
     * Get packaging URI
     */
    public String getPackaging() {
        return getText();
    }

}
