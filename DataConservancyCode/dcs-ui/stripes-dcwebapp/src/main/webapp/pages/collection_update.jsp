<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

		var element
		if(creator != null){
            element = creator.cloneNode(true);
            element.setAttribute("id", "newCreator");

            var i;
            for(i = 0; i < element.children.length - 1 ; i++){
                var child = element.children[i];
                child.children[0].value = "";
                var name = child.children[0].name;
                child.children[0].name = name.replace("0", index);
            }

            element.children[i].children[0].onclick="setSelectedIndex('selectedCreatorIndex', " + index +  " )";
        } else {
            //set up containing div for a person creator
            element = document.createElement("div");
            element.setAttribute("class","creatorTxtLnkCombo");
            element.setAttribute("id", "newCreator");

            //set up div for prefixs
            var prefixDiv = document.createElement("div");
            prefixDiv.setAttribute("class", "creator-prefix");

            var prefixTxt = document.createElement("input");
            prefixTxt.setAttribute("type", "text");
            prefixTxt.setAttribute("class", "creator-textbox");
            prefixTxt.setAttribute("name", "collection.creators[" + index + "].prefixes");
            prefixDiv.appendChild(prefixTxt);
            element.appendChild(prefixDiv);

            //set up div for given names
            var givenNameDiv = document.createElement("div");
            givenNameDiv.setAttribute("class", "creator-namefield");

            var givenNameTxt = document.createElement("input");
            givenNameTxt.setAttribute("type", "text");
            givenNameTxt.setAttribute("class", "creator-textbox");
            givenNameTxt.setAttribute("name", "collection.creators[" + index + "].givenNames");
            givenNameDiv.appendChild(givenNameTxt);
            element.appendChild(givenNameDiv);

            //set up div for middle names
            var middleNameDiv = document.createElement("div");
            middleNameDiv.setAttribute("class", "creator-namefield");

            var middleNameTxt = document.createElement("input");
            middleNameTxt.setAttribute("type", "text");
            middleNameTxt.setAttribute("class", "creator-textbox");
            middleNameTxt.setAttribute("name", "collection.creators[" + index + "].middleNames");
            middleNameDiv.appendChild(middleNameTxt);
            element.appendChild(middleNameDiv);

            //set up div for family names
            var familyNameDiv = document.createElement("div");
            familyNameDiv.setAttribute("class", "creator-namefield");

            var familyNameTxt = document.createElement("input");
            familyNameTxt.setAttribute("type", "text");
            familyNameTxt.setAttribute("class", "creator-textbox");
            familyNameTxt.setAttribute("name", "collection.creators[" + index + "].familyNames");
            familyNameDiv.appendChild(familyNameTxt);
            element.appendChild(familyNameDiv);

            //set up div for family names
            var suffixDiv = document.createElement("div");
            suffixDiv.setAttribute("class", "creator-suffix");

            var suffixTxt = document.createElement("input");
            suffixTxt.setAttribute("type", "text");
            suffixTxt.setAttribute("class", "creator-textbox");
            suffixTxt.setAttribute("name", "collection.creators[" + index + "].suffixes");
            suffixDiv.appendChild(suffixTxt);
            element.appendChild(suffixDiv);

            //set up div for action
            var actionDiv = document.createElement("div");
            actionDiv.setAttribute("class", "creator-action");

            var suffixTxt = document.createElement("input");
            suffixTxt.setAttribute("type", "submit");
            suffixTxt.setAttribute("class", "linkButton");
            suffixTxt.setAttribute("name", "deleteCreator");
            suffixTxt.setAttribute("value", "[Remove]");
            suffixTxt.setAttribute("onclick", "setSelectedIndex('selectedCreatorIndex'," + index + ");");
            actionDiv.appendChild(suffixTxt);
            element.appendChild(actionDiv);
        }

	    //Append the element in page (in span).
	    parent.appendChild(element);
	}

	function addField(parentid, type){
		var parent = document.getElementById(parentid);
        var enclosingDiv = document.createElement("div");

	    // Find the index for the new id in list of ids

		var index = parent.childElementCount;

        //creating input text box element
	    var element = document.createElement("input");

	    element.setAttribute("type", "text");

	     //creating  associated remove link
        var removeLink = document.createElement('input');
        removeLink.setAttribute("type", "submit");
        removeLink.setAttribute("class", "linkButton");
        removeLink.setAttribute("value", "[Remove]");

        //define text box div
        var textBoxDiv = document.createElement("div");

        //define remove link div
	   	var removeLinkDiv= document.createElement("div");

	    if( type == "alternateID" ){
	        var classAttribute = document.createAttribute('class');
            classAttribute.value = 'alternateIdTxtLnkCombo';
            enclosingDiv.setAttributeNode(classAttribute);

            //set up textbox div
            textBoxDiv.setAttribute("class","alternateIdTxt");
            element.setAttribute("name", "collection.alternateIds[" + index + "]");
	   		element.setAttribute("style", "clear:left;");

            ///set up remove link div
	   		removeLinkDiv.setAttribute("class","alternateIdRemoveLnk");

 			removeLink.setAttribute("style", "padding-left:20px;");
 			removeLink.setAttribute("name", "deleteAlternateId");
	   		removeLink.setAttribute("onclick", "setSelectedIndex('selectedAlternateIdIndex', " + index + ");");

	   	}else if( type == "creator" ){
            var classAttribute = document.createAttribute('class');
            classAttribute.value = 'entityCreatorTxtLnkCombo';
            enclosingDiv.setAttributeNode(classAttribute)

            //set up textbox div
            textBoxDiv.setAttribute("class","creatorTxt");
            index = parent.childElementCount;
			element.setAttribute("name", "collection.creators[" + index + "].familyNames");
			element.setAttribute("style", "margin-bottom:10px; clear: left");

            ///set up remove link div
            removeLinkDiv.setAttribute("class","creatorRemoveLnk");

 			removeLink.setAttribute("style", "padding-left:20px; padding-top:3px;");
 			removeLink.setAttribute("name", "deleteCreator");
            removeLink.setAttribute("onclick", "setSelectedIndex('selectedCreatorIndex', " + index + ");");
		}

        textBoxDiv.appendChild(element);
        removeLinkDiv.appendChild(removeLink);
        enclosingDiv.appendChild(textBoxDiv);
        enclosingDiv.appendChild(removeLinkDiv);

	    //Append the element in page (in span).
	    parent.appendChild(enclosingDiv);
	}

    function setSelectedIndex(fieldId, index){
        var element = document.getElementById(fieldId);
        element.value = index;
    }
</script>

<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
	<stripes:layout-component name="bread">
           <stripes:label name="Project: "/> 
               <stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean"
                               title="${actionBean.projectForCurrentCollection.name}" event="viewUserProject">
                               <stripes:param name="selectedProjectId" value="${actionBean.projectForCurrentCollection.id}"/>
                               ${actionBean.projectForCurrentCollection.name}
               </stripes:link>
    </stripes:layout-component>
    <stripes:layout-component name="flash">
       <stripes:messages key="UserInputMessages"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
    	Update Collection ${collection.title}
    </stripes:layout-component>
    <stripes:layout-component name="contents">          
        <stripes:form beanclass="${actionBean.class}" class="UpdateCollectionForm">
            <c:set var="collection" scope="page" value='${actionBean.collection}'/>
            <stripes:hidden id="collectionId" name="collectionId"/>
            <stripes:hidden id="selectedContactInfoIndex" name="selectedContactInfoIndex"/>
            <stripes:hidden id="selectedAlternateIdIndex" name="selectedAlternateIdIndex"/>
            <stripes:hidden id="selectedCreatorIndex" name="selectedCreatorIndex"/>
            <h2 class="SectionHeader">Updating Metadata</h2>
			
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
                            <stripes:label name="* Description" for="summary"/>
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
                            <stripes:label name="Creators" for="creators" />
                            <div class="help-icon2">   
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="collectionCreatorsDesc" var="collectionCreatorsDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" class="tip" title="${collectionCreatorsDesc}"/>
                            </div>
                        </div>
 						
                        <br />

                       	<div class="creator-prefix">                       	
                       		<stripes:label name="prefix" for="prefixes"/>
                       	</div>
                       	
                       	<div class="creator-namefield">
                       		<stripes:label name="givenNames" for="givenNames"/>
                       	</div>
                       	
                       	<div class="creator-namefield">
                       		<stripes:label name="middleNames" for="middleNames"/>
                       	</div>
                       	
                       	<div class="creator-namefield">
                       		<stripes:label name="Family / Entity Names" for="familyNames"/>
                       	</div>
                       	
                       	<div class="creator-suffix">
                       		<stripes:label name="suffix" for="suffixes"/>
                       	</div>
                       	<div class="creator-action">
                       		<stripes:label name="action" for="action"/>
                       	</div>
                       	<div id="parentID">

									<c:forEach items="${actionBean.collection.creators}" var="creator" varStatus="status">
									    <c:choose>
									        <c:when test="${not empty actionBean.collection.creators[status.index].givenNames}">
                                                <div class="creatorTxtLnkCombo" id="creatorID${status.index}">
                                                    <div class="creator-prefix">
                                                        <stripes:text name="collection.creators[${status.index}].prefixes" class="creator-textbox" value="${creator}.prefixes"/>
                                                    </div>

                                                    <div class="creator-namefield">
                                                        <stripes:text name="collection.creators[${status.index}].givenNames" class="creator-textbox" value="${creator}.givenNames"/>
                                                    </div>

                                                    <div class="creator-namefield">
                                                        <stripes:text name="collection.creators[${status.index}].middleNames" class="creator-textbox" value="${creator}.middleNames"/>
                                                    </div>

                                                    <div class="creator-namefield">
                                                        <stripes:text name="collection.creators[${status.index}].familyNames" class="creator-textbox" value="${creator}.familyNames"/>
                                                    </div>

                                                    <div class="creator-suffix">
                                                        <stripes:text name="collection.creators[${status.index}].suffixes" class="creator-textbox" value="${creator}.suffixes"/>
                                                    </div>
                                                    <div class="creator-action">
                                                        <stripes:submit name="deleteCreator" value="[Remove]" onclick="setSelectedIndex('selectedCreatorIndex', ${status.index});" class="linkButton"/>
                                                    </div>
                                                </div>
									        </c:when>
									        <c:otherwise>
									            <div class="entityCreatorTxtLnkCombo">
									                <div class="creatorTxt">
                                                        <stripes:text name="collection.creators[${status.index}].familyNames"  value="${creator}.familyNames"  style="clear:left;"/>
                                                    </div>
                                                    <div class="creatorRemoveLnk">
                                                        <stripes:submit name="deleteCreator" value="[Remove]" onclick="setSelectedIndex('selectedCreatorIndex', ${status.index});" class="linkButton"  style="padding-left:20px; padding-top:3px;"/>
                                                    </div>
	                                            </div>
									        </c:otherwise>
									    </c:choose>

			                    	</c:forEach>
                    	</div>

                       	<div class="content-one-col">
                       		<stripes:button name="addCreator" value="Add A Person Creator" onclick="addCreatorFields('parentID', 'creatorID0');" class="linkButton" style="margin-bottom:15px; width:50%;position:relative; " />
                       		<stripes:button name="addEntityCreator" value="Add An Entity Creator" onclick="addField('parentID', 'creator');" class="linkButton" style="margin-bottom:15px; width:50%; position:relative;" />
                       	</div>
                       	<br/>
                       	<br/>
                       	<div>
                       		<stripes:errors field="collection.creators[0].familyNames"/>
                       	</div>
                   	</div>
                   	
                     <div class="content-one-col">
                        <div class="label-field-group">
                            <stripes:label name="alternateIds" for="alternateIds"/>
                             <div class="help-icon2">   
                                <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="collectionAlternateIdsDesc" var="collectionAlternateIdsDesc"/>
                                </fmt_rt:bundle>
                                <img src="${pageContext.request.contextPath}/resources/images/help-icon.png" class="tip" title="${collectionAlternateIdsDesc}"/>
                            </div>
                            <div id="alternateID">
                                <c:forEach items="${actionBean.collection.alternateIds}" var="id" varStatus="status" >
                                    <div class="alternateIdTxtLnkCombo">
                                        <div class="alternateIdTxt">
                                            <stripes:text  name="collection.alternateIds[${status.index}]" value="${id}"/>
                                        </div>
                                        <div class="alternateIdRemoveLnk">
	                                        <stripes:submit name="deleteAlternateId" value="[Remove]" onclick="setSelectedIndex('selectedAlternateIdIndex', ${status.index});" class="linkButton" style="padding-left:20px;"/>
	                                    </div>
                                    </div>
                                </c:forEach>
                            </div>
                            <stripes:button name="addAlternativeID" value="Add More Alternate IDs"
                            onclick="addField('alternateID', 'alternateID');" class="linkButton" style="clear:both; margin-bottom=15px; width:50%" />
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
					    <tr><th colspan="2">Contact Info</th><th>Action</th></tr>
						<c:forEach items="${actionBean.collection.contactInfoList}" var="contactInfo" varStatus="status">
						<tr id="contactInfo${status.index}">
						    <td id="infoColumn">
						        <div class="label-field-group">
                                    <stripes:label name="Name: " for="name"/>
                                    <stripes:text name="collection.contactInfoList[${status.index}].name" value="${contactInfo.name}"/>
                                </div>
                                <div class="label-field-group">
                                    <stripes:label name="Role: " for="role"/>
                                    <stripes:text name="collection.contactInfoList[${status.index}].role" value="${contactInfo.role}"/>
                                </div>
                                <div class="label-field-group">
                                    <stripes:label name="Email: " for="email"/>
                                    <stripes:text name="collection.contactInfoList[${status.index}].emailAddress" value="${contactInfo.emailAddress}"/>
                                </div>
						        <div class="label-field-group">
                                    <stripes:label name="Phone: " for="phone"/>
                                    <stripes:text name="collection.contactInfoList[${status.index}].phoneNumber" value="${contactInfo.phoneNumber}"/>
                                </div>
						    </td>
					        <td id="infoColumn">
					            <div class="label-field-group">
					                <stripes:label name="Street Address: " for="address"/>
					                <stripes:text name="collection.contactInfoList[${status.index}].physicalAddress.streetAddress" value="${contactInfo.physicalAddress.streetAddress}"/>
					            </div>
						        <div class="label-field-group">
                                    <stripes:label name="City: " for="city"/>
					                <stripes:text name="collection.contactInfoList[${status.index}].physicalAddress.city" value="${contactInfo.physicalAddress.city}"/>
				                </div>
						        <div class="label-field-group">
                                    <stripes:label name="Zip code: " for="zipcode"/>
					        	    <stripes:text name="collection.contactInfoList[${status.index}].physicalAddress.zipCode" value="${contactInfo.physicalAddress.zipCode}"/>
					        	</div>
						        <div class="label-field-group">
                                    <stripes:label name="State: " for="state"/>
					        	    <stripes:text name="collection.contactInfoList[${status.index}].physicalAddress.state" value="${contactInfo.physicalAddress.state}"/>
					        	</div>
						        <div class="label-field-group">
                                    <stripes:label name="Country: " for="country"/>
					        	    <stripes:text name="collection.contactInfoList[${status.index}].physicalAddress.country" value="${contactInfo.physicalAddress.country}"/>
					        	</div>
					        </td>

						    <td id="actionColumn">
	                            <stripes:submit name="deleteContactInfo" value="[Remove]" onclick="setSelectedIndex('selectedContactInfoIndex', ${status.index});" class="linkButton"/>
			                </td>
						</tr>
					    </c:forEach>
					</table>
				</c:if>    
	            <stripes:submit name="displayContactInfoForm" value="Add Contact Info" class="linkButton"/>
	        </div>
		 	  <div class="SectionContent">
              	<stripes:submit name="updateCollection" value="Save changes"/>
              </div>
              <div class="SectionContent">
                <stripes:submit name="cancel" value="Cancel"/>
              </div>
         </stripes:form>      
    </stripes:layout-component>
</stripes:layout-render>