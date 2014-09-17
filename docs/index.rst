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

- Creating an ACR account: (Not needed for local bag upload) Please request an account in `Active Content Repository`_
.. http://sead-demo.ncsa.illinois.edu/acr/
- Build requirements: For building code please use maven and jdk (1.7.x) Please setup latest version of Apache Tomcat Once tomcat is setup, please copy `Hibernate`_ jar and `mysql-connector`_ jar into the into the lib folder inside tomcat folder.
.. Hibernate: http://seadva-test.d2i.indiana.edu:8081/artifactory/ext-release-local/h2/h2/1.2.139/h2-1.2.139.jar
.. mysql-connector: http://dev.mysql.com/downloads/connector/j/

Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`

