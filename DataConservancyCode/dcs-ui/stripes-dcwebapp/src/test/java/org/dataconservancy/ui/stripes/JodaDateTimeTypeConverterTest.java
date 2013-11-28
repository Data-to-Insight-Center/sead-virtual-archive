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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import net.sourceforge.stripes.validation.ValidationError;

import org.dataconservancy.ui.stripes.ext.JodaDateTimeTypeConverter;
import org.joda.time.DateTime;
import org.junit.Test;

public class JodaDateTimeTypeConverterTest extends BaseActionBeanTest {

    @Test
    public void testWrapping() {
        JodaDateTimeTypeConverter tc = new JodaDateTimeTypeConverter();

        // must set locale for the wrapped stripes DateTypeConverter
        tc.setLocale(Locale.getDefault());
        
        Collection<ValidationError> errors = new ArrayList<ValidationError>();

        Date date = new Date();
        String s = DateFormat.getDateInstance().format(date);
        DateTime dt = tc.convert(s, DateTime.class, errors);

        assertTrue(dt != null);
        assertEquals(0, errors.size());
        
        // Cannot test time equality because of variance between Joda DateTime and Java Date
        // assertEquals(date.getTime(), dt.getMillis());
    }
}
