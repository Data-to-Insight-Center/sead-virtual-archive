<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
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
        setInterval("refreshData();", 10000);
    }

    function refreshData() {
        var statusList = document.getElementsByName("dataItemRow");
    
        if (statusList.length > 0) {
            var dataItemIds = new Array();
            $.each(statusList, function( key, value ) {
                dataItemIds[key] = value.getAttribute("id");
            });
            $.getJSON("${pageContext.request.contextPath}/adi/data-item-transports.json/data-items", {ids: dataItemIds.join(',')}, function (jsonData) {
                if (dataItemIds.length == jsonData.dataItemTransportList.length) {
                    var centerMessage = document.getElementById("center-message-id");
                    while (centerMessage.children.length != 1) {
                        $(centerMessage.lastChild).remove();
                    }
                    for (var index = 0; index < jsonData.dataItemTransportList.length; index++) {
                        updateDataItemRow(jsonData.dataItemTransportList[index]);
                    }
                } else {
                    var centerMessage = document.getElementById("center-message-id");
                    if (centerMessage.children.length == 1) {
                        $(centerMessage).append('<p><b>Unable to update file list automatically.</b></p>');
                    }
                }
            });
        }
    }
    
    function getExistingDataItemStatus(dataItemElement) {
        var statusString = "";
        for (index = 0; index < dataItemElement.children.length; index++) {
            var curChild = dataItemElement.children[index];
            if (curChild.getAttribute("id").contains("status")) {
                statusString = curChild.textContent;
                break;
            }
        }
        return statusString;
    }
    
    function createFileNameColumn(fileId, fileName) {
        var fileNameElement = document.createElement('td');
        fileNameElement.setAttribute('id', fileId + '-name');
        fileNameElement.setAttribute('title', fileId);
        var breakWordDiv = document.createElement('div');
        breakWordDiv.setAttribute('class', 'break-word');
        breakWordDiv.textContent = fileName;
        fileNameElement.appendChild(breakWordDiv);
        return fileNameElement;
    }
    
    function createFileDepositorColumn(fileId, fileDepositor) {
        var fileDepositorElement = document.createElement('td');
        fileDepositorElement.setAttribute('id', fileId + '-depositor');
        fileDepositorElement.textContent = fileDepositor;
        return fileDepositorElement;
    }
    
    function createFileCreatedDateColumn(fileId, fileCreatedDate) {
        var fileCreatedDateElement = document.createElement('td');
        fileCreatedDateElement.setAttribute('id', fileId + '-created');
        fileCreatedDateElement.textContent = fileCreatedDate;
        return fileCreatedDateElement;
    }
    
    function createFileUpdatedDateColumn(fileId, fileUpdatedDate) {
        var fileUpdatedDateElement = document.createElement('td');
        fileUpdatedDateElement.setAttribute('id', fileId + '-lastUpdated');
        fileUpdatedDateElement.textContent = fileUpdatedDate;
        return fileUpdatedDateElement;
    }
    
    function createFileSizeColumn(fileId, fileSize) {
        var fileSize = parseInt(fileSize) / (1024 * 1024);
        
        if ((fileSize.toFixed(2) - Math.floor(fileSize)) != 0) {
            fileSize = fileSize.toFixed(2);
        } else {
            fileSize = Math.floor(fileSize);
        }
        var fileSizeElement = document.createElement('td');
        fileSizeElement.setAttribute('id', fileId + '-size');
        fileSizeElement.textContent = fileSize;
        return fileSizeElement;
    }
    
    function createFileStatusColumn(fileId, fileStatus) {
        var fileStatusElement = document.createElement('td');
        fileStatusElement.setAttribute('id', fileId + '-status');
        fileStatusElement.textContent = fileStatus;
        return fileStatusElement;
    }
        
    function updateDataItemRow(row) {
        var dataItemId = row.dataItemTransport.dataItem["@id"];
        var dataItemElement = document.getElementById(dataItemId);
        
        $(dataItemElement).empty();
        
        if (row.dataItemTransport.depositStatus != getExistingDataItemStatus(document.getElementById(dataItemId))) {
            for (var index = 0; index < row.dataItemTransport.dataItem.files.length; index++) {
                var fileId = row.dataItemTransport.dataItem.files[index]['@id'];
                updateDataItemCell(dataItemId, fileId, dataItemElement, row);
                $(dataItemElement).append(createFileNameColumn(fileId, row.dataItemTransport.dataItem.files[index].name));
                $(dataItemElement).append(createFileDepositorColumn(fileId, row.dataItemTransport.dataItem.depositor['@ref']));
                $(dataItemElement).append(createFileCreatedDateColumn(fileId, row.dataItemTransport.initialDepositDate.date));
                $(dataItemElement).append(createFileUpdatedDateColumn(fileId, row.dataItemTransport.dataItem.depositDate.date));
                $(dataItemElement).append(createFileSizeColumn(fileId, row.dataItemTransport.dataItem.files[index].fileSize));
                $(dataItemElement).append(createFileStatusColumn(fileId, row.dataItemTransport.depositStatus));
            }
        }
    }
    
    function updateDataItemCell(dataItemId, fileId, dataItemElement, row) {
        var dataItemElementId = dataItemId + '-dataItem';
        var element = dataItemElement;
        var tdElement = document.createElement('td');
        
        $(dataItemElement).append(tdElement);
        
        $(tdElement).append('<fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}."><fmt:message key="pendingTip" var="pending"/><fmt:message key="failedTip" var="failed"/></fmt_rt:bundle>');
    
        if (row.dataItemTransport.depositStatus == 'PENDING'){
            $(tdElement).append('<div hidden name="PENDING">' + dataItemId + '</div>');
            $(tdElement).append('<img src="${pageContext.request.contextPath}/resources/images/updating-icon.png"  class="tip" title="${pending}">');
        }
    
        if (row.dataItemTransport.depositStatus == 'FAILED') {
            $(tdElement).append('<img src="${pageContext.request.contextPath}/resources/images/updating-icon.png"  class="tip" title="${failed}">');
        }
    
        $(tdElement).append('<div class="break-word"><b>' + row.dataItemTransport.dataItem.name + '</b></div><br>');
    
        if (row.dataItemTransport.depositStatus != 'PENDING') {
            $(tdElement).append('<stripes:link href="' + fileId + '">Download</stripes:link>');
            $(tdElement).append(' | ');
            $(tdElement).append('<stripes:link beanclass="org.dataconservancy.ui.stripes.DepositActionBean" event="renderUpdateForm">Update</stripes:link>');
            var baseHref = tdElement.lastChild.href;
            tdElement.lastChild.href = baseHref + '&datasetToUpdateId=' + dataItemId + '&currentCollectionId=' + "${actionBean.currentCollection.id}";
        } else {
            $(tdElement).append('Download');
            $(tdElement).append(' | ');
            $(tdElement).append('Update');
        }
    
        $(tdElement).append(' | ');
        $(tdElement).append('<stripes:link beanclass="org.dataconservancy.ui.stripes.DataItemSplashActionBean" event="render">Splash Page</stripes:link>');
        var baseHref = tdElement.lastChild.href;
        tdElement.lastChild.href = baseHref + '&dataItemID=' + dataItemId;
    }
</script>

<stripes:layout-render name="/layout/${requestScope['stripes_layout']}/layout.jsp">
    <stripes:layout-component name="contents">
			<stripes:form beanclass="org.dataconservancy.ui.stripes.CollectionDataListActionBean">
				<div class="breadcrumb">					
					<p><b>Project:</b> 
					<stripes:link beanclass="org.dataconservancy.ui.stripes.ProjectActionBean" title="${actionBean.projectForCurrentCollection.name}" 
						event="viewUserProject" class="breadcrumb-link">
        				<stripes:param name="selectedProjectId" value="${actionBean.projectForCurrentCollection.id}"/>
        				${actionBean.projectForCurrentCollection.name}	                               
        			</stripes:link>
        			</p><p>
        			<b>Collection:</b> 
        			<stripes:link beanclass="org.dataconservancy.ui.stripes.UserCollectionsActionBean" title="${actionBean.currentCollection.title}" 
        				event="viewCollectionDetails" class="breadcrumb-link">
        				<stripes:param name="selectedCollectionId" value="${actionBean.currentCollection.id}"/>
        				${actionBean.currentCollection.title}	                               
        			</stripes:link>
                    </p>
				</div>
				
				<div id="center-message-id" class="center-message">				
					<b>${actionBean.message}</b>
				</div>
                <div id="collection-list">
                    <div>
                        <c:if test="${fn:length(actionBean.citations) > 0}" >

                               Citation:
                             <c:forEach items="${actionBean.citations}" var="citation" varStatus="status">
                                  <c:if test="${status.index gt '0'}">

                                  </c:if>
                                  <span style="font-weight:bold; font-style:italic;" >${citation.key}</span>: ${citation.value}
                              </c:forEach>
                        </c:if>
                    </div>
                    <c:if test="${!empty actionBean.dataItemTransportList}">
                        <table style="width: 100%; table-layout: fixed">
                        <col width="17%">
                        <col width="17%">
                        <col width="17%">
                        <col width="12%">
                        <col width="12%">
                        <col width="12%">
                        <col width="13%">
                            <tr>
                                <th>Data Item</th>
                                <th>File</th>
                                <th>User</th>
                                <th>Creation Date</th>
                                <th>Last Modified Date</th>
                                <th>Size (MB)</th>
                                <th>Most Recent Update Status</th>
                            </tr>
                            <c:forEach items="${actionBean.dataItemTransportList}" var="transport" varStatus="status">
                            <tr name="dataItemRow" id="${transport.dataItem.id}">
                                <c:forEach items="${transport.dataItem.files}" var="file">
                                    <fmt_rt:bundle basename="StripesResources" prefix="${actionBean.class.name}.">
                                    <fmt:message key="pendingTip" var="pending"/>
                                    <fmt:message key="failedTip" var="failed"/>
                                    </fmt_rt:bundle>
                                    <td id="${transport.dataItem.id}-dataItem">
                                        <c:if test="${transport.depositStatus == 'PENDING'}">
                                           <div hidden name="PENDING">${transport.dataItem.id}</div>
                                           <img src="${pageContext.request.contextPath}/resources/images/updating-icon.png"  class="tip" title="${pending}">
                                        </c:if>
                                        <c:if test="${transport.depositStatus == 'FAILED'}">
                                           <img src="${pageContext.request.contextPath}/resources/images/updating-icon.png"  class="tip" title="${failed}">
                                        </c:if>
                                            <div class="break-word">
                                            <b>${transport.dataItem.name}</b>
                                            </div>
                                            <br>
                                        <c:choose>
                                            <c:when test="${transport.depositStatus != 'PENDING'}">
                                                <stripes:link href="${file.id}">Download</stripes:link>
                                            </c:when>
                                            <c:otherwise>
                                                Download
                                            </c:otherwise>
                                        </c:choose>
                                        |
                                        <c:choose>
                                            <c:when test="${transport.depositStatus == 'PENDING'}">
                                            Update
                                             </c:when>
                                            <c:otherwise>
                                               <stripes:link beanclass="org.dataconservancy.ui.stripes.DepositActionBean" event="renderUpdateForm">
                                               <stripes:param name="currentCollectionId" value="${actionBean.currentCollection.id}" />
                                               <stripes:param name="datasetToUpdateId" value="${transport.dataItem.id}" />Update</stripes:link>
                                            </c:otherwise>
                                        </c:choose>
                                        |
                                        <stripes:link beanclass="org.dataconservancy.ui.stripes.DataItemSplashActionBean" event="render">
                                                                            <stripes:param name="dataItemID" value="${transport.dataItem.id}"/>Splash Page</stripes:link>

                                        </td>
                                    <td id="${file.id}-name" title="${file.id}"><div class="break-word">${file.name}</div></td>
                                    <td id="${file.id}-depositor">${transport.dataItem.depositorId}</td>
                                    <td id="${file.id}-created" title=${transport.initialDepositDate}><joda:format value="${transport.initialDepositDate}" style="SM" /></td>
                                    <td id="${file.id}-lastUpdated" title=${transport.dataItem.depositDate}><joda:format value="${transport.dataItem.depositDate}" style="SM" /></td>
                                    <td id="${file.id}-size"><fmt:formatNumber value="${(file.size) / (1024 * 1024)}" maxFractionDigits="2" /></td>
                                    <td id="${file.id}-status" name=${transport.depositStatus}>${transport.depositStatus}</td>
                                    </c:forEach>
                                 </tr>
                             </c:forEach>
                        </table>
                    </c:if>
                </div>
				<c:if test="${actionBean.page > '0'}">
						<stripes:link beanclass="org.dataconservancy.ui.stripes.CollectionDataListActionBean" event="renderResults"
							class="footer-page-link-left">
							<stripes:param name="currentCollectionId" value="${actionBean.currentCollectionId}"/>
				        	<stripes:param name="page" value="${actionBean.page - 1}"/>
				           	Previous
				        </stripes:link>
				</c:if>
				<c:if test="${actionBean.page < actionBean.lastPage}">
				    	<stripes:link beanclass="org.dataconservancy.ui.stripes.CollectionDataListActionBean" event="nextPage"
				    		class="footer-page-link-right">
				    		<stripes:param name="page" value="${actionBean.page + 1}"/>
				    		<stripes:param name="currentCollectionId" value="${actionBean.currentCollectionId}"/>
							Next
						</stripes:link>
				</c:if>
			</stripes:form>
    </stripes:layout-component>
</stripes:layout-render>
