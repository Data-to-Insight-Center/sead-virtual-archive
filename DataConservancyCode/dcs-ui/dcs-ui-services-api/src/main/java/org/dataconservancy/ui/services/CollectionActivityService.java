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


import org.dataconservancy.ui.model.Activity;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Project;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

/**
 *  This is an interface for reporting activities related to Collections. Initially we are reporting
 *  collection creation events and dataset deposit events. In order to economize and simplify the display, we aggregate
 *  similar dataset deposit events together - similar meaning that they are done by the same depositor to the same
 *  on the same day.
 */
public interface CollectionActivityService {


    /**
     * returns a list of {@code Activity}s related to the given {@code Collection}
     * sorted in reverse order by date
     * @param collection
     */
    public List<Activity> retrieveActivitiesForCollectionByDate(Collection collection);

    /**
     * returns a list of {@code Activity}s related to the given (@code Collection}
     * @param collection
     */
    public List<Activity> retrieveActivitiesForCollection(Collection collection);


    /**
     * returns a list of {@code Activitie}s sorted in reverse order, by date
     * @param activityList
     */
    public List<Activity> sortActivityListByDate(List<Activity> activityList);

}
