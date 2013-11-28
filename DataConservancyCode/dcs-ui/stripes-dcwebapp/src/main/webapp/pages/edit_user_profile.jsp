<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
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
       <stripes:messages key="UserInputMessages"/>
     </stripes:layout-component>
    
    <stripes:layout-component name="contents">
          		
	        <stripes:form beanclass="org.dataconservancy.ui.stripes.UserProfileActionBean">

		        <fieldset>
		            <div class="content-one-col">
		                <div>
                            <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                <fmt:message key="namesDesc" var="namesDesc"/>
                            </fmt_rt:bundle>
                            <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${namesDesc}" class="tip"/>
                            <br />
                        </div>
                        <div class="creator-prefix">
		        			<stripes:label name="prefix" for="prefix"/>
		        		</div>
		        		<div class="creator-namefield">
		        			<stripes:label name="firstNames" for="firstNames"/>
		        		</div>
		        		<div class="creator-namefield">
		        			<stripes:label name="middleNames" for="middleNames"/>
		        		</div>
		        		<div class="creator-namefield">
		        			<stripes:label name="lastNames" for="lastNames"/>
		        		</div>
		        		<div class="creator-suffix">
		        			<stripes:label name="suffix" for="suffix"/>
		        		</div>
		        		<div>
		        		    <div class="creator-prefix">
		        			    <stripes:text name="editedPerson.prefix" class="creator-textbox" value="${actionBean.authenticatedUser.prefix}"/>
							    <stripes:errors field="editedPerson.prefix"/>
		        		    </div>
		        		    <div class="creator-namefield">
		        			    <stripes:text name="editedPerson.firstNames" class="creator-textbox" value="${actionBean.authenticatedUser.firstNames}" disabled="${actionBean.authenticatedUser.readOnly}"/>
							    <stripes:errors field="editedPerson.firstNames"/>
		        		    </div>
		        		    <div class="creator-namefield">
		        			    <stripes:text name="editedPerson.middleNames" class="creator-textbox" value="${actionBean.authenticatedUser.middleNames}"/>
							    <stripes:errors field="editedPerson.middleNames"/>
		        		    </div>
		        		    <div class="creator-namefield">
		        			    <stripes:text name="editedPerson.lastNames" class="creator-textbox" value="${actionBean.authenticatedUser.lastNames}" disabled="${actionBean.authenticatedUser.readOnly}"/>
		            		    <stripes:errors field="editedPerson.lastNames"/>
		        		    </div>
		        		    <div class="creator-suffix">
		        			    <stripes:text name="editedPerson.suffix" class="creator-textbox" value="${actionBean.authenticatedUser.suffix}"/>
   							    <stripes:errors field="editedPerson.suffix"/>
		        		    </div>
		        		</div>		        	
		        	</div>
		        	<div class="content-left-col">
		        		<div>
		        			<stripes:label name="email" for="email"/>
	                    	<stripes:text name="editedPerson.emailAddress" value="${actionBean.authenticatedUser.emailAddress}" disabled="${actionBean.authenticatedUser.readOnly}"/>
		            		<stripes:errors field="editedPerson.emailAddress"/>
		        		</div>
		        		<div>
		        			<stripes:label name="phoneNumber" for="phoneNumber"/>
	                    	<stripes:text name="editedPerson.phoneNumber" value="${actionBean.authenticatedUser.phoneNumber}"/>
		            		<stripes:errors field="editedPerson.phoneNumber"/>
		        		</div>
		        		<div>
		        			<stripes:label name="password" for="password"/>
	                    	<stripes:password name="editedPerson.password" disabled="${actionBean.authenticatedUser.readOnly}"/>
	        	    		<stripes:errors field="editedPerson.password"/>
		        		</div>
		        		<div>
		        			<stripes:label name="confirmedPassword" for="confirmedPassword"/>
	                    	<stripes:password name="confirmedPassword" disabled="${actionBean.authenticatedUser.readOnly}"/>
	        	    		<stripes:errors field="confirmedPassword"/>
		        		</div>
		        		<div>
		        			<stripes:label name="jobTitle" for="jobTitle"/>
	                    	<stripes:text name="editedPerson.jobTitle" value="${actionBean.authenticatedUser.jobTitle}"/>
		            		<stripes:errors field="editedPerson.jobTitle"/>
		        		</div>
		        		<div>
		        			<stripes:label name="department" for="department"/>
	                    	<stripes:text name="editedPerson.department" value="${actionBean.authenticatedUser.department}"/>
		            		<stripes:errors field="editedPerson.department"/>
		        		</div>
		        		<div>
		        			<stripes:label name="instCompany" for="instCompany"/>
	                    	<stripes:text name="editedPerson.instCompany" value="${actionBean.authenticatedUser.instCompany}"/>
		            		<stripes:errors field="editedPerson.instCompany"/>
		        		</div>
		        		<div>
                            <stripes:label name="instCompanyWebsite" for="instCompanyWebsite"/>
                            <stripes:text name="editedPerson.instCompanyWebsite" value="${actionBean.authenticatedUser.instCompanyWebsite}"/>
                            <stripes:errors field="editedPerson.instCompanyWebsite"/>
                        </div>
		        		<div>
		        			<stripes:label name="city" for="city"/>
	                    	<stripes:text name="editedPerson.city" value="${actionBean.authenticatedUser.city}"/>
		            		<stripes:errors field="editedPerson.city"/>
		        		</div>
		        		<div>
		        			<stripes:label name="state" for="state"/>
	                    	<stripes:text name="editedPerson.state" value="${actionBean.authenticatedUser.state}"/>
		            		<stripes:errors field="editedPerson.state"/>
		        		</div>
		        		<div>
		        			<stripes:label name="bio" for="bio"/>
	                    	<stripes:textarea name="editedPerson.bio" value="${actionBean.authenticatedUser.bio}"/>
		            		<stripes:errors field="editedPerson.bio"/>
		        		</div>
		        		<div>
		        			<stripes:label name="preferredPubName" for="preferredPubName"/>
		        			<div class="help-icon3">
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="preferredPubNameDesc" var="preferredPubNameDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${preferredPubNameDesc}" class="tip"/>
                            </div>
	                    	<stripes:text name="editedPerson.preferredPubName" value="${actionBean.authenticatedUser.preferredPubName}"/>
		            		<stripes:errors field="editedPerson.preferredPubName"/>
		        		</div>
		        		<div>
		        			<stripes:label name="website" for="website"/>
		        			<div class="help-icon2">
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="websiteDesc" var="websiteDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${websiteDesc}" class="tip"/>
                            </div>
	                    	<stripes:text name="editedPerson.website" value="${actionBean.authenticatedUser.website}"/>
		            		<stripes:errors field="editedPerson.website"/>
		        		</div>
		        	</div>
                    <!-- TODO: separate out password change --->
	        	</fieldset>
	        	
	        	<fieldset>
                    <stripes:hidden name="editedPerson.id" value="${actionBean.authenticatedUser.id}"/>
	        	    <stripes:submit name="userProfileUpdated" value="Update profile" disabled="${actionBean.authenticatedUser.readOnly}"/>
	        	    <stripes:submit name="viewUserProfile" value="Cancel"/>
	        	</fieldset>
	        </stripes:form>
	        
    </stripes:layout-component>
</stripes:layout-render>
