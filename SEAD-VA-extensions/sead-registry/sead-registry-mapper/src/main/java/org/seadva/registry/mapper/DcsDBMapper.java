package org.seadva.registry.mapper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.dataconservancy.model.dcs.*;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadEvent;
import org.seadva.model.SeadFile;
import org.seadva.model.pack.ResearchObject;
import org.seadva.registry.client.RegistryClient;
import org.seadva.registry.database.factories.vaRegistry.HibernateVaRegistryDaoFactory;
import org.seadva.registry.database.model.obj.vaRegistry.*;
import org.seadva.registry.database.model.obj.vaRegistry.Collection;
import org.seadva.registry.database.services.data.DataLayerVaRegistry;
import org.seadva.registry.database.services.data.DataLayerVaRegistryImpl;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kavchand
 * Date: 4/8/14
 * Time: 4:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class DcsDBMapper {

    RegistryClient client = new RegistryClient("http://localhost:8080/registry/rest/");

    /**
     * Converts SIP into smaller entities and makes POST calls through REST client to insert into Registry
     * @param sip
     * @throws IOException
     * @throws ClassNotFoundException
     */

    public void mapfromSip(ResearchObject sip) throws IOException, ClassNotFoundException {

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
            collection.setState((State) client.getEntity("state:1", State.class.getName()));

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
                    property.setValuestr(((SeadDeliverableUnit)du).getAbstrct().substring(0,end));
                    property.setEntity(collection);
                    collection.addProperty(property);
                }
            }


            List<Property> properties = new ArrayList<Property>();
            for(DcsMetadata metadata:du.getMetadata()){
                XStream xStream = new XStream(new DomDriver());
                xStream.alias("map",java.util.Map.class);
                Map<String,String> map = (Map<String, String>) xStream.fromXML(metadata.getMetadata());
                Iterator iterator = map.entrySet().iterator();

                while(iterator.hasNext()){
                    Map.Entry<String, String> pair = (Map.Entry<String, String>) iterator.next();
                    String[] arr = pair.getKey().split("/");
                    String element = arr[arr.length-1];
                    metadataType = client.getMetadataByType(element);
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

            client.postCollection(collection);


            Map<String,File>  fileMap = new HashMap<String, File>();
            java.util.Collection<DcsFile> files =  sip.getFiles();
            for(DcsFile dcsFile:files){
                File file = new File();
                file.setId(dcsFile.getId());
                file.setEntityName(dcsFile.getName());
                file.setEntityCreatedTime(new Date());
                file.setEntityLastUpdatedTime(new Date());
                file.setVersionNum("1");
                file.setSizeBytes(dcsFile.getSizeBytes());
                file.setIsObsolete(0);
                file.setFileName(dcsFile.getName());


                properties = new ArrayList<Property>();
                for(DcsMetadata metadata:dcsFile.getMetadata()){
                    XStream xStream = new XStream(new DomDriver());
                    xStream.alias("map",java.util.Map.class);
                    Map<String,String> map = (Map<String, String>) xStream.fromXML(metadata.getMetadata());
                    Iterator iterator = map.entrySet().iterator();

                    while(iterator.hasNext()){
                        Map.Entry<String, String> pair = (Map.Entry<String, String>) iterator.next();
                        String[] arr = pair.getKey().split("/");
                        String element = arr[arr.length-1];
                        metadataType = client.getMetadataByType(element);
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
                    file.addProperty(property1);

                fileMap.put(file.getId(),file);
                client.postFile(file);
            }


            java.util.Collection<DcsManifestation> manifestations =  sip.getManifestations();
            List<AggregationWrapper> aggregationWrappers = new ArrayList<AggregationWrapper>();
            for(DcsManifestation manifestation:manifestations){

                //loop here
                for(DcsManifestationFile file:manifestation.getManifestationFiles()){
                    AggregationWrapper wrapper = new AggregationWrapper();
                    wrapper.setParent(collection);
                    wrapper.setChild(fileMap.get(file.getRef().getRef()));
                    aggregationWrappers.add(wrapper);
                }
                client.postAggregation(aggregationWrappers, collection.getId());
            }
            System.out.print("done");
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

    private Map<String, List<Aggregation>> groupAggregations(List<Aggregation> aggregations){
        Map<String, List<Aggregation>> aggregationMap = new HashMap<String, List<Aggregation>>();
        for(Aggregation aggregation: aggregations){
            List<Aggregation> children;
            String parentId = aggregation.getId().getParent().getId();
            if(aggregationMap.containsKey(parentId))
                children = aggregationMap.get(parentId);
            else
                children = new ArrayList<Aggregation>();
            children.add(aggregation);
            aggregationMap.put(parentId, children);
        }
        return aggregationMap;

    }

    public SeadFile getFile(File file){

        SeadFile seadFile = new SeadFile();
        seadFile.setId(file.getId());
        seadFile.setName(file.getFileName());

        for(Property property:file.getProperties()){
            if(property.getMetadata().getMetadataElement().equals("size"))
                seadFile.setSizeBytes(Long.valueOf(property.getValuestr()));
            else {
                XStream xStream = new XStream(new DomDriver());
                xStream.alias("map",java.util.Map.class);
                Map<String,String> map = new HashMap<String, String>();
                String key = property.getMetadata().getMetadataSchema()+property.getMetadata().getMetadataElement();
                map.put(key,property.getValuestr());
                DcsMetadata metadata = new DcsMetadata();
                metadata.setSchemaUri(key);
                metadata.setMetadata(xStream.toXML(map));
                seadFile.addMetadata(metadata);
            }

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

        for(Property property:collection.getProperties()){
            if(property.getMetadata().getMetadataElement().equals(DcsDBField.CoreMetadataField.ABSTRACT.dbPropertyName()))
                du.setAbstrct(property.getValuestr());
            else{
                XStream xStream = new XStream(new DomDriver());
                xStream.alias("map",java.util.Map.class);
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
