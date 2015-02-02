package org.seadva.registry.mapper;

import org.dspace.foresite.*;
import org.dspace.foresite.jena.TripleJena;
import org.seadva.registry.client.RegistryClient;
import org.seadva.registry.database.model.obj.vaRegistry.*;
import org.seadva.registry.database.model.obj.vaRegistry.Collection;
import org.seadva.registry.mapper.util.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Map Registry content to ORE
 */
public class OreDBMapper {

    RegistryClient client;

    private static Predicate DC_TERMS_IDENTIFIER = null;
    private static Predicate DC_TERMS_SOURCE = null;
    private static Predicate METS_LOCATION = null;
    private static Predicate REPLICA_LOCATION = null;
    private static Predicate DC_TERMS_TITLE = null;
        private static Predicate DC_TERMS_FORMAT = null;
    private static Predicate DC_TERMS_ABSTRACT = null;
    private static Predicate DC_REFERENCES = null;
    private static Predicate DC_TERMS_RIGHTS = null;
    private static Predicate DC_TERMS_CONTRIBUTOR = null;

    private static Predicate CITO_IS_DOCUMENTED_BY = null;
    private static Predicate DC_TERMS_TYPE = null;

    private static Predicate CITO_DOCUMENTS = null;

    public OreDBMapper(String registryUrl) throws URISyntaxException {
        client =  new RegistryClient(registryUrl);

        DC_TERMS_IDENTIFIER = new Predicate();
        DC_TERMS_IDENTIFIER.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_IDENTIFIER.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_IDENTIFIER.setName("identifier");
        DC_TERMS_IDENTIFIER.setURI(new URI(Constants.identifierTerm));

        DC_TERMS_TITLE = new Predicate();
        DC_TERMS_TITLE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_TITLE.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_TITLE.setName("title");
        DC_TERMS_TITLE.setURI(new URI(Constants.titleTerm));


        DC_TERMS_FORMAT = new Predicate();
        DC_TERMS_FORMAT.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_FORMAT.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_FORMAT.setName("format");
        DC_TERMS_FORMAT.setURI(new URI(Constants.formatTerm));

        DC_TERMS_ABSTRACT = new Predicate();
        DC_TERMS_ABSTRACT.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_ABSTRACT.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_ABSTRACT.setName("abstract");
        DC_TERMS_ABSTRACT.setURI(new URI(DC_TERMS_ABSTRACT.getNamespace()
                + DC_TERMS_ABSTRACT.getName()));

        DC_TERMS_SOURCE = new Predicate();
        DC_TERMS_SOURCE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_SOURCE.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_SOURCE.setName("source");
        DC_TERMS_SOURCE.setURI(new URI(Constants.sourceTerm));

        DC_TERMS_CONTRIBUTOR = new Predicate();
        DC_TERMS_CONTRIBUTOR.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_CONTRIBUTOR.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_CONTRIBUTOR.setName("contributor");
        DC_TERMS_CONTRIBUTOR.setURI(new URI(Constants.contributor));

        METS_LOCATION = new Predicate();
        METS_LOCATION.setNamespace("http://www.loc.gov/METS");
        METS_LOCATION.setPrefix("http://www.loc.gov/METS");
        METS_LOCATION.setName("FLocat");
        METS_LOCATION.setURI(new URI("http://www.loc.gov/METS/FLocat"));

        REPLICA_LOCATION = new Predicate();
        REPLICA_LOCATION.setNamespace("http://seadva.org/terms/");
        REPLICA_LOCATION.setPrefix("http://seadva.org/terms/");
        REPLICA_LOCATION.setName("replica");
        REPLICA_LOCATION.setURI(new URI("http://seadva.org/terms/replica"));

        // create the CITO:isDocumentedBy predicate
        CITO_IS_DOCUMENTED_BY = new Predicate();
        CITO_IS_DOCUMENTED_BY.setNamespace("http://purl.org/spar/cito/");
        CITO_IS_DOCUMENTED_BY.setPrefix("cito");
        CITO_IS_DOCUMENTED_BY.setName("isDocumentedBy");
        CITO_IS_DOCUMENTED_BY.setURI(new URI(CITO_IS_DOCUMENTED_BY.getNamespace()
                + CITO_IS_DOCUMENTED_BY.getName()));

        DC_TERMS_TYPE = new Predicate();
        DC_TERMS_TYPE.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_TYPE.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_TYPE.setName("type");
        DC_TERMS_TYPE.setURI(new URI(DC_TERMS_TYPE.getNamespace()
                + DC_TERMS_TYPE.getName()));

        DC_TERMS_RIGHTS = new Predicate();
        DC_TERMS_RIGHTS.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_TERMS_RIGHTS.setPrefix(Vocab.dcterms_Agent.schema());
        DC_TERMS_RIGHTS.setName("rights");
        DC_TERMS_RIGHTS.setURI(new URI(DC_TERMS_RIGHTS.getNamespace()
                + DC_TERMS_RIGHTS.getName()));

        DC_REFERENCES = new Predicate();
        DC_REFERENCES.setNamespace(Vocab.dcterms_Agent.ns().toString());
        DC_REFERENCES.setPrefix(Vocab.dcterms_Agent.schema());
        DC_REFERENCES.setName("references");
        DC_REFERENCES.setURI(new URI(DC_REFERENCES.getNamespace()
                + DC_REFERENCES.getName()));

        // create the CITO:documents predicate
        CITO_DOCUMENTS = new Predicate();
        CITO_DOCUMENTS.setNamespace(CITO_IS_DOCUMENTED_BY.getNamespace());
        CITO_DOCUMENTS.setPrefix(CITO_IS_DOCUMENTED_BY.getPrefix());
        CITO_DOCUMENTS.setName("documents");
        CITO_DOCUMENTS.setURI(new URI(CITO_DOCUMENTS.getNamespace()
                + CITO_DOCUMENTS.getName()));
    }


    public void mapfromOre(ResourceMap resourceMap) throws IOException, ClassNotFoundException, OREException, OREParserException {

        BaseEntity resourceMapEntity = new BaseEntity();
        resourceMapEntity.setId(resourceMap.getURI().toString());
        resourceMapEntity.setEntityName(resourceMap.getURI().toString());
        resourceMapEntity.setEntityCreatedTime(new Date());
        resourceMapEntity.setEntityLastUpdatedTime(new Date());

        client.postEntity(resourceMapEntity);


        TripleSelector titleSelector = new TripleSelector();
        titleSelector.setSubjectURI(resourceMap.getAggregation().getURI());
        titleSelector.setPredicate(DC_TERMS_TITLE);
        List<Triple> titleTriples = resourceMap.getAggregation().listAllTriples(titleSelector);

        Collection collection = new Collection();
        if(titleTriples.size()>0){
            collection.setName(titleTriples.get(0).getObjectLiteral());
            collection.setEntityName(titleTriples.get(0).getObjectLiteral());
        }

        collection.setId(resourceMap.getAggregation().getURI().toString());

        collection.setVersionNum("1");
        collection.setIsObsolete(0);

        collection.setEntityCreatedTime(new Date());
        collection.setEntityLastUpdatedTime(new Date());


        TripleSelector typeSelector = new TripleSelector();
        typeSelector.setSubjectURI(resourceMap.getAggregation().getURI());
        typeSelector.setPredicate(DC_TERMS_TYPE);
        List<Triple> typeTriples = resourceMap.getAggregation().listAllTriples(typeSelector);

        if(typeTriples.size()>0)
            collection.setState(client.getStateByName(typeTriples.get(0).getObjectLiteral()));


        TripleSelector locSelector = new TripleSelector();
        locSelector.setSubjectURI(resourceMap.getAggregation().getURI());
        locSelector.setPredicate(METS_LOCATION);
        List<Triple> locTriples = resourceMap.getAggregation().listAllTriples(locSelector);


        if(locTriples.size()>0){
            for(Triple locTriple:locTriples){
                String[] locArr = locTriple.getObjectLiteral().split(";");
                DataLocation dataLocation = new DataLocation();
                DataLocationPK dataLocationPK = new DataLocationPK();
                Repository repository = client.getRepositoryByName(locArr[0]);
                dataLocationPK.setLocationType(repository);
                dataLocation.setId(dataLocationPK);
                dataLocation.setIsMasterCopy(1);
                dataLocation.setLocationValue(locArr[2]);
                collection.addDataLocation(dataLocation);
            }
        }

        TripleSelector replicaLocSelector = new TripleSelector();
        replicaLocSelector.setSubjectURI(resourceMap.getAggregation().getURI());
        replicaLocSelector.setPredicate(REPLICA_LOCATION);
        List<Triple> replicaTriples = resourceMap.getAggregation().listAllTriples(replicaLocSelector);


        for (Triple replicaTriple: replicaTriples){
            if(replicaTriple.getObjectLiteral().contains(";")) {
                String[] locArr = replicaTriple.getObjectLiteral().split(";");
                DataLocation dataLocation = new DataLocation();
                DataLocationPK dataLocationPK = new DataLocationPK();
                Repository repository = client.getRepositoryByName(locArr[0]);
                dataLocationPK.setLocationType(repository);
                dataLocation.setId(dataLocationPK);
                dataLocation.setIsMasterCopy(0);
                dataLocation.setLocationValue(locArr[2]);
                collection.addDataLocation(dataLocation);
            }
        }

        //Insert properties
        Property property;
        MetadataType metadataType;

        List<Property> properties = new ArrayList<Property>();
        TripleSelector metadataSelector = new TripleSelector();
        metadataSelector.setSubjectURI(resourceMap.
                getAggregation().
                getURI());
        List<Triple> metadataTriples = resourceMap.
                getAggregation().
                listTriples();

        for(Triple metadataTriple: metadataTriples){
            if(metadataTriple.getPredicate()==null)
                continue;


            Predicate predicate = metadataTriple.getPredicate();

            //skip title
            if(predicate.getURI().toString().endsWith(DC_TERMS_TITLE.getName()))
                continue;

            String predicateUri = predicate.getURI().toString();
            metadataType = client.getMetadataByType(URLEncoder.encode(predicateUri)); //eventually Map ORE element to DB element if they are going to be different
            if(metadataType.getId()==null)
                continue;
            if(metadataType!=null&&metadataTriple.isLiteral()){
                property = new Property();
                property.setMetadata(metadataType);
                property.setValuestr(metadataTriple.getObjectLiteral());
                property.setEntity(collection);
                properties.add(property);
            }

        }
        Set<Property> propertiesSet = new HashSet<Property>();

        for(Property property1:properties)
                propertiesSet.add(property1);

        collection.setProperties(propertiesSet);
        client.postCollection(collection);

        Relation relation = new Relation();
        RelationPK relationPK = new RelationPK();
        relationPK.setCause(resourceMapEntity);
        relationPK.setEffect((BaseEntity)collection);
        relationPK.setRelationType(client.getRelationByType("describes"));
        relation.setId(relationPK);
        List<Relation> relations = new ArrayList<Relation>();
        relations.add(relation);
        client.postRelation(relations);

        List<AggregatedResource> aggregatedResources= resourceMap.getAggregation().getAggregatedResources();

        for(AggregatedResource aggregatedResource: aggregatedResources){

            List<URI> types = aggregatedResource.getTypes();
            boolean childIsCollection = false;
            for(URI type:types){
                if(type.toString().contains("Aggregation")){
                    locSelector = new TripleSelector();
                    locSelector.setSubjectURI(aggregatedResource.getURI());
                    locSelector.setPredicate(METS_LOCATION);
                    locTriples = resourceMap.getAggregation().listAllTriples(locSelector);

                    if(locTriples.size()>0){
                        for(Triple locTriple:locTriples){
                            String oreFilePath = locTriple.getObjectLiteral();
                            OREParser parser = OREParserFactory.getInstance("RDF/XML");
                            ResourceMap rem = parser.parse(new FileInputStream(oreFilePath));
                            mapfromOre(rem);
                            break;
                        }
                    }
                    childIsCollection = true;
                    break;
                }
            }

            if(childIsCollection)
                continue;

            titleSelector = new TripleSelector();
            titleSelector.setSubjectURI(aggregatedResource.getURI());
            titleSelector.setPredicate(DC_TERMS_TITLE);
            titleTriples = resourceMap.listAllTriples(titleSelector);

            File file = new File();
            if(titleTriples.size()>0){
                file.setFileName(titleTriples.get(0).getObjectLiteral());
                file.setEntityName(titleTriples.get(0).getObjectLiteral());
            }

            file.setId(aggregatedResource.getURI().toString());

            file.setVersionNum("1");
            file.setIsObsolete(0);

            file.setEntityCreatedTime(new Date());
            file.setEntityLastUpdatedTime(new Date());

            //Insert properties

          //  List<Property>
                    properties = new ArrayList<Property>();
         //   TripleSelector
                    metadataSelector = new TripleSelector();
            metadataSelector.setSubjectURI(aggregatedResource.getURI());
          //  List<Triple>
                    metadataTriples = resourceMap.listAllTriples(metadataSelector);
            for(Triple metadataTriple: metadataTriples){
                if(!metadataTriple.isLiteral())
                    continue;
                Predicate predicate = metadataTriple.getPredicate();
                String predicateUri = predicate.getURI().toString();
                 String splitChar = "/";
                if(predicateUri.contains("#"))
                    splitChar = "#";
                String[] metadataUri = predicateUri.split(splitChar);
                String metadataElement = metadataUri[metadataUri.length-1];
                metadataType = client.getMetadataByType(metadataElement); //eventually Map ORE element to DB element if they are going to be different
                if(metadataType.getId()==null)
                    continue;
                if(metadataType!=null){
                    property = new Property();
                    property.setMetadata(metadataType);
                    property.setValuestr(metadataTriple.getObjectLiteral());
                    property.setEntity(collection);
                    properties.add(property);
                }
            }
            for(Property property1:properties)
                file.addProperty(property1);

            client.postFile(file);
        }

        //Insert Aggregtaions

        for(AggregatedResource aggregatedResource: aggregatedResources){
            List<AggregationWrapper> aggregationWrappers = new ArrayList<AggregationWrapper>();
            AggregationWrapper aggregationWrapper = new AggregationWrapper();
            aggregationWrapper.setParentType(Collection.class.getName());

            boolean childIsCollection = false;
            List<URI> types = aggregatedResource.getTypes();
            for(URI type:types){
                if(type.toString().contains("Aggregation")){
                    childIsCollection = true;
                    break;
                }
            }

            if(childIsCollection)
                aggregationWrapper.setChildType(Collection.class.getName());
            else
                aggregationWrapper.setChildType(File.class.getName());

            BaseEntity child = new BaseEntity();
            child.setId(aggregatedResource.getURI().toString());
            BaseEntity parent = new BaseEntity();
            aggregationWrapper.setChild(child);
            parent.setId(resourceMap.getAggregation().getURI().toString());
            aggregationWrapper.setParent(parent);
            aggregationWrappers.add(aggregationWrapper);
            client.postAggregation(aggregationWrappers, resourceMap.getAggregation().getURI().toString());
        }


    }

    Map<String, List<AggregationWrapper>> aggregationMap = new HashMap<String, List<AggregationWrapper>>();
    List<BaseEntity> baseEntities = new ArrayList<BaseEntity>();
    public ResourceMap toORE(String resourceMapId) throws URISyntaxException, OREException, IOException, ClassNotFoundException {
        String collectionId = resourceMapId;
        resourceMapId = collectionId+"_resourceMap";
        ResourceMap resourceMap = OREFactory.createResourceMap(new URI(resourceMapId));

      /*  List<Relation> relations = client.getRelation(resourceMapId);

        if(relations!=null)
            for(Relation relation: relations){
                if(relation.getId().getRelationType().getRelationElement().equalsIgnoreCase("describes")){
                    collectionId = relation.getId().getEffect().getId();
                    break;
                }
            }

        if(collectionId==null)
            return resourceMap;*/

        populateCollection(collectionId, Collection.class.getName());


        for(BaseEntity baseEntity: baseEntities){
            if(baseEntity instanceof Collection) {
                if(baseEntity.getId().equalsIgnoreCase(collectionId))
                    resourceMap.setAggregation(getAggregation((Collection)baseEntity));//Add aggregation metadata
                else{
                    resourceMap.getAggregation().addAggregatedResource(getAggregatedResource((Collection)baseEntity,
                            resourceMap.getAggregation()));//Add aggregated resource metadata
                }
            }
            else if(baseEntity instanceof File)
            {
                resourceMap.getAggregation().addAggregatedResource(getAggregatedResource((File)baseEntity));//Add aggregated resource metadata
            }
        }
        return resourceMap;
    }

    void populateCollection(String entityId, String type) throws IOException, ClassNotFoundException {

        baseEntities.add(client.getEntity(entityId, type));
        List<AggregationWrapper> aggregationWrappers = client.getAggregation(entityId);
        if(aggregationWrappers!=null)
            for(AggregationWrapper aggregationWrapper: aggregationWrappers){
//                populateCollection(aggregationWrapper.getChild().getId(), aggregationWrapper.getChildType());
                baseEntities.add(client.getEntity(aggregationWrapper.getChild().getId(), aggregationWrapper.getChildType()));
            }
        if(aggregationWrappers!=null&&aggregationWrappers.size()>0)
            aggregationMap.put(entityId, aggregationWrappers);
    }

    public org.dspace.foresite.Aggregation getAggregation(Collection collection) throws URISyntaxException, OREException {

        org.dspace.foresite.Aggregation aggregation = OREFactory.createAggregation(new URI(collection.getId()));

        Triple resourceMapId = new TripleJena();
        resourceMapId.initialise(aggregation);
        resourceMapId.relate(DC_TERMS_IDENTIFIER,
                collection.getId());

        aggregation.addTriple(resourceMapId);

        Triple title= new TripleJena();
        title.initialise(aggregation);
        title.relate(DC_TERMS_TITLE,
                collection.getEntityName());

        aggregation.addTriple(title);

        Triple stateType= new TripleJena();
        stateType.initialise(aggregation);
        stateType.relate(DC_TERMS_TYPE,
               collection.getState().getStateType());

        aggregation.addTriple(stateType);

        for(Property property:collection.getProperties()){
            Triple triple = new TripleJena();
            triple.initialise(aggregation);

            Predicate ORE_TERM_PREDICATE =  new Predicate();
            ORE_TERM_PREDICATE.setNamespace(property.getMetadata().getMetadataSchema());
            ORE_TERM_PREDICATE.setPrefix(property.getMetadata().getMetadataSchema());
            URI uri = new URI(property.getMetadata().getMetadataSchema()+property.getMetadata().getMetadataElement());
            ORE_TERM_PREDICATE.setName(uri.toString().substring(uri.toString().lastIndexOf("/")));
            ORE_TERM_PREDICATE.setURI(uri);
            triple.relate(ORE_TERM_PREDICATE, property.getValuestr());
            aggregation.addTriple(triple);
            }

        return aggregation;
    }

    public AggregatedResource getAggregatedResource(File file) throws URISyntaxException, OREException {

        AggregatedResource aggregatedResource = OREFactory.createAggregatedResource(new URI(file.getId()));

        Triple resourceMapId = new TripleJena();
        resourceMapId.initialise(aggregatedResource);
        resourceMapId.relate(DC_TERMS_IDENTIFIER,
                file.getId());

        aggregatedResource.addTriple(resourceMapId);

        for(Property property:file.getProperties()){
            Triple triple = new TripleJena();
            triple.initialise(aggregatedResource);

            Predicate ORE_TERM_PREDICATE =  new Predicate();
            ORE_TERM_PREDICATE.setNamespace(property.getMetadata().getMetadataSchema());
            ORE_TERM_PREDICATE.setPrefix(property.getMetadata().getMetadataSchema());
            URI uri = new URI(property.getMetadata().getMetadataSchema()+property.getMetadata().getMetadataElement());
            ORE_TERM_PREDICATE.setName(uri.toString().substring(uri.toString().lastIndexOf("/")));
            ORE_TERM_PREDICATE.setURI(uri);
            triple.relate(ORE_TERM_PREDICATE, property.getValuestr());
            aggregatedResource.addTriple(triple);
        }

        return aggregatedResource;
    }

    public AggregatedResource getAggregatedResource(Collection collection,
                                                    org.dspace.foresite.Aggregation agg) throws URISyntaxException, OREException {

        AggregatedResource aggregatedResource = agg.createAggregatedResource(new URI(collection.getId()));

        Triple resourceMapId = new TripleJena();
        resourceMapId.initialise(aggregatedResource);
        resourceMapId.relate(DC_TERMS_IDENTIFIER,
                collection.getId());

        aggregatedResource.addTriple(resourceMapId);
        aggregatedResource.addType(new URI("http://www.openarchives.org/ore/terms/Aggregation"));

        return aggregatedResource;
    }

}
