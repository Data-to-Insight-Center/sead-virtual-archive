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
 * sword:verbose element.
 * <p>
 * MAY be included. Indicates whether the server supports the verbose developer feature
 * </p>
 * <p>
 * Found in:
 * <ul>
 * <li>app:service</li>
 * </ul>
 * </p>
 */
public class Verbose extends ElementWrapper {

    public Verbose(Element internal) {
        super(internal);
    }

    public Verbose(Factory factory, QName qname) {
        super(factory, qname);
    }

    /**
     * Set "verbose supported"
     *
     * @param value 'true' if verbose responses are supported.
     */
    public void setVerbose(boolean value) {
        setText(value ? "true" : "false");
    }

    /**
     * Get "verbose supported".
     */
    public boolean getVerbose() {
        return Boolean.parseBoolean(getText());
    }
}
