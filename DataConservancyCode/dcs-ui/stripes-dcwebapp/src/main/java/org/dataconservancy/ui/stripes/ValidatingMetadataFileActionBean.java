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

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_DISCIPLINE_DOESNT_EXIST;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_METADATA_FORMAT_DOESNT_EXIST;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.MetadataResult;
import org.dataconservancy.ui.model.MetadataResult.MetadataEventMessage;
import org.dataconservancy.ui.services.FileBizService;
import org.dataconservancy.ui.services.MetadataBizService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;


/**
 * Supports the validation of sample Metadata files against a schema in the registry.
 */
@UrlBinding("/admin/uiconfig/validatingmetadatafile.action")
public class ValidatingMetadataFileActionBean extends UiConfigurationActionBean {

    private String metadataFormatName;

    private IdService idService;
    
    private FileBizService fileBizService;

    private MetadataBizService metadataBizService;
    
    private String metadataFormatId;
    
    private String redirectUrl;
    
    private TypedRegistry<DcsMetadataFormat> formatRegistry;
    
    public ValidatingMetadataFileActionBean(){
        super();

        // Ensure desired properties are available.
        try{
            assert(messageKeys.containsKey(MSG_KEY_METADATA_FORMAT_DOESNT_EXIST));
            assert(messageKeys.containsKey(MSG_KEY_DISCIPLINE_DOESNT_EXIST));
        }  catch (AssertionError e){
          throw new RuntimeException("Missing required message key!  One of " +
            MSG_KEY_METADATA_FORMAT_DOESNT_EXIST + ", " +
            MSG_KEY_DISCIPLINE_DOESNT_EXIST + " is missing.");
        }
    }

    ///////////////////////////////
    //
    // Accessors
    //
    ///////////////////////////////

    private MetadataFile loadmetadataFile(FileBean uploadedFile) throws IOException {
        MetadataFile metadataFile = null;
        
        try {
            File tmp = File.createTempFile("metadatafile", null);
            tmp.deleteOnExit();
            uploadedFile.save(tmp);
            metadataFile = new MetadataFile();
            metadataFile.setId(idService.create(Types.DATA_FILE.name()).getUrl().toString());
            metadataFile.setSource(tmp.toURI().toURL().toExternalForm());
            metadataFile.setName(uploadedFile.getFileName());
            metadataFile.setMetadataFormatId(metadataFormatId);
            
            Resource r = new UrlResource(metadataFile.getSource());
            metadataFile.setSize(r.contentLength());
            metadataFile.setFormat(fileBizService.getMimeType(r.getFile()));
        }
        catch (IOException e) {
            throw e;
        }
        return metadataFile;
    }
    
    public String getMetadataFormatName() {
        return metadataFormatName;
    }
    
    public void setMetadataFormatName(String metadataFormatName) {
        this.metadataFormatName = metadataFormatName;
    }
    
    public String getMetadataFormatId() {
        return metadataFormatId;
    }

    public void setMetadataFormatId(String metadataFormatId) {
        this.metadataFormatId = metadataFormatId;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setRedirectUrl(String redirect) {
        this.redirectUrl = redirect;
    }
    
    // /////////////////////////////
    //
    // Stripes Resolutions
    //
    // /////////////////////////////
    
    public Resolution done() {
        return new ForwardResolution(UiConfigurationActionBean.class, getRedirectUrl());
    }
    
    public Resolution validate() throws IOException, SAXNotRecognizedException, SAXNotSupportedException, BizInternalException {
        MetadataFile mf = loadmetadataFile(sampleMetadataFile);
        
        Set<RegistryEntry<DcsMetadataFormat>> format = formatRegistry.lookup(metadataFormatId);
        MetadataResult mr = null;
        if (!format.isEmpty()) {
            mr = metadataBizService.validateMetadata(mf);
        } else {
            mr = metadataBizService.validateMetadata(mf, getMetadataFormat());
        }
        
        if (mr != null) {
            List<Message> errorMessages = getContext().getMessages("UserInputMessages");
            List<Message> successMessages = getContext().getMessages("Success");
            Set<MetadataEventMessage> messages = mr.getMetadataExtractionErrors();
            messages = mr.getMetadataValidationErrors();
            for (MetadataEventMessage m : messages) {
                if (m.getMessage().length() > 0) {
                    errorMessages.add(new SimpleMessage(m.getMessage()));
                }
                getContext().getResponse().setStatus(400, "Validation failed: " + m.getMessage());
            }
            messages = mr.getMetadataValidationSuccesses();
            for (MetadataEventMessage m : messages) {
                if (m.getMessage().length() > 0) {
                    successMessages.add(new SimpleMessage(m.getMessage()));
                }
            }
        }
        ForwardResolution fr = new ForwardResolution(VALIDATING_METADATA_FILE_PATH);
        fr.addParameter("metadataFormatName", metadataFormatName);
        return fr;
    }

    ///////////////////////////////
    //
    // Stripes injected services
    //
    ///////////////////////////////

    @SpringBean("uiIdService")
    private void injectIdService(IdService idService) {
        this.idService = idService;
    }
    
    @SpringBean("fileBizService")
    public void injectFileBizService(FileBizService fileBizService) {
        this.fileBizService = fileBizService;
    }
    
    @SpringBean("metadataBizService")
    private void injectMetadataBizService(MetadataBizService metadataBizService) {
        this.metadataBizService = metadataBizService;
    }
    
    
    @SpringBean("formatRegistryImpl")
    private void injectFormatRegistryImpl(TypedRegistry<DcsMetadataFormat> formatRegistry) {
        this.formatRegistry = formatRegistry;
    }
}
