/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.dataconservancy.ui.services;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.DataItemTransport;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@DirtiesDatabase(DirtiesDatabase.AFTER_EACH_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DataItemTransportServiceTest extends BaseUnitTest {

    @Autowired
    private DataItemTransportService dataItemTransportService;

    @Autowired
    private JdbcTemplate template;

    static final String DATA_ITEM_TABLE = "DATA_ITEM";

    static final String DATA_FILE_TABLE = "DATA_FILE";

    @Test(expected = ArchiveServiceException.class)
    public void testGetDataItemTransportWithBadDataItemId() throws ArchiveServiceException, BizPolicyException {
        String badDataItemId = "Bad ID";
        this.dataItemTransportService.retrieveDataItemTransport(badDataItemId);
    }

    @Test
    public void testGetDataItemTransportWithGoodDataItemId() throws ArchiveServiceException, BizPolicyException {
        DataItemTransport actualDataItemTransport = this.dataItemTransportService.retrieveDataItemTransport(dataItemOne.getId());
        assertEquals(dataItemOne.getId(), actualDataItemTransport.getDataItem().getId());
        assertEquals(dataItemOne.getName(), actualDataItemTransport.getDataItem().getName());
        assertEquals(dataItemOneDepositDate, actualDataItemTransport.getInitialDepositDate());
        assertEquals(ArchiveDepositInfo.Status.DEPOSITED, actualDataItemTransport.getDepositStatus());
    }
}
