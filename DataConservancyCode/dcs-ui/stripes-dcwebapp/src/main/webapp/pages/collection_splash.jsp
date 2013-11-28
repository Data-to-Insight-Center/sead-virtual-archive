<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
    <stripes:layout-component name="contents">

        <span class="SectionHeader">Collection Summary</span>
        <div class="SectionContent">
            ${actionBean.collection.summary}
        </div>
        <span class="SectionHeader">Collection Details</span>
        <div class="SectionContent">
            <table class="CollectionDetailsTable">
                <c:if test="${actionBean.collection.citableLocator != null}">
                    <tr>
                        <th><fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}."><fmt:message key="citableLocator"/></fmt_rt:bundle></th>
                        <td>${actionBean.collection.citableLocator}</td>
                    </tr>
                </c:if>
                <tr><th><fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}."><fmt:message key="associatedProject"/></fmt_rt:bundle></th><td>${actionBean.project.name}</td></tr>
                <c:if test="${actionBean.collectionPublicationDate != null}">
                    <tr>
                        <th><fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}."><fmt:message key="publicationDate"/></fmt_rt:bundle></th>
                        <td>${actionBean.collectionPublicationDate}</td>
                    </tr>
                </c:if>
                <c:if test="${!empty actionBean.collection.contactInfoList}">
                    <tr>
                        <th colspan="1" rowspan="${fn:length(actionBean.collection.contactInfoList)}"><fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}."><fmt:message key="contacts"/></fmt_rt:bundle></th>

                        <c:forEach items="${actionBean.collection.contactInfoList}" var="contactInfo" varStatus="status">
                            <c:if test="${status.index != 0}">
                                <tr>
                            </c:if>

                            <td>
                                <div id="name_${contactInfo.name}">
                                    <a href="#" onclick="showContactInfoDetails('${contactInfo.name}');">${contactInfo.name}</a>
                                </div>
                                <div class="contactInfoDetails" id="details_${contactInfo.name}">
                                    <a href="#" onclick="hideContactInfoDetails('${contactInfo.name}');">${contactInfo.name}</a>
                                    <table class="contactInfoTable">
                                        <tr><th>Role:</th><td>${contactInfo.role}</td>
                                        <tr><th>Email Address:</th><td>${contactInfo.emailAddress}</td>
                                        <tr><th>Phone Number:</th><td>${contactInfo.phoneNumber}</td>
                                        <tr>
                                            <th>Address:</th>
                                            <td>
                                                ${contactInfo.physicalAddress.streetAddress}<br />
                                                ${contactInfo.physicalAddress.city}, ${contactInfo.physicalAddress.state}<br />
                                                ${contactInfo.physicalAddress.zipCode}<br />
                                                ${contactInfo.physicalAddress.country}
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                            </td>

                            <c:if test="${status.index != fn:length(actionBean.collection.contactInfoList)}">
                                </tr>
                            </c:if>
                        </c:forEach>
                    </tr>
                </c:if>
                <c:if test="${actionBean.citation != null}">
                    <tr>
                        <th><fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}."><fmt:message key="citation"/></fmt_rt:bundle></th>
                        <td>${actionBean.citation}</td>
                    </tr>
                </c:if>
                <c:if test="${!empty actionBean.collection.alternateIds}">
                    <tr>
                        <th colspan="1" rowspan="${fn:length(actionBean.collection.alternateIds)}"><fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}."><fmt:message key="alternateIds"/></fmt_rt:bundle></th>

                        <c:forEach items="${actionBean.collection.alternateIds}" var="alternateId" varStatus="status">
                            <c:if test="${status.index != 0}">
                                <tr>
                            </c:if>

                            <td>${alternateId}</td>

                            <c:if test="${status.index != fn:length(actionBean.collection.alternateIds) - 1}">
                                </tr>
                            </c:if>
                        </c:forEach>
                    </tr>
                </c:if>
                <c:if test="${!empty actionBean.collection.creators}">
                    <tr>
                        <th colspan="1" rowspan="${fn:length(actionBean.collection.creators)}"><fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}."><fmt:message key="creators"/></fmt_rt:bundle></th>

                        <c:forEach items="${actionBean.collection.creators}" var="creator" varStatus="status">
                            <c:if test="${status.index != 0}">
                                <tr>
                            </c:if>
                            <c:set var="comma" value=""/>
                            <c:if test="${0 != fn:length(creator.suffixes)}">
                                <c:set var="comma" value=","/>
                            </c:if>
                            <td>${creator.prefixes} ${creator.givenNames} ${creator.middleNames} ${creator.familyNames}${comma} ${creator.suffixes}</td>

                            <c:if test="${status.index != fn:length(actionBean.collection.creators) - 1}">
                                </tr>
                            </c:if>
                        </c:forEach>
                    </tr>
                </c:if>
            </table>
        </div>

        <c:if test="${!empty actionBean.collectionMetadataFiles}">
            <span class="SectionHeader"><fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}."><fmt:message key="collectionMetadata"/></fmt_rt:bundle></span>
            <div class="SectionContent">
                <table class="MetadataFileTable">
                    <tr>
                        <th><fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}."><fmt:message key="metadataName"/></fmt_rt:bundle></th>
                        <th><fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}."><fmt:message key="metadataFormat"/></fmt_rt:bundle></th>
                    </tr>

                    <c:forEach items="${actionBean.collectionMetadataFiles}" var="file" varStatus="status" >
                        <tr>
                            <td><a href="${file.source}">${file.name}</a></td>
                            <td>${file.format}</td>
                        </tr>
                    </c:forEach>
                </table>
            </div>
        </c:if>

    </stripes:layout-component>
</stripes:layout-render>