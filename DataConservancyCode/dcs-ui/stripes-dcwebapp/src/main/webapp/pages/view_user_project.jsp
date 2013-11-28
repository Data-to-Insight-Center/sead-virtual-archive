<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
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
<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
  <stripes:layout-component name="flash">
    <stripes:messages key="updated"/>
  </stripes:layout-component>
  <stripes:layout-component name="contents">

    	<h2 class="SectionHeader">Viewing ${actionBean.project.name} project</h2>
	    <div class="SectionContent">   
		    <table>
		        <tr><th>Name:</th> <td>${actionBean.project.name}</td></tr>
		        <tr><th>Description:</th> <td>${actionBean.project.description}</td></tr>
		        <tr><th>Funding entity:</th> <td>${actionBean.project.fundingEntity}</td></tr>
                <tr>
                    <th>Award No:
                        <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                            <fmt:message key="projectAwardNumberToolTip" var="projectAwardNumberToolTipVar"/>
                        </fmt_rt:bundle>
                        <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${projectAwardNumberToolTipVar}" class="tip"/>
                    </th>
                    <td>
                    	<c:choose>
                    		<c:when test="${fn:length(actionBean.project.numbers) > 1}">
								<c:forEach items="${actionBean.project.numbers}" var="number">
									${number} |   
	            	        	</c:forEach>
							</c:when>
							<c:otherwise>
								${actionBean.project.numbers[0]}
							</c:otherwise>
						</c:choose>
                    </td>
                </tr>
		        <tr><th>Start date:</th> <td><stripes:format value="${actionBean.project.startDate}"/></td></tr>
		        <tr><th>End date:</th> <td><stripes:format value="${actionBean.project.endDate}"/></td></tr>
		        <tr><th>Storage allocated:</th> <td><stripes:format formatPattern="decimal" value="${actionBean.project.storageAllocated / 1000000000}"/> GB</td></tr>
		        <tr><th>Storage used:</th> <td><stripes:format formatPattern="decimal" value="${actionBean.project.storageUsed / 1000000000}"/> GB</td></tr>
        	    <tr><th colspan="1" rowspan="${fn:length(actionBean.projectAdminList)}">Project Administrators: </th>
                	<c:if test="${fn:length(actionBean.projectAdminList) == '0'}">
                		<td></td></tr>
            		</c:if>
            		<c:forEach items="${actionBean.projectAdminList}" var="pi" varStatus="status">
            			<c:if test="${status.index gt '0'}">
            				<tr>
            			</c:if>
            			
            				<td>${pi.firstNames} ${pi.lastNames}</td></tr>
            			
        			</c:forEach>
		    </table>
	    </div>
	    <p class="SectionContent">
	        <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean" title="Edit project" event="editUserProject">
	            <stripes:param name="selectedProjectId" value="${actionBean.project.id}"/>
	            <stripes:param name="forwardRequestSource" value="${actionBean.viewProjectPath}"/>
	            Edit project
	        </stripes:link>
	    </p>
	    <p class="SectionContent">
	        <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActivityActionBean" title="View project's activities log" event="render"><stripes:param name="selectedProjectId" value="${actionBean.project.id}"/>View project's activities log</stripes:link>
	    </p>
	    <p class="SectionContent">
	        <stripes:link beanclass="org.dataconservancy.ui.stripes.UserCollectionsActionBean"
                title="View collections in this project" event="render">
                <stripes:param name="currentProjectId" value="${actionBean.project.id}"/>
                    View collections in this project
            </stripes:link>
            |
            <stripes:link beanclass="org.dataconservancy.ui.stripes.AddCollectionActionBean"
	      	    title="Add Collection" event="displayCollectionForm">
	      	    <stripes:param name="projectId" value="${actionBean.project.id}"/>
	      	    Add Collection
	      	</stripes:link>
	    </p>
	    <p class="SectionContent">
            <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean" title="Object ID Export" event="exportObjectMap">
                <stripes:param name="selectedProjectId" value="${actionBean.project.id}"/>
                Object ID Export
            </stripes:link>
        </p>

  </stripes:layout-component>
</stripes:layout-render>
