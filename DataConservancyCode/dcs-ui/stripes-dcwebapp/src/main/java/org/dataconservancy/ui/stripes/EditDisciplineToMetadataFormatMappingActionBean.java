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
package org.dataconservancy.ui.stripes;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.UiConfigurationUpdateException;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.model.MetadataFormatProperties;
import org.dataconservancy.ui.services.RelationshipException;

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_DISCIPLINE_DOESNT_EXIST;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_DISCIPLINE_RELATIONSHIP_REMOVAL_ERROR;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_DISCIPLINE_RELATIONSHIP_SET_ERROR;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_METADATA_FORMAT_DOESNT_EXIST;


/**
 * Supports the editing of business properties of metadata formats: their discipline mappings and flags
 * (applies to collection, project, item, etc.).
 */
@UrlBinding("/admin/uiconfig/editdisciplinemapping.action")
public class EditDisciplineToMetadataFormatMappingActionBean extends UiConfigurationActionBean {

    static final String EDIT_DISCIPLINE_MAPPING = "/pages/admineditdisciplinetomdf.jsp";

    private String metadataFormatId;
    private List<String> disciplineIds;
    private MetadataFormatProperties mdfProperties;

    public EditDisciplineToMetadataFormatMappingActionBean(){
        super();
        // Ensure desired properties are available.
        try{
            assert(messageKeys.containsKey(MSG_KEY_DISCIPLINE_RELATIONSHIP_REMOVAL_ERROR));
            assert(messageKeys.containsKey(MSG_KEY_DISCIPLINE_RELATIONSHIP_SET_ERROR));
            assert(messageKeys.containsKey(MSG_KEY_METADATA_FORMAT_DOESNT_EXIST));
            assert(messageKeys.containsKey(MSG_KEY_DISCIPLINE_DOESNT_EXIST));
        }  catch (AssertionError e){
          throw new RuntimeException("Missing required message key!  One of " +
            MSG_KEY_DISCIPLINE_RELATIONSHIP_REMOVAL_ERROR  + ", " +
            MSG_KEY_DISCIPLINE_RELATIONSHIP_SET_ERROR   + ", " +
            MSG_KEY_METADATA_FORMAT_DOESNT_EXIST + ", " +
            MSG_KEY_DISCIPLINE_DOESNT_EXIST + " is missing.");
        }
        
    }

    ///////////////////////////////
    //
    // Accessors
    //
    ///////////////////////////////

    public List<String> getDisciplineIds() {
        return disciplineIds;
    }

    public void setDisciplineIds(List<String> disciplineIds) {
        this.disciplineIds = new ArrayList<String>();
        for (String id : disciplineIds) {
            if (disciplineDao.get(id) != null) {
                this.disciplineIds.add(id);
            }
        }
    }

    public String getMetadataFormatId() {
        return metadataFormatId;
    }

    public void setMetadataFormatId(String metadataFormatId) {
        if (metadataFormatService.getMetadataFormat(metadataFormatId) != null) {
            this.metadataFormatId = metadataFormatId;
        }   
    }

    public List<Discipline> getAllDisciplines() {
        return disciplineDao.list();
    }

    public DcsMetadataFormat getMetadataFormat() {
        if (metadataFormatId == null || "".equals(metadataFormatId)) {
            return null;
        }
        return metadataFormatService.getMetadataFormat(metadataFormatId);
    }

    public MetadataFormatProperties getMdfProperties() {

        if (this.mdfProperties != null) {
            return this.mdfProperties;
        }

        if (metadataFormatId == null) {
            return null;
        }


        this.mdfProperties = metadataFormatService.getProperties(metadataFormatId);

        if (this.mdfProperties == null) {
            this.mdfProperties = new MetadataFormatProperties();
            this.mdfProperties.setFormatId(metadataFormatId);
        }

        return this.mdfProperties;
    }

    public void setMdfProperties(MetadataFormatProperties mdfProperties) {
        this.mdfProperties = mdfProperties;
    }

    ///////////////////////////////
    //
    // Stripes Resolutions
    //
    ///////////////////////////////

    public Resolution editDisciplineMapping() {
        return new ForwardResolution(EDIT_DISCIPLINE_MAPPING);
    }

    public Resolution saveDisciplineMapping() throws UiConfigurationUpdateException, RelationshipException, BizInternalException {
        final DcsMetadataFormat mdf = metadataFormatService.getMetadataFormat(metadataFormatId);
        MetadataFormatProperties existingProperties = metadataFormatService.getProperties(mdf.getId());

        if (mdf == null) {
            final String message = String.format(messageKeys.getProperty(MSG_KEY_METADATA_FORMAT_DOESNT_EXIST), metadataFormatId);
            throw new UiConfigurationUpdateException(message);
        }

        // If metadata format does not already have a record for its business properties, makes sure that when the
        //record is added, it is set to active
        if (existingProperties == null) {
            this.mdfProperties.setActive(true);
        }

        metadataFormatService.setProperties(metadataFormatService.getMetadataFormat(metadataFormatId),
                this.mdfProperties);
        return new RedirectResolution(UiConfigurationActionBean.class, "displayMetadataFormatList");
    }


    ///////////////////////////////
    //
    // Stripes injected services
    //
    ///////////////////////////////


}
