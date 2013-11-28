/* Copyright 2012 Johns Hopkins University
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

package org.dataconservancy.ui.model.builder.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * DateTimeConverter is used to serialize and deserialize {@link DateTime}
 * objects.
 */
public class DateTimeConverter
        extends AbstractEntityConverter
        implements ConverterConstants {

    private static final DateTimeFormatter fmt = DateTimeFormat
            .forPattern("d M yyyy");

    @Override
    public void marshal(Object source,
                        HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        super.marshal(source, writer, context);

        final DateTime date = (DateTime) source;

        writer.startNode(E_DATE);
        writer.setValue(fmt.print(date));
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        DateTime date = null;

        String value = reader.getValue();
        if (!isEmptyOrNull(value)) {
            date = fmt.parseDateTime(value);
        }
        return date;
    }

    @Override
    public boolean canConvert(Class type) {
        return DateTime.class == type;
    }
}
