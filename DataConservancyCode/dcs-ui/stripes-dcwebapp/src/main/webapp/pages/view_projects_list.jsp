<%@ page import="java.util.List" %>
<%@ page import="org.dataconservancy.ui.model.Person" %>
<%@ page import="org.dataconservancy.ui.model.Role" %>
<%@ page import="org.dataconservancy.ui.stripes.BaseActionBean" %>
<%@ page import="java.util.concurrent.atomic.AtomicBoolean" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="fmt_rt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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
        <c:set var="authUser" scope="page" value='${actionBean.authenticatedUser}'/>
        <%
            Person authUser = (Person) pageContext.getAttribute("authUser", PageContext.PAGE_SCOPE);
            if (authUser != null) {
                List<Role> roles = authUser.getRoles();
                pageContext.setAttribute("isAdmin", roles.contains(Role.ROLE_ADMIN), PageContext.PAGE_SCOPE);
            }
        %>

            <h2 class="SectionHeader">List of Projects</h2>
    	    <div class="SectionContent">
    	        <c:if test="${pageScope.isAdmin == true}">
    	            <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean"
                        title="Add project" event="addUserProject">
                        <stripes:param name="forwardRequestSource" value="/pages/view_projects_list.jsp"/>
                        Add project
                        </stripes:link>
                </c:if>
                <br/>
                <br/>
    		    <c:if test="${!empty actionBean.userProjects}">
	  			<table>
					<tr><th>Name</th><th>Award Number(s)</th><th>Actions</th></tr>
	   				<c:forEach items="${actionBean.userProjects}" var="project">
		        	<tr>
          				<td> <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean"
                               title="${project.name}" event="viewUserProject">
                               <stripes:param name="selectedProjectId" value="${project.id}"/>
                               ${project.name}</stripes:link>
                               </td>
          				<td> 
          					<c:choose>
                    		<c:when test="${fn:length(project.numbers) > 1}">
								<c:forEach items="${project.numbers}" var="number">
									${number} |   
	            	        	</c:forEach>
							</c:when>
							<c:otherwise>
								${project.numbers[0]}
							</c:otherwise>
						</c:choose>
          				</td>
          				<td>
                               <stripes:link beanclass="org.dataconservancy.ui.stripes.UserCollectionsActionBean"
                               title="View list of collections" event="render"><stripes:param name="currentProjectId" value="${project.id}"/>
                               List Collections
                               </stripes:link>
                               |
                               <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActivityActionBean"
                               title="View project's activities log"
                               event="render"><stripes:param name="selectedProjectId" value="${project.id}"/>
                               View activities
                               </stripes:link>
                               |
                               <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean"
                               title="Edit project" event="editUserProject"><stripes:param name="selectedProjectId" value="${project.id}"/>
                               <stripes:param name="forwardRequestSource" value="${actionBean.viewProjectListPath}"/>
                               Edit
                               </stripes:link>

                               </td>
        			</tr>
      				</c:forEach>
	  			</table>
	  		    </c:if>
	  	    </div>

    </stripes:layout-component>
</stripes:layout-render>
