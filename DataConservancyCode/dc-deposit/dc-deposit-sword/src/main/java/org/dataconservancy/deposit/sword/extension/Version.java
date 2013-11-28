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
 * sword:version element.
 * <p>
 * SHOULD be included. Indicates the version of the specification against which
 * the server was implemented. Whilst this profile aims to be back-compatible,
 * this information may be useful for assessing compliance issues.
 * </p>
 * <p>
 * Found in:
 * <ul>
 * <li>app:service</li>
 * </ul>
 * </p>
 */
public class Version
        extends ElementWrapper {

    public Version(Element internal) {
        super(internal);
    }

    public Version(Factory factory, QName qname) {
        super(factory, qname);
    }

    /**
     * Set the SWORD version.
     */
    public void setVersion(String version) {
        setText(version);
    }

    /**
     * Get the SWORD version
     */
    public String getVersion() {
        return getText();
    }
}
