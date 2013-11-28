<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="fmt_rt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

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
<script type="text/javascript">
	function addAdminRow(divID) {

    	var parent = document.getElementById(divID);
    	var index = parent.getElementsByTagName("input").length ;

    	var element1 = document.createElement("input");
    	element1.type = "text";
    	element1.name = "projectAdminIDList[" + index + "]";
    	parent.appendChild(element1);
	}
	
	function addAwardRow(parentID) {

		var parent = document.getElementById(parentID);
		var index = parent.getElementsByTagName("input").length ;

		var element = document.createElement("input");
		element.type = "text";
		element.name = "project.numbers[" + index + "]"
		parent.appendChild(element);
	}
	
	function removeAdminRow(id) {
	    alert("Called?");
	    for (var i = 0; i < document.getElementsByTagName("input").length; i++) {
	        document.getElementById(id).remove();
	    }
    }
        
</script>
<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
  <stripes:layout-component name="flash">
    <stripes:messages key="updated"/>
  </stripes:layout-component>
  <stripes:layout-component name="contents">

        <h2 class="SectionHeader">Editing ${actionBean.project.name} project</h2>
    
            <div class="SectionContent">
            <stripes:form beanclass="org.dataconservancy.ui.stripes.ProjectActionBean" class="ProjectForm">
                <p>
                    <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                        <fmt:message key="formLabel"/>
                    </fmt_rt:bundle>
                </p>
                
                <fieldset>
                    <legend>
                        <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                            <fmt:message key="fieldsetLegend"/>
                        </fmt_rt:bundle>
                    </legend>
					<div class="content-left-col">
						<div class="tooltip-field-group">
                            <stripes:label name="projectName" for="projectName"/>
                            <div class="help-icon">
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="projectNameDesc" var="projectNameDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${projectNameDesc}" class="tip"/>
                            </div>
                            <stripes:text name="project.name"/>
                            <stripes:errors field="project.name"/>
                        </div>
                        <div class="tooltip-field-group">
                            <stripes:label name="projectDescription" for="projectDescription"/>
                            <div class="help-icon">
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="projectDescriptionDesc" var="projectDescriptionDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${projectDescriptionDesc}" class="tip"/>
                            </div>
                            <stripes:text name="project.description"/>
                            <stripes:errors field="project.description"/>
                        </div>
                        <div class="tooltip-field-group">
                            <div id="numbersList">
                                <stripes:label name="projectNumbers" for="projectNumbers"/>
                                <div class="help-icon">
                                    <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                        <fmt:message key="projectAwardNumberToolTip" var="projectAwardNumberToolTipVar"/>
                                    </fmt_rt:bundle>
                                    <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${projectAwardNumberToolTipVar}" class="tip"/>
                                </div>
                                <c:choose>
                                    <c:when test="${fn:length(project.numbers) == 0}">
                                        <stripes:text name="project.numbers[0]"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${project.numbers}" var="number" varStatus="status">
                                            <c:choose>
                                                <c:when test="${(empty number)}">
                                                    <stripes:text name="project.numbers[${status.index}]" value="${number}"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <stripes:text name="project.numbers[${status.index}]" value="${number}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div>
                                <stripes:button name="addMoreNumbers" value="Add More Award Numbers"
                                            onclick="addAwardRow('numbersList');"
                                            class="linkButton" />
                            </div>
                            <stripes:errors field="project.numbers[0]"/>
                        </div>
                        <div class="tooltip-field-group">
                            <stripes:label name="fundingEntity" for="projectFundingEntity"/>
                            <div class="help-icon">
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="projectFundingDesc" var="projectFundingDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${projectFundingDesc}" class="tip"/>
                            </div>
                            <stripes:text name="project.fundingEntity"/>
                            <stripes:errors field="project.fundingEntity"/>
                        </div>
                        <div class="tooltip-field-group">
                            <stripes:label name="startDate" for="projectStartDate"/>
                            <div class="help-icon">
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="projectStartDateDesc" var="projectStartDateDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${projectStartDateDesc}" class="tip"/>
                            </div>
                            <stripes:text name="project.startDate"/>
                            <stripes:errors field="project.startDate"/>
                        </div>
                        <div class="tooltip-field-group">
                            <stripes:label name="endDate" for="projectEndDate"/>
                            <div class="help-icon">
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="projectEndDateDesc" var="projectEndDateDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${projectEndDateDesc}" class="tip"/>
                            </div>
                            <stripes:text name="project.endDate"/>
                            <stripes:errors field="project.endDate"/>
                        </div>
                        <div class="tooltip-field-group">
                             <div id="pisList">
                                <stripes:label name="projectPis" for="projectPis"/>
                                <div class="help-icon">
                                    <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                        <fmt:message key="projectAdminDescription" var="projAdminDesc"/>
                                    </fmt_rt:bundle>
                                    <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" class="tip" title="${projAdminDesc}"/>
                                </div>
                                <stripes:text name="currentUserAdmin" value="${actionBean.authenticatedUser.emailAddress}" disabled="true"/>
                                <c:if test="${fn:length(actionBean.projectAdminList) != '0'}">
                                    <c:forEach items="${actionBean.projectAdminList}" var="pi" varStatus="status">
                                      <!-- Ignoring the admin from the list and displaying others. -->
                                      <c:if test="${pi.firstNames != 'admin'}">
                                          <stripes:text id="${pi.id}" name="projectAdminList[${status.index}]" />
                                          <stripes:button id="${pi.id}" name="removePi" value="Remove Project Admin"
                                            onclick="removeAdminRow('${pi.id}');"
                                            class="linkButton" />
                                      </c:if>
                                    </c:forEach>
                                </c:if>
                                <c:if test="${fn:length(actionBean.projectAdminIDList) != '0'}">
                                    <c:forEach items="${actionBean.projectAdminIDList}" var="pi" varStatus="status">
                                        <stripes:text name="projectAdminIDList[${status.index}]"/>
                                    </c:forEach>
                                </c:if>                                
                            </div>
                            <stripes:button name="addMorePis" value="Add More Project Administrators"
                                            onclick="addAdminRow('pisList');"
                                            class="linkButton" />
                        </div>
					</div>
                </fieldset>

                <stripes:hidden name="forwardRequestSource"/>
                <stripes:hidden name="project.id"/>
                
                <fieldset>
                    <stripes:submit name="userProjectUpdated" value="Update project"/>
	      			<stripes:submit name="cancel" value="Cancel"/>
	      			<stripes:link beanclass="org.dataconservancy.ui.stripes.AddCollectionActionBean" title="Add Collection" event="displayCollectionForm"><stripes:param name="projectId" value="${actionBean.project.id}"/> Add Collection </stripes:link>
                </fieldset>
            </stripes:form>
            </div>
  </stripes:layout-component>
</stripes:layout-render>
