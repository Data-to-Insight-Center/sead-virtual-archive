<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
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
    <stripes:layout-component name="bread">
        <stripes:label name="Project: "/> 
            <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean" title="${actionBean.project.name}" event="viewUserProject">
                <stripes:param name="selectedProjectId" value="${actionBean.project.id}"/>
                ${actionBean.project.name}
            </stripes:link>
    </stripes:layout-component>
    <stripes:layout-component name="flash">
       <stripes:messages key="UserInputMessages"/>
    </stripes:layout-component>
    <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
       <fmt:message key="projectActivitiesDesc" var="projectActivitiesDesc"/>
    </fmt_rt:bundle>
	<stripes:layout-component name="pageTitle">
	   Project Activities Log <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" class="tip" title="${projectActivitiesDesc}"/>       
    </stripes:layout-component>

    <stripes:layout-component name="contents">
	    <stripes:form beanclass="org.dataconservancy.ui.stripes.ProjectActivityActionBean">
	        <div class="SectionContent">
                <c:choose>
                    <c:when test="${!empty actionBean.activities}" >
                        <display:table name="${actionBean.activities}" id="activitiesTable" requestURI="/project/projectactivitieslog.action" pagesize="5" size="${totalActivitiesListSize}" >
                            <display:column title="Date" property="dateOfOccurrence" sortable="true" style="width: 15%; text-align: center;"/>
                            <display:column title="Activity Type" sortable="true" style="width: 8%; text-align: center;">
                                <c:choose>
                                    <c:when test="${activitiesTable.type == 'COLLECTION_DEPOSIT'}">
                                        <img class="activity-icon" src="https://scm.dataconservancy.org/issues/images/icons/newfeature.gif"
                                        alt="New Collection"
                                        title="Collection created.">
                                    </c:when>
                                    <c:when  test="${activitiesTable.type == 'DATASET_DEPOSIT'}">
                                        <img class="activity-icon" src="https://scm.dataconservancy.org/issues/images/icons/improvement.gif"
                                        alt="Data Added"
                                        title="New data is deposited.">
                                    </c:when>
                                </c:choose>
                            </display:column>
                            <display:column title="Description of Activity" property="description" sortable="false"/>
                            <display:column title="By" sortable="true" style="width: 20%; text-align:center;">
                                ${activitiesTable.actor.firstNames} ${activitiesTable.actor.lastNames}
                            </display:column>
                        </display:table>
                    </c:when>
                    <c:otherwise>
                        This project has no activities log.
                    </c:otherwise>
                </c:choose>
            </div>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>
