/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seadva.bagit.util;

import noNamespace.*;
import org.apache.xmlbeans.XmlException;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadPerson;

import java.io.File;
import java.io.IOException;

/**
 * FGDC generator
 */
public class FgdcGenerator {
    /*public static String createFGDC(CollectionNode parentCollection){
        Set<String> creators = new HashSet<String>();
        if(parentCollection.getCreators()!=null)
            for(Creator creator:parentCollection.getCreators())
                creators.add(creator.getCreatorName()+";"+creator.getCreatorId()+";"+creator.getCreatorIdType());
        Set<String> contacts = new HashSet<String>();
        contacts.add(parentCollection.getContact());

        return FgdcUtil.makeDefaultDoc(parentCollection.getTitle(),
                creators,
                contacts,
                parentCollection.getAbstract(),
                parentCollection.getDate());
    }
*/
    public static SeadDeliverableUnit fromFGDC(String fgdcFilePath, SeadDeliverableUnit du){
        try {
            //title, abstract,creator,bounding boxes,date
            MetadataDocument metadataDoc = MetadataDocument.Factory.parse(new File(fgdcFilePath));
            IdinfoType idinfoType = metadataDoc.getMetadata().getIdinfo();
            CiteinfoType citeinfoType = idinfoType.getCitation().getCiteinfo();
            for(String creator:citeinfoType.getOriginArray()){
                String[] arr = creator.split(";");
                SeadPerson dcsCreator = new SeadPerson();
                dcsCreator.setName(arr[0]);
                dcsCreator.setId(arr[1]);
                dcsCreator.setIdType(arr[2]);
                du.addDataContributor(dcsCreator);
            }

            du.setTitle(citeinfoType.getTitle());
            du.setPubdate(citeinfoType.getPubdate());
            du.setAbstrct(idinfoType.getDescript().getAbstract());

            MetainfoType metainfoType = metadataDoc.getMetadata().getMetainfo();
            if(metainfoType!=null){
                MetcType metcType = metainfoType.getMetc();
                if(metcType!=null){
                    String contactPerson = metcType.getCntinfo().getCntperp().getCntper();
                   if(!contactPerson.equalsIgnoreCase(SeadNCEDConstants.DEFAULT_CONTACTPERSON))
                   {
                       String[] persons = contactPerson.split(";");
                       for(int i=0;i<persons.length;i++)
                           ((SeadDeliverableUnit)du).setContact(persons[i]);
                   }
                }
            }
        } catch (XmlException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return du;
    }

}
