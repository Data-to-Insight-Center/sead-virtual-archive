Steps:

<ol>
<li> In src/main/webapp/WEB-INF/Config.properties change the directory path as needed. This is the directory used by the service to create temporary folders.
<li> Build package: mvn package -DskipTests
<li> Copy target/bagit-x.x.war into tomcat's webapp as bagit.war
<li> Use test cases to generate SIP from Bag. The SIP can then be used with the ingest workflow.
<br/>
In order to add metadata predicates to be queried from ACR, please use ACR_to_ORE_MappingConfig.properties in the resources folder in the code.

In test cases querying ACR, ACR credentials need to populated in src/main/resources/org/seadva/bagit/util/acrInstances.xml for the tests to succeed.
</ol>
