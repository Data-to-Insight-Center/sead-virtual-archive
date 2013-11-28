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
package org.dataconservancy.dcs.lineage.http.support;

import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcs.DcsEntity;

import javax.naming.OperationNotSupportedException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class MockDcsEntityLookupQueryService implements LookupQueryService<DcsEntity> {

    Set<String> rejects = new HashSet<String>();
    
    /**
     * Obtain the domain object by its identifier.
     *
     * @param id the identifier
     * @return the domain object, or null if it cannot be found
     * @throws QueryServiceException if an error occurs during search
     */
    @Override
    public DcsEntity lookup(String id) throws QueryServiceException {
        if (rejects.contains(id)) {
            throw new QueryServiceException("Rejected!");
        }
        DcsEntity dcsEntity = new DcsEntity();
        dcsEntity.setId(id);
        return dcsEntity;
    }

    /**
     * This is a small function that allows you to force the service to throw
     * a QueryServiceException on a specified id.
     *
     * @param id
     */
    public void addReject(String id) {
        rejects.add(id);
    }
    
    /**
     * Page through a list of objects matching a query. The number of matches
     * returned will only be less than the corresponding argument at the end of
     * the list. The params argument is used to pass implementation specific
     * parameters to the search service.
     *
     * @param query
     * @param offset
     *            offset into total matches
     * @param matches
     *            The number of matches to return. May be capped. Passing a
     *            number < 0 lets the implementation choose.
     * @param params
     *            name,value pairs to set search parameters
     * @return result of searching
     * @throws QueryServiceException
     *             on error searching
     */
    @Override
    public QueryResult<DcsEntity> query(String query, long offset, int matches,
                                String... params) throws QueryServiceException {
        throw new RuntimeException("This method is not implemented.");
    }

    /**
     * Stops the service and cleans up. This method must be called when the
     * service is done being used.
     */
    @Override
    public void shutdown() throws QueryServiceException {
    }

}
