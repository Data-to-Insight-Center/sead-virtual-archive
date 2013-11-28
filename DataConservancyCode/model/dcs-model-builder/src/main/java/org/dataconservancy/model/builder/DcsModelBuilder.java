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
package org.dataconservancy.model.builder;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * The API for (de)serializing {@link org.dataconservancy.model.dcs.DcsEntity entities} of the Data Conservancy object
 * model to XML.
 * <h3>Contract Summary</h3>
 * <ul>
 *   <li>Clients of this interface are responsible for XML validation</li>
 *   <li>This interface always produces well-formed XML when serializing</li>
 *   <li>Information is not lost converting between object forms when valid XML is used</li>
 *   <li>Round-tripping objects results in equivalent, but not identical, objects</li>
 * </ul>
 *
 * <h3>Limitations</h3>
 * <ul>
 *   <li>Currently this interface does not account for co-existance of different versions of the model.</li>
 * </ul>
 * <p/>
 *
 * <h3>Serialization</h3>
 * <p>
 * Serialization of a Data Conservancy entity from a Java object to XML should result in semantically equivalent
 * objects, with no information loss.  If information loss would result as a result of serialization, implementations
 * must throw an exception.   
 * </p>
 * <p>
 * Serialization methods of this interface <em>SHOULD</em> produce valid DCP XML.  However, the Java objects passed to
 * the serialization methods may not carry enough information to populate required elements or attributes of the
 * XML model.  Clients of this interface are responsible for asserting conformance of the produced XML to a schema.
 * </p>
 * <p>
 * Serialization methods of this interface <em>MUST</em> produce well-formed DCP XML, or throw an exception indicating
 * otherwise.
 * </p>
 *
 * <h3>Deserialization</h3>
 * <p>
 * Deserialization of a Data Conservancy Java entity to XML should result in semantically equivalent objects, with no
 * information loss.  If information loss would result as a result of deserialization, implementations
 * must throw an exception.
 * </p>
 * <p>
 * Deserialization methods of this interface <em>MUST</em> consume valid DCP XML.
 * </p>
 * <p>
 * Deserialization methods of this interface <em>MAY</em> consume well-formed (but invalid) XML.  Implementations are
 * encouraged to be lenient, and accept well-formed XML which may not conform to the DCP XML schema.  Implementations
 * that do not accept invalid DCP XML may throw an exception indicating this.  Clients of this interface are responsible
 * for asserting conformance of the provided XML to a schema.
 * </p>
 *
 * <h3>Round Tripping</h3>
 * <h4>XML to Java to XML'</h4>
 * <p>
 * This interface guarantees that round-tripped XML will be semantically <em>equivalent</em> but not necessarily
 * <em>identical</em>.  For example, round-tripped XML may differ in white-space handling, element ordering (due to
 * <code>Set</code> semantics in the Java object model), and namespace prefixes.  However, the information content
 * contained in the round-tripped XML is guaranteed to be the same.
 * </p>
 * <h4>Java to XML to Java'</h4>
 * <p>
 * This interface guarantees that round-tripped Java objects will be equal according to {@link Object#equals(Object)}.
 * </p>
 */
public interface DcsModelBuilder {

    /**
     * Builds a {@link org.dataconservancy.model.dcs.DcsDeliverableUnit} from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * Deliverable Unit DC SIP serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the DCS entity
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the DCP SIP schema
     */
    public DcsDeliverableUnit buildDeliverableUnit(InputStream in) throws InvalidXmlException;

    /**
     * Serializes the supplied {@link org.dataconservancy.model.dcs.DcsDeliverableUnit} to XML, formatted according
     * to the Deliverable Unit DC SIP serialization specification.
     *
     * @param du the Deliverable Unit to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>du</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildDeliverableUnit(DcsDeliverableUnit du, OutputStream sink);

    /**
     * Builds a {@link org.dataconservancy.model.dcs.DcsManifestation} from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * Manifestation DC SIP serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the DCS entity
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the DCP SIP schema
     */
    public DcsManifestation buildManifestation(InputStream in) throws InvalidXmlException;

    /**
     * Serializes the supplied {@link org.dataconservancy.model.dcs.DcsManifestation} to XML, formatted according
     * to the Manifestation DC SIP serialization specification.
     *
     * @param manifestation the Manifestation to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>manifestation</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildManifestation(DcsManifestation manifestation, OutputStream sink);

    /**
     * Builds a {@link org.dataconservancy.model.dcs.DcsFile} from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * File DC SIP serialization specification.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the DCS entity
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the DCP SIP schema
     */
    public DcsFile buildFile(InputStream in) throws InvalidXmlException;

    /**
     * Serializes the supplied {@link org.dataconservancy.model.dcs.DcsFile} to XML, formatted according
     * to the File DC SIP serialization specification.
     *
     * @param file the File to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>file</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildFile(DcsFile file, OutputStream sink);

    /**
     * Builds a {@link org.dataconservancy.model.dcp.Dcp} from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document, formatted according to the
     * DC SIP serialization specification.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the DCS SIP
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the DCP SIP schema
     */
    public Dcp buildSip(InputStream in) throws InvalidXmlException;

    /**
     * Serializes the supplied {@link org.dataconservancy.model.dcp.Dcp} to XML, formatted according
     * to the DC SIP serialization specification.
     *
     * @param sip the sip to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>sip</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildSip(Dcp sip, OutputStream sink);

    /**
     * Builds a {@link org.dataconservancy.model.dcs.DcsCollection} from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document, formatted according to the
     * Collection DC SIP serialization specification.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the DCS Collection
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the DCP SIP schema
     */
    public DcsCollection buildCollection(InputStream in) throws InvalidXmlException;

    /**
     * Serializes the supplied {@link org.dataconservancy.model.dcs.DcsCollection} to XML, formatted according
     * to the Collection DC SIP serialization specification.
     *
     * @param collection the Collection to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>sip</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildCollection(DcsCollection collection, OutputStream sink);
    
    /**
     * Builds a {@link org.dataconservancy.model.dcs.DcsEvent} from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document, formatted according to the
     * Event DC SIP serialization specification.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the DCS Event
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws InvalidXmlException if the supplied <code>InputStream</code> is not valid according to the DCP SIP schema
     */
    public DcsEvent buildEvent(InputStream in) throws InvalidXmlException;

    /**
     * Serializes the supplied {@link org.dataconservancy.model.dcs.DcsEvent} to XML, formatted according
     * to the Event DC SIP serialization specification.
     *
     * @param event the Event to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>sip</code> or <code>sink</code> are <code>null</code>
     */
    public void buildEvent(DcsEvent event, OutputStream sink);
}
