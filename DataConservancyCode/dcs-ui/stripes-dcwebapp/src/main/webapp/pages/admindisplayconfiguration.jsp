<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
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
        <div class="SectionContent">
            <stripes:link beanclass="org.dataconservancy.ui.stripes.UiConfigurationActionBean"
                          title="Configure Collection Metadata Formats" event="displayMetadataFormatList">
                <fmt:bundle basename="StripesResources" prefix="org.dataconservancy.ui.stripes.UiConfigurationActionBean.">
                    <fmt:message key="configureCollectionMetadataFormats"/>
                </fmt:bundle>
            </stripes:link>
        </div>
    </stripes:layout-component>

</stripes:layout-render>
