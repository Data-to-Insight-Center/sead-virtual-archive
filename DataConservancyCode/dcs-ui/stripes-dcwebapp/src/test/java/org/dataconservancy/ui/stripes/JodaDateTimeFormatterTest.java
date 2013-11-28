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

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.dataconservancy.ui.stripes.ext.JodaDateTimeFormatter;
import org.joda.time.DateTime;
import org.junit.Test;

public class JodaDateTimeFormatterTest {

    @Test
    public void testFormatWrapping() {
        JodaDateTimeFormatter fmt = new JodaDateTimeFormatter();

        // locale must be set and then init called for wrapped DateFormatter
        fmt.setLocale(Locale.getDefault());
        fmt.init();
        
        DateTime dt = new DateTime();
        String s = fmt.format(dt);

        assertTrue(s != null);
    }
}
