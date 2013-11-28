/*
 * Copyright 2013 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.dataconservancy.ui.stripes;

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_READING_FILE;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_HOW_TO_GET_INGEST_STATUS;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_INGEST_SUBMITTED;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.FlashScope;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.Validate;

import org.apache.tika.io.IOUtils;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.PackageException;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.packaging.ingest.api.Cancelable;
import org.dataconservancy.packaging.ingest.api.Http;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.ResumableDepositManager;
import org.dataconservancy.reporting.model.IngestReport;
import org.dataconservancy.ui.exceptions.IngestException;
import org.dataconservancy.ui.services.IngestReportService;
import org.dataconservancy.ui.util.UiBaseUrlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActionBean for the ingest_package view.
 */
@UrlBinding("/ingest/ingest_package.action")
public class IngestPackageActionBean extends BaseActionBean {

    private static final String PRE_INGEST_REPORT = "pre-ingest-report";

    private static final String DOCUMENT_TYPE = "document-type";

    static final String RESUME_EVENT = "resume";

    static final String INGEST_EVENT = "ingest";

    static final String STATUS_EVENT = "status";

    static final String CANCEL_EVENT = "cancel";

    private final static String INGEST_PACKAGE_PATH = "/pages/ingest_package.jsp";
    private final static String INGEST_STATUS_PATH = "/pages/ingest_status.jsp";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Validate(required = true, on = { "ingest" })
    private FileBean uploadedFile;

    private ResumableDepositManager depositManager;
    
    private UiBaseUrlConfig uiBaseUrlConfig;
    
    @Validate(required = true, on = { "status" })
    private String depositId;
    
    private boolean pause;
    private boolean phaseComplete;
    private boolean cancelFlag;
    
    /**
     * Spring wired IngestReportService
     */
    private IngestReportService ingestReportService;
    
    public IngestPackageActionBean() {
        super();
        
        try {
            assert (messageKeys.containsKey(MSG_KEY_INGEST_SUBMITTED));
            assert (messageKeys.containsKey(MSG_KEY_HOW_TO_GET_INGEST_STATUS));
        }
        catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of " + MSG_KEY_INGEST_SUBMITTED
                    + ", " + MSG_KEY_HOW_TO_GET_INGEST_STATUS + " is missing.");
        }
    }
    
    @DefaultHandler
    public Resolution render() {
        return new ForwardResolution(INGEST_PACKAGE_PATH);
    }
    
    public Resolution ingest() throws IngestException, PackageException {
        final String currentUserId = (getAuthenticatedUser() != null) ? getAuthenticatedUser().getId() : "Anonymous";
        InputStream inputStream = null;
        try {
            inputStream = loadDataFile(uploadedFile);

            // Getting the HTTP header from the request and generating the metadata map.
            Enumeration<?> e = getContext().getRequest().getHeaderNames();
            Map<String, String> metadata = new HashMap<String, String>();
            while (e.hasMoreElements()) {
                String header = (String) e.nextElement();
                if (header != null) {
                    metadata.put(header, getContext().getRequest().getHeader(header));
                }
            }

            // Adding the uploaded filename as Content-Disposition header to the map.
            metadata.put(Http.Header.CONTENT_DISPOSITION, "attachment; filename=\"" + uploadedFile.getFileName() + "\"");
            metadata.put(Http.Header.X_DCS_AUTHENTICATED_USER, getAuthenticatedUser().getId());
            DepositInfo depositInfo = depositManager.deposit(inputStream, getContext().getResponse().getContentType(),
                    Package.Types.BAGIT_DCS_10, metadata);
            setDepositId(depositInfo.getDepositID());

            return redirectToIngestStatus();
        }
        catch (IOException e) {
            IngestException ingestException = new IngestException(messageKeys.getProperty(MSG_KEY_ERROR_READING_FILE),
                    e);
            ingestException.setUploadedFile(uploadedFile);
            ingestException.setHttpStatusCode(500);
            ingestException.setUserId(currentUserId);
            throw ingestException;
        } finally {
            try {          
                if (inputStream != null) {
                    inputStream.close();
                }
                // DC-1449: No need to keep the uploaded file once its InputStream has been read.  Stripes places these
                // files in temporary storage, so a server reboot will probably clear them out if the deletion fails.
                uploadedFile.delete();
            } catch (IOException e) {
                log.warn("Unable to delete uploaded file '" + uploadedFile.getFileName() + "': " + e.getMessage(), e);
            }
        }
    }

    public Resolution resume() {
        depositManager.resume(getDepositId());
        return redirectToIngestStatus();
    }

    public Resolution cancel() {
        // Signal the DepositManager that cancellation has been requested by the user
        if (depositManager instanceof Cancelable) {
            ((Cancelable)depositManager).cancel(getDepositId());
        }
        cancelFlag = true;
        return redirectToIngestStatus();
    }

    /**
     * Creates a redirect resolution that will redirect the user to the status page for their ingest.
     *
     * @return the RedirectResolution to the ingest status page
     */
    private RedirectResolution redirectToIngestStatus() {
        RedirectResolution rr = new RedirectResolution(this.getClass(), STATUS_EVENT);
        rr.addParameter("depositId", depositId);
        rr.addParameter("cancelFlag", cancelFlag);
        return rr;
    }

    public Resolution status() {
        DepositDocument depositStatus = depositManager.getDepositInfo(depositId).getDepositStatus();
        if (cancelFlag) {
            addMessage(getContext().getRequest(),
                    "<b>Ingest of package with ID '" + depositId + "' was cancelled.</b>", "errors");
            addMessage(getContext().getRequest(),
                    "Last modified: " + DateUtility.toIso8601(depositStatus.getLastModified()), "inform");
            phaseComplete = true;
        }
        else if (depositManager.getDepositInfo(depositId).hasCompleted()
                && depositManager.getDepositInfo(depositId).isSuccessful()) {
            addMessage(getContext().getRequest(), "<b>Ingest of package with ID '" + depositId
                    + "' was completed successfully.</b>", "successful");
            addMessage(getContext().getRequest(),
                    "Last modified: " + DateUtility.toIso8601(depositStatus.getLastModified()), "inform");
            phaseComplete = true;
        }
        else if (depositManager.getDepositInfo(depositId).hasCompleted()
                && !depositManager.getDepositInfo(depositId).isSuccessful()) {
            addMessage(getContext().getRequest(), "<b>Ingest of package with ID '" + depositId
                    + "' encountered errors, please look for 'type: ingest.fail'.</b>",
                    "errors");
            addMessage(getContext().getRequest(),
                    "Last modified: " + DateUtility.toIso8601(depositStatus.getLastModified()), "inform");
            try {
                addMessage(getContext().getRequest(), IOUtils.toString(depositStatus.getInputStream()), "inform");
            }
            catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            phaseComplete = true;
        }
        else {
            boolean ingestReportFlag = false;
            for (String key : depositStatus.getMetadata().keySet()) {
                if (key.equalsIgnoreCase(DOCUMENT_TYPE)) {
                    if (depositStatus.getMetadata().get(key).equalsIgnoreCase(PRE_INGEST_REPORT)) {
                        ingestReportFlag = true;
                        break;
                    }
                }
            }
            if (ingestReportFlag) {
                try {
                    IngestReport ingestReport = ingestReportService.buildIngestReport(depositStatus.getInputStream());
                    String status = ingestReport.getStatus().name().toLowerCase();
                    addMessage(getContext().getRequest(), "<b>" + ingestReport.getStatusMessage() + "</b>", status);
                    status = "inform";
                    addMessage(getContext().getRequest(),
                            "<b>Total Content Size:</b> " + ingestReport.getTotalPackageSize() + " Bytes", status);
                    Map<String, Integer> dataItemsPerCollectionCount = ingestReportService
                            .getDataItemsPerCollectionCount(ingestReport);
                    if (dataItemsPerCollectionCount != null) {
                        String output = "<b>DataItems per Collections:</b></br>";
                        for (String title : dataItemsPerCollectionCount.keySet()) {
                            output += title + ": "
                                    + dataItemsPerCollectionCount.get(title) + "<br/>";
                        }
                        addMessage(getContext().getRequest(), output, status);
                    }
                    if (ingestReport.getGeneratedChecksumsCount() != null) {
                        String output = "<b>Generated checksums:</b><br/>";
                        for (String algorithm : ingestReport.getGeneratedChecksumsCount().keySet()) {
                            output += "Total number of '" + algorithm + "' checksums generated: "
                                    + ingestReport.getGeneratedChecksumsCount().get(algorithm) + "<br/>";
                        }
                        addMessage(getContext().getRequest(), output, status);
                    }
                    if (ingestReport.getVerifiedChecksumsCount() != null) {
                        String output = "<b>Verified checksums:</b><br/>";
                        for (String algorithm : ingestReport.getVerifiedChecksumsCount().keySet()) {
                            output += "Total number of '" + algorithm + "' checksums verified: "
                                    + ingestReport.getVerifiedChecksumsCount().get(algorithm) + "<br/>";
                        }
                        addMessage(getContext().getRequest(), output, status);
                    }
                    if (ingestReport.getFileTypeCount() != null) {
                        String output = "<b>File Types Identified:</b><br/>";
                        for (String type : ingestReport.getFileTypeCount().keySet()) {
                            output += type + ": " + ingestReport.getFileTypeCount().get(type) + "<br/>";
                        }
                        addMessage(getContext().getRequest(), output, status);
                    }
                    if (ingestReport.getContentDetectionTools() != null) {
                        String output = "<b>Content Detection Tools:</b><br/>";
                        for (String name : ingestReport.getContentDetectionTools().keySet()) {
                            output += "<b>Name:</b> " + name + " | <b>Version:</b> "
                                    + ingestReport.getContentDetectionTools().get(name) + "<br/>";
                        }
                        addMessage(getContext().getRequest(), output, status);
                    }
                    if (ingestReport.getUnmatchedFileTypes() != null) {
                        String output = "<b>File formats which failed verification:</b><br/>";
                        for (String type : ingestReport.getUnmatchedFileTypes().keySet()) {
                            output += type + ": " + ingestReport.getUnmatchedFileTypes().get(type) + "<br/>";
                        }
                        addMessage(getContext().getRequest(), output, status);
                    }
                    pause = true;
                }
                catch (InvalidXmlException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            else {
                try {
                    addMessage(getContext().getRequest(),
                            "Last modified: " + DateUtility.toIso8601(depositStatus.getLastModified()), "inform");
                    addMessage(getContext().getRequest(), IOUtils.toString(depositStatus.getInputStream()), "inform");
                }
                catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }

        ForwardResolution rr = new ForwardResolution(this.getClass(), "statusView");
        rr.addParameter("depositId", depositId);
        return rr;
    }
    
    public Resolution statusView() {
        return new ForwardResolution(INGEST_STATUS_PATH);
    }


    /**
     * @return the uploadedFile
     */
    public FileBean getUploadedFile() {
        return uploadedFile;
    }
    
    /**
     * @param uploadedFile
     *            the uploadedFile to set
     */
    public void setUploadedFile(FileBean uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    /**
     * @return the depositId
     */
    public String getDepositId() {
        return depositId;
    }
    
    /**
     * @param depositId
     *            the depositId to set
     */
    public void setDepositId(String depositId) {
        this.depositId = depositId;
    }
    

    /** 
     * @return the status URL built using the deposit id
     */
    public String getDepositStatusUrl() {
        RedirectResolution rr = redirectToIngestStatus();
        
        String statusUrl = uiBaseUrlConfig.getScheme() + "://"  + uiBaseUrlConfig.getHostname() + ":" + uiBaseUrlConfig.getPort();
        
        String contextPath = uiBaseUrlConfig.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()) {
            if (!statusUrl.endsWith("/") && !contextPath.startsWith("/")) {
                statusUrl += "/";
            }
            statusUrl += contextPath; 
        }                
        
        statusUrl += rr.getUrl(Locale.US);
        
        return statusUrl;
    }
    
    /**
     * @return the phaseComplete
     */
    public boolean isPhaseComplete() {
        return phaseComplete;
    }

    /**
     * @param phaseComplete
     *            the phaseComplete to set
     */
    public void setPhaseComplete(boolean phaseComplete) {
        this.phaseComplete = phaseComplete;
    }
    
    /**
     * @return the cancel
     */
    public boolean isCancelFlag() {
        return cancelFlag;
    }
    
    /**
     * @param cancelFlag
     *            the cancelFlag to set
     */
    public void setCancelFlag(boolean cancelFlag) {
        this.cancelFlag = cancelFlag;
    }
    
    /**
     * @return the pause
     */
    public boolean isPause() {
        return pause;
    }
    
    /**
     * @param pause
     *            the pause to set
     */
    public void setPause(boolean pause) {
        this.pause = pause;
    }
    
    /**
     * Injects DepositManager from the SpringBeans.
     * 
     * @param depositManager
     */
    @SpringBean("bagItDepositManagerImpl")
    public void injectDepositManager(ResumableDepositManager depositManager) {
        this.depositManager = depositManager;
    }
    
    @SpringBean("dcsUiBaseUrlConfig")
    public void ingestUiBaseUrlConfig(UiBaseUrlConfig urlConfig) {
        this.uiBaseUrlConfig = urlConfig;
    }
    
    /**
     * Injects IngestReportService from SpringBeans.
     * 
     * @param ingestReportService
     */
    @SpringBean("ingestReportService")
    public void injectIngestReportService(IngestReportService ingestReportService) {
        this.ingestReportService = ingestReportService;
    }

    @SuppressWarnings("unchecked")
    public void addMessage(HttpServletRequest request, String message, String status) {
        FlashScope fs = FlashScope.getCurrent(request, true);
        List<String> messages = (List<String>) fs.get(status);
        if (messages == null) {
            messages = new ArrayList<String>();
            fs.put(status, messages);
        }
        messages.add(message);
    }

    /**
     * Helper function to upload the file.
     * 
     * @param uploadedFile
     * @return
     * @throws IOException
     */
    private InputStream loadDataFile(FileBean uploadedFile) throws IOException {
        return uploadedFile.getInputStream();
    }

}
