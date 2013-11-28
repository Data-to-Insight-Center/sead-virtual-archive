<!--
  Copyright 2012 Johns Hopkins University

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  -->
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<stripes:layout-render  name="/layout/${requestScope['stripes_layout']}/layout.jsp">
    <stripes:layout-component name="flash">
       <stripes:messages key="UserInputMessages"/>
     </stripes:layout-component>

        <stripes:layout-component name="pageTitle">
        <c:choose>
            <c:when test="${!empty actionBean.project}">
     		Collections for ${actionBean.project.name} Project
			</c:when>
            <c:otherwise>
                The requested project could not be retrieved from the archive.
            </c:otherwise>
        </c:choose>
     	</stripes:layout-component>
    <stripes:layout-component name="contents">

        <div class="SectionContent">
            <c:choose>
                <c:when test="${!empty actionBean.dataTrucks}">
                    <stripes:form partial="true" beanclass="org.dataconservancy.ui.stripes.ProjectCollectionsActionBean">
	                <table class="CollectionsListTable">
	                    <tr>
         			        <th>Title</th>
         			        <th>Create date</th>
         			         <th>Depositor</th>
         			         <th>Collection summary</th>
         			         <th>Data items count</th>
         			         <th>Data Items</th>
         			         <th>Actions</th>
         			    </tr>
         			<c:forEach items="${actionBean.dataTrucks}" var="collection">
                         <tr>
                             <td>
                                 <stripes:link beanclass="org.dataconservancy.ui.stripes.UserCollectionsActionBean"
                                    title="Title" event="viewCollectionDetails"><stripes:param name="selectedCollectionId" value="${collection.id}"/>
                                    ${collection.title}
                                 </stripes:link>
          					 </td>
                             <td>${collection.date}</td>
                             <td>${collection.depositorId}</td>
                             <td>${collection.summary}</td>
                             <td>${collection.count}</td>
                             <td>
                             	<stripes:link beanclass="org.dataconservancy.ui.stripes.CollectionDataListActionBean" event="renderResults">
                             		<stripes:param name="currentCollectionId" value="${collection.id}"/>
                             		<stripes:param name="page" value="0"/>
                             		View
                         		</stripes:link>
                     		 </td>
                     		 <td>
                                 <stripes:link beanclass="org.dataconservancy.ui.stripes.DepositActionBean" event="render">
                                 <stripes:param name="currentCollectionId" value="${collection.id}"/>
                                         Deposit data
                                 </stripes:link>                                    |
                                 <stripes:link beanclass="org.dataconservancy.ui.stripes.UserCollectionsActionBean"
                                        title="Edit" event="editCollectionDepositors"><stripes:param name="selectedCollectionId" value="${collection.id}"/>
                                        Edit Depositors
                                 </stripes:link>
                             </td>
                         </tr>
         			</c:forEach>
       			    </table>
       			    <!--</stripes:form>-->
       		        </c:when>
       		        <c:otherwise>
       		            <c:if test="${!empty actionBean.project}">
       		            This project does not have any collections.
       		            </c:if>
       		        </c:otherwise>
       		 </c:choose>
       	</div>
       	<div class="SectionContent">
       	    <stripes:link beanclass="org.dataconservancy.ui.stripes.AddCollectionActionBean"
	      	    title="Add Collection" event="displayCollectionForm">
	      	    <stripes:param name="projectId" value="${actionBean.project.id}"/>
	      	    Add Collection
	      	</stripes:link>
       	</div>
 	</stripes:layout-component>
</stripes:layout-render>
