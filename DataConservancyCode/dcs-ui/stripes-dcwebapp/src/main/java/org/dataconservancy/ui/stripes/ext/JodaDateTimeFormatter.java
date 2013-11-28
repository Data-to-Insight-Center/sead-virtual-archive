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
package org.dataconservancy.ui.stripes.ext;

import java.util.Locale;

import org.joda.time.DateTime;

import net.sourceforge.stripes.format.DateFormatter;
import net.sourceforge.stripes.format.Formatter;

/**
 * Format a Joda DateTime by reusing the standard stripes DateFormatter.
 */
public class JodaDateTimeFormatter implements Formatter<DateTime> {
    private DateFormatter fmt = new DateFormatter();

    public String format(DateTime dt) {
        return fmt.format(dt.toDate());
    }

    public void init() {
        fmt.init();
    }

    public void setFormatPattern(String pat) {
        fmt.setFormatPattern(pat);
    }

    public void setFormatType(String type) {
        fmt.setFormatType(type);
    }

    public void setLocale(Locale locale) {
        fmt.setLocale(locale);
    }
}
