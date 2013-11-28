package org.dataconservancy.ui.dao;

import org.dataconservancy.storage.dropbox.model.DropboxToken;

public interface DropboxDAO {
    boolean insertToken(DropboxToken token, String userId);
    
    DropboxToken getToken(String userId);
}
