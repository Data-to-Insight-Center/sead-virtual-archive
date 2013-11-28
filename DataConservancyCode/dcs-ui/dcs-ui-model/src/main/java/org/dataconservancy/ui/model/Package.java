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
package org.dataconservancy.ui.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Package contains information about a zip or similar packaging used to deposit data sets.  fileData is a hash has keys
 * which are the objectIds of the data sets in the package, and values are the file names contained in the package. This
 * class is used to help track status at the time of deposit.
 */
public class Package {

    private String id;
    private PackageType packageType;
    private String packageFileName;
    private Map<String, String> fileData;

    public static enum PackageType {
        /**
         * String representing a zipped file package that is meant to be unpackaged for deposit
         */
        ZIP,
        TAR,
        /**
         * String representing a single datafile that is meant to be deposited as is
         */
        SIMPLE_FILE;
    }

    public Package() {
        this.fileData = new HashMap<String, String>();
    }

    public String getPackageFileName() {
        return packageFileName;
    }

    public void setPackageFileName(String fileName) {
        this.packageFileName = fileName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
    }

    /**
     * @return dataSetId to filename mapping by reference
     */
    public Map<String, String> getFileData() {
        return fileData;
    }

    public void addFile(String dataSetId, String filename) {
        fileData.put(dataSetId, filename);
    }

    public void removeFile(String dataSetId) {
        fileData.remove(dataSetId);
    }

    // We can edit this later to output what we want
    @Override
    public String toString() {
        return "Package{" +
                "id='" + id + '\'' +
                ", packageType='" + packageType + '\'' +
                ", packageFileName='" + packageFileName + '\'' +
                ", fileData='" + fileData +
                '}';
    }


    /**
     * Determines if this Package is equal to another object.
     * <p/>
     * Currently equality is determined by comparing the value of the following fields:
     * <ul>
     * <li>Object Id</li></li>
     * <li>Package Type</li>
     * <li>Package File Name</li>
     * <li>File Data Map</li>
     * </ul>
     *
     * @param o the object to test for equality
     * @return true if the objects are equal
     */

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Package)) {
            return false;
        }

        Package p = (Package) o;

        return equals(getId(), p.getId()) && equals(getPackageType(), p.getPackageType()) &&
                equals(getFileData(), p.getFileData());
    }

    private static boolean equals(Object o1, Object o2) {
        return (o1 == null && o2 == null)
                || (o1 != null && o2 != null && o1.equals(o2));
    }


    /**
     * Calculates a hash code using the fields that are considered for {@link #equals(Object) equality}.
     *
     * @return the hashcode
     */
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (packageType != null ? packageType.hashCode() : 0);
        result = 31 * result + (packageFileName != null ? packageFileName.hashCode() : 0);
        result = 31 * result + (fileData != null ? fileData.hashCode() : 0);
        return result;
    }
}
