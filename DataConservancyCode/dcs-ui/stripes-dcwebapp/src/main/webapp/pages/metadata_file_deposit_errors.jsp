<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="fmt_rt" uri="http://java.sun.com/jstl/fmt_rt" %>

<%--
  ~ Copyright 2013 Johns Hopkins University
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
</script>

<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
    <stripes:layout-component name="contents">
        <h2 class="SectionHeader center-message">Metadata Deposit Errors</h2>
        <div class="SectionContent">
            <stripes:form beanclass="org.dataconservancy.ui.stripes.MetadataFileActionBean" class="CollectionMetadataFileForm">
                <fieldset>
                    <stripes:text name="parentID" style="display:none"/>
                    <stripes:text name="redirectUrl" style="display:none"/>
                </fieldset>
                <fieldset>
                    <div>
                        <c:choose>
                            <c:when test="${!empty actionBean.message}">
                                <div class="error">   
                                    <b>${actionBean.message}</b>
                                </div>
                            </c:when>
                        </c:choose>
                    </div>
                    <br/>
                    <br/>
                	<stripes:submit name="cancel" value="Go Back"/>
                </fieldset>
            </stripes:form>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
