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
package org.dataconservancy.ui.model.builder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.dataconservancy.model.builder.InformationLossFault;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.WellFormedXmlFault;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.xml.sax.SAXException;

/**
 * BusinessObjectBuilder is an abstraction that provides methods to serialize and deserialize business objects ({@link org.dataconservancy.ui.model.Bop}).
 */
public interface BusinessObjectBuilder {
    
    /**
     * Builds a {@link org.dataconservancy.ui.model.Bop} from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * Bop serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the {@link org.dataconservancy.ui.model.Bop} object
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws Exception if the supplied <code>InputStream</code> is not valid according to the Project schema
     */
    public Bop buildBusinessObjectPackage(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link org.dataconservancy.ui.model.Bop } to XML, formatted according
     * to the Bop serialization specification.
     *
     * @param bop the {@link org.dataconservancy.ui.model.Bop } to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>project</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildBusinessObjectPackage(Bop bop, OutputStream sink);

    
    /**
     * Builds a {@link org.dataconservancy.ui.model.Project } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * Project serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the {@link org.dataconservancy.ui.model.Project } object
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws Exception if the supplied <code>InputStream</code> is not valid according to the Project schema
     */
    public Project buildProject(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link org.dataconservancy.ui.model.Project} to XML, formatted according
     * to the Project serialization specification.
     *
     * @param project the {@link org.dataconservancy.ui.model.Project} to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>project</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildProject(Project project, OutputStream sink);  
    
    /**
     * Builds a {@link org.dataconservancy.ui.model.Collection } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * Collection serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the {@link org.dataconservancy.ui.model.Collection} object
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws Exception if the supplied <code>InputStream</code> is not valid according to the Collection schema
     */
    public Collection buildCollection(InputStream in) throws InvalidXmlException;
    
    /**
     * Serializes the supplied {@link org.dataconservancy.ui.model.Collection} to XML, formatted according
     * to the Collection serialization specification.
     *
     * @param collection the {@link org.dataconservancy.ui.model.Collection} to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>project</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildCollection(Collection collection, OutputStream sink);

    /**
     * Builds a {@link org.dataconservancy.ui.model.Collection } from the supplied <code>InputStream</code>.
     * <p/>
     * The <code>InputStream</code> should be a reference to an XML document fragment, formatted according to the
     * Person serialization specification.  The <code>InputStream</code> will be deserialized into
     * the corresponding Data Conservancy java object and returned.
     *
     * @param in the <code>InputStream</code>, must not be <code>null</code>
     * @return the {@link org.dataconservancy.ui.model.Person} object
     * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
     * @throws InformationLossFault if information loss would occur during deserialization
     * @throws Exception if the supplied <code>InputStream</code> is not valid according to the Person schema
     */
    public Person buildPerson(InputStream in) throws InvalidXmlException;

    /**
     * Serializes the supplied {@link org.dataconservancy.ui.model.Person} to XML, formatted according
     * to the Person serialization specification.
     *
     * @param person the {@link org.dataconservancy.ui.model.Person} to be serialized, must not be <code>null</code>
     * @param sink the output sink, must not be <code>null</code>
     * @throws IllegalArgumentException if <code>project</code> or <code>sink</code> are <code>null</code>
     * @throws InformationLossFault if information loss would occur during serialization
     * @throws WellFormedXmlFault if well-formed XML cannot be produced as a result of serialization
     */
    public void buildPerson(Person person, OutputStream sink);

    public DataFile buildDataFile(InputStream in) throws InvalidXmlException;

    public void buildDataFile(DataFile dataFile, OutputStream sink);

    public MetadataFile buildMetadataFile(InputStream in) throws InvalidXmlException;

    public void buildMetadataFile(MetadataFile metadataFile, OutputStream sink);
}