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
package org.dataconservancy.ui.stripes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.dataconservancy.ui.stripes.DropboxActivityActionBean.ActivityInfo;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

public class DropboxActivityActionBeanTest extends BaseActionBeanTest {
    MockHttpSession adminSession;

    @Before
    public void setUpActionBean() throws Exception {
        adminSession = authenticateUser(admin);
    }

    /**
     * Verify 200 for administrative access to this page.
     * 
     * @throws Exception
     */
    @Test
    public void testStatusCodeAdmin() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx,
                DropboxActivityActionBean.class, adminSession);
        rt.execute();

        assertEquals(200, rt.getResponse().getStatus());
    }

    /**
     * Verify anonymous access disallowed.
     * 
     * @throws Exception
     */
    @Test
    public void testStatusCodeAnon() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx,
                DropboxActivityActionBean.class);
        rt.execute();

        assertEquals(302, rt.getResponse().getStatus());
    }

    /**
     * Verify a poll can be started which adds an activity and that activities
     * are sorted by decreasing date.
     * 
     * @throws Exception
     */
    @Test
    public void testPollDropbox() throws Exception {
        DropboxActivityActionBean bean;

        {
            MockRoundtrip rt = new MockRoundtrip(servletCtx,
                    DropboxActivityActionBean.class, adminSession);
            rt.execute("pollDropbox");

            assertEquals(200, rt.getResponse().getStatus());

            bean = rt.getActionBean(DropboxActivityActionBean.class);

            assertEquals(true, bean.getPollInProgress());
            assertEquals(0, bean.getPage());
            assertEquals(1, bean.getPageActivities().size());
        }

        {
            MockRoundtrip rt = new MockRoundtrip(servletCtx,
                    DropboxActivityActionBean.class, adminSession);
            rt.execute("pollDropbox");

            assertEquals(200, rt.getResponse().getStatus());

            bean = rt.getActionBean(DropboxActivityActionBean.class);

            assertEquals(true, bean.getPollInProgress());
            assertEquals(0, bean.getPage());
            assertEquals(2, bean.getPageActivities().size());
        }

        DateTime last = null;

        for (ActivityInfo info : bean.getPageActivities()) {
            if (last == null) {
                last = info.getDate();
            } else {
                assertTrue(last.compareTo(info.getDate()) > 0);
            }
        }
    }

    /**
     * Verify activities can be listed.
     * 
     * @throws Exception
     */
    @Test
    public void testActivitiesView() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx,
                DropboxActivityActionBean.class, adminSession);
        rt.execute();

        assertEquals(200, rt.getResponse().getStatus());

        DropboxActivityActionBean bean = rt
                .getActionBean(DropboxActivityActionBean.class);

        assertEquals(0, bean.getPage());
        assertEquals(false, bean.getPollInProgress());
        assertNotNull(bean.getPageActivities());

        // TODO In order to test paging, really need an actual service to setup
        // activities
    }
}
