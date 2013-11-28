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
 * sword:verboseDescription element.
 * <p>
 * If the client made the POST request with an X-Verbose:true header,
 * the server SHOULD supply a verbose description of the deposit process.
 * </p>
 * <p>
 * Found in:
 * <ul>
 * <li>atom:entry</li>
 * </ul>
 * </p>
 */
public class VerboseDescription extends ElementWrapper {

    public VerboseDescription(Element internal) {
        super(internal);
    }

    public VerboseDescription(Factory factory, QName qname) {
        super(factory, qname);
    }

    /**
     * Get verbose description
     */
    public void setVerboseDescription(String desc) {
        setText(desc);
    }

    /**
     * Set verbose description
     */
    public String getVerboseDescription() {
        return getText();
    }
}
