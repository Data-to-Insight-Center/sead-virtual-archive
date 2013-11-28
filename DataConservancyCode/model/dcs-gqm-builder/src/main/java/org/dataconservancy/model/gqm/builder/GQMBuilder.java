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
package org.dataconservancy.model.gqm.builder;

import java.io.InputStream;
import java.io.OutputStream;

import org.dataconservancy.model.gqm.DateTimeInterval;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.GQMList;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.Relation;
import org.dataconservancy.model.builder.InformationLossFault;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.WellFormedXmlFault;

/**
 * The interface for serializing/deserializing the General Query Model. 
 * <h3>Contract Summary</h3>
 * <ul>
 *   <li>Clients of this interface are responsible for XML validation</li>
 *   <li>This interface always produces well-formed XML when serializing</li>
 *   <li>Information is not lost converting between object forms when valid XML is used</li>
 *   <li>Round-tripping objects results in equivalent, but not identical, objects</li>
 * </ul>
 */
public interface GQMBuilder
{
    /**
     * Builds a {@link org.dataconservancy.model.gqm.GQMList } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * General Query Model serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the GQMList object
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the GQM schema
     */
    public GQMList buildGQMList(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link org.dataconservancy.model.gqm.GQMList} to XML, formatted according
     * to the General Query Model serialization specification.
     *
     * @param gqm the General Query Model Container to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>du</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildGQMList(GQMList gqm, OutputStream sink);
    
    /**
     * Builds a {@link org.dataconservancy.model.gqm.GQM } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * General Query Model serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the GQM object
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the GQM schema
     */
    public GQM buildGQM(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link org.dataconservancy.model.gqm.GQM} to XML, formatted according
     * to the General Query Model serialization specification.
     *
     * @param gqm the General Query Model to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>du</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildGQM(GQM gqm, OutputStream sink);
    
    /**
     * Builds a {@link org.dataconservancy.model.gqm.Geometry } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * General Query Model serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the Geometry object
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the GQM schema
     */
    public Geometry buildGeometry(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link org.dataconservancy.model.gqm.Geometry} to XML, formatted according
     * to the General Query Model serialization specification.
     *
     * @param geometry the Geometry to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>du</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildGeometry(Geometry geometry, OutputStream sink);
    
    /**
     * Builds a {@link org.dataconservancy.model.gqm.DateTimeInterval } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * General Query Model serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the DateTimeInterval object
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the GQM schema
     */
    public DateTimeInterval buildDateTimeInterval(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link org.dataconservancy.model.gqm.DateTimeInterval} to XML, formatted according
     * to the General Query Model serialization specification.
     *
     * @param interval the DateTimeInterval to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>du</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildDateTimeInterval(DateTimeInterval interval, OutputStream sink);
    
    /**
     * Builds a {@link org.dataconservancy.model.gqm.Location } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * General Query Model serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the Location object
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the GQM schema
     */
    public Location buildLocation(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link org.dataconservancy.model.gqm.Location} to XML, formatted according
     * to the General Query Model serialization specification.
     *
     * @param location the Location to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>du</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildLocation(Location location, OutputStream sink);
    
    /**
     * Builds a {@link org.dataconservancy.model.gqm.Point } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * General Query Model serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the Point object
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the GQM schema
     */
    public Point buildPoint(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link org.dataconservancy.model.gqm.Point} to XML, formatted according
     * to the General Query Model serialization specification.
     *
     * @param point the Point to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>du</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildPoint(Point point, OutputStream sink);
    
    /**
     * Builds a {@link org.dataconservancy.model.gqm.Relation } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * General Query Model serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the relation object
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the GQM schema
     */
    public Relation buildRelation(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link org.dataconservancy.model.gqm.Relation} to XML, formatted according
     * to the General Query Model serialization specification.
     *
     * @param relation the Relation to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>du</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildRelation(Relation relation, OutputStream sink);
}
