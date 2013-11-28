package org.dataconservancy.ui.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.Http;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.packaging.model.Package;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class BusinessObjectMapIngestServiceTest extends BaseUnitTest {
    private BusinessObjectMapIngestService underTest;

    @Autowired
    private VelocityTemplateHelper velocityTemplateHelper;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private RelationshipService relationshipService;

    private BusinessObjectMapService boMapService;

    private EmailService email_service;

    @Autowired
    @Qualifier("packageIngestCompleteNotification")
    private Email email;

    private IngestWorkflowState state;
    private Map<String, List<String>> alternateIdMap;
    private UserService userService;
    private String collectionWithDataLocalId;
    private String dataItemOneLocalId;
    private String dataItemTwoLocalId;
    private String dataFileOneLocalId;
    private String dataFileTwoLocalId;

    private List<Email> emailList;

    @Before
    public void setup() throws ArchiveServiceException, IOException {
        underTest = new BusinessObjectMapIngestService();
        emailList = new ArrayList<Email>();
        initializeString();
        boMapService = new BusinessObjectMapServiceImpl(archiveService, relationshipService);
        mockEmailService();
        mockIngestWorkflowStateAndPopulateAltIdsMap();
        mockUserService();

        underTest.setBusinessObjectMapService(boMapService);
        underTest.setNotificationService(email_service);
        underTest.setUserService(userService);
        underTest.setVelocityHelper(velocityTemplateHelper);
        underTest.setEmailTemplate(email);
    }

    private void initializeString() {
        collectionWithDataLocalId = "collection/1/local/id";
        dataItemOneLocalId = "dataitem/1/local/id";
        dataItemTwoLocalId = "dataitem/2/local/id";
        dataFileOneLocalId = "datafile/1/local/id";
        dataFileTwoLocalId = "datafile/2/local/id";
    }

    /**
     * mock UserService to return {@code approvedRegsiteredUser} whenever its get() method is called, regardless of the
     * supplied String.
     */
    private void mockUserService() {
        userService = Mockito.mock(UserService.class);
        when(userService.get(anyString())).thenReturn(user);
    }


    private void mockEmailService() {
        email_service = Mockito.mock(EmailService.class);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                // Extract the Attribute set and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected ONE parameter.");
                assertEquals("Expected ONE parameter, but received " + args.length + ".",
                        1, args.length);
                assertTrue("Expected argument one to be of type Email",
                        args[0] instanceof Email);
                Email email = (Email) args[0];
                emailList.add(email);
                return null;
            }
        }).when(email_service).sendNotification(any(Email.class));
    }


    private void mockIngestWorkflowStateAndPopulateAltIdsMap() {
        state = Mockito.mock(IngestWorkflowState.class);

        AttributeSetManager asm = Mockito.mock(AttributeSetManager.class);
        EventManager em = Mockito.mock(EventManager.class);
        Package pkg = Mockito.mock(Package.class);

        BusinessObjectManager bom = Mockito.mock(BusinessObjectManager.class);
        //Verifying assumptions
        assertEquals(projectOne.getId(), collectionWithData.getParentProjectId());
        assertEquals(collectionWithData.getId(), dataItemOne.getParentId());
        assertEquals(collectionWithData.getId(), dataItemTwo.getParentId());
        assertTrue(dataItemOne.getFiles().contains(dataFileOne));
        assertTrue(dataItemTwo.getFiles().contains(dataFileTwo));
                     dataItemOne.setId("id:dataItemOne");
        dataFileOne.setParentId(dataItemOne.getId());
        dataFileTwo.setParentId(dataItemTwo.getId());


        Map<BusinessObject, String> resultMap = new HashMap<BusinessObject, String>();
        resultMap.put(collectionWithData, collectionWithDataLocalId);
        resultMap.put(dataItemOne, dataItemOneLocalId);
        resultMap.put(dataItemTwo, dataItemTwoLocalId);
        resultMap.put(dataFileOne, dataFileOneLocalId);
        resultMap.put(dataFileTwo, dataFileTwoLocalId);

        when(bom.createMap()).thenReturn(resultMap);

        when(state.getBusinessObjectManager()).thenReturn(bom);

        when(state.getIngestUserId()).thenReturn("ingester:id");

        when(state.getAttributeSetManager()).thenReturn(asm);
        when(state.getEventManager()).thenReturn(em);
        when(state.getPackage()).thenReturn(pkg);

        alternateIdMap = new HashMap<String, List<String>>();
        List<String> altIds = new ArrayList<String>();
        altIds.add(collectionWithDataLocalId);
        alternateIdMap.put(collectionWithData.getId(), altIds);

        altIds = new ArrayList<String>();
        altIds.add(dataItemOneLocalId);
        alternateIdMap.put(dataItemOne.getId(), altIds);

        altIds = new ArrayList<String>();
        altIds.add(dataItemTwoLocalId);
        alternateIdMap.put(dataItemTwo.getId(), altIds);

        altIds = new ArrayList<String>();
        altIds.add(dataFileOneLocalId);
        alternateIdMap.put(dataFileOne.getId(), altIds);

        altIds = new ArrayList<String>();
        altIds.add(dataFileTwoLocalId);
        alternateIdMap.put(dataFileTwo.getId(), altIds);
    }



    /**
     * Test the normal route of execution for {@link BusinessObjectMapIngestService}.
     * Assumptions about the object graph:
     * - CollectionWithData is the at the top of the object graph in the BusinessObjectManager
     * - CollectionWithData contains DataItemOne and DataItemTwo
     * - DataItemOne contains DataFileOne
     * - DataItemTwo contains DataFileTwo.
     *
     * Expects:
     * - An email to be send via the email service
     * - Email has 2 attachments: one is of application/xml mimetype, the other is of text/html mimetype
     * - Attachments' content corresponds to the xml and html serialization of the generated business object map.
     *
     * @throws Exception
     */
    @Test
    public void testExecute() throws Exception {
        String depositId = "deposit:id:string";
        ByteArrayOutputStream actualOutputStream;

        //Generate a bo-map with collectionWithData being the top object in the object graph. provide mapping service
        //with the expected alternateIdMap containing the map between the business id of objects to their local ids.
        BusinessObjectMap businessObjectMap = boMapService.generateMap(collectionWithData, alternateIdMap, true);

        //Generate xml serialization of the expected object map
        ByteArrayOutputStream baos_xml = new ByteArrayOutputStream();
        boMapService.writeXmlMap(businessObjectMap, baos_xml);

        //Generate html serialization of the expected object map
        ByteArrayOutputStream baos_html = new ByteArrayOutputStream();
        boMapService.writeHtmlMap(businessObjectMap, baos_html);

        //execute under-test method
        underTest.execute(depositId, state);

        Thread.sleep(10000);
        //verify that emailService.sendNotification() method was called once.
        verify(email_service, times(1)).sendNotification(any(Notification.class));

        //Check that one email was sent
        assertEquals(1, emailList.size());
        //Check that the email has 2 attachments
        assertEquals(2, emailList.get(0).attachments().size());

        //Checking the content of the attachments
        List<EmailAttachment> attachments = emailList.get(0).attachments();
        for (EmailAttachment attachment : attachments) {
            actualOutputStream = new ByteArrayOutputStream();
            assertTrue(attachment.getFilename().equals(
                    String.format(BusinessObjectMapIngestService.EMAIL_ATTACHMENT_FILENAME, depositId, businessObjectMap.getName(), "xml"))
                    || attachment.getFilename().equals(
                    String.format(BusinessObjectMapIngestService.EMAIL_ATTACHMENT_FILENAME, depositId, businessObjectMap.getName(), "html")));
            assertTrue(attachment.getType().equals(Http.MimeType.APPLICATION_XML)
                    || attachment.getType().equals(Http.MimeType.TEXT_HTML));
            assertNotNull(attachment.getData());
            if (attachment.getType().equals(Http.MimeType.APPLICATION_XML)) {
                IOUtils.copy(attachment.getData(), actualOutputStream);
                assertTrue(baos_xml.toString().equals(actualOutputStream.toString()));
            } else if (attachment.getType().equals(Http.MimeType.TEXT_HTML)) {
                IOUtils.copy(attachment.getData(), actualOutputStream);
                assertTrue(baos_html.toString().equals(actualOutputStream.toString()));
            }
        }
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExecuteWithNullNotificationService() throws StatefulIngestServiceException {
        underTest.setNotificationService(null);
        underTest.execute("deposit:id", state);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExecuteWithNullUserService() throws StatefulIngestServiceException {
        underTest.setUserService(null);
        underTest.execute("deposit:id", state);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExecuteWithNullVelocityHelper() throws StatefulIngestServiceException {
        underTest.setVelocityHelper(null);
        underTest.execute("deposit:id", state);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExecuteWithNullEmailTemplate() throws StatefulIngestServiceException {
        underTest.setEmailTemplate(null);
        underTest.execute("deposit:id", state);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExecuteWithNullBOMapService() throws StatefulIngestServiceException {
        underTest.setBusinessObjectMapService(null);
        underTest.execute("deposit:id", state);
    }




}
