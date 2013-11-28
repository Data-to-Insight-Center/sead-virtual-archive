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

package org.dataconservancy.ui.stripes;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.stripes.ext.PersonListFormatter;

import static org.junit.Assert.assertTrue;

public class PersonListFormatterTest {

    @Test
    public void testFormatIncludesEmail() {
        PersonListFormatter fmt = new PersonListFormatter();
        fmt.init();
        Person p = new Person();
        p.setFirstNames("Moo");
        p.setLastNames("Grr");
        p.setEmailAddress("cow@cud.com");

        Person p2 = new Person();
        p2.setFirstNames("Moo2");
        p2.setLastNames("Grr2");
        p2.setEmailAddress("cow2@cud.com");

        List<Person> list = new ArrayList<Person>();
        list.add(p);
        list.add(p2);

        String s = fmt.format(list);

        assertTrue(s.contains(p.getEmailAddress()));
        assertTrue(s.contains(p2.getEmailAddress()));
    }
}
