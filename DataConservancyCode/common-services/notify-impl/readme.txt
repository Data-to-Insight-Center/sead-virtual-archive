Notes on the Dcs Notification Service Implementation (June 3 2010)
------------------------------------------------------------------

The notification service is implemented using Spring JMS.  Clients can call the service to fire off event
notifications to event listeners that then receive the event notifications asynchronously.

You can learn about Spring and JMS at http://static.springsource.org/spring/docs/2.5.x/reference/jms.html

Note: Add "notify-impl/src/main/resources" to your classpath in order to be able to run the notification service using spring.
You will need to do this in order to run the Junit tests in notify-impl/src/test/java/... within eclipse.

ActiveMQ:
---------
You also need to have ActiveMQ locally installed as the message broker that Spring JMS Template uses.  The notifyService.xml
configuration file looks for ActiveMq at tcp://localhost:61616 which is the default installation location.

-Download ActiveMQ from www.activemq.com
-Unzip to your hard drive
-In the base directory of the unzipped distribution, you’ll find incubator-activemq-4.1.0.jar. 
-This is the JAR file you’ll need to add to the application’s classpath to be able to use ActiveMQ’s API.
-In the bin directory, you’ll find a script that starts ActiveMQ: activemq.bat for Windows.
-Run the script and ActiveMQ will be ready to broker your messages.

ActiveMQ dashboard can be accessed at http://localhost:8161/admin/index.jsp

Sending out an event
--------------------
Clients can send out a DCS event to registered listeners using the DcsNotifyService.fire(DcsEvent) method.  Passing in a
DcsEvent object that has the desired event type.
For example:

		DcsEvent dcsEvent = new DcsEvent("Source of the event", "Some message text", DcsEventType.GENERAL_DCS_EVENT);

		DcsNotifyServiceImpl notifySvc = new DcsNotifyServiceImpl();
        
        fireEvent(dcsEvent, notifySvc);
        

Adding a notification listener:
-------------------------------
- Note that by default Spring will instantiate notification listeners when the application context is created.  
- The notifyService.xml file in notify-impl/src/main/resources/notifyService.xml provides bean wirings for the 
default topics and publishers currently set up as well as test listeners used in the Junit tests.
- You can use any Java class to be a listener so long as it is wired in the spring config file properly.
- MockEventListener in: notify-impl/src/test/java/org.dataconservancy.dcs.notify.impl 
gives an idea of a mock implementation of a listener that is used in the jUnit tests and is wired to the corresponding 
listener-container in notifyService.xml

- The DCS notification service is set up to use jms topics (as opposed to queues).  There are currently 2 topics set up:
  - GeneralEvent
  - NewDCSEntity

- Following the example of the MockEventListener users can wire up new listeners.  The wiring specifies the method on your
listener that Spring will call when a message is received from the topic it is wired to.

- So to create a new listener you will need to create a class that has a method on that should be called when a new message is
received.  For example you can create a class org.dataconservancy.dcs.index.impl.UpdateIndex which has a method called
updateIndex(DcsNotification) that takes a DcsNotification parameter to be called when a new DCS entity has been created 
in the archive.
Then add the following to the notifyService.xml file to have that class listen to the NewDCSEntity topic

		<bean id="updateIndexListener" class="org.dataconservancy.dcs.index.impl.UpdateIndex">  <-- Add this bean definition
		</bean>
		
		<jms:listener-container connection-factory="connectionFactory" cache="none"
		destination-type="topic" message-converter="newDcsEntityConverter">
				<jms:listener destination="TOPIC.NEWDCSENTITY" ref="testEventListenerNewDcsEntity"
				method="onNotification"/>
				<jms:listener destination="TOPIC.NEWDCSENTITY" ref="testEventListenerAll"
				method="onNotification"/>
				<jms:listener destination="TOPIC.NEWDCSENTITY" ref="updateIndexListener"       <-- Add the bean as a listener
				method="updateIndex"/>		<-- Specify the method to call
		</jms:listener-container>
		
... and that's it.  When the Spring application context is created that has these beans and wiring's specified, Spring will
create the listener and call the updateIndex() method when a message is received.


Adding a new Message Destination / Topic
----------------------------------------
- Adding a new message topic can be done by 
a)Adding a new topic that an existing publisher can publish to.
b)Adding a new topic with a new publisher.

a) New Topic with an existing publisher ...
It is done by simply adding an additional topic in the notifyService.xml config file, creating a new publisher bean using
an existing publisher class and wiring them together.  
In addition:
- Add a new type to DcsEventType
- Add the corresponding switch/case to DcsNotifyServiceImpl.fire(dcsEvent)
- Add a new private method to DcsNotifyService, modeled on the existing **private void newDcsEntity(DcsEvent dcsEvent)**

FOR EXAMPLE... say we want to create a new topic that receives events associated with an edited Provider Agreement.  And say we
are happy to use the PublishGeneralEvent class to do so:
- In notifyService.xml add the following ...

		<bean id="editedProviderAgreementTopic" class="org.apache.activemq.command.ActiveMQTopic">
		<constructor-arg index="0" value="TOPIC.EDITEDPROVIDERAGREEMENT"/>
		</bean>

		<bean id="editedProviderAgreementPublisher" 
		class="org.dataconservancy.dcs.notify.impl.PublishGeneralEvent">
			<property name="jmsTemplate" ref="jmsTemplate-generalEvent" />
			<property name="topic" ref="editedProviderAgreementTopic" />
		</bean>

- In DcsEventType edit to read:

public enum DcsEventType implements Serializable{
        NEW_DCS_ENTITY,
        GENERAL_DCS_EVENT,
        EDITED_PROVIDER_AGREEMENT
    }

- In DcsNotifyServiceImpl.fire() add to the switch statement:

            case EDITED_PROVIDER_AGREEMENT: editedProviderAgreementEvent(dcsEvent); 
                break;

..and also add the corresponding method..

    private void editedProviderAgreementEvent(DcsEvent dcsEvent) {
        //Use Spring to instantiate the publishers.
        BeanFactory factory = new XmlBeanFactory(new ClassPathResource("notifyService.xml"));
        PublishGeneralEvent pubGenEvent = (PublishGeneralEvent) factory.getBean("editedProviderAgreementPublisher");
        pubGenEvent.send(dcsEvent);
    }
    
b) Adding a new topic that requires a new publisher and message converter.
This is the same as the above approach but you also need to:
- Add a new PublishXXXX class (modeled on one of the existing publish classes e.g. PublishGeneralEvent)
- Add a corresponding message Converter class (modeled on one of the existing converter classes e.g. GeneralEventConverter)
- create a new jmsTemplate bean in notifyService.xml:

		<bean id="jmsTemplate-editedProviderAgreement"
		class="org.springframework.jms.core.JmsTemplate">
		<property name="connectionFactory" ref="connectionFactory" />
		<property name="messageConverter" ref="editedProviderAgreementConverter" />
		</bean>

- Wire the new jmsTemplate into the editedProviderAgreementPublisher bean.
- You can put listeners for the new topic in the existing <listener-container> in the notifyService.xml file (assuming the
method to be called on the listener takes a DcsNotification object).

