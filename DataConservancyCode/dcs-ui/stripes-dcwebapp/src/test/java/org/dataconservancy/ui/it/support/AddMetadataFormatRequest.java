/*
 * Copyright 2013 Johns Hopkins University
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

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.dataconservancy.ui.stripes.UiConfigurationActionBean;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the state and logic for adding a new metadata format to the DC UI.
 */
public class AddMetadataFormatRequest {

    private static final String STRIPES_EVENT = "addNewFormat";

    private final UiUrlConfig uiUrlConfig;

    /**
     * The name of the Metadata Format
     */
    private String name;

    /**
     * The version of the Metadata Format
     */
    private String version;

    /**
     * The HTTP URL to the schema document on the Internet.  If the schema is composed of multiple documents, this
     * should point to the main schema document that includes the remaining documents.
     */
    private String schemaUrl;

    /**
     * If the Format is allowed on Projects
     */
    private Boolean appliesToProject;

    /**
     * If the Format is allowed on Collections
     */
    private Boolean appliesToCollection;

    /**
     * If the Format is allowed on Items
     */
    private Boolean appliesToItem;

    /**
     * ??
     */
    private Boolean validates;

    /**
     * The disciplines this Format applies to
     */
    private List<String> disciplineIds;

    public AddMetadataFormatRequest(UiUrlConfig uiUrlConfig) {
        if (uiUrlConfig == null) {
            throw new IllegalArgumentException("UI URL config must not be null.");
        }
        this.uiUrlConfig = uiUrlConfig;
    }

    /**
     * Creates the request from the state held in this request object.
     *
     * @return the form request
     */
    public HttpPost asHttpPost() {
        UiConfigurationActionBean bean = new UiConfigurationActionBean();
        UiConfigurationActionBean.MetaDataFormatTransport mdft = bean.getNewMetadataFormatTransport();
        mdft.setName(name);
        mdft.setVersion(version);
        mdft.setSchemaURL(schemaUrl);
        mdft.setDisciplineIds(disciplineIds);

        if (appliesToCollection == null) {
            mdft.setAppliesToCollection(UiConfigurationActionBean.NOT_SPECIFIED);
        } else if (appliesToCollection == Boolean.TRUE) {
            mdft.setAppliesToCollection(UiConfigurationActionBean.YES);
        } else {
            mdft.setAppliesToCollection(UiConfigurationActionBean.NO);
        }

        if (appliesToProject == null) {
            mdft.setAppliesToProject(UiConfigurationActionBean.NOT_SPECIFIED);
        } else if (appliesToProject == Boolean.TRUE) {
            mdft.setAppliesToProject(UiConfigurationActionBean.YES);
        } else {
            mdft.setAppliesToProject(UiConfigurationActionBean.NO);
        }

        if (appliesToItem == null) {
            mdft.setAppliesToItem(UiConfigurationActionBean.NOT_SPECIFIED);
        } else if (appliesToItem == Boolean.TRUE) {
            mdft.setAppliesToItem(UiConfigurationActionBean.YES);
        } else {
            mdft.setAppliesToItem(UiConfigurationActionBean.NO);
        }

        if (validates == null) {
            mdft.setValidates(UiConfigurationActionBean.NOT_SPECIFIED);
        } else if (validates == Boolean.TRUE) {
            mdft.setValidates(UiConfigurationActionBean.YES);
        } else {
            mdft.setValidates(UiConfigurationActionBean.NO);
        }

        return asHttpPost(mdft);
    }

    /**
     * Creates the request from the state held in the supplied {@code MetaDataFormatTransport} object.  Any state
     * held in this request object is ignored.
     *
     * @param mdft the MetaDataFormatTransport containing the state used for the request.
     * @return the form request
     */
    public HttpPost asHttpPost(UiConfigurationActionBean.MetaDataFormatTransport mdft) {
        final HttpPost form = new HttpPost(uiUrlConfig.getUiConfigUrl().toExternalForm());
        final String FIELD_PREFIX = "newMetadataFormatTransport.";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(FIELD_PREFIX + "name", mdft.getName()));
        params.add(new BasicNameValuePair(FIELD_PREFIX + "version", mdft.getVersion()));
        params.add(new BasicNameValuePair(FIELD_PREFIX + "schemaURL", mdft.getSchemaURL()));
        params.add(new BasicNameValuePair(FIELD_PREFIX + "appliesToProject", mdft.getAppliesToProject()));
        params.add(new BasicNameValuePair(FIELD_PREFIX + "appliesToCollection", mdft.getAppliesToCollection()));
        params.add(new BasicNameValuePair(FIELD_PREFIX + "appliesToItem", mdft.getAppliesToItem()));
        params.add(new BasicNameValuePair(FIELD_PREFIX + "validates", mdft.getValidates()));
        params.add(new BasicNameValuePair("_sourcePage", uiUrlConfig.getUiConfigUrl().toExternalForm()));
        if (mdft.getDisciplineIds() != null && !mdft.getDisciplineIds().isEmpty()) {
            int i = 0;
            for (String disciplineId : mdft.getDisciplineIds()) {
                params.add(new BasicNameValuePair(FIELD_PREFIX + "disciplineIds[" + i++ + "]", disciplineId));
            }
        }

        params.add(new BasicNameValuePair(STRIPES_EVENT, "submit"));

        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        form.setEntity(entity);
        return form;
    }

    public Boolean getAppliesToCollection() {
        return appliesToCollection;
    }

    public void setAppliesToCollection(Boolean appliesToCollection) {
        this.appliesToCollection = appliesToCollection;
    }

    public Boolean getAppliesToItem() {
        return appliesToItem;
    }

    public void setAppliesToItem(Boolean appliesToItem) {
        this.appliesToItem = appliesToItem;
    }

    public Boolean getAppliesToProject() {
        return appliesToProject;
    }

    public void setAppliesToProject(Boolean appliesToProject) {
        this.appliesToProject = appliesToProject;
    }

    public List<String> getDisciplineIds() {
        return disciplineIds;
    }

    public void setDisciplineIds(List<String> disciplineIds) {
        this.disciplineIds = disciplineIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchemaUrl() {
        return schemaUrl;
    }

    public void setSchemaUrl(String schemaUrl) {
        this.schemaUrl = schemaUrl;
    }

    public Boolean getValidates() {
        return validates;
    }

    public void setValidates(Boolean validates) {
        this.validates = validates;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
