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

import java.util.ArrayList;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.StatementHandlerException;

class FDORelsStatementHandler
        implements StatementHandler {

    private ArrayList<FDOTriple> statementList = new ArrayList<FDOTriple>();

    public void handleStatement(Resource subject, URI predicate, Value object)
            throws StatementHandlerException {

        FDOTriple triple = new FDOTriple();
        triple.setRdfSubject(subject);
        triple.setRdfPredicate(predicate);
        triple.setRdfObject(object);
        statementList.add(triple);

        //System.out.println("Subject: " + subject.toString());
        //System.out.println("Predicate: " + predicate.toString());
        //System.out.println("Object: " + object.toString());

    }

    public ArrayList<FDOTriple> getStatementList() {
        return statementList;
    }

}
