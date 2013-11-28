<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fmt_rt" uri="http://java.sun.com/jstl/fmt_rt" %>

<%--
  ~ Copyright 2013 Johns Hopkins University
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
  <stripes:layout-component name="flash">
    <stripes:messages key="UserInputMessages"/>
    <c:forEach var="message" items="${inform}">
       <div>${message}</div>
    </c:forEach>
  </stripes:layout-component>
  <stripes:layout-component name="pageTitle">
    Ingest Package
  </stripes:layout-component>
    
  <stripes:layout-component name="contents">
    <stripes:form beanclass="org.dataconservancy.ui.stripes.IngestPackageActionBean" class="PackageIngestForm">
      <div class="content-left-col">
        <h2 class="SectionHeader">Select a package to ingest: (tar.gz format)</h2>
        <div class="SectionContent">
          <fieldset>
            <div class="label-field-group">
              <stripes:file name="uploadedFile"/>
              <stripes:errors field="uploadedFile"/>
              <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                <fmt:message key="maxSize" var="maxSize"/>
              </fmt_rt:bundle>
              ${maxSize}
            </div>
            <br/>
            <div>
              <stripes:submit name="ingest" value="Ingest"/>
            </div>
          </fieldset>
        </div>
      </div>
    </stripes:form>
  </stripes:layout-component>
</stripes:layout-render>
