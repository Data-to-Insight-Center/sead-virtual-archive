package org.dataconservancy.ui.services;

import java.util.List;

import org.dataconservancy.storage.dropbox.DropboxAccessor;
import org.dataconservancy.storage.dropbox.model.DropboxDelta;
import org.dataconservancy.storage.dropbox.model.DropboxModel;
import org.dataconservancy.storage.dropbox.model.DropboxToken;

public interface DropboxService {
    
    DropboxAccessor getDropboxInstance();
    
    boolean insertToken(DropboxToken token, String userId);
    
    DropboxToken getToken(String userId);

    boolean testDropboxLink(String userId);
    
    DropboxDelta pollDropbox(String userId, List<DropboxModel> dropboxModels);

}
