package org.dataconservancy.ui.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.Http;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.BusinessObjectMap;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Email;
import org.dataconservancy.ui.model.EmailAttachment;
import org.dataconservancy.ui.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessObjectMapIngestService extends BaseIngestService {
    
    private Logger log = LoggerFactory.getLogger(BusinessObjectMapIngestService.class);

    private BusinessObjectMapService map_service;
    private NotificationService notificationService;
    private Email email;
    private UserService user_service;
    private VelocityTemplateHelper velocity_helper;
    private String velocityTemplatePath;

    protected static final String EMAIL_ATTACHMENT_FILENAME = "ObjectIdsMap-%s-%s.%s";

    @Override
    public void execute(String depositId, IngestWorkflowState state)
            throws StatefulIngestServiceException {
        super.execute(depositId, state);
        
        log.trace("Executing BusinessObjectMapIngestService...");

        if (user_service == null) {
            throw new IllegalArgumentException("UserService must not be null!");
        }

        if (velocity_helper == null) {
            throw new IllegalArgumentException("VelocityTemplateHelper must not be null!");
        }

        if (email == null) {
            throw new IllegalArgumentException("An email template must be loaded!");
        }

        if (map_service == null) {
            throw new IllegalArgumentException("BusinessObjectMapService must not be null");
        }

        if (notificationService == null) {
            throw new IllegalArgumentException("NotificationService must not be null");
        }

        // BO ID -> Package URI
        Map<String, List<String>> alternate_id_map = new HashMap<String, List<String>>();
        List<BusinessObject> objects = get_toplevel_objects(
                state.getBusinessObjectManager(), alternate_id_map);

        Person user = user_service.get(state.getIngestUserId());

        EmailGenerator boMapEmailGenerator = new EmailGenerator(objects, alternate_id_map, user, depositId);
        Thread boMapEmailGeneratingThread = new Thread(boMapEmailGenerator);
        boMapEmailGeneratingThread.start();
        log.trace("Generating email for email " + user.getEmailAddress());

    }

    // Return objects without a parent in the package. Also populate the
    // alternate id map.
    private List<BusinessObject> get_toplevel_objects(
            BusinessObjectManager manager,
            Map<String, List<String>> alternate_id_map) {

        // BO -> Package URI
        Map<BusinessObject, String> pkg_map = manager.createMap();

        for (BusinessObject o : pkg_map.keySet()) {
            List<String> alt_ids = alternate_id_map.get(o.getId());

            if (alt_ids == null) {
                alt_ids = new ArrayList<String>();
                alternate_id_map.put(o.getId(), alt_ids);
            }

            alt_ids.add(pkg_map.get(o));
        }

        List<BusinessObject> result = new ArrayList<BusinessObject>();

        for (BusinessObject o : pkg_map.keySet()) {
            if (!alternate_id_map.containsKey(get_parent(o))) {
                result.add(o);
            }
        }

        return result;
    }

    private String get_parent(BusinessObject o) {
        if (o instanceof Collection) {
            return ((Collection) o).getParentId();
        }

        if (o instanceof DataItem) {
            return ((DataItem) o).getParentId();
        }

        if (o instanceof DataFile) {
            return ((DataFile) o).getParentId();
        }

        return null;
    }

    public void setBusinessObjectMapService(BusinessObjectMapService map_service) {
        this.map_service = map_service;
    }


    public void setUserService(UserService user_service) {
        this.user_service = user_service;
    }

    public void setVelocityHelper(VelocityTemplateHelper velocity_helper) {
        this.velocity_helper = velocity_helper;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setEmailTemplate(Email templateEmail) {
        this.email = templateEmail;
    }


    private class EmailGenerator implements Runnable {

        private List<BusinessObject> businessObjects;
        private Map<String, List<String>> alternate_id_map;
        private Person addressee;
        private String depositId;

        public EmailGenerator(List<BusinessObject> businessObjects,
                              Map<String, List<String>> alternate_id_map,
                              Person addressee, String depositId) {
            this.businessObjects = businessObjects;
            this.alternate_id_map = alternate_id_map;
            this.addressee = addressee;
            this.depositId = depositId;
        }
        @Override
        public void run() {
            try {
                Email boMapEmail = generateEmail();
                notificationService.sendNotification(boMapEmail);
            } catch (IOException e) {
                log.error("IOException occurred when generating email: " + e);
                throw new RuntimeException("IOException occurred when generating email: " + e);
            } catch (ArchiveServiceException e) {
                log.error("ArchiveException Occurred when generating map: "  + e);
                throw new RuntimeException("ArchiveException Occurred when generating map: "  + e);
            } catch (InterruptedException e) {
                throw new RuntimeException("InterruptedException occurred when polling for pending deposits." + e);
            }
        }

        private Email generateEmail() throws IOException, ArchiveServiceException, InterruptedException {
            //do not use the injected notification in the action bean
            //copy its properties onto a fresh notification so that
            //the original notification body field persists
            Email newEmail = new Email();
            if (email == null) {
                throw new IllegalArgumentException("Template for email must not be set to Null");
            }
            newEmail.setSender(email.getFromAddress());
            newEmail.setSubject(email.getSubject());
            //the velocity template path is stored on the
            //injected notification's body
            velocityTemplatePath=email.getBody();

            newEmail.setRecipient(new String[] { addressee.getEmailAddress() });

            VelocityContext context = new VelocityContext();
            context.put("person", addressee);

            newEmail.setNotificationMessage(velocity_helper.execute(velocityTemplatePath,
                    context));
            for (BusinessObject o : businessObjects) {
                BusinessObjectMap bom = map_service
                        .generateMap(o, alternate_id_map, true);

                ByteArrayOutputStream xml_os = new ByteArrayOutputStream();
                map_service.writeXmlMap(bom, xml_os);

                ByteArrayOutputStream html_os = new ByteArrayOutputStream();
                map_service.writeHtmlMap(bom, html_os);
                EmailAttachment xmlAttachment =
                        new EmailAttachment(new ByteArrayInputStream(xml_os.toByteArray()),
                                Http.MimeType.APPLICATION_XML,
                                String.format(EMAIL_ATTACHMENT_FILENAME, depositId, bom.getName(), "xml"));
                EmailAttachment htmlAttachment =
                        new EmailAttachment(new ByteArrayInputStream(html_os.toByteArray()),
                                Http.MimeType.TEXT_HTML,
                                String.format(EMAIL_ATTACHMENT_FILENAME, depositId, bom.getName(), "html"));

                newEmail.attachments().add(xmlAttachment);
                newEmail.attachments().add(htmlAttachment);
            }

            return newEmail;
        }
    }

}
