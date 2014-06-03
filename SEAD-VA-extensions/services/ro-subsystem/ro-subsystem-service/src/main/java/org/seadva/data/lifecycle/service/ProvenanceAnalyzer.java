/*
 * Copyright 2014 The Trustees of Indiana University
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

package org.seadva.data.lifecycle.service;

import org.dspace.foresite.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Retreives provenance metadata from RO metadata
 */
public class ProvenanceAnalyzer {

    private static Predicate prov_revision = null;
    private static Predicate prov_derivation = null;
    public static String revision = "http://www.w3.org/ns/prov#wasRevisionOf";
    public static String derivation = "http://www.w3.org/ns/prov#wasDerivedFrom";

    public Map<String, List<String>> retrieveProv(ResourceMap resourceMap) throws OREException, URISyntaxException {
        prov_revision = new Predicate();
        prov_revision.setNamespace("http://www.w3.org/ns/prov#");
        prov_revision.setPrefix("http://www.w3.org/ns/prov#");
        prov_revision.setName("wasRevisionOf");
        prov_revision.setURI(new URI(revision));

        prov_derivation = new Predicate();
        prov_derivation.setNamespace("http://www.w3.org/ns/prov#");
        prov_derivation.setPrefix("http://www.w3.org/ns/prov#");
        prov_derivation.setName("wasRevisionOf");
        prov_derivation.setURI(new URI(derivation));

        Map<String, List<String>> metadataMap = new HashMap<String, List<String>>();
        TripleSelector metadataSelector = new TripleSelector();
        URI aggregationUri = resourceMap
                .getAggregation()
                .getURI();
        metadataSelector.setSubjectURI(aggregationUri);
        metadataSelector.setPredicate(prov_revision);
        List<Triple> metadataTriples = resourceMap.listAllTriples(metadataSelector);
        for(Triple metadataTriple: metadataTriples){

            Predicate predicate = metadataTriple.getPredicate();
            String  metadataUri = predicate.getURI().toString();
            if(!metadataUri.equalsIgnoreCase("http://www.w3.org/ns/prov#wasRevisionOf"))
                continue;
            List<String> entities = new ArrayList<String>();
            if(metadataMap.containsKey(metadataUri))
                entities = metadataMap.get(metadataUri);

            entities.add(metadataTriple.getObjectLiteral());
            metadataMap.put(metadataUri,entities);
        }

        metadataSelector.setPredicate(prov_derivation);
        metadataTriples = resourceMap.listAllTriples(metadataSelector);
        for(Triple metadataTriple: metadataTriples){

            Predicate predicate = metadataTriple.getPredicate();
            String  metadataUri = predicate.getURI().toString();
            if(!metadataUri.equalsIgnoreCase("http://www.w3.org/ns/prov#wasDerivedFrom"))
                continue;
            List<String> entities = new ArrayList<String>();
            if(metadataMap.containsKey(metadataUri))
                entities = metadataMap.get(metadataUri);

            entities.add(metadataTriple.getObjectLiteral());
            metadataMap.put(metadataUri,entities);
        }
        return metadataMap;
    }
}
