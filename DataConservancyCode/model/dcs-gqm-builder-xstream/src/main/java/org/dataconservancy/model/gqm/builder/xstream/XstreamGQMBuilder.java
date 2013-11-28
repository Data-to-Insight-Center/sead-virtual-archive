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

import java.io.InputStream;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.GQMList;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.model.gqm.builder.GQMBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.support.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XstreamGQMBuilder implements GQMBuilder
{
    /**
     * Error deserializing a stream.
     * Parameters: reason
     */
    private final static String DESER_ERR = "Error encountered deserializing a stream: %s";

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private XStream x;

    public XstreamGQMBuilder() {
        x = XstreamGQMFactory.newInstance();
    }
    
    @Override
    public GQMList buildGQMList(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);
        final GQMList gqms;
        try {
            gqms = (GQMList) x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return gqms;
    }
    
    @Override
    public GQM buildGQM(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);
        final GQM gqm;
        try {
            gqm = (GQM) x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return gqm;
    }
   
    @Override
    public Geometry buildGeometry(InputStream in) throws InvalidXmlException{
        Assertion.notNull(in);
        final Geometry geometry;
        try {
            geometry = (Geometry) x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return geometry;
    }
    
    @Override
    public DateTimeInterval buildDateTimeInterval(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);
        final DateTimeInterval dti;
        try {
            dti = (DateTimeInterval) x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }        
        return dti;
    }

    @Override
    public Location buildLocation(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);
        final Location location;
        try {
            location = (Location) x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return location;
    }
     
    @Override
    public Point buildPoint(InputStream in) throws InvalidXmlException{
       Assertion.notNull(in);
       final Point point;
       try {
           point = (Point) x.fromXML(in);
       } catch (StreamException e) {
           log.debug(String.format(DESER_ERR, e.getMessage()), e);
           throw new InvalidXmlException(e);
       }
       return point;
    }
    
    @Override
    public Relation buildRelation(InputStream in) throws InvalidXmlException{
        Assertion.notNull(in);
        final Relation relation;
        try {
            relation = (Relation) x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return relation;
    }
    
    @Override
    public void buildGQMList(GQMList list, OutputStream sink) {
        Assertion.notNull(list);
        Assertion.notNull(sink);
        x.toXML(list, sink);
    }
    
    @Override
    public void buildGQM(GQM gqm, OutputStream sink) {
        Assertion.notNull(gqm);
        Assertion.notNull(sink);
        x.toXML(gqm, sink);
    }
    
    @Override
    public void buildGeometry(Geometry geometry, OutputStream sink) {
        Assertion.notNull(geometry);
        Assertion.notNull(sink);
        x.toXML(geometry, sink);
    }
   
    @Override
    public void buildDateTimeInterval(DateTimeInterval interval,
                                      OutputStream sink) {
        Assertion.notNull(interval);
        Assertion.notNull(sink);
        x.toXML(interval, sink);
    }

    @Override
    public void buildLocation(Location location, OutputStream sink) {
        Assertion.notNull(location);
        Assertion.notNull(sink);
        x.toXML(location, sink);
    }

    @Override
    public void buildPoint(Point point, OutputStream sink) {
        Assertion.notNull(point);
        Assertion.notNull(sink);
        x.toXML(point, sink);
    }

    @Override
    public void buildRelation(Relation relation, OutputStream sink) {
        Assertion.notNull(relation);
        Assertion.notNull(sink);
        x.toXML(relation, sink);
    }
    
}
