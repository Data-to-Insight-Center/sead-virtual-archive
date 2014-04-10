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
package org.seadva.bagit.event.impl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.*;
import org.seadva.bagit.event.api.Handler;
import org.seadva.bagit.model.AggregationType;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.util.Constants;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadPerson;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Handler to generate SIP from ORE
 */
public class SipParseHandler implements Handler {

    @Override
    public PackageDescriptor execute(PackageDescriptor packageDescriptor) {
        aggregation = packageDescriptor.getAggregation();
        properties = packageDescriptor.getProperties();
        typeProperty = packageDescriptor.getType();

        try {
            parseSIP(new SeadXstreamStaxModelBuilder().buildSip(new FileInputStream(packageDescriptor.getSipPath())));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidXmlException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        packageDescriptor.setPackageId(rootId);
        packageDescriptor.setAggregation(aggregation);
        packageDescriptor.setProperties(properties);
        packageDescriptor.setType(typeProperty);
        return packageDescriptor;
    }


    String rootId;
    Map<String,List<String>> aggregation;
    Map<String,Map<String,List<String>>> properties;
    Map<String,AggregationType> typeProperty;



    public void parseSIP(ResearchObject sip) throws IOException {//top collection id

        Collection<DcsDeliverableUnit> deliverableUnits = sip.getDeliverableUnits();
        for(DcsDeliverableUnit du: deliverableUnits){
            if(du.getParents()==null||du.getParents().size()==0)
                rootId = du.getId();
            typeProperty.put(du.getId(), AggregationType.COLLECTION);
            Map<String,List<String>> propertyValues = new HashMap<String, List<String>>();

            List<String> idList = new ArrayList<String>();
            idList.add(du.getId());

            for(DcsResourceIdentifier identifier: du.getAlternateIds()){
                idList.add(identifier.getIdValue());
            }

            propertyValues.put(Constants.identifierTerm, idList);

            List<String> titleList = new ArrayList<String>();
            titleList.add(du.getTitle());
            propertyValues.put(Constants.titleTerm, titleList);

            List<String> rightsList = new ArrayList<String>();
            rightsList.add(du.getRights());
            propertyValues.put(Constants.rightsTerm, rightsList);

            SeadDeliverableUnit seadDeliverableUnit = (SeadDeliverableUnit)du;

            List<String> creatorList = new ArrayList<String>();
            for(SeadPerson person: seadDeliverableUnit.getDataContributors())
                creatorList.add(person.getName());//+";"+person.getId());
            propertyValues.put(Constants.contributor, creatorList);

            List<String> metadataRefList = new ArrayList<String>();
            for(DcsMetadataRef metadataRef: seadDeliverableUnit.getMetadataRef())
                metadataRefList.add(metadataRef.getRef());
            propertyValues.put(Constants.documentedBy, metadataRefList);

            List<String> abstractList = new ArrayList<String>();
            abstractList.add(seadDeliverableUnit.getAbstrct());
            propertyValues.put(Constants.abstractTerm, abstractList);



            for(DcsMetadata metadata: du.getMetadata()){
                List<String> otherMdList = new ArrayList<String>();
                XStream xStream = new XStream(new DomDriver());
                Map<String,Object> map = (Map<String,Object>) xStream.fromXML(metadata.getMetadata());
                Iterator itr = map.entrySet().iterator();
                while(itr.hasNext()){
                    Map.Entry<String,String> pair = (Map.Entry<String, String>) itr.next();
                    if(propertyValues.containsKey(pair.getKey()))
                        otherMdList = propertyValues.get(pair.getKey());
                    otherMdList.add(pair.getValue());
                    if(pair.getKey()!=null)
                        propertyValues.put(pair.getKey(), otherMdList);
                    break;
                }
            }

            properties.put(du.getId(), propertyValues);
            if(du.getParents()!=null){
                for(DcsDeliverableUnitRef parent: du.getParents()){
                    List<String> children = aggregation.get(parent.getRef());
                    if(children==null)
                        children = new ArrayList<String>();
                    children.add(du.getId());
                    aggregation.put(parent.getRef(),children);
                }
            }
        }

        Collection<DcsFile> files = sip.getFiles();
        for(DcsFile file: files){
            typeProperty.put(file.getId(), AggregationType.FILE);
            Map<String,List<String>> propertyValues = new HashMap<String, List<String>>();

            List<String> idList = new ArrayList<String>();
            idList.add(file.getId());

            for(DcsResourceIdentifier identifier: file.getAlternateIds()){
                idList.add(identifier.getIdValue());
            }

            propertyValues.put(Constants.identifierTerm, idList);

            List<String> titleList = new ArrayList<String>();
            titleList.add(file.getName());
            propertyValues.put(Constants.titleTerm, titleList);

            List<String> sizeList = new ArrayList<String>();
            sizeList.add(String.valueOf(file.getSizeBytes()));
            propertyValues.put(Constants.sizeTerm, sizeList);

            List<String> formatList = new ArrayList<String>();
            for(DcsFormat format: file.getFormats()){
                formatList.add(format.getFormat());
            }
            propertyValues.put(Constants.formatTerm, formatList);


            if(file.getSource()!=null){
                List<String> locationList = new ArrayList<String>();
                locationList.add(file.getSource());
                propertyValues.put(Constants.sourceTerm, locationList);
            }

            List<String> metadataRefList = new ArrayList<String>();
            for(DcsMetadataRef metadataRef: file.getMetadataRef())
                metadataRefList.add(metadataRef.getRef());
            propertyValues.put(Constants.documentedBy, metadataRefList);

            for(DcsMetadata metadata: file.getMetadata()){
                List<String> otherMdList = new ArrayList<String>();
                XStream xStream = new XStream(new DomDriver());
                Map<String,Object> map = (Map<String,Object>) xStream.fromXML(metadata.getMetadata());
                Iterator itr = map.entrySet().iterator();
                while(itr.hasNext()){
                    Map.Entry<String,String> pair = (Map.Entry<String, String>) itr.next();
                    if(propertyValues.containsKey(pair.getKey()))
                        otherMdList = propertyValues.get(pair.getKey());
                    otherMdList.add(pair.getValue());
                    if(pair.getKey()!=null)
                        propertyValues.put(pair.getKey(), otherMdList);
                    break;
                }
            }

            properties.put(file.getId(), propertyValues);
        }

        Collection<DcsManifestation> manifestations = sip.getManifestations();
        for(DcsManifestation manifestation: manifestations){

            if(manifestation.getDeliverableUnit()!=null){
                List<String> children = aggregation.get(manifestation.getDeliverableUnit());
                for(DcsManifestationFile child: manifestation.getManifestationFiles()){
                    if(children==null)
                        children = new ArrayList<String>();
                    children.add(child.getRef().getRef());
                }
                aggregation.put(manifestation.getDeliverableUnit(), children);
            }
        }
    }
}