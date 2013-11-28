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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.Activity;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link CollectionActivityService}.
 */
public class CollectionActivityServiceImpl implements CollectionActivityService {

    private ArchiveService archiveService;
    private UserService userService;
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /*This object is used to group together all deposits for a given depositor. 
     * It stores a map of all their daily deposits, the last deposit time for that date, and the count. 
     * 
     * This is used to compress all deposit activities for a depositor for a single day into one activity. 
     * 
     * Note: Deposits will be represented by the time of the latest deposit in the group.
     */
    class DataItemCompressionGroup {
        private HashMap<DateTime, DailyDataItemCount> dailyDeposits = new HashMap<DateTime, DailyDataItemCount>();
        
        DataItemCompressionGroup(DateTime depositDate) {
            DateTime depositDay = getDateTimeOrNull(depositDate, true);
            
            dailyDeposits.put(depositDay, new DailyDataItemCount(depositDate));
        }
        
        //Adds a new deposit to the depositors group
        public void addDeposit(DateTime depositDate) {
            DateTime depositDay = getDateTimeOrNull(depositDate, true);
            
            //If their is already a map entry update it, otherwise create a new one
            if(dailyDeposits.containsKey(depositDay)) {
                dailyDeposits.get(depositDay).updateDeposit(depositDate);
            } else {
                dailyDeposits.put(depositDay, new DailyDataItemCount(depositDate));
            }
            
        }
        
        //Returns a list of all the deposit dates for the group
        public Set<DateTime> getAllDepositDates() {
            return dailyDeposits.keySet();
        }
        
        //Get the number of deposits for the given day
        public int getDailyDepositCount(DateTime depositDate) {
            
            int count = 0;
            
            DateTime depositDay = getDateTimeOrNull(depositDate, true);
            if(dailyDeposits.containsKey(depositDay)) {
                count = dailyDeposits.get(depositDay).depositCount;
            } 
            
            return count;
        }
        
        //Returns the last deposit time for the given day
        public DateTime getLastDepositDate(DateTime depositDate) {
            DateTime lastDate = depositDate;
            
            DateTime depositDay = getDateTimeOrNull(depositDate, true);
            
            if(dailyDeposits.containsKey(depositDay)) {
                lastDate = dailyDeposits.get(depositDay).lastDepositDate;
            } 
            
            return lastDate;
        }
        
        private DateTime getDateTimeOrNull(DateTime depositDate, Boolean dayOnly) {
            if (depositDate == null) {
                return null;
            } else {
                if (dayOnly) {
                    return new DateTime(depositDate.getYear(), depositDate.getMonthOfYear(),
                        depositDate.getDayOfMonth(), 1, 0);
                } else {
                    return new DateTime(depositDate.getYear(), depositDate.getMonthOfYear(),
                            depositDate.getDayOfMonth(), depositDate.getHourOfDay(), 
                            depositDate.getMinuteOfHour(), depositDate.getSecondOfMinute());
                }
            }
        }
        
        /*
         * This object stores the last deposit date and the count of deposits for a given day. 
         *
         */
        private class DailyDataItemCount {
            private DateTime lastDepositDate;
            private int depositCount;
            
            DailyDataItemCount(DateTime depositDate) {
                //This new DateTime is done because the chronology changes when the date is parsed by joda data time in the mapper. The code will work find without but tests will fail.
                this.lastDepositDate = getDateTimeOrNull(depositDate, false);  
                depositCount = 1;
            }
            
            public void updateDeposit(DateTime depositDate) {
                depositCount++;
                //This new DateTime is done because the chronology changes when the date is parsed by joda data time in the mapper. The code will work find without but tests will fail.
                if(lastDepositDate == null || lastDepositDate.isBefore(getDateTimeOrNull(depositDate, false))) {
                    lastDepositDate = getDateTimeOrNull(depositDate, false);
                }
            }
        }
    }
    
    /**
     * constructs a new collection activity service implementation
     * @param archiveService Archive Service archiveService must not be null
     */
    public CollectionActivityServiceImpl(ArchiveService archiveService, UserService userService) {
        if (archiveService == null) {
            throw new IllegalArgumentException("archiveService must not be null.");
        }
        
        if (userService == null) {
        	throw new IllegalArgumentException("userService must not be null.");
        }
        
        this.archiveService = archiveService;
        this.userService = userService;
        
        log.trace("Instantiated {} with {}", this, archiveService);
        log.trace("Instantiated {} with {}", this, userService);
    }

    /**
     * returns a most recent to earliest sorted list of activities for a given collection. includes the activity for
     * the collection creation itself.
     * @param collection
     */
    public List<Activity> retrieveActivitiesForCollectionByDate(Collection collection){
      return sortActivityListByDate(retrieveActivitiesForCollection(collection));
    }

    /**
     * returns a list of activities for a given collection. includes the activity for
     * the collection creation itself.
     * @param collection
     * @return  activityList
     */
    public List<Activity> retrieveActivitiesForCollection(Collection collection){
        List<Activity> activityList = new ArrayList<Activity>();
        activityList.add(retrieveCreationActivityForCollection(collection));
        String collectionDepositId = archiveService.listDepositInfo(collection.getId(), ArchiveDepositInfo.Status.DEPOSITED).get(0).getDepositId();
        List<DataItem> dataSetList = retrieveDataSetsForCollection(collectionDepositId);
        activityList.addAll(compressDataSetDepositsByDay(dataSetList, collection));
        return activityList;
    }

    /**
     * A convenience method. might be used from an action bean to sort an aggregation of Activity lists by date
     * most recent is at index 0
     * @param activityList
     * @return activityList
     */
    public List<Activity> sortActivityListByDate(List<Activity> activityList){
            Collections.sort(activityList, new Comparator<Activity>() {
            public int compare(Activity o1, Activity o2) {
                if (o1.getDateTimeOfOccurrence() == null) {
                    return 1;
                }
                if (o2.getDateTimeOfOccurrence() == null) {
                    return -1;
                }
                return -1 * o1.getDateTimeOfOccurrence().compareTo(o2.getDateTimeOfOccurrence());
            }
        });
       return activityList;
    }

    /**
     * returns creation activity for a collection
     * @param collection
     * @return activity
     */
    private Activity retrieveCreationActivityForCollection(Collection collection){
        DateTime depositDate = collection.getDepositDate();
        
        Activity activity = new Activity();
        Person actor = userService.get(collection.getDepositorId());
        activity.setActor(actor);
        activity.setDateTimeOfOccurrence(new DateTime(depositDate.getYear(), depositDate.getMonthOfYear(),
                                                      depositDate.getDayOfMonth(), depositDate.getHourOfDay(), depositDate.getMinuteOfHour(), depositDate.getSecondOfMinute()));
        activity.setType(Activity.Type.COLLECTION_DEPOSIT);
        activity.setCount(1);

        return activity;
    }

    /**
     * retrieves all data sets for a collection from archive
     * @param collectionDepositId
     * @return dataSetList
     */
   private List<DataItem> retrieveDataSetsForCollection(String collectionDepositId){
        //get all the data set objects for the collection
        java.util.Collection<DataItem> dataSetSet = new HashSet<DataItem>();
        try {
            ArchiveSearchResult<DataItem> results = archiveService.retrieveDataSetsForCollection(collectionDepositId, -1, 0);
            dataSetSet = results.getResults();
        } catch (ArchiveServiceException e){
            log.error("Could not retrieve data sets for collection " + collectionDepositId + " from archive.", e);
        }

        //make Set into a List
        List<DataItem> dataSetList = new ArrayList<DataItem>();
        for (DataItem ds : dataSetSet) {
            if(ds!= null){
                dataSetList.add(ds);
            }
        }
        return dataSetList;
    }

    /**
     * aggregates data set deposit events into activities: one activity for each date-depositor pair
     * since this takes collection as a parameter, this translates into one activity for each date-depositor-collection
     * triplet
     * @param dataSetList
     * @param collection
     * @return  dataSetResults
     */
   private List<Activity> compressDataSetDepositsByDay(List<DataItem> dataSetList, Collection collection){
        List<Activity> dataSetResults = new ArrayList<Activity>();
        HashMap<Person, DataItemCompressionGroup> countStructure = new HashMap<Person, DataItemCompressionGroup>();
        for(DataItem ds : dataSetList){
            //Check to see if we have a group for this depositor if we do add the deposit otherwise create a new group.
        	Person depositor = userService.get(ds.getDepositorId());
            if (countStructure.containsKey(depositor)){
                countStructure.get(depositor).addDeposit(ds.getDepositDate());
            } else {
                countStructure.put(depositor, new DataItemCompressionGroup(ds.getDepositDate()));
            }            
        }
        
        //Loop through all the depositors we have deposits for 
        for(Person depositor : countStructure.keySet()){
            DataItemCompressionGroup depositorGroup = countStructure.get(depositor);
            Set<DateTime> depositDates = depositorGroup.getAllDepositDates();
            //Loop through all the deposit dates we have for the depositor and add activities for each one
            for(DateTime date : depositDates) {
                Activity activity = new Activity();
                activity.setActor(depositor);
                //We get the last deposit time for the deposit group
                activity.setDateTimeOfOccurrence(depositorGroup.getLastDepositDate(date));
                activity.setType(Activity.Type.DATASET_DEPOSIT);
                activity.setCount(depositorGroup.getDailyDepositCount(date));
                dataSetResults.add(activity);
            }
        }
        
       return  dataSetResults;
   }
}
