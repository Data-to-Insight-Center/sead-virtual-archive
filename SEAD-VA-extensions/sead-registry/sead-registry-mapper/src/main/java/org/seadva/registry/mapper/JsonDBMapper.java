package org.seadva.registry.mapper;

import com.google.gson.Gson;
import org.dspace.foresite.*;
import org.dspace.foresite.jena.TripleJena;
import org.json.JSONException;
import org.seadva.registry.client.RegistryClient;
import org.seadva.registry.database.model.obj.vaRegistry.*;
import org.seadva.registry.database.model.obj.vaRegistry.Collection;
import org.seadva.registry.mapper.util.Constants;
import org.seadva.registry.mapper.util.RO;
import org.seadva.registry.mapper.util.ROObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Map Registry content to JSON-LD
 */
public class JsonDBMapper {

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

    public JsonDBMapper(String registryUrl) throws URISyntaxException {
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

    public String toJSONLD(String collectionId)
            throws URISyntaxException, OREException, IOException, ClassNotFoundException, JSONException {

        List<AggregationWrapper> aggregationWrappers = client.getAggregation(collectionId);
        BaseEntity baseEntity;
        RO ro = new RO();
        if(aggregationWrappers!=null)
            for(AggregationWrapper aggregationWrapper: aggregationWrappers){
                baseEntity = client.getEntity(aggregationWrapper.getParent().getId(),
                        aggregationWrapper.getParentType());
                ro.setParent(baseEntity);
                baseEntity = client.getEntity(aggregationWrapper.getChild().getId(),
                        aggregationWrapper.getChildType());
                ro.appendChild(baseEntity);
            }

        return ro.toJSON();
    }
}
