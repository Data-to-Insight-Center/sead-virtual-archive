package org.dataconservancy.ui.services;

import java.util.List;

import org.dataconservancy.storage.dropbox.DropboxAccessor;
import org.dataconservancy.storage.dropbox.model.DropboxDelta;
import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.dataconservancy.storage.dropbox.model.DropboxToken;
import org.dataconservancy.ui.dao.DropboxDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DropboxServiceImpl implements DropboxService {
    
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private DropboxDAO dropboxDao;
    private DropboxAccessor dropboxAccessor;
    
    public DropboxServiceImpl(DropboxDAO dropboxDao, DropboxAccessor dropboxAccessor) {
        if (dropboxDao == null || dropboxAccessor == null) {
            throw new IllegalArgumentException("dropboxDao or dropboxAccessor must not be null.");
        }
        this.dropboxDao = dropboxDao;
        this.dropboxAccessor = dropboxAccessor;
    }

    @Override
    public boolean insertToken(DropboxToken token, String userId) {
        if (dropboxDao.insertToken(token, userId)) {
            return true;
        }
        else {
            LOG.error("Failed to insert the appKey/appSecret into the database.");
            return false;
        }
    }
    
    @Override
    public DropboxToken getToken(String userId) {
        return dropboxDao.getToken(userId);
    }
    
    @Override
    public DropboxAccessor getDropboxInstance() {
        return dropboxAccessor;
    }
    
    @Override
    public boolean testDropboxLink(String userId) {
        if (dropboxAccessor.testDropboxLink(getToken(userId))) {
            return true;
        }
        else {
            LOG.error("Failed to test the linkage to Dropbox.");
            return false;
        }
    }
    
    /**
     * Polls dropbox and return a DropboxDelta to the BizService.
     */
    @Override
    public DropboxDelta pollDropbox(String userId, List<DropboxModel> dropboxModels) {
        return null;
    }

}
