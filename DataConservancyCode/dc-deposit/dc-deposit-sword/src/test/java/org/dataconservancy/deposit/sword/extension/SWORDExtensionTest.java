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

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Element;

import java.io.StringReader;
import java.io.StringWriter;

public abstract class SWORDExtensionTest {
    private static Factory factory = new Abdera().getFactory();

    public static Factory getFactory() {
        return factory;
    }

    /**
     * Serialize, deserialize, and return a wrapped extension element.
     */
    public static Element reconstitute(Element in) {
        try {
            StringWriter w = new StringWriter();
            in.writeTo(w);

            StringReader r = new StringReader(w.toString());

            return factory.newParser().parse(r).getRoot();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
