<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="fmt_rt" uri="http://java.sun.com/jstl/fmt_rt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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
    <stripes:layout-component name="success">
        <stripes:messages key="success"/>
    </stripes:layout-component>
    <stripes:layout-component name="error">
        <stripes:messages key="failure"/>
    </stripes:layout-component>
    <stripes:layout-component name="contents">
      	<h2 class="SectionHeader">Viewing user profile</h2>
      
      	<div class="SectionContent">  
			<table> 
           		<tr>
           		   <th class="vertical">Registration status:</th>
           		   <td> ${actionBean.authenticatedUser.registrationStatus}</td>
           		</tr>
           		<fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                       <fmt:message key="namesDesc" var="namesDesc"/>
                </fmt_rt:bundle>
           		<tr>
           		   <th class="vertical">Prefix: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${namesDesc}" class="tip"/></th>
           		   <td> ${actionBean.authenticatedUser.prefix}</td>
           		</tr>
	       		<tr>
	       		   <th class="vertical">First Names: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${namesDesc}" class="tip"/></th>
	       		   <td> ${actionBean.authenticatedUser.firstNames}</td>
	       		</tr>
	       		<tr>
	       		   <th class="vertical">Middle Names: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${namesDesc}" class="tip"/></th>
	       		   <td> ${actionBean.authenticatedUser.middleNames}</td>
	       		</tr>
		   		<tr>
		   		   <th class="vertical">Last Names: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${namesDesc}" class="tip"/></th>
		   		   <td> ${actionBean.authenticatedUser.lastNames}</td>
		   		</tr>
		   		<tr>
		   		   <th class="vertical">Suffix: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${namesDesc}" class="tip"/></th>
		   		   <td> ${actionBean.authenticatedUser.suffix}</td>
		   		</tr>
		    	<tr>
		    	   <th class="vertical">Email:</th>
		    	   <td> ${actionBean.authenticatedUser.emailAddress}</td>
		    	</tr>
		    	<tr>
		    	   <th class="vertical">Phone Number:</th>
		    	   <td> ${actionBean.authenticatedUser.phoneNumber}</td>
		    	</tr>
		    	<tr>
		    	   <th class="vertical">Job Title:</th>
		    	   <td> ${actionBean.authenticatedUser.jobTitle}</td>
		    	</tr>
		    	<tr>
		    	   <th class="vertical">Department:</th>
		    	   <td> ${actionBean.authenticatedUser.department}</td>
		    	</tr>
		    	<tr>
		    	   <th class="vertical">Institution/Company:</th>
		    	   <td> ${actionBean.authenticatedUser.instCompany}</td>
		    	</tr>
		    	<tr>
                   <th class="vertical">Institution/Company Website:</th>
                   <td> ${actionBean.authenticatedUser.instCompanyWebsite}</td>
                </tr>
		    	<tr>
		    	   <th class="vertical">City:</th>
		    	   <td> ${actionBean.authenticatedUser.city}</td>
		    	</tr>
		    	<tr>
		    	   <th class="vertical">State:</th>
		    	   <td> ${actionBean.authenticatedUser.state}</td>
		    	</tr>
		    	<tr>
		    	   <th class="vertical">Bio:</th>
		    	   <td> ${actionBean.authenticatedUser.bio}</td>
		    	</tr>
				<tr>
				   <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                      <fmt:message key="preferredPubNameDesc" var="preferredPubNameDesc"/>
                   </fmt_rt:bundle>
				   <th class="vertical">Preferred Published Name: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${preferredPubNameDesc}" class="tip"/></th>
				   <td> ${actionBean.authenticatedUser.preferredPubName}</td>
				</tr>
		    	<tr>
                   <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                      <fmt:message key="websiteDesc" var="websiteDesc"/>
                   </fmt_rt:bundle>
                   <th class="vertical">Website: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${websiteDesc}" class="tip"/></th>
                   <td> ${actionBean.authenticatedUser.website}</td>
		    	</tr>
		    	<tr>
                   <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                      <fmt:message key="externalDesc" var="externalDesc"/>
                   </fmt_rt:bundle>
                   <th class="vertical">External Storage Linked? <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${externalDesc}" class="tip"/></th>
                   <c:choose>
                      <c:when test="${actionBean.authenticatedUser.externalStorageLinked == false}">
                          <td>No - <stripes:link beanclass="org.dataconservancy.ui.stripes.UserProfileActionBean" title="LinkDropbox" event="linkDropbox">Link Storage</stripes:link></td>    
                      </c:when>
                      <c:otherwise>
                          <td>Yes - <stripes:link beanclass="org.dataconservancy.ui.stripes.UserProfileActionBean" title="TestDropbox" event="testDropbox">Test</stripes:link></td>
                      </c:otherwise>
                   </c:choose>
                </tr>
		 	</table>
		 	<br/>
		  	<stripes:link beanclass="org.dataconservancy.ui.stripes.UserProfileActionBean"
                               title="Edit" event="editUserProfile">Edit profile</stripes:link>
      	</div>                      

    </stripes:layout-component>
</stripes:layout-render>
