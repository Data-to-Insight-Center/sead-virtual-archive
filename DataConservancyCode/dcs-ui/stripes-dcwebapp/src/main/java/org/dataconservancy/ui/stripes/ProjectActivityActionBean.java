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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.ViewProjectActivityException;
import org.dataconservancy.ui.model.Activity;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.services.CollectionActivityService;
import org.dataconservancy.ui.services.ProjectBizService;
import org.dataconservancy.ui.services.RelationshipService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_NOT_AUTHORIZED_TO_VIEW_ACTIVITY;

/**
 * {@link ProjectActivityActionBean} handles requests to view list of
 * {@link Activity} associated with a {@link Project}.
 */
@UrlBinding(value = "/project/projectactivitieslog.action")
public class ProjectActivityActionBean
        extends BaseActionBean {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String selectedProjectId;

    private ProjectBizService projectBizService;

    private CollectionActivityService collectionActivityService;

    private RelationshipService relationshipService;

    /**
     * The page for viewing a project's collections
     */
    static final String VIEW_PROJECT_ACTIVITIES_PATH =
            "/pages/project_activities_log.jsp";

    /**
     * The forward destination when viewing the user's project membership
     */
    static final String VIEW_PROJECT_PATH = "/pages/view_user_project.jsp";

    public ProjectActivityActionBean() {
        super();

        // Ensure desired properties are available.
        try {
            assert (messageKeys
                    .containsKey(MSG_KEY_NOT_AUTHORIZED_TO_VIEW_ACTIVITY));
        } catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of "
                    + MSG_KEY_NOT_AUTHORIZED_TO_VIEW_ACTIVITY + " is missing.");
        }

    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public int getTotalActivitiesListSize() {
        return this.activities.size();
    }

    private List<Activity> activities;

    public String getSelectedProjectId() {
        return selectedProjectId;
    }

    public void setSelectedProjectId(String selectedProjectId) {
        this.selectedProjectId = selectedProjectId;
    }

    public Project getProject() throws BizPolicyException {
        return projectBizService.getProject(getSelectedProjectId(),
                                            getAuthenticatedUser());
    }

    public Resolution viewUserProject() {
        return new ForwardResolution(VIEW_PROJECT_PATH);
    }

    @DefaultHandler
    public Resolution render() throws ViewProjectActivityException {
        Person currentUser = getAuthenticatedUser();

        try {
            Project currentProject =
                    projectBizService
                            .getProject(selectedProjectId, currentUser);

            if (currentProject == null) {
                ViewProjectActivityException e =
                        new ViewProjectActivityException("Project "
                                + selectedProjectId
                                + " "
                                + "is an invalid Project identifier, or the Project cannot be found.");
                e.setHttpStatusCode(400);
                e.setProjectId(selectedProjectId);
                e.setProjectName("");
                throw e;
            }

            buildActivitiesListForProject(currentProject);
        } catch (BizPolicyException e) {
            log.info(currentUser.getId()
                    + " is not authorized to get the activities log for project "
                    + selectedProjectId);
            final String msg =
                    messageKeys
                            .getProperty(MSG_KEY_NOT_AUTHORIZED_TO_VIEW_ACTIVITY);
            ViewProjectActivityException ve =
                    new ViewProjectActivityException(msg);
            ve.setHttpStatusCode(401);
            ve.setProjectId(selectedProjectId);
            throw ve;
        }

        return new ForwardResolution(VIEW_PROJECT_ACTIVITIES_PATH);
    }

    private List<Activity> mockActivitiesList() {
        List<Activity> activities = new ArrayList<Activity>();
        Activity temp = new Activity();
        temp.setActor(getAuthenticatedUser());
        temp.setDateTimeOfOccurrence(new DateTime());
        temp.setDescription("<a href=\"#\"> Collection 5</a> was created.");
        temp.setType(Activity.Type.COLLECTION_DEPOSIT);
        activities.add(temp);

        temp = new Activity();
        temp.setActor(getAuthenticatedUser());
        temp.setDateTimeOfOccurrence(new DateTime());
        temp.setDescription("<a href=\"#\"> Collection 4</a> was created.");
        temp.setType(Activity.Type.COLLECTION_DEPOSIT);
        activities.add(temp);

        temp = new Activity();
        temp.setActor(getAuthenticatedUser());
        temp.setDateTimeOfOccurrence(new DateTime());
        temp.setDescription("5 data items were deposited into <a href=\"#\"> Collection 3</a>.");
        temp.setType(Activity.Type.DATASET_DEPOSIT);
        activities.add(temp);

        temp = new Activity();
        temp.setActor(getAuthenticatedUser());
        temp.setDateTimeOfOccurrence(new DateTime());
        temp.setDescription("5 data items were deposited into <a href=\"#\"> Collection 2</a>.");
        temp.setType(Activity.Type.DATASET_DEPOSIT);
        activities.add(temp);

        temp = new Activity();
        temp.setActor(getAuthenticatedUser());
        temp.setDateTimeOfOccurrence(new DateTime());
        temp.setDescription("<a href=\"#\"> Collection 7</a> was created.");
        temp.setType(Activity.Type.COLLECTION_DEPOSIT);
        activities.add(temp);

        temp = new Activity();
        temp.setActor(getAuthenticatedUser());
        temp.setDateTimeOfOccurrence(new DateTime());
        temp.setDescription("8 data items were deposited into <a href=\"#\"> Collection 8</a>.");
        temp.setType(Activity.Type.DATASET_DEPOSIT);
        activities.add(temp);

        temp = new Activity();
        temp.setActor(getAuthenticatedUser());
        temp.setDateTimeOfOccurrence(new DateTime());
        temp.setDescription("<a href=\"#\"> Collection 9</a> was created.");
        temp.setType(Activity.Type.COLLECTION_DEPOSIT);
        activities.add(temp);

        temp = new Activity();
        temp.setActor(getAuthenticatedUser());
        temp.setDateTimeOfOccurrence(new DateTime());
        temp.setDescription("65 data items were deposited into <a href=\"#\"> Collection 66</a>.");
        temp.setType(Activity.Type.DATASET_DEPOSIT);
        activities.add(temp);

        temp = new Activity();
        temp.setActor(getAuthenticatedUser());
        temp.setDateTimeOfOccurrence(new DateTime());
        temp.setDescription("<a href=\"#\"> Collection 89</a> was created.");
        temp.setType(Activity.Type.COLLECTION_DEPOSIT);
        activities.add(temp);

        temp = new Activity();
        temp.setActor(getAuthenticatedUser());
        temp.setDateTimeOfOccurrence(new DateTime());
        temp.setDescription("<a href=\"#\"> Collection 56</a> was created.");
        temp.setType(Activity.Type.COLLECTION_DEPOSIT);
        activities.add(temp);

        temp = new Activity();
        temp.setActor(getAuthenticatedUser());
        temp.setDateTimeOfOccurrence(new DateTime());
        temp.setDescription("8 data items were deposited into <a href=\"#\"> Collection 8</a>.");
        temp.setType(Activity.Type.DATASET_DEPOSIT);
        activities.add(temp);

        temp = new Activity();
        temp.setActor(getAuthenticatedUser());
        temp.setDateTimeOfOccurrence(new DateTime());
        temp.setDescription("78 data items were deposited into <a href=\"#\"> Collection 5</a>.");
        temp.setType(Activity.Type.DATASET_DEPOSIT);
        activities.add(temp);

        return activities;
    }

    private void buildActivitiesListForProject(Project currentProject)
            throws RuntimeException {
        activities = retrieveActivitiesForProjectByDate(currentProject);
    }

    public List<Activity> retrieveActivitiesForProjectByDate(Project project) {
        List<Activity> activityList = new ArrayList<Activity>();
        List<DataItem> dataSetList = new ArrayList<DataItem>();
        Set<Collection> collections =
                relationshipService.getCollectionsForProject(project);
        for (Collection col : collections) {
            List<Activity> collectionActivityList =
                    collectionActivityService
                            .retrieveActivitiesForCollection(col);
            for (Activity act : collectionActivityList) {
                if ((null == act.getDescription())
                        || (0 == act.getDescription().length())) {
                    if (act.getType() == Activity.Type.COLLECTION_DEPOSIT) {
                        act.setDescription("Collection <b>" + col.getTitle()
                                + "</b> created");
                    } else if (act.getType() == Activity.Type.DATASET_DEPOSIT) {
                        if (act.getCount() == 1) {
                            act.setDescription("1 data item deposited to <b>"
                                    + col.getTitle() + "</b>");
                        } else if (act.getCount() > 1) {
                            act.setDescription(act.getCount().toString()
                                    + " data items deposited to <b>"
                                    + col.getTitle() + "</b>");
                        }
                    }
                }
            }
            activityList.addAll(collectionActivityList);
        }
        return collectionActivityService.sortActivityListByDate(activityList);
    }

    /**
     * Stripes-injected ProjectBizService
     * 
     * @param bizService
     */
    @SpringBean("projectBizService")
    public void injectProjectBizService(ProjectBizService bizService) {
        this.projectBizService = bizService;
    }

    /**
     * Stripes-injected projectService
     * 
     * @param collectionActivityService
     */
    @SpringBean("collectionActivityService")
    private void injectCollectionActivityService(CollectionActivityService collectionActivityService) {
        this.collectionActivityService = collectionActivityService;
    }

    /**
     * Stripes-injected relationshipService
     * 
     * @param relationshipService
     */
    @SpringBean("relationshipService")
    private void injectRelationshipService(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

}
