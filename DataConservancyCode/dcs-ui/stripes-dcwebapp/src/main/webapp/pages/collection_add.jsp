<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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

    function addCreatorFields(parentid, creatorid) {
        var parent = document.getElementById(parentid);

        // Find the index for the new id in list of ids

        var index = parent.childElementCount;
        var creator = document.getElementById(creatorid);
        var element = creator.cloneNode(true);
        element.setAttribute("id", "creatorID" + index);
        inputList = element.getElementsByTagName("input");
        
        for (var curIndex = 0; curIndex < inputList.length; curIndex++) {
            inputList[curIndex].name = inputList[curIndex].name.replace("[0]", "[" + index + "]");
            inputList[curIndex].value = "";
        }
      
        //Append the element in page (in span).
        parent.appendChild(element);
    }

    function addField(parentid, type){
        var parent = document.getElementById(parentid);

        // Find the index for the new id in list of ids

        var index = parent.getElementsByTagName("input").length;

        var element = document.createElement("input");

        element.setAttribute("type", "text");

        if( type == "alternateID" ){
            element.setAttribute("style", "width: 320px;");
            element.setAttribute("name", "collection.alternateIds[" + index + "]");
        }else if( type == "creator" ){
            index = parent.childElementCount;
            element.setAttribute("name", "collection.creators[" + index + "].familyNames");
            element.setAttribute("style", "width: 320px; margin-bottom:10px; clear: left");
            element.setAttribute("value", "");
            element.setAttribute("onFocus","if(this.value=='Name of Entity') this.value=''");
        }

        //Append the element in page (in span).
        parent.appendChild(element);
    }
    
    if(document.all && !document.getElementById) {
    	  document.getElementById = function(id) { return document.all[id]; }
    }
  
</script>

<style type="text/css">
#pleaseWait {
  background-color:#A4A4A4;
  width:59.9%;
  min-height:42%;
  height:auto;
  visibility:hidden;
  z-index:0;
  position:absolute;
  left:0;
  top:0;
  padding: 30% 20%;
  text-align:center;
}
</style>

<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
    <stripes:layout-component name="bread">
           <stripes:label name="Project: "/> 
               <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean"
                               title="${actionBean.projectName}" event="viewUserProject">
                               <stripes:param name="selectedProjectId" value="${actionBean.projectId}"/>
                               ${actionBean.projectName}
               </stripes:link>
    </stripes:layout-component>
    <stripes:layout-component name="flash">
       <stripes:messages key="UserInputMessages"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
       Add a Collection for Project
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <stripes:form beanclass="org.dataconservancy.ui.stripes.AddCollectionActionBean" class="AddCollectionForm" onsubmit="var el=document.getElementById('pleaseWait');el.style.visibility='visible';el.style.zIndex='2';">
            <h2 class="SectionHeader">Add a Collection</h2>

            <div class="SectionContent">
                <p>
                    <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                       <fmt:message key="formLabel"/>
                    </fmt_rt:bundle>
                </p>

                <fieldset class="collection">
                    <div class="content-left-col">
                        <div class="tooltip-field-group">
                            <stripes:label name="title" for="title"/>
                            <div class="help-icon">
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="collectionTitleDesc" var="collectionTitleDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${collectionTitleDesc}" class="tip"/>
                            </div>
                            <stripes:text name="collection.title"/>
                            <stripes:errors field="collection.title"/>
                        </div>
                        <div class="tooltip-field-group">
                            <stripes:label name="publicationDate" for="publicationDate"/>
                            <div class="help-icon">
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="collectionPubDateDesc" var="collectionPubDateDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${collectionPubDateDesc}" class="tip"/>
                            </div>
                            <stripes:text name="collection.publicationDate"/>
                            <stripes:errors field="collection.publicationDate"/>
                        </div>
                    </div>
                    <div class="content-right-col">
                        <div class="tooltip-field-group">
                            <stripes:label name="summary" for="summary"/>
                            <div class="help-icon">
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="collectionDescriptionDesc" var="collectionDescriptionDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" title="${collectionDescriptionDesc}" class="tip"/>
                            </div>
                            <stripes:textarea name="collection.summary"/>
                            <stripes:errors field="collection.summary"/>
                        </div>
                    </div>
                    <div class="content-one-col">
                    	<div class="tooltip-field-group">
                            <stripes:label name="* Creators" for="creators" />
                            <div class="help-icon2">   
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="collectionCreatorsDesc" var="collectionCreatorsDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" class="tip" title="${collectionCreatorsDesc}"/>
                            </div>
                        </div>
                        
                        <br />
                        
                        Fields with more than one value should be separated by a space.
                        <br />
                        <br />

                        <div id="parentID">
                        	<c:choose>
                            	<c:when test="${fn:length(collection.creators) == 0}">
                                	<div id="creatorID0" style="clear:both;">
                                    	<div class="creator-prefix">
                                    		<stripes:label name="prefix" for="prefixes"/>
                                        	<stripes:text name="collection.creators[0].prefixes" class="creator-textbox"/>
                                    	</div>

                                    	<div class="creator-namefield">
                                    		<stripes:label name="Given / First Names" for="givenNames"/>
                                        	<stripes:text name="collection.creators[0].givenNames" class="creator-textbox"/>
                                    	</div>

                                    	<div class="creator-namefield">
                                    		<stripes:label name="Middle Names" for="middleNames"/>
                                        	<stripes:text name="collection.creators[0].middleNames" class="creator-textbox"/>
                                    	</div>

                                    	<div class="creator-namefield">
                                    		<stripes:label name="Family / Entity Names" for="familyNames"/>
                                        	<stripes:text name="collection.creators[0].familyNames" class="creator-textbox"/>
                                    	</div>

                                    	<div class="creator-suffix">
                                    		<stripes:label name="suffix" for="suffixes"/>
                                        	<stripes:text name="collection.creators[0].suffixes" class="creator-textbox"/>
                                    	</div>
                                	</div>
                            	</c:when>
                            	<c:otherwise>
                                	<c:forEach items="${collection.creators}" var="creator" varStatus="status">

                                    	<c:choose>
                                        	<c:when test="${(empty creator.prefixes) and (empty creator.givenNames) and (empty creator.middleNames) and (not empty creator.familyNames) and (empty creator.suffixes) }">
                                            	<div id="creatorID${status.index}" style="clear:both;">
                                                	<stripes:text name="collection.creators[${status.index}].familyNames" class="creator-textbox" value="${creator.familyNames}"/>
                                            	</div>
                                        	</c:when>
                                        	<c:otherwise>
                                            	<div id="creatorID${status.index}" style="clear:both;">
                                                	<div class="creator-prefix">
                                                    	<stripes:text name="collection.creators[${status.index}].prefixes" class="creator-textbox" value="${creator.prefixes}"/>
                                                	</div>

                                                	<div class="creator-namefield">
                                                    	<stripes:text name="collection.creators[${status.index}].givenNames" class="creator-textbox" value="${creator.givenNames}"/>
	                                                </div>

    	                                            <div class="creator-namefield">
        	                                            <stripes:text name="collection.creators[${status.index}].middleNames" class="creator-textbox" value="${creator.middleNames}"/>
            	                                    </div>

                	                                <div class="creator-namefield">
                    	                                <stripes:text name="collection.creators[${status.index}].familyNames" class="creator-textbox" value="${creator.familyNames}"/>
                        	                        </div>

                            	                    <div class="creator-suffix">
                                	                    <stripes:text name="collection.creators[${status.index}].suffixes" class="creator-textbox" value="${creator.suffixes}"/>
                                    	            </div>
                                        	    </div>
                                        	</c:otherwise>
                                    	</c:choose>
                                	</c:forEach>
                            	</c:otherwise>
                        	</c:choose>
                        </div>
                        <div style="clear:both;">
                            <stripes:button name="addCreator" value="Add Creator as a Person" onclick="addCreatorFields('parentID', 'creatorID0');" class="linkButton" style="margin-bottom:15px; width:50%; position:relative;" />
                            <stripes:button name="addEntityCreator" value="Add Creator as an Entity" onclick="addField('parentID', 'creator');" class="linkButton" style="margin-bottom:15px; width:50%; position:relative;" />
                        </div>
                        <stripes:errors field="collection.creators[0].familyNames"/>
                    </div>

                    <div class="content-left-col">
                        <div class="label-field-group">
                            <stripes:label name="alternateIds" for="alternateIds"/>
                            <div class="help-icon4">   
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="collectionAlternateIdsDesc" var="collectionAlternateIdsDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" class="tip" title="${collectionAlternateIdsDesc}"/>
                            </div>
                            <div id="alternateID">
                                <c:forEach items="${collection.alternateIds}" var="id" varStatus="status" >
                                    <stripes:text style="width: 320px;" name="collection.alternateIds[${status.index}]" value="${id}"/>
                                </c:forEach>
                            </div>
                            <stripes:button name="addAlternativeID" value="Add More Alternate IDs"
                            onclick="addField('alternateID', 'alternateID');" class="linkButton" style="clear:both; margin-bottom=15px;" />
                        </div>
                    </div>

                </fieldset>
            </div>

            <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
              <fmt:message key="collectionContactsDesc" var="collectionContactsDesc"/>
            </fmt_rt:bundle>
            <h2 class="SectionHeader">Collection's Contact Info <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" class="tip" title="${collectionContactsDesc}"/></h2>
            <div class="SectionContent">
                <!-- Display contact info list here. After each Contact Info entry, provided link to edit or delete the entry.
                If none exists. Display nothing.
                Display link to add more contact info. -->
                <c:if test="${!empty actionBean.collection.contactInfoList}">
                    <table id="contactInfoList">
                        <tr><th>Name</th><th>Roles</th><th>Address</th><th>Email</th><th>Phone</th><th>Action</th></tr>
                        <c:forEach items="${collection.contactInfoList}" var="contactInfo" varStatus="status">
                            <tr>
                                <td id="contactInfoName">${contactInfo.name}</td>
                                <td id="contactInfoRole">${contactInfo.role}</td>
                                <td id="contactInfoAddress">${contactInfo.physicalAddress.streetAddress},
                                    ${contactInfo.physicalAddress.city}
                                    ${contactInfo.physicalAddress.zipCode},
                                    ${contactInfo.physicalAddress.state},
                                    ${contactInfo.physicalAddress.country}
                                </td>
                                <td id="contactInfoEmail">${contactInfo.emailAddress}</td>
                                <td id="contactInfoPhone">${contactInfo.phoneNumber}</td>
                                <td id="contactInfoAction">
                                    <stripes:link beanclass="org.dataconservancy.ui.stripes.AddCollectionActionBean" title="Remove" event="deleteContactInfo">
                                        <stripes:param name="contactInfoIndex" value="${status.index}"/>
                                        [X]
                                    </stripes:link>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:if>

                <stripes:submit name="displayContactInfoForm" value="Add Contact Info" class="linkButton"/>

            </div>

            <div class="SectionContent">
                <stripes:submit name="addCollection" value="Submit Collection"/>
                <stripes:submit name="cancel" value="Cancel"/>
            </div>
        </stripes:form>
        <div id="pleaseWait">
			<strong>Please wait while the collection is being created...</strong> <br/> <img src="../resources/images/ajax-loader.gif">	
		</div>
    </stripes:layout-component>
</stripes:layout-render>
