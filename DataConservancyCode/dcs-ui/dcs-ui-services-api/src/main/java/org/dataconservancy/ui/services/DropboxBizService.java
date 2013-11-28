package org.dataconservancy.ui.services;

import java.util.List;

/**
 */
public interface DropboxBizService {

    /**
     * Place holder method
     * Given a user ID, return a list of Dropbox Activities.
     * Return type has not been decided. some implementation details such as offset and paging has not been considered
     * @param userId
     */
    public void getDropboxActivitiesForUser(String userId);

    /**
     * Place holder method
     * Given a user ID an Dropbox Activity log the the activities. Java type fo the Dropbox activities has not been
     * considered
     * @param userId
     */
    public void registerDropboxActivitiesForUser(String userId);

    /**
     * return id of the transaction?
     * @param userId
     */
    public List<String> pollDropbox(String userId);



}
