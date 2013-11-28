<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
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
    <stripes:layout-component name="contents">
    	<stripes:form beanclass="org.dataconservancy.ui.stripes.UiConfigurationActionBean">
        <span class="SectionHeader">Validation Result</span>
        <div class="SectionContent">
        	<fieldset>
        		<div id="schemaName">
        		<b>Schema Name:</b> ${actionBean.metadataFormatDescription.name}<br>  
        		</div>
        		<div id="schemaVersion">      		
        		<b>Version:</b> ${actionBean.metadataFormatDescription.version}<br><br>
        		</div>
        		<div id="namespaces">
        		<b>Namespaces:</b><br>
        		<c:forEach items="${actionBean.metadataFormatDescription.namespaces}" var="namespace">   
        			<div id="namespace">     			
        			<b>Namespace:</b> ${namespace.name}
        			<c:if test="${!empty namespace.prefix}">
        				<b>Prefix:</b> ${namespace.prefix}
        			</c:if>        			
        			<br>
        			</div>
        		</c:forEach>
        		</div>
        		<br/>
        		<b>Event Log:</b><br>
        		<c:forEach items="${actionBean.schemaValidationResult.metadataValidationErrors}" var="validationError">
        			<font color="red">${validationError.message}</font>
        			<br/>
        		</c:forEach>
				<c:forEach items="${actionBean.schemaValidationResult.metadataValidationSuccesses}" var="validationSuccess">
        			<font color="green">${validationSuccess.message}</font>        			
        			<br/>
        		</c:forEach>
    		</fieldset>   
    		<fieldset>
     			<c:if test="${empty actionBean.schemaValidationResult.metadataValidationErrors}">
        			<h2 class="SectionHeader">Validate Sample Files against the Schema:(Optional)</h2>
					<div class="label-field-group">			
						
						<stripes:link beanclass="org.dataconservancy.ui.stripes.ValidatingMetadataFileActionBean"
                               title="${actionBean.metadataFormatDescription.name}" event="validatingMetadataFile">
                               <stripes:param name="metadataFormatName" value="${actionBean.metadataFormatDescription.name}"/>
                               <stripes:param name="metadataFormatId" value="${actionBean.metadataFormatDescription.id}"/>
                               <stripes:param name="redirectUrl" value="displaySchemaValidationResults"/>
                               Validate Sample Files
               			</stripes:link>
					</div>
					
				</c:if>
			</fieldset>
			<fieldset>
				<c:if test="${empty actionBean.schemaValidationResult.metadataValidationErrors}">
					<stripes:submit name="saveNewFormat" value="Save"/>
				</c:if>
				<stripes:submit name="cancel" value="Cancel"/>
			</fieldset>   
		</div>
		</stripes:form>
    </stripes:layout-component>
</stripes:layout-render>