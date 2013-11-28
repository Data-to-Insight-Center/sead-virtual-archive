<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="fmt_rt" uri="http://java.sun.com/jstl/fmt_rt" %>

<%@ page import="org.dataconservancy.ui.model.Person" %>
<%@ page import="org.dataconservancy.ui.model.Role" %>
<%@ page import="org.dataconservancy.ui.stripes.BaseActionBean" %>
<%@ page import="java.util.List" %>
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
    function showContactInfoDetails(name) {
        document.getElementById("details_" + name).style.display="block";
        document.getElementById("name_" + name).style.display="none";
    }
    function hideContactInfoDetails(name) {
        document.getElementById("details_" + name).style.display="none";
        document.getElementById("name_" + name).style.display="block";
    }
</script>

<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
    <c:set var="selectedCollection" scope="page" value='${actionBean.selectedCollection}'/>
    <stripes:layout-component name="bread">
    <c:if test="${empty selectedCollection.parentId}">
        <stripes:label name="Project: "/> 
               <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean"
                               title="${actionBean.projectForCurrentCollection.name}" event="viewUserProject">
                               <stripes:param name="selectedProjectId" value="${actionBean.projectForCurrentCollection.id}"/>
                               ${actionBean.projectForCurrentCollection.name}
               </stripes:link>
    </c:if>
    <c:if test="${!empty selectedCollection.parentId}">
        <stripes:label name="Parent Collection: "/> 
               <stripes:link beanclass="org.dataconservancy.ui.stripes.UserCollectionsActionBean"
                               title="${actionBean.parentCollectionName}" event="viewCollectionDetails">
                               <stripes:param name="selectedCollectionId" value="${actionBean.parentCollectionId}"/>
                               ${actionBean.parentCollectionName}
               </stripes:link>
    </c:if>
    </stripes:layout-component>
    <stripes:layout-component name="flash">
       <stripes:messages key="PermissionDenied"/>
       <c:forEach var="message" items="${inform}">
    		<div>${message}</div>
		</c:forEach>
    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
    	Collection Details
    </stripes:layout-component>
    <stripes:layout-component name="contents">

      <c:set var="authUser" scope="page" value='${actionBean.authenticatedUser}'/>
        <%
            Person authUser = (Person) pageContext.getAttribute("authUser", PageContext.PAGE_SCOPE);
            if (authUser != null) {
                List<Role> roles = authUser.getRoles();
                pageContext.setAttribute("isAdmin", roles.contains(Role.ROLE_ADMIN), PageContext.PAGE_SCOPE);
            }
        %>
      <c:choose>
        <c:when test="${actionBean.selectedCollectionDeposited}">      
    	<stripes:form beanclass="org.dataconservancy.ui.stripes.UserCollectionsActionBean">
	            <h2 class="SectionHeader">Metadata</h2>
	            <c:if test="${actionBean.canUpdateCollection}">
    	            <div class="SectionContent">
	                    <stripes:link beanclass="org.dataconservancy.ui.stripes.UpdateCollectionActionBean" event="displayCollectionUpdateForm">
                            <stripes:param name="collectionId" value="${selectedCollection.id}"/>
                            Edit metadata fields
                        </stripes:link>
    	            </div>
    	        </c:if>
	            <div class="SectionContent">

	                <table class="CollectionViewTable">
	                    <tr>
	                        <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                               <fmt:message key="collectionTitleDesc" var="collectionTitleDesc"/>
                            </fmt_rt:bundle>
	                        <th width="23%" align="right">Title: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${collectionTitleDesc}" class="tip"/></th><td>${selectedCollection.title}</td>
	                    </tr>
	                    <tr>
	                        <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                               <fmt:message key="collectionCitableLocDesc" var="collectionCitableLocDesc"/>
                            </fmt_rt:bundle>
	                        <th align="right">Citable Locator: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${collectionCitableLocDesc}" class="tip"/></th>
	                        <td>
                                <c:if test="${actionBean.isAdminForProject == true && empty selectedCollection.citableLocator}">
                                    <stripes:link beanclass="org.dataconservancy.ui.stripes.CitableLocatorActionBean" title="Reserve a citable locator from server" event="reserveDOI">
                                        <stripes:param name="collectionId" value="${selectedCollection.id}"/>
                                        Get citable locator
                                    </stripes:link>
                                </c:if>
	                            ${selectedCollection.citableLocator}
	                        </td>
	                    </tr>
	                    <tr>
	                        <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                               <fmt:message key="collectionPubDateDesc" var="collectionPubDateDesc"/>
                            </fmt_rt:bundle>
	                        <th align="right">Publication Date: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${collectionPubDateDesc}" class="tip"/> </th><td>${actionBean.selectedCollectionPublicationDateString}</td>
	                    </tr>
	
	                    <tr>
	                        <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                               <fmt:message key="collectionContactsDesc" var="collectionContactsDesc"/>
                            </fmt_rt:bundle>
	                        <th colspan="1" rowspan="${fn:length(selectedCollection.contactInfoList)}" align="right">Contacts: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${collectionContactsDesc}" class="tip"/></th>
	                    	<c:if test="${fn:length(selectedCollection.contactInfoList) == '0'}">
	                    		<td></td></tr>
	                    	</c:if>
	                        <c:forEach items="${selectedCollection.contactInfoList}" var="contactInfo" varStatus="status">
								<c:if test="${status.index gt '0'}">  
	                		 		<tr>

	                   		 	</c:if>
	            		 		<td> 
	                                <div id="name_${contactInfo.name}">
	                                    <a href="#details_${contactInfo.name}" onclick="showContactInfoDetails('${contactInfo.name}');">${contactInfo.name}</a>
	                                </div>
	                                <div class="contactInfoDetails" id="details_${contactInfo.name}">
	                                    <a name="#details_asdf${contactInfo.name}" />
	                                    <a href="#name_${contactInfo.name}" onclick="hideContactInfoDetails('${contactInfo.name}');">${contactInfo.name}</a>
	                                    <table class="contactInfoTable">
	                                        <tr><th>Role:</th><td>${contactInfo.role}</td>
	                                        <tr><th>Email Address:</th><td>${contactInfo.emailAddress}</td>
	                                        <tr><th>Phone Number:</th><td>${contactInfo.phoneNumber}</td>
	                                        <tr><th>Address:</th><td>${contactInfo.physicalAddress.streetAddress} <br>
	                                        ${contactInfo.physicalAddress.city} ${contactInfo.physicalAddress.state} ${contactInfo.physicalAddress.zipCode} ${contactInfo.physicalAddress.country}</td>
	                                    </table>
	                                </div>
	                          	</td></tr>
	                        </c:forEach>
	                    <c:if test="${fn:length(actionBean.citations) > 0}" >
                            <tr>
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="collectionCitationDesc" var="collectionCitationDesc"/>
                                </fmt_rt:bundle>
                                <th colspan="1" rowspan="${fn:length(actionBean.citations)}" align="right">Citation: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${collectionCitationDesc}" class="tip"/> </th>
                                <c:forEach items="${actionBean.citations}" var="citation" varStatus="status">
                                   <c:if test="${status.index gt '0'}">
                                       <tr>
                                   </c:if>
                                   <td><span style="font-weight:bold; font-style:italic;" >${citation.key}</span>: ${citation.value}</td></tr>
                                </c:forEach>
	                    </c:if>
	                    <tr>
	                        <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                               <fmt:message key="collectionDescriptionDesc" var="collectionDescriptionDesc"/>
                            </fmt_rt:bundle>
	                        <th align="right">Description: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${collectionDescriptionDesc}" class="tip"/> </th><td>${selectedCollection.summary}</td>
	                    </tr>
	                    <tr>
	                        <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                               <fmt:message key="collectionAltIdDesc" var="collectionAltIdDesc"/>
                            </fmt_rt:bundle>
	                        <th colspan="1" rowspan="${fn:length(selectedCollection.alternateIds)}" align="right"> <a href="#" class="tip" title="The Alternate ID is something that we need to explain">Alternate ID: </a><img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${collectionAltIdDesc}" class="tip"/></th>
	                    	<c:if test="${fn:length(selectedCollection.alternateIds) == '0'}">
	                    		<td></td></tr>
	                    	</c:if>
	                    	<c:forEach items="${selectedCollection.alternateIds}" var="alternateID" varStatus="status">
	                    		<c:choose>
	                    		 	<c:when test="${status.index == '0'}">
	                    				<td>${alternateID}</td></tr>
	                    			</c:when>
	                    			<c:otherwise>
	                    				<tr><td>${alternateID}</td></tr>
	                    			</c:otherwise>
	                			</c:choose>
	                		</c:forEach>
	                    <tr>
	                        <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                               <fmt:message key="collectionCreatorsDesc" var="collectionCreatorsDesc"/>
                            </fmt_rt:bundle>
	                        <th colspan="1" rowspan="${fn:length(selectedCollection.creators)}" align="right">Creators: <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${collectionCreatorsDesc}" class="tip"/> </th>
	                    	<c:if test="${fn:length(selectedCollection.creators) == '0'}">
	                    		<td></td></tr>
                    		</c:if>
                    		<c:forEach items="${selectedCollection.creators}" var="creator" varStatus="status">
                    			<c:if test="${status.index gt '0'}">
                    				<tr>
                    			</c:if>
                    			                            <c:set var="comma" value=""/>
                                                            <c:if test="${0 != fn:length(creator.suffixes)}">
                                                                <c:set var="comma" value=","/>
                                                            </c:if>
                    			<div>
                    				<td>${creator.prefixes} ${creator.givenNames} ${creator.middleNames} ${creator.familyNames}${comma} ${creator.suffixes}
                    				</td></tr>
                    			</div>
                			</c:forEach>
                			<tr>
                			  <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                <fmt:message key="collectionIsChildDesc" var="collectionIsChildDesc"/>
                              </fmt_rt:bundle>
                			  <th colspan="1" align="right">is Child? <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${collectionIsChildDesc}" class="tip"/></th>
                			  <c:if test="${!empty selectedCollection.parentId}">
                			     <td>Yes</td>
                			  </c:if>
                			  <c:if test="${empty selectedCollection.parentId}">
                                 <td>No</td>
                              </c:if>
                			</tr>
	                </table>
	        </div>
	        

           <span class="SectionHeader">Collection's Metadata Files</span>
	       <c:if test="${!empty actionBean.collectionMetadataFiles}">
        	 <div class="SectionContent">
	           <table class="MetadataFileTable">
	             <tr><th>Name</th><th>Format</th></tr>
	                <c:forEach items="${actionBean.collectionMetadataFiles}" var="file" varStatus="status" >
	                  <tr>
	                    <td><a href="${file.id}">${file.name}</a></td>
	                    <td>${actionBean.selectedCollectionFormatNames[file.metadataFormatId]}</td>
	                    <td><stripes:link beanclass="org.dataconservancy.ui.stripes.MetadataFileActionBean" event="displayMetadataFileForm">
			             		<stripes:param name="parentID" value="${actionBean.selectedCollection.id}"/>
			             		<stripes:param name="redirectUrl" value="${actionBean.redirectUrl}"/>
			             		<stripes:param name="metadataFileID" value="${file.id}"/>
			             		Edit
							</stripes:link></td> 
						<td><stripes:link beanclass="org.dataconservancy.ui.stripes.MetadataFileActionBean" event="deleteMetadataFile">
			             		<stripes:param name="parentID" value="${actionBean.selectedCollection.id}"/>
			             		<stripes:param name="redirectUrl" value="${actionBean.redirectUrl}"/>
			             		<stripes:param name="metadataFileID" value="${file.id}"/>
			             		Delete
							</stripes:link></td>       
	                  </tr>
	                </c:forEach>
	           </table>
	         </div>	   
	       </c:if>
	       
            <c:if test="${actionBean.canUpdateCollection}">
                <div class="SectionContent">
                    <stripes:link beanclass="org.dataconservancy.ui.stripes.MetadataFileActionBean" event="displayMetadataFileForm">
                        <stripes:param name="parentID" value="${actionBean.selectedCollection.id}"/>
                        <stripes:param name="redirectUrl" value="${actionBean.redirectUrl}"/>
                        Add Metadata File
                    </stripes:link>
                </div>
            </c:if>
            <span class="SectionHeader">Content</span>
            <c:if test="${actionBean.isDepositorForCollection}">
    	        <div class="SectionContent">
	                <stripes:link beanclass="org.dataconservancy.ui.stripes.DepositActionBean" event="render">
                        <stripes:param name="currentCollectionId" value="${selectedCollection.id}"/>
                        <stripes:param name="redirectUrl" value="/pages/collection_view.jsp"/>
                        Deposit data
                    </stripes:link>
                </div>
            </c:if>
            
            <c:if test="${actionBean.isAdminForProject}">
            	 <div class="SectionContent">
	                <stripes:link beanclass="org.dataconservancy.ui.stripes.CollectionDataListActionBean" event="renderResults">
                        <stripes:param name="currentCollectionId" value="${selectedCollection.id}"/>
                   		<stripes:param name="page" value="0"/>
                        View Data For Collection
                    </stripes:link>
                </div>
            </c:if>
            <!-- Commented out link to add sub collection, UI support for sub collection (adding, displaying, navigating
            are not fully developed yet -->
            <!-- <div class="SectionContent">
                    <stripes:link beanclass="org.dataconservancy.ui.stripes.AddCollectionActionBean" event="render">
                        <stripes:param name="parentCollectionId" value="${selectedCollection.id}"/>
                        Add Sub-Collection
                    </stripes:link>
                </div>
            -->
	        <stripes:submit name="render" value="Done"/>
        </stripes:form>
        
      </c:when>
      
      <c:otherwise>
        <span class="SectionHeader">Collection deposit status: ${actionBean.selectedCollectionDepositStatusMsg}</span>
        
        <div class="SectionContent">
          ${actionBean.selectedCollectionDepositStatusExplanation}
        </div>
      </c:otherwise>
    </c:choose>
        
      
    </stripes:layout-component>
</stripes:layout-render>