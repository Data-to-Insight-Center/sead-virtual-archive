<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="fmt_rt" uri="http://java.sun.com/jstl/fmt_rt" %>
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
<script type = "text/javascript">
    function syncNone(checkBox){
        var checkboxes = document.getElementsByName("disciplineIds");
        if(checkBox.value == "dc:discipline:None"){
            if(checkBox.checked){
                for (var i = 0; i < checkboxes.length; i++) {
                    if (checkboxes[i].value !=  "dc:discipline:None"){
                        checkboxes[i].checked = false;  //clear all "real" boxes
                    }
                }
            }
        } else {
            if(checkBox.checked) {
                //uncheck None box
                for (var i = 0; i < checkboxes.length; i++) {
                    if (checkboxes[i].value ==  "dc:discipline:None"){
                        checkboxes[i].checked = false;  //clear the "None" box
                        break;
                    }
                }
            }
        }
    }
</script>
<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">

    <c:set var="mdf" value="${actionBean.metadataFormat}"/>
    <c:set var="mdfProps" value="${actionBean.mdfProperties}"/>
    <c:set var="booleanOptions" value="${actionBean.booleanOptions}"/>

    <stripes:layout-component name="flash">
       <stripes:messages key="UserInputMessages"/>
    </stripes:layout-component>

    <stripes:layout-component name="pageTitle">
        <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
            <fmt:message key="pageTitle">
                <fmt_rt:param value="${mdf.name}"/>
                <fmt_rt:param value="${mdf.version}"/>
            </fmt:message>
        </fmt_rt:bundle>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <div class="SectionContent">
            <stripes:form beanclass="org.dataconservancy.ui.stripes.EditDisciplineToMetadataFormatMappingActionBean"
                          id="editDisciplineMapping">
                <table>
                    <tr>
                        <th>Applies to Project</th>
                        <th>Applies to Collection</th>
                        <th>Applies to Item</th>
                        <th>Validates?</th>
                    </tr>
                    <tr>
                        <td><stripes:select name="mdfProperties.appliesToProject" value="${mdfProps.appliesToProject}">
                            <stripes:options-collection collection="${booleanOptions}"
                                                            value="booleanValue" label="name"/>
                            </stripes:select></td>
                        <td><stripes:select name="mdfProperties.appliesToCollection" value="${mdfProps.appliesToCollection}">
                                <stripes:options-collection collection="${booleanOptions}"
                                                            value="booleanValue" label="name"/>
                            </stripes:select></td>
                        <td><stripes:select name="mdfProperties.appliesToItem" value="${mdfProps.appliesToItem}">
                                <stripes:options-collection collection="${booleanOptions}"
                                                            value="booleanValue" label="name"/>
                            </stripes:select></td>
                        <td><stripes:select name="mdfProperties.validates" value="${mdfProps.validates}">
                            <stripes:options-collection collection="${booleanOptions}"
                                                            value="booleanValue" label="name"/>
                            </stripes:select></td>
                    </tr>
                </table>

                <table>
                    <tr>
                        <th>Select</th>
                        <th>Discipline</th>
                    </tr>

                    <c:forEach items="${actionBean.allDisciplines}" var="discipline">
                        <tr>
                            <td>
                                <stripes:checkbox name="mdfProperties.disciplineIds" value="${discipline.id}"  onchange="syncNone(this)"/>
                            </td>
                            <td>
                                ${discipline.title}
                            </td>
                        </tr>
                    </c:forEach>

                </table>


                <stripes:hidden name="metadataFormatId" value="${actionBean.metadataFormatId}"/>
                <stripes:submit name="displayCollectionMetadataFormats" value="Cancel"/>
                <stripes:submit name="saveDisciplineMapping" value="Save"/>

            </stripes:form>
        </div>

    </stripes:layout-component>

</stripes:layout-render>
