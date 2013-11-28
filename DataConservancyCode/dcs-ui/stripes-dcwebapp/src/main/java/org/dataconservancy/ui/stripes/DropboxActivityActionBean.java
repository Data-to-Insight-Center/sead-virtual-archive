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

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.joda.time.DateTime;

/**
 * Page through dropbox activities. Pages start at 0 and go to page count - 1.
 */
@UrlBinding("/dropbox/activity.action")
public class DropboxActivityActionBean extends BaseActionBean {
    private final static String ACTIVITY_VIEW_JSP_PATH = "/pages/dropbox_activity_view.jsp";
    private final static int ITEMS_PER_PAGE = 10;

    // TODO For the moment, mimic an activities service
    private final static List<ActivityInfo> activities_hack = new CopyOnWriteArrayList<ActivityInfo>();

    // Information needed by the JSP which is initialized by setup_render
    private int page;
    private int last_page;
    private String message;
    private List<ActivityInfo> page_activities;
    private boolean poll_in_progress;

    public DropboxActivityActionBean() {
        this.page = 0;
        this.last_page = 0;
        this.message = "";
        this.poll_in_progress = false;
    }

    private void setup_render(boolean polling) {
        List<ActivityInfo> activities = lookup_activities();

        poll_in_progress = polling; 

        if (poll_in_progress) {
            message = "Polling in progress.";
        }

        if (page < 0) {
            page = 0;
        }

        last_page = activities.size() / ITEMS_PER_PAGE;

        if ((activities.size() % ITEMS_PER_PAGE) == 0) {
            if (last_page > 0) {
                last_page--;
            }
        }

        if (page > last_page) {
            page = last_page;
        }

        int offset = page * ITEMS_PER_PAGE;
        int end = offset + ITEMS_PER_PAGE;

        if (end > activities.size()) {
            end = activities.size();
        }

        page_activities = activities.subList(offset, end);
    }

    @DefaultHandler
    public Resolution render() {
        setup_render(lookup_poll_in_progress());

        return new ForwardResolution(ACTIVITY_VIEW_JSP_PATH);
    }

    public Resolution pollDropbox() {
        boolean polling = lookup_poll_in_progress();
        
        if (!polling) {
            poll_dropbox();
            polling = true;
        }

        setup_render(polling);
        
        return new ForwardResolution(ACTIVITY_VIEW_JSP_PATH);
    }

    private void poll_dropbox() {
        Random r = new Random();
        ActivityStatusType[] types = ActivityStatusType.values();
        ActivityStatusType type = types[r.nextInt(types.length)];
        
        activities_hack.add(0, new ActivityInfo("In progress",
                getAuthenticatedUser().getEmailAddress(), new DateTime(),
                "This is a description", type));
    }

    /**
     * Lookup activities which the current user can see. Activities must be in order of decreasing date.
     * 
     * @return
     */
    private List<ActivityInfo> lookup_activities() {
        return activities_hack;
    }

    /**
     * Determine whether or not the user has a poll in progress. TODO Faked for
     * the moment.
     * 
     * @return status
     */
    private boolean lookup_poll_in_progress() {
        return false;
    }

    public List<ActivityInfo> getPageActivities() {
        return page_activities;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLastPage() {
        return last_page;
    }

    public String getMessage() {
        return message;
    }

    public boolean getPollInProgress() {
        return poll_in_progress;
    }

    /**
     * Type of information being given about activity.
     */
    public enum ActivityStatusType {
        SUCCESS, WARNING, ERROR, INFO;
    }
    
    /**
     * View model of an activity.
     */
    public static class ActivityInfo {
        private String status;
        private String user;
        private DateTime date;
        private String description;
        private ActivityStatusType type;

        public ActivityInfo(String status, String user, DateTime date,
                String description, ActivityStatusType type) {
            this.status = status;
            this.user = user;
            this.date = date;
            this.description = description;
            this.type = type;
        }

        public String getStatus() {
            return status;
        }

        public String getUser() {
            return user;
        }

        public DateTime getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }
        
        public ActivityStatusType getType() {
            return type;
        }
    }
}
