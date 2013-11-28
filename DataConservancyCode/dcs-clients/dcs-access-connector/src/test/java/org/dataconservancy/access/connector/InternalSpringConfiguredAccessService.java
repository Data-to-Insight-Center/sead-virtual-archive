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

import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcp.Dcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 */
public class InternalSpringConfiguredAccessService implements QueryService<Dcp> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final QueryService<Dcp> delegate;

    public InternalSpringConfiguredAccessService() {
        final ApplicationContext appCtx = new ClassPathXmlApplicationContext("/applicationContext.xml");
        delegate = (QueryService<Dcp>) appCtx.getBean("dcsQueryService");
        if (delegate == null) {
            throw new IllegalArgumentException("Unable to obtain AccessService implementation from Spring.");
        }
    }

    @Override
    public QueryResult<Dcp> query(String query,
                                  long offset,
                                  int matches,
                                  String... params)
            throws QueryServiceException {
        return delegate.query(query, offset, matches, params);
    }

    @Override
    public void shutdown() throws QueryServiceException {
        delegate.shutdown();
        
    }
}
