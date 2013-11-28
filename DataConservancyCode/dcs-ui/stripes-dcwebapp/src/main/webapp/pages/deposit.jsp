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
	<stripes:layout-component name="bread">
		<stripes:label name="Project: "/> 
		<stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean"
			title="${actionBean.projectForCurrentCollection.name}" event="viewUserProject">
            <stripes:param name="selectedProjectId" value="${actionBean.projectForCurrentCollection.id}"/>
            ${actionBean.projectForCurrentCollection.name}
		</stripes:link>
		<stripes:label name="Collection: "/> 
		<stripes:link beanclass="org.dataconservancy.ui.stripes.UserCollectionsActionBean"
			title="Title" event="viewCollectionDetails">
			<stripes:param name="selectedCollectionId" value="${actionBean.currentCollection.id}"/>
			${actionBean.currentCollection.title}
		</stripes:link>
    </stripes:layout-component>
    <stripes:layout-component name="flash">
		<stripes:messages key="UserInputMessages"/>
    </stripes:layout-component>
    <stripes:layout-component name="contents">
		<div class="content-left-col">
			<stripes:form beanclass="org.dataconservancy.ui.stripes.DepositActionBean" class="DataDepositForm">
				<h2 class="SectionHeader">Select file(s) to deposit to:</h2>
				<div class="SectionContent">
					<fieldset>
						<div class="label-field-group">
							<stripes:file name="uploadedFile"/>
							<stripes:errors field="uploadedFile"/>
							${actionBean.maxSizeInfoMessage}
						</div>
						<div class="checkbox-grouping">
							<stripes:checkbox name="isContainer"/>
							<stripes:label  for="chkbx_isContainer"/>
						</div>
					</fieldset>
					<fieldset>
						<stripes:hidden name="currentCollectionId"/>
					</fieldset>
					<fieldset>
						<stripes:submit name="deposit" value="Deposit"/>
						<stripes:submit name="cancel" value="Cancel"/>
					</fieldset>
				</div>
				<stripes:hidden name="redirectUrl"/>
			</stripes:form>
		</div>
	</stripes:layout-component>
</stripes:layout-render>
