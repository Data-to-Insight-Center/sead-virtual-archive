package org.seadva.bagit.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bag package description that is passed between handlers
 */
public class PackageDescriptor {

    private String packageId;
    private MediciInstance mediciInstance;
    private String packageName;
    private String bagPath;
    private String unzippedBagPath;
    private String fetchFilePath;
    private String oreFilePath;
    private String manifestFilePath;
    private String sipPath;
    private String unTarredBagPath;
    private String bagittxtFilePath;
    private String bagInfoTxtFilePath;
    private String tagManifestFilePath;
    private String dpntagTxtFilePath;

    /**
     * Contains aggregations with structure:
     * <parent-entity-id, List<String> of child-entity-ids>
     */
    private Map<String,List<String>> aggregation;

    /**
     * Contains type collection or file for each entity
     */
    private Map<String, AggregationType> type;

    /**
     * Stores the properties for each entity:
     * entity-id: <predicate-key, List<String> of object-values>
     */
    private Map<String,Map<String,List<String>>> properties;

    public PackageDescriptor(String packageName, String bagPath, String unzippedBagPath){

        this.packageName = packageName;
        this.bagPath = bagPath;
        this.unzippedBagPath = unzippedBagPath;
        aggregation = new HashMap<String, List<String>>();
        type = new HashMap<String, AggregationType>();
        properties = new HashMap<String, Map<String, List<String>>>();
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getBagPath() {
        return bagPath;
    }

    public void setBagPath(String bagPath) {
        this.bagPath = bagPath;
    }

    public String getUnzippedBagPath() {
        return unzippedBagPath;
    }

    public void setUnzippedBagPath(String unzippedBagPath) {
        this.unzippedBagPath = unzippedBagPath;
    }

    public String getUntarredBagPath() {
        return unTarredBagPath;
    }

    public void setUntarredBagPath(String unTarredBagPath) {
        this.unTarredBagPath = unTarredBagPath;
    }

    public String getBagittxtFilePath() {
        return bagittxtFilePath;
    }

    public void setBagittxtFilePath(String manifestFilePath) {
        this.bagittxtFilePath = bagittxtFilePath;
    }

    public void setBagInfoTxtFilePath(String manifestFilePath) {
        this.bagInfoTxtFilePath = bagInfoTxtFilePath;
    }

    public String getBagInfoTxtFilePath() {
        return bagInfoTxtFilePath;
    }

    public void setDpntagTxtFilePath(String dpntagTxtFilePath) {
        this.dpntagTxtFilePath = dpntagTxtFilePath;
    }

    public String getDpntagTxtFilePath() {
        return dpntagTxtFilePath;
    }

    public void setTagManifestFilePath(String manifestFilePath) {
        this.tagManifestFilePath = tagManifestFilePath;
    }

    public String getTagManifestFilePath() {
        return tagManifestFilePath;
    }

    public String getSipPath() {
        return sipPath;
    }

    public void setSipPath(String sipPath) {
        this.sipPath = sipPath;
    }

    public String getFetchFilePath() {
        return fetchFilePath;
    }


    public String getManifestFilePath() {
        return manifestFilePath;
    }

    public void setManifestFilePath(String manifestFilePath) {
        this.manifestFilePath = manifestFilePath;
    }

    public Map<String, List<String>> getAggregation() {
        return aggregation;
    }

    public void setAggregation(Map<String, List<String>> aggregation) {
        this.aggregation = aggregation;
    }


    public Map<String, Map<String,List<String>>> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Map<String,List<String>>> properties) {
        this.properties = properties;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public MediciInstance getMediciInstance() {
        return mediciInstance;
    }

    public void setMediciInstance(MediciInstance mediciInstance) {
        this.mediciInstance = mediciInstance;
    }

    public Map<String, AggregationType> getType() {
        return type;
    }

    public void setType(Map<String, AggregationType> type) {
        this.type = type;
    }

    public String getOreFilePath() {
        return oreFilePath;
    }

    public void setOreFilePath(String oreFilePath) {
        this.oreFilePath = oreFilePath;
    }

    public void setFetchFilePath(String fetchFilePath) {
        this.fetchFilePath = fetchFilePath;
    }
}
