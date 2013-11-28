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

import javax.xml.namespace.QName;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.GQMList;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.model.builder.xstream.DcsPullDriver;

public class XstreamGQMFactory {
    
    static XStream newInstance() {
     // QName Map
        final QNameMap qnames = new QNameMap();
        
        final String defaultnsUri = "http://dataconservancy.org/schemas/gqm/1.0";
        qnames.setDefaultNamespace(defaultnsUri);

        final DcsPullDriver driver = new DcsPullDriver(qnames);
        
        // The XStream Driver
        final XStream x = new XStream(driver);
        
        // XStream converter, alias, and QName registrations
        x.alias(DateTimeIntervalConverter.E_DATETIMEINTERVAL, DateTimeInterval.class);
        x.registerConverter(new DateTimeIntervalConverter());
        qnames.registerMapping(new QName(defaultnsUri, DateTimeIntervalConverter.E_DATETIMEINTERVAL), DateTimeInterval.class);

        x.alias(GeometryConverter.E_GEOMETRY, Geometry.class);
        x.registerConverter(new GeometryConverter());
        qnames.registerMapping(new QName(defaultnsUri, GeometryConverter.E_GEOMETRY), Geometry.class);
        
        x.alias(LocationConverter.E_LOCATION, Location.class);
        x.registerConverter(new LocationConverter());
        qnames.registerMapping(new QName(defaultnsUri, LocationConverter.E_LOCATION), Location.class);
        
        x.alias(PointConverter.E_POINT, Point.class);
        x.registerConverter(new PointConverter());
        qnames.registerMapping(new QName(defaultnsUri, PointConverter.E_POINT), Point.class);
        
        x.alias(RelationConverter.E_RELATION, Relation.class);
        x.registerConverter(new RelationConverter());
        qnames.registerMapping(new QName(defaultnsUri, RelationConverter.E_RELATION), Relation.class);
        
        x.alias(GqmConverter.E_GQM, GQM.class);
        x.registerConverter(new GqmConverter());
        qnames.registerMapping(new QName(defaultnsUri, GqmConverter.E_GQM), GQM.class);
        
        x.alias(GqmListConverter.E_GQMLIST, GQMList.class);
        x.registerConverter(new GqmListConverter());
        qnames.registerMapping(new QName(defaultnsUri, GqmListConverter.E_GQMLIST), GQMList.class);
        return x;
    }
}
