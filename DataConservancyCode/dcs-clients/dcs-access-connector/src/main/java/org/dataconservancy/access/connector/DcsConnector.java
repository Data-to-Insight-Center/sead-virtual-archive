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
package org.dataconservancy.access.connector;

import java.io.InputStream;

import java.net.URL;

import java.util.Iterator;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;

/**
 * Data Conservancy connector interface.  The primary motivation for this interface is to abstract clients from writing
 * their own deserialization routines for the Data Conservancy package XML format, and to provide some abstraction for
 * executing search queries against the Data Conservancy.  Implementations are expected to deal with authentication,
 * search query formulation, search result pagination, and other mundane issues.  Implementations are expected to be
 * threadsafe, unless noted otherwise. 
 */
public interface DcsConnector {

    /**
     * Obtain the byte stream identified by <code>streamId</code>.  If <code>streamId</code> identifies a content
     * stream (i.e. it is the value of a <code>&lt;File&gt; src</code> attribute), then the byte stream is
     * returned untouched.  If <code>streamId</code> identifies a DCS entity such as a Collection, Deliverable
     * Unit, Event, or File, then the entity is serialized to the Data Conservancy Packaging XML, and the stream is
     * returned.
     *
     * @param streamId the identifier for the stream.
     * @return the stream, or <code>null</code> if no stream was found for that <code>streamId</code>
     */
    public InputStream getStream(String streamId) throws DcsConnectorFault;

    /**
     * Obtain the {@link DcsFile files} that are descendants (not just direct descendants) of the supplied DCS entity
     * identifier.  For example, if <code>entityId</code> specifies a Deliverable Unit, this method returns all Files
     * belonging to that DU.  If <code>entityId</code> specifies a Collection, this method returns all Files that are
     * members of the Collection.  If the entity identifier is <code>null</code>, retrieve all files.
     *
     * @param entityId the DCS entity identifier, may be <code>null</code>
     * @return an <code>Iterator</code> over the results, never <code>null</code>
     */
    public Iterator<DcsFile> getFiles(String entityId) throws DcsConnectorFault;

    /**
     * Expert: execute an arbitrary search query.  <em>Note:</em> the query format is not guaranteed to be stable over
     * time.
     *
     * @param query the search query
     * @return an <code>CountableIterator</code> over the results, never <code>null</code>
     */
    public CountableIterator<DcsEntity> search(String query) throws DcsConnectorFault;

    
    public CountableIterator<DcsEntity> search(String query, int maxResults, int offset) throws DcsConnectorFault;
    
    /**
     * Upload a file of the given length and return an identifier for the upload.
     * 
     * @param is the content to upload
     * @param length -1 if not known
     * @return identifier for uploaded file
     * @throws DcsClientFault
     */
    public String uploadFile(InputStream is, long length) throws DcsClientFault;
    
    /**
     * Deposit a SIP and return a ticket for the deposit status. A GET on the ticket
     *  will return an atom feed of the deposit status.
     * 
     * @param dcp
     * @return ticket for the deposit.
     * @throws DcsClientFault 
     */
    public URL depositSIP(Dcp dcp) throws DcsClientFault;
}
