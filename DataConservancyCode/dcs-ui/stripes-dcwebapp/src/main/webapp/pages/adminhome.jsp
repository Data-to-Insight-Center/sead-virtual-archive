<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

    <stripes:layout-component name="contents">

        <h2 class="SectionHeader">Registrations</h2>
        <div class="SectionContent">
            <stripes:link beanclass="org.dataconservancy.ui.stripes.AdminRegistrationManagerActionBean"
                          title="Manage Pending Registrations">
                <fmt:bundle basename="StripesResources" prefix="org.dataconservancy.ui.stripes.AdminHomeActionBean.">
                    <fmt:message key="manageRegistrations"/>
                </fmt:bundle>
            </stripes:link>


            <stripes:link beanclass="org.dataconservancy.ui.stripes.AdminUpdateRegistrationManagerActionBean"
                          title="Update Registrations">
                <fmt:bundle basename="StripesResources" prefix="org.dataconservancy.ui.stripes.AdminHomeActionBean.">
                    <fmt:message key="updateRegistrations"/>
                </fmt:bundle>
            </stripes:link>
        </div>


        <h2 class="SectionHeader">Configuration Settings</h2>
        <div class="SectionContent">
            <stripes:link beanclass="org.dataconservancy.ui.stripes.UiConfigurationActionBean"
                          title="Instance Configuration">
                <fmt:bundle basename="StripesResources" prefix="org.dataconservancy.ui.stripes.AdminHomeActionBean.">
                    <fmt:message key="manageConfiguration"/>
                </fmt:bundle>
            </stripes:link>
        </div>

    	<h2 class="SectionHeader">Projects</h2>
    	<div class="SectionContent">
            <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean"
                          title="Add project" event="addUserProject">
				<stripes:param name="forwardRequestSource" value="/pages/adminhome.jsp"/>
				Add project
			</stripes:link>
            <c:if test="${!empty actionBean.allProjects}">
                <table>
                    <tr>
                        <th>Name</th>
                        <th>Award Number(s)</th>
                        <th>Action</th>
                    </tr>

                    <c:forEach items="${actionBean.allProjects}" var="project">
		        	    <tr>
          				    <td>
                                <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean" title="${project.name}" event="viewUserProject">
                                    <stripes:param name="selectedProjectId" value="${project.id}"/>${project.name}</stripes:link>
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
                                <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean" title="Edit" event="editUserProject">
                                    <stripes:param name="selectedProjectId" value="${project.id}"/>
                                    <stripes:param name="forwardRequestSource" value="/pages/adminhome.jsp"/>
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
