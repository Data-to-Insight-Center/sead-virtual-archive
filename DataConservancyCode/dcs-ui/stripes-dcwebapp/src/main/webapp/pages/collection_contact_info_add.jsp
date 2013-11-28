<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
   
            <h2 class="SectionHeader">Add Contact Info</h2>
            <div class="SectionContent">
                <stripes:form beanclass="${actionBean.class}" class="AddCollectionContactInfoForm">
                    <stripes:hidden id="collectionId" name="collectionId"/>

                    <p>
                        <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                            <fmt:message key="formLabel"/>
                        </fmt_rt:bundle>
                    </p>
               
                    <fieldset class="contactInfo">	
                    	<div class="content-left-col">
                    		<div class="label-field-group">
                    			<stripes:label name="FullName" for="ci_fullName"/>
	                    		<stripes:text name="contactInfo.name" value="" />
						    	<stripes:errors field="contactInfo.name"/>
                    		</div>
                    		<div class="label-field-group">
                    			<stripes:label name="Roles" for="ci_roles"/>
	                    		<stripes:text name="contactInfo.role" value="" />
                    		</div>
                    		<div class="label-field-group">
                    			<stripes:label name="StreetAddress" for="ci_streetAddress"/>
	                    		<stripes:text name="contactInfo.physicalAddress.streetAddress" value="" />
                    		</div>
                    		<div class="address-grouping-city">
                    			<stripes:label name="city" for="ci_city"/>
	                    		<stripes:text name="contactInfo.physicalAddress.city" value="" />
                    		</div>
                    		<div class="address-grouping-state">
                    			<stripes:label name="state" for="ci_state"/>
								<stripes:select name="contactInfo.physicalAddress.state" size="1">
							 		 <stripes:option value=""/>
							 		 <stripes:options-collection collection="${stateHelper.allStates}"
							    	label="code" value="code"/>
								</stripes:select>
							</div>
							<div class="address-grouping-zip">
							    <stripes:label name="zip" for="ci_zip"/>
								<stripes:text name="contactInfo.physicalAddress.zipCode" value="" />
							</div>                     
							<div class=content-right-col>
							</div>
                    		<div class="label-field-group">
                    			<stripes:label name="Country" for="ci_country"/>
	                    		<stripes:text name="contactInfo.physicalAddress.country" value="" />
                    		</div>
                    		<div class="label-field-group">
                    			<stripes:label name="Email" for="ci_email"/>
	                    		<stripes:text name="contactInfo.emailAddress" value="" />
							<stripes:errors field="contactInfo.emailAddress"/>
                    		</div>
                    		<div class="label-field-group">
                    			<stripes:label name="Phone" for="ci_phone"/>
								<stripes:text name="contactInfo.phoneNumber" value="" />
							</div>
                    	</div> 
							
						<div class=content-right-col>
						</div>
				    </fieldset>

				    <fieldset>
				        <stripes:submit name="saveAndAddMoreContactInfo" value="Save and Add more" class="submitBtn" />
				        
				    </fieldset>
				    <fieldset>
              		    <stripes:submit name="displayCollectionForm" value="Cancel"/>
                        <stripes:submit name="saveAndDoneContactInfo" value="Save and Go back"/>
                    </fieldset>
			</stripes:form>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
