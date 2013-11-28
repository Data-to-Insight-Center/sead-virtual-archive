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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.BizPolicyException.Type;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.dataconservancy.ui.util.ContentTypeUtil;
import org.dataconservancy.ui.util.UserVerificationUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link FileBizService}.
 */
public class FileBizServiceImpl implements FileBizService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private ArchiveService archiveService;
    private AuthorizationService authService;
    private RelationshipService relService;
    private UserService userService;
    
    @Override
    public DataFile getFile(String id, Person user) throws ArchiveServiceException, RelationshipConstraintException, BizPolicyException {

        if (log.isDebugEnabled()) {
            if (user != null) {
                log.debug("Retrieving file id {} for {} ({})",
                        new Object[]{id, user.getEmailAddress(), user.getId()});
            } else {
                log.debug("Retrieving file id {} for 'null'", id);
            }
        }

        DataFile file = null;

        DataItem ds = null;

        //Get the dataset that this file belongs to
        ds = relService.getDataSetForDataFileId(id);

        if (ds != null && ds.getId() != null && !ds.getId().isEmpty()) {
            //Get the dataset from the archive.
            List<ArchiveDepositInfo> infoList = archiveService.listDepositInfo(ds.getId(), Status.DEPOSITED);

            boolean found = false;

            //Loop through each set of info ...??
            Iterator<ArchiveDepositInfo> infoListIter = infoList.iterator();
            while (!found && infoListIter.hasNext()) {
                ArchiveDepositInfo info = infoListIter.next();
                ArchiveSearchResult<DataItem> result = archiveService.retrieveDataSet(info.getDepositId());

                Iterator<DataItem> dataSetIter = result.getResults().iterator();
                while (!found && dataSetIter.hasNext()) {
                    DataItem dataItem = dataSetIter.next();

                    for (DataFile dsFile : dataItem.getFiles()) {
                        if (dsFile.getId().equalsIgnoreCase(id)) {
                            file = dsFile;
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                log.debug("File {} belonging to DataItem {} could not be found in the archive.", id,
                        ds.getId());
            }
        } else if (ds == null) {
            log.debug("No DataItem exists for file {}: DataItem could not be looked up by file id in the" +
                    "relationship service.", id);
        } else if (ds.getId() == null||ds.getId().isEmpty()) {
            log.debug("No DataItem exists for file {}: DataItem identifier is null or empty: {}", id, ds);
        } else {
            log.debug("No DataItem exists for file {}", id);
        }

        //Verify that the user is in the userService (and not spoofed).
        user = UserVerificationUtil.VerifyUser(userService, user);

        if (file != null && !authService.canRetrieveDataFile(user, (DataFile) file)) {
            if (user == null) {
                throw new BizPolicyException("Please log in to retrieve the given file.",
                        BizPolicyException.Type.AUTHENTICATION_ERROR);
            } else {
                throw new BizPolicyException("This user is not authorized to retrieve the given file.",
                        Type.AUTHORIZATION_ERROR);
            }
        }

        return file;
    }
    @Override
    public String getMimeType(File file) throws IOException{
        ContentTypeUtil ctu = new ContentTypeUtil();
        String contentType = ctu.detectMimeType(file);
        return contentType;
    }

    public void setArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }
    
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authService = authorizationService;
    }
    
    public void setRelationshipService(RelationshipService relationshipService) {
        this.relService = relationshipService;
    }
    
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public DateTime getLastModifiedDate(String id) throws RelationshipConstraintException {
        DateTime lastModifiedDate = null;
        DataItem ds = relService.getDataSetForDataFileId(id);

        if (ds != null && ds.getId() != null && !ds.getId().isEmpty()) {
            List<ArchiveDepositInfo> depositInfo = archiveService.listDepositInfo(ds.getId(), Status.DEPOSITED);

            if (depositInfo != null && !depositInfo.isEmpty()) {
                lastModifiedDate = depositInfo.get(0).getDepositDateTime();
            }
        }

        return lastModifiedDate;
    }
    
}