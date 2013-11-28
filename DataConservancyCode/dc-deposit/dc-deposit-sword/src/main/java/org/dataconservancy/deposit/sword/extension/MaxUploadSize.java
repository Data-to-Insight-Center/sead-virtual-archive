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
 * sword:maxUploadSize element.
 * <p>
 * MAY be included to indicate the maximum size (in kB) of package
 * that can be uploaded to the SWORD service.
 * </p>
 * <p>
 * Found in:
 * <ul>
 * <li>app:service</li>
 * </ul>
 * </p>
 */
public class MaxUploadSize extends ElementWrapper {

    public MaxUploadSize(Element internal) {
        super(internal);
    }

    public MaxUploadSize(Factory factory, QName qname) {
        super(factory, qname);
    }

    /**
     * Set max upload size.
     *
     * @param size Maximum size, in kB.
     */
    public void setMaxUploadSize(int size) {
        setText(Integer.toString(size));
    }

    /**
     * Get max upload size
     */
    public int getMaxUploadSize() {
        return Integer.decode(getText());
    }
}
