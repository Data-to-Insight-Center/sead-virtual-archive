<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
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
        <c:forEach var="message" items="${inform}">
    		<div>${message}</div>
		</c:forEach>
    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
          <c:choose>
               <c:when test="${fn:length(actionBean.project.name) > 0}">
         		Collections for Project: ${actionBean.project.name}
    			</c:when>
                <c:otherwise>
                Collection List
                </c:otherwise>
          </c:choose>
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <div class="SectionContent">
            <c:choose>
            <c:when test="${!empty actionBean.collectionsForUser}">
	            <table class="CollectionsListTable">
	                <tr>
	                   <th>Collection Title</th>
	                   <th>Create date</th>
	                   <th>Depositor</th>
	                   <th>Collection Summary</th>
	                   <th>Data items count</th>
	                   <th>Data Items</th>
	                   <th>is Child?</th>
	                   <th>Actions</th>
	                </tr>
         			<c:forEach items="${actionBean.collectionsForUser}" var="collection">
         			<tr>
              			<td>
              				<stripes:link beanclass="org.dataconservancy.ui.stripes.UserCollectionsActionBean"
              					title="Title" event="viewCollectionDetails">
              					<stripes:param name="selectedCollectionId" value="${collection.id}"/>
              					${collection.title}
          					</stripes:link>
                        </td>
      					<td>${collection.depositDate.year}-${collection.depositDate.monthOfYear}-${collection.depositDate.dayOfMonth}</td>
                        <c:if test="${!empty actionBean.depositorForCollection[collection.id]}">
                        	<td>${actionBean.depositorForCollection[collection.id].firstNames} ${actionBean.depositorForCollection[collection.id].lastNames}</td>
                    	</c:if>
                        <td>${collection.summary}</td>
                        <td>${actionBean.dataItemsCountForCollections[collection.id]}</td>
                        <td>
                            <stripes:link beanclass="org.dataconservancy.ui.stripes.CollectionDataListActionBean" event="renderResults">
                                <stripes:param name="currentCollectionId" value="${collection.id}"/>
                                <stripes:param name="page" value="0"/>
                                View
                            </stripes:link>
                        </td>
                        <c:if test='${!empty collection.parentId}'>
                            <td>Yes</td>
                        </c:if>
                        <c:if test='${empty collection.parentId}'>
                            <td>No</td>                        
                        </c:if>
                        <td>
                            <c:if test='${actionBean.containsUserAsAdministrator[collection.id] == "true" || actionBean.containsUserAsDepositor[collection.id] == "true"}'>
                                <stripes:link beanclass="org.dataconservancy.ui.stripes.DepositActionBean" event="render">
                                <stripes:param name="currentCollectionId" value="${collection.id}"/>
                                <stripes:param name="redirectUrl" value="/pages/usercollections.jsp"/>
                                    Deposit data
                                 </stripes:link>
                            </c:if>
                            <c:if test= "${actionBean.containsUserAsAdministrator[collection.id] == 'true'}">
                                |
                                <stripes:link beanclass="org.dataconservancy.ui.stripes.UserCollectionsActionBean"
                                    title="Edit" event="editCollectionDepositors"><stripes:param name="selectedCollectionId" value="${collection.id}"/>
                                    Edit Depositors
                                </stripes:link>
                            </c:if>
					    </td>
         			</tr>
         			</c:forEach>
       			</table>
       		</c:when>
            <c:otherwise>
            <p>There are no collections to list.</p>
            </c:otherwise>
            </c:choose>
       	</div>
 	</stripes:layout-component>
</stripes:layout-render>
