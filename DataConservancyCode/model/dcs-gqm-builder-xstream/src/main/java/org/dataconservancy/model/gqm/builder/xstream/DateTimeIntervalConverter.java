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
package org.dataconservancy.model.gqm.builder.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

public class DateTimeIntervalConverter extends AbstractEntityConverter
{
    /**
     * The DateTimeInterval element name.
     */
    public static final String E_DATETIMEINTERVAL = "DateTimeInterval";
    
    /**
     * The Start attribute name.
     */
    public static final String A_START = "start";
    
    /**
     * The End attribute name.
     */
    public static final String A_END = "end";
    
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(source, writer, context);
        
        final DateTimeInterval interval = (DateTimeInterval) source;
        
        if( interval.getStart() > -1){
            writer.addAttribute(A_START, String.valueOf(interval.getStart()));            
        }
        
        if( interval.getEnd() > -1){
            writer.addAttribute(A_END, String.valueOf(interval.getEnd()));
        }
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        String start = reader.getAttribute(A_START);
        String end = reader.getAttribute(A_END);
        
        long startVal = -1;
        if( !isEmptyOrNull(start)){
            startVal = Long.parseLong(start);
        }
        
        long endVal = -1;
        if( !isEmptyOrNull(end)){
            endVal = Long.parseLong(end);
        }
        DateTimeInterval interval = new DateTimeInterval(startVal, endVal);
        
        return interval;
    }

    @Override
    public boolean canConvert(Class type) {
        return DateTimeInterval.class == type;
    }
    
}
