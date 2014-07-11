<b>SEAD Virtual Archive code</b>

The code includes extensions to Data Conservancy Services code base.<br/>
<b>Pre-requisites:</b>
Before setting up the Virtual Archive service, please complete the following tasks:
<ul>
<li>Creating an ACR account: (Not needed for local bag upload)
<br/>
Please request an account in Active Content Repository at http://sead-demo.ncsa.illinois.edu/acr/ 
<li>Build requirements:
For building code  please use maven and jdk (1.7.x)
Please setup latest version of Apache Tomcat
Once tomcat is setup, please copy Hibernate jar[http://seadva-test.d2i.indiana.edu:8081/artifactory/ext-release-local/h2/h2/1.2.139/h2-1.2.139.jar] and mysql-connector jar [http://dev.mysql.com/downloads/connector/j/] into the into the lib folder inside tomcat folder.
<li>Increase the stack size as with JAVA_OPTS environment variable for jvm and to also allow encoding of slash and backslash as shown below.
</ul>
```export JAVA_OPTS="-Xss512m -Xms512m -Xmx1024m -XX:MaxPermSize=1024m
-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true
-Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=true"```

In tomcat/conf/server.xml, in Connector set maxHttpHeaderSize to 65536. This allows for large registry requests, needed for POST calls for insert or updates on entities with large number of properties. 
 ```<Connector port="8080" protocol="HTTP/1.1"
                maxHttpHeaderSize="65536"
               connectionTimeout="20000"
               redirectPort="8443" />```


<b>Build Code </b><br/>

Build Pre-requisites:
<br/>
<ul>
<li>Before building the code, please copy the settings.xml file from maven/conf folder in the git repository into your maven folder.

<li>Setting up Database:
Please follow instructions in SEAD-VA-extensions/dcs-access/sead-access-ui/README file
</ul>

<u><b>Build</b></u>
<ol>

Support will be developed soon to build/deploy some of these module in a single step
<li>Building SEAD Registry module: Please follow README instructions in sead-registry module
<li>Building Komadu: Please follow instructions from Komadu github site [https://github.com/Data-to-Insight-Center/komadu/]
<li>Building RO REST service:
<br/>cd SEAD-VA-extensions/services/ro-subsystem/
<br/>Please ensure port numbers are right in ro-subsystem-service/src/main/resources/org/seadva/data/lifecycle/service/Config.properties
<br/>mvn clean install -DskipTests
<br/>Then copy ro-subsystem-service/target/ro-x.x.x.war into tomcat/webapps/ro.war
<li>Building BagIt Service:
<br/>Copy acrInstances.xml[http://seadva-test.d2i.indiana.edu:8081/artifactory/ext-snapshot-local/acrInstances.xml] into resources folder [src/main/resources/org/seadva/bagit/util/acrInstances.xml]
<br/>mvn clean package -DskipTests
<br/>Then copy bagItRestService/target/bagit-x.x.x.war into tomcat/webapps/bagit.war
<li>Building backend workflow: See below

cd dcs-integration/sead-workflow-integration

<u>Build</u>:
<br/>
To clean and build code run:
<br/>
mvn clean
<br/>
mvn package (preferred mvn package -DskipTests, in case any tests fail at this point)
<br/>
The above maven build generates a .war file in target folder.

Install: <br/>Please deploy this war in a servlet container. If you are using tomcat, please copy to webapp folder. When copying, please rename to sead-wf.war, since this is the value that is set as 'prefix' in WEB-INF/classes/default.properties file in the war. Please also change any values you want to change in default.properties file as needed. 
<br/>
The endpoints enabled would include
<br/>
SIP Deposit Servlet at http://localhost:8080/sead-wf/deposit/sip
<br/>
Query Servlet at http://localhost:8080/sead-wf/deposit/squery
<br/>
Data stream Servlet at http://localhost:8080/sead-wf/deposit/datastream
<br/>
Delete Servlet at http://localhost:8080/sead-wf/deposit/del
<br/>
<br/>

<li>Building front-end UI
<br/>
cd ..
<br/>
cd dcs-access/sead-access-ui   
<br/>
Build:
<br/>
mvn clean compile gwt:compile package
<br/>
Install:
<br/>
The above maven commands will clean, compile and package the UI code. It will generate a war file which also needs to be copied to tomcat webapp (rename war to sead-access.war when copying).<Change snapshot files to release files>

<br/>
To open in gwt-dev mode in eclipse, install GWT plugin for eclipse. Import project as maven project. Right click->Properties->Google
<br/>->Web Application -> check 'This project has a war directory' -> browse src/main/webapp
<br/>->Web Toolkit -> check 'Use Google web toolkit'
<br/>Click Ok
</ol>

<b>Setting up the configuration file</b>:
<br/>
During the installation process, sead-access.war file must have been copied into tomcat/webapp folder. Once deployed, please set values in the following configuration files:

1) tomcat/webapps/sead-access/sead_access/Config.properties.
<br/>
2) tomcat/webapps/sead-access/WEB-INF/classes/acrInstances.xml 

<b>Running the service</b>:
Once installation is complete, you can access the UI at http://localhost:8080/sead-access

Faceted Browsing is available on the left pane under 'Data Search' tab. Data can be ingested into Virtual Archive using the 'Upload Data' tab which needs login.

<u><b>Test Cases<b></u>:

<ul>
<li>Admin Login:

To login, the database was initialized with admin username as 'seadva@gmail.com' and password as 'password'.

<li>Creating new user:

The resgister button is used to register a new user. When  logged in as 'Admin' (eg: seadva@gmail.com), an 'Administration' tab is visible, where the admin can select a role for a new user and approve the user.

<li>Google Login:

A user can register using Google Login. If it is the first time a user logs in using Google, they need to be approved by the admin before they are able to ingest data sets into Virtual Archive. 

<li>Ingesting datasets:

In the 'Upload Data' tab, please login using admin credentials or any other login that was created. Now select "Nced Project" and click on "View Collections to Publish". 20 datasets in this case will displayed that can be published.

</ul>
