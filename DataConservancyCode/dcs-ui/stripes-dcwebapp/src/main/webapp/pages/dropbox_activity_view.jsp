<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fmt_rt" uri="http://java.sun.com/jstl/fmt_rt" %>

<%--
  ~ Copyright 2012 Johns Hopkins University
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


<%-- Reload every 30 seconds, make sure just hitting default handler. --%>
<c:if test="${actionBean.pollInProgress}">
  <script type="text/javascript">
    setTimeout(function () {window.location.assign(location.protocol + "//" + location.host + location.pathname);}, 30000);
  </script>
</c:if>

<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
  <stripes:layout-component name="pageTitle">
    Dropbox
  </stripes:layout-component>
    
  <stripes:layout-component name="contents">
    <div class="dropbox-nav">
      <c:if test="${!actionBean.pollInProgress}">
        <stripes:link beanclass="org.dataconservancy.ui.stripes.DropboxActivityActionBean" event="pollDropbox">
          Poll Dropbox
        </stripes:link>
      </c:if>
      
      <stripes:link beanclass="org.dataconservancy.ui.stripes.DropboxActivityActionBean" event="render">
         <stripes:param name="page" value="${actionBean.page}"/>
         View Package
      </stripes:link>
      
      <stripes:link beanclass="org.dataconservancy.ui.stripes.DropboxActivityActionBean" event="render">
         <stripes:param name="page" value="${actionBean.page}"/>
         Edit Metadata
      </stripes:link>
      
      <stripes:link beanclass="org.dataconservancy.ui.stripes.DropboxActivityActionBean" event="render">
        <stripes:param name="page" value="${actionBean.page}"/>
        Ingest Package
      </stripes:link>
    </div>
              
    <div class="center-message">        
      <b>${actionBean.message}</b>
    </div>
                
    <h2 class="SectionHeader">Activities</h2>

    <div class="SectionContent">
      <table style="width: 100%; table-layout: fixed">
        <tr>
          <th>Date</th>
          <th>Activity Description</th>
          <th>Status</th>
          <th>User</th>
        </tr>
                            
        <c:forEach items="${actionBean.pageActivities}" var="a" varStatus="status">
          <tr class="dropbox-activity-${fn:toLowerCase(a.type)}">
            <td><joda:format value="${a.date}" style="SM"/></td>
            <td>${a.description}</td>
            <td>${a.status}</td>
            <td>${a.user}</td>
          </tr>
        </c:forEach>
      </table>
    </div>
        
    <c:if test="${actionBean.page > '0'}">
      <stripes:link beanclass="org.dataconservancy.ui.stripes.DropboxActivityActionBean" event="render" class="footer-page-link-left">
        <stripes:param name="page" value="${actionBean.page - 1}"/>
        Previous
      </stripes:link>
    </c:if>
        
    <c:if test="${actionBean.page < actionBean.lastPage}">
      <stripes:link beanclass="org.dataconservancy.ui.stripes.DropboxActivityActionBean" event="render" class="footer-page-link-right">
         <stripes:param name="page" value="${actionBean.page + 1}"/>
         Next
      </stripes:link>
    </c:if>
  </stripes:layout-component>
</stripes:layout-render>
