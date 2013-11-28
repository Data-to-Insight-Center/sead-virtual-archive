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
 * sword:userAgent element.
 * <p>
 * Clients SHOULD provide a User-Agent request-header
 * (as described in [HTTP1.1] section 14.43). If provided, servers SHOULD
 * store the value in the sword:userAgent element.
 * </p>
 * <p>
 * Found in:
 * <ul>
 * <li>atom:entry</li>
 * </ul>
 * </p>
 */
public class UserAgent extends ElementWrapper {

    public UserAgent(Element internal) {
        super(internal);
    }

    public UserAgent(Factory factory, QName name) {
        super(factory, name);
    }

    /**
     * Set http user agent
     */
    public void setUserAgent(String agent) {
        setText(agent);
    }

    /**
     * Get http agent
     */
    public String getUserAgent() {
        return getText();
    }
}
