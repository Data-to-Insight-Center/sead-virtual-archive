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



import java.net.URI;
import java.net.URISyntaxException;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.model.builder.xstream.AbstractEntityConverter;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

public class RelationConverter extends AbstractEntityConverter
{
    /**
     * The Relation Element name.
     */
    public static final String E_RELATION = "Relation";
    /**
     * The Predicate Attribute name.
     */
    public static final String A_PREDICATE = "predicate";
    /**
     * The Object Attribute name.
     */
    public static final String A_OBJECT = "object";
    
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context){
        super.marshal(source, writer, context);
        
        Relation relation = (Relation) source;
        
        if( !isEmptyOrNull(relation.getObject())) {
            writer.addAttribute(A_OBJECT, relation.getObject());
        }
        
        if( !isEmptyOrNull(relation.getPredicate().toString())){
            writer.addAttribute(A_PREDICATE, relation.getPredicate().toString());
        }        
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        final String object = reader.getAttribute(A_OBJECT);
        URI predicate = null;
        
        if(!isEmptyOrNull(reader.getAttribute(A_PREDICATE))){
            try {
                predicate = new URI(reader.getAttribute(A_PREDICATE));
            } catch (URISyntaxException e) {
            }
        }
        
        return new Relation(predicate, object);
    }

    @Override
    public boolean canConvert(Class type) {
       return Relation.class == type;
    }
    
}
