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

import junit.framework.Assert;
import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.model.Activity;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.ProjectService;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: HanhVu
 * Date: 2/13/12
 * Time: 3:02 PM
 * To change this template use File | Settings | File Templates.
 */
@DirtiesDatabase
@DirtiesContext
public class ProjectActivityActionBeanTest extends BaseActionBeanTest {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;

    @Autowired
    private RelationshipService relService;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private JdbcTemplate template;

    private MockHttpSession adminSession;

    @Autowired
    @Qualifier("userService")
    private UserService userService;

    @Before
    public void setUpStuff() throws Exception {
        setUpMockHttpSessions();
    }

    /**
     * Initialize the mock http session with authenticated user credentials.  Tests that re-use this mock session
     * will be already logged in.
     * @throws Exception
     */
    public void setUpMockHttpSessions() throws Exception {
        //mock a session for registered approved user
        adminSession = new MockHttpSession(servletCtx);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", adminSession);
        rt.setParameter("j_username", admin.getEmailAddress());
        rt.setParameter("j_password", admin.getPassword());
        rt.execute();
        SecurityContext ctx = (SecurityContext) adminSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        Assert.assertNotNull("Spring Security Context was null!", ctx);
        Assert.assertEquals(admin.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());

    }

    /**
     * Test that an admin user can look at the projectOne's activities log
     * @throws Exception
     */
    @Test
    public void testViewProjectActivitiesLog() throws Exception{
        assertTrue(true);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActivityActionBean.class, adminSession);
        rt.setParameter("selectedProjectId", projectOne.getId());
        rt.execute("render");

        Assert.assertEquals(200, rt.getResponse().getStatus());
        Assert.assertEquals("/pages/project_activities_log.jsp", rt.getDestination());

        List<Activity> activities = rt.getActionBean(ProjectActivityActionBean.class).getActivities();
        //check the number of activities returned for this projectOne.
        //Expect 4  (2 for 2 collection creation, 2 for two data item deposit)
        Assert.assertEquals(4, rt.getActionBean(ProjectActivityActionBean.class).getTotalActivitiesListSize());

        int collectionDepositCount = 0;
        int dataDepositCount = 0;
        for(Activity activity : activities){
            if(activity.getType().equals(Activity.Type.COLLECTION_DEPOSIT)) collectionDepositCount++;
            else if (activity.getType().equals(Activity.Type.DATASET_DEPOSIT)) dataDepositCount++;
        }

        assertEquals(2, collectionDepositCount);
        assertEquals(2, dataDepositCount);
    }

    /**
     * Test that when a projectOne has no activity, attempt to load the projectOne's activity log would result
     * in a successful page load with no activity on it.
     * @throws Exception
     */
    @Test
    public void testViewEmptyProjectActivities() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActivityActionBean.class, adminSession);
        rt.setParameter("selectedProjectId", projectTwo.getId());
        rt.execute("render");

        Assert.assertEquals(200, rt.getResponse().getStatus());
        Assert.assertEquals("/pages/project_activities_log.jsp", rt.getDestination());

        List<Activity> activities = rt.getActionBean(ProjectActivityActionBean.class).getActivities();
        //check the number of activities returned for this projectOne.
        //Expect 3  (2 for 2 collection creation, 1 for one data item deposit)
        Assert.assertEquals(0, rt.getActionBean(ProjectActivityActionBean.class).getTotalActivitiesListSize());
    }

    /**
     * Test that when a regular user (no admin right) attempt to load the projectOne's activities log page
     * an error page occurs with error code 401 for bad request.
     * @throws Exception
     */
    @Test
    public void testUnauthorizedUserAccessAttempt() throws Exception{

        MockHttpSession userSession = new MockHttpSession(servletCtx);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", userSession);
        rt.setParameter("j_username", user.getEmailAddress());
        rt.setParameter("j_password", user.getPassword());
        rt.execute();

        rt = new MockRoundtrip(servletCtx, ProjectActivityActionBean.class, userSession);
        rt.setParameter("selectedProjectId", projectOne.getId());
        rt.execute("render");
         Assert.assertEquals(401, rt.getResponse().getStatus());
    }


}
