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
<script type="text/javascript">
    function setFocus(textBox){
        textBox.focus();
    }
</script>

<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
    <stripes:layout-component name="flash">
       <stripes:messages key="UserInputMessages"/>
     </stripes:layout-component>
    <stripes:layout-component name="contents">
		<div class="SectionContent">	
	        <stripes:form beanclass="org.dataconservancy.ui.stripes.RegistrationActionBean">
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
		        			    <stripes:text name="user.prefix" class="creator-textbox"/>
							    <stripes:errors field="user.prefix"/>
		        		    </div>
		        		    <div class="creator-namefield">
		        			    <stripes:text name="user.firstNames" class="creator-textbox"/>
							    <stripes:errors field="user.firstNames"/>
		        		    </div>
		        		    <div class="creator-namefield">
		        			    <stripes:text name="user.middleNames" class="creator-textbox"/>
							    <stripes:errors field="user.middleNames"/>
		        		    </div>
		        		    <div class="creator-namefield">
		        			    <stripes:text name="user.lastNames" class="creator-textbox"/>
		            		    <stripes:errors field="user.lastNames"/>
		        		    </div>
		        		    <div class="creator-suffix">
		        			    <stripes:text name="user.suffix" class="creator-textbox"/>
   							    <stripes:errors field="user.suffix"/>
		        		    </div>
		        		</div>		        	
		        	</div>
		        	<div class="content-left-col">
		        		<div>
		        			<stripes:label name="email" for="email"/>
	                    	<stripes:text class="label-field-group" name="user.emailAddress"/>
		            		<stripes:errors field="user.emailAddress"/>
		        		</div>
		        		<div>
		        			<stripes:label name="phone" for="phone"/>
	                    	<stripes:text class="label-field-group" name="user.phoneNumber"/>
		            		<stripes:errors field="user.phoneNumber"/>
		        		</div>
		        		<div>
		        			<stripes:label name="password" for="password"/>
	                    	<stripes:password name="user.password" title="Your password must be between 5 and 20 characters in length." class="label-field-group"/>
	        	    		<stripes:errors field="user.password"/>
		        		</div>
		        		<div>
		        			<stripes:label name="confirmedPassword" for="confirmedPassword"/>
	                    	<stripes:password class="label-field-group" name="confirmedPassword"/>
	        	    		<stripes:errors field="confirmedPassword"/>
		        		</div>
		        		<div>
		        			<stripes:label name="jobTitle" for="jobTitle"/>
	                    	<stripes:text class="label-field-group" name="user.jobTitle"/>
		            		<stripes:errors field="user.jobTitle"/>
		        		</div>
		        		<div>
		        			<stripes:label name="department" for="department"/>
	                    	<stripes:text class="label-field-group" name="user.department"/>
		            		<stripes:errors field="user.department"/>
		        		</div>
		        		<div>
		        			<stripes:label name="instCompany" for="instCompany"/>
	                    	<stripes:text class="label-field-group" name="user.instCompany"/>
		            		<stripes:errors field="user.instCompany"/>
		        		</div>
		        		<div>
                            <stripes:label name="instCompanyWebsite" for="instCompanyWebsite"/>
                            <stripes:text class="label-field-group" name="user.instCompanyWebsite"/>
                            <stripes:errors field="user.instCompanyWebsite"/>
                        </div>
		        		<div>
		        			<stripes:label name="city" for="city"/>
	                    	<stripes:text class="label-field-group" name="user.city"/>
		            		<stripes:errors field="user.city"/>
		        		</div>
		        		<div>
		        			<stripes:label name="state" for="state"/>
	                    	<stripes:text class="label-field-group" name="user.state"/>
		            		<stripes:errors field="user.state"/>
		        		</div>
		        		<div class="label-field-group">
		        			<stripes:label name="bio" for="bio"/>
	                    	<stripes:textarea name="user.bio"/>
		            		<stripes:errors field="user.bio"/>
		        		</div>
		        		<div>
		        			<stripes:label name="preferredPubName" for="preferredPubName"/>
	                    	<div class="help-icon3">
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="preferredPubNameDesc" var="preferredPubNameDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${preferredPubNameDesc}" class="tip"/>
                            </div>
	                    	<stripes:text class="label-field-group" name="user.preferredPubName"/>
		            		<stripes:errors field="user.preferredPubName"/>
		        		</div>
		        		<div>
		        			<stripes:label name="website" for="website"/>
						    <div class="help-icon2">
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="websiteDesc" var="websiteDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${websiteDesc}" class="tip"/>
                            </div>
	                    	<stripes:text class="label-field-group" name="user.website"/>
		            		<stripes:errors field="user.website"/>
		        		</div>
		        	</div>
		        	
		        	<div class="content-right-col">  	</div>
        			 
	        	</fieldset>
	        	<fieldset>
	        	 	<stripes:submit name="register" value="Create Account" class="register_button"/>
	        	</fieldset>
	        	<div>
	        		* denotes required fields.
	        	</div>
	        </stripes:form>
	    </div>
    </stripes:layout-component>
</stripes:layout-render>
