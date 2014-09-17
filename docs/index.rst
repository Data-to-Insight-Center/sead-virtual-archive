.. SEAD Virtual Archive documentation master file, created by
   sphinx-quickstart on Wed Sep 17 15:09:17 2014.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Welcome to SEAD Virtual Archive's documentation!
================================================

Contents:

.. toctree::
   :maxdepth: 2

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
	copy ro-subsystem-service/target/ro-x.x.x.war into tomcat/webapps/ro.war
	$ mvn test




Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`

