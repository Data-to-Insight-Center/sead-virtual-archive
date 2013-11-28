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
<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
    <stripes:layout-component name="flash">
       <stripes:messages key="UserInputMessages"/>
     </stripes:layout-component>
     <stripes:layout-component name="success">
       <stripes:messages key="Success"/>
     </stripes:layout-component>
    <stripes:layout-component name="contents">
        <div class="SectionContent">
            <h3>Select a metadata file to validate against schema: ${actionBean.metadataFormatName}</h3>
            <stripes:form beanclass="org.dataconservancy.ui.stripes.ValidatingMetadataFileActionBean" class="TestMetadataActionBean">
                <fieldset>
                    <div class="label-field-group">
                        <stripes:file name="sampleMetadataFile"/>
                        <stripes:errors field="sampleMetadataFile"/>
                    </div>
                </fieldset>
                <stripes:hidden name="metadataFormatName" value="${actionBean.metadataFormatName}"/>
                <stripes:hidden name="metadataFormatId" value="${actionBean.metadataFormatId}"/>
                <stripes:hidden name="redirectUrl" value="${actionBean.redirectUrl}"/>
                <stripes:submit value="Validate" name="validate" style="clear:both; margin-bottom=15px;" />
                <stripes:submit value="Done" name="done" style="clear:both; margin-bottom=15px;" />
            </stripes:form>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
