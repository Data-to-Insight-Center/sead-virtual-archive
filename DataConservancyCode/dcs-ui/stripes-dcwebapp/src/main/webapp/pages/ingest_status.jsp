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

<script type="text/javascript">

</script>
        
<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
  <stripes:layout-component name="pageTitle">
    Ingest Status
  </stripes:layout-component>  
  
  <stripes:layout-component name="contents">
    <stripes:form name="MyForm" beanclass="org.dataconservancy.ui.stripes.IngestPackageActionBean" class="PackageIngestForm">
      <div class="dc-content">
        <div class="SectionContent">          
	      <h3 class="metadataPreviewLeft">Deposit ID: ${actionBean.depositId}</h3>
		  <h3 class="metadataPreviewLeft">Deposit Status URL: <stripes:link href="${actionBean.depositStatusUrl}"/></h3>
		  <h3 class="metadataPreviewLeft">Deposit Status:</h3>
		  <c:if test="${actionBean.phaseComplete == false && actionBean.pause == false }">
            <a class="metadataPreviewLeft" href="${actionBean.depositStatusUrl}">Get Current Status</a> 
          </c:if>
	      <stripes:messages key="UserInputMessages"/>
	      <c:forEach var="message" items="${successful}" varStatus="status">
            <div class="statusSuccess" id="depositStatus-${status.index}"><pre>${message}</pre></div>
          </c:forEach>
          <c:forEach var="message" items="${warnings}" varStatus="status">
            <div class="statusWarning" id="depositStatus-${status.index}"><pre>${message}</pre></div>
          </c:forEach>
          <c:forEach var="message" items="${errors}" varStatus="status">
            <div class="statusError" id="depositStatus-${status.index}"><pre>${message}</pre></div>
          </c:forEach>
	      <c:forEach var="message" items="${inform}" varStatus="status">
	        <div class="metadataPreviewRight" id="depositStatus-${status.index}"><pre>${message}</pre></div>
	      </c:forEach>

          <div class="metadataPreviewLeft">
            <c:if test="${actionBean.phaseComplete == false && actionBean.pause == false }">
              <a href="${actionBean.depositStatusUrl}">Get Current Status</a>
            </c:if>
            <c:if test="${actionBean.pause == true }">
              <stripes:submit name="resume" value="Continue"/>
              <stripes:submit name="cancel" value="Cancel"/>
            </c:if>
          </div>    
        </div>
        
      </div>
      <stripes:hidden name="depositUrl" value="${actionBean.depositStatusUrl}"/>
      <stripes:hidden name="depositId" value="${actionBean.depositId}"/>
      <stripes:hidden name="cancelFlag" value="${actionBean.cancelFlag}"/>
    </stripes:form>
  </stripes:layout-component>
    
</stripes:layout-render>
