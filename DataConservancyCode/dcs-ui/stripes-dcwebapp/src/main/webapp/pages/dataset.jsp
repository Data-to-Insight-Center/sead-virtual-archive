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
     </stripes:layout-component>
     <stripes:layout-component name="contents">
       <stripes:form beanclass="org.dataconservancy.ui.stripes.UserDataSetsBean">
	      <table>
	         <tr><th>ID</th><th>Name</th><th>Description</th>
		     <c:forEach items="${actionBean.userData}" var="DataSet">
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
        <stripes:submit name="close" value="Close"/>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>