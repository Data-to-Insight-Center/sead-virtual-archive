<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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

 <stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
     <stripes:layout-component name="contents">
      <h2 class="SectionHeader">Viewing Package</h2>
          <div class="SectionContent">

                <h3>Files</h3>
                <c:if test="${!empty actionBean.pkg.serialization.filePaths}">
                    <table class="PackageFileListTable">
                        <tr><th>File Path</th><th>Checksum</th></tr>
                        <c:forEach items="${actionBean.pkg.serialization.filePaths}" var="filePath">
                        <tr>
                             <td>${filePath}</td>
                             <td>"${actionBean.pkg.serialization.checksums[filePath][0].value}"</td>
                         </tr>
                         </c:forEach>
                    </table>
                    <p/>
                </c:if>

                <h3>Metadata</h3>
                <c:if test="${!empty actionBean.pkg.serialization.packageMetadata}">
                    <table class="PackageMetadataListTable">
                        <tr><th>Name</th><th>Value</th></tr>
                        <c:forEach items="${actionBean.pkg.serialization.packageMetadata}" var="metadataEntry">
                        <tr>
                             <td>${metadataEntry.key}</td>
                             <td>${metadataEntry.value}</td>
                        </tr>
                        </c:forEach>
                    </table>
                    <p/>
                </c:if>

                <h3>Relationships</h3>
                <c:if test="${!empty actionBean.pkg.description.relationships}">
                <table class="PackageRelationshipsListTable">
                    <tr><th>Subject</th><th>Predicate</th><th>Object</th></tr>
                        <c:forEach items="${actionBean.pkg.description.relationships}" var="relationship">
                        <tr>
                             <td>${relationship.subject}</td>
                             <td>${relationship.predicate}</td>
                             <td>${relationship.object}</td>
                        </tr>
                        </c:forEach>
                    </table>
                </c:if>

          </div>
     </stripes:layout-component>
 </stripes:layout-render>
