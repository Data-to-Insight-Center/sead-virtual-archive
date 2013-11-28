<%@ page import="org.dataconservancy.ui.model.RegistrationStatus" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
            <stripes:messages key="UserInputMessages"/>
        </stripes:layout-component>
        <stripes:layout-component name="contents">
            <div class="SectionContent">
                <stripes:form beanclass="org.dataconservancy.ui.stripes.AdminUpdateRegistrationManagerActionBean">
                Change Status to:
                <stripes:select name="registrationStatus">
                    <stripes:options-collection collection="<%=RegistrationStatus.values()%>"/>
                </stripes:select>

                <div><stripes:errors field="registrationStatus"/></div>
                <table>


                <tr>
                    <th></th>
                    <th>ID</th>
                    <th>First Names</th>
                    <th>Last Names</th>
                    <th>Status</th>
                </tr>

                <c:forEach items="${actionBean.pendingRegistrations}" var="person">
                <tr>
                    <td align="center">
                        <stripes:checkbox name="userIdsToUpdate" value="${person.emailAddress}"/>
                    </td>
                    <td>"${person.emailAddress}"
                    </td>
                    <td>"${person.firstNames}"
                    </td>
                    <td>"${person.lastNames}"
                    </td>
                    <td>"${person.registrationStatus}"
                    </td>
                </tr>
                </c:forEach>
                <c:forEach items="${actionBean.approvedRegistrations}" var="person">
                    <tr>
                        <td align="center">
                            <stripes:checkbox name="userIdsToUpdate" value="${person.emailAddress}"/>
                        </td>
                        <td>"${person.emailAddress}"
                        </td>
                        <td>"${person.firstNames}"
                        </td>
                        <td>"${person.lastNames}"
                        </td>
                        <td>"${person.registrationStatus}"
                        </td>
                </tr>
                </c:forEach>
                <c:forEach items="${actionBean.blacklistedRegistrations}" var="person">
                <tr>
                    <td align="center">
                        <stripes:checkbox name="userIdsToUpdate" value="${person.emailAddress}"/>
                    </td>
                    <td>"${person.emailAddress}"
                    </td>
                    <td>"${person.firstNames}"
                    </td>
                    <td>"${person.lastNames}"
                    </td>
                    <td>"${person.registrationStatus}"
                    </td>
                 </tr>
                 </c:forEach>
              </table>
              <div><stripes:errors field="userIdsToUpdate"/></div>
            <stripes:submit name="updateRegistrations" value="Update"/>
            <br/>
            <stripes:link beanclass="org.dataconservancy.ui.stripes.AdminHomeActionBean"
                              title="Back to Admin home page">
                        <fmt:bundle basename="StripesResources" prefix="org.dataconservancy.ui.stripes.AdminRegistrationManagerActionBean.">
                            <fmt:message key="cancel"/>
                        </fmt:bundle>
            </stripes:link>
            </stripes:form>
        </div>
    </stripes:layout-component>
   </stripes:layout-render>
