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

import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.dataconservancy.ui.model.MetadataFormatProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.ui.dao.DisciplineDAO;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.UiConfigurationUpdateException;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.model.MetadataResult;
import org.dataconservancy.ui.services.MetadataBizService;
import org.dataconservancy.ui.services.MetadataFormatService;
import org.dataconservancy.ui.services.RelationshipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.util.*;

/**
 * Responsible for directing an Administrator's access to various aspects of the UI configuration.
 */
@UrlBinding("/admin/uiconfig.action")
public class UiConfigurationActionBean extends BaseActionBean {

    /**
     * The JSP which displays the Overall UI configuration page.  The configuration "home page" so to speak
     */
    static final String DISPLAY_CONFIGURATION_PAGE = "/pages/admindisplayconfiguration.jsp";

    /**
     * The JSP which display the supported Collection Metadata schemes
     */
    static final String DISPLAY_COLLECTION_METADATA_FORMATS = "/pages/admindisplaycollectionmetadataformats.jsp";

    /**
     * The JSP which supports the editing of Discipline to MetadataFormat mappings
     */
    static final String EDIT_DISCIPLINE_TO_COLLECTION_METADATA_FORMAT = "/pages/admineditdisciplinetomdf.jsp";
    
    /**
     * The JSP which shows the results of the metadata schema validation
     */
    static final String SCHEMA_VALIDATION_RESULT = "/pages/schema_validation_result.jsp";

    /**
     * The JSP which supports the testing of metadata sample files against the schema
     */
    static final String VALIDATING_METADATA_FILE_PATH = "/pages/adminvalidatingmetadatafile.jsp";

    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Consulted when enumerating the UI's supported Metadata Formats, and persisting new Metadata Formats
     */
    MetadataFormatService metadataFormatService;

    /**
     * Consulted when determining the Disciplines that a Metadata Format belongs to
     */
    RelationshipService relationshipService;
    
    /**
     * Consulted to validate a metadata schema when it's added.
     */
    private MetadataBizService metadataBizService;
    
    /**
     * SaxParserFactory used to produce a SaxParser to parse schema files to retrieve included schemas.
     */
    private SAXParserFactory saxFactory;
    
    /**
     * The result from the validation process.
     */
    private MetadataResult schemaValidationResult;
    
    /**
     * Used to relay information about the metadata format on the validation screen
     */
    private MetadataFormatDescriptor metadataFormatDescription;
    
    /**
     * The metadata format object that will be saved if the user is satisfied with the validation result.
     */
    private DcsMetadataFormat metadataFormat;
    
    /**
     * SaxParser user to parse schema files to retrieve included schemas.
     */
    private SAXParser saxParser;
    
    /**
     * Consulted to retrieve Discipline objects
     */
    DisciplineDAO disciplineDao;

    private String metadataFormatId;

    private static final String METADATAFORMAT_LIST_SESSION_KEY = "metadataFormatList";
    private static final String DISCIPLINE_LIST_SESSION_KEY = "disciplineList";
    private static final String METADATA_FORMAT_SESSION_KEY = "metadataFormat";
    private static final String METADATA_FORMAT_DESCRIPTION_SESSION_KEY = "metadataFormatDescription";
    private static final String METADATA_FORMAT_TRANSPORT_SESSION_KEY = "metadataFormatTransport";
    private static final String SCHEMA_VALIDATION_RESULT_SESSION_KEY = "schemaValidationResult";
    
    public MetaDataFormatTransport newMetadataFormatTransport;
    public List<MetaDataFormatTransport> metaDataFormatTransportList;

    public static  List<BooleanOption> booleanOptions = new ArrayList<BooleanOption>();
    public static final String NOT_SPECIFIED = "Not Specified";
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String MASK = YES + "|" + NO;
    
    protected FileBean sampleMetadataFile;

    public UiConfigurationActionBean() {
        saxFactory = SAXParserFactory.newInstance();
        try {
            saxParser = saxFactory.newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Unable to obtain a SAXParser from the SAXParserFactory: " + e.getMessage(), e);
        }
    }
    
    ///////////////////////////////
    //
    // Methods accessed by the JSPs
    //
    ///////////////////////////////

    /**
     * The list of Metadata Formats supported by this UI instance
     *
     * @return the List of MetadataFormat objects
     */
    public List<DcsMetadataFormat> getMetadataFormats() {
        Set<DcsMetadataFormat> formatSet = metadataFormatService.getMetadataFormats();
        List<DcsMetadataFormat> formatList = new ArrayList<DcsMetadataFormat>();
        formatList.addAll(formatSet);
        return formatList;
    }

    public List<String> getAllDisciplinesString() {
        List<Discipline> allDisciplines = disciplineDao.list();
        List<String> disciplines = new ArrayList<String>();
        for (Discipline disc : allDisciplines) {
            disciplines.add(disc.getTitle()+ "/" + disc.getId());
        }
        return disciplines;
    }

    public List<Discipline> getAllDisciplines() {
        List<Discipline> allDisciplines = disciplineDao.list();
        return allDisciplines;
    }

    public List<BooleanOption> getBooleanOptions() {

        if(booleanOptions.isEmpty()){
            booleanOptions.add(new BooleanOption(NOT_SPECIFIED, "--"));
            booleanOptions.add(new BooleanOption(YES, YES));
            booleanOptions.add(new BooleanOption(NO, NO));
        }
        return booleanOptions;
    }

    /**
     * Returns all the metadata formats in the system, keyed by metadata format identifier.
     * 
     * @return results
     */
    public Map<String, MetadataFormatProperties> getMetadataFormatProperties() {
        Map<String, MetadataFormatProperties> results = new HashMap<String, MetadataFormatProperties>();

        for (DcsMetadataFormat f : metadataFormatService.getMetadataFormats()) {
            results.put(f.getId(), metadataFormatService.getProperties(f.getId()));
        }

        return results;
    }

    /**
     * Return a Map of Discipline <em>objects</em> keyed by MetadataFormat identifier.
     *
     * @return a List of Disciplines keyed by MetadataFormat identifier
     */
    public Map<String, List<Discipline>> getDisciplinesForMetadataFormat() {
        HttpSession ses = getContext().getRequest().getSession();

        Map<String, List<Discipline>>   disciplinesForMetadataFormat = (Map<String, List<Discipline>> ) ses
                .getAttribute(DISCIPLINE_LIST_SESSION_KEY);

        if (disciplinesForMetadataFormat == null) {
            disciplinesForMetadataFormat = getDisciplineIdsForMetadataFormatInternal();
            ses.setAttribute(DISCIPLINE_LIST_SESSION_KEY, disciplinesForMetadataFormat);
        }

        return disciplinesForMetadataFormat;
    }

    /**
     * Return a Map of Discipline <em>identifiers</em> keyed by MetadataFormat identifier.
     *
     * @return a List of Disciplines keyed by MetadataFormat identifier
     */
    public Map<String, List<String>> getDisciplineIdsForMetadataFormat() {
        Map<String, List<String>> results = new HashMap<String, List<String>>();

        HttpSession ses = getContext().getRequest().getSession();

        Map<String, List<Discipline>>   disciplinesForMetadataFormat = (Map<String, List<Discipline>> ) ses
                .getAttribute(DISCIPLINE_LIST_SESSION_KEY);

        if (disciplinesForMetadataFormat == null) {
            disciplinesForMetadataFormat = getDisciplineIdsForMetadataFormatInternal();
            ses.setAttribute(DISCIPLINE_LIST_SESSION_KEY, disciplinesForMetadataFormat);
        }

        for (Map.Entry<String, List<Discipline>> e : disciplinesForMetadataFormat.entrySet()) {
            List<String> discipineIds;
            if (!results.containsKey(e.getKey())) {
                discipineIds = new ArrayList<String>();
                results.put(e.getKey(), discipineIds);
            } else {
                discipineIds = results.get(e.getKey());
            }

            for (Discipline d : e.getValue()) {
                discipineIds.add(d.getId());
            }
        }

        return results;
    }

    /////////////////////////////
    //
    // custom validation method
    //
    /////////////////////////////

    @ValidationMethod(on = "addNewFormat")
    public void checkFillInBoxes(){
        ValidationErrors ve =getContext().getValidationErrors();
        if(newMetadataFormatTransport == null){
            newMetadataFormatTransport = new MetaDataFormatTransport();
        }
        if(newMetadataFormatTransport.getName() == null){
            ve.add("newMetadataFormatTransport.name", new SimpleError("Name is a required field"));
        }
        if(newMetadataFormatTransport.getVersion() == null){
            ve.add("newMetadataFormatTransport.version", new SimpleError("Version is a required field"));
        }
        if(newMetadataFormatTransport.getSchemaURL() == null){
            ve.add("newMetadataFormatTransport.schemaURL", new SimpleError("SchemaURL is a required field"));
        }  else {
            String schemaURL = newMetadataFormatTransport.getSchemaURL();
            if(!schemaURL.startsWith("http://") && !schemaURL.startsWith("https://")){
                ve.add("newMetadataFormatTransport.schemaURL", new SimpleError("SchemaURL must be a valid URL"));
            }
        }
        if(!YES.equals(newMetadataFormatTransport.getAppliesToCollection()) && !NO.equals(newMetadataFormatTransport.getAppliesToCollection()) ){
          ve.add("newMetadataFormatTransport.appliesToCollection", new SimpleError("Yes or No must be specified"));
        }
        if(!YES.equals(newMetadataFormatTransport.getAppliesToProject()) && !NO.equals(newMetadataFormatTransport.getAppliesToProject())){
            ve.add("newMetadataFormatTransport.appliesToProject", new SimpleError("Yes or No must be specified"));
        }
        if(!YES.equals(newMetadataFormatTransport.getAppliesToItem()) && !NO.equals(newMetadataFormatTransport.getAppliesToItem())){
            ve.add("newMetadataFormatTransport.appliesToItem", new SimpleError("Yes or No must be specified"));
        }
        if(!YES.equals(newMetadataFormatTransport.getValidates())&& !NO.equals(newMetadataFormatTransport.getValidates())){
            ve.add("newMetadataFormatTransport.validates", new SimpleError("Yes or No must be specified"));
        }
        if (newMetadataFormatTransport.getDisciplineIds() == null) {
            ve.add("newMetadataFormatTransport.allDisciplines", new SimpleError("Select at least one."));
        }
    }

    public MetaDataFormatTransport getNewMetadataFormatTransport() {
         if(newMetadataFormatTransport == null) {
             newMetadataFormatTransport  = new MetaDataFormatTransport();
         }
         return newMetadataFormatTransport;
    }

    public void setNewMetadataFormatTransport(MetaDataFormatTransport metadataFormatTransport) {
        this.newMetadataFormatTransport = metadataFormatTransport;
    }

    public List<MetaDataFormatTransport> generateMetaDataFormatTransportList(){
        List<MetaDataFormatTransport> metaDataFormatTransportList = new ArrayList<MetaDataFormatTransport>();

        List<DcsMetadataFormat> formatList = getMetadataFormats();
        for (DcsMetadataFormat mdf : formatList) {
            MetaDataFormatTransport dt = new MetaDataFormatTransport();
            dt.setName(mdf.getName());
            dt.setId(mdf.getId());

            dt.properties = metadataFormatService.getProperties(mdf.getId());
            //exclude deactivated properties
            if (dt.properties != null && !dt.properties.isActive()) {
                continue;
            }

            //The master schema will always be the first schema so get the first schema url.
            if (!mdf.getSchemes().isEmpty()) {
                dt.setSchemaURL(mdf.getSchemes().iterator().next().getSchemaUrl());
                dt.setSchemaSource(mdf.getSchemes().iterator().next().getSource());                
            }
            
            dt.setVersion(mdf.getVersion());

            // If the format is unknown to the MetadataFormatService#getProperties method, instantiate an empty
            // properties object with default state, and set its format id.
            if (dt.properties == null) {
                dt.properties = new MetadataFormatProperties();
                dt.properties.setFormatId(mdf.getId());
                dt.properties.setActive(true);

            }
            metaDataFormatTransportList.add(dt);
        }

        return metaDataFormatTransportList;
    }

    public List<MetaDataFormatTransport> getMetaDataFormatTransportList() {

        HttpSession ses = getContext().getRequest().getSession();

        metaDataFormatTransportList = (List<MetaDataFormatTransport>) ses
                .getAttribute(METADATAFORMAT_LIST_SESSION_KEY);

        if (metaDataFormatTransportList == null) {
            metaDataFormatTransportList = generateMetaDataFormatTransportList();
            ses.setAttribute(METADATAFORMAT_LIST_SESSION_KEY, metaDataFormatTransportList);
        }

        return metaDataFormatTransportList;
    }

    public void setMetaDataFormatTransportList(List<MetaDataFormatTransport> metaDataFormatTransportList) {
        this.metaDataFormatTransportList = metaDataFormatTransportList;
    }
    
    public DcsMetadataFormat getMetadataFormat() {
        HttpSession ses = getContext().getRequest().getSession();

        if (metadataFormat == null) {
           metadataFormat = (DcsMetadataFormat) ses.getAttribute(METADATA_FORMAT_SESSION_KEY);
        }
        
        return metadataFormat;
    }

    private MetaDataFormatTransport getMetadataFormatTransport() {
        return (MetaDataFormatTransport) getContext().getRequest().getSession().
                getAttribute(METADATA_FORMAT_TRANSPORT_SESSION_KEY);
    }
    
    public void setMetadataFormat(DcsMetadataFormat format) {
        metadataFormat = format;
        
        HttpSession ses = getContext().getRequest().getSession();
        ses.setAttribute(METADATA_FORMAT_SESSION_KEY, metadataFormat);
    }
    
    public MetadataResult getSchemaValidationResult() {
        HttpSession ses = getContext().getRequest().getSession();

        if (schemaValidationResult == null) {
           schemaValidationResult = (MetadataResult) ses.getAttribute(SCHEMA_VALIDATION_RESULT_SESSION_KEY);
        }
        
        return schemaValidationResult;
    }
    
    public void setSchemaValidationResult(MetadataResult schemaValidationResult) {
        this.schemaValidationResult = schemaValidationResult;
        
        HttpSession ses = getContext().getRequest().getSession();
        ses.setAttribute(SCHEMA_VALIDATION_RESULT_SESSION_KEY, schemaValidationResult);
    }
    
    public MetadataFormatDescriptor getMetadataFormatDescription() {
        HttpSession ses = getContext().getRequest().getSession();

        if (metadataFormatDescription == null) {
           metadataFormatDescription = (MetadataFormatDescriptor) ses.getAttribute(METADATA_FORMAT_DESCRIPTION_SESSION_KEY);
        }
        
        return metadataFormatDescription;
    }
    
    public void setMetadataFormatDescription(MetadataFormatDescriptor formatDescription) {
        this.metadataFormatDescription = formatDescription;
        
        HttpSession ses = getContext().getRequest().getSession();
        ses.setAttribute(METADATA_FORMAT_DESCRIPTION_SESSION_KEY, metadataFormatDescription);
    }

    public void setMetadataFormatTransport(MetaDataFormatTransport formatTransport) {
        HttpSession s = getContext().getRequest().getSession();
        s.setAttribute(METADATA_FORMAT_TRANSPORT_SESSION_KEY, formatTransport);
    }
    
    ///////////////////////////////
    //
    // Stripes Resolutions
    //
    ///////////////////////////////

    /**
     * Forwards to a Resolution which displays the global/overall configuration of this UI instance.
     *
     * @return the Resolution
     */
    @DefaultHandler
    public Resolution displayOverallConfiguration() {
        clearMetadataFormatTransportList();
        clearDisciplineForMetadataFormat();
        return new ForwardResolution(DISPLAY_CONFIGURATION_PAGE);
    }

    /**
     * Forwards to a Resolution which allows an Admin to enumerate all the Metadata Formats available to this UI
     * instance, and to classify the MetadataFormats to a Discipline.
     *
     * @return the Resolution
     */
    public Resolution displayMetadataFormatList() {
        clearMetadataFormatTransportList();
        clearDisciplineForMetadataFormat();
        return new ForwardResolution(DISPLAY_COLLECTION_METADATA_FORMATS);
    }

    public Resolution displayEditDisciplineMapping() {
        clearMetadataFormatTransportList();
        clearDisciplineForMetadataFormat();
        return new ForwardResolution(EDIT_DISCIPLINE_TO_COLLECTION_METADATA_FORMAT);
    }

    public Resolution cancel() {
        clearMetadataFormatTransportList();
        clearMetadataFormat();
        clearMetadataFormatTransport();
        clearMetadataDescription();
        clearDisciplineForMetadataFormat();
        return new ForwardResolution(DISPLAY_COLLECTION_METADATA_FORMATS);
    }

    public Resolution removeFormat() throws BizInternalException {
        DcsMetadataFormat format = metadataFormatService.getMetadataFormat(metadataFormatId);

        MetadataFormatProperties mfp = metadataFormatService.getProperties(metadataFormatId);

        // this happens when a DcsMetadataFormat is found in the archive, but it has no corresponding
        // business properties in the local database, for example
        if (mfp == null) {
            mfp = new MetadataFormatProperties();
            mfp.setFormatId(format.getId());
        }
        mfp.setActive(false);

        try {
            metadataFormatService.setProperties(format, mfp);
            //metadataFormatService.up
        } catch (BizInternalException e) {
            log.warn("Error removing Metadata Format record: " + e.getMessage(), e);
            throw e;
        }
        return displayMetadataFormatList();
    }

    public Resolution addBlankFormat() {
        getMetaDataFormatTransportList().add(null);

        return new ForwardResolution(DISPLAY_COLLECTION_METADATA_FORMATS);
    }
    
    public Resolution addNewFormat() throws UiConfigurationUpdateException {
        
        URL schemaUrl = null;
        try {
            schemaUrl = new URL(newMetadataFormatTransport.schemaURL);
        } catch (MalformedURLException e) {
            throw new UiConfigurationUpdateException(e.getMessage());
        }
        
        if (schemaUrl != null) {
            try {
                schemaValidationResult = metadataBizService.validateMetadataSchema(schemaUrl);
            } catch (IOException e) {
                throw new UiConfigurationUpdateException(e.getMessage());
            }
        }
        
        metadataFormat = new DcsMetadataFormat();
        metadataFormat.setName(newMetadataFormatTransport.getName());
        metadataFormat.setVersion(newMetadataFormatTransport.getVersion());
        metadataFormat.setId("dataconservancy.org:registry:metadata-format:entry:id:" + metadataFormat.getName() + ":" + metadataFormat.getVersion());
              
        if (schemaUrl != null) {
            DcsMetadataScheme baseScheme = new DcsMetadataScheme();
            baseScheme.setName(metadataFormat.getName());
            baseScheme.setSchemaVersion(metadataFormat.getVersion());
            baseScheme.setSchemaUrl(newMetadataFormatTransport.getSchemaURL());
            baseScheme.setSource(newMetadataFormatTransport.getSchemaURL());
            metadataFormatDescription = new MetadataFormatDescriptor();
            final List<DcsMetadataScheme> schemes = new ArrayList<DcsMetadataScheme>();
            schemes.add(baseScheme);

            //Only do this if the schema validation result passes doesn't make sense to do it otherwise.
            if (schemaValidationResult.getMetadataValidationErrors().isEmpty()) {
                org.xml.sax.helpers.DefaultHandler handler = new org.xml.sax.helpers.DefaultHandler() {
                    public void startElement(String uri, String localName,String qName, 
                                             Attributes attributes) throws SAXException {
                        if (qName.contains("schema")) {                           
                            for (int i = 0; i < attributes.getLength(); i++) {
                                String potentialNamespace = attributes.getQName(i);
                                if (potentialNamespace.contains("xmlns")) {
                                    if (potentialNamespace.equalsIgnoreCase("xmlns")) {
                                        metadataFormatDescription.addNamespace( new Namespace("", attributes.getValue(i)) );                                      
                                    } else {                                        
                                        int prefixStart = potentialNamespace.indexOf(':') + 1;
                                        metadataFormatDescription.addNamespace( new Namespace(potentialNamespace.substring(prefixStart), attributes.getValue(i)) );
                                    }
                                }
                            }
                        }
                    }
                };        
                try {
                    InputStream xsdInputStream = schemaUrl.openStream();
                    saxParser.parse(xsdInputStream, handler);
                } catch (IOException e) {
                    throw new UiConfigurationUpdateException(e.getMessage());
                } catch (SAXException e) {
                    throw new UiConfigurationUpdateException(e.getMessage());
                }
            }
            metadataFormat.setSchemes(schemes);
        }        
        //Display the result format     
        metadataFormatDescription.format = metadataFormat;
        
        //Add the metadata format to the session
        setMetadataFormat(metadataFormat);
        setMetadataFormatDescription(metadataFormatDescription);
        setMetadataFormatTransport(newMetadataFormatTransport);
        setSchemaValidationResult(schemaValidationResult);
        if (validationFailed(schemaValidationResult)) {
            StringBuilder sb = new StringBuilder();
            for (MetadataResult.MetadataEventMessage m : schemaValidationResult.getMetadataValidationErrors()) {
                sb.append(m.getMessage() + "\n");
            }

            getContext().getResponse().setStatus(400, "Validation failed: " + sb.toString());
        }
        return displaySchemaValidationResults();
    }
    
    //This is broken out into a seperate resolution so the sample file validation can return to it without re validating the file.
    public Resolution displaySchemaValidationResults() {
        return new ForwardResolution(SCHEMA_VALIDATION_RESULT);
    }

    public Resolution saveNewFormat() throws BizInternalException {
        if (getMetadataFormat() != null) {
            try {
                final DcsMetadataFormat mdf = getMetadataFormat();
                metadataFormatService.addMetadataFormat(mdf);
                getMetadataFormatTransport().properties.setFormatId(mdf.getId());
                getMetadataFormatTransport().properties.setActive(true);
                metadataFormatService.setProperties(mdf, getMetadataFormatTransport().properties);
            } catch (BizInternalException e) {
                log.warn("Error saving new Metadata Format: " + e.getMessage(), e);
                throw e;
            }
        }
        
        //Remove the temporary format from the session
        clearMetadataFormat();
        clearSchemaValidationResult();
        clearMetadataDescription();
        clearMetadataFormatTransport();
        
        // reload the list after save
        return displayMetadataFormatList();        
    }

    public Resolution validatingMetadataFile() {
        return new ForwardResolution(VALIDATING_METADATA_FILE_PATH);
    }

    ///////////////////////////////
    //
    // Inner classes
    //
    ///////////////////////////////
    public class BooleanOption {
        private String value;

        private String name;

        private Boolean booleanValue;

        public BooleanOption(String value, String name) {
            this.value = value;
            this.name = name;
            if (YES.equals(value)) {
                booleanValue = Boolean.TRUE;
            }

            if (NO.equals(value)) {
                booleanValue = Boolean.FALSE;
            }
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(Boolean booleanValue) {
            this.booleanValue = booleanValue;
        }
    }

    public class MetaDataFormatTransport {
        private String name;
        private String version;
        private String id;


        private String schemaURL;

        private String schemaSource;

        private MetadataFormatProperties properties = new MetadataFormatProperties();

        @Override
        public String toString() {
            return "MetaDataFormatTransport{" +
                    "name='" + name + '\'' +
                    ", version='" + version + '\'' +
                    ", id='" + id + '\'' +
                    ", schemaURL='" + schemaURL + '\'' +
                    ", properties='" + properties.toString() + '\'' +
                    '}';
        }

        public String getSchemaURL() {
            return schemaURL;
        }

        public void setSchemaURL(String schemaURL) {
            this.schemaURL = schemaURL;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAppliesToProject() {
            if (properties.isAppliesToProject()) {
                return YES;
            }

            return NO;
        }

        public void setAppliesToProject(String appliesToProject) {
            if (YES.equals(appliesToProject)) {
                properties.setAppliesToProject(true);
            } else {
                properties.setAppliesToProject(false);
            }
        }

        public void setAppliesToProject(boolean appliesToProject) {
            properties.setAppliesToProject(appliesToProject);
        }
        
        public String getAppliesToCollection() {
            if (properties.isAppliesToCollection()) {
                return YES;
            }

            return NO;
        }

        public void setAppliesToCollection(String appliesToCollection) {
            if (YES.equals(appliesToCollection)) {
                properties.setAppliesToCollection(true);
            } else {
                properties.setAppliesToCollection(false);
            }
        }

        public void setAppliesToCollection(boolean appliesToCollection) {
            properties.setAppliesToCollection(appliesToCollection);
        }
        
        public String getAppliesToItem() {
            if (properties.isAppliesToItem()) {
                return YES;
            }

            return NO;
        }

        public void setAppliesToItem(String appliesToItem) {
            if (YES.equals(appliesToItem)) {
                properties.setAppliesToItem(true);
            } else {
                properties.setAppliesToItem(false);
            }
        }

        public void setAppliesToItem(boolean appliesToItem) {
            properties.setAppliesToItem(appliesToItem);
        }

        public List<String> getDisciplineIds() {
            return properties.getDisciplineIds();
        }

        public void setDisciplineIds(List<String> disciplineIds) {
            properties.setDisciplineIds(disciplineIds);
        }

        public String getValidates() {
            if (properties.isValidates()) {
                return YES;
            }

            return NO;
        }
        
        public void setValidates(String validates) {
            if (YES.equals(validates)) {
                properties.setValidates(true);
            } else {
                properties.setValidates(false);
            }
        }

        public void setValidates(boolean validates) {
            properties.setValidates(validates);
        }

        public String getSchemaSource() {
            return schemaSource;
        }

        public void setSchemaSource(String schemaSource) {
            this.schemaSource = schemaSource;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MetaDataFormatTransport that = (MetaDataFormatTransport) o;

            if (properties != null ? !properties.equals(that.properties) : that.properties != null)
                return false;
            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (schemaSource != null ? !schemaSource.equals(that.schemaSource) : that.schemaSource != null)
                return false;
            if (schemaURL != null ? !schemaURL.equals(that.schemaURL) : that.schemaURL != null) return false;
            if (version != null ? !version.equals(that.version) : that.version != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (version != null ? version.hashCode() : 0);
            result = 31 * result + (id != null ? id.hashCode() : 0);
            result = 31 * result + (schemaURL != null ? schemaURL.hashCode() : 0);
            result = 31 * result + (schemaSource != null ? schemaSource.hashCode() : 0);
            result = 31 * result + (properties != null ? properties.hashCode() : 0);
            return result;
        }
    }
    
    public class MetadataFormatDescriptor {
        private DcsMetadataFormat format;
        private List<Namespace> namespaces;
        
        public MetadataFormatDescriptor() {
            format = new DcsMetadataFormat();
            namespaces = new ArrayList<Namespace>();
        }
        
        public MetadataFormatDescriptor(DcsMetadataFormat format, List<Namespace> namespaces) {
            this.format = format;
            this.namespaces = namespaces;
        }
        
        public String getName() {
            return format.getName();
        }
        
        public void setFormat(DcsMetadataFormat format) {
            this.format = format;
        }
        
        public String getVersion() {
            return format.getVersion();
        }
        
        public List<Namespace> getNamespaces() {
            return namespaces;
        }
        
        public void setNamespaces(List<Namespace> namespaces) {
            this.namespaces = namespaces;
        }
        
        public void addNamespace(Namespace namespace) {
            namespaces.add(namespace);
        }
        
        public String getId() {
            return format.getId();
        }       
    }
    
    public class Namespace {
        private String prefix;
        private String name;
        
        public Namespace(String prefix, String name) {
            this.prefix = prefix;
            this.name = name;
        }
        
        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
        
        public String getPrefix() {
            return prefix;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }

    public FileBean getSampleMetadataFile() {
        return sampleMetadataFile;
    }
    
    public void setSampleMetadataFile(FileBean uploadedFile) {
        this.sampleMetadataFile = uploadedFile;
    }


    public String getMetadataFormatId() {
        return metadataFormatId;
    }

    public void setMetadataFormatId(String metadataFormatId) {
        this.metadataFormatId = metadataFormatId;
    }


    ///////////////////////////////
    //
    // Stripes injected services
    //
    ///////////////////////////////

    /**
     * Stripes-injected MetadataFormatService
     *
     * @param metadataFormatService the service
     */
    @SpringBean("metadataFormatService")
    private void injectMetadataService(MetadataFormatService metadataFormatService) {
        this.metadataFormatService = metadataFormatService;
    }

    /**
     * Stripes-injected RelationshipService
     *
     * @param relationshipService the service
     */
    @SpringBean("relationshipService")
    private void injectRelationshipService(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @SpringBean("disciplineDao")
    private void injectDisciplineDao(DisciplineDAO disciplineDao) {
        this.disciplineDao = disciplineDao;
    }

    @SpringBean("metadataBizService")
    private void injectMetadataBizService(MetadataBizService metadataBizService) {
        this.metadataBizService = metadataBizService;
    }

    ///////////////////////////////
    //
    // Supporting methods
    //
    ///////////////////////////////

    private Map<String, List<Discipline>> getDisciplineIdsForMetadataFormatInternal() {
        Map<String, List<Discipline>> results = new HashMap<String, List<Discipline>>();

        List<Discipline> allDisciplines = disciplineDao.list();
        Set<DcsMetadataFormat> allMetadataFormats = metadataFormatService.getMetadataFormats();

        for (DcsMetadataFormat mdf : allMetadataFormats) {
            List<Discipline> disciplinesForMdf;
            if (!results.containsKey(mdf.getId())) {
                disciplinesForMdf = new ArrayList<Discipline>();
                results.put(mdf.getId(), disciplinesForMdf);
            } else {
                disciplinesForMdf = results.get(mdf.getId());
            }

            for (String disciplineId : relationshipService.getDisciplinesForMetadataFormats(mdf.getId())) {
                for (Discipline d : allDisciplines) {
                    if (d.getId().equals(disciplineId)) {
                        disciplinesForMdf.add(d);
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder("Disciplines for MetadataFormats: ");
        for (Map.Entry<String, List<Discipline>> entry : results.entrySet()) {
            sb.append(entry.getKey()).append(": [");
            for (Discipline d : entry.getValue()) {
                sb.append(d.getId()).append(" (").append(d.getTitle()).append(")");
            }
            sb.append("] ");
        }
        log.debug(sb.toString());
        return results;
    }


    /**
     * Removes the MetadataFormatTransportList object from the HTTP session.
     */
    private void clearMetadataFormatTransportList() {
        HttpSession ses = getContext().getRequest().getSession();
        ses.removeAttribute(METADATAFORMAT_LIST_SESSION_KEY);
    }

    private void clearDisciplineForMetadataFormat() {
        getContext().getRequest().getSession().removeAttribute(DISCIPLINE_LIST_SESSION_KEY);
    }

    private void clearMetadataFormat() {
        HttpSession ses = getContext().getRequest().getSession();
        ses.removeAttribute(METADATA_FORMAT_SESSION_KEY);
    }
    
    private void clearSchemaValidationResult() {
        HttpSession ses = getContext().getRequest().getSession();
        ses.removeAttribute(SCHEMA_VALIDATION_RESULT_SESSION_KEY);
    }
    
    private void clearMetadataDescription() {
        HttpSession ses = getContext().getRequest().getSession();
        ses.removeAttribute(METADATA_FORMAT_DESCRIPTION_SESSION_KEY);
    }

    private void clearMetadataFormatTransport() {
        getContext().getRequest().getSession().removeAttribute(METADATA_FORMAT_TRANSPORT_SESSION_KEY);
    }

    private boolean validationFailed(MetadataResult result) {
        return result.getMetadataValidationErrors().size() > 0;
    }
}