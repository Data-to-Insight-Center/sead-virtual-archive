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
 * sword:noOp element.
 * <p>
 * MAY be included in service document. Indicates whether the server supports
 * the No Op developer feature as described
 * </p>
 * <p>
 * In entry documents, if the client made the POST request with an X-No-Op:true
 * header, the server SHOULD reflect this by including a sword:noOp element
 * with a value of "true' in the response.  Servers MAY use a value of 'false'
 * to indicate that the deposit proceeded but MUST NOT use this element to
 * signify an error.
 * </p>
 * <p>
 * Found in:
 * <ul>
 * <li>app:service</li>
 * <li>atom:entry</li>
 * </ul>
 * </p>
 */
public class NoOp extends ElementWrapper {

    public NoOp(Element internal) {
        super(internal);
    }

    public NoOp(Factory factory, QName qname) {
        super(factory, qname);
    }

    /**
     * Set NoOp value.
     *
     * @param value 'true' indicates that a no-op is accepted or has ocurred.
     */
    public void setNoOp(boolean value) {
        setText(value ? "true" : "false");
    }

    /**
     * Get NoOp value.
     */
    public boolean getNoOp() {
        return Boolean.parseBoolean(getText());
    }

}
