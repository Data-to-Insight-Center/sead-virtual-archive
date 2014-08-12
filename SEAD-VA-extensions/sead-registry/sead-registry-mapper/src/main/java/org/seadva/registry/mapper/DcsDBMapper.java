package org.seadva.registry.mapper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.dataconservancy.model.dcs.*;
import org.seadva.model.*;
import org.seadva.model.pack.ResearchObject;
import org.seadva.registry.client.RegistryClient;
import org.seadva.registry.database.model.obj.vaRegistry.*;
import org.seadva.registry.database.model.obj.vaRegistry.Collection;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Maps SIP to client POST calls to registry and maps GET client calls to registry to reconstructed SIP
 */
public class DcsDBMapper {

    RegistryClient client;

    public DcsDBMapper(String registryUrl){
        client = new RegistryClient(registryUrl);
    }

    /**
     * Converts SIP into smaller entities and makes POST calls through REST client to insert into Registry
     * @param sip
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */

    public void mapfromSip(ResearchObject sip) throws IOException, ClassNotFoundException, ParseException {

        Map<String, Collection> collectionMap = new HashMap<String, Collection>();
        java.util.Collection<DcsDeliverableUnit> deliverableUnits =  sip.getDeliverableUnits();
        for(DcsDeliverableUnit du:deliverableUnits){

            Collection collection = new Collection();
            collection.setId(du.getId());
            collection.setName(du.getTitle());
            collection.setVersionNum("1");
            collection.setIsObsolete(0);
            collection.setEntityName(du.getTitle());
            collection.setEntityCreatedTime(new Date());
            collection.setEntityLastUpdatedTime(new Date());

            collectionMap.put(du.getId(), collection);

           //Abstract
            Property property;
            MetadataType metadataType;
            if(((SeadDeliverableUnit)du).getAbstrct()!=null){
                property = new Property();
                metadataType = client.getMetadataByType(DcsDBField.CoreMetadataField.ABSTRACT.dbPropertyName());
                if(metadataType!=null){
                    property.setMetadata(metadataType);
                    int end = ((SeadDeliverableUnit)du).getAbstrct().length()-1;
                    if(end>1020)
                        end = 1020;
                    property.setValuestr(((SeadDeliverableUnit)du).getAbstrct().substring(0,end+1));
                    property.setEntity(collection);
                    collection.addProperty(property);
                }
            }

            if(du.getType()!=null)
                collection.setState((State) client.getStateByName(du.getType()));

            List<Property> properties = new ArrayList<Property>();
            for(DcsMetadata metadata:du.getMetadata()){
                XStream xStream = new XStream(new DomDriver());
                xStream.alias("map",Map.class);
                Map<String,String> map = (Map<String, String>) xStream.fromXML(metadata.getMetadata());
                Iterator iterator = map.entrySet().iterator();

                while(iterator.hasNext()){
                    Map.Entry<String, String> pair = (Map.Entry<String, String>) iterator.next();
                    String[] arr = pair.getKey().split("/");
                    String element = arr[arr.length-1];
                    metadataType = client.getMetadataByType(element);
                    if(metadataType.getId()==null)
                        continue;
                    if(metadataType!=null){
                        property = new Property();
                        property.setMetadata(metadataType);
                        property.setValuestr(pair.getValue());
                        property.setEntity(collection);
                        properties.add(property);
                    }
                    break;//Since we are adding only one metadata pair per DcsMetadata
                }

            }
            for(Property property1:properties)
                collection.addProperty(property1);

            SeadDataLocation seadDataLocation = ((SeadDeliverableUnit)du).getPrimaryLocation();
            if(seadDataLocation!=null&&seadDataLocation.getLocation()!=null){
                DataLocation dataLocation = new DataLocation();
                DataLocationPK dataLocationPK = new DataLocationPK();
                Repository repository = client.getRepositoryByName(seadDataLocation.getName());
                dataLocationPK.setLocationType(repository);
                dataLocation.setId(dataLocationPK);
                dataLocation.setIsMasterCopy(1);
                dataLocation.setLocationValue(seadDataLocation.getLocation());
                collection.addDataLocation(dataLocation);
            }

            Set<SeadDataLocation> secondaryLocations = ((SeadDeliverableUnit)du).getSecondaryDataLocations();
            for(SeadDataLocation secondaryLocation: secondaryLocations)
            {
                if(secondaryLocation!=null&&secondaryLocation.getLocation()!=null){
                DataLocation dataLocation = new DataLocation();
                DataLocationPK dataLocationPK = new DataLocationPK();
                Repository repository = client.getRepositoryByName(secondaryLocation.getName());
                dataLocationPK.setLocationType(repository);
                dataLocation.setId(dataLocationPK);
                dataLocation.setIsMasterCopy(0);
                dataLocation.setLocationValue(secondaryLocation.getLocation());
                collection.addDataLocation(dataLocation);
                }
            }

            for(DcsResourceIdentifier alternateId: du.getAlternateIds()){
                DataIdentifier dataIdentifier = new DataIdentifier();
                DataIdentifierPK dataIdentifierPK = new DataIdentifierPK();
                DataIdentifierType dataIdentifierType = client.getDataIdentifierType(alternateId.getTypeId());
                dataIdentifierPK.setDataIdentifierType(dataIdentifierType);
                dataIdentifier.setId(dataIdentifierPK);
                dataIdentifier.setDataIdentifierValue(alternateId.getIdValue());
                collection.addDataIdentifier(dataIdentifier);
            }

            client.postCollection(collection);

        }
        for(DcsDeliverableUnit du:deliverableUnits){
            if(du.getParents()!=null&& du.getParents().size()>0){

                for(DcsDeliverableUnitRef parent: du.getParents())
                {
                    List<AggregationWrapper> aggregationWrappers = new ArrayList<AggregationWrapper>();
                    AggregationWrapper wrapper = new AggregationWrapper();
                    if(!collectionMap.containsKey(parent.getRef())||!collectionMap.containsKey(du.getId()))
                        continue;
                    wrapper.setParent(collectionMap.get(parent.getRef()));
                    wrapper.setChild(collectionMap.get(du.getId()));
                    wrapper.setChildType(Collection.class.getName());
                    wrapper.setParentType(Collection.class.getName());
                    aggregationWrappers.add(wrapper);
                    client.postAggregation(aggregationWrappers, parent.getRef());
                }
            }
        }

        Map<String,File>  fileMap = new HashMap<String, File>();
        java.util.Collection<DcsFile> files =  sip.getFiles();
        for(DcsFile dcsFile:files){
            File file = new File();
            file.setId(dcsFile.getId());
            file.setEntityName(dcsFile.getName());
            if(((SeadFile)dcsFile).getDepositDate()!=null)
                file.setEntityCreatedTime(simpleDateFormat.parse(((SeadFile)dcsFile).getDepositDate()));
            else
                file.setEntityCreatedTime(new Date());
            if(((SeadFile)dcsFile).getMetadataUpdateDate()!=null)
                file.setEntityLastUpdatedTime(simpleDateFormat.parse(((SeadFile)dcsFile).getMetadataUpdateDate()));
            else
                file.setEntityLastUpdatedTime(new Date());
            file.setVersionNum("1");
            file.setSizeBytes(dcsFile.getSizeBytes());
            file.setIsObsolete(0);
            file.setFileName(dcsFile.getName());


            for(DcsFormat dcsFormat:dcsFile.getFormats()){
                Format format = new Format();
                format.setType(dcsFormat.getSchemeUri());
                format.setValuestr(dcsFormat.getFormat());
                file.addFormat(format);
            }


            for(DcsResourceIdentifier alternateId: (dcsFile).getAlternateIds()){
                DataIdentifier dataIdentifier = new DataIdentifier();
                DataIdentifierPK dataIdentifierPK = new DataIdentifierPK();
                DataIdentifierType dataIdentifierType = client.getDataIdentifierType(alternateId.getTypeId());
                dataIdentifierPK.setDataIdentifierType(dataIdentifierType);
                dataIdentifier.setId(dataIdentifierPK);
                dataIdentifier.setDataIdentifierValue(alternateId.getIdValue());
                file.addDataIdentifier(dataIdentifier);
            }


            SeadDataLocation seadDataLocation = ((SeadFile)dcsFile).getPrimaryLocation();
            if(seadDataLocation!=null&&seadDataLocation.getLocation()!=null){
                DataLocation dataLocation = new DataLocation();
                DataLocationPK dataLocationPK = new DataLocationPK();
                Repository repository = client.getRepositoryByName(seadDataLocation.getName());
                dataLocationPK.setLocationType(repository);
                dataLocation.setId(dataLocationPK);
                dataLocation.setIsMasterCopy(1);
                dataLocation.setLocationValue(seadDataLocation.getLocation());
                file.addDataLocation(dataLocation);
            }
            else {

            }

            List<Property> properties = new ArrayList<Property>();
            for(DcsMetadata metadata:dcsFile.getMetadata()){
                XStream xStream = new XStream(new DomDriver());
                xStream.alias("map",Map.class);
                Map<String,String> map = (Map<String, String>) xStream.fromXML(metadata.getMetadata());
                Iterator iterator = map.entrySet().iterator();

                while(iterator.hasNext()){
                    Map.Entry<String, String> pair = (Map.Entry<String, String>) iterator.next();
                    String[] arr = pair.getKey().split("/");
                    String element = arr[arr.length-1];
                    MetadataType metadataType = client.getMetadataByType(element);
                    if(metadataType.getId()==null)
                        continue;
                    if(metadataType!=null){
                        Property property = new Property();
                        property.setMetadata(metadataType);
                        property.setValuestr(pair.getValue());
                        property.setEntity(file);
                        properties.add(property);
                    }
                    break;//Since we are adding only one metadata pair per DcsMetadata
                }

            }
            for(Property property1:properties)
                file.addProperty(property1);

            fileMap.put(file.getId(), file);
            client.postFile(file);

            List<Fixity> fixities = new ArrayList<Fixity>();
            for(DcsFixity dcsFixity:dcsFile.getFixity()){
                Fixity fixity = new Fixity();
                FixityPK fixityPK = new FixityPK();
                fixityPK.setType(dcsFixity.getAlgorithm());
                fixityPK.setEntity(file);
                fixity.setId(fixityPK);
                fixity.setValuestr(dcsFixity.getValue());
                fixities.add(fixity);
            }
            client.postFixity(fixities);
        }


        java.util.Collection<DcsManifestation> manifestations =  sip.getManifestations();

        for(DcsManifestation manifestation:manifestations){

            //loop here
            for(DcsManifestationFile file:manifestation.getManifestationFiles()){
                List<AggregationWrapper> aggregationWrappers = new ArrayList<AggregationWrapper>();
                AggregationWrapper wrapper = new AggregationWrapper();
                wrapper.setParent(collectionMap.get(manifestation.getDeliverableUnit()));
                if(!fileMap.containsKey(file.getRef().getRef())){
                    System.out.println("Not found:"+file.getRef().getRef());
                    continue;
                }
                wrapper.setChild(fileMap.get(file.getRef().getRef()));
                wrapper.setChildType(File.class.getName());
                wrapper.setParentType(Collection.class.getName());
                aggregationWrappers.add(wrapper);
                client.postAggregation(aggregationWrappers, manifestation.getDeliverableUnit());//send multiple aggregations if possible
            }
        }
    }

    List<BaseEntity> baseEntities = new ArrayList<BaseEntity>();
    Map<String, List<AggregationWrapper>> aggregationMap = new HashMap<String, List<AggregationWrapper>>();



    void populateCollection(String entityId, String type) throws IOException, ClassNotFoundException {

        baseEntities.add(client.getEntity(entityId, type));
        List<AggregationWrapper> aggregationWrappers = client.getAggregation(entityId);
        for(AggregationWrapper aggregationWrapper: aggregationWrappers){
            populateCollection(aggregationWrapper.getChild().getId(), aggregationWrapper.getChildType());
        }
        if(aggregationWrappers.size()>0)
            aggregationMap.put(entityId, aggregationWrappers);
    }

    public ResearchObject getSip(String collectionId) throws IOException, ClassNotFoundException {

        populateCollection(collectionId, Collection.class.getName());

        ResearchObject sip = new ResearchObject();

        for(BaseEntity baseEntity: baseEntities){
            if(baseEntity instanceof Collection)
                sip.addDeliverableUnit(getDeliverableUnit((Collection) baseEntity));
            else if(baseEntity instanceof File)
                sip.addFile(getFile((File) baseEntity));
            else if(baseEntity instanceof Event)
                sip.addEvent(getEvent((Event) baseEntity));

        }

        //Map<String, List<Aggregation>> aggregationMap = groupAggregations(aggregations);
        Iterator iterator = aggregationMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, List<AggregationWrapper>> pair = (Map.Entry<String, List<AggregationWrapper>>) iterator.next();
            sip.addManifestation(getManifestation(pair.getKey(), pair.getValue()));
        }

        return sip;
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public SeadFile getFile(File file) throws IOException {

        SeadFile seadFile = new SeadFile();
        seadFile.setId(file.getId());
        seadFile.setName(file.getFileName());
        if(file.getSizeBytes()!=null){
            if(file.getSizeBytes()<0)
                seadFile.setSizeBytes(0);
            else
                seadFile.setSizeBytes(file.getSizeBytes());
        }

        seadFile.setDepositDate(simpleDateFormat.format(file.getEntityCreatedTime()));
        seadFile.setMetadataUpdateDate(simpleDateFormat.format(file.getEntityLastUpdatedTime()));
        if(file.getIsObsolete()==0)
            seadFile.setExtant(true);

        for(Format format:file.getFormats()){
            DcsFormat dcsFormat = new DcsFormat();
            dcsFormat.setFormat(format.getValuestr());
            dcsFormat.setSchemeUri(format.getType());
            seadFile.addFormat(dcsFormat);
        }

        Set<DataLocation> dataLocations = file.getDataLocations();
        for(DataLocation location:dataLocations){

            SeadDataLocation seadDataLocation = new SeadDataLocation();
            seadDataLocation.setType(location.getId().getLocationType().getSoftwareType());
            seadDataLocation.setName(location.getId().getLocationType().getRepositoryName());
            seadDataLocation.setLocation(location.getLocationValue());
            seadFile.setPrimaryLocation(seadDataLocation);
        }


        for(DataIdentifier dataIdentifier: file.getDataIdentifiers()){
            DcsResourceIdentifier dcsResourceIdentifier = new DcsResourceIdentifier();
            dcsResourceIdentifier.setTypeId(dataIdentifier.getId().getDataIdentifierType().getDataIdentifierTypeName());
            dcsResourceIdentifier.setIdValue(dataIdentifier.getDataIdentifierValue());
            seadFile.addAlternateId(dcsResourceIdentifier);
        }

        for(Property property:file.getProperties()){
            XStream xStream = new XStream(new DomDriver());
            xStream.alias("map",Map.class);
            Map<String,String> map = new HashMap<String, String>();
            String key = property.getMetadata().getMetadataSchema()+property.getMetadata().getMetadataElement();
            if(key.contains("FLocat"))
                seadFile.setSource(property.getValuestr());
            map.put(key,property.getValuestr());
            DcsMetadata metadata = new DcsMetadata();
            metadata.setSchemaUri(key);
            metadata.setMetadata(xStream.toXML(map));
            seadFile.addMetadata(metadata);
        }

        List<Fixity> fixityList = client.getFixity(file.getId());
        for(Fixity fixity:fixityList){
            DcsFixity dcsFixity = new DcsFixity();
            dcsFixity.setAlgorithm(fixity.getId().getType());
            dcsFixity.setValue(fixity.getValuestr());
            seadFile.addFixity(dcsFixity);
        }

        return seadFile;
    }

    public SeadEvent getEvent(Event event){

        SeadEvent seadEvent = new SeadEvent();
        seadEvent.setId(event.getId());
        for(Property property:event.getProperties()){
//            if(property.getMetadata().getMetadataElement().equals("size"))
//                seadEvent.setSizeBytes(Long.valueOf(property.getValuestr()));
        }
        return seadEvent;
    }

    public DcsDeliverableUnit getDeliverableUnit(Collection collection){

        SeadDeliverableUnit du = new SeadDeliverableUnit();
        du.setId(collection.getId());
        du.setTitle(collection.getName());


        Set<DataLocation> dataLocations = collection.getDataLocations();
        for(DataLocation location:dataLocations){
            if(location.getIsMasterCopy()==1)  {
                SeadDataLocation seadDataLocation = new SeadDataLocation();
                seadDataLocation.setType(location.getId().getLocationType().getSoftwareType());
                seadDataLocation.setName(location.getId().getLocationType().getRepositoryName());
                seadDataLocation.setLocation(location.getLocationValue());
                du.setPrimaryLocation(seadDataLocation);
            }
            else{
                SeadDataLocation seadDataLocation = new SeadDataLocation();
                seadDataLocation.setType(location.getId().getLocationType().getSoftwareType());
                seadDataLocation.setName(location.getId().getLocationType().getRepositoryName());
                seadDataLocation.setLocation(location.getLocationValue());
                du.addSecondaryDataLocation(seadDataLocation);
            }
        }

        for(DataIdentifier dataIdentifier: collection.getDataIdentifiers()){
            DcsResourceIdentifier dcsResourceIdentifier = new DcsResourceIdentifier();
            dcsResourceIdentifier.setTypeId(dataIdentifier.getId().getDataIdentifierType().getDataIdentifierTypeName());
            dcsResourceIdentifier.setIdValue(dataIdentifier.getDataIdentifierValue());
            du.addAlternateId(dcsResourceIdentifier);
        }

        du.setType(collection.getState().getStateType());

        du.setDataContributors(new HashSet<SeadPerson>());

        for(Property property:collection.getProperties()){
            if(property.getMetadata().getMetadataElement().equals(DcsDBField.CoreMetadataField.ABSTRACT.dbPropertyName()))
                du.setAbstrct(property.getValuestr());
            else if(property.getMetadata().getMetadataElement().equals(DcsDBField.CoreMetadataField.TYPE.dbPropertyName()))
                du.setType(property.getValuestr());
            else if(property.getMetadata().getMetadataElement().equals(DcsDBField.CoreMetadataField.CONTRIBUTOR.dbPropertyName()))
            {
                SeadPerson person = new SeadPerson();
                String contributor = property.getValuestr();
                if(contributor.contains(";")){
                    String[] arr = contributor.split(";");
                    person.setName(arr[0]);
                    person.setId(arr[2]);
                    person.setIdType(arr[1]);
                }
                else
                    person.setName(contributor);

                du.addDataContributor(person);
            }
            else{
                XStream xStream = new XStream(new DomDriver());
                xStream.alias("map",Map.class);
                Map<String,String> map = new HashMap<String, String>();
                String key = property.getMetadata().getMetadataSchema()+property.getMetadata().getMetadataElement();
                map.put(key,property.getValuestr());
                DcsMetadata metadata = new DcsMetadata();
                metadata.setSchemaUri(key);
                metadata.setMetadata(xStream.toXML(map));
                du.addMetadata(metadata);
            }

        }
        return du;
    }

    DcsManifestation getManifestation(String parentId, List<AggregationWrapper> aggregations){

        DcsManifestation manifestation = new DcsManifestation();
        manifestation.setId(parentId+"_man");
        manifestation.setDeliverableUnit(parentId);
        for(AggregationWrapper aggregation:  aggregations){
            DcsManifestationFile manifestationFile = new DcsManifestationFile();
            DcsFileRef fileRef = new DcsFileRef();
            fileRef.setRef((aggregation.getChild()).getId());
            manifestationFile.setRef(fileRef);
            manifestation.addManifestationFile(manifestationFile);
        }
        return manifestation;
    }

}