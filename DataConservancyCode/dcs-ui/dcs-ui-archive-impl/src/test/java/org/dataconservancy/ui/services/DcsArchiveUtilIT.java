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
package org.dataconservancy.ui.services;

import org.dataconservancy.access.connector.DcsClientFault;
import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 */
@Ignore("TODO")
@DirtiesContext
public class DcsArchiveUtilIT {

    private static final String ENTITY_RESOURCE = "426029.xml";

    private String formerRef = "http://dms.jhu.edu/4";

    private DcsConnector connector;

    private DcsModelBuilder modelBuilder;

    private DcsArchiveUtil underTest;

    @Before
    public void setUp() throws InvalidXmlException, DcsClientFault {
        underTest = new DcsArchiveUtil(connector);
    }

    @Test
    public void testGetExistingEntity() throws Exception {
        Dcp dcp = modelBuilder.buildSip(this.getClass().getResourceAsStream(ENTITY_RESOURCE));
        connector.depositSIP(dcp);
    }

    @Test
    public void testGetNonExistantEntity() throws Exception {
        //To change body of created methods use File | Settings | File Templates.
    }
}
