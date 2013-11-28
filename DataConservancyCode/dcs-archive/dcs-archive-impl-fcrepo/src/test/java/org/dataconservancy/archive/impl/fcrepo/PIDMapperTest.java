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
package org.dataconservancy.archive.impl.fcrepo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PIDMapperTest {

    @Test (expected=IllegalArgumentException.class)
    public void badNamespaceNull() {
        new PIDMapper(null);
    }

    @Test (expected=IllegalArgumentException.class)
    public void badNamespaceTooShort() {
        new PIDMapper("");
    }

    @Test (expected=IllegalArgumentException.class)
    public void badNamespaceTooLong() {
        new PIDMapper("waytoolong" +
                      "waytoolong" +
                      "waytoolong" +
                      "waytoolong" +
                      "12345678"); // 48 chars
    }

    @Test (expected=IllegalArgumentException.class)
    public void badNamespaceIllegalChar() {
        new PIDMapper("slash/is/illegal");
    }

    @Test
    public void goodNamespaces() {
        new PIDMapper("a");
        new PIDMapper("1");
        new PIDMapper("goodNamespace");
        new PIDMapper("good-namespace");
        new PIDMapper("good.namespace");
        new PIDMapper("reallylong" +
                      "reallylong" +
                      "reallylong" +
                      "reallylong" +
                      "ButOkay"); // 47 chars
    }

    @Test
    public void expectedMappings() {
        PIDMapper m = new PIDMapper("dcs");
        assertEquals("dcs:acbd18db4cc2f85cedef654fccc4a4d8", m.getPID("foo"));
        assertEquals("dcs:37b51d194a7513e45b56f6524f2d51f2", m.getPID("bar"));
        assertEquals("dcs:d32c3327f10dbcef936e8b13d550b45c",
                     m.getPID("http://example.org/this/is/a/really/long" +
                     "/url;it+is+over+64+chars+which+is+fedoras+pid+limit"));
    }

}
