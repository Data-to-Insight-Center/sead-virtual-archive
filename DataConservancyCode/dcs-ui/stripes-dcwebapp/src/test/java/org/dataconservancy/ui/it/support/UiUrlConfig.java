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
package org.dataconservancy.ui.it.support;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public class UiUrlConfig {

    private String hostname;
    private int port;
    private boolean isSecure;
    private String contextPath;
    private Scheme scheme;

    private String loginPath;
    private String loginPostPath;
    private String logoutPath;
    private String profilePath;
    private String homePath;
    private String registrationPath;
    private String editProfilePath;
    private String projectPath;
    private String projectApiPath;
    private String adminPath;
    private String adminRegistrationPath;
    private String adminValidatingMetadataFilePath;
    private String uiConfigPath;
    private String addCollectionPath;
    private String viewCollectionPath;
    private String listCollectionsPath;
    private String listProjectCollectionsPath;
    private String depositPath;
    private String userCollectionsPath;
    private String depositStatusPath;
    private String projectCollectionsPath;
    private String collectionDataListPath;
    private String listProjectActivityPath;
    private String dataItemSplashPath;
    private String collectionSplashPath;
    private String createIdApiPath;
    private String metadataFilePath;
    private String updateCollectionPath;
    private String citableLocatorPath;
    private String listMetadataFormatPath;
    private String ingestPackagePath;
    private String ingestStatusPath;


    private enum Scheme {HTTP, HTTPS}

    public URL getBaseUrl() {
        URL u = null;
        try {
            u = new URL(
                    scheme.toString().toLowerCase(),
                    hostname,
                    port,
                    contextPath);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return u;
    }

    public URL getLoginUrl() {
        return createUrl(getBaseUrl(), contextPath + loginPath);
    }

    public URL getHomeUrl() {
        return createUrl(getBaseUrl(), contextPath + homePath);
    }

    public URL getProfileUrl() {
        return createUrl(getBaseUrl(), contextPath + profilePath);
    }
    
    public URL getRegistrationUrl() {
        return createUrl(getBaseUrl(), contextPath + registrationPath);
    }

    public URL getEditProfileUrl() {
        return createUrl(getBaseUrl(), contextPath + editProfilePath);
    }

    public URL getProjectUrl() {
        return createUrl(getBaseUrl(), contextPath + projectPath);
    }
    
    public URL getProjectApiUrl() {
        return createUrl(getBaseUrl(), contextPath + projectApiPath);
    }

    public URL getProjectUrl(String projectId) {
        // /userprofile/project.action?selectedProjectId=projectId
        // http://localhost:8080/userprofile/project.action?viewUserProject=&selectedProjectId=http%3A%2F%2Fdms.jhu.edu%2F1
        return createUrl(getBaseUrl(), contextPath + projectPath + "?viewUserProject=&selectedProjectId=" + projectId);
    }

    public URL getAdminUrl() {
        return createUrl(getBaseUrl(), contextPath + adminPath);
    }

    public URL getUiConfigUrl() {
        return createUrl(getBaseUrl(), contextPath + uiConfigPath);
    }

    public URL getAddCollectionUrl() {
        return createUrl(getBaseUrl(), contextPath + addCollectionPath);
    }

    public URL getListCollectionsUrl() {
        return createUrl(getBaseUrl(), contextPath + listCollectionsPath);
    }

    public URL getListProjectCollectionsUrl() {
          return createUrl(getBaseUrl(), contextPath + listProjectCollectionsPath);
    }

    public URL getViewCollectionUrl(String collectionId) {
        return createUrl(getBaseUrl(), contextPath + viewCollectionPath + collectionId);
    }

    public URL getLogoutUrl() {
        return createUrl(getBaseUrl(), contextPath + logoutPath);
    }

    public URL getDepositUrl() {
        return createUrl(getBaseUrl(), contextPath + depositPath);
    }
    
    public URL getUserCollectionsUrl() {
        return createUrl(getBaseUrl(), contextPath + userCollectionsPath);
    }

    public URL getDepositStatusUrl(String objectId) {
        return createUrl(getBaseUrl(), contextPath + depositStatusPath + "?objectId=" + objectId);
    }

    public URL getDepositStatusUrl() {
        return createUrl(getBaseUrl(), contextPath + depositStatusPath);
    }

    public URL getProjectCollectionsUrl() {
        return createUrl(getBaseUrl(), contextPath + projectCollectionsPath);
    }
    
    public URL getCollectionDataListUrl() {
        return createUrl(getBaseUrl(), contextPath + collectionDataListPath);
    }

    public URL getListProjectActivityUrl(String selectedProjectId) {
         return createUrl(getBaseUrl(), contextPath + listProjectActivityPath + selectedProjectId);
    }

    public URL getAdminRegistrationUrl() {
        return createUrl(getBaseUrl(), contextPath + adminRegistrationPath);
    }
    
    public URL getDataItemSplashUrl() {
        return createUrl(getBaseUrl(), contextPath + dataItemSplashPath);
    }
    
    public URL getCollectionSplashUrl() {
        return createUrl(getBaseUrl(), contextPath + collectionSplashPath);
    }

    public URL getCreateIdApiUrl() {
        return createUrl(getBaseUrl(), contextPath + createIdApiPath);
    }

    public URL getMetadataFileUrl() {
        return createUrl(getBaseUrl(), contextPath + metadataFilePath);
    }
    
    public URL getUpdateCollectionUrl() {
        return createUrl(getBaseUrl(), contextPath + updateCollectionPath);
    }

    public URL getCitableLocatorPostUrl(){
        return createUrl(getBaseUrl(), contextPath + citableLocatorPath);
    }

    public URL getCitableLocatorGetUrl(String collectionId, String citableLocatorId){
        return createUrl(getBaseUrl(), contextPath + citableLocatorPath + "?collectionId=" + collectionId + "&reservedCitableLocator=" + citableLocatorId);
    }
    
    public URL getAdminValidatingMetadataFilePathPostUrl() {
        return createUrl(getBaseUrl(), contextPath + adminValidatingMetadataFilePath);
    }

    public URL getListMetadataFormatUrl() {
        return createUrl(getBaseUrl(), contextPath + listMetadataFormatPath);
    }

    public URL getIngestPackageUrl() {
        return createUrl(getBaseUrl(), contextPath + ingestPackagePath);
    }

    public URL getIngestStatusUrl(String depositId) {
        return createUrl(getBaseUrl(), contextPath + ingestStatusPath + depositId);
    }

    public String getHostname() {
        return hostname;
    }

    public URL getLoginPostUrl() {
        return createUrl(getBaseUrl(), contextPath + loginPostPath);
    }

    public void setHostname(String hostname) {
        if (hostname == null || hostname.trim().length() == 0) {
            throw new IllegalArgumentException("Hostname must not be empty or null.");
        }
        if (hostname.contains(":")) {
            throw new IllegalArgumentException("Hostname should not contain a port or url scheme, just a fully qualified domain name.");
        }
        this.hostname = hostname.toLowerCase().trim();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (port < 1) {
            throw new IllegalArgumentException("Port must be 1 or greater.");
        }
        this.port = port;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public void setSecure(boolean secure) {
        isSecure = secure;
        if (secure && scheme != Scheme.HTTPS) {
            scheme = Scheme.HTTPS;
        }
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        if (contextPath == null) {
            throw new IllegalArgumentException("Context path must not be null.");
        }
        this.contextPath = normalizePath(contextPath);
    }

    public String getScheme() {
        return scheme.toString().toLowerCase();
    }

    public void setScheme(String scheme) {
        if (scheme == null || scheme.trim().length() == 0) {
            throw new IllegalArgumentException("Protocol scheme must not be empty or null.");
        }
        scheme = scheme.toUpperCase().trim();
        this.scheme = Scheme.valueOf(scheme);
        if (this.scheme == Scheme.HTTPS) {
            isSecure = true;
        }
    }

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        if (loginPath == null || loginPath.trim().length() == 0) {
            throw new IllegalArgumentException("Login path must not be empty or null.");
        }
        this.loginPath = normalizePath(loginPath);
    }

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        if (profilePath == null || profilePath.trim().length() == 0) {
            throw new IllegalArgumentException("Profile path must not be empty or null.");
        }
        this.profilePath = normalizePath(profilePath);
    }

    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        if (homePath == null || homePath.trim().length() == 0) {
            throw new IllegalArgumentException("Home path must not be empty or null.");
        }
        this.homePath = normalizePath(homePath);
    }
    
    public String getRegistrationPath(){
        return registrationPath;
    }
    
    public void setRegistrationPath(String registrationPath){
        if (registrationPath == null || registrationPath.trim().length() == 0) {
            throw new IllegalArgumentException("Registration path must not be empty or null.");
        }
        this.registrationPath = normalizePath(registrationPath);
    }
    
    public String getEditProfilePath(){
        return editProfilePath;
    }
    
    public void setEditProfilePath(String editProfilePath){
        if (editProfilePath == null || editProfilePath.trim().length() == 0) {
            throw new IllegalArgumentException("Registration path must not be empty or null.");
        }
        this.editProfilePath = normalizePath(editProfilePath);
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        if (projectPath == null || projectPath.trim().length() == 0) {
            throw new IllegalArgumentException("Project path must not be empty or null.");
        }
        this.projectPath = normalizePath(projectPath);
    }
    
    public void setProjectApiPath(String projectPath) {
        if (projectPath == null || projectPath.trim().length() == 0) {
            throw new IllegalArgumentException("Project api path must not be empty or null.");
        }
        this.projectApiPath = normalizePath(projectPath);
    }

    public void setAdminPath(String adminPath) {
        if (adminPath == null || adminPath.trim().length() == 0) {
            throw new IllegalArgumentException("Admin path must not be empty or null.");
        }
        this.adminPath = normalizePath(adminPath);
    }

    public String getUiConfigPath() {
        return uiConfigPath;
    }

    public void setUiConfigPath(String uiConfigPath) {
        if (uiConfigPath == null || uiConfigPath.trim().length() == 0) {
            throw new IllegalArgumentException("UI Configuration path must not be empty or null.");
        }
        this.uiConfigPath = normalizePath(uiConfigPath);
    }

    public String getAddCollectionPath() {
        return addCollectionPath;
    }

    public void setAddCollectionPath(String addCollectionPath) {
        if (addCollectionPath == null || addCollectionPath.trim().length() == 0) {
            throw new IllegalArgumentException("Add collection path must not be empty or null.");
        }
        this.addCollectionPath = normalizePath(addCollectionPath);
    }

    public String getLogoutPath() {
        return logoutPath;
    }

    public void setLogoutPath(String logoutPath) {
        if (logoutPath == null || logoutPath.trim().length() == 0) {
            throw new IllegalArgumentException("Logout path must not be empty or null.");
        }
        this.logoutPath = logoutPath;
    }

    public String getViewCollectionPath() {
        return viewCollectionPath;
    }

    public void setViewCollectionPath(String viewCollectionPath) {
        if (viewCollectionPath == null || viewCollectionPath.trim().length() == 0) {
            throw new IllegalArgumentException("View collection path must not be empty or null.");
        }
        this.viewCollectionPath = viewCollectionPath;
    }

    public String getDepositPath() {
        return depositPath;
    }

    public void setDepositPath(String depositPath) {
        if (depositPath == null || depositPath.trim().length() == 0) {
            throw new IllegalArgumentException("Deposit path must not be empty or null.");
        }
        this.depositPath = depositPath;
    }

    public String getListCollectionsPath() {
        return listCollectionsPath;
    }

    public void setListCollectionsPath(String listCollectionsPath) {
        if (listCollectionsPath == null || listCollectionsPath.trim().length() == 0) {
            throw new IllegalArgumentException("List collections path must not be empty or null.");
        }
        this.listCollectionsPath = listCollectionsPath;
    }

     public String getListProjectCollectionsPath() {
        return listProjectCollectionsPath;
    }

    public void setListProjectCollectionsPath(String listProjectCollectionsPath) {
        if (listProjectCollectionsPath == null || listProjectCollectionsPath.trim().length() == 0) {
            throw new IllegalArgumentException("List project's collections path must not be empty or null.");
        }
        this.listProjectCollectionsPath = listProjectCollectionsPath;
    }
    
    public String getUserCollectionsPath() {
        return userCollectionsPath;
    }
    
    public void setUserCollectionsPath(String userCollectionsPath) {
        if (userCollectionsPath == null || userCollectionsPath.trim().length() == 0) {
            throw new IllegalArgumentException("User collections path must not be empty or null.");
        }
        this.userCollectionsPath = userCollectionsPath;
    }

    public String getLoginPostPath() {
        return loginPostPath;
    }

    public void setLoginPostPath(String loginPostPath) {
        if (loginPostPath == null || loginPostPath.isEmpty()) {
            throw new IllegalArgumentException("Login POST path must not be empty.");
        }
        this.loginPostPath = loginPostPath;
    }

    public String getDepositStatusPath() {
        return depositStatusPath;
    }

    public void setDepositStatusPath(String depositStatusPath) {
        if (depositStatusPath == null || depositStatusPath.trim().length() == 0) {
            throw new IllegalArgumentException("Deposit status path must not be empty or null.");
        }
        this.depositStatusPath = depositStatusPath;
    }
    
    public void setProjectCollectionsPath(String projectCollectionsPath) {
        if (projectCollectionsPath == null || projectCollectionsPath.trim().length() == 0) {
            throw new IllegalArgumentException("Project collections path must noy be empty or null.");
        }
        this.projectCollectionsPath = projectCollectionsPath;
    }
    
    public String getCollectionDataListPath() {
        return collectionDataListPath;
    }

    public void setCollectionDataListPath(String collectionDataListPath) {
        if (collectionDataListPath == null || collectionDataListPath.trim().length() == 0) {
            throw new IllegalArgumentException("Collection data list path must not be empty or null.");
        }
        this.collectionDataListPath = collectionDataListPath;
    }

    public void setListProjectActivityPath(String listProjectActivityPath) {
        if (listProjectActivityPath == null || listProjectActivityPath.trim().length() == 0) {
            throw new IllegalArgumentException("Project activity path must noy be empty or null.");
        }
        this.listProjectActivityPath = listProjectActivityPath;
    }

    public String getAdminRegistrationPath() {
        return adminRegistrationPath;
    }

    public void setAdminRegistrationPath(String adminRegistrationPath) {
        if (adminRegistrationPath == null || adminRegistrationPath.trim().length() == 0) {
            throw new IllegalArgumentException("Admin registration path must not be empty or null.");
        }
        this.adminRegistrationPath = adminRegistrationPath;
    }
    
    public String getAdminValidatingMetadataFilePath() {
        return adminValidatingMetadataFilePath;
    }
    
    public void setAdminValidatingMetadataFilePath(String adminValidatingMetadataFilePath) {
        if (adminValidatingMetadataFilePath == null || adminValidatingMetadataFilePath.trim().length() == 0) {
            throw new IllegalArgumentException("Admin test metadata file path must not be empty or null.");
        }
        this.adminValidatingMetadataFilePath = adminValidatingMetadataFilePath;
    }
    
    public String getDataItemSplashPath() {
        return dataItemSplashPath;
    }
    
    public String getCollectionSplashPath() {
        return collectionSplashPath;
    }
    
    public void setDataItemSplashPath(String dataItemSplashPath) {
        if (dataItemSplashPath == null || dataItemSplashPath.trim().length() == 0) {
            throw new IllegalArgumentException("Admin registration path must not be empty or null.");
        }
        this.dataItemSplashPath = dataItemSplashPath;
    }
    
    public void setCollectionSplashPath(String collectionSplashPath) {
        if (null == collectionSplashPath || collectionSplashPath.length() == 0) {
            throw new IllegalArgumentException("Collection path must not be empty or null.");
        }
        this.collectionSplashPath = collectionSplashPath;
    }
    
    public String getMetadataFilePath() {
        return metadataFilePath;
    }
    
    public void setMetadataFilePath(String metadataFilePath) {
        if (null == metadataFilePath || metadataFilePath.isEmpty()) {
            throw new IllegalArgumentException("Metadata file path must not be empty or null.");
        }
        this.metadataFilePath = metadataFilePath;
    }

    public void setCreateIdApiPath(String createIdApiPath) {
        if (createIdApiPath == null || createIdApiPath.trim().length() == 0) {
            throw new IllegalArgumentException("Id Creation API path must not be empty or null.");
        }
        this.createIdApiPath = createIdApiPath;
    }

    public String getCreateIdApiPath() {
        return this.createIdApiPath;
    }
    
    public String getUpdateCollectionPath() {
        return updateCollectionPath;
    }
    
    public void setUpdateCollectionPath(String updateCollectionPath) {
        if (updateCollectionPath == null || updateCollectionPath.trim().length() == 0) {
            throw new IllegalArgumentException("Update collection path must not be empty or null.");
        }
        this.updateCollectionPath = updateCollectionPath;
    }

    public String getCitableLocatorPath(){
        return citableLocatorPath;
    }

    public void setCitableLocatorPath(String citableLocatorPath) {
        if (citableLocatorPath == null || citableLocatorPath.trim().length() == 0) {
            throw new IllegalArgumentException("Citable locator path path must not be empty or null.");
        }
        this.citableLocatorPath = citableLocatorPath;
    }

    public String getListMetadataFormatPath() {
        return listMetadataFormatPath;
    }

    public void setListMetadataFormatPath(String listMetadataFormatPath) {
        if (listMetadataFormatPath == null || listMetadataFormatPath.trim().length() == 0) {
            throw new IllegalArgumentException("List metadataformat path must not be empty or null.");
        }
        this.listMetadataFormatPath = listMetadataFormatPath;
    }

    public String getIngestPackagePath() {
        return ingestPackagePath;
    }

    public void setIngestPackagePath(String ingestPackagePath) {
        if (ingestPackagePath == null || ingestPackagePath.trim().length() == 0) {
            throw new IllegalArgumentException("Ingest package path must not be empty or null.");
        }
        this.ingestPackagePath = ingestPackagePath;
    }

    public String getIngestStatusPath() {
        return ingestStatusPath;
    }

    public void setIngestStatusPath(String ingestStatusPath) {
        if (ingestPackagePath == null || ingestPackagePath.trim().length() == 0) {
            throw new IllegalArgumentException("Ingest status path must not be empty or null.");
        }
        this.ingestStatusPath = ingestStatusPath;
    }

    private URL createUrl(URL context, String spec) {
        URL u = null;
        try {
            u = new URL(getBaseUrl(), spec);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Attempt to create a URL from '" + getBaseUrl().toString() + "' and '" + spec +
                    "' failed: " + e.getMessage(), e);
        }

        return u;
    }

    /**
     * Lowercases and trims a String, and insures that the string doesn't
     * end with a "/".
     *
     * @param path the path to normalize
     * @return the normalized path
     */
    private String normalizePath(String path) {
        path = path.toLowerCase().trim();
        while (path.endsWith("/") && path.length() > 0) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public String toString() {
        return "UiUrlConfig{" +
                "hostname='" + hostname + '\'' + 
                ", port=" + port + 
                ", isSecure=" + isSecure + 
                ", contextPath='" + contextPath + '\'' + 
                ", scheme=" + scheme + 
                ", loginPath='" + loginPath + '\'' + 
                ", loginPostPath='" + loginPostPath + '\'' +
                ", logoutPath='" + logoutPath + '\'' +
                ", profilePath='" + profilePath + '\'' +
                ", homePath='" + homePath + '\'' +
                ", registrationPath='" + registrationPath + '\'' +
                ", editProfilePath='" + editProfilePath + '\'' +
                ", projectPath='" + projectPath + '\'' +
                ", adminPath='" + adminPath + '\'' +
                ", uiConfigPath='" + uiConfigPath + '\'' +
                ", addCollectionPath='" + addCollectionPath + '\'' +
                ", viewCollectionPath='" + viewCollectionPath + '\'' +
                ", listCollectionsPath='" + listCollectionsPath + '\'' +
                ", listProjectCollectionsPath='" + listProjectCollectionsPath + '\'' +
                ", depositPath='" + depositPath + '\'' +
                ", userCollectionsPath='" + userCollectionsPath + '\'' +
                ", depositStatusPath='" + depositStatusPath + '\'' +
                ", projectCollectionsPath='" + projectCollectionsPath + '\'' +
                ", listProjectActivityPath='" + listProjectActivityPath + '\'' +
                ", collectionSplashPath='" + collectionSplashPath + '\'' +
                ", citableLocatorPath='" + citableLocatorPath + '\'' +
 ", adminValidatingMetadataFilePath='"
                + adminValidatingMetadataFilePath + '\'' +
                '}';
    }
}
