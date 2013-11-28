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
package org.dataconservancy.ui.exceptions;

import net.sourceforge.stripes.action.FileBean;

/**
 * Indicates there was a problem with a UI deposit operation, which probably resulted in a failed deposit.
 */
public class IngestException extends BaseUiException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The name of the file deposited.  It might be a {@link #isContainer() container} (e.g. a zip file) or a
     * file intended to be deposited as a DataItem.
     */
    private FileBean uploadedFile;

    /**
     * The userId attempting the deposit or update operation
     */
    private String userId;

    public IngestException() {
        super();
    }

    public IngestException(String message, Throwable cause) {
        super(message, cause);
    }

    public IngestException(String message) {
        super(message);
    }

    public IngestException(Throwable cause) {
        super(cause);
    }


    /**
     * The actual Stripes FileBean that was uploaded.
     * 
     * @return the Stripes FileBean
     */
    public FileBean getUploadedFile() {
        return uploadedFile;
    }

    /**
     * The actual Stripes FileBean that was uploaded.
     * 
     * @param uploadedFile
     *            the Stripes FileBean
     */
    public void setUploadedFile(FileBean uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

}
