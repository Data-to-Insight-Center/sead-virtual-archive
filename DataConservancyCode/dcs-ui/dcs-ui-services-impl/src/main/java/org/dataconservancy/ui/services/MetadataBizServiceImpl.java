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

import org.dataconservancy.mhf.eventing.events.MetadataExtractionEvent;
import org.dataconservancy.mhf.eventing.events.MetadataHandlingEvent;
import org.dataconservancy.mhf.eventing.events.MetadataValidationEvent;
import org.dataconservancy.mhf.eventing.events.MetadataXmlParsingEvent;
import org.dataconservancy.mhf.eventing.events.MetadataXmlValidationEvent;
import org.dataconservancy.mhf.eventing.listener.MetadataHandlingEventListener;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instances.FileMetadataInstance;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.services.MetadataHandlingService;
import org.dataconservancy.mhf.services.MetadataValidationService;
import org.dataconservancy.mhf.validators.CapturingSaxErrorHandler;
import org.dataconservancy.mhf.validators.DebuggingErrorHandler;
import org.dataconservancy.mhf.validators.DefaultSaxErrorHandler;
import org.dataconservancy.mhf.validators.EventProducingErrorHandler;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.MetadataResult;
import org.dataconservancy.ui.model.MetadataResult.MetadataEventMessage;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implementation of MetadataBizService.
 *
 */
public class MetadataBizServiceImpl implements MetadataBizService, MetadataHandlingEventListener {

    private MetadataHandlingService metadataHandlingService;
    private MetadataValidationService metadataValidationService;
    
    private MetadataResult result;
    
    public MetadataBizServiceImpl(MetadataHandlingService metadataHandlingService) {
        this.metadataHandlingService = metadataHandlingService;
    }
    
    @Override
    public void index(String businessId, Collection<AttributeSet> metadataAttributeSets) {

        //TODO: transform metadata attribute set to appropriate input for archive indexing service
        //invoke indexing service
    }

    @Override
    public MetadataResult validateAndExtractMetadata(BusinessObject businessObject,
                                           String parentId) {
        
        result = new MetadataResult();
        
        MetadataHandlingEventManager.getInstance().registerListener(this);
        Set<AttributeSet> attributeSets = metadataHandlingService.validateAndExtractMetadata(businessObject, parentId, null);
        result.setAttributeSets(attributeSets);
        Collection<BusinessObject> childBusinessObjects = getChildBusinessObjects(businessObject);
        for (BusinessObject childBusinessObject : childBusinessObjects) {
            attributeSets = metadataHandlingService.validateAndExtractMetadata(childBusinessObject, businessObject.getId(), null);
            result.getAttributeSets().addAll(attributeSets);
        }
        MetadataHandlingEventManager.getInstance().unRegisterListener(this);

        return result;
    }

    @Override
    public MetadataResult extractMetadata(BusinessObject businessObject) {

        result = new MetadataResult();

        MetadataHandlingEventManager.getInstance().registerListener(this);
        Set<AttributeSet> attributeSets = metadataHandlingService.extractMetadata(businessObject);
        result.setAttributeSets(attributeSets);
        Collection<BusinessObject> childBusinessObjects = getChildBusinessObjects(businessObject);
        for (BusinessObject childBusinessObject : childBusinessObjects) {
            attributeSets = metadataHandlingService.extractMetadata(childBusinessObject);
            result.getAttributeSets().addAll(attributeSets);
        }
        MetadataHandlingEventManager.getInstance().unRegisterListener(this);

        return result;
    }

    @Override
    public void onMetadataFileHandlingEvent(MetadataHandlingEvent event) {

        if (event instanceof MetadataValidationEvent) {
            MetadataValidationEvent validationEvent = (MetadataValidationEvent) event;
            if (validationEvent.getType().equals(MetadataValidationEvent.ValidationType.FAILURE)) {
                MetadataEventMessage eventMessage = result.new MetadataEventMessage(validationEvent.getMessage(), validationEvent.getValidationFailure());
                result.addMetadataValidationError(eventMessage);
                result.setValidationPerformed(true);
            } else if (validationEvent.getType().equals(MetadataValidationEvent.ValidationType.NOOP)) {
                MetadataEventMessage eventMessage = result.new MetadataEventMessage(validationEvent.getMessage(), validationEvent.getValidationFailure());
                result.addMetadataValidationWarning(eventMessage);
            } else {
                MetadataEventMessage eventMessage = result.new MetadataEventMessage(validationEvent.getMessage(), "");
                result.addMetadataValidationSuccess(eventMessage);
                result.setValidationPerformed(true);
            }
                
        } else if (event instanceof MetadataExtractionEvent) {
            MetadataExtractionEvent extractionEvent = (MetadataExtractionEvent) event;
            if (extractionEvent.getType().equals(MetadataExtractionEvent.ExtractionEventType.ERROR)) {
                MetadataEventMessage eventMessage = result.new MetadataEventMessage(extractionEvent.getMessage(), "");
                result.addMetadataExtractionError(eventMessage);
            } else {
                MetadataEventMessage eventMessage = result.new MetadataEventMessage(extractionEvent.getMessage(), "");
                result.addMetadataExtractionSuccess(eventMessage);
            }
        } else if (event instanceof MetadataXmlParsingEvent) {
            MetadataXmlParsingEvent parsingEvent = (MetadataXmlParsingEvent) event;
            if (parsingEvent.getSeverity() == MetadataXmlParsingEvent.SEVERITY.ERROR || parsingEvent.getSeverity() == MetadataXmlParsingEvent.SEVERITY.FATAL) {
                MetadataEventMessage eventMessage = result.new MetadataEventMessage(parsingEvent.getMessage(), parsingEvent.toString());
                result.addMetadataValidationError(eventMessage);
                result.setValidationPerformed(true);
            } else if (parsingEvent.getSeverity() == MetadataXmlParsingEvent.SEVERITY.WARN) {
                MetadataEventMessage eventMessage = result.new MetadataEventMessage(parsingEvent.getMessage(), parsingEvent.toString());
                result.addMetadataValidationWarning(eventMessage);
                result.setValidationPerformed(true);
            }
        }
        
    }

    private Collection<BusinessObject> getChildBusinessObjects(BusinessObject businessObject) {
        Collection<BusinessObject> decomposedBusinessObjects = new ArrayList<BusinessObject>();
        if (businessObject instanceof DataItem) {
            DataItem dataItem = (DataItem)businessObject;
            for (DataFile file : dataItem.getFiles()) {
                decomposedBusinessObjects.add(file);
            }
        }
        return decomposedBusinessObjects;
    }

    @Override
    public MetadataResult validateMetadataSchema(URL schemaUrl) throws IOException {
        result = new MetadataResult();
        MetadataHandlingEventManager.getInstance().registerListener(this);
        
        FileMetadataInstance fileInstance = new FileMetadataInstance(MetadataFormatId.XSD_XML_FORMAT_ID, schemaUrl);

        final String baseUrl = schemaUrl.toString().substring(0, schemaUrl.toString().lastIndexOf("/")+1);
        metadataValidationService.validate("", fileInstance, new URL(baseUrl));
        
        MetadataHandlingEventManager.getInstance().unRegisterListener(this);
        return result;
    }
    
    public void setMetadataValidationService(MetadataValidationService validationService) {
        this.metadataValidationService = validationService;
    }
    
    @Override
    public MetadataResult validateMetadata(BusinessObject businessObject) {
        result = new MetadataResult();

        MetadataHandlingEventManager.getInstance().registerListener(this);
        metadataHandlingService.validateMetadata(businessObject, null);
        Collection<BusinessObject> childBusinessObjects = getChildBusinessObjects(businessObject);
        for (BusinessObject childBusinessObject : childBusinessObjects) {
            metadataHandlingService.validateMetadata(childBusinessObject, null);
        }
        MetadataHandlingEventManager.getInstance().unRegisterListener(this);
        
        return result;
    }
    
    @Override
    public MetadataResult validateMetadata(MetadataFile metadataFile, DcsMetadataFormat metadataFormat) throws BizInternalException {
        result = new MetadataResult();
        MetadataHandlingEventManager eventManager = MetadataHandlingEventManager.getInstance();
        eventManager.registerListener(this);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (!metadataFormat.getSchemes().isEmpty()) {
            DcsMetadataScheme scheme = metadataFormat.getSchemes().iterator().next();
            URL schemaUrl = null;
            try {
                schemaUrl = new URL(scheme.getSchemaUrl());
            } catch (MalformedURLException e) {
                throw new BizInternalException(e);
            }
            
            if (schemaUrl != null) {
                Schema schema = null;
                try {
                    schema = schemaFactory.newSchema(schemaUrl);
                } catch (SAXException e) {
                    throw new BizInternalException(e);
                }     
                
                if (schema != null) {
                    RegistryEntry<DcsMetadataFormat> entry = new BasicRegistryEntryImpl<DcsMetadataFormat>(metadataFormat.getId(), metadataFormat, "org.dataconservancy:registry:metadataformat", Arrays.asList("org.dataconservancy:registry:metadataformat"), metadataFormat.getName());

                    final CapturingSaxErrorHandler capturingErrorHandler = new CapturingSaxErrorHandler();
                    final ErrorHandler eh = new DebuggingErrorHandler(
                              new EventProducingErrorHandler(capturingErrorHandler, entry, eventManager));
                    Validator validator = schema.newValidator();
                    validator.setErrorHandler(eh);
                    
                    try {
                        validator.validate(new SAXSource(new InputSource(new URL(metadataFile.getSource()).openStream())));
                    } catch (MalformedURLException e) {
                        throw new BizInternalException(e);
                    } catch (SAXException e) {
                        // TODO Add to log the error handler should produce an event for this
                    } catch (IOException e) {
                      throw new BizInternalException(e);
                    }
                    
                    if (result.getMetadataValidationErrors().isEmpty()) {
                        final String message = String.format("Validation succeeded: validation of an XML instance document with format id %s succeeded against the " +
                                "schema %s obtained from %s", metadataFormat.getId(),
                                 scheme.getSchemaUrl(), scheme.getSource());
                        MetadataXmlValidationEvent mve = new MetadataXmlValidationEvent(null,
                                message, null, MetadataValidationEvent.ValidationType.PASS);
                        mve.setSchemaSource(scheme.getSource());
                        mve.setSchemaUrl(scheme.getSchemaUrl());
                        
                        MetadataEventMessage eventMessage = result.new MetadataEventMessage(message, "");
                        result.addMetadataValidationSuccess(eventMessage);
                    }
                }
            }            
        }
        
        eventManager.unRegisterListener(this);
        return result;
    }
}
