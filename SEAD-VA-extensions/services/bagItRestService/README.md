Steps:

<ol>
<li> In src/main/webapp/WEB-INF/Config.properties change the directory path as needed. This is the directory used by the service to create temporary folders.
<li> Build package: mvn package -DskipTests
<li> Copy target/bagit-x.x.war into tomcat's webapp as bagit.war
<li> Use test cases to generate SIP from Bag. The SIP can then be used with the ingest workflow.
</ol>
