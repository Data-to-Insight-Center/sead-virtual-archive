<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
<script  type="text/javascript">

    window.onload = setupRefresh;
    function setupRefresh() {
        setInterval("refreshData();", 5000);
    }

function refreshData() {
    var status = document.getElementsByName("PENDING");
    //we just refresh the whole page for now
    //later on can loop through list to do partial refreshing
    if (status.length > 0) {
        document.location.reload("true");
    };
}
</script>
<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
    <stripes:layout-component name="contents">
			<stripes:form beanclass="org.dataconservancy.ui.stripes.DataItemSplashActionBean">			
				<c:if test="${actionBean.dataItem != null}">
					<fmt:setBundle basename="/pageText/common_labels" var="commonLabels"/>
					<table>
						<c:if test="${!empty actionBean.dataItem.description}">
							<tr><th><fmt:message key="label.description" bundle="${commonLabels}"/></th><td>${actionBean.dataItem.description}</td></tr>
						</c:if>
						
						<c:if test="${!empty actionBean.depositor}">
							<tr><th><fmt:message key="label.depositor" bundle="${commonLabels}"/></th><td>${actionBean.depositor.lastNames}, ${actionBean.depositor.firstNames}</td></tr>
						</c:if>
						
						<c:if test="${!empty actionBean.dataItem.depositDate}">
							<tr><th><fmt:message key="label.deposit_date" bundle="${commonLabels}"/></th><td><fmt:formatDate value="${actionBean.depositDate}" pattern="MM/dd/yyyy"/></td></tr>
						</c:if>
						
						<c:if test="${!empty actionBean.dataItem.files}">
							<tr><th><fmt:message key="label.files" bundle="${commonLabels}"/></th>
								<td>
									<c:forEach items="${actionBean.dataItem.files}" var="file">
										<table>
											<c:if test="${!empty file.name}">
												<tr><th><fmt:message key="label.file_name" bundle="${commonLabels}"/></th><td>${file.name}</td></tr>
											</c:if>
											
											<c:if test="${!empty file.size}">
												<tr><th><fmt:message key="label.file_size" bundle="${commonLabels}"/></th><td>${file.size/1000} KB</td></tr>
											</c:if>
											<c:if test="${!empty file.id}">
                                            <c:choose>
											<c:when test="${actionBean.depositStatus != 'PENDING'}">
												<tr><th><fmt:message key="label.download" bundle="${commonLabels}"/></th><td><stripes:link href="${file.id}">
					            						${file.id}                              
					            					</stripes:link></td></tr>
					            			</c:when>
					            			<c:otherwise>
					            			         <tr><th><fmt:message key="label.download" bundle="${commonLabels}"/></th>
					            			         <div hidden name="PENDING">$actionBean.dataItem.id</div>
					            			            <td>
                                                             <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                                                  <fmt:message key="pendingDownloadMessage"/>
                                                             </fmt_rt:bundle>
   					            			            </td>
   					            			         </tr>
					            			</c:otherwise>
					            			</c:choose>
					            			</c:if>
					            		</table>
					            	</c:forEach>
				            	</td>
				            </tr>
			            </c:if>		            					
					</table>
				</c:if>


			</stripes:form>
    </stripes:layout-component>
</stripes:layout-render>