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
package org.dataconservancy.archive.impl.fcrepo.semantic;

import java.io.InputStream;

import java.util.ArrayList;

import org.openrdf.rio.Parser;
import org.openrdf.rio.rdfxml.RdfXmlParser;

public class FDORels {
    
    private FDORelsStatementHandler handler = new FDORelsStatementHandler();
    
    public String getDCSEntityType(InputStream inputStream) {
        
        Parser parser = new RdfXmlParser();
        parser.setStatementHandler(handler);
        //parser.setParseErrorListener(myParseErrorListener);
        parser.setVerifyData(true);
        parser.setStopAtFirstError(false);
        String entityType = null;

        // Parse the data from inputStream, resolving any
        // relative URIs against the Fedora model:
        try {
            
            parser.parse(inputStream, "info:fedora/fedora-system:def/model#");
            ArrayList<FDOTriple> statementList = handler.getStatementList();
            
            // This is a simple lexical check.  Its does not provide all the
            // functionality we want but it will work for the moment.
            for (FDOTriple triple : statementList) {

                if (triple.getRdfPredicate().toString().equals("info:fedora/fedora-system:def/model#hasModel")) {

                    // We only want to match the DCS entity types since other content models
                    // may be mixed in.
                    if (triple.getRdfObject().toString().equals("info:fedora/dcs:File")) {
                        entityType = "info:fedora/dcs:File";
                    }
                    else if (triple.getRdfObject().toString().equals("info:fedora/dcs:DeliverableUnit")) {
                        entityType = "info:fedora/dcs:DeliverableUnit";
                    }
                    else if (triple.getRdfObject().toString().equals("info:fedora/dcs:Manifestation")) {
                        entityType = "info:fedora/dcs:Manifestation";   
                    }
                    else if (triple.getRdfObject().toString().equals("info:fedora/dcs:Collection")) {
                        entityType = "info:fedora/dcs:Collection";   
                    }
                    
                }
            }
            
        }
        catch (Exception e) {
            
            // TODO Should be logged.
            //System.out.println("Did not parse.");
            return null;
            
        }
        
        return entityType;
    }
    
}
