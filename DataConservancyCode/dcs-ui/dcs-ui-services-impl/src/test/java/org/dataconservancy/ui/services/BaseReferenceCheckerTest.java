package org.dataconservancy.ui.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.AttributeSetManager;
import org.dataconservancy.packaging.ingest.api.BusinessObjectManager;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.shared.AttributeSetManagerImpl;
import org.dataconservancy.packaging.ingest.shared.AttributeSetImpl;
import org.dataconservancy.packaging.model.Package;
import org.dataconservancy.packaging.model.PackageDescription;
import org.dataconservancy.packaging.model.PackageSerialization;
import org.dataconservancy.packaging.model.impl.DescriptionImpl;
import org.dataconservancy.packaging.model.impl.PackageImpl;
import org.dataconservancy.packaging.model.impl.SerializationImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class BaseReferenceCheckerTest {
    
    protected Package pkg;
    protected IngestWorkflowState state;
    protected AttributeSetManager attributeManager;
    protected BusinessObjectManager businessObjectManager;
    protected EventManager eventManager;
    protected Set<DcsEvent> eventSet;
    
    protected AttributeSet packageAttributeSet;
    protected AttributeSet projectAttributeSet;
    protected AttributeSet collectionAttributeSet;
    protected AttributeSet subCollectionAttributeSet;
    protected AttributeSet dataItemAttributeSet;
    protected AttributeSet versionedDataItemAttributeSet;
    protected AttributeSet fileAttributeSet;
    protected AttributeSet collectionOnlyFileAttributeSet;
    protected AttributeSet dataItemOnlyFileAttributeSet;
    protected AttributeSet fileCollectionMetadataAttributeSet;
    protected AttributeSet fileDataItemMetadataAttributeSet;
    
    @Before
    public void setup() throws Exception {
        
        File extractDir = new File("/tmp/package-extraction");
        extractDir.mkdir();
        extractDir.deleteOnExit();

        File baseDir = new File("deposit-01", "bagFoo");
        baseDir.mkdir();
        baseDir.deleteOnExit();

        PackageDescription description = new DescriptionImpl();
        PackageSerialization serialization = new SerializationImpl();
        serialization.setExtractDir(extractDir);
        serialization.setBaseDir(baseDir);
        pkg = new PackageImpl(description, serialization);
        businessObjectManager = mock(BusinessObjectManager.class);
        
        attributeManager = new AttributeSetManagerImpl();

        eventSet = new HashSet<DcsEvent>();
        eventManager = mock(EventManager.class);
        doAnswer(new Answer<DcsEvent>() {
            @Override
            public DcsEvent answer(InvocationOnMock invocation) throws Throwable {
                
                // Extract the event and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the key and the event to be added", args);
                assertEquals("Expected two arguments: the key and the event to be added", 2, args.length);
                assertTrue("Expected argument one to be of type string", args[0] instanceof String);
                assertTrue("Expected argument two to be of type DcsEvent", args[1] instanceof DcsEvent);
                @SuppressWarnings("unused")
                String key = (String) args[0];
                DcsEvent event = (DcsEvent) args[1];
                eventSet.add(event);
                return null;
            }
        }).when(eventManager).addEvent(anyString(), any(DcsEvent.class));
        
        doAnswer(new Answer<DcsEvent>() {
            
            @Override
            public DcsEvent answer(InvocationOnMock invocation) throws Throwable {
                // Extract the Event and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the type of the event to be generated", args);
                assertEquals("Expected one argument: the type of the event to be retrieved", 1, args.length);
                assertTrue("Expected argument one to be of type string", args[0] instanceof String);
                String type = (String) args[0];
                
                DcsEvent dcsEvent = new DcsEvent();
                dcsEvent.setEventType(type);
                dcsEvent.setDate(DateTime.now().toString());
                dcsEvent.setId("foo");
                return dcsEvent;
            }
            
        }).when(eventManager).newEvent(anyString());
        
        doAnswer(new Answer<Collection<DcsEvent>>() {
            @Override
            public Collection<DcsEvent> answer(InvocationOnMock invocation) throws Throwable {
                
                // Extract the Event and key from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected two arguments: the id and the type of the event to be retrieved", args);
                assertEquals("Expected two arguments: the id and the type of the event to be retrieved", 2, args.length);
                assertTrue("Expected argument one to be of type string", args[0] instanceof String);
                assertTrue("Expected argument two to be of type string", args[1] instanceof String);
                @SuppressWarnings("unused")
                String key = (String) args[0];
                String type = (String) args[1];
                
                List<DcsEvent> events = new ArrayList<DcsEvent>();
                for (DcsEvent event : eventSet) {
                    if (event.getEventType().equalsIgnoreCase(type)) {
                        events.add(event);
                    }
                }
                return events;
            }
        }).when(eventManager).getEvents(anyString(), anyString());

        packageAttributeSet = new AttributeSetImpl("Ore-Rem-Package");
        projectAttributeSet = new AttributeSetImpl("Ore-Rem-Project");
        collectionAttributeSet = new AttributeSetImpl("Ore-Rem-Collection");
        dataItemAttributeSet = new AttributeSetImpl("Ore-Rem-DataItem");
        fileAttributeSet = new AttributeSetImpl("Ore-Rem-File");
        subCollectionAttributeSet = new AttributeSetImpl("Ore-Rem-Collection");
        versionedDataItemAttributeSet = new AttributeSetImpl("Ore-Rem-DataItem");
        collectionOnlyFileAttributeSet = new AttributeSetImpl("Ore-Rem-File");
        dataItemOnlyFileAttributeSet = new AttributeSetImpl("Ore-Rem-File");
        fileCollectionMetadataAttributeSet = new AttributeSetImpl("Ore-Rem-Collection");
        fileDataItemMetadataAttributeSet = new AttributeSetImpl("Ore-Rem-DataItem");
        
        state = mock(IngestWorkflowState.class);
        when(state.getBusinessObjectManager()).thenReturn(businessObjectManager);
        when(state.getAttributeSetManager()).thenReturn(attributeManager);
        when(state.getPackage()).thenReturn(pkg);
        when(state.getEventManager()).thenReturn(eventManager);
    }

}
