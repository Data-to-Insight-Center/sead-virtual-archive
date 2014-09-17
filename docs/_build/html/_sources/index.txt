.. SEAD Virtual Archive documentation master file, created by
   sphinx-quickstart on Wed Sep 17 15:09:17 2014.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Welcome to SEAD Virtual Archive's documentation!
================================================

Codeset
=======

The code works with and substantially extends the Data Conservancy Services code base.

Pre-requisites
--------------

Before setting up the Virtual Archive service, please complete the following tasks:

- Creating an ACR account: (Not needed for local bag upload) Please request an account in `Active Content Repository <http://sead-demo.ncsa.illinois.edu/acr/>`_

- Build requirements: For building code please use maven and jdk (1.7.x) Please setup latest version of Apache Tomcat Once tomcat is setup, please copy `Hibernate jar <http://seadva-test.d2i.indiana.edu:8081/artifactory/ext-release-local/h2/h2/1.2.139/h2-1.2.139.jar>`_ jar and `mysql-connector jar <http://dev.mysql.com/downloads/connector/j/>`_ into the into the lib folder inside tomcat folder.

- Increase the stack size as with JAVA_OPTS environment variable for jvm and to also allow encoding of slash and backslash as shown below::

	export JAVA_OPTS="-Xss512m -Xms512m -Xmx1024m -XX:MaxPermSize=1024m -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true -Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=true"

- In tomcat/conf/server.xml, in Connector set maxHttpHeaderSize to 65536. This allows for large registry requests, needed for POST calls for insert or updates on entities with large number of properties::

	<Connector port="8080" protocol="HTTP/1.1" maxHttpHeaderSize="65536" connectionTimeout="20000" redirectPort="8443" />

- Install curl::

	yum install curl or apt-get install curl

Build Code
----------

Pre-requisites
--------------

- Before building the code, please copy the settings.xml file from maven/conf folder in the git repository into your maven folder.
- Setting up Database: Please follow instructions in SEAD-VA-extensions/dcs-access/sead-access-ui/README file

Build
-----

- Build Registry Module: Please follow README instructions in sead-registry module
- Build Komadu Module: Please follow instructions from `Komadu github site <https://github.com/Data-to-Insight-Center/komadu/>`_
- Build RO REST Service (Please ensure port numbers are right in ro-subsystem-service/src/main/resources/org/seadva/data/lifecycle/service/Config.properties)::

	$ cd SEAD-VA-extensions/services/ro-subsystem/
	$ mvn clean install -DskipTests
	(copy ro-subsystem-service/target/ro-x.x.x.war into tomcat/webapps/ro.war)
	$ mvn test

- Build BagIt Service::

	Copy `acrInstances.xml <http://seadva-test.d2i.indiana.edu:8081/artifactory/ext-snapshot-local/acrInstances.xml>` into resources folder [src/main/resources/org/seadva/bagit/util/acrInstances.xml]
	$ mvn clean package -DskipTests
	(Please note that some test cases that depend on ACR credentials will not succeed if ACR credentials are not set correctly)
	Copy bagItRestService/target/bagit-x.x.x.war into tomcat/webapps/bagit.war

- Build Backend Workflow Module::

	$ cd dcs-integration/sead-workflow-integration
	$ mvn clean
	$ mvn package -DskipTests

.. note::

	If you are using tomcat, please rename the war to sead-wf.war and copy to webapp folder. Make changes to the values in the following files

	- default.properties
	- sead-wf/WEB-INF/classes/RepositoryCredentials.xml 

	$ mvn test

.. note::

	The end points enabled would include:

	- Deposit Status Servlet: http://localhost:8080/sead-wf/content/sipDeposit/{sipId}
	- SIP Deposit Servlet: http://localhost:8080/sead-wf/deposit/sip (POST method)
	- Query Servlet: http://localhost:8080/sead-wf/deposit/squery?q=title:eel (sample query) 
	- Data Stream Servlet: http://localhost:8080/sead-wf/deposit/datastream/

- Build the Front End::

	$ cd dcs-access/sead-access-ui 
	$ mvn clean compile gwt:compile package
	(Rename the generated war file to sead-access.war and copy the war file into tomcat webapps folder)

.. note::

	- To open in gwt-dev mode in eclipse, install GWT plugin for eclipse. 
	- Import project as maven project. Right click->Properties->Google ->Web Application -> check 'This project has a war directory' 
	- -> browse src/main/webapp ->Web Toolkit -> check 'Use Google web toolkit' 
	- Click Ok

	Once the war file is deployed, the following configuration files needs to be updated::

	- webapps/sead-access/sead_access/Config.properties
	- webapps/sead-access/WEB-INF/classes/DBConfig.properties​ 
	- webapps/sead-access/WEB-INF/classes/acrInstances.xml

- Running the Service::

	Once the installation is complete, access the UI at `Sead Virtual Archive <http://localhost:8080/sead-access>`_

Faceted Browsing is available on the left pane under 'Data Search' tab. Data can be ingested into Virtual Archive using the 'Upload Data' tab after the user haslogged in.

Test Cases
----------

- Admin Login: To login, the database was initialized with admin username as 'seadva@gmail.com' and password as 'password'.
- Creating new user: The resgister button is used to register a new user. When logged in as 'Admin' (eg: seadva@gmail.com), an 'Administration' tab is visible, where the admin can select a role for a new user and approve the user.
- Google Login: A user can register using Google Login. If it is the first time a user logs in using Google, they need to be approved by the admin before they are able to ingest data sets into Virtual Archive.
- Ingesting datasets: In the 'Upload Data' tab, please login using admin credentials or any other login that was created. Now select "Nced Project" and click on "View Collections to Publish". 20 datasets in this case will displayed that can be published.

Contents:

.. toctree::
   :maxdepth: 2

Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`

