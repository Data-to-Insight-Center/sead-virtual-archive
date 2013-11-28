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
package org.dataconservancy.ui.model.builder.xstream;

import javax.xml.namespace.QName;

import org.dataconservancy.model.builder.xstream.DcsPullDriver;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.ContactInfo;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.joda.time.DateTime;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;

public class XstreamBusinessObjectFactory {

    private static final String ISE_MSG = "The Project Converter, Collection Converter, and DataItem Converter " +
                    "must be set before getting an instance of the BusinessObjectFactory";

    private XStream newInstance() {
        
     // QName Map
        final QNameMap qnames = new QNameMap();
        
        final String defaultnsUri ="http://dataconservancy.org/schemas/bop/1.0";
        qnames.setDefaultNamespace(defaultnsUri);

        final DcsPullDriver driver = new DcsPullDriver(qnames);
        
        // The XStream Driver
        final XStream x = new XStream(driver);
        x.setMode(XStream.NO_REFERENCES);
        
        // XStream converter, alias, and QName registrations
        x.alias(BopConverter.E_BOP, Bop.class);
        x.registerConverter(new BopConverter());
        qnames.registerMapping(new QName(defaultnsUri, BopConverter.E_BOP), Bop.class);

        x.addDefaultImplementation(Project.class, Project.class);
        x.alias(ProjectConverter.E_PROJECT, Project.class);
        x.registerConverter(new ProjectConverter());
        qnames.registerMapping(new QName(defaultnsUri, ProjectConverter.E_PROJECT), Project.class);

        x.addDefaultImplementation(Collection.class, Collection.class);
        x.alias(CollectionConverter.E_COLLECTION, Collection.class);
        x.alias(CollectionConverter.E_COL_CONTACT_INFO, ContactInfo.class);
        x.alias(CollectionConverter.E_COL_CREATOR, PersonName.class);
        x.registerConverter(new CollectionConverter());
        x.registerConverter(new ContactInfoConverter());
        x.registerConverter(new PersonNameConverter());

        qnames.registerMapping(new QName(defaultnsUri, CollectionConverter.E_COLLECTION), Collection.class);
        qnames.registerMapping(new QName(defaultnsUri, ContactInfoConverter.E_COL_CONTACT_INFO), ContactInfo.class);
        qnames.registerMapping(new QName(defaultnsUri, CollectionConverter.E_COL_CREATOR), PersonName.class);

        x.alias(DateTimeConverter.E_DATE, DateTime.class);
        x.registerConverter(new DateTimeConverter());
        qnames.registerMapping(new QName(defaultnsUri, DateTimeConverter.E_DATE), DateTime.class);

        x.alias(PersonConverter.E_PERSON, Person.class);
        x.registerConverter(new PersonConverter());
        qnames.registerMapping(new QName(defaultnsUri, PersonConverter.E_PERSON), Person.class);

        x.alias(DataItemConverter.E_DATA_ITEM, DataItem.class);
        x.registerConverter(new DataItemConverter());
        qnames.registerMapping(new QName(defaultnsUri, DataItemConverter.E_DATA_ITEM), DataItem.class);
        qnames.registerMapping(new QName(defaultnsUri, DataItemConverter.E_DEPOSITOR), Person.class);

        x.alias(DataFileConverter.E_FILE, DataFile.class);
        x.registerConverter(new DataFileConverter());
        qnames.registerMapping(new QName(defaultnsUri, DataFileConverter.E_FILE), DataFile.class);

        x.alias(MetadataFileConverter.E_METADATA_FILE, MetadataFile.class);
        x.registerConverter(new MetadataFileConverter());
        qnames.registerMapping(new QName(defaultnsUri, MetadataFileConverter.E_METADATA_FILE), MetadataFile.class);

        return x;
    }

    //This is used for the spring wiring
    public XStream createInstance() {
        return newInstance();
    }
}
