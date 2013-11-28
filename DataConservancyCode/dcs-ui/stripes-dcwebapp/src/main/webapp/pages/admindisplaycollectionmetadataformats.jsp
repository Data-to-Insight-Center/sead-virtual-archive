<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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
  function confirmRemove(formatName) {
    var question = confirm("Are you sure you want to remove format " + formatName+  " ?");
    return question;
  }
  </script>

<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">

    <stripes:layout-component name="contents">
        <stripes:form beanclass="org.dataconservancy.ui.stripes.UiConfigurationActionBean" class="UiConfigurationForm">
            <div class="SectionContent">

                <table id="metadataFormatTable" style="width: 107%; table-layout: fixed">
                    <col width="12%">
                    <col width="10%">
                    <col width="20%">
                    <col width="12%">
                    <col width="12%">
                    <col width="12%">
                    <col width="17%">
                    <col width="12%">
                    <col width="15%">
                    <tr>
                        <th><stripes:label id="formatName" name="formatName" title="Format Name"/></th>
                        <th><stripes:label id="version" name="version" title="Version Number of Metadata format"/></th>
                        <th><stripes:label id="schemaURL" name="schemaURL" title="URL to the format's schema"/></th>
                        <th><stripes:label id="project" name="project" title="Applicable at project level?"/></th>
                        <th><stripes:label id="collection" name="collection" title="Applicable at collection level?"/></th>
                        <th><stripes:label id="item" name="item" title="Applicable at item level?"/></th>
                        <th id="disciplineHeader"><stripes:label id="disciplines" name="disciplines" title="Applicable to discipline"/></th>
                        <th><stripes:label id="validate" name="validate" title="Validate?"/></th>
                        <th><stripes:label id="action" name="actions" title="Actions"/></th>
                    </tr>
                    <c:forEach items="${actionBean.metaDataFormatTransportList}" var="metadataFormatTransport" varStatus="status">
                        <c:choose>
                            <c:when test="${!empty metadataFormatTransport}" >
                                <tr>
                                    <td>${metadataFormatTransport.name}</td>
                                    <td>${metadataFormatTransport.version}</td>
                                    <td><stripes:link href="${metadataFormatTransport.schemaURL}" title="Schema Source">${metadataFormatTransport.schemaURL}</stripes:link></td>
                                    <td>${metadataFormatTransport.appliesToProject}</td>
                                    <td>${metadataFormatTransport.appliesToCollection}</td>
                                    <td>${metadataFormatTransport.appliesToItem}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${!empty actionBean.disciplinesForMetadataFormat[metadataFormatTransport.id]}">
                                                <c:forEach items="${actionBean.disciplinesForMetadataFormat[metadataFormatTransport.id]}" var="discipline" varStatus="s">
                                                    <c:choose>
                                                        <c:when test="${!s.last}">
                                                            ${discipline.title},
                                                        </c:when>
                                                        <c:otherwise>
                                                            ${discipline.title}
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                Not Specified
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${metadataFormatTransport.validates}</td>
                                    <td>
                                        <stripes:link beanclass="org.dataconservancy.ui.stripes.EditDisciplineToMetadataFormatMappingActionBean"
                                           title="Edit" event="displayEditDisciplineMapping">
                                           <fmt:bundle basename="StripesResources" prefix="org.dataconservancy.ui.stripes.UiConfigurationActionBean.">
                                               <fmt:message key="displayEditDisciplineMapping"/>
                                           </fmt:bundle>
                                           <stripes:param name="metadataFormatId" value="${metadataFormatTransport.id}"/>
                                           <stripes:param name="disciplineIds" value="${actionBean.disciplineIdsForMetadataFormat[metadataFormatTransport.id]}"/>
                                       </stripes:link> <br><br>
                                       <c:if test="${metadataFormatTransport.name != 'XSD Schema'}">
	                                       <stripes:link beanclass="org.dataconservancy.ui.stripes.UiConfigurationActionBean"
	                                           title="Remove" event="removeFormat" onclick="return confirmRemove('${metadataFormatTransport.name}');">
	                                           <fmt:bundle basename="StripesResources" prefix="org.dataconservancy.ui.stripes.UiConfigurationActionBean.">
	                                               <fmt:message key="removeFormat"/>
	                                           </fmt:bundle>
	                                           <stripes:param name="metadataFormatId" value="${metadataFormatTransport.id}"/>
	                                       </stripes:link> <br><br>                                     
	                                       <stripes:link beanclass="org.dataconservancy.ui.stripes.ValidatingMetadataFileActionBean"
	                                           title="Validate Metadata File" event="validatingMetadataFile">
	                                           <fmt:bundle basename="StripesResources" prefix="org.dataconservancy.ui.stripes.UiConfigurationActionBean.">
	                                               <fmt:message key="validatingMetadataFileFormat"/>
	                                           </fmt:bundle>
	                                           <stripes:param name="metadataFormatName" value="${metadataFormatTransport.name}"/>
	                                           <stripes:param name="metadataFormatId" value="${metadataFormatTransport.id}"/>
	                                           <stripes:param name="redirectUrl" value="displayMetadataFormatList"/>
	                                       </stripes:link><br><br>
                                       </c:if>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:set var="hasNewFormat" value="true" scope="page"/>
                                <fieldset>
                                <tr>
                                    <td>
                                        <stripes:text name="newMetadataFormatTransport.name" value="${metadataFormatTransport.name  }"/>
                                        <stripes:errors field="newMetadataFormatTransport.name"/>
                                    </td>
                                    <td>
                                        <stripes:text name="newMetadataFormatTransport.version"/>
                                        <stripes:errors field="newMetadataFormatTransport.version"/>
                                    </td>
                                    <td>
                                        <stripes:text name="newMetadataFormatTransport.schemaURL"/>
                                        <stripes:errors field="newMetadataFormatTransport.schemaURL"/>
                                    </td>
                                    <td>
                                        <stripes:select name="newMetadataFormatTransport.appliesToProject">
                                            <stripes:options-collection collection="${actionBean.booleanOptions}" value="value" label="name"/>
                                        </stripes:select>
                                        <stripes:errors field="newMetadataFormatTransport.appliesToProject"/>
                                    </td>
                                    <td>
                                        <stripes:select name="newMetadataFormatTransport.appliesToCollection">
                                            <stripes:options-collection collection="${actionBean.booleanOptions}" value="value" label="name"/>
                                        </stripes:select>
                                        <stripes:errors field="newMetadataFormatTransport.appliesToCollection"/>
                                    </td>
                                    <td>
                                        <stripes:select name="newMetadataFormatTransport.appliesToItem">
                                            <stripes:options-collection collection="${actionBean.booleanOptions}" value="value" label="name"/>
                                        </stripes:select>
                                        <stripes:errors field="newMetadataFormatTransport.appliesToItem"/>
                                    </td>
                                    <td>

                                        <c:forEach items="${actionBean.allDisciplines}" var="discipline" varStatus="status">
                                            <c:if test="${discipline.id != 'dc:discipline:None'}">
                                            <stripes:checkbox  name="newMetadataFormatTransport.disciplineIds" value="${discipline.id}" title="${discipline.title}" />
                                            <stripes:label class="checkboxLabel" for="newMetadataFormatTransport.disciplineIds[${status.index}]" name="${discipline.title}"/>
                                            </c:if>
                                        </c:forEach>
                                        <stripes:errors field="newMetadataFormatTransport.allDisciplines"/>
                                    </td>
                                    <td>
                                        <stripes:select name="newMetadataFormatTransport.validates">
                                            <stripes:options-collection collection="${actionBean.booleanOptions}" value="value" label="name"/>
                                        </stripes:select>
                                        <stripes:errors field="newMetadataFormatTransport.validates"/>
                                    </td>
                                    <td></td>
                                </tr>
                                </fieldset>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>

                </table>
            </div>
            <div class="SectionContent">
            </div>
            <div id="formActionDiv" class="SectionContent">
                <c:choose>
                    <c:when test="${pageScope.hasNewFormat==true}">
                        <stripes:submit id="addNewFormat" name="addNewFormat" class="linkButton" style="clear:both; margin-bottom=15px;" />
                        <stripes:submit id="cancel" name="cancel"  class="linkButton" style="clear:both; margin-bottom=15px;" />
                    </c:when>
                    <c:otherwise>
                        <stripes:submit id="addBlankFormat" name="addBlankFormat" class="linkButton" style="clear:both; margin-bottom=15px;" />
                    </c:otherwise>
                </c:choose>
            </div>
        </stripes:form>
    </stripes:layout-component>

</stripes:layout-render>
