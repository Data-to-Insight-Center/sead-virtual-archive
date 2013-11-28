<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
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
<script type="text/javascript">
	function addRow(tableID, offset) {
 
            var table = document.getElementById(tableID);
            var index = table.getElementsByTagName("input").length - offset ;
 			
	 
            var rowCount = table.rows.length;
            var row = table.insertRow(rowCount);
 
            var cell1 = row.insertCell(0);
 
           	var cell2 = row.insertCell(1);
            var element1 = document.createElement("input");
            element1.type = "text";
            element1.name = "userIdsToAdd[" + index + "]"
            cell2.appendChild(element1);
            
  			row.insertCell(2);
  			row.insertCell(3);
  	}
  	function confirmUpdate()
  	{
  		
  	}
</script>

<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
     <stripes:layout-component name="flash">
       <stripes:messages key="updated"/>
     </stripes:layout-component>
     <stripes:layout-component name="success">
       <stripes:messages key="success"/>
     </stripes:layout-component>
	<stripes:layout-component name="contents">

     		<h2 class="SectionHeader">Edit Depositors list for collection: <stripes:label name="${actionBean.selectedCollection.title}" /> </h2>
		<stripes:form beanclass="org.dataconservancy.ui.stripes.UserCollectionsActionBean">
		    <div class="content-one-col">
		    	<div class="SectionContent">
		    		Select depositors to remove. Add new depositors at the end of the table.
		    		<br/>
		    		<br/>
		    		Enter user's email address to use as depositor ID.
		    		<br/>
		    		<br/>
		       		<table id="depositorsTable">
		       			<tr>
		       				<th id="remove">Remove</th>
		       				<th id="depositorId">Depositor ID</th>
		       				<th id="depositorLastName">First Name</th>
		       				<th id="depositorFirstName">Last Name</th>
		       			</tr>
		       			<c:forEach items="${actionBean.depositorsForSelectedCollection}" var="person" >
		       			<tr>
		       			    <c:set var="removable" value="false" />
              			    <c:forEach var="item" items="${actionBean.removableDepositors}">
                			    <c:if test="${item.emailAddress eq person.emailAddress}">
                	    		    <c:set var="removable" value="true" />
                			    </c:if>
              			    </c:forEach>
              			    <c:choose>
            		 			<c:when test="${removable}">
              			        	<td><stripes:checkbox name="userIdsToRemove" value="${person.emailAddress}"/></td>
              			        </c:when>
            					<c:otherwise>
            						<td><stripes:checkbox name="userIdsToRemove" value="${person.emailAddress}" style="display:none"/></td>
		       					</c:otherwise>
	       					</c:choose>
		       				<td>${person.emailAddress}</td>
		       				<td>${person.firstNames}</td>
		       				<td>${person.lastNames}</td>
		       			</tr>
		       			</c:forEach>
		       			<c:forEach begin="0" end="0" var="index" >
		       			<tr>
		       				<td></td>
		       				<td>
		       				    <stripes:text name="userIdsToAdd[${index}]"/>
						    	<stripes:errors field="userIdsToAdd[${index}]"/>
		       				</td>
		       				<td></td>
		       				<td></td>
		       			</tr>
		       			</c:forEach>
		    		</table>
		    		<stripes:button name="addAdditionalDepositor" value="Add More Depositors" onclick="addRow('depositorsTable',${actionBean.selectedCollectionDepositorListSize});" class="linkButton" style="clear:both; margin-bottom=15px;" />
		    		<stripes:hidden name="selectedCollectionId" value="${collection.id}" />
		    	</div>
		    	<div class="SectionContent">
		       		<stripes:submit name="editDepositors" value="Save Changes" />
			    </div>
			    <div class="SectionContent">	
		       		<stripes:submit name="render" value="Back to Collections List" />
			    </div>
			    
		    </div>
	    </stripes:form>
  	</stripes:layout-component>
</stripes:layout-render>
	