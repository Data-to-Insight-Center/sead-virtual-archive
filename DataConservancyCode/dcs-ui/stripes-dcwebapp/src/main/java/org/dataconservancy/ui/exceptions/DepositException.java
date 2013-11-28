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
public class DepositException extends BaseUiException {

    /**
     * The Collection ID being deposited to
     */
    private String collectionId;

    /**
     * The ID of the DataItem being deposited.
     */
    private String datasetId;

    /**
     * The name of the DataItem being deposited, or the name of the file if it is a {@link #isContainer()}
     */
    private String datasetName;

    /**
     * The name of the file deposited.  It might be a {@link #isContainer() container} (e.g. a zip file) or a
     * file intended to be deposited as a DataItem.
     */
    private FileBean depositedFile;

    /**
     * If true, the deposit was a packaged file (e.g. a zip file)
     */
    private boolean isContainer = false;

    /**
     * The userId attempting the deposit or update operation
     */
    private String userId;

    public DepositException() {
        super();
    }

    public DepositException(String message, Throwable cause) {
        super(message, cause);
    }

    public DepositException(String message) {
        super(message);
    }

    public DepositException(Throwable cause) {
        super(cause);
    }

    /**
     * The ID of the collection that was being deposited to.
     *
     * @return the collection id
     */
    public String getCollectionId() {
        return collectionId;
    }

    /**
     * The ID of the collection that was being deposited to.
     *
     * @param collectionId the collection id
     */
    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    /**
     * The ID of the DataItem that was being deposited.
     *
     * @return the DataItem id
     */
    public String getDatasetId() {
        return datasetId;
    }

    /**
     * The ID of the DataItem that was being deposited.
     *
     * @param datasetId the DataItem id
     */
    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * The actual Stripes FileBean that was deposited.
     *
     * @return the Stripes FileBean
     */
    public FileBean getDepositedFile() {
        return depositedFile;
    }

    /**
     * The actual Stripes FileBean that was deposited.
     *
     * @param depositedFile the Stripes FileBean
     */
    public void setDepositedFile(FileBean depositedFile) {
        this.depositedFile = depositedFile;
    }

    /**
     * The name of the DataItem being deposited.  If the deposit is a container, this will
     * be the name of the file.
     *
     * @return the DataItem name
     */
    public String getDatasetName() {
        return datasetName;
    }

    /**
     * The name of the DataItem being deposited.  If the deposit is a container, this will
     * be the name of the file.
     *
     * @param datasetName the DataItem name
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    /**
     * Indicates whether the deposited file is a container (e.g. a zip file) to be unpacked into multiple
     * DataSets, or whether the deposited file is itself a DataItem.
     *
     * @return the container flag
     */
    public boolean isContainer() {
        return isContainer;
    }

    /**
     * Indicates whether the deposited file is a container (e.g. a zip file) to be unpacked into multiple
     * DataSets, or whether the deposited file is itself a DataItem.
     *
     * @param container the container flag
     */
    public void setContainer(boolean container) {
        isContainer = container;
    }

    /**
     * The userId performing the deposit or update operation.
     *
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
