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
 * sword:service element.
 * <p>
 * 0 or more MAY be included to direct clients to nested service definitions.
 * If present, the value MUST be a URI that dereferences
 * to another SWORD Service Document.
 * </p>
 * <p>
 * Found in:
 * <ul>
 * <li>atom:collection</li>
 * </ul>
 * </p>
 */
public class Service extends ElementWrapper {

    public Service(Element internal) {
        super(internal);
    }

    public Service(Factory factory, QName qname) {
        super(factory, qname);
    }

    /**
     * Set the URI of a nested service document.
     *
     * @param uri URI of service doc.
     */
    public void setServiceDocumentURI(String uri) {
        setText(uri);
    }

    /**
     * Get the URI of a nested service document
     */
    public String getServiceDocumentURI() {
        return getText();
    }
}
