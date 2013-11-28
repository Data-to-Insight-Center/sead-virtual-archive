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

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class FDOTriple {
    
    private Resource rdfSubject;
    private URI rdfPredicate;
    private Object rdfObject;
    
    public Resource getRdfSubject() {
        return rdfSubject;
    }
    
    public void setRdfSubject(Resource rdfSubject) {
        this.rdfSubject = rdfSubject;
    }
    
    public URI getRdfPredicate() {
        return rdfPredicate;
    }
    
    public void setRdfPredicate(URI rdfPredicate) {
        this.rdfPredicate = rdfPredicate;
    }
    
    public Object getRdfObject() {
        return rdfObject;
    }
    
    public void setRdfObject(Object rdfObject) {
        this.rdfObject = rdfObject;
    }
    
}
